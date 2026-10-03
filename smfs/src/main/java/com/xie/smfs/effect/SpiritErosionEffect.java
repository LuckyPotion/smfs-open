package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.event.ModEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class SpiritErosionEffect extends StatusEffect {
   public SpiritErosionEffect() {
      super(StatusEffectCategory.HARMFUL, 9055202);
   }

   public String getTranslationKey() {
      return "effect.smfs.spirit_erosion";
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      int tickInterval = 20;
      return duration % tickInterval == 0;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (!entity.getWorld().isClient()) {
         this.handleSpiritErosionDamage(entity, amplifier);
      }
   }

   private void handleSpiritErosionDamage(LivingEntity entity, int amplifier) {
      float baseDamage = 5.0F * (amplifier + 1);
      ModEvents.processingSpiritDamage.set(true);

      try {
         if (entity instanceof PlayerEntity player) {
            PlayerEvents.handleSpiritDamage(player, baseDamage, baseDamage, ModDamageSources.ghost(entity.getWorld()));
         } else {
            entity.damage(ModDamageSources.ghost(entity.getWorld()), baseDamage);
         }
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }
}
