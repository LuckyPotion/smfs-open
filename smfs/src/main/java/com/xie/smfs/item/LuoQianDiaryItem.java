package com.xie.smfs.item;

import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class LuoQianDiaryItem extends Item {
   public static final String NBT_KEY = "luo_qian_diary";

   public LuoQianDiaryItem() {
      super(new FabricItemSettings().maxCount(1));
   }

   public static ItemStack createDiary() {
      ItemStack stack = new ItemStack(ModItems.LUO_QIAN_DIARY);
      NbtCompound nbt = stack.getOrCreateNbt();
      nbt.putBoolean("luo_qian_diary", true);
      return stack;
   }

   public static boolean isDiary(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      }

      if (!stack.isOf(ModItems.LUO_QIAN_DIARY)) {
         return false;
      }

      NbtCompound nbt = stack.getNbt();
      return nbt != null && nbt.getBoolean("luo_qian_diary");
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
