package com.xie.smfs.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface SpiritWeapon {
   float getSpiritDamageBonus();

   default float getSpiritDamageMultiplier() {
      return 0.5F;
   }

   default void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
   }

   static boolean isSpiritWeapon(ItemStack stack) {
      return stack.getItem() instanceof SpiritWeapon;
   }
}
