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
         attackDamageAttribute.method_6192(10.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.method_5996(EntityAttributes.field_23722);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.method_6192(0.2);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(30.0);
      }
   }

   public static Builder createBurnGhostAttributes() {
      return GhostEntity.createGhostAttributes()
         .method_26868(EntityAttributes.field_23716, 100000.0)
         .method_26868(EntityAttributes.field_23719, 0.25)
         .method_26868(EntityAttributes.field_23717, 32.0);
   }

   public String getGhostType() {
      return "burn_ghost";
   }

   public String getGhostDomainColor() {
      return "green";
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      player.method_6092(
         new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false)
      );
   }

   @Override
   protected void method_5693() {
      super.method_5693();
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
         if (this.isSuppressed() || this.isDeadlocked() || this.isMovementDisabled()) {
            return;
         }

         this.method_5639(100);
         if (this.field_6012 % 5 == 0) {
            this.spawnBurnParticles();
         }

         if (this.field_6012 % 40 == 0) {
            this.igniteEntitiesInGhostDomain();
         }

         if (this.field_6012 % 20 == 0) {
            this.spawnFireOnPath();
         }
      }
   }

   private void spawnBurnParticles() {
      if (!this.method_37908().method_8608()) {
         for (int i = 0; i < 3; i++) {
            double offsetX = (this.method_6051().method_43058() - 0.5) * 1.5;
            double offsetY = this.method_6051().method_43058() * 2.0;
            double offsetZ = (this.method_6051().method_43058() - 0.5) * 1.5;
            this.method_37908()
               .method_8406(
                  ParticleTypes.field_11240, this.method_23317() + offsetX, this.method_23318() + offsetY, this.method_23321() + offsetZ, 0.0, 0.1, 0.0
               );
         }

         for (int i = 0; i < 2; i++) {
            double offsetX = (this.method_6051().method_43058() - 0.5) * 1.2;
            double offsetY = this.method_6051().method_43058() * 1.8;
            double offsetZ = (this.method_6051().method_43058() - 0.5) * 1.2;
            this.method_37908()
               .method_8406(
                  ParticleTypes.field_11251, this.method_23317() + offsetX, this.method_23318() + offsetY, this.method_23321() + offsetZ, 0.0, 0.05, 0.0
               );
         }
      }
   }

   private void igniteEntitiesInGhostDomain() {
      if (!this.method_37908().method_8608()) {
         double radius = this.getGhostDomainRadius();

         for (LivingEntity entity : this.method_37908()
            .method_8390(LivingEntity.class, this.method_5829().method_1014(radius), entityx -> entityx != this && entityx.method_5858(this) <= radius * radius)) {
            if (!(entity instanceof PlayerEntity player && this.isPlayerProtected(player)) && !(entity instanceof GhostEntity)) {
               int fireTicks = 60 + this.method_6051().method_43048(100);
               entity.method_5639(fireTicks / 20);
               this.spawnIgnitionParticles(entity);
            }
         }
      }
   }

   private void spawnFireOnPath() {
      if (!this.method_37908().method_8608()) {
         int x = (int)Math.floor(this.method_23317());
         int y = (int)Math.floor(this.method_23318());
         int z = (int)Math.floor(this.method_23321());
         BlockPos pos = new BlockPos(x, y, z);
         BlockPos downPos = pos.method_10074();
         if (this.method_37908().method_8320(downPos).method_26212(this.method_37908(), downPos) && this.method_37908().method_8320(pos).method_26215()) {
            FireBlock fireBlock = (FireBlock)Blocks.field_10036;
            if (fireBlock.method_9558(this.method_37908().method_8320(pos), this.method_37908(), pos)) {
               this.method_37908().method_8501(pos, Blocks.field_10036.method_9564());
               this.spawnFirePlacementParticles(pos);
            }
         }
      }
   }

   private void spawnIgnitionParticles(LivingEntity entity) {
      if (!this.method_37908().method_8608()) {
         for (int i = 0; i < 5; i++) {
            double offsetX = (this.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = this.method_6051().method_43058() * entity.method_17682();
            double offsetZ = (this.method_6051().method_43058() - 0.5) * 2.0;
            this.method_37908()
               .method_8406(
                  ParticleTypes.field_11240, entity.method_23317() + offsetX, entity.method_23318() + offsetY, entity.method_23321() + offsetZ, 0.0, 0.1, 0.0
               );
         }
      }
   }

   private void spawnFirePlacementParticles(BlockPos pos) {
      if (!this.method_37908().method_8608()) {
         for (int i = 0; i < 3; i++) {
            double x = pos.method_10263() + 0.5 + (this.method_6051().method_43058() - 0.5) * 0.5;
            double y = pos.method_10264() + 0.1;
            double z = pos.method_10260() + 0.5 + (this.method_6051().method_43058() - 0.5) * 0.5;
            this.method_37908().method_8406(ParticleTypes.field_11240, x, y, z, 0.0, 0.05, 0.0);
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !RedGhostCandleItem.isHoldingCandle(player) && this.isPlayerBurning(player) && this.method_5858(player) <= 900.0 && this.attackCooldown <= 0;
   }

   private boolean isPlayerBurning(PlayerEntity player) {
      return player.method_5809() || player.method_20802() > 0;
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 20;
            this.method_20620(player.method_23317(), player.method_23318(), player.method_23321());
            if (player.method_5809()) {
               player.method_20803(player.method_20802() + 100);
            } else {
               player.method_5639(5);
            }

            player.method_6092(new StatusEffectInstance(StatusEffects.field_5911, 60, 1));
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
