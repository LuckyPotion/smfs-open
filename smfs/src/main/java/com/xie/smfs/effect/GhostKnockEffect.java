package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GhostKnockEffect extends StatusEffect {
   public GhostKnockEffect() {
      super(StatusEffectCategory.HARMFUL, 2767692);
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.getWorld().isClient && entity.getWorld().getTime() % 20L == 0L) {
         player.sendMessage(Text.translatable("effect.smfs.ghost_knock.warning").formatted(Formatting.RED), true);
      }
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return duration % 20 == 0;
   }

   public String getTranslationKey() {
      return "effect.smfs.ghost_knock";
   }
}
