package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TaitouGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';
   private int headUpTicks = 0;

   public TaitouGhostEntity(EntityType<TaitouGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1200, 120, 60, 0.3F);
      this.ghostLevel = 3;
      this.attackCooldown = 0;
      this.initHeadUpAttributes();
   }

   private void initHeadUpAttributes() {
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(100000.0);
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
         followRangeAttribute.setBaseValue(32.0);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         if (this.isSuppressed() || this.isDead()) {
            this.setVelocity(Vec3d.ZERO);
            this.getNavigation().stop();
         } else if (this.age % 200 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 8.0;
            double x = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.getNavigation().startMovingTo(x, this.getY(), z, 0.8);
         }

         if (this.isSuppressed() || this.isDeadlocked()) {
            this.setPitch(0.0F);
         } else if (this.headUpTicks > 0) {
            this.headUpTicks--;
            this.setPitch(-60.0F);
         } else if (this.getGhostRandom().nextInt(300) < 5) {
            this.headUpTicks = 60;
            this.setPitch(-60.0F);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && player.getPitch() < -30.0F && this.squaredDistanceTo(player) <= 1024.0 && this.attackCooldown <= 0;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      if (this.isDeadlocked()) {
         return false;
      } else {
         return ghost.isDeadlocked() ? false : ghost instanceof DitouGhostEntity && this.squaredDistanceTo(ghost) <= 256.0 && this.attackCooldown <= 0;
      }
   }

   public boolean isHeadUp() {
      return this.headUpTicks > 0;
   }

   public int getHeadUpTicks() {
      return this.headUpTicks;
   }
}
