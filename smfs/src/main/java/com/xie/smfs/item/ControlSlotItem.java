package com.xie.smfs.item;

import java.util.List;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ControlSlotItem extends Item {
   public ControlSlotItem() {
      super(new FabricItemSettings().maxCount(64).rarity(Rarity.EPIC));
   }

   public Text getName() {
      return Text.translatable("item.smfs.control_slot");
   }

   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.control_slot");
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.control_slot.description.source"));
      tooltip.add(Text.translatable("item.smfs.control_slot.description.desc"));
      tooltip.add(Text.translatable("item.smfs.control_slot.description.type"));
   }
}
