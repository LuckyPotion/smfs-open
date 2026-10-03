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
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.25);
      }

      EntityAttributeInstance attackDamageAttribute = this.method_5996(EntityAttributes.field_23721);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.method_6192(8.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.method_5996(EntityAttributes.field_23722);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.method_6192(0.0);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(24.0);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.isSuppressed() || this.isDeadlocked() || this.isMovementDisabled()) {
            this.method_18799(Vec3d.field_1353);
            this.method_5942().method_6340();
            return;
         }

         if (this.field_6012 % 150 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 10.0;
            double x = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.method_5942().method_6337(x, this.method_23318(), z, 0.8);
         }

         if (this.field_6012 % 100 == 0) {
            this.performFarmingActions();
         }
      }
   }

   private void performFarmingActions() {
      if (this.method_37908() instanceof ServerWorld serverWorld) {
         BlockPos var9 = this.method_24515();
         int radius = 8;

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               for (int y = -2; y <= 2; y++) {
                  BlockPos checkPos = var9.method_10069(x, y, z);
                  BlockState blockState = serverWorld.method_8320(checkPos);
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
      BlockState blockState = world.method_8320(pos);
      BlockState aboveState = world.method_8320(pos.method_10084());
      return (blockState.method_27852(Blocks.field_10219) || blockState.method_27852(Blocks.field_10566) || blockState.method_27852(Blocks.field_10253))
         && aboveState.method_26215();
   }

   private void tillLand(ServerWorld world, BlockPos pos) {
      BlockState blockState = world.method_8320(pos);
      if (blockState.method_27852(Blocks.field_10219)) {
         world.method_8501(pos, Blocks.field_10362.method_9564());
      } else if (blockState.method_27852(Blocks.field_10566) || blockState.method_27852(Blocks.field_10253)) {
         world.method_8501(pos, Blocks.field_10362.method_9564());
      }

      world.method_14199(ParticleTypes.field_11211, pos.method_10263() + 0.5, pos.method_10264() + 1, pos.method_10260() + 0.5, 5, 0.3, 0.3, 0.3, 0.1);
   }

   private boolean canPlantCrops(ServerWorld world, BlockPos pos) {
      BlockState blockState = world.method_8320(pos);
      BlockState aboveState = world.method_8320(pos.method_10084());
      return blockState.method_27852(Blocks.field_10362) && aboveState.method_26215();
   }

   private void plantCrops(ServerWorld world, BlockPos pos) {
      BlockPos cropPos = pos.method_10084();
      int cropType = this.getGhostRandom().nextInt(4);
      switch (cropType) {
         case 0:
            world.method_8501(cropPos, Blocks.field_10293.method_9564());
            break;
         case 1:
            world.method_8501(cropPos, Blocks.field_10609.method_9564());
            break;
         case 2:
            world.method_8501(cropPos, Blocks.field_10247.method_9564());
            break;
         case 3:
            world.method_8501(cropPos, Blocks.field_10341.method_9564());
      }

      world.method_14199(
         ParticleTypes.field_11211, cropPos.method_10263() + 0.5, cropPos.method_10264() + 0.5, cropPos.method_10260() + 0.5, 5, 0.3, 0.3, 0.3, 0.1
      );
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      boolean hasGoldenBoots = ((ItemStack)player.method_31548().field_7548.get(0)).method_7909() == Items.field_8753;
      return !RedGhostCandleItem.isHoldingCandle(player)
         && !hasGoldenBoots
         && this.isPlayerOnCrops(player)
         && this.method_5858(player) <= 576.0
         && this.attackCooldown <= 0;
   }

   private boolean isPlayerOnCrops(PlayerEntity player) {
      return player.method_37908().method_8320(player.method_24515().method_10074()).method_26164(BlockTags.field_20341)
         || player.method_37908().method_8320(player.method_24515()).method_26164(BlockTags.field_20341);
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
