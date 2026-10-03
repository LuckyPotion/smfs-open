package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GhostKnockEffect extends StatusEffect {
   public GhostKnockEffect() {
      super(StatusEffectCategory.field_18272, 2767692);
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.method_37908().field_9236 && entity.method_37908().method_8510() % 20L == 0L) {
         player.method_7353(Text.method_43471("effect.smfs.ghost_knock.warning").method_27692(Formatting.field_1061), true);
      }
   }

   public boolean method_5552(int duration, int amplifier) {
      return duration % 20 == 0;
   }

   public String method_5567() {
      return "effect.smfs.ghost_knock";
   }
}
