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
         attackDamageAttribute.method_6192(7.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.method_5996(EntityAttributes.field_23722);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.method_6192(0.0);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(20.0);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.isSuppressed() || this.method_29504()) {
            this.method_18799(Vec3d.field_1353);
            this.method_5942().method_6340();
         } else if (this.field_6012 % 180 == 0 && this.getGhostRandom().nextBoolean()) {
            double radius = 6.0;
            double x = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double z = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            this.method_5942().method_6337(x, this.method_23318(), z, 0.6);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player)
         && this.hasItemsNearPlayer(player, 6.0)
         && this.method_5858(player) <= 400.0
         && this.attackCooldown <= 0;
   }

   private boolean hasItemsNearPlayer(PlayerEntity player, double radius) {
      List<ItemEntity> items = player.method_37908().method_8390(ItemEntity.class, player.method_5829().method_1014(radius), item -> true);
      return !items.isEmpty();
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
