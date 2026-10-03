package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurpleGhostDomainEffect extends StatusEffect implements ICurseEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/PurpleGhostDomainEffect");

   public PurpleGhostDomainEffect() {
      super(StatusEffectCategory.HARMFUL, 8388736);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.PURPLE_GHOST_DOMAIN_VISUAL, 5, amplifier, false, false, false));
      }
   }

   public static void updateRevivalDegreeInGhostDomain(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         PlayerEvents.balanceRevivalDegree(player, 3, 1);
      }
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.onRemoved(entity, attributes, amplifier);
   }
}
