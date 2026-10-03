package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrayGhostDomainEffect extends StatusEffect implements ICurseEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GrayGhostDomainEffect");

   public GrayGhostDomainEffect() {
      super(StatusEffectCategory.HARMFUL, 8421504);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      boolean isTargetVersion = entity.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
      if (entity instanceof PlayerEntity player && !isTargetVersion) {
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 5, 0, false, false, false));
      }

      if (isTargetVersion && !entity.hasStatusEffect(ModEffects.SPIRIT_EROSION)) {
         entity.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, amplifier + 5, false, false, false));
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
