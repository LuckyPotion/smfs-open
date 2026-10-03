package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class DreamEffect extends StatusEffect {
   public DreamEffect() {
      super(StatusEffectCategory.HARMFUL, 7032433);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return false;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }
}
