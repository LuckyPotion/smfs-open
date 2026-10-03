package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WaterGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'B';

   public WaterGhostEntity(EntityType<WaterGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'B', 1300, 110, 60, 0.3F);
      this.ghostLevel = 4;
      this.attackCooldown = 30;
      this.initWaterGhostAttributes();
   }

   private void initWaterGhostAttributes() {
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.35);
      }

      EntityAttributeInstance attackDamageAttribute = this.method_5996(EntityAttributes.field_23721);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.method_6192(12.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.method_5996(EntityAttributes.field_23722);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.method_6192(0.3);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(30.0);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.isSuppressed() || this.isDeadlocked() || this.isMovementDisabled()) {
            this.method_18799(Vec3d.field_1353);
            this.method_5942().method_6340();
            return;
         }

         if (this.field_6012 % 50 == 0 && !this.isInWater()) {
            this.findAndEnterWater();
         }

         if (this.isInWater()) {
            this.handleWaterMovement();
         } else {
            this.handleLandMovement();
         }

         if (this.field_6012 % 20 == 0) {
            this.pullPlayersToWater();
         }
      }
   }

   private double getWaterAdjustedSpeed(double baseSpeed) {
      return this.isInWater() ? baseSpeed * 1.5 : baseSpeed;
   }

   private void handleWaterMovement() {
      if (this.field_6012 % 40 == 0) {
         PlayerEntity nearestPlayer = this.method_37908().method_18460(this, 30.0);
         if (nearestPlayer != null && !RedGhostCandleItem.isHoldingCandle(nearestPlayer) && this.isPlayerInWater(nearestPlayer)) {
            this.method_5942().method_6335(nearestPlayer, this.getWaterAdjustedSpeed(1.2));
         } else {
            double radius = 20.0;
            double x = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double y = this.method_23318() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 0.5;
            double z = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            BlockPos targetPos = BlockPos.method_49637(x, y, z);
            if (this.method_37908().method_8316(targetPos).method_15767(FluidTags.field_15517)) {
               this.method_5942().method_6337(x, y, z, this.getWaterAdjustedSpeed(1.0));
            }
         }
      }

      if (this.method_5799()) {
         Vec3d velocity = this.method_18798();
         if (this.method_6051().method_43057() < 0.05F) {
            this.method_18800(velocity.field_1352, velocity.field_1351 + (this.method_6051().method_43058() - 0.5) * 0.2, velocity.field_1350);
         }
      }
   }

   private void handleLandMovement() {
      if (this.field_6012 % 100 == 0 && this.getGhostRandom().nextBoolean()) {
         double radius = 15.0;
         double x = this.method_23317() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
         double z = this.method_23321() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
         this.method_5942().method_6337(x, this.method_23318(), z, this.getWaterAdjustedSpeed(0.48));
      }
   }

   private void pullPlayersToWater() {
      List<PlayerEntity> players = this.method_37908()
         .method_8390(
            PlayerEntity.class,
            this.method_5829().method_1014(30.0),
            playerx -> !RedGhostCandleItem.isHoldingCandle(playerx) && playerx.method_5805() && !this.isPlayerInWater(playerx)
         );
      if (!this.isInWater()) {
         this.findAndEnterWater();
      } else {
         for (PlayerEntity player : players) {
            Vec3d ghostPos = this.method_19538();
            Vec3d playerPos = player.method_19538();
            Vec3d direction = ghostPos.method_1020(playerPos).method_1029();
            double pullStrength = 0.1;
            Vec3d currentVelocity = player.method_18798();
            Vec3d newVelocity = currentVelocity.method_1019(direction.method_1021(pullStrength));
            player.method_18799(newVelocity);
            player.field_6037 = true;
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 40, 1));
            this.spawnPullEffectParticles(player);
         }
      }
   }

   private void spawnPullEffectParticles(PlayerEntity player) {
      if (!this.method_37908().field_9236) {
         for (int i = 0; i < 3; i++) {
            double offsetX = (this.method_6051().method_43058() - 0.5) * 1.0;
            double offsetY = this.method_6051().method_43058() * 1.5;
            double offsetZ = (this.method_6051().method_43058() - 0.5) * 1.0;
            this.method_37908()
               .method_8406(
                  ParticleTypes.field_11202, player.method_23317() + offsetX, player.method_23318() + offsetY, player.method_23321() + offsetZ, 0.0, 0.1, 0.0
               );
         }
      }
   }

   private boolean isInWater() {
      return this.method_5799() || this.method_37908().method_8316(this.method_24515()).method_15767(FluidTags.field_15517);
   }

   private boolean isPlayerInWater(PlayerEntity player) {
      return player.method_5799() || player.method_37908().method_8316(player.method_24515()).method_15767(FluidTags.field_15517);
   }

   private void findAndEnterWater() {
      double searchRadius = 32.0;
      Vec3d currentPos = this.method_19538();
      Vec3d bestWaterPos = null;
      double closestDistance = Double.MAX_VALUE;

      for (double x = -searchRadius; x <= searchRadius; x += 2.0) {
         for (double z = -searchRadius; z <= searchRadius; z += 2.0) {
            for (double y = -searchRadius; y <= searchRadius; y += 2.0) {
               Vec3d checkPos = currentPos.method_1031(x, y, z);
               BlockPos blockPos = BlockPos.method_49638(checkPos);
               if (this.method_37908().method_8316(blockPos).method_15767(FluidTags.field_15517) && this.isWaterAccessible(blockPos)) {
                  double distance = checkPos.method_1022(currentPos);
                  if (distance < closestDistance) {
                     closestDistance = distance;
                     bestWaterPos = this.findWaterEntryPoint(blockPos);
                  }
               }
            }
         }
      }

      if (bestWaterPos != null) {
         double moveSpeed = this.isInWater() ? 2.0 : 1.5;
         this.method_5942().method_6337(bestWaterPos.field_1352, bestWaterPos.field_1351, bestWaterPos.field_1350, moveSpeed);
         this.spawnWaterEntryParticles();
      }
   }

   private boolean isWaterAccessible(BlockPos waterPos) {
      World world = this.method_37908();
      BlockPos abovePos = waterPos.method_10084();
      if (world.method_8320(abovePos).method_26212(world, abovePos)) {
         return false;
      }

      for (Direction direction : Direction.values()) {
         if (direction != Direction.field_11036 && direction != Direction.field_11033) {
            BlockPos adjacentPos = waterPos.method_10093(direction);
            BlockPos adjacentAbovePos = adjacentPos.method_10084();
            if (!world.method_8320(adjacentPos).method_26212(world, adjacentPos) && !world.method_8320(adjacentAbovePos).method_26212(world, adjacentAbovePos)) {
               return true;
            }
         }
      }

      return false;
   }

   private Vec3d findWaterEntryPoint(BlockPos waterPos) {
      BlockPos abovePos = waterPos.method_10084();
      if (!this.method_37908().method_8320(abovePos).method_26215() && this.method_37908().method_8320(abovePos).method_26212(this.method_37908(), abovePos)) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dz != 0) {
                  BlockPos sidePos = waterPos.method_10069(dx, 0, dz);
                  BlockPos sideAbovePos = sidePos.method_10084();
                  if (this.method_37908().method_8316(sidePos).method_15767(FluidTags.field_15517)
                     && (
                        this.method_37908().method_8320(sideAbovePos).method_26215()
                           || !this.method_37908().method_8320(sideAbovePos).method_26212(this.method_37908(), sideAbovePos)
                     )) {
                     return new Vec3d(sideAbovePos.method_10263() + 0.5, sideAbovePos.method_10264(), sideAbovePos.method_10260() + 0.5);
                  }
               }
            }
         }

         return new Vec3d(waterPos.method_10263() + 0.5, waterPos.method_10264() + 1, waterPos.method_10260() + 0.5);
      } else {
         return new Vec3d(abovePos.method_10263() + 0.5, abovePos.method_10264(), abovePos.method_10260() + 0.5);
      }
   }

   private void spawnWaterEntryParticles() {
      if (!this.method_37908().field_9236) {
         for (int i = 0; i < 8; i++) {
            double offsetX = (this.method_6051().method_43058() - 0.5) * 1.5;
            double offsetY = this.method_6051().method_43058() * 1.0;
            double offsetZ = (this.method_6051().method_43058() - 0.5) * 1.5;
            this.method_37908()
               .method_8406(
                  ParticleTypes.field_11202, this.method_23317() + offsetX, this.method_23318() + offsetY, this.method_23321() + offsetZ, 0.0, 0.2, 0.0
               );
         }

         for (int i = 0; i < 5; i++) {
            double offsetX = (this.method_6051().method_43058() - 0.5) * 1.0;
            double offsetY = this.method_6051().method_43058() * 1.5;
            double offsetZ = (this.method_6051().method_43058() - 0.5) * 1.0;
            this.method_37908()
               .method_8406(
                  ParticleTypes.field_11247, this.method_23317() + offsetX, this.method_23318() + offsetY, this.method_23321() + offsetZ, 0.0, 0.1, 0.0
               );
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.isPlayerInWater(player) && this.method_5858(player) <= 900.0 && this.attackCooldown <= 0;
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 30;
            this.method_20620(player.method_23317(), player.method_23318(), player.method_23321());
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5923, 80, 0));
            player.method_5855(-20);
            DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
            PlayerEvents.handleSpiritDamage(player, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
         }
      }
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
