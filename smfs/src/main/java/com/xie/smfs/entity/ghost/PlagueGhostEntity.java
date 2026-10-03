package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModEffects;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class PlagueGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';
   private static final double PLAGUE_RANGE = 15.0;

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 80000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
   }

   public PlagueGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1800, 110, 35, 0.15F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(80000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.2);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(3.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : player.hasStatusEffect(ModEffects.PLAGUE);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= 1024.0;
   }

   private boolean isPlayerHealthNotFull(PlayerEntity player) {
      return player.getHealth() < player.getMaxHealth();
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient() && this.age % 40 == 0) {
         this.spreadPlagueToNearbyPlayers();
      }
   }

   private void spreadPlagueToNearbyPlayers() {
      if (!this.isSuppressed() && !this.isDeadlocked()) {
         for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (this.squaredDistanceTo(player) <= 225.0
               && this.canSee(player)
               && this.isPlayerHealthNotFull(player)
               && !player.hasStatusEffect(ModEffects.PLAGUE)) {
               player.addStatusEffect(new StatusEffectInstance(ModEffects.PLAGUE, 600, 0));
            }
         }
      }
   }

   public boolean canHaveStatusEffect(StatusEffectInstance effect) {
      return effect.getEffectType() == ModEffects.PLAGUE ? false : super.canHaveStatusEffect(effect);
   }
}
