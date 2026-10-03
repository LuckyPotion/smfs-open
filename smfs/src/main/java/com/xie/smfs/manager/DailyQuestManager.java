package com.xie.smfs.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyQuestManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/DailyQuestManager");
   public static final int MAX_DAILY_QUESTS = 1;
   public static final int MAX_EVENT_QUESTS = 1;
   public static final String DAILY_QUEST_UNLOCK_PREREQUISITE = "newbie_craft_gold_container";
   public static final String EVENT_QUEST_UNLOCK_PREREQUISITE = "main_tame_ghost";

   public static boolean hasUnlockedDailyQuests(PlayerEntity player) {
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (int i = 0; i < completedQuests.size(); i++) {
         NbtCompound quest = completedQuests.method_10602(i);
         if ("newbie_craft_gold_container".equals(quest.method_10558("id"))) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasUnlockedEventQuests(PlayerEntity player) {
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (int i = 0; i < completedQuests.size(); i++) {
         NbtCompound quest = completedQuests.method_10602(i);
         if ("main_tame_ghost".equals(quest.method_10558("id"))) {
            return true;
         }
      }

      return false;
   }

   public static void clearDailyQuests(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity) {
         NbtCompound questData = QuestManager.getQuestData(player);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);
         NbtList availableQuests = questData.method_10554("availableQuests", 10);
         NbtList newActiveQuests = new NbtList();
         int clearedActiveCount = 0;

         for (int i = 0; i < activeQuests.size(); i++) {
            NbtCompound quest = activeQuests.method_10602(i);
            String questId = quest.method_10558("id");
            String questType = quest.method_10558("type");
            if ("daily".equals(questType)) {
               clearedActiveCount++;
            } else {
               newActiveQuests.add(quest);
            }
         }

         NbtList newAvailableQuests = new NbtList();
         int clearedAvailableCount = 0;

         for (int i = 0; i < availableQuests.size(); i++) {
            NbtCompound quest = availableQuests.method_10602(i);
            String questId = quest.method_10558("id");
            String questType = quest.method_10558("type");
            if ("daily".equals(questType)) {
               clearedAvailableCount++;
            } else {
               newAvailableQuests.add(quest);
            }
         }

         questData.method_10566("activeQuests", newActiveQuests);
         questData.method_10566("availableQuests", newAvailableQuests);
         QuestManager.saveQuestData(player, questData);
      }
   }

   public static void clearEventQuests(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity) {
         NbtCompound questData = QuestManager.getQuestData(player);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);
         NbtList availableQuests = questData.method_10554("availableQuests", 10);
         NbtList newActiveQuests = new NbtList();
         int clearedActiveCount = 0;

         for (int i = 0; i < activeQuests.size(); i++) {
            NbtCompound quest = activeQuests.method_10602(i);
            String questId = quest.method_10558("id");
            String questType = quest.method_10558("type");
            if ("side".equals(questType)) {
               clearedActiveCount++;
            } else {
               newActiveQuests.add(quest);
            }
         }

         NbtList newAvailableQuests = new NbtList();
         int clearedAvailableCount = 0;

         for (int i = 0; i < availableQuests.size(); i++) {
            NbtCompound quest = availableQuests.method_10602(i);
            String questId = quest.method_10558("id");
            String questType = quest.method_10558("type");
            if ("side".equals(questType)) {
               clearedAvailableCount++;
            } else {
               newAvailableQuests.add(quest);
            }
         }

         questData.method_10566("activeQuests", newActiveQuests);
         questData.method_10566("availableQuests", newAvailableQuests);
         QuestManager.saveQuestData(player, questData);
      }
   }

   public static void assignNewDailyQuests(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         if (hasUnlockedDailyQuests(player)) {
            clearDailyQuests(player);
            List<QuestManager.QuestTemplate> dailyQuestTemplates = QuestConfig.getQuestsByType("daily");
            if (!dailyQuestTemplates.isEmpty()) {
               Collections.shuffle(dailyQuestTemplates);
               int assignedCount = 0;

               for (QuestManager.QuestTemplate template : dailyQuestTemplates) {
                  if (assignedCount >= 1) {
                     break;
                  }

                  assignDailyQuestAsAvailable(player, template.getId());
                  assignedCount++;
               }

               NbtCompound questData = QuestManager.getQuestData(player);
               long currentDay = serverPlayer.method_37908().method_8532() / 24000L;
               questData.method_10544("lastDailyAssignDay", currentDay);
               QuestManager.saveQuestData(player, questData);
               if (assignedCount > 0) {
                  serverPlayer.method_7353(Text.method_43470("§a今日日常任务已刷新！请查看任务界面"), false);
                  LOGGER.info("已向玩家 {} 发送日常任务刷新通知", player.method_5477().getString());
               } else {
                  LOGGER.warn("未为玩家 {} 分配任何日常任务", player.method_5477().getString());
               }
            }
         }
      }
   }

   private static void assignDailyQuestAsAvailable(PlayerEntity player, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template != null) {
         NbtCompound questData = QuestManager.getQuestData(player);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);

         for (int i = 0; i < activeQuests.size(); i++) {
            NbtCompound quest = activeQuests.method_10602(i);
            if (quest.method_10558("id").equals(questId)) {
               return;
            }
         }

         NbtCompound newQuest = new NbtCompound();
         newQuest.method_10582("id", questId);
         newQuest.method_10582("title", template.title);
         newQuest.method_10582("description", template.description);
         newQuest.method_10582("type", template.type);
         newQuest.method_10556("isNewbieQuest", template.isNewbieQuest);
         newQuest.method_10569("status", 0);
         newQuest.method_10544("assignTime", System.currentTimeMillis());
         NbtList objectives = new NbtList();

         for (QuestManager.QuestObjective objective : template.objectives) {
            NbtCompound obj = new NbtCompound();
            obj.method_10582("id", objective.id);
            obj.method_10582("description", objective.description);
            obj.method_10569("target", objective.target);
            obj.method_10569("progress", 0);
            objectives.add(obj);
         }

         newQuest.method_10566("objectives", objectives);
         NbtList availableQuests = questData.method_10554("availableQuests", 10);
         availableQuests.add(newQuest);
         questData.method_10566("availableQuests", availableQuests);
         QuestManager.saveQuestData(player, questData);
      }
   }

   private static void assignEventQuestAsAvailable(PlayerEntity player, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template != null) {
         NbtCompound questData = QuestManager.getQuestData(player);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);

         for (int i = 0; i < activeQuests.size(); i++) {
            NbtCompound quest = activeQuests.method_10602(i);
            if (quest.method_10558("id").equals(questId)) {
               return;
            }
         }

         NbtCompound newQuest = new NbtCompound();
         newQuest.method_10582("id", questId);
         newQuest.method_10582("title", template.title);
         newQuest.method_10582("description", template.description);
         newQuest.method_10582("type", template.type);
         newQuest.method_10556("isNewbieQuest", template.isNewbieQuest);
         newQuest.method_10569("status", 0);
         newQuest.method_10544("assignTime", System.currentTimeMillis());
         NbtList objectives = new NbtList();

         for (QuestManager.QuestObjective objective : template.objectives) {
            NbtCompound obj = new NbtCompound();
            obj.method_10582("id", objective.id);
            obj.method_10582("description", objective.description);
            obj.method_10569("target", objective.target);
            obj.method_10569("progress", 0);
            objectives.add(obj);
         }

         newQuest.method_10566("objectives", objectives);
         NbtList availableQuests = questData.method_10554("availableQuests", 10);
         availableQuests.add(newQuest);
         questData.method_10566("availableQuests", availableQuests);
         QuestManager.saveQuestData(player, questData);
      }
   }

   public static void assignNewEventQuests(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         if (hasUnlockedEventQuests(player)) {
            clearEventQuests(player);
            List<QuestManager.QuestTemplate> eventQuestTemplates = QuestConfig.getQuestsByType("side");
            if (!eventQuestTemplates.isEmpty()) {
               Collections.shuffle(eventQuestTemplates);
               if (!eventQuestTemplates.isEmpty()) {
                  QuestManager.QuestTemplate selectedTemplate = eventQuestTemplates.get(0);
                  assignEventQuestAsAvailable(player, selectedTemplate.getId());
                  NbtCompound questData = QuestManager.getQuestData(player);
                  long currentDay = serverPlayer.method_37908().method_8532() / 24000L;
                  questData.method_10544("lastEventAssignDay", currentDay);
                  QuestManager.saveQuestData(player, questData);
                  serverPlayer.method_7353(Text.method_43470("§a今日事件任务已刷新！请查看任务界面"), false);
               }
            }
         }
      }
   }

   public static void checkAndAssignDailyQuests(PlayerEntity player) {
      if (!(player instanceof ServerPlayerEntity serverPlayer)) {
         LOGGER.info("检查任务分配失败：玩家不是ServerPlayerEntity");
      } else {
         NbtCompound questData = QuestManager.getQuestData(player);
         long currentDay = serverPlayer.method_37908().method_8532() / 24000L;
         long lastDailyAssignDay = questData.method_10537("lastDailyAssignDay");
         long lastEventAssignDay = questData.method_10537("lastEventAssignDay");
         if (currentDay != lastDailyAssignDay && hasUnlockedDailyQuests(player)) {
            LOGGER.info("玩家 {} 需要刷新日常任务（天数变化: {} != {})", player.method_5477().getString(), currentDay, lastDailyAssignDay);
            assignNewDailyQuests(player);
         } else if (currentDay == lastDailyAssignDay) {
            LOGGER.info("玩家 {} 今日已分配过日常任务，无需刷新", player.method_5477().getString());
            if (hasUnlockedDailyQuests(player)) {
               List<String> currentDailyQuests = getCurrentDailyQuests(player);
               if (currentDailyQuests.isEmpty() && lastDailyAssignDay == 0L) {
                  LOGGER.info("玩家 {} 刚刚解锁日常任务系统但还没有任务，立即分配新任务", player.method_5477().getString());
                  assignNewDailyQuests(player);
               } else {
                  if (!currentDailyQuests.isEmpty()) {
                     serverPlayer.method_7353(Text.method_43470("§6今日日常任务已分配，请查看任务界面"), false);
                  }

                  forceRefreshQuestUI(serverPlayer);
               }
            }
         } else if (!hasUnlockedDailyQuests(player)) {
            LOGGER.info("玩家 {} 未解锁日常任务系统", player.method_5477().getString());
         }

         if (currentDay != lastEventAssignDay && hasUnlockedEventQuests(player)) {
            assignNewEventQuests(player);
         } else if (currentDay == lastEventAssignDay) {
            if (hasUnlockedEventQuests(player)) {
               List<String> currentEventQuests = getCurrentEventQuests(player);
               if (currentEventQuests.isEmpty() && lastEventAssignDay == 0L) {
                  assignNewEventQuests(player);
               } else {
                  if (!currentEventQuests.isEmpty()) {
                     serverPlayer.method_7353(Text.method_43470("§6今日事件任务已分配，请查看任务界面"), false);
                  }

                  forceRefreshQuestUI(serverPlayer);
               }
            }
         } else if (!hasUnlockedEventQuests(player)) {
            LOGGER.info("玩家 {} 未解锁事件任务系统", player.method_5477().getString());
         }
      }
   }

   public static int getCurrentDailyQuestCount(PlayerEntity player) {
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      int dailyCount = 0;

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         String questType = quest.method_10558("type");
         if ("daily".equals(questType)) {
            dailyCount++;
         }
      }

      return dailyCount;
   }

   public static int getCurrentEventQuestCount(PlayerEntity player) {
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      int eventCount = 0;

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         String questType = quest.method_10558("type");
         if ("side".equals(questType)) {
            eventCount++;
         }
      }

      return eventCount;
   }

   public static List<String> getCurrentDailyQuests(PlayerEntity player) {
      List<String> dailyQuests = new ArrayList<>();
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
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
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         String questId = quest.method_10558("id");
         String questType = quest.method_10558("type");
         if ("side".equals(questType)) {
            eventQuests.add(questId);
         }
      }

      return eventQuests;
   }

   public static boolean acceptDailyQuest(PlayerEntity player, String questId) {
      if (!(player instanceof ServerPlayerEntity serverPlayer)) {
         return false;
      } else {
         NbtCompound questData = QuestManager.getQuestData(player);
         NbtList availableQuests = questData.method_10554("availableQuests", 10);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);
         NbtCompound acceptedQuest = null;

         for (int template = 0; template < availableQuests.size(); template++) {
            NbtCompound quest = availableQuests.method_10602(template);
            if (quest.method_10558("id").equals(questId)) {
               acceptedQuest = quest;
               availableQuests.method_10536(template);
               break;
            }
         }

         if (acceptedQuest != null) {
            acceptedQuest.method_10569("status", 1);
            acceptedQuest.method_10544("startTime", serverPlayer.method_37908().method_8510());
            activeQuests.add(acceptedQuest);
            questData.method_10566("availableQuests", availableQuests);
            questData.method_10566("activeQuests", activeQuests);
            QuestManager.saveQuestData(player, questData);
            QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
            if (template != null) {
               serverPlayer.method_7353(Text.method_43470("§a日常任务开始: " + template.title), false);
            }

            LOGGER.info("玩家 {} 接受了日常任务: {}", player.method_5477().getString(), questId);
            return true;
         } else {
            LOGGER.warn("玩家 {} 尝试接受不存在的日常任务: {}", player.method_5477().getString(), questId);
            return false;
         }
      }
   }

   public static void forceRefreshQuestUI(ServerPlayerEntity player) {
      LOGGER.info("强制刷新玩家 {} 的任务界面", player.method_5477().getString());

      try {
         player.method_7353(Text.method_43470("§6任务界面已刷新，请查看当前任务"), false);
         LOGGER.info("已向玩家 {} 发送任务界面刷新通知", player.method_5477().getString());
      } catch (Exception e) {
         LOGGER.error("强制刷新任务界面时发生错误: {}", e.getMessage());
      }
   }
}
