package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevivalSuppressionEffect extends StatusEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger(RevivalSuppressionEffect.class);

   public RevivalSuppressionEffect() {
      super(StatusEffectCategory.field_18271, 4286945);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.method_37908().method_8608()) {
         for (int i = 0; i < 10; i++) {
            if (PlayerEvents.isGhostSlotOccupied(player, i)) {
               PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", 0);
            }
         }
      }
   }

   public void method_5555(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.method_37908().method_8608()) {
         player.method_7353(Text.method_43471("effect.smfs.revival_suppression.applied"), true);
      }

      super.method_5555(entity, attributes, amplifier);
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.method_37908().method_8608()) {
         player.method_7353(Text.method_43471("effect.smfs.revival_suppression.removed"), true);
      }

      super.method_5562(entity, attributes, amplifier);
   }
}
