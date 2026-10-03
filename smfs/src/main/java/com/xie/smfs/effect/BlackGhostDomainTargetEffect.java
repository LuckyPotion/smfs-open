package com.xie.smfs.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackGhostDomainTargetEffect extends StatusEffect implements ICurseEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/BlackGhostDomainTargetEffect");

   public BlackGhostDomainTargetEffect() {
      super(StatusEffectCategory.field_18272, 0);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.method_5562(entity, attributes, amplifier);
   }
}
