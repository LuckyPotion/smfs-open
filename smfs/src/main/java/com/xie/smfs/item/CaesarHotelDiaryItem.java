package com.xie.smfs.item;

import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class CaesarHotelDiaryItem extends Item {
   public static final String NBT_KEY = "caesar_hotel_diary";

   public CaesarHotelDiaryItem() {
      super(new FabricItemSettings().maxCount(1));
   }

   public static ItemStack createDiary() {
      ItemStack stack = new ItemStack(ModItems.CAESAR_HOTEL_DIARY);
      NbtCompound nbt = stack.method_7948();
      nbt.method_10556("caesar_hotel_diary", true);
      return stack;
   }

   public static boolean isDiary(ItemStack stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      }

      if (!stack.method_31574(ModItems.CAESAR_HOTEL_DIARY)) {
         return false;
      }

      NbtCompound nbt = stack.method_7969();
      return nbt != null && nbt.method_10577("caesar_hotel_diary");
   }

   public static boolean hasDiary(PlayerEntity player) {
      if (player == null) {
         return false;
      }

      for (ItemStack stack : player.method_31548().field_7547) {
         if (isDiary(stack)) {
            return true;
         }
      }

      for (ItemStack stack : player.method_31548().field_7548) {
         if (isDiary(stack)) {
            return true;
         }
      }

      return isDiary(player.method_6079());
   }
}
