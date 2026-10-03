package com.xie.smfs.item;

import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class AncientHouseDiaryItem extends Item {
   public static final String NBT_KEY = "ancient_house_diary";

   public AncientHouseDiaryItem() {
      super(new FabricItemSettings().maxCount(1));
   }

   public static ItemStack createDiary() {
      ItemStack stack = new ItemStack(ModItems.ANCIENT_HOUSE_DIARY);
      NbtCompound nbt = stack.getOrCreateNbt();
      nbt.putBoolean("ancient_house_diary", true);
      return stack;
   }

   public static boolean isDiary(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      }

      if (!stack.isOf(ModItems.ANCIENT_HOUSE_DIARY)) {
         return false;
      }

      NbtCompound nbt = stack.getNbt();
      return nbt != null && nbt.getBoolean("ancient_house_diary");
   }

   public static boolean hasDiary(PlayerEntity player) {
      if (player == null) {
         return false;
      }

      for (ItemStack stack : player.getInventory().main) {
         if (isDiary(stack)) {
            return true;
         }
      }

      for (ItemStack stack : player.getInventory().armor) {
         if (isDiary(stack)) {
            return true;
         }
      }

      return isDiary(player.getOffHandStack());
   }
}
