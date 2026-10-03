package com.xie.smfs.effect;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;

public class PurificationEffect extends StatusEffect {
   public PurificationEffect() {
      super(StatusEffectCategory.field_18271, 65280);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public void method_5555(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.method_5555(entity, attributes, amplifier);
      entity.method_6016(StatusEffects.field_5909);
      entity.method_6016(StatusEffects.field_5916);
      entity.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
   }
}
