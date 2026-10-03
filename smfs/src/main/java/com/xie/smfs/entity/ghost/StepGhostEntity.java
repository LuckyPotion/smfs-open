package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class StepGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';

   public StepGhostEntity(EntityType<StepGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1100, 90, 45, 0.25F);
      this.ghostLevel = 3;
      this.attackCooldown = 35;
      this.initStepGhostAttributes();
   }

   private void initStepGhostAttributes() {
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.3);
      }

      EntityAttributeInstance attackDamageAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.setBaseValue(9.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.setBaseValue(0.5);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(28.0);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         if (this.isSuppressed() || this.isDead()) {
            this.setVelocity(Vec3d.ZERO);
            this.getNavigation().stop();
         } else if (this.age % 120 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 12.0;
            double x = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.getNavigation().startMovingTo(x, this.getY(), z, 1.0);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player)
         && player.getY() < this.getY() - 1.0
         && this.squaredDistanceTo(player) <= 784.0
         && this.attackCooldown <= 0;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
