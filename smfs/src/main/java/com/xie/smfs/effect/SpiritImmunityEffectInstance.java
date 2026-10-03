package com.xie.smfs.effect;

import java.util.UUID;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public class SpiritImmunityEffectInstance extends StatusEffectInstance {
   private final UUID sourceUuid;

   public SpiritImmunityEffectInstance(
      StatusEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, UUID sourceUuid
   ) {
      super(effect, duration, amplifier, ambient, showParticles, showIcon);
      this.sourceUuid = sourceUuid;
   }

   public UUID getSourceUuid() {
      return this.sourceUuid;
   }
}
