package com.xie.smfs.api;

import com.xie.smfs.manager.QuestManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestAPI {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestAPI");
   public static final String QUEST_TYPE_NEWBIE = "newbie";
   public static final String QUEST_TYPE_MAIN = "main";
   public static final String QUEST_TYPE_SIDE = "side";
   public static final String QUEST_TYPE_DAILY = "daily";
   public static final String QUEST_TYPE_EVENT = "event";
   public static final int QUEST_STATUS_NOT_STARTED = 0;
   public static final int QUEST_STATUS_IN_PROGRESS = 1;
   public static final int QUEST_STATUS_COMPLETED = 2;
   public static final int QUEST_STATUS_FAILED = 3;
   private static final Map<String, QuestManager.QuestTemplate> ADDON_QUEST_TEMPLATES = new HashMap<>();

   public static boolean registerQuestTemplate(QuestManager.QuestTemplate template) {
      if (template == null) {
         LOGGER.warn("尝试注册空的任务模板");
         return false;
      } else if (template.id == null || template.id.trim().isEmpty()) {
         LOGGER.warn("任务模板ID不能为空");
         return false;
      } else if (QuestManager.QUEST_TEMPLATES.containsKey(template.id)) {
         LOGGER.warn("任务ID '{}' 已存在，无法重复注册", template.id);
         return false;
      } else if (ADDON_QUEST_TEMPLATES.containsKey(template.id)) {
         LOGGER.warn("任务ID '{}' 已被其他附属模组注册，无法重复注册", template.id);
         return false;
      } else if (!isValidQuestType(template.type)) {
         LOGGER.warn("无效的任务类型: {}", template.type);
         return false;
      } else if (template.objectives != null && !template.objectives.isEmpty()) {
         ADDON_QUEST_TEMPLATES.put(template.id, template);
         QuestManager.QUEST_TEMPLATES.put(template.id, template);
         LOGGER.debug("附属模组成功注册任务模板: {} - {}", template.id, template.title);
         return true;
      } else {
         LOGGER.warn("任务 '{}' 必须包含至少一个目标", template.id);
         return false;
      }
   }

   public static int registerQuestTemplates(List<QuestManager.QuestTemplate> templates) {
      if (templates != null && !templates.isEmpty()) {
         int successCount = 0;

         for (QuestManager.QuestTemplate template : templates) {
            if (registerQuestTemplate(template)) {
               successCount++;
            }
         }

         LOGGER.debug("批量注册任务模板完成，成功注册 {} 个任务", successCount);
         return successCount;
      } else {
         return 0;
      }
   }

   public static boolean registerSimpleQuest(
      String questId,
      String title,
      String description,
      String questType,
      String objectiveId,
      String objectiveDesc,
      int targetCount,
      Map<String, Object> rewards,
      List<String> prerequisites
   ) {
      QuestManager.QuestObjective objective = new QuestManager.QuestObjective(objectiveId, objectiveDesc, targetCount);
      QuestManager.QuestTemplate template = new QuestManager.QuestTemplate(
         questId, title, description, questType, Arrays.asList(objective), rewards, prerequisites, false, 0
      );
      return registerQuestTemplate(template);
   }

   public static boolean registerSimpleQuest(String questId, String questName, String questType, String description, Map<String, Integer> objectives) {
      if (objectives != null && !objectives.isEmpty()) {
         List<QuestManager.QuestObjective> objectiveList = new ArrayList<>();

         for (Entry<String, Integer> entry : objectives.entrySet()) {
            objectiveList.add(new QuestManager.QuestObjective(entry.getKey(), entry.getKey(), entry.getValue()));
         }

         QuestManager.QuestTemplate template = new QuestManager.QuestTemplate(questId, questName, description, questType, objectiveList, null, null, false, 0);
         return registerQuestTemplate(template);
      } else {
         LOGGER.warn("任务 '{}' 必须包含至少一个目标", questId);
         return false;
      }
   }

   public static boolean isQuestRegistered(String questId) {
      return QuestManager.QUEST_TEMPLATES.containsKey(questId);
   }

   public static Map<String, QuestManager.QuestTemplate> getAddonQuestTemplates() {
      return new HashMap<>(ADDON_QUEST_TEMPLATES);
   }

   public static int getAddonQuestCount() {
      return ADDON_QUEST_TEMPLATES.size();
   }

   public static boolean unregisterQuestTemplate(String questId) {
      if (!ADDON_QUEST_TEMPLATES.containsKey(questId)) {
         LOGGER.warn("任务 '{}' 不是由附属模组注册的，无法卸载", questId);
         return false;
      } else {
         QuestManager.QUEST_TEMPLATES.remove(questId);
         ADDON_QUEST_TEMPLATES.remove(questId);
         LOGGER.debug("成功卸载附属模组任务模板: {}", questId);
         return true;
      }
   }

   public static int unregisterQuestTemplates(List<String> questIds) {
      if (questIds != null && !questIds.isEmpty()) {
         int successCount = 0;

         for (String questId : questIds) {
            if (unregisterQuestTemplate(questId)) {
               successCount++;
            }
         }

         LOGGER.debug("批量卸载任务模板完成，成功卸载 {} 个任务", successCount);
         return successCount;
      } else {
         return 0;
      }
   }

   private static boolean isValidQuestType(String questType) {
      return "newbie".equals(questType) || "main".equals(questType) || "side".equals(questType) || "daily".equals(questType) || "event".equals(questType);
   }

   public static void updateQuestProgress(PlayerEntity player, String questId, String objectiveId, int amount) {
      if (player == null) {
         LOGGER.warn("任务进度更新失败: 玩家实体为null");
      } else if (questId != null && objectiveId != null) {
         QuestManager.updateQuestProgress(player, questId, objectiveId, amount);
         LOGGER.debug("附属模组任务进度更新: {} - {} - {}", questId, objectiveId, amount);
      } else {
         LOGGER.warn("任务进度更新失败: 任务ID或目标ID为null");
      }
   }

   public static boolean hasCompletedQuest(PlayerEntity player, String questId) {
      if (player != null && questId != null) {
         NbtCompound questData = QuestManager.getQuestData(player);
         if (questData == null) {
            return false;
         }

         NbtList completedQuests = questData.method_10554("completedQuests", 10);

         for (int i = 0; i < completedQuests.size(); i++) {
            NbtCompound quest = completedQuests.method_10602(i);
            if (questId.equals(quest.method_10558("id"))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static List<String> getActiveQuests(PlayerEntity player) {
      List<String> activeQuestIds = new ArrayList<>();
      if (player == null) {
         return activeQuestIds;
      }

      NbtCompound questData = QuestManager.getQuestData(player);
      if (questData == null) {
         return activeQuestIds;
      }

      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         activeQuestIds.add(quest.method_10558("id"));
      }

      return activeQuestIds;
   }

   public static void reloadAddonQuests() {
      LOGGER.debug("开始重新加载附属模组任务模板...");

      for (String questId : ADDON_QUEST_TEMPLATES.keySet()) {
         QuestManager.QUEST_TEMPLATES.remove(questId);
      }

      int reloadedCount = 0;

      for (Entry<String, QuestManager.QuestTemplate> entry : ADDON_QUEST_TEMPLATES.entrySet()) {
         QuestManager.QUEST_TEMPLATES.put(entry.getKey(), entry.getValue());
         reloadedCount++;
      }

      LOGGER.debug("重新加载附属模组任务模板完成，共加载 {} 个任务", reloadedCount);
   }
}
