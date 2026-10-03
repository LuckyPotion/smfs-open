package com.xie.smfs.effect;

import com.xie.smfs.registry.ModEffects;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiritImmunityEffect extends StatusEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger(SpiritImmunityEffect.class);

   public SpiritImmunityEffect() {
      super(StatusEffectCategory.field_18271, 16776960);
   }

   public String method_5567() {
      return "effect.smfs.spirit_immunity";
   }

   public boolean method_5552(int duration, int amplifier) {
      return duration <= 1;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      StatusEffectInstance effectInstance = entity.method_6112(this);
      if (effectInstance != null
         && effectInstance.method_5579() == ModEffects.SPIRIT_IMMUNITY
         && effectInstance instanceof SpiritImmunityEffectInstance immunityInstance) {
         UUID var5 = immunityInstance.getSourceUuid();
      }
   }
}
