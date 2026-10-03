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
      PlayerInventory inventory = player.getInventory();
      int count = 0;

      for (int i = 0; i < inventory.size(); i++) {
         ItemStack stack = inventory.getStack(i);
         if (!stack.isEmpty() && isItemMatch(stack, itemId)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   static void consumePlayerItems(PlayerEntity player, String itemId, int amount) {
      PlayerInventory inventory = player.getInventory();
      int remaining = amount;

      for (int i = 0; i < inventory.size() && remaining > 0; i++) {
         ItemStack stack = inventory.getStack(i);
         if (!stack.isEmpty() && isItemMatch(stack, itemId)) {
            int toRemove = Math.min(stack.getCount(), remaining);
            stack.decrement(toRemove);
            remaining -= toRemove;
            LOGGER.info("消耗物品: {} x{}", itemId, toRemove);
         }
      }
   }

   private static boolean isItemMatch(ItemStack stack, String itemId) {
      String stackItemKey = stack.getItem().getTranslationKey();
      String stackItemId = Registries.ITEM.getId(stack.getItem()).toString();
      return stackItemKey.equals("item.smfs." + itemId)
         || stackItemId.equals("smfs:" + itemId)
         || stackItemKey.equals("item.minecraft." + itemId)
         || stackItemId.equals("minecraft:" + itemId)
         || stackItemKey.endsWith("." + itemId)
         || stackItemId.endsWith(":" + itemId);
   }
}
