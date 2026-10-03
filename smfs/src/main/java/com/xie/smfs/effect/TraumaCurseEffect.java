package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TraumaCurseEffect extends StatusEffect implements ICurseEffect {
   public TraumaCurseEffect() {
      super(StatusEffectCategory.field_18272, 9109504);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public boolean method_5573() {
      return false;
   }
}
