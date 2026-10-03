package com.xie.smfs.item;

import com.xie.smfs.common.events.PlayerEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class EndingSupernaturalSecretItem extends Item {
   public static final String COMPREHENDED_KEY = "comprehended_ending_secret";

   public EndingSupernaturalSecretItem() {
      super(new FabricItemSettings().maxCount(1));
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (world.isClient) {
         return TypedActionResult.success(stack);
      } else if (hasComprehended(user)) {
         user.sendMessage(Text.literal("§e..."), true);
         return TypedActionResult.fail(stack);
      } else {
         setComprehended(user);
         stack.decrement(1);
         user.sendMessage(Text.literal("§d你从老一辈驭鬼者的经历中感悟到了终结灵异时代的关键！"), true);
         return TypedActionResult.success(stack);
      }
   }

   public static boolean hasComprehended(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      return data.getBoolean("comprehended_ending_secret");
   }

   public static void setComprehended(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      data.putBoolean("comprehended_ending_secret", true);
      PlayerEvents.saveDataToPlayer(player, data);
   }
}
