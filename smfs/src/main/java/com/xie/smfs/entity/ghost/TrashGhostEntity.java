package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TrashGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';

   public TrashGhostEntity(EntityType<TrashGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1000, 375, 35, 0.15F);
      this.ghostLevel = 3;
      this.attackCooldown = 45;
      this.initTrashGhostAttributes();
   }

   private void initTrashGhostAttributes() {
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
         attackDamageAttribute.setBaseValue(7.0);
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
         if (this.isSuppressed() || this.isDead()) {
            this.setVelocity(Vec3d.ZERO);
            this.getNavigation().stop();
         } else if (this.age % 180 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 6.0;
            double x = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.getNavigation().startMovingTo(x, this.getY(), z, 0.6);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player)
         && this.hasItemsNearPlayer(player, 6.0)
         && this.squaredDistanceTo(player) <= 400.0
         && this.attackCooldown <= 0;
   }

   private boolean hasItemsNearPlayer(PlayerEntity player, double radius) {
      List<ItemEntity> items = player.getWorld().getEntitiesByClass(ItemEntity.class, player.getBoundingBox().expand(radius), item -> true);
      return !items.isEmpty();
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
