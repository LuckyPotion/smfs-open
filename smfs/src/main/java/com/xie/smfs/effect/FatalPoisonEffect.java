package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class FatalPoisonEffect extends StatusEffect {
   public FatalPoisonEffect() {
      super(StatusEffectCategory.HARMFUL, 4521796);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return duration % 20 == 0;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         float maxHealth = player.getMaxHealth();
         float damageAmount = maxHealth * 0.1F * (amplifier + 1);
         if (player.getHealth() > 0.0F) {
            player.damage(player.getDamageSources().magic(), damageAmount);
         }
      } else {
         float maxHealth = entity.getMaxHealth();
         float damageAmount = maxHealth * 0.1F * (amplifier + 1);
         entity.damage(entity.getDamageSources().magic(), damageAmount);
      }
   }
}
