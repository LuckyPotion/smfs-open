package com.xie.smfs.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestUIManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestUIManager");

   public static List<QuestUIManager.QuestInfo> getAvailableQuests(PlayerEntity player) {
      List<QuestUIManager.QuestInfo> availableQuests = new ArrayList<>();
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (Entry<String, QuestManager.QuestTemplate> entry : QuestManager.QUEST_TEMPLATES.entrySet()) {
         String questId = entry.getKey();
         QuestManager.QuestTemplate template = entry.getValue();
         if (!"daily".equals(template.type) && !"event".equals(template.type)) {
            boolean alreadyHasQuest = false;

            for (int i = 0; i < activeQuests.size(); i++) {
               NbtCompound quest = activeQuests.method_10602(i);
               if (quest.method_10558("id").equals(questId)) {
                  alreadyHasQuest = true;
                  break;
               }
            }

            boolean alreadyCompleted = false;

            for (int i = 0; i < completedQuests.size(); i++) {
               NbtCompound quest = completedQuests.method_10602(i);
               if (quest.method_10558("id").equals(questId)) {
                  alreadyCompleted = true;
                  break;
               }
            }

            if (!alreadyHasQuest && !alreadyCompleted) {
               boolean prerequisitesMet = true;

               for (String prerequisite : template.prerequisites) {
                  boolean prerequisiteCompleted = false;

                  for (int i = 0; i < completedQuests.size(); i++) {
                     NbtCompound completedQuest = completedQuests.method_10602(i);
                     if (completedQuest.method_10558("id").equals(prerequisite)) {
                        prerequisiteCompleted = true;
                        break;
                     }
                  }

                  if (!prerequisiteCompleted) {
                     prerequisitesMet = false;
                     break;
                  }
               }

               if (prerequisitesMet) {
                  QuestUIManager.QuestInfo questInfo = new QuestUIManager.QuestInfo(questId, template.title, template.description, template.type);
                  availableQuests.add(questInfo);
               }
            }
         }
      }

      if (DailyQuestManager.hasUnlockedDailyQuests(player)) {
         for (String questId : DailyQuestManager.getCurrentDailyQuests(player)) {
            QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
            if (template != null) {
               QuestUIManager.QuestInfo questInfo = new QuestUIManager.QuestInfo(questId, template.title, template.description, template.type);
               availableQuests.add(questInfo);
            }
         }
      }

      if (DailyQuestManager.hasUnlockedEventQuests(player)) {
         for (String questId : DailyQuestManager.getCurrentEventQuests(player)) {
            QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
            if (template != null) {
               QuestUIManager.QuestInfo questInfo = new QuestUIManager.QuestInfo(questId, template.title, template.description, template.type);
               availableQuests.add(questInfo);
            }
         }
      }

      return availableQuests;
   }

   public static List<String> getCurrentDailyQuests(PlayerEntity player) {
      List<String> dailyQuests = new ArrayList<>();
      if (!DailyQuestManager.hasUnlockedDailyQuests(player)) {
         return dailyQuests;
      }

      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList availableQuests = questData.method_10554("availableQuests", 10);

      for (int i = 0; i < availableQuests.size(); i++) {
         NbtCompound quest = availableQuests.method_10602(i);
         String questId = quest.method_10558("id");
         String questType = quest.method_10558("type");
         if ("daily".equals(questType)) {
            dailyQuests.add(questId);
         }
      }

      return dailyQuests;
   }

   public static List<String> getCurrentEventQuests(PlayerEntity player) {
      List<String> eventQuests = new ArrayList<>();
      if (!DailyQuestManager.hasUnlockedEventQuests(player)) {
         return eventQuests;
      }

      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList availableQuests = questData.method_10554("availableQuests", 10);

      for (int i = 0; i < availableQuests.size(); i++) {
         NbtCompound quest = availableQuests.method_10602(i);
         String questId = quest.method_10558("id");
         String questType = quest.method_10558("type");
         if ("side".equals(questType)) {
            eventQuests.add(questId);
         }
      }

      return eventQuests;
   }

   public static Map<String, List<String>> getCategorizedQuests(PlayerEntity player) {
      Map<String, List<String>> categorizedQuests = new LinkedHashMap<>();
      List<String> mainQuests = new ArrayList<>();
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         String questId = quest.method_10558("id");
         if (QuestConfig.getMainQuestIds().contains(questId)) {
            mainQuests.add(questId);
         }
      }

      if (!mainQuests.isEmpty()) {
         categorizedQuests.put("主线任务", mainQuests);
      }

      List<String> dailyQuests = getCurrentDailyQuests(player);
      if (!dailyQuests.isEmpty()) {
         categorizedQuests.put("日常任务", dailyQuests);
      }

      List<String> eventQuests = getCurrentEventQuests(player);
      if (!eventQuests.isEmpty()) {
         categorizedQuests.put("事件任务", eventQuests);
      }

      List<String> newbieQuests = new ArrayList<>();

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         String questId = quest.method_10558("id");
         String questType = quest.method_10558("type");
         if ("newbie".equals(questType)) {
            newbieQuests.add(questId);
         }
      }

      if (!newbieQuests.isEmpty()) {
         categorizedQuests.put("新手任务", newbieQuests);
      }

      return categorizedQuests;
   }

   public static List<QuestUIManager.QuestInfo> getAvailableMainQuests(PlayerEntity player) {
      List<QuestUIManager.QuestInfo> mainQuests = new ArrayList<>();

      for (QuestUIManager.QuestInfo questInfo : getAvailableQuests(player)) {
         if (!"daily".equals(questInfo.type) && !"side".equals(questInfo.type)) {
            mainQuests.add(questInfo);
         }
      }

      return mainQuests;
   }

   public static List<QuestUIManager.QuestInfo> getAvailableDailyQuests(PlayerEntity player) {
      List<QuestUIManager.QuestInfo> dailyQuests = new ArrayList<>();
      if (!DailyQuestManager.hasUnlockedDailyQuests(player)) {
         return dailyQuests;
      }

      for (String questId : DailyQuestManager.getCurrentDailyQuests(player)) {
         QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
         if (template != null) {
            QuestUIManager.QuestInfo questInfo = new QuestUIManager.QuestInfo(questId, template.title, template.description, template.type);
            dailyQuests.add(questInfo);
         }
      }

      return dailyQuests;
   }

   public static List<QuestUIManager.QuestInfo> getAvailableEventQuests(PlayerEntity player) {
      List<QuestUIManager.QuestInfo> eventQuests = new ArrayList<>();
      if (!DailyQuestManager.hasUnlockedEventQuests(player)) {
         return eventQuests;
      }

      for (String questId : DailyQuestManager.getCurrentEventQuests(player)) {
         QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
         if (template != null) {
            QuestUIManager.QuestInfo questInfo = new QuestUIManager.QuestInfo(questId, template.title, template.description, template.type);
            eventQuests.add(questInfo);
         }
      }

      return eventQuests;
   }

   public static boolean isDailyQuestSystemUnlocked(PlayerEntity player) {
      return DailyQuestManager.hasUnlockedDailyQuests(player);
   }

   public static boolean isEventQuestSystemUnlocked(PlayerEntity player) {
      return DailyQuestManager.hasUnlockedEventQuests(player);
   }

   public static Map<String, Boolean> getQuestUnlockStatus(PlayerEntity player) {
      Map<String, Boolean> unlockStatus = new HashMap<>();
      unlockStatus.put("dailyQuests", DailyQuestManager.hasUnlockedDailyQuests(player));
      unlockStatus.put("eventQuests", DailyQuestManager.hasUnlockedEventQuests(player));
      return unlockStatus;
   }

   public static Map<String, Integer> getQuestStatistics(PlayerEntity player) {
      Map<String, Integer> statistics = new HashMap<>();
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      statistics.put("activeQuests", activeQuests.size());
      NbtList completedQuests = questData.method_10554("completedQuests", 10);
      statistics.put("completedQuests", completedQuests.size());
      statistics.put("dailyQuests", DailyQuestManager.getCurrentDailyQuestCount(player));
      statistics.put("eventQuests", DailyQuestManager.getCurrentEventQuestCount(player));
      return statistics;
   }

   public static boolean canAcceptQuest(PlayerEntity player, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template == null) {
         return false;
      }

      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId)) {
            return false;
         }
      }

      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (int i = 0; i < completedQuests.size(); i++) {
         NbtCompound quest = completedQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId)) {
            return false;
         }
      }

      for (String prerequisite : template.prerequisites) {
         boolean prerequisiteCompleted = false;

         for (int i = 0; i < completedQuests.size(); i++) {
            NbtCompound completedQuest = completedQuests.method_10602(i);
            if (completedQuest.method_10558("id").equals(prerequisite)) {
               prerequisiteCompleted = true;
               break;
            }
         }

         if (!prerequisiteCompleted) {
            return false;
         }
      }

      return true;
   }

   public static Map<String, Object> getQuestDetails(PlayerEntity player, String questId) {
      Map<String, Object> details = new HashMap<>();
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template == null) {
         return details;
      }

      details.put("id", questId);
      details.put("title", template.title);
      details.put("description", template.description);
      details.put("type", template.type);
      details.put("isNewbieQuest", template.isNewbieQuest);
      List<Map<String, Object>> objectives = new ArrayList<>();

      for (QuestManager.QuestObjective objective : template.objectives) {
         Map<String, Object> objInfo = new HashMap<>();
         objInfo.put("id", objective.id);
         objInfo.put("description", objective.description);
         objInfo.put("target", objective.target);
         objectives.add(objInfo);
      }

      details.put("objectives", objectives);
      details.put("rewards", template.rewards);
      details.put("prerequisites", template.prerequisites);
      details.put("canAccept", canAcceptQuest(player, questId));
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      boolean isActive = false;

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId)) {
            isActive = true;
            NbtList questObjectives = quest.method_10554("objectives", 10);
            List<Map<String, Object>> progressInfo = new ArrayList<>();

            for (int j = 0; j < questObjectives.size(); j++) {
               NbtCompound objective = questObjectives.method_10602(j);
               Map<String, Object> progress = new HashMap<>();
               progress.put("id", objective.method_10558("id"));
               progress.put("progress", objective.method_10550("progress"));
               progress.put("target", objective.method_10550("target"));
               progressInfo.add(progress);
            }

            details.put("progress", progressInfo);
            break;
         }
      }

      details.put("isActive", isActive);
      return details;
   }

   public static Text getQuestDisplayText(PlayerEntity player, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template == null) {
         return Text.method_43470("未知任务");
      }

      String prefix = "";

      return Text.method_43470(switch (template.type) {
         case "main" -> "§6[主线]§r ";
         case "side" -> "§5[事件]§r ";
         case "daily" -> "§a[日常]§r ";
         default -> "§7[任务]§r ";
      } + template.title);
   }

   public static Text getQuestDescriptionText(PlayerEntity player, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      return template == null ? Text.method_43470("任务描述不可用") : Text.method_43470("§7" + template.description);
   }

   public static class QuestInfo {
      public String id;
      public String title;
      public String description;
      public String type;

      public QuestInfo(String id, String title, String description, String type) {
         this.id = id;
         this.title = title;
         this.description = description;
         this.type = type;
      }
   }
}
