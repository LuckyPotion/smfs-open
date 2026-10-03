package com.xie.smfs.util;

import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class WeaponOilHandler {
   public static boolean applyCorpseOil(PlayerEntity player) {
      ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
      ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
      if (offHand.getItem() == ModItems.CORPSE_OIL && mainHand.getItem() instanceof SwordItem) {
         NbtCompound nbt = mainHand.getOrCreateNbt();
         int currentLayers = nbt.getInt("corpse_oil_layers");
         if (currentLayers < 3) {
            nbt.putInt("corpse_oil_layers", currentLayers + 1);
            offHand.decrement(1);
            player.sendMessage(Text.translatable("message.smfs.corpse_oil_applied", new Object[]{currentLayers + 1}), true);
            return true;
         }

         player.sendMessage(Text.translatable("message.smfs.corpse_oil_max"), true);
      }

      return false;
   }

   public static int getCorpseOilLayers(ItemStack stack) {
      return stack.hasNbt() && stack.getNbt().contains("corpse_oil_layers") ? stack.getNbt().getInt("corpse_oil_layers") : 0;
   }

   public static void consumeCorpseOilLayer(ItemStack stack) {
      if (stack.hasNbt() && stack.getNbt().contains("corpse_oil_layers")) {
         int layers = stack.getNbt().getInt("corpse_oil_layers");
         if (layers > 0) {
            stack.getNbt().putInt("corpse_oil_layers", --layers);
            if (layers == 0) {
               stack.getNbt().remove("corpse_oil_layers");
            }
         }
      }
   }
}
