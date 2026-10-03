package com.xie.smfs.manager;

import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

class SellerQuestStrategy implements QuestDetectionStrategy {
   private String requiredGhostType = null;

   public void setRequiredGhostType(String ghostType) {
      this.requiredGhostType = ghostType;
   }

   @Override
   public boolean checkCondition(PlayerEntity player, String objectiveId, int targetCount) {
      int currentProgress = this.getCurrentProgress(player, objectiveId);
      return currentProgress >= targetCount;
   }

   @Override
   public int getCurrentProgress(PlayerEntity player, String objectiveId) {
      if ("collect_heavy_gold_container".equals(objectiveId)) {
         return this.countHeavyGoldenContainers(player);
      }

      LOGGER.warn("卖货郎任务策略不支持的目标ID: {}", objectiveId);
      return 0;
   }

   private int countHeavyGoldenContainers(PlayerEntity player) {
      PlayerInventory inventory = player.method_31548();
      int count = 0;

      for (int i = 0; i < inventory.method_5439(); i++) {
         ItemStack stack = inventory.method_5438(i);
         if (!stack.method_7960() && stack.method_7909() == ModItems.GOLDEN_CONTAINER && stack.method_7985() && stack.method_7969().method_10577("IsHeavy")) {
            if (this.requiredGhostType != null && !this.requiredGhostType.isEmpty()) {
               String containedGhost = GoldenContainerItem.getContainedGhostType(stack);
               if (containedGhost != null) {
                  String containedGhostType = containedGhost.contains(":") ? containedGhost.split(":")[1] : containedGhost;
                  if (this.requiredGhostType.equals(containedGhostType)) {
                     count += stack.method_7947();
                  }
               }
            } else {
               count += stack.method_7947();
            }
         }
      }

      return count;
   }

   @Override
   public void consumeItems(PlayerEntity player, String objectiveId, int amount) {
      if ("collect_heavy_gold_container".equals(objectiveId)) {
         this.consumeHeavyGoldenContainers(player, amount);
         LOGGER.info("卖货郎任务消耗沉重的黄金容器: {} 个", amount);
      } else {
         LOGGER.info("卖货郎任务目标 {} 不需要消耗物品", objectiveId);
      }
   }

   private void consumeHeavyGoldenContainers(PlayerEntity player, int amount) {
      PlayerInventory inventory = player.method_31548();
      int remaining = amount;

      for (int i = 0; i < inventory.method_5439() && remaining > 0; i++) {
         ItemStack stack = inventory.method_5438(i);
         if (!stack.method_7960() && stack.method_7909() == ModItems.GOLDEN_CONTAINER && stack.method_7985() && stack.method_7969().method_10577("IsHeavy")) {
            if (this.requiredGhostType != null && !this.requiredGhostType.isEmpty()) {
               String containedGhost = GoldenContainerItem.getContainedGhostType(stack);
               if (containedGhost == null) {
                  continue;
               }

               String containedGhostType = containedGhost.contains(":") ? containedGhost.split(":")[1] : containedGhost;
               if (!this.requiredGhostType.equals(containedGhostType)) {
                  continue;
               }
            }

            int toRemove = Math.min(stack.method_7947(), remaining);
            stack.method_7934(toRemove);
            remaining -= toRemove;
            LOGGER.info("消耗沉重的黄金容器: {} 个", toRemove);
            this.returnEmptyGoldenContainers(player, toRemove);
         }
      }
   }

   private void returnEmptyGoldenContainers(PlayerEntity player, int amount) {
      if (amount > 0) {
         ItemStack emptyContainer = new ItemStack(ModItems.GOLDEN_CONTAINER, amount);
         if (player.method_7270(emptyContainer)) {
            LOGGER.info("返还空的黄金容器: {} 个", amount);
         } else {
            LOGGER.warn("无法返还空的黄金容器给玩家，背包可能已满");
            player.method_7328(emptyContainer, false);
         }
      }
   }
}
