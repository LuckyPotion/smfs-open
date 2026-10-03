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
   public ItemStack method_7861(ItemStack stack, World world, LivingEntity user) {
      if (!world.field_9236 && user instanceof PlayerEntity player && !player.method_31549().field_7477) {
         stack.method_7934(1);
      }

      return stack;
   }
}
