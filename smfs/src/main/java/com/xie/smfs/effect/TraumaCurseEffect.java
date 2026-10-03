package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TraumaCurseEffect extends StatusEffect implements ICurseEffect {
   public TraumaCurseEffect() {
      super(StatusEffectCategory.HARMFUL, 9109504);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }

   public boolean isBeneficial() {
      return false;
   }
}
