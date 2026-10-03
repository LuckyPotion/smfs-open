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
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 80000.0)
         .method_26868(EntityAttributes.field_23719, 0.2)
         .method_26868(EntityAttributes.field_23721, 3.0);
   }

   public PlagueGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1800, 110, 35, 0.15F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(80000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.2);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(3.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.method_6059(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : player.method_6059(ModEffects.PLAGUE);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.method_5858(player) <= 1024.0;
   }

   private boolean isPlayerHealthNotFull(PlayerEntity player) {
      return player.method_6032() < player.method_6063();
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608() && this.field_6012 % 40 == 0) {
         this.spreadPlagueToNearbyPlayers();
      }
   }

   private void spreadPlagueToNearbyPlayers() {
      if (!this.isSuppressed() && !this.isDeadlocked()) {
         for (PlayerEntity player : this.method_37908().method_18456()) {
            if (this.method_5858(player) <= 225.0 && this.method_6057(player) && this.isPlayerHealthNotFull(player) && !player.method_6059(ModEffects.PLAGUE)) {
               player.method_6092(new StatusEffectInstance(ModEffects.PLAGUE, 600, 0));
            }
         }
      }
   }

   public boolean method_6049(StatusEffectInstance effectInstance) {
      return effectInstance.method_5579() == ModEffects.PLAGUE ? false : super.method_6049(effectInstance);
   }
}
