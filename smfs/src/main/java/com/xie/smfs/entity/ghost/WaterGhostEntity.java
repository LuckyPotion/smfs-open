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
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.35);
      }

      EntityAttributeInstance attackDamageAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.setBaseValue(12.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.setBaseValue(0.3);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(30.0);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         if (this.isSuppressed() || this.isDeadlocked() || this.isMovementDisabled()) {
            this.setVelocity(Vec3d.ZERO);
            this.getNavigation().stop();
            return;
         }

         if (this.age % 50 == 0 && !this.isInWater()) {
            this.findAndEnterWater();
         }

         if (this.isInWater()) {
            this.handleWaterMovement();
         } else {
            this.handleLandMovement();
         }

         if (this.age % 20 == 0) {
            this.pullPlayersToWater();
         }
      }
   }

   private double getWaterAdjustedSpeed(double baseSpeed) {
      return this.isInWater() ? baseSpeed * 1.5 : baseSpeed;
   }

   private void handleWaterMovement() {
      if (this.age % 40 == 0) {
         PlayerEntity nearestPlayer = this.getWorld().getClosestPlayer(this, 30.0);
         if (nearestPlayer != null && !RedGhostCandleItem.isHoldingCandle(nearestPlayer) && this.isPlayerInWater(nearestPlayer)) {
            this.getNavigation().startMovingTo(nearestPlayer, this.getWaterAdjustedSpeed(1.2));
         } else {
            double radius = 20.0;
            double x = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            double y = this.getY() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 0.5;
            double z = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
            BlockPos targetPos = BlockPos.ofFloored(x, y, z);
            if (this.getWorld().getFluidState(targetPos).isIn(FluidTags.WATER)) {
               this.getNavigation().startMovingTo(x, y, z, this.getWaterAdjustedSpeed(1.0));
            }
         }
      }

      if (this.isTouchingWater()) {
         Vec3d velocity = this.getVelocity();
         if (this.getRandom().nextFloat() < 0.05F) {
            this.setVelocity(velocity.x, velocity.y + (this.getRandom().nextDouble() - 0.5) * 0.2, velocity.z);
         }
      }
   }

   private void handleLandMovement() {
      if (this.age % 100 == 0 && this.getGhostRandom().nextBoolean()) {
         double radius = 15.0;
         double x = this.getX() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
         double z = this.getZ() + (this.getGhostRandom().nextDouble() - 0.5) * radius * 2.0;
         this.getNavigation().startMovingTo(x, this.getY(), z, this.getWaterAdjustedSpeed(0.48));
      }
   }

   private void pullPlayersToWater() {
      List<PlayerEntity> players = this.getWorld()
         .getEntitiesByClass(
            PlayerEntity.class,
            this.getBoundingBox().expand(30.0),
            playerx -> !RedGhostCandleItem.isHoldingCandle(playerx) && playerx.isAlive() && !this.isPlayerInWater(playerx)
         );
      if (!this.isInWater()) {
         this.findAndEnterWater();
      } else {
         for (PlayerEntity player : players) {
            Vec3d ghostPos = this.getPos();
            Vec3d playerPos = player.getPos();
            Vec3d direction = ghostPos.subtract(playerPos).normalize();
            double pullStrength = 0.1;
            Vec3d currentVelocity = player.getVelocity();
            Vec3d newVelocity = currentVelocity.add(direction.multiply(pullStrength));
            player.setVelocity(newVelocity);
            player.velocityModified = true;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
            this.spawnPullEffectParticles(player);
         }
      }
   }

   private void spawnPullEffectParticles(PlayerEntity player) {
      if (!this.getWorld().isClient) {
         for (int i = 0; i < 3; i++) {
            double offsetX = (this.getRandom().nextDouble() - 0.5) * 1.0;
            double offsetY = this.getRandom().nextDouble() * 1.5;
            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 1.0;
            this.getWorld().addParticle(ParticleTypes.SPLASH, player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ, 0.0, 0.1, 0.0);
         }
      }
   }

   private boolean isInWater() {
      return this.isTouchingWater() || this.getWorld().getFluidState(this.getBlockPos()).isIn(FluidTags.WATER);
   }

   private boolean isPlayerInWater(PlayerEntity player) {
      return player.isTouchingWater() || player.getWorld().getFluidState(player.getBlockPos()).isIn(FluidTags.WATER);
   }

   private void findAndEnterWater() {
      double searchRadius = 32.0;
      Vec3d currentPos = this.getPos();
      Vec3d bestWaterPos = null;
      double closestDistance = Double.MAX_VALUE;

      for (double x = -searchRadius; x <= searchRadius; x += 2.0) {
         for (double z = -searchRadius; z <= searchRadius; z += 2.0) {
            for (double y = -searchRadius; y <= searchRadius; y += 2.0) {
               Vec3d checkPos = currentPos.add(x, y, z);
               BlockPos blockPos = BlockPos.ofFloored(checkPos);
               if (this.getWorld().getFluidState(blockPos).isIn(FluidTags.WATER) && this.isWaterAccessible(blockPos)) {
                  double distance = checkPos.distanceTo(currentPos);
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
         this.getNavigation().startMovingTo(bestWaterPos.x, bestWaterPos.y, bestWaterPos.z, moveSpeed);
         this.spawnWaterEntryParticles();
      }
   }

   private boolean isWaterAccessible(BlockPos waterPos) {
      World world = this.getWorld();
      BlockPos abovePos = waterPos.up();
      if (world.getBlockState(abovePos).isSolidBlock(world, abovePos)) {
         return false;
      }

      for (Direction direction : Direction.values()) {
         if (direction != Direction.UP && direction != Direction.DOWN) {
            BlockPos adjacentPos = waterPos.offset(direction);
            BlockPos adjacentAbovePos = adjacentPos.up();
            if (!world.getBlockState(adjacentPos).isSolidBlock(world, adjacentPos)
               && !world.getBlockState(adjacentAbovePos).isSolidBlock(world, adjacentAbovePos)) {
               return true;
            }
         }
      }

      return false;
   }

   private Vec3d findWaterEntryPoint(BlockPos waterPos) {
      BlockPos abovePos = waterPos.up();
      if (!this.getWorld().getBlockState(abovePos).isAir() && this.getWorld().getBlockState(abovePos).isSolidBlock(this.getWorld(), abovePos)) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx != 0 || dz != 0) {
                  BlockPos sidePos = waterPos.add(dx, 0, dz);
                  BlockPos sideAbovePos = sidePos.up();
                  if (this.getWorld().getFluidState(sidePos).isIn(FluidTags.WATER)
                     && (
                        this.getWorld().getBlockState(sideAbovePos).isAir()
                           || !this.getWorld().getBlockState(sideAbovePos).isSolidBlock(this.getWorld(), sideAbovePos)
                     )) {
                     return new Vec3d(sideAbovePos.getX() + 0.5, sideAbovePos.getY(), sideAbovePos.getZ() + 0.5);
                  }
               }
            }
         }

         return new Vec3d(waterPos.getX() + 0.5, waterPos.getY() + 1, waterPos.getZ() + 0.5);
      } else {
         return new Vec3d(abovePos.getX() + 0.5, abovePos.getY(), abovePos.getZ() + 0.5);
      }
   }

   private void spawnWaterEntryParticles() {
      if (!this.getWorld().isClient) {
         for (int i = 0; i < 8; i++) {
            double offsetX = (this.getRandom().nextDouble() - 0.5) * 1.5;
            double offsetY = this.getRandom().nextDouble() * 1.0;
            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 1.5;
            this.getWorld().addParticle(ParticleTypes.SPLASH, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0.0, 0.2, 0.0);
         }

         for (int i = 0; i < 5; i++) {
            double offsetX = (this.getRandom().nextDouble() - 0.5) * 1.0;
            double offsetY = this.getRandom().nextDouble() * 1.5;
            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 1.0;
            this.getWorld().addParticle(ParticleTypes.BUBBLE, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0.0, 0.1, 0.0);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.isPlayerInWater(player) && this.squaredDistanceTo(player) <= 900.0 && this.attackCooldown <= 0;
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 30;
            this.teleport(player.getX(), player.getY(), player.getZ());
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 80, 0));
            player.setAir(-20);
            DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
            PlayerEvents.handleSpiritDamage(player, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
         }
      }
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }
}
