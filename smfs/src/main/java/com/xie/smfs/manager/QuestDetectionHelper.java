package com.xie.smfs.manager;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QuestDetectionHelper {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestDetectionHelper");

   static int countPlayerItems(PlayerEntity player, String itemId) {
      PlayerInventory inventory = player.method_31548();
      int count = 0;

      for (int i = 0; i < inventory.method_5439(); i++) {
         ItemStack stack = inventory.method_5438(i);
         if (!stack.method_7960() && isItemMatch(stack, itemId)) {
            count += stack.method_7947();
         }
      }

      return count;
   }

   static void consumePlayerItems(PlayerEntity player, String itemId, int amount) {
      PlayerInventory inventory = player.method_31548();
      int remaining = amount;

      for (int i = 0; i < inventory.method_5439() && remaining > 0; i++) {
         ItemStack stack = inventory.method_5438(i);
         if (!stack.method_7960() && isItemMatch(stack, itemId)) {
            int toRemove = Math.min(stack.method_7947(), remaining);
            stack.method_7934(toRemove);
            remaining -= toRemove;
            LOGGER.info("消耗物品: {} x{}", itemId, toRemove);
         }
      }
   }

   private static boolean isItemMatch(ItemStack stack, String itemId) {
      String stackItemKey = stack.method_7909().method_7876();
      String stackItemId = Registries.field_41178.method_10221(stack.method_7909()).toString();
      return stackItemKey.equals("item.smfs." + itemId)
         || stackItemId.equals("smfs:" + itemId)
         || stackItemKey.equals("item.minecraft." + itemId)
         || stackItemId.equals("minecraft:" + itemId)
         || stackItemKey.endsWith("." + itemId)
         || stackItemId.endsWith(":" + itemId);
   }
}
