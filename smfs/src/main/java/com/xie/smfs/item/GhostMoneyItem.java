package com.xie.smfs.item;

import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class GhostMoneyItem extends Item {
   public GhostMoneyItem(Settings settings) {
      super(settings);
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      if (stack.isOf(ModItems.GHOST_MONEY_3)) {
         tooltip.add(Text.translatable("item.smfs.ghost_money_3.description.source"));
         tooltip.add(Text.translatable("item.smfs.ghost_money_3.description.desc"));
         tooltip.add(Text.translatable("item.smfs.ghost_money_3.description.type"));
      } else if (stack.isOf(ModItems.GHOST_MONEY_7)) {
         tooltip.add(Text.translatable("item.smfs.ghost_money_7.description.source"));
         tooltip.add(Text.translatable("item.smfs.ghost_money_7.description.desc"));
         tooltip.add(Text.translatable("item.smfs.ghost_money_7.description.type"));
      }
   }
}
