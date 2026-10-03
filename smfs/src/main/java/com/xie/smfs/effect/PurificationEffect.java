package com.xie.smfs.effect;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;

public class PurificationEffect extends StatusEffect {
   public PurificationEffect() {
      super(StatusEffectCategory.BENEFICIAL, 65280);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }

   public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.onApplied(entity, attributes, amplifier);
      entity.removeStatusEffect(StatusEffects.SLOWNESS);
      entity.removeStatusEffect(StatusEffects.NAUSEA);
      entity.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
   }
}
