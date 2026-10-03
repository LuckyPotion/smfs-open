package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class MarkCurseEffect extends StatusEffect implements ICurseEffect {
   public MarkCurseEffect() {
      super(StatusEffectCategory.field_18272, 16766720);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public String method_5567() {
      return "effect.smfs.mark_curse";
   }
}
