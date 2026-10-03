package com.xie.smfs.manager;

import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

class EventQuestStrategy implements QuestDetectionStrategy {
   @Override
   public boolean checkCondition(PlayerEntity player, String objectiveId, int targetCount) {
      return this.getCurrentProgress(player, objectiveId) >= targetCount;
   }

   @Override
   public int getCurrentProgress(PlayerEntity player, String objectiveId) {
      PlayerInventory inventory = player.method_31548();
      int count = 0;
      String ghostType = this.getGhostTypeForEvent(objectiveId);
      if (ghostType == null) {
         LOGGER.warn("未知的事件任务目标: {}", objectiveId);
         return 0;
      }

      for (int i = 0; i < inventory.method_5439(); i++) {
         ItemStack stack = inventory.method_5438(i);
         if (!stack.method_7960() && stack.method_7909() == ModItems.GOLDEN_CONTAINER && GoldenContainerItem.hasGhost(stack)) {
            String containedGhostType = this.getContainedGhostType(stack);
            if (ghostType.equals(containedGhostType) || containedGhostType.endsWith(":" + ghostType) || containedGhostType.equals("smfs:" + ghostType)) {
               count++;
               LOGGER.info("玩家 {} 的黄金容器包含对应鬼类型: {} (任务: {})", player.method_5477().getString(), ghostType, objectiveId);
            }
         }
      }

      return count;
   }

   @Override
   public void consumeItems(PlayerEntity player, String objectiveId, int amount) {
      PlayerInventory inventory = player.method_31548();
      int remaining = amount;
      String ghostType = this.getGhostTypeForEvent(objectiveId);
      if (ghostType == null) {
         LOGGER.warn("未知的事件任务目标，无法消耗物品: {}", objectiveId);
      } else {
         for (int i = 0; i < inventory.method_5439() && remaining > 0; i++) {
            ItemStack stack = inventory.method_5438(i);
            if (!stack.method_7960() && stack.method_7909() == ModItems.GOLDEN_CONTAINER && GoldenContainerItem.hasGhost(stack)) {
               String containedGhostType = this.getContainedGhostType(stack);
               if (ghostType.equals(containedGhostType) || containedGhostType.endsWith(":" + ghostType) || containedGhostType.equals("smfs:" + ghostType)) {
                  stack.method_7934(1);
                  remaining--;
                  LOGGER.info("消耗包含鬼 {} 的黄金容器，任务: {}", ghostType, objectiveId);
               }
            }
         }
      }
   }

   private String getGhostTypeForEvent(String objectiveId) {
      switch (objectiveId) {
         case "ghost_knock_event":
            return "qiaomen_ghost";
         case "ghost_look_up_event":
            return "taitou_ghost";
         case "ghost_look_down_event":
            return "ditou_ghost";
         case "invisible_event":
            return "death_sight_ghost";
         case "dense_fog_event":
            return "fog_ghost";
         default:
            return null;
      }
   }

   private String getContainedGhostType(ItemStack goldenContainer) {
      return GoldenContainerItem.getContainedGhostType(goldenContainer);
   }
}
