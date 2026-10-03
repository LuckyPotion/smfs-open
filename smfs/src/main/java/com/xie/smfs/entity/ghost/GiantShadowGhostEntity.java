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
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.0);
      }

      EntityAttributeInstance attackDamageAttribute = this.method_5996(EntityAttributes.field_23721);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.method_6192(12.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.method_5996(EntityAttributes.field_23722);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.method_6192(0.5);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(32.0);
      }
   }

   public static Builder createAttributes() {
      return GhostEntity.createGhostAttributes()
         .method_26868(EntityAttributes.field_23716, 100000.0)
         .method_26868(EntityAttributes.field_23719, 0.0)
         .method_26868(EntityAttributes.field_23721, 12.0)
         .method_26868(EntityAttributes.field_23722, 0.5)
         .method_26868(EntityAttributes.field_23717, 32.0);
   }

   public void method_6091(Vec3d movementInput) {
      if (this.method_24828()) {
         if (this.method_5942().method_23966()) {
            super.method_6091(movementInput);
         } else {
            this.method_18799(Vec3d.field_1353);
         }
      } else {
         super.method_6091(movementInput);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.moveCooldown > 0) {
            this.moveCooldown--;
         }

         if (this.isSuppressed() || this.isDeadlocked()) {
            this.method_18799(Vec3d.field_1353);
            this.method_5942().method_6340();
            return;
         }

         PlayerEntity nearestPlayer = this.method_37908().method_18460(this, 32.0);
         if (nearestPlayer != null) {
            this.handlePlayerInteraction(nearestPlayer);
         }
      }
   }

   private void handlePlayerInteraction(PlayerEntity player) {
      double distance = this.method_5739(player);
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
      Vec3d playerLookVec = player.method_5828(1.0F).method_1029();
      Vec3d toGhostVec = new Vec3d(
            this.method_23317() - player.method_23317(), this.method_23318() - player.method_23318(), this.method_23321() - player.method_23321()
         )
         .method_1029();
      double dotProduct = playerLookVec.method_1026(toGhostVec);
      return dotProduct < 0.0;
   }

   private void moveTowardsPlayer(PlayerEntity player) {
      Vec3d direction = new Vec3d(
            player.method_23317() - this.method_23317(), player.method_23318() - this.method_23318(), player.method_23321() - this.method_23321()
         )
         .method_1029();
      double distanceToPlayer = this.method_5739(player);
      double actualMoveDistance = Math.min(8.0, distanceToPlayer - 1.0);
      if (actualMoveDistance > 0.0) {
         double targetX = this.method_23317() + direction.field_1352 * actualMoveDistance;
         double targetY = this.method_23318() + direction.field_1351 * actualMoveDistance;
         double targetZ = this.method_23321() + direction.field_1350 * actualMoveDistance;
         this.method_20620(targetX, targetY, targetZ);
         LOGGER.debug("高大鬼影瞬移到玩家: 距离={}, 移动距离={}, 目标位置=({}, {}, {})", distanceToPlayer, actualMoveDistance, targetX, targetY, targetZ);
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.isPlayerFacingAway(player) && this.method_5739(player) <= 4.0 && this.attackCooldown <= 0;
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
