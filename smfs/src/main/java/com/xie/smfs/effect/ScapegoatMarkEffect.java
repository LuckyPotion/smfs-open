package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ScapegoatMarkEffect extends StatusEffect implements ICurseEffect {
   public ScapegoatMarkEffect() {
      super(StatusEffectCategory.HARMFUL, 16729344);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }

   public String getTranslationKey() {
      return "effect.smfs.scapegoat_mark";
   }
}
