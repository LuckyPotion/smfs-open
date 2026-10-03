package com.xie.smfs.event;

import com.xie.smfs.Smfs;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.config.GhostRespawnConfig;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.effect.SpiritSurgeStatusEffect;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.MineralGhostEntity;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.item.ScapegoatGhostItem;
import com.xie.smfs.item.SpiritWeapon;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.GhostDreamManager;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.manager.GhostSpawnManager;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModSounds;
import com.xie.smfs.util.GhostUtils;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents.StopSleeping;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDamage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDeath;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.After;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;

public class ModEvents {
   public static final AtomicBoolean processingSpiritDamage = new AtomicBoolean(false);
   public static final Map<UUID, Float> preSpiritWeaponAttackHealth = new HashMap<>();
   private static final CopyOnWriteArrayList<SpiritDamageCallback> spiritDamageCallbacks = new CopyOnWriteArrayList<>();
   private static final Random RANDOM = new Random();
   private static final Map<String, Map<EntityType<?>, Integer>> WORLD_GHOST_SPAWN_TIMERS = new HashMap<>();
   private static final Map<UUID, Long> SPIRIT_WEAPON_COOLDOWN = new HashMap<>();
   private static int ghostCleanupTimer = 0;
   private static final int GHOST_CLEANUP_INTERVAL = 6000;
   private static int lockedGhostCheckTimer = 0;
   private static final int LOCKED_GHOST_CHECK_INTERVAL = 24000;
   private static boolean firstNightPianoPlayed = false;
   private static boolean secondNightBabyCryingPlayed = false;
   private static final int MIDNIGHT_TIME = 18000;

   public static void registerSpiritDamageCallback(SpiritDamageCallback callback) {
      spiritDamageCallbacks.add(callback);
   }

   private static void fireSpiritDamageCallback(PlayerEntity attacker, LivingEntity target, float calculatedDamage, float actualDamage, DamageSource source) {
      for (SpiritDamageCallback callback : spiritDamageCallbacks) {
         callback.onSpiritDamage(attacker, target, calculatedDamage, actualDamage, source);
      }
   }

   private static void spawnSpiritAttackParticles(LivingEntity target, float damageAmount) {
      if (!(damageAmount <= 0.0F) && !target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         Vec3d pos = target.getPos();
         serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1.0F, 1.0F);

         for (int i = 0; i < 25; i++) {
            double offsetX = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = target.getRandom().nextDouble() * 1.5 + 0.5;
            double offsetZ = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double velocityX = (target.getRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = target.getRandom().nextDouble() * 0.3 + 0.1;
            double velocityZ = (target.getRandom().nextDouble() - 0.5) * 0.2;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
         }
      }
   }

   public static void registerEvents() {
      ServerLivingEntityEvents.ALLOW_DEATH.register((AllowDeath)(entity, source, damageAmount) -> {
         if (entity instanceof ServerPlayerEntity player) {
            GhostDomainManager.checkRevivalDegree(player);
         }

         return true;
      });
      ServerLivingEntityEvents.ALLOW_DAMAGE.register((AllowDamage)(entity, source, amount) -> {
         if (processingSpiritDamage.get()) {
            return true;
         }

         if (entity instanceof PlayerEntity player) {
            if (ScapegoatGhostItem.handleScapegoatPassiveSkill(player, source, amount)) {
               return false;
            }

            if (source.isOf(ModDamageSources.GHOST)) {
               return true;
            }

            if (source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.ON_FIRE) || source.isOf(DamageTypes.LAVA)) {
               if (player.hasStatusEffect(ModEffects.SILENCE) || player.hasStatusEffect(ModEffects.DREAM)) {
                  return true;
               }

               if (GhostDomainManager.hasValidGhostFire(player)) {
                  return false;
               }
            }
         }

         if (source.getAttacker() instanceof PlayerEntity attacker) {
            if (entity instanceof PlayerEntity targetPlayer) {
               return handlePlayerVsPlayerDamage(attacker, targetPlayer, source, amount);
            }

            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
               return handlePlayerVsEntityDamage(attacker, entity, source, amount);
            }
         }

         if (entity instanceof PlayerEntity targetPlayer && source.getAttacker() instanceof LivingEntity attacker) {
            handlePlayerAttackedPassiveEffect(targetPlayer, attacker, source);
         }

         return true;
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.isClient()) {
            RegistryKey<World> worldKey = world.getRegistryKey();
            String worldKeyStr = worldKey.getValue().toString();
            boolean isOverworld = worldKey.equals(World.OVERWORLD);
            boolean isSpiritRealm = worldKey.equals(Smfs.SPIRIT_REALM_DIMENSION);
            if (!isOverworld && !isSpiritRealm) {
               return;
            }

            Map<EntityType<?>, Integer> worldTimers = WORLD_GHOST_SPAWN_TIMERS.computeIfAbsent(worldKeyStr, k -> new HashMap<>());

            for (EntityType<?> ghostType : GhostSpawnManager.getSpawnedGhostTypes()) {
               int currentTimer = worldTimers.getOrDefault(ghostType, 0);
               if (++currentTimer >= GhostSpawnManager.getSpawnInterval(ghostType)) {
                  worldTimers.put(ghostType, 0);
                  trySpawnGhost(world, ghostType);
               } else {
                  worldTimers.put(ghostType, currentTimer);
               }
            }

            checkFirstNightMidnightSound(world);
            checkSecondNightBabyCryingSound(world);
            checkLockedGhostExpiration(world);
         }
      });
      PlayerBlockBreakEvents.AFTER.register((After)(world, player, pos, state, blockEntity) -> {
         if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            handleMiningEventForMineralGhost(serverWorld, player, pos, state);
         }
      });
      EntitySleepEvents.STOP_SLEEPING.register((StopSleeping)(entity, sleepingPos) -> {
         if (entity instanceof ServerPlayerEntity player && !player.getWorld().isClient() && player.getWorld().getRegistryKey() == World.OVERWORLD) {
            if (PlayerEvents.hasGhostType(player, "ghost_dream")) {
               return;
            }

            WorldConfig worldConfig = WorldConfig.getInstance(player.getWorld());
            if (worldConfig.modDifficulty != 2 && PlayerEvents.countOccupiedGhostSlots(player) < 2) {
               return;
            }

            double chance = ModConfig.getInstance().ghostDreamChance;
            if (RANDOM.nextDouble() <= chance) {
               GhostDreamManager.enterGhostDream(player, true);
            }
         }
      });
      ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> {
         if (!alive) {
            GhostDreamManager.onPlayerDeath(newPlayer);
         }
      });
   }

   private static void handleMiningEventForMineralGhost(ServerWorld world, PlayerEntity player, BlockPos pos, BlockState state) {
      GhostRespawnConfig config = ModConfig.getInstance().getGhostRespawnConfig("mineral_ghost");
      if (config != null && config.enabled) {
         long worldTime = world.getTimeOfDay();
         int worldDays = (int)(worldTime / 24000L);
         if (worldDays >= config.respawnDays) {
            if (isMineralBlock(state.getBlock())) {
               if (RANDOM.nextDouble() <= 0.02) {
                  spawnMineralGhost(world, pos);
               }
            }
         }
      }
   }

   private static void spawnMineralGhost(ServerWorld world, BlockPos pos) {
      try {
         MineralGhostEntity mineralGhost = (MineralGhostEntity)ModEntities.MINERAL_GHOST.create(world);
         if (mineralGhost != null) {
            mineralGhost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, RANDOM.nextFloat() * 360.0F, 0.0F);
            mineralGhost.setSpiritualStrength(1000);
            world.spawnEntity(mineralGhost);
            Smfs.LOGGER.debug("矿物鬼在位置 {} 生成", pos.toString());
         }
      } catch (Exception e) {
         Smfs.LOGGER.error("生成矿物鬼时发生错误: {}", e.getMessage());
      }
   }

   private static boolean handlePlayerVsPlayerDamage(PlayerEntity attacker, PlayerEntity target, DamageSource source, float baseAmount) {
      if (!ModConfig.getInstance().enablePlayerSpiritDamage) {
         return true;
      }

      SpiritSurgeStatusEffect.applySpiritSurgeDamage(attacker, target, baseAmount);
      boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(attacker.getMainHandStack());
      boolean isSkillDamage = GhostSkillManager.isSkillDamage(source);
      boolean isCorpseOilSwordAttack = false;
      float corpseOilSwordDamage = 0.0F;
      if (attacker.getMainHandStack().hasNbt() && attacker.getMainHandStack().getNbt().contains("corpse_oil_layers")) {
         int oilLayers = attacker.getMainHandStack().getNbt().getInt("corpse_oil_layers");
         if (oilLayers > 0) {
            isCorpseOilSwordAttack = true;
            corpseOilSwordDamage = attacker.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : baseAmount * 10.0F;
         }
      }

      if (!hasSpiritWeapon && !isSkillDamage && !isCorpseOilSwordAttack) {
         return true;
      }

      NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
      float playerSpiritDamage = attackerData.contains("spiritDamage") ? (float)attackerData.getDouble("spiritDamage") : 0.0F;
      float tempSpiritDamage = attackerData.contains("tempSpiritDamage") ? (float)attackerData.getDouble("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = attackerData.contains("tempSpiritDamageMultiplier")
         ? (float)attackerData.getDouble("tempSpiritDamageMultiplier")
         : 1.0F;
      float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
      float effectiveSpiritDamage = totalSpiritDamage;
      if (hasSpiritWeapon) {
         float weaponDamageBonus = 0.0F;
         float damageMultiplier = 0.5F;
         ItemStack mainHandStack = attacker.getMainHandStack();
         if (SpiritWeapon.isSpiritWeapon(mainHandStack)) {
            SpiritWeapon weapon = (SpiritWeapon)mainHandStack.getItem();
            if (attacker.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
               weaponDamageBonus = 0.0F;
               damageMultiplier = 0.0F;
            } else {
               weaponDamageBonus = weapon.getSpiritDamageBonus();
               damageMultiplier = weapon.getSpiritDamageMultiplier();
            }
         }

         effectiveSpiritDamage = weaponDamageBonus + totalSpiritDamage * damageMultiplier;
      }

      float spiritDamageAmount = effectiveSpiritDamage;
      if (hasSpiritWeapon) {
         long currentTime = System.currentTimeMillis();
         UUID playerUUID = attacker.getUuid();
         Long lastAttackTime = SPIRIT_WEAPON_COOLDOWN.get(playerUUID);
         if (lastAttackTime != null) {
            long timeDiff = currentTime - lastAttackTime;
            if (timeDiff < 1000L) {
               float cooldownMultiplier = (float)timeDiff / 1000.0F;
               if (isCorpseOilSwordAttack) {
                  corpseOilSwordDamage *= cooldownMultiplier;
               } else {
                  spiritDamageAmount *= cooldownMultiplier;
               }
            }
         }

         SPIRIT_WEAPON_COOLDOWN.put(playerUUID, currentTime);
      }

      processingSpiritDamage.set(true);

      try {
         if (isCorpseOilSwordAttack) {
            spawnSpiritAttackParticles(target, corpseOilSwordDamage);
            float actualDamage = PlayerEvents.handleSpiritDamage(target, corpseOilSwordDamage, baseAmount, source);
            fireSpiritDamageCallback(attacker, target, corpseOilSwordDamage, actualDamage, source);
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid()) && actualDamage > 0.0F) {
               attacker.sendMessage(
                  Text.literal("§a对 §f" + target.getName().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(actualDamage) + " §a点灵异伤害"), true
               );
            }
         } else {
            spawnSpiritAttackParticles(target, spiritDamageAmount);
            float actualDamage = PlayerEvents.handleSpiritDamage(target, spiritDamageAmount, baseAmount, source);
            fireSpiritDamageCallback(attacker, target, spiritDamageAmount, actualDamage, source);
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid()) && actualDamage > 0.0F) {
               attacker.sendMessage(
                  Text.literal("§a对 §f" + target.getName().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(actualDamage) + " §a点灵异伤害"), true
               );
            }
         }
      } finally {
         processingSpiritDamage.set(false);
      }

      SpiritSurgeStatusEffect.applySpiritSurgeDamage(attacker, target, baseAmount);
      return false;
   }

   private static boolean handlePlayerVsEntityDamage(PlayerEntity attacker, LivingEntity target, DamageSource source, float baseAmount) {
      SpiritSurgeStatusEffect.applySpiritSurgeDamage(attacker, target, baseAmount);
      boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(attacker.getMainHandStack());
      boolean isSkillDamage = GhostSkillManager.isSkillDamage(source);
      boolean isCorpseOilSwordAttack = false;
      float corpseOilSwordDamage = 0.0F;
      if (attacker.getMainHandStack().hasNbt() && attacker.getMainHandStack().getNbt().contains("corpse_oil_layers")) {
         int oilLayers = attacker.getMainHandStack().getNbt().getInt("corpse_oil_layers");
         if (oilLayers > 0) {
            isCorpseOilSwordAttack = true;
            corpseOilSwordDamage = attacker.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : baseAmount * 10.0F;
         }
      }

      if (!hasSpiritWeapon && !isSkillDamage && !isCorpseOilSwordAttack) {
         return true;
      }

      NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
      float playerSpiritDamage = attackerData.contains("spiritDamage") ? (float)attackerData.getDouble("spiritDamage") : 0.0F;
      float tempSpiritDamage = attackerData.contains("tempSpiritDamage") ? (float)attackerData.getDouble("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = attackerData.contains("tempSpiritDamageMultiplier")
         ? (float)attackerData.getDouble("tempSpiritDamageMultiplier")
         : 1.0F;
      float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
      float spiritDamageAmount = 0.0F;
      if (hasSpiritWeapon) {
         float weaponDamageBonus = 0.0F;
         float damageMultiplier = 0.5F;
         if (SpiritWeapon.isSpiritWeapon(attacker.getMainHandStack())) {
            SpiritWeapon weapon = (SpiritWeapon)attacker.getMainHandStack().getItem();
            if (attacker.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
               weaponDamageBonus = 0.0F;
               damageMultiplier = 0.0F;
            } else {
               weaponDamageBonus = weapon.getSpiritDamageBonus();
               damageMultiplier = weapon.getSpiritDamageMultiplier();
            }
         }

         spiritDamageAmount = weaponDamageBonus + totalSpiritDamage * damageMultiplier;
      } else {
         spiritDamageAmount = totalSpiritDamage;
      }

      if (hasSpiritWeapon) {
         long currentTime = System.currentTimeMillis();
         UUID playerUUID = attacker.getUuid();
         Long lastAttackTime = SPIRIT_WEAPON_COOLDOWN.get(playerUUID);
         if (lastAttackTime != null) {
            long timeDiff = currentTime - lastAttackTime;
            if (timeDiff < 1000L) {
               float cooldownMultiplier = (float)timeDiff / 1000.0F;
               if (isCorpseOilSwordAttack) {
                  corpseOilSwordDamage *= cooldownMultiplier;
               } else {
                  spiritDamageAmount *= cooldownMultiplier;
               }
            }
         }

         SPIRIT_WEAPON_COOLDOWN.put(playerUUID, currentTime);
      }

      processingSpiritDamage.set(true);

      try {
         if (isCorpseOilSwordAttack) {
            spawnSpiritAttackParticles(target, corpseOilSwordDamage);
            float healthBefore = target.getHealth();
            target.damage(source, corpseOilSwordDamage);
            float actualDamage = healthBefore - target.getHealth();
            float penetrationThreshold = (float)ModConfig.getInstance().spiritDamagePenetrationThreshold;
            boolean skipCreative = target instanceof PlayerEntity playerTarget
               && (
                  playerTarget.isCreative() && !ModConfig.getInstance().penetrateCreativeMode
                     || playerTarget.isSpectator() && !ModConfig.getInstance().penetrateSpectatorMode
               );
            float threshold = corpseOilSwordDamage * penetrationThreshold;
            if (!skipCreative && actualDamage < threshold && actualDamage >= 0.0F && target.isAlive()) {
               float missingDamage = corpseOilSwordDamage - actualDamage;
               target.setHealth(Math.max(0.0F, target.getHealth() - missingDamage));
            }

            fireSpiritDamageCallback(attacker, target, corpseOilSwordDamage, actualDamage, source);
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid()) && actualDamage > 0.0F) {
               attacker.sendMessage(
                  Text.literal("§a对 §f" + target.getName().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(actualDamage) + " §a点灵异伤害"), true
               );
            }
         } else {
            spawnSpiritAttackParticles(target, spiritDamageAmount);
            float healthBefore = target.getHealth();
            target.damage(source, spiritDamageAmount);
            float actualDamage = healthBefore - target.getHealth();
            float penetrationThreshold2 = (float)ModConfig.getInstance().spiritDamagePenetrationThreshold;
            boolean skipCreative2 = target instanceof PlayerEntity playerTarget
               && (
                  playerTarget.isCreative() && !ModConfig.getInstance().penetrateCreativeMode
                     || playerTarget.isSpectator() && !ModConfig.getInstance().penetrateSpectatorMode
               );
            float threshold2 = spiritDamageAmount * penetrationThreshold2;
            if (!skipCreative2 && actualDamage < threshold2 && actualDamage >= 0.0F && target.isAlive()) {
               float missingDamage = spiritDamageAmount - actualDamage;
               target.setHealth(Math.max(0.0F, target.getHealth() - missingDamage));
            }

            fireSpiritDamageCallback(attacker, target, spiritDamageAmount, actualDamage, source);
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid()) && actualDamage > 0.0F) {
               Float preAttackHealth = preSpiritWeaponAttackHealth.remove(target.getUuid());
               float displayDamage = preAttackHealth != null ? preAttackHealth - target.getHealth() : actualDamage;
               attacker.sendMessage(
                  Text.literal("§a对 §f" + target.getName().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(displayDamage) + " §a点灵异伤害"), true
               );
            }
         }

         if (!target.isAlive() && attacker instanceof ServerPlayerEntity serverPlayer) {
            handleSpiritWeaponKill(serverPlayer, target);
         }
      } finally {
         processingSpiritDamage.set(false);
      }

      SpiritSurgeStatusEffect.applySpiritSurgeDamage(attacker, target, baseAmount);
      return false;
   }

   private static void trySpawnGhost(ServerWorld world, EntityType<?> ghostType) {
      String ghostName = GhostUtils.getGhostDisplayName(ghostType);
      Smfs.LOGGER.debug("开始尝试生成厉鬼: {}, 当前世界：{}", ghostName, world.getRegistryKey());
      boolean canSpawn;
      if (world.getRegistryKey().equals(Smfs.SPIRIT_REALM_DIMENSION)) {
         canSpawn = GhostSpawnManager.canSpawnGhostInSpiritRealm(world, ghostType);
      } else {
         canSpawn = GhostSpawnManager.canSpawnGhostInOverworld(world, ghostType);
      }

      if (!canSpawn) {
         Smfs.LOGGER.debug("厉鬼生成失败: 未找到生成配置或生成条件不满足");
      } else {
         ServerWorld targetWorld;
         if (world.getRegistryKey().equals(Smfs.SPIRIT_REALM_DIMENSION)) {
            targetWorld = world;
            Smfs.LOGGER.debug("在灵异世界生成厉鬼: {}", ghostName);
            List<ServerPlayerEntity> spiritRealmPlayers = targetWorld.getPlayers();
            if (spiritRealmPlayers.isEmpty()) {
               Smfs.LOGGER.debug("厉鬼生成失败: 灵异世界中没有玩家");
               return;
            }
         } else {
            targetWorld = world.getServer().getOverworld();
            Smfs.LOGGER.debug("在主世界生成厉鬼: {}", ghostName);
            if (targetWorld == null) {
               Smfs.LOGGER.error("厉鬼生成失败: 无法获取主世界");
               return;
            }

            List<ServerPlayerEntity> overworldPlayers = targetWorld.getPlayers();
            if (overworldPlayers.isEmpty()) {
               Smfs.LOGGER.debug("厉鬼生成失败: 主世界中没有玩家");
               return;
            }
         }

         List<ServerPlayerEntity> players = targetWorld.getPlayers();
         if (players.isEmpty()) {
            Smfs.LOGGER.debug("厉鬼生成失败: {}中没有玩家", targetWorld.getRegistryKey());
         } else {
            ServerPlayerEntity targetPlayer = players.get(RANDOM.nextInt(players.size()));
            Smfs.LOGGER.debug("选择玩家 {} 作为生成中心", targetPlayer.getName().getString());
            if (!checkSpecialSpawnConditions(targetWorld, ghostType, targetPlayer)) {
               Smfs.LOGGER.debug("厉鬼生成失败: 特殊生成条件不满足");
            } else {
               ModConfig config = ModConfig.getInstance();
               int spawnRadius = config.evilGhostMaxSpawnRange;
               BlockPos spawnPos = GhostSpawnManager.findSuitableSpawnPosition(targetWorld, targetPlayer, spawnRadius);
               if (spawnPos == null) {
                  Smfs.LOGGER.debug("厉鬼生成失败: 未找到合适的生成位置");
               } else {
                  Smfs.LOGGER.debug("找到合适的生成位置: {}", spawnPos.toString());
                  Entity ghostEntity = ghostType.create(targetWorld);
                  if (ghostEntity != null) {
                     ghostEntity.refreshPositionAndAngles(
                        spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, targetWorld.getRandom().nextFloat() * 360.0F, 0.0F
                     );
                     targetWorld.spawnEntity(ghostEntity);
                     Smfs.LOGGER.debug("成功生成厉鬼: {} 在位置: {}", ghostName, spawnPos.toString());
                     GhostSpawnManager.markGhostSpawned(targetWorld, ghostType);
                     if (ghostEntity instanceof GhostEntity ghost) {
                        Smfs.LOGGER.debug("为厉鬼生成3个鬼奴");
                        spawnGhostSlavesForGhost(ghost, 3);
                     }

                     for (PlayerEntity nearbyPlayer : targetWorld.getPlayers()) {
                        if (nearbyPlayer.getBlockPos().getSquaredDistance(spawnPos) <= 256.0) {
                           nearbyPlayer.sendMessage(Text.translatable("event.smfs.ghost_spawn").formatted(Formatting.DARK_PURPLE), true);
                        }
                     }
                  } else {
                     Smfs.LOGGER.error("厉鬼生成失败: 无法创建鬼实体实例");
                  }
               }
            }
         }
      }
   }

   private static BlockPos findSuitableSpawnPosition(ServerWorld world, BlockPos centerPos, int radius) {
      for (int attempt = 0; attempt < 10; attempt++) {
         int x = centerPos.getX() + RANDOM.nextInt(radius * 2) - radius;
         int z = centerPos.getZ() + RANDOM.nextInt(radius * 2) - radius;
         Mutable checkPos = new Mutable(x, centerPos.getY(), z);

         for (int yOffset = 0; yOffset <= 16; yOffset++) {
            checkPos.setY(centerPos.getY() + yOffset);
            if (isValidSpawnPosition(world, checkPos)) {
               return checkPos.toImmutable();
            }

            if (yOffset > 0) {
               checkPos.setY(centerPos.getY() - yOffset);
               if (isValidSpawnPosition(world, checkPos)) {
                  return checkPos.toImmutable();
               }
            }
         }
      }

      return null;
   }

   private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos.down());
      if (!blockState.isSolidBlock(world, pos.down())) {
         return false;
      }

      BlockPos abovePos = pos.up();
      BlockState aboveState = world.getBlockState(abovePos);
      BlockState aboveAboveState = world.getBlockState(abovePos.up());
      return aboveState.isAir() && aboveAboveState.isAir();
   }

   private static boolean checkSpecialSpawnConditions(ServerWorld world, EntityType<?> ghostType, ServerPlayerEntity targetPlayer) {
      BlockPos playerPos = targetPlayer.getBlockPos();
      if (ghostType == ModEntities.VILLAGER_GHOST) {
         boolean hasVillagerNearby = false;
         List<VillagerEntity> villagers = world.getEntitiesByClass(VillagerEntity.class, new Box(playerPos).expand(64.0), villager -> villager.isAlive());
         hasVillagerNearby = !villagers.isEmpty();
         if (!hasVillagerNearby) {
            return false;
         }
      }

      if (ghostType == ModEntities.BOX_GHOST) {
         boolean hasChestNearby = false;

         for (int x = -32; x <= 32; x += 4) {
            for (int z = -32; z <= 32; z += 4) {
               for (int y = -16; y <= 16; y += 4) {
                  BlockPos checkPos = playerPos.add(x, y, z);
                  BlockState blockState = world.getBlockState(checkPos);
                  if (blockState.getBlock() instanceof ChestBlock) {
                     hasChestNearby = true;
                     break;
                  }
               }

               if (hasChestNearby) {
                  break;
               }
            }

            if (hasChestNearby) {
               break;
            }
         }

         if (!hasChestNearby) {
            return false;
         }
      }

      if (ghostType == ModEntities.WATER_GHOST) {
         boolean hasWaterNearby = false;

         for (int x = -48; x <= 48; x += 6) {
            for (int z = -48; z <= 48; z += 6) {
               for (int y = -24; y <= 24; y += 6) {
                  BlockPos checkPos = playerPos.add(x, y, z);
                  FluidState fluidState = world.getFluidState(checkPos);
                  if (fluidState.isIn(FluidTags.WATER)) {
                     hasWaterNearby = true;
                     break;
                  }
               }

               if (hasWaterNearby) {
                  break;
               }
            }

            if (hasWaterNearby) {
               break;
            }
         }

         if (!hasWaterNearby) {
            return false;
         }
      }

      return true;
   }

   private static void handlePlayerAttackedPassiveEffect(PlayerEntity targetPlayer, LivingEntity attacker, DamageSource source) {
      boolean hasPassiveEffect = false;
      if (GhostDomainManager.hasUntouchableGhost(targetPlayer)) {
         hasPassiveEffect = true;
         if (source.isOf(ModDamageSources.GHOST)) {
            return;
         }

         if (targetPlayer.getWorld().isClient()) {
            return;
         }

         StatusEffectInstance spiritErosionEffect = new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 4, false, true, true);
         attacker.addStatusEffect(spiritErosionEffect);
      }

      if (GhostDomainManager.hasQiaomenGhost(targetPlayer)) {
         hasPassiveEffect = true;
         if (source.isOf(ModDamageSources.GHOST)) {
            return;
         }

         if (targetPlayer.getWorld().isClient()) {
            return;
         }

         StatusEffectInstance spiritErosionEffect = new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 60, 1, false, true, true);
         attacker.addStatusEffect(spiritErosionEffect);
      }

      if (hasPassiveEffect && !targetPlayer.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)targetPlayer.getWorld();
         Vec3d pos = attacker.getPos();

         for (int i = 0; i < 15; i++) {
            double offsetX = (targetPlayer.getRandom().nextDouble() - 0.5) * 1.5;
            double offsetY = targetPlayer.getRandom().nextDouble() * 2.0;
            double offsetZ = (targetPlayer.getRandom().nextDouble() - 0.5) * 1.5;
            serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.1, 0.0, 0.1);
         }
      }
   }

   private static void spawnGhostSlavesForGhost(GhostEntity ghost, int count) {
      ServerWorld world = (ServerWorld)ghost.getWorld();
      BlockPos ghostPos = ghost.getBlockPos();

      for (int i = 0; i < count; i++) {
         BlockPos slaveSpawnPos = findSuitableSpawnPosition(world, ghostPos, 10);
         if (slaveSpawnPos != null) {
            try {
               GhostSlaveEntity slave = ghost.spawnGhostSlave();
               if (slave != null) {
                  slave.refreshPositionAndAngles(
                     slaveSpawnPos.getX() + 0.5, slaveSpawnPos.getY(), slaveSpawnPos.getZ() + 0.5, world.getRandom().nextFloat() * 360.0F, 0.0F
                  );
               }
            } catch (Exception var7) {
            }
         }
      }
   }

   private static void checkFirstNightMidnightSound(ServerWorld world) {
      if (world.getRegistryKey().getValue().toString().equals("minecraft:overworld")) {
         if (!firstNightPianoPlayed) {
            long totalTime = world.getTime();
            int currentDay = (int)(totalTime / 24000L);
            if (currentDay == 0) {
               long timeOfDay = world.getTimeOfDay() % 24000L;
               if (timeOfDay == 18000L) {
                  for (ServerPlayerEntity player : world.getPlayers()) {
                     BlockPos playerPos = player.getBlockPos();
                     world.playSound(null, playerPos, ModSounds.BACKGROUND_EERIE_PIANO, SoundCategory.AMBIENT, 1.0F, 1.0F);
                  }

                  firstNightPianoPlayed = true;
                  Smfs.LOGGER.debug("第一天午夜eerie_piano音效已播放");
               }
            }
         }
      }
   }

   private static void checkSecondNightBabyCryingSound(ServerWorld world) {
      if (world.getRegistryKey().getValue().toString().equals("minecraft:overworld")) {
         if (!secondNightBabyCryingPlayed) {
            long totalTime = world.getTime();
            int currentDay = (int)(totalTime / 24000L);
            if (currentDay == 1) {
               long timeOfDay = world.getTimeOfDay() % 24000L;
               if (timeOfDay == 18000L) {
                  for (ServerPlayerEntity player : world.getPlayers()) {
                     BlockPos playerPos = player.getBlockPos();
                     world.playSound(null, playerPos, ModSounds.BACKGROUND_HORROR_BACKGROUND, SoundCategory.AMBIENT, 1.0F, 1.0F);
                  }

                  secondNightBabyCryingPlayed = true;
                  Smfs.LOGGER.debug("第二天午夜horror_background音效已播放");
               }
            }
         }
      }
   }

   private static void checkLockedGhostExpiration(ServerWorld world) {
      if (world.getRegistryKey().equals(World.OVERWORLD)) {
         lockedGhostCheckTimer++;
         if (lockedGhostCheckTimer >= 24000) {
            lockedGhostCheckTimer = 0;
            GhostSpawnManager.checkAndUnlockExpiredGhostTypes(world);
         }
      }
   }

   public static int getGhostSpawnTimer(EntityType<?> ghostType) {
      Map<EntityType<?>, Integer> overworldTimers = WORLD_GHOST_SPAWN_TIMERS.get("minecraft:overworld");
      return overworldTimers != null ? overworldTimers.getOrDefault(ghostType, -1) : -1;
   }

   public static int getGhostSpawnTimer(String dimensionKey, EntityType<?> ghostType) {
      Map<EntityType<?>, Integer> worldTimers = WORLD_GHOST_SPAWN_TIMERS.get(dimensionKey);
      return worldTimers != null ? worldTimers.getOrDefault(ghostType, -1) : -1;
   }

   public static Map<EntityType<?>, Integer> getAllGhostSpawnTimers() {
      Map<EntityType<?>, Integer> overworldTimers = WORLD_GHOST_SPAWN_TIMERS.get("minecraft:overworld");
      return overworldTimers != null ? new HashMap<>(overworldTimers) : new HashMap<>();
   }

   public static Map<EntityType<?>, Integer> getAllGhostSpawnTimers(String dimensionKey) {
      Map<EntityType<?>, Integer> worldTimers = WORLD_GHOST_SPAWN_TIMERS.get(dimensionKey);
      return worldTimers != null ? new HashMap<>(worldTimers) : new HashMap<>();
   }

   public static void setGhostSpawnTimer(EntityType<?> ghostType, int timerValue) {
      Map<EntityType<?>, Integer> overworldTimers = WORLD_GHOST_SPAWN_TIMERS.computeIfAbsent("minecraft:overworld", k -> new HashMap<>());
      overworldTimers.put(ghostType, timerValue);
   }

   public static void setGhostSpawnTimer(String dimensionKey, EntityType<?> ghostType, int timerValue) {
      Map<EntityType<?>, Integer> worldTimers = WORLD_GHOST_SPAWN_TIMERS.computeIfAbsent(dimensionKey, k -> new HashMap<>());
      worldTimers.put(ghostType, timerValue);
   }

   private static void handleSpiritWeaponKill(ServerPlayerEntity player, LivingEntity killedEntity) {
      QuestEventHandler.handleEntityKill(player, killedEntity);
   }

   private static boolean isMineralBlock(Block block) {
      Set<Block> mineralBlocks = Set.of(
         Blocks.COAL_ORE,
         Blocks.DEEPSLATE_COAL_ORE,
         Blocks.IRON_ORE,
         Blocks.DEEPSLATE_IRON_ORE,
         Blocks.COPPER_ORE,
         Blocks.DEEPSLATE_COPPER_ORE,
         Blocks.GOLD_ORE,
         Blocks.DEEPSLATE_GOLD_ORE,
         Blocks.REDSTONE_ORE,
         Blocks.DEEPSLATE_REDSTONE_ORE,
         Blocks.LAPIS_ORE,
         Blocks.DEEPSLATE_LAPIS_ORE,
         Blocks.DIAMOND_ORE,
         Blocks.DEEPSLATE_DIAMOND_ORE,
         Blocks.EMERALD_ORE,
         Blocks.DEEPSLATE_EMERALD_ORE,
         Blocks.NETHER_QUARTZ_ORE,
         Blocks.NETHER_GOLD_ORE,
         Blocks.ANCIENT_DEBRIS,
         ModBlocks.DEFILED_ORE,
         ModBlocks.DEEP_DEFILED_ORE
      );
      return mineralBlocks.contains(block);
   }
}
