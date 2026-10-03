package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class StarvingGhostCurseEffect extends StatusEffect implements ICurseEffect {
   public StarvingGhostCurseEffect() {
      super(StatusEffectCategory.HARMFUL, 6684672);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.isDead()) {
         player.addExhaustion(0.025F);
      }
   }
}
