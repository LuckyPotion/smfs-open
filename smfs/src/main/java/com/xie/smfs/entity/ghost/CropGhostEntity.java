package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CropGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';

   public CropGhostEntity(EntityType<CropGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1500, 180, 40, 0.2F);
      this.ghostLevel = 3;
      this.attackCooldown = 40;
      this.initCropGhostAttributes();
   }

   private void initCropGhostAttributes() {
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.25);
      }

      EntityAttributeInstance attackDamageAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.setBaseValue(8.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.setBaseValue(0.0);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(24.0);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         if (this.isSuppressed() || this.isDeadlocked() || this.isMovementDisabled()) {
            this.setVelocity(Vec3d.ZERO);
            this.getNavigation().stop();
            return;
         }

         if (this.age % 150 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 10.0;
            double x = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.getNavigation().startMovingTo(x, this.getY(), z, 0.8);
         }

         if (this.age % 100 == 0) {
            this.performFarmingActions();
         }
      }
   }

   private void performFarmingActions() {
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         BlockPos var9 = this.getBlockPos();
         int radius = 8;

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               for (int y = -2; y <= 2; y++) {
                  BlockPos checkPos = var9.add(x, y, z);
                  BlockState blockState = serverWorld.getBlockState(checkPos);
                  if (this.canTillLand(serverWorld, checkPos) && this.getGhostRandom().nextBoolean()) {
                     this.tillLand(serverWorld, checkPos);
                  }

                  if (this.canPlantCrops(serverWorld, checkPos) && this.getGhostRandom().nextBoolean()) {
                     this.plantCrops(serverWorld, checkPos);
                  }
               }
            }
         }
      }
   }

   private boolean canTillLand(ServerWorld world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      BlockState aboveState = world.getBlockState(pos.up());
      return (blockState.isOf(Blocks.GRASS_BLOCK) || blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.COARSE_DIRT)) && aboveState.isAir();
   }

   private void tillLand(ServerWorld world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      if (blockState.isOf(Blocks.GRASS_BLOCK)) {
         world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
      } else if (blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.COARSE_DIRT)) {
         world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
      }

      world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.1);
   }

   private boolean canPlantCrops(ServerWorld world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      BlockState aboveState = world.getBlockState(pos.up());
      return blockState.isOf(Blocks.FARMLAND) && aboveState.isAir();
   }

   private void plantCrops(ServerWorld world, BlockPos pos) {
      BlockPos cropPos = pos.up();
      int cropType = this.getGhostRandom().nextInt(4);
      switch (cropType) {
         case 0:
            world.setBlockState(cropPos, Blocks.WHEAT.getDefaultState());
            break;
         case 1:
            world.setBlockState(cropPos, Blocks.CARROTS.getDefaultState());
            break;
         case 2:
            world.setBlockState(cropPos, Blocks.POTATOES.getDefaultState());
            break;
         case 3:
            world.setBlockState(cropPos, Blocks.BEETROOTS.getDefaultState());
      }

      world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, cropPos.getX() + 0.5, cropPos.getY() + 0.5, cropPos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.1);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      boolean hasGoldenBoots = ((ItemStack)player.getInventory().armor.get(0)).getItem() == Items.GOLDEN_BOOTS;
      return !RedGhostCandleItem.isHoldingCandle(player)
         && !hasGoldenBoots
         && this.isPlayerOnCrops(player)
         && this.squaredDistanceTo(player) <= 576.0
         && this.attackCooldown <= 0;
   }

   private boolean isPlayerOnCrops(PlayerEntity player) {
      return player.getWorld().getBlockState(player.getBlockPos().down()).isIn(BlockTags.CROPS)
         || player.getWorld().getBlockState(player.getBlockPos()).isIn(BlockTags.CROPS);
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
