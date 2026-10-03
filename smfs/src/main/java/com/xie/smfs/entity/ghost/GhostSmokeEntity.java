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
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 120000.0)
         .method_26868(EntityAttributes.field_23719, 0.15)
         .method_26868(EntityAttributes.field_23721, 0.0);
   }

   public GhostSmokeEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 48.0, 'A', 1500, 0, 80, 0.25F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(120000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.15);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(0.0);
      this.setVisible(false);
      this.method_5648(true);
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

   public void method_5648(boolean invisible) {
      super.method_5648(true);
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      player.method_6092(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN_TARGET, 200, this.getGhostDomainActualLevel() - 1, false, false));
   }
}
