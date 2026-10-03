package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ScapegoatMarkEffect extends StatusEffect implements ICurseEffect {
   public ScapegoatMarkEffect() {
      super(StatusEffectCategory.field_18272, 16729344);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public String method_5567() {
      return "effect.smfs.scapegoat_mark";
   }
}
