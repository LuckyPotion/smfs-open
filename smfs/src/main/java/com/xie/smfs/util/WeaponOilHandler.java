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
      ItemStack mainHand = player.method_5998(Hand.field_5808);
      ItemStack offHand = player.method_5998(Hand.field_5810);
      if (offHand.method_7909() == ModItems.CORPSE_OIL && mainHand.method_7909() instanceof SwordItem) {
         NbtCompound nbt = mainHand.method_7948();
         int currentLayers = nbt.method_10550("corpse_oil_layers");
         if (currentLayers < 3) {
            nbt.method_10569("corpse_oil_layers", currentLayers + 1);
            offHand.method_7934(1);
            player.method_7353(Text.method_43469("message.smfs.corpse_oil_applied", new Object[]{currentLayers + 1}), true);
            return true;
         }

         player.method_7353(Text.method_43471("message.smfs.corpse_oil_max"), true);
      }

      return false;
   }

   public static int getCorpseOilLayers(ItemStack stack) {
      return stack.method_7985() && stack.method_7969().method_10545("corpse_oil_layers") ? stack.method_7969().method_10550("corpse_oil_layers") : 0;
   }

   public static void consumeCorpseOilLayer(ItemStack stack) {
      if (stack.method_7985() && stack.method_7969().method_10545("corpse_oil_layers")) {
         int layers = stack.method_7969().method_10550("corpse_oil_layers");
         if (layers > 0) {
            stack.method_7969().method_10569("corpse_oil_layers", --layers);
            if (layers == 0) {
               stack.method_7969().method_10551("corpse_oil_layers");
            }
         }
      }
   }
}
