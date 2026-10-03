package com.xie.smfs.event;

import com.xie.smfs.Smfs;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.ghost.GhostDreamEntity;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.item.GhostLotItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.GhostDreamManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.manager.QuestManager;
import com.xie.smfs.manager.TutorialManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModEntities;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTickHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(ServerTickHandler.class);
   private static int tickCounter = 0;
   private static int lastDay = -1;
   private static final int MAX_GHOST_DREAM_PER_PLAYER = 10;
   private static final int MIN_SPAWN_COOLDOWN_TICKS = 200;
   private static final int INITIAL_SPAWN_COOLDOWN_TICKS = 1000;
   private static final Map<UUID, Long> LAST_SPAWN_TIME = new HashMap<>();

   public static void register() {
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         enforceGhostDreamRain(world);
         GhostLotItem.tickPendingResults(world);
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         tickCounter++;
         checkDayChange(server);
         if (tickCounter % 60 == 0) {
            updateRevivalDegreeInGhostDomain(server);
         }

         if (tickCounter % 24000 == 0) {
            increaseRevivalDegreeDaily(server);
         }

         if (tickCounter % 200 == 0) {
            updateRedCoffinRevivalEffect(server);
            updatePlayerGhostDomainEffects(server);
            spawnGhostDreamForUnaffectedPlayers(server);
         }

         if (tickCounter % 100 == 0) {
            validatePlayerCoffinStatus(server);
         }

         if (tickCounter % 20 == 0) {
            updatePlayerGoldBlockShelterStatus(server);
            enforceDeadlockRevivalState(server);
            checkPlayerGhostDreamDimension(server);
            applyDreamEffectToGhostDreamPlayers(server);
         }

         GhostDreamManager.tick(server);
      });
   }

   private static void applyDreamEffectToGhostDreamPlayers(MinecraftServer server) {
      ServerWorld ghostDreamWorld = server.getWorld(Smfs.GHOST_DREAM_DIMENSION);
      if (ghostDreamWorld != null) {
         for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION && !player.hasStatusEffect(ModEffects.DREAM)) {
               player.addStatusEffect(new StatusEffectInstance(ModEffects.DREAM, 40, 0, false, false, false));
            }
         }
      }
   }

   private static void checkPlayerGhostDreamDimension(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(player -> {
         if (GhostDreamManager.isInGhostDream(player)) {
            GhostDreamManager.checkPlayerDimension(player);
         }
      });
   }

   private static void checkDayChange(MinecraftServer server) {
      if (server.getOverworld() != null) {
         long totalTime = server.getOverworld().getTime();
         int currentDay = (int)(totalTime / 24000L) + 1;
         if (currentDay != lastDay) {
            lastDay = currentDay;
            server.execute(() -> {
               LOGGER.info("游戏天数已更新至第 {} 天", currentDay);
               int serverDay = TutorialManager.calculateCurrentDay(server.getOverworld());
               LOGGER.info("剧情管理天数统计已更新至第 {} 天", serverDay);
               server.getPlayerManager().getPlayerList().forEach(QuestManager::updateWorldDaysProgress);
            });
         }
      }
   }

   private static void updateRevivalDegreeInGhostDomain(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(PlayerEvents::updateRevivalDegreeInGhostDomain);
   }

   private static void increaseRevivalDegreeDaily(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(player -> {
         PlayerEvents.increaseRevivalDegreeDaily(player);
         distributeGoldSalary(player);
      });
   }

   private static void distributeGoldSalary(PlayerEntity player) {
      int salary = FactionManager.getGoldSalary(player);
      if (salary > 0) {
         ItemStack goldIngots = new ItemStack(Items.GOLD_INGOT, salary);
         if (!player.getInventory().insertStack(goldIngots)) {
            player.dropItem(goldIngots, false);
         }
      }
   }

   private static void updateRedCoffinRevivalEffect(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(CoffinEffectManager::updateRedCoffinRevivalEffect);
   }

   private static void validatePlayerCoffinStatus(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(player -> CoffinEffectManager.validatePlayerCoffinStatus(player, player.getWorld()));
   }

   private static void updatePlayerGoldBlockShelterStatus(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(player -> GoldBlockProtectionManager.updatePlayerGoldBlockShelterStatus(player));
   }

   private static void enforceDeadlockRevivalState(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(player -> PlayerEvents.enforceAllDeadlockRevivalStates(player));
   }

   private static void updatePlayerGhostDomainEffects(MinecraftServer server) {
      server.getPlayerManager().getPlayerList().forEach(player -> {
         if (GhostDomainManager.isGhostDomainActive(player)) {
            int level = GhostDomainManager.getCurrentLevel(player);
            if (level > 0) {
               if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.RED_GHOST_DOMAIN_TARGET, level);
               }

               if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.GREEN_GHOST_DOMAIN_TARGET, level);
               }

               if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.GOLDEN_GHOST_DOMAIN_TARGET, level);
               }

               if (player.hasStatusEffect(ModEffects.THICK_FOG)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.THICK_FOG_TARGET, level);
               }

               if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
               }

               if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.CYAN_GHOST_DOMAIN_TARGET, level);
               }

               if (player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.GRAY_GHOST_DOMAIN_TARGET, level);
               }
            }
         }
      });
   }

   private static void enforceGhostDreamRain(World world) {
      if (world.getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION && world instanceof ServerWorld serverWorld) {
         serverWorld.setWeather(0, 6000, true, false);
      }
   }

   private static void spawnGhostDreamForUnaffectedPlayers(MinecraftServer server) {
      ServerWorld ghostDreamWorld = server.getWorld(Smfs.GHOST_DREAM_DIMENSION);
      if (ghostDreamWorld != null) {
         for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
               boolean hasGhostDream = PlayerEvents.hasGhostType(player, "ghost_dream");
               if (!hasGhostDream) {
                  spawnGhostDreamNearPlayer(ghostDreamWorld, player);
               }
            }
         }
      }
   }

   private static void spawnGhostDreamNearPlayer(ServerWorld world, PlayerEntity player) {
      Vec3d playerPos = player.getPos();
      UUID playerId = player.getUuid();
      long currentTime = world.getTime();
      Long lastSpawnTime = LAST_SPAWN_TIME.get(playerId);
      int currentCooldown = calculateSpawnCooldown(player);
      if (lastSpawnTime == null || currentTime - lastSpawnTime >= currentCooldown) {
         int existingCount = countGhostDreamNearPlayer(world, player);
         if (existingCount < 10) {
            double offsetX = (world.random.nextDouble() - 0.5) * 32.0 + (world.random.nextDouble() - 0.5) * 8.0;
            double offsetZ = (world.random.nextDouble() - 0.5) * 32.0 + (world.random.nextDouble() - 0.5) * 8.0;
            BlockPos spawnPos = new BlockPos((int)(playerPos.x + offsetX), (int)playerPos.y, (int)(playerPos.z + offsetZ));
            if (!world.getBlockState(spawnPos.down()).isSolid()) {
               spawnPos = world.getTopPosition(Type.WORLD_SURFACE, spawnPos);
            }

            if (!isValidSpawnPosition(world, spawnPos)) {
               spawnPos = findValidSpawnPosition(world, spawnPos);
               if (spawnPos == null) {
                  return;
               }
            }

            GhostDreamEntity ghostDream = (GhostDreamEntity)ModEntities.GHOST_DREAM.create(world);
            if (ghostDream != null) {
               ghostDream.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, world.random.nextFloat() * 360.0F, 0.0F);
               ghostDream.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.EVENT, null, null);
               world.spawnEntity(ghostDream);
               LAST_SPAWN_TIME.put(playerId, currentTime);
               LOGGER.debug("在鬼梦维度中为玩家 {} 附近生成了鬼梦生物，当前数量: {}", player.getName().getString(), existingCount + 1);
            }
         }
      }
   }

   private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
      return world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir() && world.getBlockState(pos.up(2)).isAir();
   }

   private static BlockPos findValidSpawnPosition(ServerWorld world, BlockPos centerPos) {
      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            BlockPos pos = centerPos.add(dx, 0, dz);
            if (!world.getBlockState(pos.down()).isSolid()) {
               pos = world.getTopPosition(Type.WORLD_SURFACE, pos);
            }

            if (isValidSpawnPosition(world, pos)) {
               return pos;
            }
         }
      }

      return null;
   }

   private static int calculateSpawnCooldown(PlayerEntity player) {
      long remainingTime = GhostDreamManager.getRemainingGameTime(player);
      long totalTime = GhostDreamManager.getTotalGameTime();
      double remainingRatio = (double)remainingTime / totalTime;
      if (remainingRatio <= 0.7) {
         return 200;
      }

      double progress = (remainingRatio - 0.7) / 0.30000000000000004;
      return (int)(200.0 + 800.0 * progress);
   }

   private static int countGhostDreamNearPlayer(ServerWorld world, PlayerEntity player) {
      Vec3d playerPos = player.getPos();
      int count = 0;

      for (Entity entity : world.getEntitiesByClass(
         GhostDreamEntity.class,
         new Box(playerPos.x - 30.0, playerPos.y - 10.0, playerPos.z - 30.0, playerPos.x + 30.0, playerPos.y + 10.0, playerPos.z + 30.0),
         entityx -> !entityx.isRemoved()
      )) {
         count++;
      }

      return count;
   }
}
