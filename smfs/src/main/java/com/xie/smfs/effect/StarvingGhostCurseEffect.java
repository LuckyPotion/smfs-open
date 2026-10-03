package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class StarvingGhostCurseEffect extends StatusEffect implements ICurseEffect {
   public StarvingGhostCurseEffect() {
      super(StatusEffectCategory.field_18272, 6684672);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.method_29504()) {
         player.method_7322(0.025F);
      }
   }
}
