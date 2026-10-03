package com.xie.smfs.manager;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

class DaySurvivalStrategy implements QuestDetectionStrategy {
   @Override
   public boolean checkCondition(PlayerEntity player, String objectiveId, int targetCount) {
      return this.getCurrentProgress(player, objectiveId) >= targetCount;
   }

   @Override
   public int getCurrentProgress(PlayerEntity player, String objectiveId) {
      return player instanceof ServerPlayerEntity serverPlayer && serverPlayer.method_37908() instanceof ServerWorld serverWorld
         ? TutorialManager.calculateCurrentDay(serverWorld)
         : 0;
   }

   @Override
   public void consumeItems(PlayerEntity player, String objectiveId, int amount) {
   }
}
