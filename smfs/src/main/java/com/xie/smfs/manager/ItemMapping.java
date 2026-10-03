package com.xie.smfs.manager;

public class ItemMapping {
   private final String objectiveId;
   private final String itemId;
   private final boolean consumeItems;

   public ItemMapping(String objectiveId, String itemId, boolean consumeItems) {
      this.objectiveId = objectiveId;
      this.itemId = itemId;
      this.consumeItems = consumeItems;
   }

   public String getObjectiveId() {
      return this.objectiveId;
   }

   public String getItemId() {
      return this.itemId;
   }

   public boolean shouldConsumeItems() {
      return this.consumeItems;
   }
}
