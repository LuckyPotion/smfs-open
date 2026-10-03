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

public class GhostFurnaceItem extends BlockItem {
   public GhostFurnaceItem(Block block, Settings settings) {
      super(block, settings);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_furnace.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_furnace.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_furnace.description.type"));
   }
}
