package com.xie.smfs.manager;

import net.minecraft.entity.player.PlayerEntity;

class ItemCollectionStrategy implements QuestDetectionStrategy {
   @Override
   public boolean checkCondition(PlayerEntity player, String objectiveId, int targetCount) {
      int currentProgress = this.getCurrentProgress(player, objectiveId);
      return currentProgress >= targetCount;
   }

   @Override
   public int getCurrentProgress(PlayerEntity player, String objectiveId) {
      ItemMapping itemMapping = QuestConfig.getItemMapping(objectiveId);
      if (itemMapping == null) {
         LOGGER.warn("未找到目标ID对应的物品配置: {}", objectiveId);
         return 0;
      } else {
         return QuestDetectionHelper.countPlayerItems(player, itemMapping.getItemId());
      }
   }

   @Override
   public void consumeItems(PlayerEntity player, String objectiveId, int amount) {
      ItemMapping itemMapping = QuestConfig.getItemMapping(objectiveId);
      if (itemMapping == null) {
         LOGGER.warn("未找到目标ID对应的物品配置，无法消耗物品: {}", objectiveId);
      } else {
         QuestDetectionHelper.consumePlayerItems(player, itemMapping.getItemId(), amount);
      }
   }
}
