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
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.3);
      }

      EntityAttributeInstance attackDamageAttribute = this.method_5996(EntityAttributes.field_23721);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.method_6192(9.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.method_5996(EntityAttributes.field_23722);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.method_6192(0.5);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(28.0);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.isSuppressed() || this.method_29504()) {
            this.method_18799(Vec3d.field_1353);
            this.method_5942().method_6340();
         } else if (this.field_6012 % 120 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 12.0;
            double x = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.method_5942().method_6337(x, this.method_23318(), z, 1.0);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player)
         && player.method_23318() < this.method_23318() - 1.0
         && this.method_5858(player) <= 784.0
         && this.attackCooldown <= 0;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
