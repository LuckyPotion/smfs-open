package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.registry.ModBlocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GraveEarthGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = false;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 0.0;
   private static final char TERROR_LEVEL = 'C';
   private int stationaryPlayerTimer = 0;
   private static final int STATIONARY_THRESHOLD = 60;

   public GraveEarthGhostEntity(EntityType<GraveEarthGhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 0.0, 'C', 1800, 60, 30, 0.15F);
      this.ghostLevel = 2;
      this.attackCooldown = 40;
      this.initGraveEarthGhostAttributes();
   }

   private void initGraveEarthGhostAttributes() {
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(80000.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.2);
      }

      EntityAttributeInstance attackDamageAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.setBaseValue(6.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.setBaseValue(0.0);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(20.0);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         if (!this.isSuppressed() && !this.isDeadlocked() && !this.isMovementDisabled()) {
            if (this.age % 120 == 0 && this.getGhostRandom().nextBoolean()) {
               double radius = 10.0;
               double x = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
               double z = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
               this.getNavigation().startMovingTo(x, this.getY(), z, 0.8);
            }

            if (this.age % 20 == 0) {
               this.checkStationaryPlayers();
            }
         } else {
            this.setVelocity(Vec3d.ZERO);
            this.getNavigation().stop();
         }
      }
   }

   private void checkStationaryPlayers() {
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         double var8 = 20.0;

         for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (!this.isPlayerProtected(player) && !(this.squaredDistanceTo(player) > var8 * var8)) {
               Vec3d currentPos = player.getPos();
               Vec3d prevPos = new Vec3d(player.prevX, player.prevY, player.prevZ);
               if (currentPos.squaredDistanceTo(prevPos) < 0.01) {
                  this.stationaryPlayerTimer += 20;
                  if (this.stationaryPlayerTimer >= 60) {
                     this.spawnGraveMoundAt(serverWorld, player.getBlockPos());
                     this.stationaryPlayerTimer = 0;
                  }
               } else {
                  this.stationaryPlayerTimer = 0;
               }
            }
         }
      }
   }

   private void spawnGraveMoundAt(ServerWorld world, BlockPos pos) {
      BlockPos groundPos = pos;

      while (groundPos.getY() > world.getBottomY() && world.getBlockState(groundPos).isAir()) {
         groundPos = groundPos.down();
      }

      BlockPos placePos = groundPos.up();
      if (world.getBlockState(placePos).isAir()) {
         world.setBlockState(placePos, ModBlocks.GRAVE_MOUND.getDefaultState());
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.squaredDistanceTo(player) <= 400.0 && this.attackCooldown <= 0;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
