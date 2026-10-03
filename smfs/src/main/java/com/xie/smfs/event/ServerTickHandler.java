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
      ServerWorld ghostDreamWorld = server.method_3847(Smfs.GHOST_DREAM_DIMENSION);
      if (ghostDreamWorld != null) {
         for (PlayerEntity player : server.method_3760().method_14571()) {
            if (player.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION && !player.method_6059(ModEffects.DREAM)) {
               player.method_6092(new StatusEffectInstance(ModEffects.DREAM, 40, 0, false, false, false));
            }
         }
      }
   }

   private static void checkPlayerGhostDreamDimension(MinecraftServer server) {
      server.method_3760().method_14571().forEach(player -> {
         if (GhostDreamManager.isInGhostDream(player)) {
            GhostDreamManager.checkPlayerDimension(player);
         }
      });
   }

   private static void checkDayChange(MinecraftServer server) {
      if (server.method_30002() != null) {
         long totalTime = server.method_30002().method_8510();
         int currentDay = (int)(totalTime / 24000L) + 1;
         if (currentDay != lastDay) {
            lastDay = currentDay;
            server.execute(() -> {
               LOGGER.info("游戏天数已更新至第 {} 天", currentDay);
               int serverDay = TutorialManager.calculateCurrentDay(server.method_30002());
               LOGGER.info("剧情管理天数统计已更新至第 {} 天", serverDay);
               server.method_3760().method_14571().forEach(QuestManager::updateWorldDaysProgress);
            });
         }
      }
   }

   private static void updateRevivalDegreeInGhostDomain(MinecraftServer server) {
      server.method_3760().method_14571().forEach(PlayerEvents::updateRevivalDegreeInGhostDomain);
   }

   private static void increaseRevivalDegreeDaily(MinecraftServer server) {
      server.method_3760().method_14571().forEach(player -> {
         PlayerEvents.increaseRevivalDegreeDaily(player);
         distributeGoldSalary(player);
      });
   }

   private static void distributeGoldSalary(PlayerEntity player) {
      int salary = FactionManager.getGoldSalary(player);
      if (salary > 0) {
         ItemStack goldIngots = new ItemStack(Items.field_8695, salary);
         if (!player.method_31548().method_7394(goldIngots)) {
            player.method_7328(goldIngots, false);
         }
      }
   }

   private static void updateRedCoffinRevivalEffect(MinecraftServer server) {
      server.method_3760().method_14571().forEach(CoffinEffectManager::updateRedCoffinRevivalEffect);
   }

   private static void validatePlayerCoffinStatus(MinecraftServer server) {
      server.method_3760().method_14571().forEach(player -> CoffinEffectManager.validatePlayerCoffinStatus(player, player.method_37908()));
   }

   private static void updatePlayerGoldBlockShelterStatus(MinecraftServer server) {
      server.method_3760().method_14571().forEach(player -> GoldBlockProtectionManager.updatePlayerGoldBlockShelterStatus(player));
   }

   private static void enforceDeadlockRevivalState(MinecraftServer server) {
      server.method_3760().method_14571().forEach(player -> PlayerEvents.enforceAllDeadlockRevivalStates(player));
   }

   private static void updatePlayerGhostDomainEffects(MinecraftServer server) {
      server.method_3760().method_14571().forEach(player -> {
         if (GhostDomainManager.isGhostDomainActive(player)) {
            int level = GhostDomainManager.getCurrentLevel(player);
            if (level > 0) {
               if (player.method_6059(ModEffects.RED_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.RED_GHOST_DOMAIN_TARGET, level);
               }

               if (player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.GREEN_GHOST_DOMAIN_TARGET, level);
               }

               if (player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.GOLDEN_GHOST_DOMAIN_TARGET, level);
               }

               if (player.method_6059(ModEffects.THICK_FOG)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.THICK_FOG_TARGET, level);
               }

               if (player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
               }

               if (player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.CYAN_GHOST_DOMAIN_TARGET, level);
               }

               if (player.method_6059(ModEffects.GRAY_GHOST_DOMAIN)) {
                  GhostDomainManager.applyTargetGhostDomainToOtherPlayers(player, ModEffects.GRAY_GHOST_DOMAIN_TARGET, level);
               }
            }
         }
      });
   }

   private static void enforceGhostDreamRain(World world) {
      if (world.method_27983() == Smfs.GHOST_DREAM_DIMENSION && world instanceof ServerWorld serverWorld) {
         serverWorld.method_27910(0, 6000, true, false);
      }
   }

   private static void spawnGhostDreamForUnaffectedPlayers(MinecraftServer server) {
      ServerWorld ghostDreamWorld = server.method_3847(Smfs.GHOST_DREAM_DIMENSION);
      if (ghostDreamWorld != null) {
         for (PlayerEntity player : server.method_3760().method_14571()) {
            if (player.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
               boolean hasGhostDream = PlayerEvents.hasGhostType(player, "ghost_dream");
               if (!hasGhostDream) {
                  spawnGhostDreamNearPlayer(ghostDreamWorld, player);
               }
            }
         }
      }
   }

   private static void spawnGhostDreamNearPlayer(ServerWorld world, PlayerEntity player) {
      Vec3d playerPos = player.method_19538();
      UUID playerId = player.method_5667();
      long currentTime = world.method_8510();
      Long lastSpawnTime = LAST_SPAWN_TIME.get(playerId);
      int currentCooldown = calculateSpawnCooldown(player);
      if (lastSpawnTime == null || currentTime - lastSpawnTime >= currentCooldown) {
         int existingCount = countGhostDreamNearPlayer(world, player);
         if (existingCount < 10) {
            double offsetX = (world.field_9229.method_43058() - 0.5) * 32.0 + (world.field_9229.method_43058() - 0.5) * 8.0;
            double offsetZ = (world.field_9229.method_43058() - 0.5) * 32.0 + (world.field_9229.method_43058() - 0.5) * 8.0;
            BlockPos spawnPos = new BlockPos((int)(playerPos.field_1352 + offsetX), (int)playerPos.field_1351, (int)(playerPos.field_1350 + offsetZ));
            if (!world.method_8320(spawnPos.method_10074()).method_51367()) {
               spawnPos = world.method_8598(Type.field_13202, spawnPos);
            }

            if (!isValidSpawnPosition(world, spawnPos)) {
               spawnPos = findValidSpawnPosition(world, spawnPos);
               if (spawnPos == null) {
                  return;
               }
            }

            GhostDreamEntity ghostDream = (GhostDreamEntity)ModEntities.GHOST_DREAM.method_5883(world);
            if (ghostDream != null) {
               ghostDream.method_5808(
                  spawnPos.method_10263() + 0.5, spawnPos.method_10264(), spawnPos.method_10260() + 0.5, world.field_9229.method_43057() * 360.0F, 0.0F
               );
               ghostDream.method_5943(world, world.method_8404(spawnPos), SpawnReason.field_16467, null, null);
               world.method_8649(ghostDream);
               LAST_SPAWN_TIME.put(playerId, currentTime);
               LOGGER.debug("在鬼梦维度中为玩家 {} 附近生成了鬼梦生物，当前数量: {}", player.method_5477().getString(), existingCount + 1);
            }
         }
      }
   }

   private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
      return world.method_8320(pos).method_26215()
         && world.method_8320(pos.method_10084()).method_26215()
         && world.method_8320(pos.method_10086(2)).method_26215();
   }

   private static BlockPos findValidSpawnPosition(ServerWorld world, BlockPos centerPos) {
      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            BlockPos pos = centerPos.method_10069(dx, 0, dz);
            if (!world.method_8320(pos.method_10074()).method_51367()) {
               pos = world.method_8598(Type.field_13202, pos);
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
      Vec3d playerPos = player.method_19538();
      int count = 0;

      for (Entity entity : world.method_8390(
         GhostDreamEntity.class,
         new Box(
            playerPos.field_1352 - 30.0,
            playerPos.field_1351 - 10.0,
            playerPos.field_1350 - 30.0,
            playerPos.field_1352 + 30.0,
            playerPos.field_1351 + 10.0,
            playerPos.field_1350 + 30.0
         ),
         entityx -> !entityx.method_31481()
      )) {
         count++;
      }

      return count;
   }
}
