package com.xie.smfs.manager;

import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface QuestDetectionStrategy {
   Logger LOGGER = LoggerFactory.getLogger("smfs/QuestDetectionStrategy");

   boolean checkCondition(PlayerEntity playerEntity, String string, int i);

   int getCurrentProgress(PlayerEntity playerEntity, String string);

   void consumeItems(PlayerEntity playerEntity, String string, int i);
}
