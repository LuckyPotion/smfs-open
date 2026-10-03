package com.xie.smfs.manager;

import com.xie.smfs.common.events.PlayerEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

class KillQuestStrategy implements QuestDetectionStrategy {
   @Override
   public boolean checkCondition(PlayerEntity player, String objectiveId, int targetCount) {
      return this.getCurrentProgress(player, objectiveId) >= targetCount;
   }

   @Override
   public int getCurrentProgress(PlayerEntity player, String objectiveId) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      String progressKey = "quest_kill_progress_" + objectiveId;
      return data.contains(progressKey) ? data.getInt(progressKey) : 0;
   }

   @Override
   public void consumeItems(PlayerEntity player, String objectiveId, int amount) {
      LOGGER.info("击杀任务目标 {} 不需要消耗物品", objectiveId);
   }
}
