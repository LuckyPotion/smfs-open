package com.xie.smfs.manager;

import com.xie.smfs.util.EventStoryEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TutorialManager {
   protected static final Logger LOGGER = LoggerFactory.getLogger(TutorialManager.class);
   protected final List<TutorialManager.TutorialEntry> tutorials = new ArrayList<>();
   protected final List<EventStoryEntry> eventStories = new ArrayList<>();
   protected int currentDay;

   protected TutorialManager() {
   }

   public abstract int calculateCurrentDay();

   protected abstract void loadTutorials();

   protected abstract void loadEventStories();

   public static int calculateCurrentDay(World world) {
      if (world != null) {
         long totalTime = world.getTime();
         int day = (int)(totalTime / 24000L) + 1;
         return Math.max(day, 1);
      } else {
         return 1;
      }
   }

   protected List<TutorialManager.TutorialEntry> createDefaultTutorials() {
      List<TutorialManager.TutorialEntry> defaultTutorials = new ArrayList<>();
      defaultTutorials.add(new TutorialManager.TutorialEntry(1, "加载失败。"));
      return defaultTutorials;
   }

   protected List<EventStoryEntry> createDefaultEvents() {
      List<EventStoryEntry> defaultEvents = new ArrayList<>();
      EventStoryEntry.TriggerConditions conditions = new EventStoryEntry.TriggerConditions(1, Collections.singletonList("smfs:guiyan"), false);
      defaultEvents.add(new EventStoryEntry("ghost_eye_event", "默认事件剧情。", conditions));
      return defaultEvents;
   }

   protected abstract String getPlayerName();

   protected String processStoryVariables(String story) {
      String playerName = this.getPlayerName();
      return story.replace("{playerName}", playerName);
   }

   public String getCurrentStory() {
      this.currentDay = this.calculateCurrentDay();
      int viewCount = this.getViewCount();
      if (viewCount == 0) {
         String dailyStory = this.processStoryVariables(this.getDailyStory());
         this.incrementViewCount();
         return dailyStory;
      }

      EventStoryEntry eventStory = this.getValidEventStory();
      if (eventStory != null) {
         if (!eventStory.getTriggerConditions().isRepeatable()) {
            this.addShownEvent(eventStory.getId());
         }

         return this.processStoryVariables(eventStory.getStory());
      } else {
         return this.processStoryVariables(this.getDailyStory());
      }
   }

   public String getDailyStory() {
      if (this.tutorials.isEmpty()) {
         return "暂无剧情内容";
      }

      for (TutorialManager.TutorialEntry entry : this.tutorials) {
         if (entry.getDay() == this.currentDay) {
            return entry.getStory();
         }
      }

      return this.currentDay > this.tutorials.get(this.tutorials.size() - 1).getDay()
         ? this.tutorials.get(this.tutorials.size() - 1).getStory()
         : this.tutorials.get(0).getStory();
   }

   protected abstract int getViewCount();

   public boolean isFirstView() {
      return this.getViewCount() == 0;
   }

   protected abstract void incrementViewCount();

   protected abstract List<String> getShownEvents();

   protected abstract void addShownEvent(String string);

   protected abstract EventStoryEntry getValidEventStory();

   protected boolean hasRequiredItems(PlayerEntity player, List<String> requiredItems) {
      if (requiredItems.isEmpty()) {
         return true;
      }

      for (String itemId : requiredItems) {
         boolean found = false;

         for (int i = 0; i < player.getInventory().size(); i++) {
            if (Registries.ITEM.getKey(player.getInventory().getStack(i).getItem()).toString().equals(itemId)) {
               found = true;
               break;
            }
         }

         if (!found) {
            return false;
         }
      }

      return true;
   }

   public static class TutorialEntry {
      private int day;
      private String story;

      public TutorialEntry(int day, String story) {
         this.day = day;
         this.story = story;
      }

      public int getDay() {
         return this.day;
      }

      public String getStory() {
         return this.story;
      }
   }
}
