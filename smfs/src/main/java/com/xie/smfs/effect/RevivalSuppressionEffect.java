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
      super(StatusEffectCategory.BENEFICIAL, 4286945);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.getWorld().isClient()) {
         for (int i = 0; i < 10; i++) {
            if (PlayerEvents.isGhostSlotOccupied(player, i)) {
               PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", 0);
            }
         }
      }
   }

   public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.getWorld().isClient()) {
         player.sendMessage(Text.translatable("effect.smfs.revival_suppression.applied"), true);
      }

      super.onApplied(entity, attributes, amplifier);
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.getWorld().isClient()) {
         player.sendMessage(Text.translatable("effect.smfs.revival_suppression.removed"), true);
      }

      super.onRemoved(entity, attributes, amplifier);
   }
}
