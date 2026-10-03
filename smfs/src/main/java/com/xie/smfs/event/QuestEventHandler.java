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
         if (server.getTicks() % 20 == 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
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
      String entityTypeId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();

      for (NbtCompound quest : activeQuests) {
         String questId = quest.getString("id");
         NbtList objectives = quest.getList("objectives", 10);

         for (int i = 0; i < objectives.size(); i++) {
            NbtCompound objective = objectives.getCompound(i);
            String objectiveId = objective.getString("id");
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
                  int currentProgress = data.contains(progressKey) ? data.getInt(progressKey) : 0;
                  int newProgress = currentProgress + 1;
                  data.putInt(progressKey, newProgress);
                  LOGGER.info("保存击杀进度: progressKey={}, 旧进度={}, 新进度={}", progressKey, currentProgress, newProgress);
                  PlayerEvents.saveDataToPlayer(player, data);
                  NbtCompound savedData = PlayerEvents.getCachedData(player);
                  int savedProgress = savedData.contains(progressKey) ? savedData.getInt(progressKey) : -1;
                  LOGGER.info("验证保存结果: progressKey={}, 保存后进度={}", progressKey, savedProgress);
                  player.sendMessage(Text.literal("§a击杀进度更新: " + objective.getString("description")), false);
                  QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               }
            }
         }
      }
   }

   private static void handleItemUse(ServerPlayerEntity player, ItemStack itemStack) {
      IPlayerData playerData = (IPlayerData)player;

      for (NbtCompound quest : playerData.getActiveQuests()) {
         String questId = quest.getString("id");
         NbtList objectives = quest.getList("objectives", 10);

         for (int i = 0; i < objectives.size(); i++) {
            NbtCompound objective = objectives.getCompound(i);
            String objectiveId = objective.getString("id");
            if (objectiveId.equals("get_log") && itemStack.getItem() == Items.OAK_LOG) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_stone") && itemStack.getItem() == Items.STONE) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_iron") && itemStack.getItem() == Items.IRON_INGOT) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_gold") && itemStack.getItem() == Items.GOLD_INGOT) {
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
         String questId = quest.getString("id");
         NbtList objectives = quest.getList("objectives", 10);

         for (int i = 0; i < objectives.size(); i++) {
            NbtCompound objective = objectives.getCompound(i);
            String objectiveId = objective.getString("id");
            if (objectiveId.equals("get_log") && itemStack.getItem() == Items.OAK_LOG) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_stone") && itemStack.getItem() == Items.STONE) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_iron") && itemStack.getItem() == Items.IRON_INGOT) {
               QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
               QuestManager.checkQuestCompletion(player, quest);
            } else if (objectiveId.equals("get_gold") && itemStack.getItem() == Items.GOLD_INGOT) {
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
      if (itemStack.getItem() == Items.GOLD_INGOT) {
         IPlayerData playerData = (IPlayerData)player;

         for (NbtCompound quest : playerData.getActiveQuests()) {
            String questId = quest.getString("id");
            if ("submit_gold".equals(questId)) {
               NbtList objectives = quest.getList("objectives", 10);

               for (int i = 0; i < objectives.size(); i++) {
                  NbtCompound objective = objectives.getCompound(i);
                  String objectiveId = objective.getString("id");
                  if ("submit_gold".equals(objectiveId)) {
                     QuestManager.updateQuestProgress(player, questId, objectiveId, 1);
                     QuestManager.checkQuestCompletion(player, quest);
                     player.sendMessage(Text.literal("§a成功提交一块黄金！"), false);
                     break;
                  }
               }
            }
         }
      }
   }

   private static void checkTimeLimitedQuests(ServerPlayerEntity player) {
      for (NbtCompound quest : QuestManager.getActiveQuests(player)) {
         String questId = quest.getString("id");
         long startTime = quest.getLong("startTime");
         int timeLimitMinutes = quest.getInt("timeLimit");
         if (timeLimitMinutes > 0) {
            long timeLimitTicks = timeLimitMinutes * 1200L;
            long currentTime = player.getWorld().getTime();
            long elapsedTicks = currentTime - startTime;
            long remainingTicks = timeLimitTicks - elapsedTicks;
            if (remainingTicks > 0L) {
               long remainingMinutes = remainingTicks / 1200L;
               if (remainingMinutes <= 5L && remainingMinutes > 0L && currentTime % 200L == 0L) {
                  player.sendMessage(Text.literal("§c警告: 任务 " + quest.getString("title") + " 剩余时间仅剩 " + remainingMinutes + " 分钟！"), false);
               }
            }

            if (elapsedTicks >= timeLimitTicks) {
               QuestManager.failQuest(player, questId);
            }
         }
      }
   }

   public static void checkWorldDaysProgress(ServerPlayerEntity player, NbtCompound quest) {
      String questId = quest.getString("id");
      NbtList objectives = quest.getList("objectives", 10);

      for (int i = 0; i < objectives.size(); i++) {
         NbtCompound objective = objectives.getCompound(i);
         String objectiveId = objective.getString("id");
         int target = objective.getInt("target");
         int currentProgress = objective.getInt("progress");
         if ("survive_5_days".equals(objectiveId)) {
            int currentDay = TutorialManager.calculateCurrentDay(player.getWorld());
            if (currentDay >= target) {
               if (currentProgress < currentDay) {
                  QuestManager.updateQuestProgress(player, questId, objectiveId, currentDay);
               }

               player.sendMessage(Text.literal("§a天数条件满足！当前世界天数为：" + currentDay + "/" + target + "，可以提交任务"), false);
            } else {
               player.sendMessage(Text.literal("§c天数不足！当前世界天数为：" + currentDay + "/" + target + "，无法完成任务"), false);
            }
            break;
         }
      }
   }

   public static void submitWorldDaysQuestFromClient(String questId) {
      checkWorldDaysProgressClient(questId);
   }

   public static void checkWorldDaysProgressClient(String questId) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         long totalTime = client.world != null ? client.world.getTime() : 0L;
         int currentDay = (int)(totalTime / 24000L) + 1;
         currentDay = Math.max(currentDay, 1);
         NbtCompound playerData = client.player.writeNbt(new NbtCompound());
         if (playerData.contains("smfs_spirit_data") && playerData.getCompound("smfs_spirit_data").contains("questData")) {
            NbtCompound questData = playerData.getCompound("smfs_spirit_data").getCompound("questData");
            NbtList activeQuests = questData.getList("activeQuests", 10);

            for (int i = 0; i < activeQuests.size(); i++) {
               NbtCompound quest = activeQuests.getCompound(i);
               if (quest.getString("id").equals(questId)) {
                  NbtList objectives = quest.getList("objectives", 10);

                  for (int j = 0; j < objectives.size(); j++) {
                     NbtCompound objective = objectives.getCompound(j);
                     if ("survive_5_days".equals(objective.getString("id"))) {
                        int target = objective.getInt("target");
                        int currentProgress = objective.getInt("progress");
                        if (currentDay >= target) {
                           if (currentProgress < currentDay) {
                              objective.putInt("progress", currentDay);
                              questData.put("activeQuests", activeQuests);
                              NbtCompound spiritData = playerData.getCompound("smfs_spirit_data");
                              spiritData.put("questData", questData);
                              client.player.readNbt(playerData);
                              client.player.sendMessage(Text.literal("§a天数条件满足！当前世界天数为：" + currentDay + "/" + target + "，可以提交任务"), false);
                              return;
                           }
                        } else if (currentProgress != currentDay) {
                           objective.putInt("progress", currentDay);
                           questData.put("activeQuests", activeQuests);
                           NbtCompound spiritData = playerData.getCompound("smfs_spirit_data");
                           spiritData.put("questData", questData);
                           client.player.readNbt(playerData);
                           client.player.sendMessage(Text.literal("§a当前世界天数: " + currentDay + "/" + target), false);
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
