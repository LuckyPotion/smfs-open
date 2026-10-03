package com.xie.smfs.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpiritBrewingStandItem extends BlockItem {
   public SpiritBrewingStandItem(Block block, Settings settings) {
      super(block, settings);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.spirit_brewing_stand.description.source"));
      tooltip.add(Text.method_43471("item.smfs.spirit_brewing_stand.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.spirit_brewing_stand.description.type"));
   }
}
