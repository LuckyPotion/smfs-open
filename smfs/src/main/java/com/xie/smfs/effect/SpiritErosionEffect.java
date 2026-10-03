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
      super(StatusEffectCategory.field_18272, 9055202);
   }

   public String method_5567() {
      return "effect.smfs.spirit_erosion";
   }

   public boolean method_5552(int duration, int amplifier) {
      int tickInterval = 20;
      return duration % tickInterval == 0;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (!entity.method_37908().method_8608()) {
         this.handleSpiritErosionDamage(entity, amplifier);
      }
   }

   private void handleSpiritErosionDamage(LivingEntity entity, int amplifier) {
      float baseDamage = 5.0F * (amplifier + 1);
      ModEvents.processingSpiritDamage.set(true);

      try {
         if (entity instanceof PlayerEntity player) {
            PlayerEvents.handleSpiritDamage(player, baseDamage, baseDamage, ModDamageSources.ghost(entity.method_37908()));
         } else {
            entity.method_5643(ModDamageSources.ghost(entity.method_37908()), baseDamage);
         }
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }
}
