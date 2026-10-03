package com.xie.smfs.util;

import java.util.List;

public class EventStoryEntry {
   private String id;
   private EventStoryEntry.TriggerConditions triggerConditions;
   private String story;

   public EventStoryEntry() {
   }

   public EventStoryEntry(String id, EventStoryEntry.TriggerConditions triggerConditions, String story) {
      this.id = id;
      this.triggerConditions = triggerConditions;
      this.story = story;
   }

   public EventStoryEntry(String ghostEyeEvent, String s, EventStoryEntry.TriggerConditions conditions) {
   }

   public String getId() {
      return this.id;
   }

   public EventStoryEntry.TriggerConditions getTriggerConditions() {
      return this.triggerConditions;
   }

   public String getStory() {
      return this.story;
   }

   public static class TriggerConditions {
      private int minDay;
      private List<String> requiredItems;
      private boolean isRepeatable;
      private String timeRange;

      public TriggerConditions() {
      }

      public TriggerConditions(int minDay, List<String> requiredItems, boolean isRepeatable) {
         this.minDay = minDay;
         this.requiredItems = requiredItems;
         this.isRepeatable = isRepeatable;
      }

      public int getMinDay() {
         return this.minDay;
      }

      public List<String> getRequiredItems() {
         return this.requiredItems;
      }

      public boolean isRepeatable() {
         return this.isRepeatable;
      }
   }
}
