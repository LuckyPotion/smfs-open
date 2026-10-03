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
      return stack.method_7909() instanceof PhotoItem && stack.method_7938();
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      return TypedActionResult.method_22430(user.method_5998(hand));
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.photo.description.source"));
      tooltip.add(Text.method_43471("item.smfs.photo.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.photo.description.type"));
   }
}
