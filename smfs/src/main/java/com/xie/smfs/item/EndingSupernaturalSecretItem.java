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

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (world.field_9236) {
         return TypedActionResult.method_22427(stack);
      } else if (hasComprehended(user)) {
         user.method_7353(Text.method_43470("§e..."), true);
         return TypedActionResult.method_22431(stack);
      } else {
         setComprehended(user);
         stack.method_7934(1);
         user.method_7353(Text.method_43470("§d你从老一辈驭鬼者的经历中感悟到了终结灵异时代的关键！"), true);
         return TypedActionResult.method_22427(stack);
      }
   }

   public static boolean hasComprehended(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      return data.method_10577("comprehended_ending_secret");
   }

   public static void setComprehended(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      data.method_10556("comprehended_ending_secret", true);
      PlayerEvents.saveDataToPlayer(player, data);
   }
}
