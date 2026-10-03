package com.xie.smfs.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;

public interface DialogueCapable {
   String[] getGreetingDialogues();

   default List<String> getStoryDialogues() {
      return null;
   }

   default boolean isStoryMode() {
      List<String> stories = this.getStoryDialogues();
      return stories != null && !stories.isEmpty();
   }

   default boolean hasCompletedStory(PlayerEntity player) {
      return false;
   }

   default void markStoryCompleted(PlayerEntity player) {
   }

   default Map<String, String> getQuestList() {
      return Collections.emptyMap();
   }
}
