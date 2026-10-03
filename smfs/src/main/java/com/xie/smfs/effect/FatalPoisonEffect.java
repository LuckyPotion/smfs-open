package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class FatalPoisonEffect extends StatusEffect {
   public FatalPoisonEffect() {
      super(StatusEffectCategory.field_18272, 4521796);
   }

   public boolean method_5552(int duration, int amplifier) {
      return duration % 20 == 0;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         float maxHealth = player.method_6063();
         float damageAmount = maxHealth * 0.1F * (amplifier + 1);
         if (player.method_6032() > 0.0F) {
            player.method_5643(player.method_48923().method_48831(), damageAmount);
         }
      } else {
         float maxHealth = entity.method_6063();
         float damageAmount = maxHealth * 0.1F * (amplifier + 1);
         entity.method_5643(entity.method_48923().method_48831(), damageAmount);
      }
   }
}
