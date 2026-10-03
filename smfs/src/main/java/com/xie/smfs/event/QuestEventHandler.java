package com.xie.smfs.event;

import com.xie.smfs.api.IPlayerData;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.QuestConfig;
import com.xie.smfs.manager.QuestManager;
import com.xie.smfs.manager.TutorialManager;
import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents.AfterKilledOtherEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.CopyFrom;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestEventHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(QuestEventHandler.class);

   public static void register() {
      ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((AfterKilledOtherEntity)(world, entity, killedEntity) -> {
         if (entity instanceof ServerPlayerEntity player) {
            handleEntityKill(player, killedEntity);
         }
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         if (server.method_3780() % 20 == 0) {
            for (ServerPlayerEntity player : server.method_3760().method_14571()) {
               checkTimeLimitedQuests(player);
            }
         }
      });
      ServerPlayerEvents.COPY_FROM.register((CopyFrom)(oldPlayer, newPlayer, alive) -> {
         if (oldPlayer instanceof IPlayerData && newPlayer instanceof IPlayerData) {
            IPlayerData oldData = (IPlayerData)oldPlayer;
            IPlayerData newData = (IPlayerData)newPlayer;
            newData.setQuestData(oldData.getQuestData());
         }
      });
      LOGGER.info("任务事件处理器已注册");
   }

   public static void handleEntityKill(ServerPlayerEntity player, Entity entity) {
      IPlayerData playerData = (IPlayerData)player;
      List<NbtCompound> activeQuests = playerData.getActiveQuests();
      String entityTypeId = Registries.field_41177.method_10221(entity.method_5864()).toString();

      for (NbtCompound quest : activeQuests) {
         String questId = quest.method_10558("id");
         NbtList objectives = quest.method_10554("objectives", 10);

         for (int i = 0; i < objectives.size(); i++) {
            NbtCompound objective = objectives.method_10602(i);
            String objectiveId = objective.method_10558("id");
            if (objectiveId.startsWith("kill_")) {
               boolean shouldUpdate = false;
               LOGGER.info("检查击杀目标: objectiveId={}, entityTypeId={}", objectiveId, entityTypeId);
               switch (objectiveId) {
                  case "kill_zombie":
                     if (entityTypeId.equals("minecraft:zombie")
                        || entityTypeId.equals("minecraft:husk")
                        || entityTypeId.equals("minecraft:zombified_piglin")
                        || entityTypeId.equals("minecraft:drowned")) {
                        shouldUpdate = true;
                        LOGGER.info("僵尸实体匹配成功: objectiveId={}, entityTypeId={}", objectiveId, entityTypeId);
                     }
                     break;
                  case "kill_skeleton":
                     if (entityTypeId.equals("minecraft:skeleton") || entityTypeId.equals("minecraft:stray")) {
                        shouldUpdate = true;
                     }
                     break;
                  case "kill_creeper":
                     if (entityTypeId.equals("minecraft:creeper")) {
                        shouldUpdate = true;
                     }
                     break;
                  case "kill_spider":
                     if (entityTypeId.equals("minecraft:spider") || entityTypeId.equals("minecraft:cave_spider")) {
                        shouldUpdate = true;
                     }
                     break;
                  case "kill_ghost_slave":
                     if (entityTypeId.equals("smfs:ghost_slave")) {
                        shouldUpdate = true;
                     }
                     break;
                  default:
                     shouldUpdate = false;
               }

               if (shouldUpdate) {
                  NbtCompound data = PlayerEvents.getCachedData(player);
                  String progressKey = "quest_kill_progress_" + objectiveId;
                  int currentProgress = data.method_10545(progressKey) ? data.method_10550(progressKey) : 0;
                  int newProgress = currentProgress + 1;
                  data.method_10569(progressKey, newProgress);
                  LOGGER.info("保存击杀进度: progressKey={}, 旧进度={}, 新进度={}", progressKey, currentProgress, newProgress);
                  PlayerEvents.saveDataToPlayer(player, data);
                  NbtCompound savedData = PlayerEvents.getCachedData(player);
                  int savedProgress = savedData.method_10545(progressKey) ? savedData.method_10550(progressKey) : -1;
                  LOGGER.info("验证保存结果: progressKey={}, 保存后进度={}", progressKey, savedProgress);
                  player.method_7353(Text.method_43470("§a击杀进度更新: " + objective.method_10558("description")), false);
                  QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               }
            }
         }
      }
   }

   private static void handleItemUse(ServerPlayerEntity player, ItemStack itemStack) {
      IPlayerData playerData = (IPlayerData)player;

      for (NbtCompound quest : playerData.getActiveQuests()) {
         String questId = quest.method_10558("id");
         NbtList objectives = quest.method_10554("objectives", 10);

         for (int i = 0; i < objectives.size(); i++) {
            NbtCompound objective = objectives.method_10602(i);
            String objectiveId = objective.method_10558("id");
            if (objectiveId.equals("get_log") && itemStack.method_7909() == Items.field_8583) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_stone") && itemStack.method_7909() == Items.field_20391) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_iron") && itemStack.method_7909() == Items.field_8620) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_gold") && itemStack.method_7909() == Items.field_8695) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.startsWith("get_")) {
               String itemId = QuestConfig.getItemIdForObjective(objectiveId);
               if (QuestManager.isItemMatch(itemStack, itemId)) {
                  QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
                  QuestManager.checkQuestCompletion(player, quest);
               }
            }
         }
      }

      handleGoldSubmission(player, itemStack);
   }

   private static void handleItemPickup(ServerPlayerEntity player, ItemStack itemStack) {
      IPlayerData playerData = (IPlayerData)player;

      for (NbtCompound quest : playerData.getActiveQuests()) {
         String questId = quest.method_10558("id");
         NbtList objectives = quest.method_10554("objectives", 10);

         for (int i = 0; i < objectives.size(); i++) {
            NbtCompound objective = objectives.method_10602(i);
            String objectiveId = objective.method_10558("id");
            if (objectiveId.equals("get_log") && itemStack.method_7909() == Items.field_8583) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_stone") && itemStack.method_7909() == Items.field_20391) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_iron") && itemStack.method_7909() == Items.field_8620) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_gold") && itemStack.method_7909() == Items.field_8695) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.startsWith("get_")) {
               String itemId = QuestConfig.getItemIdForObjective(objectiveId);
               if (QuestManager.isItemMatch(itemStack, itemId)) {
                  QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
                  QuestManager.checkQuestCompletion(player, quest);
               }
            }
         }
      }
   }

   private static void handleGoldSubmission(ServerPlayerEntity player, ItemStack itemStack) {
      if (itemStack.method_7909() == Items.field_8695) {
         IPlayerData playerData = (IPlayerData)player;

         for (NbtCompound quest : playerData.getActiveQuests()) {
            String questId = quest.method_10558("id");
            if ("submit_gold".equals(questId)) {
               NbtList objectives = quest.method_10554("objectives", 10);

               for (int i = 0; i < objectives.size(); i++) {
                  NbtCompound objective = objectives.method_10602(i);
                  String objectiveId = objective.method_10558("id");
                  if ("submit_gold".equals(objectiveId)) {
                     QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
                     QuestManager.checkQuestCompletion(player, quest);
                     player.method_7353(Text.method_43470("§a成功提交一块黄金！"), false);
                     break;
                  }
               }
            }
         }
      }
   }

   private static void checkTimeLimitedQuests(ServerPlayerEntity player) {
      for (NbtCompound quest : QuestManager.getActiveQuests(player)) {
         String questId = quest.method_10558("id");
         long startTime = quest.method_10537("startTime");
         int timeLimitMinutes = quest.method_10550("timeLimit");
         if (timeLimitMinutes > 0) {
            long timeLimitTicks = timeLimitMinutes * 1200L;
            long currentTime = player.method_37908().method_8510();
            long elapsedTicks = currentTime - startTime;
            long remainingTicks = timeLimitTicks - elapsedTicks;
            if (remainingTicks > 0L) {
               long remainingMinutes = remainingTicks / 1200L;
               if (remainingMinutes <= 5L && remainingMinutes > 0L && currentTime % 200L == 0L) {
                  player.method_7353(Text.method_43470("§c警告: 任务 " + quest.method_10558("title") + " 剩余时间仅剩 " + remainingMinutes + " 分钟！"), false);
               }
            }

            if (elapsedTicks >= timeLimitTicks) {
               QuestManager.failQuest(player, questId);
            }
         }
      }
   }

   public static void checkWorldDaysProgress(ServerPlayerEntity player, NbtCompound quest) {
      String questId = quest.method_10558("id");
      NbtList objectives = quest.method_10554("objectives", 10);

      for (int i = 0; i < objectives.size(); i++) {
         NbtCompound objective = objectives.method_10602(i);
         String objectiveId = objective.method_10558("id");
         int target = objective.method_10550("target");
         int currentProgress = objective.method_10550("progress");
         if ("survive_5_days".equals(objectiveId)) {
            int currentDay = TutorialManager.calculateCurrentDay(player.method_37908());
            if (currentDay >= target) {
               if (currentProgress < currentDay) {
                  QuestManager.updateQuestProgress(player, questId, objectiveId, currentDay);
               }

               player.method_7353(Text.method_43470("§a天数条件满足！当前世界天数为：" + currentDay + "/" + target + "，可以提交任务"), false);
            } else {
               player.method_7353(Text.method_43470("§c天数不足！当前世界天数为：" + currentDay + "/" + target + "，无法完成任务"), false);
            }
            break;
         }
      }
   }

   public static void submitWorldDaysQuestFromClient(String questId) {
      checkWorldDaysProgressClient(questId);
   }

   public static void checkWorldDaysProgressClient(String questId) {
      MinecraftClient client = MinecraftClient.method_1551();
      if (client.field_1724 != null && client.field_1687 != null) {
         long totalTime = client.field_1687 != null ? client.field_1687.method_8510() : 0L;
         int currentDay = (int)(totalTime / 24000L) + 1;
         currentDay = Math.max(currentDay, 1);
         NbtCompound playerData = client.field_1724.method_5647(new NbtCompound());
         if (playerData.method_10545("smfs_spirit_data") && playerData.method_10562("smfs_spirit_data").method_10545("questData")) {
            NbtCompound questData = playerData.method_10562("smfs_spirit_data").method_10562("questData");
            NbtList activeQuests = questData.method_10554("activeQuests", 10);

            for (int i = 0; i < activeQuests.size(); i++) {
               NbtCompound quest = activeQuests.method_10602(i);
               if (quest.method_10558("id").equals(questId)) {
                  NbtList objectives = quest.method_10554("objectives", 10);

                  for (int j = 0; j < objectives.size(); j++) {
                     NbtCompound objective = objectives.method_10602(j);
                     if ("survive_5_days".equals(objective.method_10558("id"))) {
                        int target = objective.method_10550("target");
                        int currentProgress = objective.method_10550("progress");
                        if (currentDay >= target) {
                           if (currentProgress < currentDay) {
                              objective.method_10569("progress", currentDay);
                              questData.method_10566("activeQuests", activeQuests);
                              NbtCompound spiritData = playerData.method_10562("smfs_spirit_data");
                              spiritData.method_10566("questData", questData);
                              client.field_1724.method_5651(playerData);
                              client.field_1724.method_7353(Text.method_43470("§a天数条件满足！当前世界天数为：" + currentDay + "/" + target + "，可以提交任务"), false);
                              return;
                           }
                        } else if (currentProgress != currentDay) {
                           objective.method_10569("progress", currentDay);
                           questData.method_10566("activeQuests", activeQuests);
                           NbtCompound spiritData = playerData.method_10562("smfs_spirit_data");
                           spiritData.method_10566("questData", questData);
                           client.field_1724.method_5651(playerData);
                           client.field_1724.method_7353(Text.method_43470("§a当前世界天数: " + currentDay + "/" + target), false);
                           return;
                        }

                        return;
                     }
                  }
                  break;
               }
            }
         }
      }
   }
}
