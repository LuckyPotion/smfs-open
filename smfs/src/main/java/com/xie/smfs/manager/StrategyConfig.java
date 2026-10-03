package com.xie.smfs.manager;

class StrategyConfig {
   private final String objectiveId;
   private final String strategyType;

   public StrategyConfig(String objectiveId, String strategyType) {
      this.objectiveId = objectiveId;
      this.strategyType = strategyType;
   }

   public String getStrategyType() {
      return this.strategyType;
   }

   public String getObjectiveId() {
      return this.objectiveId;
   }
}
