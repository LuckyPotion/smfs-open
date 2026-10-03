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
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(80000.0);
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
         followRangeAttribute.method_6192(20.0);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (!this.isSuppressed() && !this.isDeadlocked() && !this.isMovementDisabled()) {
            if (this.field_6012 % 120 == 0 && this.getGhostRandom().nextBoolean()) {
               double radius = 10.0;
               double x = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
               double z = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
               this.method_5942().method_6337(x, this.method_23318(), z, 0.8);
            }

            if (this.field_6012 % 20 == 0) {
               this.checkStationaryPlayers();
            }
         } else {
            this.method_18799(Vec3d.field_1353);
            this.method_5942().method_6340();
         }
      }
   }

   private void checkStationaryPlayers() {
      if (this.method_37908() instanceof ServerWorld serverWorld) {
         double var8 = 20.0;

         for (PlayerEntity player : this.method_37908().method_18456()) {
            if (!this.isPlayerProtected(player) && !(this.method_5858(player) > var8 * var8)) {
               Vec3d currentPos = player.method_19538();
               Vec3d prevPos = new Vec3d(player.field_6014, player.field_6036, player.field_5969);
               if (currentPos.method_1025(prevPos) < 0.01) {
                  this.stationaryPlayerTimer += 20;
                  if (this.stationaryPlayerTimer >= 60) {
                     this.spawnGraveMoundAt(serverWorld, player.method_24515());
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

      while (groundPos.method_10264() > world.method_31607() && world.method_8320(groundPos).method_26215()) {
         groundPos = groundPos.method_10074();
      }

      BlockPos placePos = groundPos.method_10084();
      if (world.method_8320(placePos).method_26215()) {
         world.method_8501(placePos, ModBlocks.GRAVE_MOUND.method_9564());
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.method_5858(player) <= 400.0 && this.attackCooldown <= 0;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
