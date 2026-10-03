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
      if (!(damageAmount <= 0.0F) && !target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         Vec3d pos = target.method_19538();
         serverWorld.method_43128(null, pos.field_1352, pos.field_1351, pos.field_1350, SoundEvents.field_14858, SoundCategory.field_15251, 1.0F, 1.0F);

         for (int i = 0; i < 25; i++) {
            double offsetX = (target.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = target.method_6051().method_43058() * 1.5 + 0.5;
            double offsetZ = (target.method_6051().method_43058() - 0.5) * 2.0;
            double velocityX = (target.method_6051().method_43058() - 0.5) * 0.2;
            double velocityY = target.method_6051().method_43058() * 0.3 + 0.1;
            double velocityZ = (target.method_6051().method_43058() - 0.5) * 0.2;
            serverWorld.method_14199(
               ParticleTypes.field_11249, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5
            );
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

            if (source.method_49708(ModDamageSources.GHOST)) {
               return true;
            }

            if (source.method_49708(DamageTypes.field_42335) || source.method_49708(DamageTypes.field_42337) || source.method_49708(DamageTypes.field_42338)) {
               if (player.method_6059(ModEffects.SILENCE) || player.method_6059(ModEffects.DREAM)) {
                  return true;
               }

               if (GhostDomainManager.hasValidGhostFire(player)) {
                  return false;
               }
            }
         }

         if (source.method_5529() instanceof PlayerEntity attacker) {
            if (entity instanceof PlayerEntity targetPlayer) {
               return handlePlayerVsPlayerDamage(attacker, targetPlayer, source, amount);
            }

            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
               return handlePlayerVsEntityDamage(attacker, entity, source, amount);
            }
         }

         if (entity instanceof PlayerEntity targetPlayer && source.method_5529() instanceof LivingEntity attacker) {
            handlePlayerAttackedPassiveEffect(targetPlayer, attacker, source);
         }

         return true;
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (!world.method_8608()) {
            RegistryKey<World> worldKey = world.method_27983();
            String worldKeyStr = worldKey.method_29177().toString();
            boolean isOverworld = worldKey.equals(World.field_25179);
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
         if (!world.method_8608() && world instanceof ServerWorld serverWorld) {
            handleMiningEventForMineralGhost(serverWorld, player, pos, state);
         }
      });
      EntitySleepEvents.STOP_SLEEPING.register((StopSleeping)(entity, sleepingPos) -> {
         if (entity instanceof ServerPlayerEntity player && !player.method_37908().method_8608() && player.method_37908().method_27983() == World.field_25179) {
            if (PlayerEvents.hasGhostType(player, "ghost_dream")) {
               return;
            }

            WorldConfig worldConfig = WorldConfig.getInstance(player.method_37908());
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
         long worldTime = world.method_8532();
         int worldDays = (int)(worldTime / 24000L);
         if (worldDays >= config.respawnDays) {
            if (isMineralBlock(state.method_26204())) {
               if (RANDOM.nextDouble() <= 0.02) {
                  spawnMineralGhost(world, pos);
               }
            }
         }
      }
   }

   private static void spawnMineralGhost(ServerWorld world, BlockPos pos) {
      try {
         MineralGhostEntity mineralGhost = (MineralGhostEntity)ModEntities.MINERAL_GHOST.method_5883(world);
         if (mineralGhost != null) {
            mineralGhost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 1.0, pos.method_10260() + 0.5, RANDOM.nextFloat() * 360.0F, 0.0F);
            mineralGhost.setSpiritualStrength(1000);
            world.method_8649(mineralGhost);
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
      boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(attacker.method_6047());
      boolean isSkillDamage = GhostSkillManager.isSkillDamage(source);
      boolean isCorpseOilSwordAttack = false;
      float corpseOilSwordDamage = 0.0F;
      if (attacker.method_6047().method_7985() && attacker.method_6047().method_7969().method_10545("corpse_oil_layers")) {
         int oilLayers = attacker.method_6047().method_7969().method_10550("corpse_oil_layers");
         if (oilLayers > 0) {
            isCorpseOilSwordAttack = true;
            corpseOilSwordDamage = attacker.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : baseAmount * 10.0F;
         }
      }

      if (!hasSpiritWeapon && !isSkillDamage && !isCorpseOilSwordAttack) {
         return true;
      }

      NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
      float playerSpiritDamage = attackerData.method_10545("spiritDamage") ? (float)attackerData.method_10574("spiritDamage") : 0.0F;
      float tempSpiritDamage = attackerData.method_10545("tempSpiritDamage") ? (float)attackerData.method_10574("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = attackerData.method_10545("tempSpiritDamageMultiplier")
         ? (float)attackerData.method_10574("tempSpiritDamageMultiplier")
         : 1.0F;
      float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
      float effectiveSpiritDamage = totalSpiritDamage;
      if (hasSpiritWeapon) {
         float weaponDamageBonus = 0.0F;
         float damageMultiplier = 0.5F;
         ItemStack mainHandStack = attacker.method_6047();
         if (SpiritWeapon.isSpiritWeapon(mainHandStack)) {
            SpiritWeapon weapon = (SpiritWeapon)mainHandStack.method_7909();
            if (attacker.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
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
         UUID playerUUID = attacker.method_5667();
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
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667()) && actualDamage > 0.0F) {
               attacker.method_7353(
                  Text.method_43470("§a对 §f" + target.method_5477().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(actualDamage) + " §a点灵异伤害"),
                  true
               );
            }
         } else {
            spawnSpiritAttackParticles(target, spiritDamageAmount);
            float actualDamage = PlayerEvents.handleSpiritDamage(target, spiritDamageAmount, baseAmount, source);
            fireSpiritDamageCallback(attacker, target, spiritDamageAmount, actualDamage, source);
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667()) && actualDamage > 0.0F) {
               attacker.method_7353(
                  Text.method_43470("§a对 §f" + target.method_5477().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(actualDamage) + " §a点灵异伤害"),
                  true
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
      boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(attacker.method_6047());
      boolean isSkillDamage = GhostSkillManager.isSkillDamage(source);
      boolean isCorpseOilSwordAttack = false;
      float corpseOilSwordDamage = 0.0F;
      if (attacker.method_6047().method_7985() && attacker.method_6047().method_7969().method_10545("corpse_oil_layers")) {
         int oilLayers = attacker.method_6047().method_7969().method_10550("corpse_oil_layers");
         if (oilLayers > 0) {
            isCorpseOilSwordAttack = true;
            corpseOilSwordDamage = attacker.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : baseAmount * 10.0F;
         }
      }

      if (!hasSpiritWeapon && !isSkillDamage && !isCorpseOilSwordAttack) {
         return true;
      }

      NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
      float playerSpiritDamage = attackerData.method_10545("spiritDamage") ? (float)attackerData.method_10574("spiritDamage") : 0.0F;
      float tempSpiritDamage = attackerData.method_10545("tempSpiritDamage") ? (float)attackerData.method_10574("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = attackerData.method_10545("tempSpiritDamageMultiplier")
         ? (float)attackerData.method_10574("tempSpiritDamageMultiplier")
         : 1.0F;
      float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
      float spiritDamageAmount = 0.0F;
      if (hasSpiritWeapon) {
         float weaponDamageBonus = 0.0F;
         float damageMultiplier = 0.5F;
         if (SpiritWeapon.isSpiritWeapon(attacker.method_6047())) {
            SpiritWeapon weapon = (SpiritWeapon)attacker.method_6047().method_7909();
            if (attacker.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
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
         UUID playerUUID = attacker.method_5667();
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
            float healthBefore = target.method_6032();
            target.method_5643(source, corpseOilSwordDamage);
            float actualDamage = healthBefore - target.method_6032();
            float penetrationThreshold = (float)ModConfig.getInstance().spiritDamagePenetrationThreshold;
            boolean skipCreative = target instanceof PlayerEntity playerTarget
               && (
                  playerTarget.method_7337() && !ModConfig.getInstance().penetrateCreativeMode
                     || playerTarget.method_7325() && !ModConfig.getInstance().penetrateSpectatorMode
               );
            float threshold = corpseOilSwordDamage * penetrationThreshold;
            if (!skipCreative && actualDamage < threshold && actualDamage >= 0.0F && target.method_5805()) {
               float missingDamage = corpseOilSwordDamage - actualDamage;
               target.method_6033(Math.max(0.0F, target.method_6032() - missingDamage));
            }

            fireSpiritDamageCallback(attacker, target, corpseOilSwordDamage, actualDamage, source);
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667()) && actualDamage > 0.0F) {
               attacker.method_7353(
                  Text.method_43470("§a对 §f" + target.method_5477().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(actualDamage) + " §a点灵异伤害"),
                  true
               );
            }
         } else {
            spawnSpiritAttackParticles(target, spiritDamageAmount);
            float healthBefore = target.method_6032();
            target.method_5643(source, spiritDamageAmount);
            float actualDamage = healthBefore - target.method_6032();
            float penetrationThreshold2 = (float)ModConfig.getInstance().spiritDamagePenetrationThreshold;
            boolean skipCreative2 = target instanceof PlayerEntity playerTarget
               && (
                  playerTarget.method_7337() && !ModConfig.getInstance().penetrateCreativeMode
                     || playerTarget.method_7325() && !ModConfig.getInstance().penetrateSpectatorMode
               );
            float threshold2 = spiritDamageAmount * penetrationThreshold2;
            if (!skipCreative2 && actualDamage < threshold2 && actualDamage >= 0.0F && target.method_5805()) {
               float missingDamage = spiritDamageAmount - actualDamage;
               target.method_6033(Math.max(0.0F, target.method_6032() - missingDamage));
            }

            fireSpiritDamageCallback(attacker, target, spiritDamageAmount, actualDamage, source);
            if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667()) && actualDamage > 0.0F) {
               Float preAttackHealth = preSpiritWeaponAttackHealth.remove(target.method_5667());
               float displayDamage = preAttackHealth != null ? preAttackHealth - target.method_6032() : actualDamage;
               attacker.method_7353(
                  Text.method_43470("§a对 §f" + target.method_5477().getString() + " §a造成了 §f" + new DecimalFormat("#.###").format(displayDamage) + " §a点灵异伤害"),
                  true
               );
            }
         }

         if (!target.method_5805() && attacker instanceof ServerPlayerEntity serverPlayer) {
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
      Smfs.LOGGER.debug("开始尝试生成厉鬼: {}, 当前世界：{}", ghostName, world.method_27983());
      boolean canSpawn;
      if (world.method_27983().equals(Smfs.SPIRIT_REALM_DIMENSION)) {
         canSpawn = GhostSpawnManager.canSpawnGhostInSpiritRealm(world, ghostType);
      } else {
         canSpawn = GhostSpawnManager.canSpawnGhostInOverworld(world, ghostType);
      }

      if (!canSpawn) {
         Smfs.LOGGER.debug("厉鬼生成失败: 未找到生成配置或生成条件不满足");
      } else {
         ServerWorld targetWorld;
         if (world.method_27983().equals(Smfs.SPIRIT_REALM_DIMENSION)) {
            targetWorld = world;
            Smfs.LOGGER.debug("在灵异世界生成厉鬼: {}", ghostName);
            List<ServerPlayerEntity> spiritRealmPlayers = targetWorld.method_18456();
            if (spiritRealmPlayers.isEmpty()) {
               Smfs.LOGGER.debug("厉鬼生成失败: 灵异世界中没有玩家");
               return;
            }
         } else {
            targetWorld = world.method_8503().method_30002();
            Smfs.LOGGER.debug("在主世界生成厉鬼: {}", ghostName);
            if (targetWorld == null) {
               Smfs.LOGGER.error("厉鬼生成失败: 无法获取主世界");
               return;
            }

            List<ServerPlayerEntity> overworldPlayers = targetWorld.method_18456();
            if (overworldPlayers.isEmpty()) {
               Smfs.LOGGER.debug("厉鬼生成失败: 主世界中没有玩家");
               return;
            }
         }

         List<ServerPlayerEntity> players = targetWorld.method_18456();
         if (players.isEmpty()) {
            Smfs.LOGGER.debug("厉鬼生成失败: {}中没有玩家", targetWorld.method_27983());
         } else {
            ServerPlayerEntity targetPlayer = players.get(RANDOM.nextInt(players.size()));
            Smfs.LOGGER.debug("选择玩家 {} 作为生成中心", targetPlayer.method_5477().getString());
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
                  Entity ghostEntity = ghostType.method_5883(targetWorld);
                  if (ghostEntity != null) {
                     ghostEntity.method_5808(
                        spawnPos.method_10263() + 0.5,
                        spawnPos.method_10264(),
                        spawnPos.method_10260() + 0.5,
                        targetWorld.method_8409().method_43057() * 360.0F,
                        0.0F
                     );
                     targetWorld.method_8649(ghostEntity);
                     Smfs.LOGGER.debug("成功生成厉鬼: {} 在位置: {}", ghostName, spawnPos.toString());
                     GhostSpawnManager.markGhostSpawned(targetWorld, ghostType);
                     if (ghostEntity instanceof GhostEntity ghost) {
                        Smfs.LOGGER.debug("为厉鬼生成3个鬼奴");
                        spawnGhostSlavesForGhost(ghost, 3);
                     }

                     for (PlayerEntity nearbyPlayer : targetWorld.method_18456()) {
                        if (nearbyPlayer.method_24515().method_10262(spawnPos) <= 256.0) {
                           nearbyPlayer.method_7353(Text.method_43471("event.smfs.ghost_spawn").method_27692(Formatting.field_1064), true);
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
         int x = centerPos.method_10263() + RANDOM.nextInt(radius * 2) - radius;
         int z = centerPos.method_10260() + RANDOM.nextInt(radius * 2) - radius;
         Mutable checkPos = new Mutable(x, centerPos.method_10264(), z);

         for (int yOffset = 0; yOffset <= 16; yOffset++) {
            checkPos.method_33098(centerPos.method_10264() + yOffset);
            if (isValidSpawnPosition(world, checkPos)) {
               return checkPos.method_10062();
            }

            if (yOffset > 0) {
               checkPos.method_33098(centerPos.method_10264() - yOffset);
               if (isValidSpawnPosition(world, checkPos)) {
                  return checkPos.method_10062();
               }
            }
         }
      }

      return null;
   }

   private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
      BlockState blockState = world.method_8320(pos.method_10074());
      if (!blockState.method_26212(world, pos.method_10074())) {
         return false;
      }

      BlockPos abovePos = pos.method_10084();
      BlockState aboveState = world.method_8320(abovePos);
      BlockState aboveAboveState = world.method_8320(abovePos.method_10084());
      return aboveState.method_26215() && aboveAboveState.method_26215();
   }

   private static boolean checkSpecialSpawnConditions(ServerWorld world, EntityType<?> ghostType, ServerPlayerEntity targetPlayer) {
      BlockPos playerPos = targetPlayer.method_24515();
      if (ghostType == ModEntities.VILLAGER_GHOST) {
         boolean hasVillagerNearby = false;
         List<VillagerEntity> villagers = world.method_8390(VillagerEntity.class, new Box(playerPos).method_1014(64.0), villager -> villager.method_5805());
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
                  BlockPos checkPos = playerPos.method_10069(x, y, z);
                  BlockState blockState = world.method_8320(checkPos);
                  if (blockState.method_26204() instanceof ChestBlock) {
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
                  BlockPos checkPos = playerPos.method_10069(x, y, z);
                  FluidState fluidState = world.method_8316(checkPos);
                  if (fluidState.method_15767(FluidTags.field_15517)) {
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
         if (source.method_49708(ModDamageSources.GHOST)) {
            return;
         }

         if (targetPlayer.method_37908().method_8608()) {
            return;
         }

         StatusEffectInstance spiritErosionEffect = new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 4, false, true, true);
         attacker.method_6092(spiritErosionEffect);
      }

      if (GhostDomainManager.hasQiaomenGhost(targetPlayer)) {
         hasPassiveEffect = true;
         if (source.method_49708(ModDamageSources.GHOST)) {
            return;
         }

         if (targetPlayer.method_37908().method_8608()) {
            return;
         }

         StatusEffectInstance spiritErosionEffect = new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 60, 1, false, true, true);
         attacker.method_6092(spiritErosionEffect);
      }

      if (hasPassiveEffect && !targetPlayer.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)targetPlayer.method_37908();
         Vec3d pos = attacker.method_19538();

         for (int i = 0; i < 15; i++) {
            double offsetX = (targetPlayer.method_6051().method_43058() - 0.5) * 1.5;
            double offsetY = targetPlayer.method_6051().method_43058() * 2.0;
            double offsetZ = (targetPlayer.method_6051().method_43058() - 0.5) * 1.5;
            serverWorld.method_14199(
               ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, 0.0, 0.1, 0.0, 0.1
            );
         }
      }
   }

   private static void spawnGhostSlavesForGhost(GhostEntity ghost, int count) {
      ServerWorld world = (ServerWorld)ghost.method_37908();
      BlockPos ghostPos = ghost.method_24515();

      for (int i = 0; i < count; i++) {
         BlockPos slaveSpawnPos = findSuitableSpawnPosition(world, ghostPos, 10);
         if (slaveSpawnPos != null) {
            try {
               GhostSlaveEntity slave = ghost.spawnGhostSlave();
               if (slave != null) {
                  slave.method_5808(
                     slaveSpawnPos.method_10263() + 0.5,
                     slaveSpawnPos.method_10264(),
                     slaveSpawnPos.method_10260() + 0.5,
                     world.method_8409().method_43057() * 360.0F,
                     0.0F
                  );
               }
            } catch (Exception var7) {
            }
         }
      }
   }

   private static void checkFirstNightMidnightSound(ServerWorld world) {
      if (world.method_27983().method_29177().toString().equals("minecraft:overworld")) {
         if (!firstNightPianoPlayed) {
            long totalTime = world.method_8510();
            int currentDay = (int)(totalTime / 24000L);
            if (currentDay == 0) {
               long timeOfDay = world.method_8532() % 24000L;
               if (timeOfDay == 18000L) {
                  for (ServerPlayerEntity player : world.method_18456()) {
                     BlockPos playerPos = player.method_24515();
                     world.method_8396(null, playerPos, ModSounds.BACKGROUND_EERIE_PIANO, SoundCategory.field_15256, 1.0F, 1.0F);
                  }

                  firstNightPianoPlayed = true;
                  Smfs.LOGGER.debug("第一天午夜eerie_piano音效已播放");
               }
            }
         }
      }
   }

   private static void checkSecondNightBabyCryingSound(ServerWorld world) {
      if (world.method_27983().method_29177().toString().equals("minecraft:overworld")) {
         if (!secondNightBabyCryingPlayed) {
            long totalTime = world.method_8510();
            int currentDay = (int)(totalTime / 24000L);
            if (currentDay == 1) {
               long timeOfDay = world.method_8532() % 24000L;
               if (timeOfDay == 18000L) {
                  for (ServerPlayerEntity player : world.method_18456()) {
                     BlockPos playerPos = player.method_24515();
                     world.method_8396(null, playerPos, ModSounds.BACKGROUND_HORROR_BACKGROUND, SoundCategory.field_15256, 1.0F, 1.0F);
                  }

                  secondNightBabyCryingPlayed = true;
                  Smfs.LOGGER.debug("第二天午夜horror_background音效已播放");
               }
            }
         }
      }
   }

   private static void checkLockedGhostExpiration(ServerWorld world) {
      if (world.method_27983().equals(World.field_25179)) {
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
         Blocks.field_10418,
         Blocks.field_29219,
         Blocks.field_10212,
         Blocks.field_29027,
         Blocks.field_27120,
         Blocks.field_29221,
         Blocks.field_10571,
         Blocks.field_29026,
         Blocks.field_10080,
         Blocks.field_29030,
         Blocks.field_10090,
         Blocks.field_29028,
         Blocks.field_10442,
         Blocks.field_29029,
         Blocks.field_10013,
         Blocks.field_29220,
         Blocks.field_10213,
         Blocks.field_23077,
         Blocks.field_22109,
         ModBlocks.DEFILED_ORE,
         ModBlocks.DEEP_DEFILED_ORE
      );
      return mineralBlocks.contains(block);
   }
}
