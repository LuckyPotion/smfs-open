package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BurnGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 3;
   private static final double GHOST_DOMAIN_RADIUS = 36.0;
   private static final char TERROR_LEVEL = 'B';
   private static final String GHOST_DOMAIN_COLOR = "green";

   public BurnGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 36.0, 'B', 2200, 100, 50, 0.3F);
      this.ghostLevel = 3;
      this.setGhostDomainActualLevel(3);
      this.attackCooldown = 20;
      this.initBurnGhostAttributes();
   }

   private void initBurnGhostAttributes() {
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
         attackDamageAttribute.setBaseValue(10.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.setBaseValue(0.2);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(30.0);
      }
   }

   public static Builder createBurnGhostAttributes() {
      return GhostEntity.createGhostAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 100000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
   }

   public String getGhostType() {
      return "burn_ghost";
   }

   public String getGhostDomainColor() {
      return "green";
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      player.addStatusEffect(
         new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false)
      );
   }

   @Override
   protected void initDataTracker() {
      super.initDataTracker();
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient()) {
         if (this.isSuppressed() || this.isDeadlocked() || this.isMovementDisabled()) {
            return;
         }

         this.setOnFireFor(100);
         if (this.age % 5 == 0) {
            this.spawnBurnParticles();
         }

         if (this.age % 40 == 0) {
            this.igniteEntitiesInGhostDomain();
         }

         if (this.age % 20 == 0) {
            this.spawnFireOnPath();
         }
      }
   }

   private void spawnBurnParticles() {
      if (!this.getWorld().isClient()) {
         for (int i = 0; i < 3; i++) {
            double offsetX = (this.getRandom().nextDouble() - 0.5) * 1.5;
            double offsetY = this.getRandom().nextDouble() * 2.0;
            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 1.5;
            this.getWorld().addParticle(ParticleTypes.FLAME, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0.0, 0.1, 0.0);
         }

         for (int i = 0; i < 2; i++) {
            double offsetX = (this.getRandom().nextDouble() - 0.5) * 1.2;
            double offsetY = this.getRandom().nextDouble() * 1.8;
            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 1.2;
            this.getWorld().addParticle(ParticleTypes.SMOKE, this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0.0, 0.05, 0.0);
         }
      }
   }

   private void igniteEntitiesInGhostDomain() {
      if (!this.getWorld().isClient()) {
         double radius = this.getGhostDomainRadius();

         for (LivingEntity entity : this.getWorld()
            .getEntitiesByClass(
               LivingEntity.class, this.getBoundingBox().expand(radius), entityx -> entityx != this && entityx.squaredDistanceTo(this) <= radius * radius
            )) {
            if (!(entity instanceof PlayerEntity player && this.isPlayerProtected(player)) && !(entity instanceof GhostEntity)) {
               int fireTicks = 60 + this.getRandom().nextInt(100);
               entity.setOnFireFor(fireTicks / 20);
               this.spawnIgnitionParticles(entity);
            }
         }
      }
   }

   private void spawnFireOnPath() {
      if (!this.getWorld().isClient()) {
         int x = (int)Math.floor(this.getX());
         int y = (int)Math.floor(this.getY());
         int z = (int)Math.floor(this.getZ());
         BlockPos pos = new BlockPos(x, y, z);
         BlockPos downPos = pos.down();
         if (this.getWorld().getBlockState(downPos).isSolidBlock(this.getWorld(), downPos) && this.getWorld().getBlockState(pos).isAir()) {
            FireBlock fireBlock = (FireBlock)Blocks.FIRE;
            if (fireBlock.canPlaceAt(this.getWorld().getBlockState(pos), this.getWorld(), pos)) {
               this.getWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
               this.spawnFirePlacementParticles(pos);
            }
         }
      }
   }

   private void spawnIgnitionParticles(LivingEntity entity) {
      if (!this.getWorld().isClient()) {
         for (int i = 0; i < 5; i++) {
            double offsetX = (this.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = this.getRandom().nextDouble() * entity.getHeight();
            double offsetZ = (this.getRandom().nextDouble() - 0.5) * 2.0;
            this.getWorld().addParticle(ParticleTypes.FLAME, entity.getX() + offsetX, entity.getY() + offsetY, entity.getZ() + offsetZ, 0.0, 0.1, 0.0);
         }
      }
   }

   private void spawnFirePlacementParticles(BlockPos pos) {
      if (!this.getWorld().isClient()) {
         for (int i = 0; i < 3; i++) {
            double x = pos.getX() + 0.5 + (this.getRandom().nextDouble() - 0.5) * 0.5;
            double y = pos.getY() + 0.1;
            double z = pos.getZ() + 0.5 + (this.getRandom().nextDouble() - 0.5) * 0.5;
            this.getWorld().addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.05, 0.0);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.isPlayerBurning(player) && this.squaredDistanceTo(player) <= 900.0 && this.attackCooldown <= 0;
   }

   private boolean isPlayerBurning(PlayerEntity player) {
      return player.isOnFire() || player.getFireTicks() > 0;
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 20;
            this.teleport(player.getX(), player.getY(), player.getZ());
            if (player.isOnFire()) {
               player.setFireTicks(player.getFireTicks() + 100);
            } else {
               player.setOnFireFor(5);
            }

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1));
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
