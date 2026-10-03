package com.xie.smfs.manager;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.event.QuestEventHandler;
import com.xie.smfs.network.packets.skills.s2c.BoxGhostContainerPositionsS2CPacket;
import com.xie.smfs.network.packets.skills.s2c.MineralGhostMineralPositionsS2CPacket;
import com.xie.smfs.network.packets.skills.s2c.SyncGhostOfficerQuotaS2CPacket;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlastFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SmokerBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostSkillManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostSkillManager");
   private static final Map<UUID, UUID> prioritySuppressionTargets = new HashMap<>();
   private static final Map<UUID, Integer> bonusSuppressionSlots = new HashMap<>();
   private static final Map<UUID, Boolean> isPlayingMap = new HashMap<>();
   private static final Map<UUID, Map<UUID, Integer>> playerMarksMap = new HashMap<>();
   private static final Map<UUID, Long> skillUseTimeMap = new HashMap<>();
   private static final Map<UUID, Boolean> playerStationaryState = new HashMap<>();
   private static final Map<UUID, List<UUID>> ghostOfficerWhitelist = new HashMap<>();

   public static void handleGhostFistActiveSkill(PlayerEntity player, LivingEntity target) {
      if (hasGhostFistEquipped(player)) {
         ItemStack mainHandStack = player.method_5998(Hand.field_5808);
         if (mainHandStack.method_7960()) {
            int spiritDamageBonus = 0;

            for (int i = 0; i < 10; i++) {
               ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
               if (ghostItem.method_7909() == ModItems.GHOST_FIST) {
                  spiritDamageBonus = PlayerEvents.getGhostSlotLevel(player, i) * 20;
                  break;
               }
            }

            if (spiritDamageBonus > 0) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  float damageAmount = spiritDamageBonus;
                  DamageSource damageSource = player.method_48923().method_48802(player);
                  if (target instanceof ServerPlayerEntity targetPlayer) {
                     handleSkillDamageToPlayer((ServerPlayerEntity)player, targetPlayer, damageSource, damageAmount);
                  } else {
                     handleSkillDamageToEntity((ServerPlayerEntity)player, target, damageSource, damageAmount);
                  }

                  double knockbackStrength = 1.2;
                  target.method_6005(knockbackStrength, player.method_23317() - target.method_23317(), player.method_23321() - target.method_23321());
                  spawnGhostFistAttackParticles(player, target);
                  if (!target.method_5805() && player instanceof ServerPlayerEntity serverPlayer) {
                     QuestEventHandler.handleEntityKill(serverPlayer, target);
                  }
               } finally {
                  ModEvents.processingSpiritDamage.set(false);
               }
            }
         }
      }
   }

   private static void handleSkillDamageToPlayer(ServerPlayerEntity attacker, ServerPlayerEntity target, DamageSource source, float damageAmount) {
      if (ModConfig.getInstance().enablePlayerSpiritDamage) {
         if (!ClientModConfig.getInstance().isDamageWhitelisted(attacker.method_5667(), target.method_5477().getString())) {
            NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
            float spiritDamage = attackerData.method_10545("spiritDamage") ? (float)attackerData.method_10574("spiritDamage") : 0.0F;
            NbtCompound targetData = PlayerEvents.getSpiritAttributes(target);
            float spiritResistance = targetData.method_10545("spiritResistance") ? (float)targetData.method_10574("spiritResistance") : 0.0F;
            float rawSpiritDamage = PlayerEvents.calculateSpiritDamage(spiritDamage, spiritResistance);
            float actualDamage = PlayerEvents.handleSpiritDamage(target, rawSpiritDamage, damageAmount, source);
            if (actualDamage > 0.0F && ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667())) {
               attacker.method_7353(
                  Text.method_43470("§a对 " + target.method_5477().getString() + " 造成了 " + new DecimalFormat("#.###").format(actualDamage) + " 点灵异伤害"), true
               );
            }

            if (actualDamage > 0.0F && !target.method_37908().method_8608()) {
               ServerWorld serverWorld = (ServerWorld)target.method_37908();
               Vec3d pos = target.method_19538();

               for (int i = 0; i < 25; i++) {
                  double offsetX = (target.method_6051().method_43058() - 0.5) * 2.0;
                  double offsetY = target.method_6051().method_43058() * 1.5 + 0.5;
                  double offsetZ = (target.method_6051().method_43058() - 0.5) * 2.0;
                  double velocityX = (target.method_6051().method_43058() - 0.5) * 0.2;
                  double velocityY = target.method_6051().method_43058() * 0.3 + 0.1;
                  double velocityZ = (target.method_6051().method_43058() - 0.5) * 0.2;
                  serverWorld.method_14199(
                     ParticleTypes.field_11249,
                     pos.field_1352 + offsetX,
                     pos.field_1351 + offsetY,
                     pos.field_1350 + offsetZ,
                     1,
                     velocityX,
                     velocityY,
                     velocityZ,
                     0.5
                  );
               }
            }
         }
      }
   }

   private static void handleSkillDamageToEntity(ServerPlayerEntity attacker, LivingEntity target, DamageSource source, float damageAmount) {
      NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
      float spiritDamage = attackerData.method_10545("spiritDamage") ? (float)attackerData.method_10574("spiritDamage") : 0.0F;
      float actualDamage = spiritDamage > 0.0F ? spiritDamage : damageAmount;
      float healthBeforeDamage = target.method_6032();
      target.method_5643(source, actualDamage);
      if (!target.method_5805() && healthBeforeDamage > 0.0F) {
         QuestEventHandler.handleEntityKill(attacker, target);
      }

      if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667())) {
         float actualDamageDealt = healthBeforeDamage - target.method_6032();
         attacker.method_7353(
            Text.method_43470("§a对 " + target.method_5477().getString() + " 造成了 " + new DecimalFormat("#.###").format(actualDamageDealt) + " 点灵异伤害"), true
         );
      }

      if (actualDamage > 0.0F && !target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         Vec3d pos = target.method_19538();

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

   public static void handleGhostFistPassiveSkill(PlayerEntity player, LivingEntity target) {
      if (hasGhostFistEquipped(player)) {
         if (!isSilencedOrDreaming(player)) {
            target.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 20, 0, false, false, true));
            LOGGER.info("玩家 {} 的攻击附带沉寂效果", player.method_5477().getString());
         }
      }
   }

   private static boolean isSilencedOrDreaming(PlayerEntity player) {
      return player.method_6059(ModEffects.SILENCE) || player.method_6059(ModEffects.DREAM);
   }

   public static boolean hasGhostFistEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_fist");
   }

   public static boolean hasLostGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "lost_ghost");
   }

   public static boolean hasFoodGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "food_ghost");
   }

   public static boolean hasGhostPressureEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_pressure");
   }

   public static boolean hasPlagueGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "plague_ghost");
   }

   public static boolean hasMineralGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "mineral_ghost");
   }

   public static boolean hasCropGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "crop_ghost");
   }

   public static boolean hasShadowGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "shadow_ghost");
   }

   public static void handleLostGhostPassiveSkill(PlayerEntity player) {
      if (hasLostGhostEquipped(player)) {
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5905, 60, 0, false, false, true));
         if (!player.method_37908().method_8608()) {
            double radius = 16.0;

            for (LivingEntity entity : player.method_37908()
               .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof HostileEntity)) {
               if (entity instanceof HostileEntity hostileEntity && hostileEntity.method_5968() == player) {
                  hostileEntity.method_5980(null);
               }
            }
         }
      }
   }

   public static void handleGhostPressurePassiveSkill(PlayerEntity player) {
      if (hasGhostPressureEquipped(player)) {
         boolean hasLevel5 = false;

         for (int i = 0; i < 10; i++) {
            ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
            if (ghostItem.method_7909() == ModItems.GHOST_PRESSURE) {
               int revivalLevel = PlayerEvents.getGhostSlotLevel(player, i);
               if (revivalLevel >= 5) {
                  hasLevel5 = true;
                  break;
               }
            }
         }

         if (hasLevel5) {
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 60, 0, false, false, true));
         }
      }
   }

   public static void handleFoodGhostPassiveSkill(PlayerEntity player) {
      if (hasFoodGhostEquipped(player)) {
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5922, 40, 0, false, false, true));
         LOGGER.info("玩家 {} 触发食物鬼被动技能：获得饱和效果", player.method_5477().getString());
      }
   }

   public static void handleMineralGhostPassiveSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      String mainGhostType = PlayerEvents.getGhostTypeInSlot(player, mainSlot);
      if ("mineral_ghost".equals(mainGhostType)) {
         if (!player.method_37908().method_8608()) {
            BlockPos playerPos = player.method_24515();
            World world = player.method_37908();
            int detectionRadius = 15;
            List<BlockPos> mineralPositions = new ArrayList<>();
            List<Integer> oreTypeList = new ArrayList<>();

            for (int x = -detectionRadius; x <= detectionRadius; x++) {
               for (int y = -detectionRadius; y <= detectionRadius; y++) {
                  for (int z = -detectionRadius; z <= detectionRadius; z++) {
                     BlockPos checkPos = playerPos.method_10069(x, y, z);
                     BlockState blockState = world.method_8320(checkPos);
                     Block block = blockState.method_26204();
                     if (isMineralBlock(block)) {
                        mineralPositions.add(checkPos);
                        oreTypeList.add(getOreType(block));
                     }
                  }
               }
            }

            if (!mineralPositions.isEmpty() && player instanceof ServerPlayerEntity serverPlayer) {
               sendMineralPositionsToClient(serverPlayer, mineralPositions, oreTypeList);
            }
         }
      }
   }

   public static void handleShadowGhostPassiveSkill(PlayerEntity player) {
      if (hasShadowGhostEquipped(player)) {
         int lightLevel = player.method_37908().method_22339(player.method_24515());
         if (lightLevel < 7) {
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5905, 60, 0, false, false, true));
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5904, 60, 1, false, false, true));
         } else {
            player.method_6016(StatusEffects.field_5905);
            player.method_6016(StatusEffects.field_5904);
         }
      }
   }

   public static void handleBoxGhostPassiveSkill(PlayerEntity player) {
      if (hasBoxGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            BlockPos playerPos = player.method_24515();
            World world = player.method_37908();
            int detectionRadius = 15;
            List<BlockPos> containerPositions = new ArrayList<>();

            for (int x = -detectionRadius; x <= detectionRadius; x++) {
               for (int y = -detectionRadius; y <= detectionRadius; y++) {
                  for (int z = -detectionRadius; z <= detectionRadius; z++) {
                     BlockPos checkPos = playerPos.method_10069(x, y, z);
                     BlockState blockState = world.method_8320(checkPos);
                     if (isContainerBlock(blockState.method_26204()) && hasLootTableNBT(world, checkPos)) {
                        containerPositions.add(checkPos);
                     }
                  }
               }
            }

            if (!containerPositions.isEmpty() && player instanceof ServerPlayerEntity serverPlayer) {
               sendContainerPositionsToClient(serverPlayer, containerPositions);
            }
         }
      }
   }

   private static boolean isContainerBlock(Block block) {
      return block instanceof ChestBlock
         || block instanceof BarrelBlock
         || block instanceof ShulkerBoxBlock
         || block instanceof DispenserBlock
         || block instanceof DropperBlock
         || block instanceof HopperBlock
         || block instanceof FurnaceBlock
         || block instanceof BlastFurnaceBlock
         || block instanceof SmokerBlock
         || block instanceof BrewingStandBlock
         || block instanceof TrappedChestBlock;
   }

   private static boolean isMineralBlock(Block block) {
      Set<Block> mineralBlocks = new HashSet<>(
         Arrays.asList(
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
         )
      );
      return mineralBlocks.contains(block);
   }

   private static int getOreType(Block block) {
      if (block == Blocks.field_10571 || block == Blocks.field_29026 || block == Blocks.field_23077) {
         return 1;
      } else {
         return block != ModBlocks.DEFILED_ORE && block != ModBlocks.DEEP_DEFILED_ORE ? 0 : 2;
      }
   }

   private static boolean hasLootTableNBT(World world, BlockPos pos) {
      BlockEntity blockEntity = world.method_8321(pos);
      if (blockEntity == null) {
         return false;
      }

      NbtCompound nbt = blockEntity.method_38244();
      return nbt.method_10545("LootTable") || nbt.method_10545("LootTableSeed");
   }

   private static void sendContainerPositionsToClient(ServerPlayerEntity player, List<BlockPos> containerPositions) {
      BoxGhostContainerPositionsS2CPacket.sendToClient(player, containerPositions);

      for (BlockPos pos : containerPositions) {
         spawnContainerMarkerParticles(player.method_37908(), pos);
      }
   }

   private static void sendMineralPositionsToClient(ServerPlayerEntity player, List<BlockPos> mineralPositions, List<Integer> oreTypeList) {
      MineralGhostMineralPositionsS2CPacket.sendToClient(player, mineralPositions, oreTypeList);

      for (BlockPos pos : mineralPositions) {
         spawnMineralMarkerParticles(player.method_37908(), pos);
      }
   }

   private static void spawnMineralMarkerParticles(World world, BlockPos pos) {
      if (!world.method_8608()) {
         ServerWorld serverWorld = (ServerWorld)world;

         for (int i = 0; i < 3; i++) {
            double offsetX = (world.method_8409().method_43058() - 0.5) * 0.5;
            double offsetY = world.method_8409().method_43058() * 0.5 + 1.0;
            double offsetZ = (world.method_8409().method_43058() - 0.5) * 0.5;
            serverWorld.method_14199(
               ParticleTypes.field_11215,
               pos.method_10263() + 0.5 + offsetX,
               pos.method_10264() + offsetY,
               pos.method_10260() + 0.5 + offsetZ,
               1,
               0.0,
               0.0,
               0.0,
               0.1
            );
         }
      }
   }

   private static void spawnContainerMarkerParticles(World world, BlockPos pos) {
      if (!world.method_8608()) {
         ServerWorld serverWorld = (ServerWorld)world;

         for (int i = 0; i < 3; i++) {
            double offsetX = (world.method_8409().method_43058() - 0.5) * 0.5;
            double offsetY = world.method_8409().method_43058() * 0.5 + 1.0;
            double offsetZ = (world.method_8409().method_43058() - 0.5) * 0.5;
            serverWorld.method_14199(
               ParticleTypes.field_11211,
               pos.method_10263() + 0.5 + offsetX,
               pos.method_10264() + offsetY,
               pos.method_10260() + 0.5 + offsetZ,
               1,
               0.0,
               0.0,
               0.0,
               0.1
            );
         }
      }
   }

   public static boolean hasBoxGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "box_ghost");
   }

   public static boolean hasGhostOfficerEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_officer");
   }

   public static boolean hasSneakGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "sneak_ghost");
   }

   public static boolean hasNoSneakGhostEquipped(PlayerEntity player) {
      return !hasSneakGhostEquipped(player);
   }

   public static boolean hasClothesGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "clothes_ghost");
   }

   public static boolean hasNoClothesGhostEquipped(PlayerEntity player) {
      return !hasClothesGhostEquipped(player);
   }

   public static boolean hasPuppetGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "puppet_ghost");
   }

   public static boolean hasNoPuppetGhostEquipped(PlayerEntity player) {
      return !PlayerEvents.hasGhostType(player, "puppet_ghost");
   }

   public static boolean hasFuneralMusicGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "funeral_music_ghost");
   }

   public static boolean hasNoFuneralMusicGhostEquipped(PlayerEntity player) {
      return !PlayerEvents.hasGhostType(player, "funeral_music_ghost");
   }

   public static boolean hasGiantShadowGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "giant_shadow_ghost") || PlayerEvents.hasGhostType(player, "complete_shadow_ghost");
   }

   public static void handleGiantShadowGhostPassiveSkill(PlayerEntity player) {
      if (hasGiantShadowGhostEquipped(player)) {
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5924, 40, 2, false, false, true));
      }
   }

   public static void handleGiantShadowGhostAreaAttack(PlayerEntity player, double radius, long sneakDuration) {
      if (player != null && !player.method_37908().method_8608() && player instanceof ServerPlayerEntity serverPlayer) {
         List<Entity> nearbyEntities = player.method_37908()
            .method_8333(player, player.method_5829().method_1014(radius), entityx -> entityx instanceof LivingEntity && entityx != player);
         NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
         float spiritDamage = playerData.method_10545("spiritDamage") ? (float)playerData.method_10574("spiritDamage") : 50.0F;
         long maxSneakDuration = 200L;
         double damageMultiplier = Math.min((double)sneakDuration / maxSneakDuration, 1.0);
         float actualDamage = (float)(spiritDamage * damageMultiplier);
         if (!(actualDamage <= 0.0F)) {
            for (Entity entity : nearbyEntities) {
               if (entity instanceof LivingEntity livingEntity) {
                  GhostDomainManager.executeSkillSpiritAttack(serverPlayer, livingEntity, actualDamage);
                  livingEntity.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 60, 2, false, false, true));
               }
            }
         }
      }
   }

   public static void handleGhostOfficerPassiveSkill(PlayerEntity player) {
      if (hasGhostOfficerEquipped(player)) {
         if (GhostDomainManager.isGhostDomainActive(player)) {
            if (!player.method_37908().method_8608()) {
               int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
               int bonusSlots = PlayerEvents.getBonusSuppressionSlots(player);
               int suppressionSlots = ghostCount + bonusSlots;
               if (suppressionSlots <= 0) {
                  return;
               }

               int mainSlot = MainGhostManager.getMainGhostSlot(player);
               int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
               int radius = GhostDomainManager.getDomainRadius(player, level);
               List<LivingEntity> entitiesInRange = findEntitiesInGhostDomain(player, radius);
               if (entitiesInRange.isEmpty()) {
                  return;
               }

               Object[] result = calculateSuppressionTargets(player, entitiesInRange, suppressionSlots);
               List<GhostSkillManager.SuppressionTarget> suppressionTargets = (List<GhostSkillManager.SuppressionTarget>)result[0];
               int usedSlots = (Integer)result[1];
               applySuppressionEffects(player, suppressionTargets);
               int remainingQuota = suppressionSlots - usedSlots;
               syncGhostOfficerQuotaToClient(player, remainingQuota, suppressionSlots);
            }
         }
      }
   }

   private static void syncGhostOfficerQuotaToClient(PlayerEntity player, int remainingQuota, int maxQuota) {
      if (!player.method_37908().field_9236) {
         if (hasGhostOfficerEquipped(player)) {
            SyncGhostOfficerQuotaS2CPacket.sendToPlayer((ServerPlayerEntity)player, remainingQuota, maxQuota);
         }
      }
   }

   private static List<LivingEntity> findEntitiesInGhostDomain(PlayerEntity player, int radius) {
      List<LivingEntity> entities = new ArrayList<>();
      List<LivingEntity> entitiesInRange = player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entity -> entity != player && entity instanceof LivingEntity);
      entities.addAll(entitiesInRange);
      return entities;
   }

   public static boolean setPrioritySuppressionTarget(ServerPlayerEntity player, LivingEntity target) {
      int slotCost = calculateEntitySlotCost(target);
      if (slotCost <= 0) {
         return false;
      }

      prioritySuppressionTargets.put(player.method_5667(), target.method_5667());
      LOGGER.info("玩家 {} 设置优先压制目标：{}", player.method_5477().getString(), target.method_5477().getString());
      return true;
   }

   public static void addBonusSuppressionSlot(PlayerEntity player, int amount) {
      PlayerEvents.addBonusSuppressionSlots(player, amount);
      UUID uuid = player.method_5667();
      int current = PlayerEvents.getBonusSuppressionSlots(player);
      bonusSuppressionSlots.put(uuid, current);
      LOGGER.info("玩家 {} 获得额外压制名额 +{}，当前额外名额：{}", player.method_5477().getString(), amount, current);
   }

   public static int getBonusSuppressionSlots(PlayerEntity player) {
      return PlayerEvents.getBonusSuppressionSlots(player);
   }

   public static void clearPrioritySuppressionTarget(ServerPlayerEntity player) {
      prioritySuppressionTargets.remove(player.method_5667());
      LOGGER.info("玩家 {} 清除优先压制目标", player.method_5477().getString());
   }

   private static LivingEntity getPrioritySuppressionTarget(ServerPlayerEntity player, List<LivingEntity> entities) {
      UUID targetUUID = prioritySuppressionTargets.get(player.method_5667());
      if (targetUUID == null) {
         return null;
      }

      for (LivingEntity entity : entities) {
         if (entity.method_5667().equals(targetUUID)) {
            return entity;
         }
      }

      prioritySuppressionTargets.remove(player.method_5667());
      return null;
   }

   private static Object[] calculateSuppressionTargets(PlayerEntity player, List<LivingEntity> entities, int maxSlots) {
      List<GhostSkillManager.SuppressionTarget> targets = new ArrayList<>();
      int usedSlots = 0;
      List<LivingEntity> sortedEntities = sortEntitiesByPriority(player, entities);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         LivingEntity priorityTarget = getPrioritySuppressionTarget(serverPlayer, entities);
         if (priorityTarget != null && sortedEntities.contains(priorityTarget)) {
            sortedEntities.remove(priorityTarget);
            sortedEntities.add(0, priorityTarget);
            LOGGER.debug("玩家 {} 的优先压制目标 {} 已设置为最高优先级", serverPlayer.method_5477().getString(), priorityTarget.method_5477().getString());
         }
      }

      for (LivingEntity entity : sortedEntities) {
         if (!(player instanceof ServerPlayerEntity serverPlayer && isInGhostOfficerWhitelist(serverPlayer, entity))
            && !(
               entity instanceof PlayerGhostEntity playerGhost
                  && playerGhost.getMasterUuid() != null
                  && playerGhost.getMasterUuid().equals(player.method_5667())
            )
            && !(entity instanceof GhostChildEntity ghostChild && ghostChild.getMasterUuid() != null && ghostChild.getMasterUuid().equals(player.method_5667()))
            && !(entity instanceof LuoQianGhostEntity)) {
            int slotCost = calculateEntitySlotCost(entity);
            if (slotCost > 0) {
               if (usedSlots + slotCost > maxSlots) {
                  break;
               }

               targets.add(new GhostSkillManager.SuppressionTarget(entity, slotCost));
               usedSlots += slotCost;
            }
         }
      }

      return new Object[]{targets, usedSlots};
   }

   private static List<LivingEntity> sortEntitiesByPriority(PlayerEntity player, List<LivingEntity> entities) {
      List<LivingEntity> sortedEntities = new ArrayList<>(entities);
      sortedEntities.sort((entity1, entity2) -> {
         double distance1 = player.method_5858(entity1);
         double distance2 = player.method_5858(entity2);
         return Double.compare(distance1, distance2);
      });
      return sortedEntities;
   }

   private static int calculateEntitySlotCost(LivingEntity entity) {
      if (entity instanceof GhostEntity) {
         return ((GhostEntity)entity).getSuppressionSlotCost();
      } else if (entity instanceof PlayerEntity targetPlayer) {
         return PlayerEvents.countOccupiedGhostSlots(targetPlayer);
      } else {
         return entity instanceof GhostMasterEntity ? 1 : 0;
      }
   }

   private static void applySuppressionEffects(PlayerEntity sourcePlayer, List<GhostSkillManager.SuppressionTarget> targets) {
      for (GhostSkillManager.SuppressionTarget target : targets) {
         if (!(sourcePlayer instanceof ServerPlayerEntity serverPlayer && isInGhostOfficerWhitelist(serverPlayer, target.entity))) {
            target.entity.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 100, 0, false, false, true));
         }
      }
   }

   public static void onPlayerAttack(PlayerEntity player, LivingEntity target) {
      if (!player.method_37908().method_8608() && hasGhostFistEquipped(player)) {
         ItemStack mainHandStack = player.method_5998(Hand.field_5808);
         if (mainHandStack.method_7960()) {
            handleGhostFistPassiveSkill(player, target);
            handleGhostFistActiveSkill(player, target);
         }

         handleGhostFistRevivalDegree(player);
      }
   }

   public static void handleFuneralMusicGhostPassiveSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            UUID playerId = player.method_5667();
            boolean isPlaying = isPlayingMap.getOrDefault(playerId, false);
            if (isPlaying) {
               NbtCompound spiritData = PlayerEvents.getSpiritAttributes(player);
               double currentSpirit = spiritData.method_10545("currentSpirit") ? spiritData.method_10574("currentSpirit") : 0.0;
               if (currentSpirit < 1000.0) {
                  exitPlayingState(player);
                  triggerAllMarks(player);
                  return;
               }

               long currentTime = System.currentTimeMillis();
               Long lastMarkTime = skillUseTimeMap.getOrDefault(playerId, 0L);
               if (currentTime - lastMarkTime >= 3000L) {
                  addMarksToNearbyEntities(player);
                  double newCurrentSpirit = Math.max(0.0, currentSpirit - 100.0);
                  spiritData.method_10549("currentSpirit", newCurrentSpirit);
                  PlayerEvents.setSpiritAttributes(player, spiritData);
                  skillUseTimeMap.put(playerId, currentTime);
               }
            }
         }
      }
   }

   public static void handleFuneralMusicGhostActiveSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            UUID playerId = player.method_5667();
            boolean hasTargets = playerMarksMap.containsKey(playerId) && !playerMarksMap.get(playerId).isEmpty();
            triggerAllMarks(player);
            if (!hasTargets) {
               player.method_7353(Text.method_43470("§a没有可触发的标记"), true);
            }
         }
      }
   }

   public static void handleFuneralMusicGhostFirstSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            int purificationDuration = 60;
            if (!player.method_6059(ModEffects.SILENCE)) {
               purificationDuration = 600;
            }

            player.method_6092(new StatusEffectInstance(ModEffects.PURIFICATION, purificationDuration, 1, false, false, true));
            enterPlayingState(player);
            skillUseTimeMap.put(player.method_5667(), System.currentTimeMillis());
         }
      }
   }

   public static void handleFuneralMusicGhostSecondSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            exitPlayingState(player);
            int purificationDuration = 60;
            if (!player.method_6059(ModEffects.SILENCE)) {
               purificationDuration = 600;
            }

            player.method_6092(new StatusEffectInstance(ModEffects.PURIFICATION, purificationDuration, 1, false, false, true));
            skillUseTimeMap.put(player.method_5667(), System.currentTimeMillis());
         }
      }
   }

   public static void handleFuneralMusicGhostThirdSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            UUID playerId = player.method_5667();
            boolean hasTargets = playerMarksMap.containsKey(playerId) && !playerMarksMap.get(playerId).isEmpty();
            triggerAllMarks(player, true);
            if (hasTargets) {
               player.method_7353(Text.method_43470("§a触发全部标记，目标陷入短暂沉寂"), true);
            } else {
               player.method_7353(Text.method_43470("§a没有可触发的标记"), true);
            }
         }
      }
   }

   private static void enterPlayingState(PlayerEntity player) {
      UUID playerId = player.method_5667();
      isPlayingMap.put(playerId, true);
      player.method_7353(Text.method_43470("§a进入演奏状态"), true);
   }

   private static void exitPlayingState(PlayerEntity player) {
      UUID playerId = player.method_5667();
      isPlayingMap.put(playerId, false);
      player.method_7353(Text.method_43470("§a退出演奏状态"), true);
   }

   private static void addMarksToNearbyEntities(PlayerEntity player) {
      UUID playerId = player.method_5667();
      List<LivingEntity> nearbyEntities = player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(10.0), entityx -> entityx != player);
      if (!playerMarksMap.containsKey(playerId)) {
         playerMarksMap.put(playerId, new HashMap<>());
      }

      Map<UUID, Integer> marks = playerMarksMap.get(playerId);

      for (LivingEntity entity : nearbyEntities) {
         UUID entityId = entity.method_5667();
         int currentMarks = marks.getOrDefault(entityId, 0);
         marks.put(entityId, currentMarks + 1);
      }
   }

   private static void triggerAllMarks(PlayerEntity player) {
      triggerAllMarks(player, false);
   }

   private static void triggerAllMarks(PlayerEntity player, boolean applySilence) {
      UUID playerId = player.method_5667();
      if (playerMarksMap.containsKey(playerId)) {
         Map<UUID, Integer> marks = playerMarksMap.get(playerId);

         for (Entry<UUID, Integer> entry : marks.entrySet()) {
            UUID entityId = entry.getKey();
            int markLevel = entry.getValue();
            LivingEntity target = null;

            for (LivingEntity entity : player.method_37908().method_8390(LivingEntity.class, player.method_5829().method_1014(100.0), entityx -> true)) {
               if (entity.method_5667().equals(entityId)) {
                  target = entity;
                  break;
               }
            }

            if (target != null && target.method_5805()) {
               if (applySilence) {
                  target.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 40, 0, false, false, true));
               } else {
                  float damage = markLevel * 250;
                  ModEvents.processingSpiritDamage.set(true);

                  try {
                     if (player instanceof ServerPlayerEntity serverPlayer) {
                        DamageSource damageSource = serverPlayer.method_48923().method_48802(serverPlayer);
                        if (target instanceof ServerPlayerEntity targetPlayer) {
                           handleSkillDamageToPlayer(serverPlayer, targetPlayer, damageSource, damage);
                        } else if (target instanceof GhostEntity ghost) {
                           ghost.handleSkillSpiritDamage(serverPlayer, damage);
                        } else {
                           handleSkillDamageToEntity(serverPlayer, target, damageSource, damage);
                        }
                     }
                  } finally {
                     ModEvents.processingSpiritDamage.set(false);
                  }
               }
            }
         }

         marks.clear();
      }
   }

   public static boolean isSkillDamage(DamageSource source) {
      return source.method_5529() instanceof PlayerEntity player ? ModEvents.processingSpiritDamage.get() : false;
   }

   public static void handleGhostFistRevivalDegree(PlayerEntity player) {
      if (hasGhostFistEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            int ghostFistSlot = findGhostFistSlot(player);
            if (ghostFistSlot >= 0) {
               int currentDegree = PlayerEvents.getGhostSlotRevivalDegree(player, ghostFistSlot);
               int requiredDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(player, ghostFistSlot);
               int newDegree = Math.min(currentDegree + 12, requiredDegree);
               if (newDegree != currentDegree) {
                  PlayerEvents.updateGhostSlotValue(player, ghostFistSlot, "revivalDegree", newDegree);
                  GhostDomainManager.checkRevivalDegree(player);
               }
            }
         }
      }
   }

   private static int findGhostFistSlot(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.isGhostSlotOccupied(player, i)) {
            String ghostType = PlayerEvents.getGhostTypeInSlot(player, i);
            if ("ghost_fist".equals(ghostType)) {
               return i;
            }
         }
      }

      return -1;
   }

   public static void handleSneakGhostActiveSkill(PlayerEntity player) {
      if (!hasNoSneakGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            for (PlayerEntity target : player.method_37908()
               .method_8390(PlayerEntity.class, player.method_5829().method_1014(10.0), targetx -> targetx != player && targetx.method_5715())) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
                  float spiritDamage = playerData.method_10545("spiritDamage") ? (float)playerData.method_10574("spiritDamage") : 50.0F;
                  boolean bothEquipped = GhostDomainManager.hasJumpGhost(player) && hasSneakGhostEquipped(player);
                  if (bothEquipped) {
                     spiritDamage *= 2.0F;
                  }

                  DamageSource damageSource = player.method_48923().method_48802(player);
                  if (target instanceof ServerPlayerEntity targetPlayer) {
                     handleSkillDamageToPlayer((ServerPlayerEntity)player, targetPlayer, damageSource, spiritDamage);
                  }

                  spawnGhostSkillParticles(player, target);
               } finally {
                  ModEvents.processingSpiritDamage.set(false);
               }
            }
         }
      }
   }

   public static void handleClothesGhostActiveSkill(PlayerEntity player) {
      if (!hasNoClothesGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            for (PlayerEntity target : player.method_37908()
               .method_8390(PlayerEntity.class, player.method_5829().method_1014(10.0), targetx -> targetx != player && !hasAnyEquipment(targetx))) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
                  float spiritDamage = playerData.method_10545("spiritDamage") ? (float)playerData.method_10574("spiritDamage") : 50.0F;
                  DamageSource damageSource = player.method_48923().method_48802(player);
                  if (target instanceof ServerPlayerEntity targetPlayer) {
                     handleSkillDamageToPlayer((ServerPlayerEntity)player, targetPlayer, damageSource, spiritDamage);
                  }

                  spawnGhostSkillParticles(player, target);
               } finally {
                  ModEvents.processingSpiritDamage.set(false);
               }
            }
         }
      }
   }

   public static void handlePuppetGhostActiveSkill(PlayerEntity player) {
      if (!hasNoPuppetGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            List<LivingEntity> markedEntities = new ArrayList<>();

            for (LivingEntity target : player.method_37908()
               .method_8390(LivingEntity.class, player.method_5829().method_1014(10.0), targetx -> targetx != player)) {
               if (target.method_6059(ModEffects.MARK_CURSE)) {
                  markedEntities.add(target);
               }
            }

            if (markedEntities.isEmpty()) {
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.method_7353(Text.method_43470("§c木偶鬼：范围内没有被标记的生物"), true);
               }

               return;
            }

            for (LivingEntity target : markedEntities) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
                  float spiritDamage = playerData.method_10545("spiritDamage") ? (float)playerData.method_10574("spiritDamage") : 50.0F;
                  DamageSource damageSource = player.method_48923().method_48802(player);
                  if (target instanceof ServerPlayerEntity targetPlayer) {
                     handleSkillDamageToPlayer((ServerPlayerEntity)player, targetPlayer, damageSource, spiritDamage);
                  } else {
                     target.method_5643(damageSource, spiritDamage);
                  }

                  spawnGhostSkillParticles(player, target);
               } finally {
                  ModEvents.processingSpiritDamage.set(false);
               }
            }
         }
      }
   }

   public static void handlePuppetGhostPassiveSkill(PlayerEntity player) {
      if (!hasNoPuppetGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            for (LivingEntity target : player.method_37908()
               .method_8390(LivingEntity.class, player.method_5829().method_1014(10.0), targetx -> targetx != player)) {
               if (target.method_18798().method_1027() < 0.01 && !target.method_6059(ModEffects.MARK_CURSE)) {
                  target.method_6092(new StatusEffectInstance(ModEffects.MARK_CURSE, 200, 0, false, false, true));
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.method_7353(Text.method_43470("§a木偶鬼标记了静止的生物: " + target.method_5477().getString()), true);
                  }
               }
            }
         }
      }
   }

   public static void handlePuppetGhostResistanceSkill(PlayerEntity player) {
      if (hasPuppetGhostEquipped(player)) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         if (!spiritAttributes.method_10545("tempSpiritResistance")) {
            spiritAttributes.method_10549("tempSpiritResistance", 0.0);
         }

         if (!spiritAttributes.method_10545("lastPositionX")) {
            spiritAttributes.method_10549("lastPositionX", player.method_23317());
            spiritAttributes.method_10549("lastPositionY", player.method_23318());
            spiritAttributes.method_10549("lastPositionZ", player.method_23321());
            spiritAttributes.method_10569("staticTicks", 0);
            spiritAttributes.method_10569("checkTicks", 0);
         }

         double lastX = spiritAttributes.method_10574("lastPositionX");
         double lastY = spiritAttributes.method_10574("lastPositionY");
         double lastZ = spiritAttributes.method_10574("lastPositionZ");
         Vec3d lastPosition = new Vec3d(lastX, lastY, lastZ);
         Vec3d currentPosition = player.method_19538();
         double distance = currentPosition.method_1022(lastPosition);
         if (distance < 0.1) {
            int staticTicks = spiritAttributes.method_10550("staticTicks") + 20;
            int checkTicks = spiritAttributes.method_10550("checkTicks") + 20;
            spiritAttributes.method_10569("staticTicks", staticTicks);
            spiritAttributes.method_10569("checkTicks", checkTicks);
            if (checkTicks >= 20) {
               if (staticTicks >= 100) {
                  double currentTempResistance = spiritAttributes.method_10574("tempSpiritResistance");
                  double newTempResistance = Math.min(currentTempResistance + 10.0, 2000.0);
                  spiritAttributes.method_10549("tempSpiritResistance", newTempResistance);
                  spiritAttributes.method_10569("staticTicks", 0);
               }

               spiritAttributes.method_10569("checkTicks", 0);
               PlayerEvents.setSpiritAttributes(player, spiritAttributes);
            } else {
               PlayerEvents.setSpiritAttributes(player, spiritAttributes);
            }
         } else {
            spiritAttributes.method_10549("lastPositionX", player.method_23317());
            spiritAttributes.method_10549("lastPositionY", player.method_23318());
            spiritAttributes.method_10549("lastPositionZ", player.method_23321());
            spiritAttributes.method_10569("staticTicks", 0);
            spiritAttributes.method_10569("checkTicks", 0);
            spiritAttributes.method_10549("tempSpiritResistance", 0.0);
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
         }
      }
   }

   public static void handleSneakGhostPassiveSkill(PlayerEntity player) {
      if (hasSneakGhostEquipped(player)) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         if (player.method_5715()) {
            if (!spiritAttributes.method_10545("normalSpiritDamage")) {
               spiritAttributes.method_10569("normalSpiritDamage", spiritAttributes.method_10550("spiritDamage"));
            }

            if (!spiritAttributes.method_10545("normalSpiritResistance")) {
               spiritAttributes.method_10569("normalSpiritResistance", spiritAttributes.method_10550("spiritResistance"));
            }

            int baseDamage = spiritAttributes.method_10550("normalSpiritDamage");
            int baseResistance = spiritAttributes.method_10550("normalSpiritResistance");
            int enhancedDamage = (int)(baseDamage * 1.15);
            int enhancedResistance = (int)(baseResistance * 1.2);
            spiritAttributes.method_10549("tempSpiritDamage", enhancedDamage - baseDamage);
            spiritAttributes.method_10549("tempSpiritResistance", enhancedResistance - baseResistance);
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
         } else {
            spiritAttributes.method_10549("tempSpiritDamage", 0.0);
            spiritAttributes.method_10549("tempSpiritResistance", 0.0);
            spiritAttributes.method_10551("normalSpiritDamage");
            spiritAttributes.method_10551("normalSpiritResistance");
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
         }
      }
   }

   public static void handleClothesGhostPassiveSkill(PlayerEntity player) {
      if (!hasNoClothesGhostEquipped(player)) {
         if (!player.method_37908().method_8608()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
               ItemStack stack = player.method_6118(slot);
               if (stack.method_7963()
                  && (
                     slot == EquipmentSlot.field_6169
                        || slot == EquipmentSlot.field_6174
                        || slot == EquipmentSlot.field_6172
                        || slot == EquipmentSlot.field_6166
                  )
                  && stack.method_7919() > 0) {
                  stack.method_7974(stack.method_7919() - 1);
               }
            }
         }
      }
   }

   private static boolean hasAnyEquipment(PlayerEntity player) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (!player.method_6118(slot).method_7960()) {
            return true;
         }
      }

      return false;
   }

   private static boolean isPlayerStationary(PlayerEntity player) {
      UUID playerId = player.method_5667();
      boolean currentState = player.method_18798().method_1027() < 0.001;
      if (!playerStationaryState.containsKey(playerId) || playerStationaryState.get(playerId) != currentState) {
         playerStationaryState.put(playerId, currentState);
      }

      return playerStationaryState.get(playerId);
   }

   private static void spawnGhostSkillParticles(PlayerEntity player, LivingEntity target) {
      if (!player.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)player.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 20; i++) {
            double offsetX = (player.method_6051().method_43058() - 0.5) * 1.5;
            double offsetY = player.method_6051().method_43058() * 1.0 + 0.5;
            double offsetZ = (player.method_6051().method_43058() - 0.5) * 1.5;
            double velocityX = (player.method_6051().method_43058() - 0.5) * 0.2;
            double velocityY = player.method_6051().method_43058() * 0.3 + 0.1;
            double velocityZ = (player.method_6051().method_43058() - 0.5) * 0.2;
            serverWorld.method_14199(
               ParticleTypes.field_11249, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5
            );
         }
      }
   }

   private static void spawnGhostFistAttackParticles(PlayerEntity player, LivingEntity target) {
      if (!player.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)player.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 25; i++) {
            double offsetX = (player.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = player.method_6051().method_43058() * 1.5 + 0.5;
            double offsetZ = (player.method_6051().method_43058() - 0.5) * 2.0;
            double velocityX = (player.method_6051().method_43058() - 0.5) * 0.2;
            double velocityY = player.method_6051().method_43058() * 0.3 + 0.1;
            double velocityZ = (player.method_6051().method_43058() - 0.5) * 0.2;
            serverWorld.method_14199(
               ParticleTypes.field_11249, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5
            );
         }
      }
   }

   private static void spawnGhostFistPassiveParticles(PlayerEntity player, LivingEntity target) {
      if (!player.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)player.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 15; i++) {
            double offsetX = (player.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = player.method_6051().method_43058() * 2.0;
            double offsetZ = (player.method_6051().method_43058() - 0.5) * 2.0;
            serverWorld.method_14199(
               ParticleTypes.field_22246, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, 0.0, 0.1, 0.0, 0.1
            );
         }
      }
   }

   public static boolean isInGhostOfficerWhitelist(ServerPlayerEntity player, LivingEntity target) {
      List<UUID> whitelist = ghostOfficerWhitelist.get(player.method_5667());
      return whitelist == null ? false : whitelist.contains(target.method_5667());
   }

   public static boolean removeFromSuppressionAndAddToWhitelist(ServerPlayerEntity player, LivingEntity target) {
      if (!target.method_6059(ModEffects.SILENCE)) {
         return false;
      }

      target.method_6016(ModEffects.SILENCE);
      List<UUID> whitelist = ghostOfficerWhitelist.computeIfAbsent(player.method_5667(), k -> new ArrayList<>());
      if (!whitelist.contains(target.method_5667())) {
         whitelist.add(target.method_5667());
      }

      LOGGER.info("玩家 {} 使用鬼差V键技能解除 {} 的压制并加入白名单", player.method_5477().getString(), target.method_5477().getString());
      return true;
   }

   public static boolean restoreSuppression(ServerPlayerEntity player, LivingEntity target) {
      if (!isInGhostOfficerWhitelist(player, target)) {
         return false;
      }

      List<UUID> whitelist = ghostOfficerWhitelist.get(player.method_5667());
      if (whitelist != null) {
         whitelist.remove(target.method_5667());
         if (whitelist.isEmpty()) {
            ghostOfficerWhitelist.remove(player.method_5667());
         }
      }

      target.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 100, 0, false, false, true));
      LOGGER.info("玩家 {} 使用鬼差V键技能恢复对 {} 的压制", player.method_5477().getString(), target.method_5477().getString());
      return true;
   }

   public static boolean hasVillagerGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "villager_ghost");
   }

   public static void handleVillagerGhostPassiveSkill(PlayerEntity player) {
      if (hasVillagerGhostEquipped(player)) {
         if (GhostDomainManager.isGhostDomainActive(player)) {
            int villagerGhostSlot = PlayerEvents.findEquippedVillagerGhostSlot(player);
            int villagerGhostLevel = PlayerEvents.getGhostSlotLevel(player, villagerGhostSlot);
            int domainRadius = GhostDomainManager.getDomainRadius(player, villagerGhostLevel);
            List<ZombieEntity> zombies = player.method_37908()
               .method_8390(
                  ZombieEntity.class,
                  new Box(
                     player.method_24515().method_10263() - domainRadius,
                     player.method_24515().method_10264() - domainRadius,
                     player.method_24515().method_10260() - domainRadius,
                     player.method_24515().method_10263() + domainRadius,
                     player.method_24515().method_10264() + domainRadius,
                     player.method_24515().method_10260() + domainRadius
                  ),
                  zombie -> zombie.method_5805() && zombie.method_5739(player) <= domainRadius
               );
            if (!zombies.isEmpty()) {
               ZombieEntity targetZombie = zombies.stream().min((z1, z2) -> Float.compare(z1.method_5739(player), z2.method_5739(player))).orElse(null);
               if (targetZombie != null) {
                  VillagerEntity villager = (VillagerEntity)targetZombie.method_29243(EntityType.field_6077, false);
                  if (villager != null && player.method_37908() instanceof ServerWorld serverWorld) {
                     serverWorld.method_14199(
                        ParticleTypes.field_11211, villager.method_23317(), villager.method_23318() + 1.0, villager.method_23321(), 10, 0.5, 0.5, 0.5, 0.1
                     );
                  }
               }
            }
         }
      }
   }

   public static void handleCropGhostPassiveSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      String mainGhostType = PlayerEvents.getGhostTypeInSlot(player, mainSlot);
      if ("crop_ghost".equals(mainGhostType)) {
         if (!player.method_37908().method_8608()) {
            BlockPos posBelow = player.method_24515().method_10074();
            BlockState blockState = player.method_37908().method_8320(posBelow);
            Block block = blockState.method_26204();
            if (block == Blocks.field_10566 || block == Blocks.field_10219) {
               player.method_37908().method_8652(posBelow, (BlockState)Blocks.field_10362.method_9564().method_11657(FarmlandBlock.field_11009, 7), 3);
               BlockPos cropPos = posBelow.method_10084();
               if (player.method_37908().method_8320(cropPos).method_26215()) {
                  Random random = new Random();
                  int cropType = random.nextInt(4);

                  player.method_37908().method_8652(cropPos, switch (cropType) {
                     case 0 -> (BlockState)Blocks.field_10293.method_9564().method_11657(CropBlock.field_10835, random.nextInt(4));
                     case 1 -> (BlockState)Blocks.field_10609.method_9564().method_11657(CropBlock.field_10835, random.nextInt(4));
                     case 2 -> (BlockState)Blocks.field_10247.method_9564().method_11657(CropBlock.field_10835, random.nextInt(4));
                     default -> (BlockState)Blocks.field_10341.method_9564().method_11657(Properties.field_12497, random.nextInt(3));
                  }, 3);
               }
            }
         }
      }
   }

   private static class SuppressionTarget {
      public final LivingEntity entity;
      public final int slotCost;

      public SuppressionTarget(LivingEntity entity, int slotCost) {
         this.entity = entity;
         this.slotCost = slotCost;
      }
   }
}
