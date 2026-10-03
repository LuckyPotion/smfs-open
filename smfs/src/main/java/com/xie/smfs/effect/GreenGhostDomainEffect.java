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

public class GreenGhostDomainEffect extends StatusEffect implements ICurseEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GreenGhostDomainEffect");

   public GreenGhostDomainEffect() {
      super(StatusEffectCategory.HARMFUL, 65280);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         boolean isTargetVersion = player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
         if (!isTargetVersion) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 5, 0, false, false, false));
         }
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
