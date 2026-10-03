package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CorpsePieceItem extends Item {
   public CorpsePieceItem(Settings settings) {
      super(settings);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.corpse_piece.description.source"));
      tooltip.add(Text.translatable("item.smfs.corpse_piece.description.desc"));
      tooltip.add(Text.translatable("item.smfs.corpse_piece.description.type"));
      tooltip.add(Text.translatable("item.smfs.brewable_material"));
   }
}
