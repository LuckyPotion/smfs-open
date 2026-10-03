package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PhotoItem extends Item {
   public PhotoItem(Settings settings) {
      super(settings);
   }

   public static boolean isNamedPhoto(ItemStack stack) {
      return stack.getItem() instanceof PhotoItem && stack.hasCustomName();
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      return TypedActionResult.pass(user.getStackInHand(hand));
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.photo.description.source"));
      tooltip.add(Text.translatable("item.smfs.photo.description.desc"));
      tooltip.add(Text.translatable("item.smfs.photo.description.type"));
   }
}
