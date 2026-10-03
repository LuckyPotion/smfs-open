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
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.2);
      }

      EntityAttributeInstance attackDamageAttribute = this.method_5996(EntityAttributes.field_23721);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.method_6192(6.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.method_5996(EntityAttributes.field_23722);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.method_6192(0.0);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(32.0);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.isSuppressed() || this.method_29504()) {
            this.method_18799(Vec3d.field_1353);
            this.method_5942().method_6340();
         } else if (this.field_6012 % 200 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 8.0;
            double x = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.method_5942().method_6337(x, this.method_23318(), z, 0.8);
         }

         if (this.isSuppressed() || this.isDeadlocked()) {
            this.method_36457(0.0F);
         } else if (this.headUpTicks > 0) {
            this.headUpTicks--;
            this.method_36457(-60.0F);
         } else if (this.getGhostRandom().nextInt(300) < 5) {
            this.headUpTicks = 60;
            this.method_36457(-60.0F);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && player.method_36455() < -30.0F && this.method_5858(player) <= 1024.0 && this.attackCooldown <= 0;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      if (this.isDeadlocked()) {
         return false;
      } else {
         return ghost.isDeadlocked() ? false : ghost instanceof DitouGhostEntity && this.method_5858(ghost) <= 256.0 && this.attackCooldown <= 0;
      }
   }

   public boolean isHeadUp() {
      return this.headUpTicks > 0;
   }

   public int getHeadUpTicks() {
      return this.headUpTicks;
   }
}
