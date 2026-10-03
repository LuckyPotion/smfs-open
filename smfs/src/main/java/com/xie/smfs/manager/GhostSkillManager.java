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
         ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
         if (mainHandStack.isEmpty()) {
            int spiritDamageBonus = 0;

            for (int i = 0; i < 10; i++) {
               ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
               if (ghostItem.getItem() == ModItems.GHOST_FIST) {
                  spiritDamageBonus = PlayerEvents.getGhostSlotLevel(player, i) * 20;
                  break;
               }
            }

            if (spiritDamageBonus > 0) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  float damageAmount = spiritDamageBonus;
                  DamageSource damageSource = player.getDamageSources().playerAttack(player);
                  if (target instanceof ServerPlayerEntity targetPlayer) {
                     handleSkillDamageToPlayer((ServerPlayerEntity)player, targetPlayer, damageSource, damageAmount);
                  } else {
                     handleSkillDamageToEntity((ServerPlayerEntity)player, target, damageSource, damageAmount);
                  }

                  double knockbackStrength = 1.2;
                  target.takeKnockback(knockbackStrength, player.getX() - target.getX(), player.getZ() - target.getZ());
                  spawnGhostFistAttackParticles(player, target);
                  if (!target.isAlive() && player instanceof ServerPlayerEntity serverPlayer) {
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
         if (!ClientModConfig.getInstance().isDamageWhitelisted(attacker.getUuid(), target.getName().getString())) {
            NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
            float spiritDamage = attackerData.contains("spiritDamage") ? (float)attackerData.getDouble("spiritDamage") : 0.0F;
            NbtCompound targetData = PlayerEvents.getSpiritAttributes(target);
            float spiritResistance = targetData.contains("spiritResistance") ? (float)targetData.getDouble("spiritResistance") : 0.0F;
            float rawSpiritDamage = PlayerEvents.calculateSpiritDamage(spiritDamage, spiritResistance);
            float actualDamage = PlayerEvents.handleSpiritDamage(target, rawSpiritDamage, damageAmount, source);
            if (actualDamage > 0.0F && ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid())) {
               attacker.sendMessage(
                  Text.literal("§a对 " + target.getName().getString() + " 造成了 " + new DecimalFormat("#.###").format(actualDamage) + " 点灵异伤害"), true
               );
            }

            if (actualDamage > 0.0F && !target.getWorld().isClient()) {
               ServerWorld serverWorld = (ServerWorld)target.getWorld();
               Vec3d pos = target.getPos();

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
      }
   }

   private static void handleSkillDamageToEntity(ServerPlayerEntity attacker, LivingEntity target, DamageSource source, float damageAmount) {
      NbtCompound attackerData = PlayerEvents.getSpiritAttributes(attacker);
      float spiritDamage = attackerData.contains("spiritDamage") ? (float)attackerData.getDouble("spiritDamage") : 0.0F;
      float actualDamage = spiritDamage > 0.0F ? spiritDamage : damageAmount;
      float healthBeforeDamage = target.getHealth();
      target.damage(source, actualDamage);
      if (!target.isAlive() && healthBeforeDamage > 0.0F) {
         QuestEventHandler.handleEntityKill(attacker, target);
      }

      if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid())) {
         float actualDamageDealt = healthBeforeDamage - target.getHealth();
         attacker.sendMessage(
            Text.literal("§a对 " + target.getName().getString() + " 造成了 " + new DecimalFormat("#.###").format(actualDamageDealt) + " 点灵异伤害"), true
         );
      }

      if (actualDamage > 0.0F && !target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         Vec3d pos = target.getPos();

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

   public static void handleGhostFistPassiveSkill(PlayerEntity player, LivingEntity target) {
      if (hasGhostFistEquipped(player)) {
         if (!isSilencedOrDreaming(player)) {
            target.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 20, 0, false, false, true));
            LOGGER.info("玩家 {} 的攻击附带沉寂效果", player.getName().getString());
         }
      }
   }

   private static boolean isSilencedOrDreaming(PlayerEntity player) {
      return player.hasStatusEffect(ModEffects.SILENCE) || player.hasStatusEffect(ModEffects.DREAM);
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
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false, true));
         if (!player.getWorld().isClient()) {
            double radius = 16.0;

            for (LivingEntity entity : player.getWorld()
               .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof HostileEntity)) {
               if (entity instanceof HostileEntity hostileEntity && hostileEntity.getTarget() == player) {
                  hostileEntity.setTarget(null);
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
            if (ghostItem.getItem() == ModItems.GHOST_PRESSURE) {
               int revivalLevel = PlayerEvents.getGhostSlotLevel(player, i);
               if (revivalLevel >= 5) {
                  hasLevel5 = true;
                  break;
               }
            }
         }

         if (hasLevel5) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, false, false, true));
         }
      }
   }

   public static void handleFoodGhostPassiveSkill(PlayerEntity player) {
      if (hasFoodGhostEquipped(player)) {
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, 40, 0, false, false, true));
         LOGGER.info("玩家 {} 触发食物鬼被动技能：获得饱和效果", player.getName().getString());
      }
   }

   public static void handleMineralGhostPassiveSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      String mainGhostType = PlayerEvents.getGhostTypeInSlot(player, mainSlot);
      if ("mineral_ghost".equals(mainGhostType)) {
         if (!player.getWorld().isClient()) {
            BlockPos playerPos = player.getBlockPos();
            World world = player.getWorld();
            int detectionRadius = 15;
            List<BlockPos> mineralPositions = new ArrayList<>();
            List<Integer> oreTypeList = new ArrayList<>();

            for (int x = -detectionRadius; x <= detectionRadius; x++) {
               for (int y = -detectionRadius; y <= detectionRadius; y++) {
                  for (int z = -detectionRadius; z <= detectionRadius; z++) {
                     BlockPos checkPos = playerPos.add(x, y, z);
                     BlockState blockState = world.getBlockState(checkPos);
                     Block block = blockState.getBlock();
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
         int lightLevel = player.getWorld().getLightLevel(player.getBlockPos());
         if (lightLevel < 7) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1, false, false, true));
         } else {
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
            player.removeStatusEffect(StatusEffects.SPEED);
         }
      }
   }

   public static void handleBoxGhostPassiveSkill(PlayerEntity player) {
      if (hasBoxGhostEquipped(player)) {
         if (!player.getWorld().isClient()) {
            BlockPos playerPos = player.getBlockPos();
            World world = player.getWorld();
            int detectionRadius = 15;
            List<BlockPos> containerPositions = new ArrayList<>();

            for (int x = -detectionRadius; x <= detectionRadius; x++) {
               for (int y = -detectionRadius; y <= detectionRadius; y++) {
                  for (int z = -detectionRadius; z <= detectionRadius; z++) {
                     BlockPos checkPos = playerPos.add(x, y, z);
                     BlockState blockState = world.getBlockState(checkPos);
                     if (isContainerBlock(blockState.getBlock()) && hasLootTableNBT(world, checkPos)) {
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
         )
      );
      return mineralBlocks.contains(block);
   }

   private static int getOreType(Block block) {
      if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) {
         return 1;
      } else {
         return block != ModBlocks.DEFILED_ORE && block != ModBlocks.DEEP_DEFILED_ORE ? 0 : 2;
      }
   }

   private static boolean hasLootTableNBT(World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity == null) {
         return false;
      }

      NbtCompound nbt = blockEntity.createNbt();
      return nbt.contains("LootTable") || nbt.contains("LootTableSeed");
   }

   private static void sendContainerPositionsToClient(ServerPlayerEntity player, List<BlockPos> containerPositions) {
      BoxGhostContainerPositionsS2CPacket.sendToClient(player, containerPositions);

      for (BlockPos pos : containerPositions) {
         spawnContainerMarkerParticles(player.getWorld(), pos);
      }
   }

   private static void sendMineralPositionsToClient(ServerPlayerEntity player, List<BlockPos> mineralPositions, List<Integer> oreTypeList) {
      MineralGhostMineralPositionsS2CPacket.sendToClient(player, mineralPositions, oreTypeList);

      for (BlockPos pos : mineralPositions) {
         spawnMineralMarkerParticles(player.getWorld(), pos);
      }
   }

   private static void spawnMineralMarkerParticles(World world, BlockPos pos) {
      if (!world.isClient()) {
         ServerWorld serverWorld = (ServerWorld)world;

         for (int i = 0; i < 3; i++) {
            double offsetX = (world.getRandom().nextDouble() - 0.5) * 0.5;
            double offsetY = world.getRandom().nextDouble() * 0.5 + 1.0;
            double offsetZ = (world.getRandom().nextDouble() - 0.5) * 0.5;
            serverWorld.spawnParticles(
               ParticleTypes.ENCHANT, pos.getX() + 0.5 + offsetX, pos.getY() + offsetY, pos.getZ() + 0.5 + offsetZ, 1, 0.0, 0.0, 0.0, 0.1
            );
         }
      }
   }

   private static void spawnContainerMarkerParticles(World world, BlockPos pos) {
      if (!world.isClient()) {
         ServerWorld serverWorld = (ServerWorld)world;

         for (int i = 0; i < 3; i++) {
            double offsetX = (world.getRandom().nextDouble() - 0.5) * 0.5;
            double offsetY = world.getRandom().nextDouble() * 0.5 + 1.0;
            double offsetZ = (world.getRandom().nextDouble() - 0.5) * 0.5;
            serverWorld.spawnParticles(
               ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5 + offsetX, pos.getY() + offsetY, pos.getZ() + 0.5 + offsetZ, 1, 0.0, 0.0, 0.0, 0.1
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
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 2, false, false, true));
      }
   }

   public static void handleGiantShadowGhostAreaAttack(PlayerEntity player, double radius, long sneakDuration) {
      if (player != null && !player.getWorld().isClient() && player instanceof ServerPlayerEntity serverPlayer) {
         List<Entity> nearbyEntities = player.getWorld()
            .getOtherEntities(player, player.getBoundingBox().expand(radius), entityx -> entityx instanceof LivingEntity && entityx != player);
         NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
         float spiritDamage = playerData.contains("spiritDamage") ? (float)playerData.getDouble("spiritDamage") : 50.0F;
         long maxSneakDuration = 200L;
         double damageMultiplier = Math.min((double)sneakDuration / maxSneakDuration, 1.0);
         float actualDamage = (float)(spiritDamage * damageMultiplier);
         if (!(actualDamage <= 0.0F)) {
            for (Entity entity : nearbyEntities) {
               if (entity instanceof LivingEntity livingEntity) {
                  GhostDomainManager.executeSkillSpiritAttack(serverPlayer, livingEntity, actualDamage);
                  livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2, false, false, true));
               }
            }
         }
      }
   }

   public static void handleGhostOfficerPassiveSkill(PlayerEntity player) {
      if (hasGhostOfficerEquipped(player)) {
         if (GhostDomainManager.isGhostDomainActive(player)) {
            if (!player.getWorld().isClient()) {
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
      if (!player.getWorld().isClient) {
         if (hasGhostOfficerEquipped(player)) {
            SyncGhostOfficerQuotaS2CPacket.sendToPlayer((ServerPlayerEntity)player, remainingQuota, maxQuota);
         }
      }
   }

   private static List<LivingEntity> findEntitiesInGhostDomain(PlayerEntity player, int radius) {
      List<LivingEntity> entities = new ArrayList<>();
      List<LivingEntity> entitiesInRange = player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entity -> entity != player && entity instanceof LivingEntity);
      entities.addAll(entitiesInRange);
      return entities;
   }

   public static boolean setPrioritySuppressionTarget(ServerPlayerEntity player, LivingEntity target) {
      int slotCost = calculateEntitySlotCost(target);
      if (slotCost <= 0) {
         return false;
      }

      prioritySuppressionTargets.put(player.getUuid(), target.getUuid());
      LOGGER.info("玩家 {} 设置优先压制目标：{}", player.getName().getString(), target.getName().getString());
      return true;
   }

   public static void addBonusSuppressionSlot(PlayerEntity player, int amount) {
      PlayerEvents.addBonusSuppressionSlots(player, amount);
      UUID uuid = player.getUuid();
      int current = PlayerEvents.getBonusSuppressionSlots(player);
      bonusSuppressionSlots.put(uuid, current);
      LOGGER.info("玩家 {} 获得额外压制名额 +{}，当前额外名额：{}", player.getName().getString(), amount, current);
   }

   public static int getBonusSuppressionSlots(PlayerEntity player) {
      return PlayerEvents.getBonusSuppressionSlots(player);
   }

   public static void clearPrioritySuppressionTarget(ServerPlayerEntity player) {
      prioritySuppressionTargets.remove(player.getUuid());
      LOGGER.info("玩家 {} 清除优先压制目标", player.getName().getString());
   }

   private static LivingEntity getPrioritySuppressionTarget(ServerPlayerEntity player, List<LivingEntity> entities) {
      UUID targetUUID = prioritySuppressionTargets.get(player.getUuid());
      if (targetUUID == null) {
         return null;
      }

      for (LivingEntity entity : entities) {
         if (entity.getUuid().equals(targetUUID)) {
            return entity;
         }
      }

      prioritySuppressionTargets.remove(player.getUuid());
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
            LOGGER.debug("玩家 {} 的优先压制目标 {} 已设置为最高优先级", serverPlayer.getName().getString(), priorityTarget.getName().getString());
         }
      }

      for (LivingEntity entity : sortedEntities) {
         if (!(player instanceof ServerPlayerEntity serverPlayer && isInGhostOfficerWhitelist(serverPlayer, entity))
            && !(entity instanceof PlayerGhostEntity playerGhost && playerGhost.getMasterUuid() != null && playerGhost.getMasterUuid().equals(player.getUuid()))
            && !(entity instanceof GhostChildEntity ghostChild && ghostChild.getMasterUuid() != null && ghostChild.getMasterUuid().equals(player.getUuid()))
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
         double distance1 = player.squaredDistanceTo(entity1);
         double distance2 = player.squaredDistanceTo(entity2);
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
            target.entity.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 100, 0, false, false, true));
         }
      }
   }

   public static void onPlayerAttack(PlayerEntity player, LivingEntity target) {
      if (!player.getWorld().isClient() && hasGhostFistEquipped(player)) {
         ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
         if (mainHandStack.isEmpty()) {
            handleGhostFistPassiveSkill(player, target);
            handleGhostFistActiveSkill(player, target);
         }

         handleGhostFistRevivalDegree(player);
      }
   }

   public static void handleFuneralMusicGhostPassiveSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.getWorld().isClient()) {
            UUID playerId = player.getUuid();
            boolean isPlaying = isPlayingMap.getOrDefault(playerId, false);
            if (isPlaying) {
               NbtCompound spiritData = PlayerEvents.getSpiritAttributes(player);
               double currentSpirit = spiritData.contains("currentSpirit") ? spiritData.getDouble("currentSpirit") : 0.0;
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
                  spiritData.putDouble("currentSpirit", newCurrentSpirit);
                  PlayerEvents.setSpiritAttributes(player, spiritData);
                  skillUseTimeMap.put(playerId, currentTime);
               }
            }
         }
      }
   }

   public static void handleFuneralMusicGhostActiveSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.getWorld().isClient()) {
            UUID playerId = player.getUuid();
            boolean hasTargets = playerMarksMap.containsKey(playerId) && !playerMarksMap.get(playerId).isEmpty();
            triggerAllMarks(player);
            if (!hasTargets) {
               player.sendMessage(Text.literal("§a没有可触发的标记"), true);
            }
         }
      }
   }

   public static void handleFuneralMusicGhostFirstSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.getWorld().isClient()) {
            int purificationDuration = 60;
            if (!player.hasStatusEffect(ModEffects.SILENCE)) {
               purificationDuration = 600;
            }

            player.addStatusEffect(new StatusEffectInstance(ModEffects.PURIFICATION, purificationDuration, 1, false, false, true));
            enterPlayingState(player);
            skillUseTimeMap.put(player.getUuid(), System.currentTimeMillis());
         }
      }
   }

   public static void handleFuneralMusicGhostSecondSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.getWorld().isClient()) {
            exitPlayingState(player);
            int purificationDuration = 60;
            if (!player.hasStatusEffect(ModEffects.SILENCE)) {
               purificationDuration = 600;
            }

            player.addStatusEffect(new StatusEffectInstance(ModEffects.PURIFICATION, purificationDuration, 1, false, false, true));
            skillUseTimeMap.put(player.getUuid(), System.currentTimeMillis());
         }
      }
   }

   public static void handleFuneralMusicGhostThirdSkill(PlayerEntity player) {
      if (hasFuneralMusicGhostEquipped(player)) {
         if (!player.getWorld().isClient()) {
            UUID playerId = player.getUuid();
            boolean hasTargets = playerMarksMap.containsKey(playerId) && !playerMarksMap.get(playerId).isEmpty();
            triggerAllMarks(player, true);
            if (hasTargets) {
               player.sendMessage(Text.literal("§a触发全部标记，目标陷入短暂沉寂"), true);
            } else {
               player.sendMessage(Text.literal("§a没有可触发的标记"), true);
            }
         }
      }
   }

   private static void enterPlayingState(PlayerEntity player) {
      UUID playerId = player.getUuid();
      isPlayingMap.put(playerId, true);
      player.sendMessage(Text.literal("§a进入演奏状态"), true);
   }

   private static void exitPlayingState(PlayerEntity player) {
      UUID playerId = player.getUuid();
      isPlayingMap.put(playerId, false);
      player.sendMessage(Text.literal("§a退出演奏状态"), true);
   }

   private static void addMarksToNearbyEntities(PlayerEntity player) {
      UUID playerId = player.getUuid();
      List<LivingEntity> nearbyEntities = player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(10.0), entityx -> entityx != player);
      if (!playerMarksMap.containsKey(playerId)) {
         playerMarksMap.put(playerId, new HashMap<>());
      }

      Map<UUID, Integer> marks = playerMarksMap.get(playerId);

      for (LivingEntity entity : nearbyEntities) {
         UUID entityId = entity.getUuid();
         int currentMarks = marks.getOrDefault(entityId, 0);
         marks.put(entityId, currentMarks + 1);
      }
   }

   private static void triggerAllMarks(PlayerEntity player) {
      triggerAllMarks(player, false);
   }

   private static void triggerAllMarks(PlayerEntity player, boolean applySilence) {
      UUID playerId = player.getUuid();
      if (playerMarksMap.containsKey(playerId)) {
         Map<UUID, Integer> marks = playerMarksMap.get(playerId);

         for (Entry<UUID, Integer> entry : marks.entrySet()) {
            UUID entityId = entry.getKey();
            int markLevel = entry.getValue();
            LivingEntity target = null;

            for (LivingEntity entity : player.getWorld().getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(100.0), entityx -> true)) {
               if (entity.getUuid().equals(entityId)) {
                  target = entity;
                  break;
               }
            }

            if (target != null && target.isAlive()) {
               if (applySilence) {
                  target.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 40, 0, false, false, true));
               } else {
                  float damage = markLevel * 250;
                  ModEvents.processingSpiritDamage.set(true);

                  try {
                     if (player instanceof ServerPlayerEntity serverPlayer) {
                        DamageSource damageSource = serverPlayer.getDamageSources().playerAttack(serverPlayer);
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
      return source.getAttacker() instanceof PlayerEntity player ? ModEvents.processingSpiritDamage.get() : false;
   }

   public static void handleGhostFistRevivalDegree(PlayerEntity player) {
      if (hasGhostFistEquipped(player)) {
         if (!player.getWorld().isClient()) {
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
         if (!player.getWorld().isClient()) {
            for (PlayerEntity target : player.getWorld()
               .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(10.0), targetx -> targetx != player && targetx.isSneaking())) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
                  float spiritDamage = playerData.contains("spiritDamage") ? (float)playerData.getDouble("spiritDamage") : 50.0F;
                  boolean bothEquipped = GhostDomainManager.hasJumpGhost(player) && hasSneakGhostEquipped(player);
                  if (bothEquipped) {
                     spiritDamage *= 2.0F;
                  }

                  DamageSource damageSource = player.getDamageSources().playerAttack(player);
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
         if (!player.getWorld().isClient()) {
            for (PlayerEntity target : player.getWorld()
               .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(10.0), targetx -> targetx != player && !hasAnyEquipment(targetx))) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
                  float spiritDamage = playerData.contains("spiritDamage") ? (float)playerData.getDouble("spiritDamage") : 50.0F;
                  DamageSource damageSource = player.getDamageSources().playerAttack(player);
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
         if (!player.getWorld().isClient()) {
            List<LivingEntity> markedEntities = new ArrayList<>();

            for (LivingEntity target : player.getWorld()
               .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(10.0), targetx -> targetx != player)) {
               if (target.hasStatusEffect(ModEffects.MARK_CURSE)) {
                  markedEntities.add(target);
               }
            }

            if (markedEntities.isEmpty()) {
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.sendMessage(Text.literal("§c木偶鬼：范围内没有被标记的生物"), true);
               }

               return;
            }

            for (LivingEntity target : markedEntities) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
                  float spiritDamage = playerData.contains("spiritDamage") ? (float)playerData.getDouble("spiritDamage") : 50.0F;
                  DamageSource damageSource = player.getDamageSources().playerAttack(player);
                  if (target instanceof ServerPlayerEntity targetPlayer) {
                     handleSkillDamageToPlayer((ServerPlayerEntity)player, targetPlayer, damageSource, spiritDamage);
                  } else {
                     target.damage(damageSource, spiritDamage);
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
         if (!player.getWorld().isClient()) {
            for (LivingEntity target : player.getWorld()
               .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(10.0), targetx -> targetx != player)) {
               if (target.getVelocity().lengthSquared() < 0.01 && !target.hasStatusEffect(ModEffects.MARK_CURSE)) {
                  target.addStatusEffect(new StatusEffectInstance(ModEffects.MARK_CURSE, 200, 0, false, false, true));
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.sendMessage(Text.literal("§a木偶鬼标记了静止的生物: " + target.getName().getString()), true);
                  }
               }
            }
         }
      }
   }

   public static void handlePuppetGhostResistanceSkill(PlayerEntity player) {
      if (hasPuppetGhostEquipped(player)) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         if (!spiritAttributes.contains("tempSpiritResistance")) {
            spiritAttributes.putDouble("tempSpiritResistance", 0.0);
         }

         if (!spiritAttributes.contains("lastPositionX")) {
            spiritAttributes.putDouble("lastPositionX", player.getX());
            spiritAttributes.putDouble("lastPositionY", player.getY());
            spiritAttributes.putDouble("lastPositionZ", player.getZ());
            spiritAttributes.putInt("staticTicks", 0);
            spiritAttributes.putInt("checkTicks", 0);
         }

         double lastX = spiritAttributes.getDouble("lastPositionX");
         double lastY = spiritAttributes.getDouble("lastPositionY");
         double lastZ = spiritAttributes.getDouble("lastPositionZ");
         Vec3d lastPosition = new Vec3d(lastX, lastY, lastZ);
         Vec3d currentPosition = player.getPos();
         double distance = currentPosition.distanceTo(lastPosition);
         if (distance < 0.1) {
            int staticTicks = spiritAttributes.getInt("staticTicks") + 20;
            int checkTicks = spiritAttributes.getInt("checkTicks") + 20;
            spiritAttributes.putInt("staticTicks", staticTicks);
            spiritAttributes.putInt("checkTicks", checkTicks);
            if (checkTicks >= 20) {
               if (staticTicks >= 100) {
                  double currentTempResistance = spiritAttributes.getDouble("tempSpiritResistance");
                  double newTempResistance = Math.min(currentTempResistance + 10.0, 2000.0);
                  spiritAttributes.putDouble("tempSpiritResistance", newTempResistance);
                  spiritAttributes.putInt("staticTicks", 0);
               }

               spiritAttributes.putInt("checkTicks", 0);
               PlayerEvents.setSpiritAttributes(player, spiritAttributes);
            } else {
               PlayerEvents.setSpiritAttributes(player, spiritAttributes);
            }
         } else {
            spiritAttributes.putDouble("lastPositionX", player.getX());
            spiritAttributes.putDouble("lastPositionY", player.getY());
            spiritAttributes.putDouble("lastPositionZ", player.getZ());
            spiritAttributes.putInt("staticTicks", 0);
            spiritAttributes.putInt("checkTicks", 0);
            spiritAttributes.putDouble("tempSpiritResistance", 0.0);
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
         }
      }
   }

   public static void handleSneakGhostPassiveSkill(PlayerEntity player) {
      if (hasSneakGhostEquipped(player)) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         if (player.isSneaking()) {
            if (!spiritAttributes.contains("normalSpiritDamage")) {
               spiritAttributes.putInt("normalSpiritDamage", spiritAttributes.getInt("spiritDamage"));
            }

            if (!spiritAttributes.contains("normalSpiritResistance")) {
               spiritAttributes.putInt("normalSpiritResistance", spiritAttributes.getInt("spiritResistance"));
            }

            int baseDamage = spiritAttributes.getInt("normalSpiritDamage");
            int baseResistance = spiritAttributes.getInt("normalSpiritResistance");
            int enhancedDamage = (int)(baseDamage * 1.15);
            int enhancedResistance = (int)(baseResistance * 1.2);
            spiritAttributes.putDouble("tempSpiritDamage", enhancedDamage - baseDamage);
            spiritAttributes.putDouble("tempSpiritResistance", enhancedResistance - baseResistance);
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
         } else {
            spiritAttributes.putDouble("tempSpiritDamage", 0.0);
            spiritAttributes.putDouble("tempSpiritResistance", 0.0);
            spiritAttributes.remove("normalSpiritDamage");
            spiritAttributes.remove("normalSpiritResistance");
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
         }
      }
   }

   public static void handleClothesGhostPassiveSkill(PlayerEntity player) {
      if (!hasNoClothesGhostEquipped(player)) {
         if (!player.getWorld().isClient()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
               ItemStack stack = player.getEquippedStack(slot);
               if (stack.isDamageable()
                  && (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET)
                  && stack.getDamage() > 0) {
                  stack.setDamage(stack.getDamage() - 1);
               }
            }
         }
      }
   }

   private static boolean hasAnyEquipment(PlayerEntity player) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (!player.getEquippedStack(slot).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private static boolean isPlayerStationary(PlayerEntity player) {
      UUID playerId = player.getUuid();
      boolean currentState = player.getVelocity().lengthSquared() < 0.001;
      if (!playerStationaryState.containsKey(playerId) || playerStationaryState.get(playerId) != currentState) {
         playerStationaryState.put(playerId, currentState);
      }

      return playerStationaryState.get(playerId);
   }

   private static void spawnGhostSkillParticles(PlayerEntity player, LivingEntity target) {
      if (!player.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)player.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 20; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 1.5;
            double offsetY = player.getRandom().nextDouble() * 1.0 + 0.5;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 1.5;
            double velocityX = (player.getRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = player.getRandom().nextDouble() * 0.3 + 0.1;
            double velocityZ = (player.getRandom().nextDouble() - 0.5) * 0.2;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
         }
      }
   }

   private static void spawnGhostFistAttackParticles(PlayerEntity player, LivingEntity target) {
      if (!player.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)player.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 25; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = player.getRandom().nextDouble() * 1.5 + 0.5;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2.0;
            double velocityX = (player.getRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = player.getRandom().nextDouble() * 0.3 + 0.1;
            double velocityZ = (player.getRandom().nextDouble() - 0.5) * 0.2;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
         }
      }
   }

   private static void spawnGhostFistPassiveParticles(PlayerEntity player, LivingEntity target) {
      if (!player.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)player.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 15; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = player.getRandom().nextDouble() * 2.0;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2.0;
            serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.1, 0.0, 0.1);
         }
      }
   }

   public static boolean isInGhostOfficerWhitelist(ServerPlayerEntity player, LivingEntity target) {
      List<UUID> whitelist = ghostOfficerWhitelist.get(player.getUuid());
      return whitelist == null ? false : whitelist.contains(target.getUuid());
   }

   public static boolean removeFromSuppressionAndAddToWhitelist(ServerPlayerEntity player, LivingEntity target) {
      if (!target.hasStatusEffect(ModEffects.SILENCE)) {
         return false;
      }

      target.removeStatusEffect(ModEffects.SILENCE);
      List<UUID> whitelist = ghostOfficerWhitelist.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());
      if (!whitelist.contains(target.getUuid())) {
         whitelist.add(target.getUuid());
      }

      LOGGER.info("玩家 {} 使用鬼差V键技能解除 {} 的压制并加入白名单", player.getName().getString(), target.getName().getString());
      return true;
   }

   public static boolean restoreSuppression(ServerPlayerEntity player, LivingEntity target) {
      if (!isInGhostOfficerWhitelist(player, target)) {
         return false;
      }

      List<UUID> whitelist = ghostOfficerWhitelist.get(player.getUuid());
      if (whitelist != null) {
         whitelist.remove(target.getUuid());
         if (whitelist.isEmpty()) {
            ghostOfficerWhitelist.remove(player.getUuid());
         }
      }

      target.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 100, 0, false, false, true));
      LOGGER.info("玩家 {} 使用鬼差V键技能恢复对 {} 的压制", player.getName().getString(), target.getName().getString());
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
            List<ZombieEntity> zombies = player.getWorld()
               .getEntitiesByClass(
                  ZombieEntity.class,
                  new Box(
                     player.getBlockPos().getX() - domainRadius,
                     player.getBlockPos().getY() - domainRadius,
                     player.getBlockPos().getZ() - domainRadius,
                     player.getBlockPos().getX() + domainRadius,
                     player.getBlockPos().getY() + domainRadius,
                     player.getBlockPos().getZ() + domainRadius
                  ),
                  zombie -> zombie.isAlive() && zombie.distanceTo(player) <= domainRadius
               );
            if (!zombies.isEmpty()) {
               ZombieEntity targetZombie = zombies.stream().min((z1, z2) -> Float.compare(z1.distanceTo(player), z2.distanceTo(player))).orElse(null);
               if (targetZombie != null) {
                  VillagerEntity villager = (VillagerEntity)targetZombie.convertTo(EntityType.VILLAGER, false);
                  if (villager != null && player.getWorld() instanceof ServerWorld serverWorld) {
                     serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
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
         if (!player.getWorld().isClient()) {
            BlockPos posBelow = player.getBlockPos().down();
            BlockState blockState = player.getWorld().getBlockState(posBelow);
            Block block = blockState.getBlock();
            if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK) {
               player.getWorld().setBlockState(posBelow, (BlockState)Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7), 3);
               BlockPos cropPos = posBelow.up();
               if (player.getWorld().getBlockState(cropPos).isAir()) {
                  Random random = new Random();
                  int cropType = random.nextInt(4);

                  player.getWorld().setBlockState(cropPos, switch (cropType) {
                     case 0 -> (BlockState)Blocks.WHEAT.getDefaultState().with(CropBlock.AGE, random.nextInt(4));
                     case 1 -> (BlockState)Blocks.CARROTS.getDefaultState().with(CropBlock.AGE, random.nextInt(4));
                     case 2 -> (BlockState)Blocks.POTATOES.getDefaultState().with(CropBlock.AGE, random.nextInt(4));
                     default -> (BlockState)Blocks.BEETROOTS.getDefaultState().with(Properties.AGE_3, random.nextInt(3));
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
