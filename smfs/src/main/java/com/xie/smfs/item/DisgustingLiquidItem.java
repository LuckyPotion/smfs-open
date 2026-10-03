package com.xie.smfs.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.world.World;

public class DisgustingLiquidItem extends SpiritPotionItem {
   public DisgustingLiquidItem(Settings settings) {
      super(settings, null, 0, 0, 0);
   }

   @Override
   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      if (!world.isClient && user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
         stack.decrement(1);
      }

      return stack;
   }
}
