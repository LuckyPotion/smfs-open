package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GiantShadowGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 48.0;
   private static final char TERROR_LEVEL = 'B';
   private int moveCooldown = 0;
   private static final int MOVE_COOLDOWN_TICKS = 60;
   private static final double MOVE_DISTANCE = 8.0;
   private static final double ATTACK_DISTANCE = 4.0;

   public GiantShadowGhostEntity(EntityType<GiantShadowGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 48.0, 'B', 2500, 650, 110, 0.25F);
      this.ghostLevel = 2;
      this.attackCooldown = 30;
      this.setEnableChaseAfterRule(false);
      this.initGiantShadowAttributes();
   }

   private void initGiantShadowAttributes() {
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.0);
      }

      EntityAttributeInstance attackDamageAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.setBaseValue(12.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.setBaseValue(0.5);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(32.0);
      }
   }

   public static Builder createAttributes() {
      return GhostEntity.createGhostAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 100000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
         .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.5)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
   }

   public void travel(Vec3d movementInput) {
      if (this.isOnGround()) {
         if (this.getNavigation().isFollowingPath()) {
            super.travel(movementInput);
         } else {
            this.setVelocity(Vec3d.ZERO);
         }
      } else {
         super.travel(movementInput);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         if (this.moveCooldown > 0) {
            this.moveCooldown--;
         }

         if (this.isSuppressed() || this.isDeadlocked()) {
            this.setVelocity(Vec3d.ZERO);
            this.getNavigation().stop();
            return;
         }

         PlayerEntity nearestPlayer = this.getWorld().getClosestPlayer(this, 32.0);
         if (nearestPlayer != null) {
            this.handlePlayerInteraction(nearestPlayer);
         }
      }
   }

   private void handlePlayerInteraction(PlayerEntity player) {
      double distance = this.distanceTo(player);
      boolean isPlayerFacingAway = this.isPlayerFacingAway(player);
      if (isPlayerFacingAway) {
         if (this.moveCooldown <= 0) {
            this.moveTowardsPlayer(player);
            this.moveCooldown = 60;
         }

         if (this.shouldAttackPlayer(player)) {
            this.executeAttack(player);
         }
      }
   }

   private boolean isPlayerFacingAway(PlayerEntity player) {
      Vec3d playerLookVec = player.getRotationVec(1.0F).normalize();
      Vec3d toGhostVec = new Vec3d(this.getX() - player.getX(), this.getY() - player.getY(), this.getZ() - player.getZ()).normalize();
      double dotProduct = playerLookVec.dotProduct(toGhostVec);
      return dotProduct < 0.0;
   }

   private void moveTowardsPlayer(PlayerEntity player) {
      Vec3d direction = new Vec3d(player.getX() - this.getX(), player.getY() - this.getY(), player.getZ() - this.getZ()).normalize();
      double distanceToPlayer = this.distanceTo(player);
      double actualMoveDistance = Math.min(8.0, distanceToPlayer - 1.0);
      if (actualMoveDistance > 0.0) {
         double targetX = this.getX() + direction.x * actualMoveDistance;
         double targetY = this.getY() + direction.y * actualMoveDistance;
         double targetZ = this.getZ() + direction.z * actualMoveDistance;
         this.teleport(targetX, targetY, targetZ);
         LOGGER.debug("高大鬼影瞬移到玩家: 距离={}, 移动距离={}, 目标位置=({}, {}, {})", distanceToPlayer, actualMoveDistance, targetX, targetY, targetZ);
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.isPlayerFacingAway(player) && this.distanceTo(player) <= 4.0 && this.attackCooldown <= 0;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }

   public boolean isOnMoveCooldown() {
      return this.moveCooldown > 0;
   }

   public int getMoveCooldownTicks() {
      return this.moveCooldown;
   }
}
