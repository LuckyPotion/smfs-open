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

public class GoldenGhostDomainEffect extends StatusEffect implements ICurseEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GoldenGhostDomainEffect");

   public GoldenGhostDomainEffect() {
      super(StatusEffectCategory.field_18272, 16766720);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         boolean isTargetVersion = player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
         if (!isTargetVersion) {
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5925, 5, 0, false, false, false));
         }
      }
   }

   public static void updateRevivalDegreeInGhostDomain(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         int ghostEyeSlot = PlayerEvents.findEquippedGhostEyeSlot(player);
         int ghostFireSlot = PlayerEvents.findEquippedGhostFireSlot(player);
         if (ghostEyeSlot != -1) {
            PlayerEvents.balanceRevivalDegree(player, 1, 1);
         }

         if (ghostFireSlot != -1) {
            PlayerEvents.balanceRevivalDegree(player, 1, 1);
         }
      }
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.method_5562(entity, attributes, amplifier);
   }
}
