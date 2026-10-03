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
      super(StatusEffectCategory.field_18272, 8421504);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      boolean isTargetVersion = entity.method_6059(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
      if (entity instanceof PlayerEntity player && !isTargetVersion) {
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5925, 5, 0, false, false, false));
      }

      if (isTargetVersion && !entity.method_6059(ModEffects.SPIRIT_EROSION)) {
         entity.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, amplifier + 5, false, false, false));
      }
   }

   public static void updateRevivalDegreeInGhostDomain(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         PlayerEvents.balanceRevivalDegree(player, 3, 1);
      }
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.method_5562(entity, attributes, amplifier);
   }
}
