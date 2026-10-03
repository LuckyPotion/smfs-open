package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModEffects;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class GhostSmokeEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 3;
   private static final double GHOST_DOMAIN_RADIUS = 48.0;
   private static final char TERROR_LEVEL = 'A';
   private static final int EROSION_INTERVAL = 40;

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 120000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0);
   }

   public GhostSmokeEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 48.0, 'A', 1500, 0, 80, 0.25F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(120000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.15);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(0.0);
      this.setVisible(false);
      this.setInvisible(true);
      this.setKillingRulesEnabled(false);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return false;
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
   }

   @Override
   public void setVisible(boolean visible) {
      super.setVisible(false);
   }

   @Override
   public boolean isVisible() {
      return false;
   }

   @Override
   public boolean getVisible() {
      return false;
   }

   public void setInvisible(boolean invisible) {
      super.setInvisible(true);
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      player.addStatusEffect(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN_TARGET, 200, this.getGhostDomainActualLevel() - 1, false, false));
   }
}
