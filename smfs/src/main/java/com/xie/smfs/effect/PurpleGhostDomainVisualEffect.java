package com.xie.smfs.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class PurpleGhostDomainVisualEffect extends StatusEffect implements ICurseEffect {
   public PurpleGhostDomainVisualEffect() {
      super(StatusEffectCategory.NEUTRAL, 8388736);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return false;
   }
}
