package com.xie.smfs.manager;

import com.xie.smfs.api.IPlayerData;
import com.xie.smfs.api.QuestAPI;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.network.ModNetwork;
import com.xie.smfs.network.packets.quests.s2c.QuestRewardPacket;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.GhostUtils;
import com.xie.smfs.util.InstantKillUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameterSet.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestManager");
   public static final String QUEST_TYPE_NEWBIE = "newbie";
   public static final String QUEST_TYPE_MAIN = "main";
   public static final String QUEST_TYPE_SIDE = "side";
   public static final String QUEST_TYPE_DAILY = "daily";
   public static final int QUEST_STATUS_NOT_STARTED = 0;
   public static final int QUEST_STATUS_IN_PROGRESS = 1;
   public static final int QUEST_STATUS_COMPLETED = 2;
   public static final int QUEST_STATUS_FAILED = 3;
   public static final Map<String, QuestManager.QuestTemplate> QUEST_TEMPLATES = new HashMap<>();

   private static void initializeQuestTemplates() {
      LOGGER.debug("开始初始化任务模板...");
      QUEST_TEMPLATES.putAll(QuestConfig.getAllQuestTemplates());
      loadAddonQuestTemplates();
      logQuestTemplates();
   }

   private static void loadAddonQuestTemplates() {
      try {
         Map<String, QuestManager.QuestTemplate> addonTemplates = QuestAPI.getAddonQuestTemplates();
         if (addonTemplates != null && !addonTemplates.isEmpty()) {
            QUEST_TEMPLATES.putAll(addonTemplates);
            LOGGER.debug("成功加载 {} 个附属模组任务模板", addonTemplates.size());
         } else {
            LOGGER.debug("未发现附属模组任务模板");
         }
      } catch (Exception e) {
         LOGGER.warn("加载附属模组任务模板时发生错误: {}", e.getMessage());
      }
   }

   public static boolean assignQuest(PlayerEntity player, String questId) {
      return assignQuest(player, questId, null);
   }

   public static boolean assignQuest(PlayerEntity player, String questId, String requiredGhostType) {
      QuestManager.QuestTemplate template = QUEST_TEMPLATES.get(questId);
      if (template == null) {
         return false;
      }

      NbtCompound questData = getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c已经领取过该任务，有进行中的该任务时，不支持重复领取"), false);
            }

            return false;
         }
      }

      if (!template.prerequisites.isEmpty()) {
         NbtList completedQuests = questData.method_10554("completedQuests", 10);
         boolean allPrerequisitesMet = true;

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
               allPrerequisitesMet = false;
               break;
            }
         }

         if (!allPrerequisitesMet) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c无法接受任务: 前置任务未完成"), false);
            }

            return false;
         }
      }

      NbtCompound newQuest = new NbtCompound();
      newQuest.method_10582("id", questId);
      newQuest.method_10582("title", template.title);
      newQuest.method_10582("description", template.description);
      newQuest.method_10582("type", template.type);
      newQuest.method_10556("isNewbieQuest", template.isNewbieQuest);
      newQuest.method_10569("status", 1);
      newQuest.method_10544("startTime", player.method_37908().method_8510());
      newQuest.method_10569("timeLimit", template.timeLimit);
      if (template.timeLimit > 0) {
         newQuest.method_10582("title", template.title.replace("（限时）", "（限时1h）"));
      }

      if (requiredGhostType != null && !requiredGhostType.isEmpty()) {
         newQuest.method_10582("requiredGhostType", requiredGhostType);
         String ghostDisplayName = getGhostDisplayName(requiredGhostType);
         newQuest.method_10582("description", "使用黄金容器关押一只" + ghostDisplayName + "。");
      }

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
      activeQuests.add(newQuest);
      questData.method_10566("activeQuests", activeQuests);
      saveQuestData(player, questData);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_7353(Text.method_43470("§a新任务开始: " + template.title), false);
      }

      return true;
   }

   public static void updateQuestProgress(PlayerEntity player, String questId, String objectiveId, int amount) {
      NbtCompound questData = getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      boolean updated = false;
      boolean taskCompleted = false;

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10550("status") == 1 && quest.method_10558("id").equals(questId)) {
            NbtList objectives = quest.method_10554("objectives", 10);

            for (int j = 0; j < objectives.size(); j++) {
               NbtCompound objective = objectives.method_10602(j);
               if (objective.method_10558("id").equals(objectiveId)) {
                  int currentProgress = objective.method_10550("progress");
                  int target = objective.method_10550("target");
                  int newProgress = Math.min(currentProgress + amount, target);
                  objective.method_10569("progress", newProgress);
                  updated = true;
                  if (newProgress >= target) {
                     taskCompleted = checkQuestCompletion(player, quest);
                  }
                  break;
               }
            }

            if (updated) {
               if (!taskCompleted) {
                  activeQuests.method_10606(i, quest);
                  questData.method_10566("activeQuests", activeQuests);
                  saveQuestData(player, questData);
               }
               break;
            }
         }
      }
   }

   public static boolean checkQuestCompletion(PlayerEntity player, NbtCompound quest) {
      if (quest.method_10550("status") == 2) {
         return true;
      }

      NbtList objectives = quest.method_10554("objectives", 10);
      boolean allCompleted = true;

      for (int i = 0; i < objectives.size(); i++) {
         NbtCompound objective = objectives.method_10602(i);
         int progress = objective.method_10550("progress");
         int target = objective.method_10550("target");
         if (progress < target) {
            allCompleted = false;
            break;
         }
      }

      if (allCompleted) {
         quest.method_10569("status", 2);
         quest.method_10544("completeTime", System.currentTimeMillis());
         giveQuestRewards(player, quest.method_10558("id"));
         NbtCompound questData = getQuestData(player);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);
         NbtList completedQuests = questData.method_10554("completedQuests", 10);

         for (int i = 0; i < activeQuests.size(); i++) {
            NbtCompound activeQuest = activeQuests.method_10602(i);
            if (activeQuest.method_10558("id").equals(quest.method_10558("id"))) {
               activeQuests.method_10536(i);
               break;
            }
         }

         completedQuests.add(quest);
         questData.method_10566("activeQuests", activeQuests);
         questData.method_10566("completedQuests", completedQuests);
         saveQuestData(player, questData);
         if ("newbie_craft_gold_container".equals(quest.method_10558("id"))) {
            LOGGER.debug("黄金容器任务已完成并添加到已完成列表，开始分配日常任务");
            DailyQuestManager.assignNewDailyQuests(player);
         }

         if ("main_tame_ghost".equals(quest.method_10558("id"))) {
            LOGGER.debug("驾驭鬼魂任务已完成并添加到已完成列表，开始分配事件任务");
            DailyQuestManager.assignNewEventQuests(player);
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§6任务完成: " + quest.method_10558("title") + "! 奖励已发放"), false);
            FactionManager.addReputation(serverPlayer, 500);
         }
      }

      return allCompleted;
   }

   public static void giveQuestRewards(PlayerEntity player, String questId) {
      QuestManager.QuestTemplate template = QUEST_TEMPLATES.get(questId);
      if (template == null) {
         LOGGER.warn("任务模板不存在: " + questId);
      } else {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            for (Entry<String, Object> entry : template.rewards.entrySet()) {
               String rewardType = entry.getKey();
               Object rewardValue = entry.getValue();
               switch (rewardType) {
                  case "loot_table":
                     String lootTableId = rewardValue.toString();
                     Identifier lootTable = Identifier.method_12829(lootTableId);
                     if (lootTable != null && serverPlayer.method_5682().method_3857() != null) {
                        LootContextParameterSet lootContext = new Builder((ServerWorld)player.method_37908())
                           .method_51871(player.method_7292())
                           .method_51875(LootContextTypes.field_1175);
                        serverPlayer.method_5682().method_3857().getLootTable(lootTable).method_51882(lootContext, serverPlayer.method_31548()::method_7398);
                        serverPlayer.method_7353(Text.method_43470("§6恭喜！你获得了卖货郎的奖励！"), false);
                        break;
                     }

                     LOGGER.warn("无效的战利品表: {}", lootTableId);
                     serverPlayer.method_7270(new ItemStack(Items.field_8695, 10));
                     serverPlayer.method_7353(Text.method_43470("§6恭喜！你获得了10个金锭！"), false);
                     break;
                  case "random_items":
                     String[] itemList = rewardValue.toString().split(",");
                     if (itemList.length > 0) {
                        Random random = new Random();
                        String randomItemId = itemList[random.nextInt(itemList.length)].trim();
                        LOGGER.debug("卖货郎任务随机奖励抽取: 从 {} 个物品中抽中 {}", itemList.length, randomItemId);
                        ItemStack rewardStack = createItemStack(randomItemId, 1);
                        if (rewardStack != null) {
                           serverPlayer.method_7270(rewardStack);
                           serverPlayer.method_7353(Text.method_43470("§6恭喜！你获得了" + getItemDisplayName(randomItemId) + "！"), false);
                           ModNetwork.sendToClient(new QuestRewardPacket(randomItemId, 1), serverPlayer);
                        } else {
                           LOGGER.warn("无法创建随机奖励物品: {}", randomItemId);
                           serverPlayer.method_7270(new ItemStack(Items.field_8695, 10));
                           serverPlayer.method_7353(Text.method_43470("§6恭喜！你获得了10个金锭！"), false);
                        }
                     } else {
                        LOGGER.warn("随机奖励列表为空，使用默认奖励");
                        serverPlayer.method_7270(new ItemStack(Items.field_8695, 10));
                        serverPlayer.method_7353(Text.method_43470("§6恭喜！你获得了10个金锭！"), false);
                     }
                     break;
                  case "items":
                     String[] items = rewardValue.toString().split(",");

                     for (String item : items) {
                        String[] parts = item.split(":");
                        if (parts.length == 2) {
                           String itemId = parts[0];
                           int count = Integer.parseInt(parts[1]);
                           ItemStack rewardStack = createItemStack(itemId, count);
                           if (rewardStack != null) {
                              serverPlayer.method_7270(rewardStack);
                              ModNetwork.sendToClient(new QuestRewardPacket(itemId, count), serverPlayer);
                           } else {
                              LOGGER.warn("无法创建奖励物品: {}", itemId);
                              serverPlayer.method_7270(new ItemStack(Items.field_8695, count));
                              ModNetwork.sendToClient(new QuestRewardPacket("gold_ingot", count), serverPlayer);
                           }
                        } else {
                           LOGGER.warn("物品奖励格式错误: {}", item);
                        }
                     }
                     break;
                  case "experience":
                     int exp = Integer.parseInt(rewardValue.toString());
                     serverPlayer.method_7255(exp);
                     break;
                  case "spirit_damage":
                     float damage = Float.parseFloat(rewardValue.toString());
                     serverPlayer.method_7353(Text.method_43470("§a灵异伤害增加了: " + new DecimalFormat("#.###").format(damage)), false);
                     break;
                  case "max_spirit":
                     float maxSpirit = Float.parseFloat(rewardValue.toString());
                     serverPlayer.method_7353(Text.method_43470("§a最大灵异值增加了: " + maxSpirit), false);
                     break;
                  default:
                     LOGGER.warn("未知的奖励类型: " + rewardType);
               }
            }
         } else {
            for (Entry<String, Object> entry : template.rewards.entrySet()) {
               String rewardType = entry.getKey();
               Object rewardValue = entry.getValue();
               switch (rewardType) {
                  case "random_items":
                     String[] itemList = rewardValue.toString().split(",");
                     if (itemList.length > 0) {
                        Random random = new Random();
                        String randomItemId = itemList[random.nextInt(itemList.length)].trim();
                        LOGGER.debug("卖货郎任务随机奖励抽取: 从 {} 个物品中抽中 {}", itemList.length, randomItemId);
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                           ItemStack rewardStack = createItemStack(randomItemId, 1);
                           if (rewardStack != null) {
                              if (serverPlayer.method_31548().method_7394(rewardStack)) {
                                 player.method_7353(Text.method_43470("§6恭喜！你获得了" + getItemDisplayName(randomItemId) + "！"), false);
                                 ModNetwork.sendToClient(new QuestRewardPacket(randomItemId, 1), serverPlayer);
                              } else {
                                 serverPlayer.method_7328(rewardStack, false);
                                 player.method_7353(Text.method_43470("§6背包已满，" + getItemDisplayName(randomItemId) + "已掉落在地！"), false);
                                 ModNetwork.sendToClient(new QuestRewardPacket(randomItemId, 1), serverPlayer);
                              }
                           } else {
                              LOGGER.warn("无法创建随机奖励物品: {}", randomItemId);
                              player.method_7353(Text.method_43470("§c随机奖励发放失败: 无法创建物品 " + randomItemId), false);
                              serverPlayer.method_7270(new ItemStack(Items.field_8695, 10));
                              player.method_7353(Text.method_43470("§6恭喜！你获得了10个金锭！"), false);
                              ModNetwork.sendToClient(new QuestRewardPacket("gold_ingot", 10), serverPlayer);
                           }
                        } else {
                           LOGGER.warn("无法识别的玩家类型: {}", player.getClass().getName());
                        }
                     } else {
                        LOGGER.warn("随机奖励列表为空，使用默认奖励");
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                           serverPlayer.method_7270(new ItemStack(Items.field_8695, 10));
                           player.method_7353(Text.method_43470("§6恭喜！你获得了10个金锭！"), false);
                        }
                     }
                     break;
                  case "items":
                     String[] items = rewardValue.toString().split(",");

                     for (String item : items) {
                        String[] parts = item.split(":");
                        if (parts.length == 2) {
                           String itemId = parts[0];
                           int count = Integer.parseInt(parts[1]);
                           if (player instanceof ServerPlayerEntity serverPlayer) {
                              ItemStack rewardStack = createItemStack(itemId, count);
                              if (rewardStack != null) {
                                 if (serverPlayer.method_31548().method_7394(rewardStack)) {
                                    player.method_7353(Text.method_43470("§a获得奖励: " + count + " 个 " + getItemDisplayName(itemId)), false);
                                    ModNetwork.sendToClient(new QuestRewardPacket(itemId, count), serverPlayer);
                                 } else {
                                    serverPlayer.method_7328(rewardStack, false);
                                    player.method_7353(Text.method_43470("§a背包已满，奖励已掉落在地: " + count + " 个 " + getItemDisplayName(itemId)), false);
                                    ModNetwork.sendToClient(new QuestRewardPacket(itemId, count), serverPlayer);
                                 }
                              } else {
                                 LOGGER.warn("无法创建奖励物品: {}", itemId);
                                 player.method_7353(Text.method_43470("§c奖励发放失败: 无法创建物品 " + itemId), false);
                                 serverPlayer.method_7270(new ItemStack(Items.field_8695, 10));
                                 player.method_7353(Text.method_43470("§6恭喜！你获得了10个金锭！"), false);
                                 ModNetwork.sendToClient(new QuestRewardPacket("gold_ingot", 10), serverPlayer);
                              }
                           } else {
                              LOGGER.warn("无法识别的玩家类型: {}", player.getClass().getName());
                           }
                        }
                     }
                     break;
                  case "experience":
                     int exp = Integer.parseInt(rewardValue.toString());
                     if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)player).method_7255(exp);
                        player.method_7353(Text.method_43470("§a获得经验值: " + exp), false);
                     } else {
                        player.method_7353(Text.method_43470("§a获得经验值: " + exp), false);
                     }
                     break;
                  default:
                     LOGGER.warn("客户端不支持此奖励类型: " + rewardType);
               }
            }
         }
      }
   }

   private static ItemStack createItemStack(String itemId, int count) {
      return GhostUtils.createItemStack(itemId, count);
   }

   private static String getItemDisplayName(String itemId) {
      return GhostUtils.getItemDisplayName(itemId);
   }

   public static NbtCompound getQuestData(PlayerEntity player) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      if (!playerData.method_10545("questData")) {
         NbtCompound newQuestData = new NbtCompound();
         newQuestData.method_10566("activeQuests", new NbtList());
         newQuestData.method_10566("completedQuests", new NbtList());
         playerData.method_10566("questData", newQuestData);
         PlayerEvents.saveDataToPlayer(player, playerData);
         return newQuestData;
      }

      NbtCompound questData = playerData.method_10562("questData");
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound var6 = activeQuests.method_10602(i);
      }

      for (int i = 0; i < completedQuests.size(); i++) {
         NbtCompound var9 = completedQuests.method_10602(i);
      }

      logPlayerQuestSummary(player, activeQuests, completedQuests);
      return questData;
   }

   public static void saveQuestData(PlayerEntity player, NbtCompound questData) {
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound var5 = activeQuests.method_10602(i);
      }

      NbtCompound playerData = PlayerEvents.getCachedData(player);
      playerData.method_10566("questData", questData.method_10553());
      PlayerEvents.saveDataToPlayer(player, playerData);
      LOGGER.debug("已保存玩家 {} 的任务数据，活跃任务: {}，已完成任务: {}", player.method_5477().getString(), activeQuests.size(), completedQuests.size());
   }

   private static void logPlayerQuestSummary(PlayerEntity player, NbtList activeQuests, NbtList completedQuests) {
      Map<String, Integer> activeByType = new HashMap<>();

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         String type = quest.method_10558("type");
         activeByType.put(type, activeByType.getOrDefault(type, 0) + 1);
      }

      Map<String, Integer> completedByType = new HashMap<>();

      for (int i = 0; i < completedQuests.size(); i++) {
         NbtCompound quest = completedQuests.method_10602(i);
         String type = quest.method_10558("type");
         completedByType.put(type, completedByType.getOrDefault(type, 0) + 1);
      }
   }

   private static void logQuestTemplates() {
   }

   public static List<NbtCompound> getActiveQuests(PlayerEntity player) {
      NbtCompound questData = getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      List<NbtCompound> result = new ArrayList<>();

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10550("status") == 1) {
            result.add(quest);
         }
      }

      return result;
   }

   public static boolean checkItemSubmission(PlayerEntity player, String questId, String itemId, int amount, boolean consume) {
      boolean success = checkQuestWithStrategy(player, questId, itemId, amount, consume);
      LOGGER.debug("策略检测结果 - 玩家: {}, 任务ID: {}, 物品ID: {}, 结果: {}", player.method_5477().getString(), questId, itemId, success);
      if (success) {
         LOGGER.debug("检测成功，开始检查任务完成状态 - 玩家: {}, 任务ID: {}", player.method_5477().getString(), questId);
         NbtCompound questData = getQuestData(player);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);
         LOGGER.debug("查找活跃任务 - 玩家: {}, 活跃任务数: {}", player.method_5477().getString(), activeQuests.size());
         boolean found = false;

         for (int i = 0; i < activeQuests.size(); i++) {
            NbtCompound quest = activeQuests.method_10602(i);
            if (quest.method_10558("id").equals(questId)) {
               LOGGER.debug("找到匹配任务 - 玩家: {}, 任务ID: {}, 任务状态: {}", player.method_5477().getString(), questId, quest.method_10550("status"));
               found = true;
               checkQuestCompletion(player, quest);
               LOGGER.debug("任务完成检查已调用 - 玩家: {}, 任务ID: {}", player.method_5477().getString(), questId);
               break;
            }
         }

         if (!found) {
            LOGGER.warn("未找到匹配的活跃任务 - 玩家: {}, 任务ID: {}", player.method_5477().getString(), questId);
         }
      }

      LOGGER.debug("物品提交检测完成 - 玩家: {}, 任务ID: {}, 最终结果: {}", player.method_5477().getString(), questId, success);
      return success;
   }

   public static boolean submitEventQuest(PlayerEntity player, String questId) {
      LOGGER.debug("玩家 {} 尝试提交事件任务: {}", player.method_5477().getString(), questId);
      QuestManager.QuestTemplate template = QUEST_TEMPLATES.get(questId);
      if (template == null) {
         LOGGER.warn("任务模板不存在: {}", questId);
         return false;
      }

      NbtCompound questData = getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtCompound targetQuest = null;

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId) && quest.method_10550("status") == 1) {
            targetQuest = quest;
            break;
         }
      }

      if (targetQuest == null) {
         LOGGER.warn("任务未在进行中或不存在: {}", questId);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c任务未在进行中或不存在"), false);
         }

         return false;
      } else {
         NbtList objectives = targetQuest.method_10554("objectives", 10);
         boolean allCompleted = true;

         for (int i = 0; i < objectives.size(); i++) {
            NbtCompound objective = objectives.method_10602(i);
            int progress = objective.method_10550("progress");
            int target = objective.method_10550("target");
            if (progress < target) {
               allCompleted = false;
               LOGGER.debug("目标未完成: {} - 进度: {}/{}", objective.method_10558("description"), progress, target);
               break;
            }
         }

         if (!allCompleted) {
            LOGGER.warn("任务目标未全部完成，无法提交: {}", questId);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c任务目标未全部完成，无法提交"), false);
            }

            return false;
         } else {
            for (int i = 0; i < objectives.size(); i++) {
               NbtCompound objective = objectives.method_10602(i);
               String objectiveId = objective.method_10558("id");
               if (objectiveId.endsWith("_event")) {
                  int target = objective.method_10550("target");
                  EventQuestStrategy strategy = new EventQuestStrategy();
                  boolean conditionMet = strategy.checkCondition(player, objectiveId, target);
                  if (!conditionMet) {
                     LOGGER.warn("事件任务条件不满足: questId={}, objectiveId={}", questId, objectiveId);
                     if (player instanceof ServerPlayerEntity serverPlayer) {
                        int currentProgress = strategy.getCurrentProgress(player, objectiveId);
                        String ghostType = getGhostTypeForEvent(objectiveId);
                        String ghostName = GhostUtils.getGhostDisplayName(ghostType);
                        if (currentProgress == 0) {
                           serverPlayer.method_7353(Text.method_43470("§c未找到包含§6" + ghostName + "§c的黄金容器"), false);
                           serverPlayer.method_7353(Text.method_43470("§7请确保背包中有包含对应鬼的黄金容器"), false);
                        } else {
                           serverPlayer.method_7353(Text.method_43470("§c黄金容器数量不足: §e" + currentProgress + "§c/§a" + target), false);
                           serverPlayer.method_7353(Text.method_43470("§7还需要§6" + (target - currentProgress) + "§7个包含§6" + ghostName + "§7的黄金容器"), false);
                        }
                     }

                     return false;
                  }

                  strategy.consumeItems(player, objectiveId, target);
                  LOGGER.debug("已消耗黄金容器: questId={}, objectiveId={}, amount={}", questId, objectiveId, target);
               }
            }

            return checkQuestCompletion(player, targetQuest);
         }
      }
   }

   private static boolean checkQuestWithStrategy(PlayerEntity player, String questId, String objectiveId, int amount, boolean consume) {
      String strategyType = QuestConfig.getStrategyTypeForObjective(objectiveId);
      LOGGER.debug("检测任务目标: questId={}, objectiveId={}, strategyType={}", questId, objectiveId, strategyType);
      switch (strategyType) {
         case "special":
            return checkSpecialObjective(player, questId, objectiveId, amount);
         case "day":
            return checkDayObjective(player, questId, objectiveId, amount);
         case "kill":
            return checkKillObjective(player, questId, objectiveId, amount);
         case "event":
            return checkEventObjective(player, questId, objectiveId, amount, consume);
         case "seller":
            return checkSellerObjective(player, questId, objectiveId, amount, consume);
         case "item":
         default:
            return checkItemObjective(player, questId, objectiveId, amount, consume);
      }
   }

   private static boolean checkSpecialObjective(PlayerEntity player, String questId, String objectiveId, int amount) {
      LOGGER.debug("检测特殊任务目标: questId={}, objectiveId={}", questId, objectiveId);
      SpecialQuestStrategy strategy = new SpecialQuestStrategy();
      boolean conditionMet = strategy.checkCondition(player, objectiveId, amount);
      if (conditionMet) {
         LOGGER.debug("特殊任务条件满足: questId={}, objectiveId={}", questId, objectiveId);
         updateQuestProgress(player, questId, objectiveId, amount);
         return true;
      } else {
         LOGGER.debug("特殊任务条件不满足: questId={}, objectiveId={}", questId, objectiveId);
         return false;
      }
   }

   private static boolean checkDayObjective(PlayerEntity player, String questId, String objectiveId, int amount) {
      LOGGER.debug("检测天数任务目标: questId={}, objectiveId={}", questId, objectiveId);
      if ("survive_5_days".equals(objectiveId)
         && player instanceof ServerPlayerEntity serverPlayer
         && serverPlayer.method_37908() instanceof ServerWorld serverWorld) {
         int currentDay = TutorialManager.calculateCurrentDay(serverWorld);
         if (currentDay >= amount) {
            updateQuestProgress(player, questId, objectiveId, amount);
            return true;
         }
      }

      return false;
   }

   private static boolean checkKillObjective(PlayerEntity player, String questId, String objectiveId, int amount) {
      LOGGER.debug("检测击杀任务目标: questId={}, objectiveId={}, amount={}", questId, objectiveId, amount);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         NbtCompound data = PlayerEvents.getCachedData(player);
         String progressKey = "quest_kill_progress_" + objectiveId;
         int currentProgress = 0;
         if (data.method_10545(progressKey)) {
            currentProgress = data.method_10550(progressKey);
         }

         LOGGER.debug("击杀任务进度: objectiveId={}, 当前进度={}, 目标进度={}", objectiveId, currentProgress, amount);
         LOGGER.debug("数据缓存检查: progressKey={}, 数据存在={}, 玩家UUID={}", progressKey, data.method_10545(progressKey), player.method_5667());
         if (currentProgress >= amount) {
            updateQuestProgress(player, questId, objectiveId, amount);
            data.method_10551(progressKey);
            PlayerEvents.saveDataToPlayer(player, data);
            LOGGER.debug("已清除击杀任务进度缓存: {}", progressKey);
            return true;
         } else {
            serverPlayer.method_7353(Text.method_43470("§c击杀进度不足: 当前 " + currentProgress + "/" + amount), false);
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean checkEventObjective(PlayerEntity player, String questId, String objectiveId, int amount, boolean consume) {
      LOGGER.debug("检测事件任务目标: questId={}, objectiveId={}, consume={}", questId, objectiveId, consume);
      EventQuestStrategy strategy = new EventQuestStrategy();
      boolean conditionMet = strategy.checkCondition(player, objectiveId, amount);
      if (conditionMet) {
         LOGGER.debug("事件任务条件满足: questId={}, objectiveId={}, 需要消耗物品={}", questId, objectiveId, consume);
         if (consume) {
            strategy.consumeItems(player, objectiveId, amount);
            updateQuestProgress(player, questId, "submit_" + objectiveId, amount);
         } else {
            updateQuestProgress(player, questId, "complete_" + objectiveId, amount);
         }

         return true;
      } else {
         LOGGER.debug("事件任务条件不满足: questId={}, objectiveId={}", questId, objectiveId);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            int currentProgress = strategy.getCurrentProgress(player, objectiveId);
            String ghostType = getGhostTypeForEvent(objectiveId);
            String ghostName = GhostUtils.getGhostDisplayName(ghostType);
            if (currentProgress == 0) {
               serverPlayer.method_7353(Text.method_43470("§c未找到包含§6" + ghostName + "§c的黄金容器"), false);
               serverPlayer.method_7353(Text.method_43470("§7请确保背包中有包含对应鬼的黄金容器"), false);
            } else {
               serverPlayer.method_7353(Text.method_43470("§c黄金容器数量不足: §e" + currentProgress + "§c/§a" + amount), false);
               serverPlayer.method_7353(Text.method_43470("§7还需要§6" + (amount - currentProgress) + "§7个包含§6" + ghostName + "§7的黄金容器"), false);
            }
         }

         return false;
      }
   }

   private static boolean checkSellerObjective(PlayerEntity player, String questId, String objectiveId, int amount, boolean consume) {
      LOGGER.debug("检测卖货郎任务目标: questId={}, objectiveId={}, consume={}", questId, objectiveId, consume);
      String requiredGhostType = null;
      NbtCompound questData = getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId)) {
            if (quest.method_10545("requiredGhostType")) {
               requiredGhostType = quest.method_10558("requiredGhostType");
            }
            break;
         }
      }

      SellerQuestStrategy strategy = new SellerQuestStrategy();
      strategy.setRequiredGhostType(requiredGhostType);
      boolean conditionMet = strategy.checkCondition(player, objectiveId, amount);
      if (conditionMet) {
         LOGGER.debug("卖货郎任务条件满足: questId={}, objectiveId={}", questId, objectiveId);
         if (consume) {
            strategy.consumeItems(player, objectiveId, amount);
            updateQuestProgress(player, questId, objectiveId, amount);
         } else {
            updateQuestProgress(player, questId, objectiveId, amount);
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§a卖货郎任务条件满足！"), false);
         }

         return true;
      } else {
         LOGGER.debug("卖货郎任务条件不满足: questId={}, objectiveId={}", questId, objectiveId);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            String itemName = getSellerItemDisplayName(objectiveId);
            if (requiredGhostType != null && !requiredGhostType.isEmpty()) {
               itemName = "关押" + getGhostDisplayName(requiredGhostType) + "的沉重黄金容器";
            }

            serverPlayer.method_7353(Text.method_43470("§c卖货郎任务条件不满足: 需要§6" + amount + "§c个§6" + itemName), false);
         }

         return false;
      }
   }

   private static String getSellerItemDisplayName(String objectiveId) {
      switch (objectiveId) {
         case "collect_heavy_gold_container":
            return "沉重黄金容器";
         default:
            return "任务物品";
      }
   }

   private static boolean checkItemObjective(PlayerEntity player, String questId, String objectiveId, int amount, boolean consume) {
      LOGGER.debug("检测物品收集任务目标: questId={}, objectiveId={}, consume={}", questId, objectiveId, consume);
      String itemId = QuestConfig.getItemIdForObjective(objectiveId);
      boolean shouldConsume = determineShouldConsumeItems(questId, objectiveId, consume);
      LOGGER.debug("物品映射配置: objectiveId={} -> itemId={}, shouldConsume={}", objectiveId, itemId, shouldConsume);
      int itemCount = 0;
      PlayerInventory inventory = player.method_31548();

      for (int i = 0; i < inventory.method_5439(); i++) {
         ItemStack stack = inventory.method_5438(i);
         if (!stack.method_7960() && isItemMatch(stack, itemId)) {
            itemCount += stack.method_7947();
            LOGGER.debug("找到匹配物品: {} x{}", stack.method_7909().method_7876(), stack.method_7947());
         }
      }

      if (itemCount >= amount) {
         if (shouldConsume) {
            consumeItemsFromInventory(player, itemId, amount);
            updateQuestProgress(player, questId, objectiveId, amount);
         } else {
            updateQuestProgress(player, questId, objectiveId, amount);
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean determineShouldConsumeItems(String questId, String objectiveId, boolean consumeParam) {
      if (consumeParam) {
         return true;
      }

      QuestManager.QuestTemplate template = QUEST_TEMPLATES.get(questId);
      if (template != null) {
         switch (template.type) {
            case "daily":
               return QuestConfig.shouldConsumeItemsForObjective(objectiveId);
            case "newbie":
            case "main":
            case "side":
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isItemMatch(ItemStack stack, String targetItemId) {
      return isItemMatchPrivate(stack, targetItemId);
   }

   private static boolean isItemMatchPrivate(ItemStack stack, String targetItemId) {
      if (stack.method_7960()) {
         return false;
      } else {
         String stackItemKey = stack.method_7909().method_7876();
         String stackItemId = Registries.field_41178.method_10221(stack.method_7909()).toString();
         if ("corpse_piece".equals(targetItemId) && stack.method_7909() == ModItems.CORPSE_PIECE) {
            return true;
         } else if ("corpse_oil".equals(targetItemId) && stack.method_7909() == ModItems.CORPSE_OIL) {
            return true;
         } else if ("gold_block".equals(targetItemId) && stack.method_7909() == Items.field_8494) {
            return true;
         } else if ("oak_log".equals(targetItemId) && stack.method_7909() == Items.field_8583) {
            return true;
         } else if ("stone".equals(targetItemId) && stack.method_7909() == Items.field_20391) {
            return true;
         } else if ("cobblestone".equals(targetItemId) && stack.method_7909() == Items.field_20412) {
            return true;
         } else if ("iron_ingot".equals(targetItemId) && stack.method_7909() == Items.field_8620) {
            return true;
         } else if ("gold_ingot".equals(targetItemId) && stack.method_7909() == Items.field_8695) {
            return true;
         } else if ("golden_container".equals(targetItemId) && stack.method_7909() == ModItems.GOLDEN_CONTAINER) {
            return !stack.method_7985() || !stack.method_7969().method_10577("IsHeavy");
         } else {
            return "heavy_golden_container".equals(targetItemId) && stack.method_7909() == ModItems.GOLDEN_CONTAINER
               ? stack.method_7985() && stack.method_7969().method_10577("IsHeavy")
               : stackItemKey.equals("item.smfs." + targetItemId)
                  || stackItemId.equals("smfs:" + targetItemId)
                  || stackItemKey.equals("item.minecraft." + targetItemId)
                  || stackItemId.equals("minecraft:" + targetItemId)
                  || stackItemKey.endsWith("." + targetItemId)
                  || stackItemId.endsWith(":" + targetItemId);
         }
      }
   }

   private static void consumeItemsFromInventory(PlayerEntity player, String itemId, int amount) {
      PlayerInventory inventory = player.method_31548();
      int remaining = amount;

      for (int i = 0; i < inventory.method_5439() && remaining > 0; i++) {
         ItemStack stack = inventory.method_5438(i);
         if (!stack.method_7960() && isItemMatch(stack, itemId)) {
            int toRemove = Math.min(stack.method_7947(), remaining);
            stack.method_7934(toRemove);
            remaining -= toRemove;
            LOGGER.debug("消耗物品: {} x{}", stack.method_7909().method_7876(), toRemove);
         }
      }
   }

   public static Optional<QuestManager.QuestTemplate> getQuest(String questId) {
      return Optional.ofNullable(QUEST_TEMPLATES.get(questId));
   }

   public static void startQuest(PlayerEntity player, String questId) {
      assignQuest(player, questId);
   }

   public static void updateWorldDaysProgress(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer && serverPlayer.method_37908() instanceof ServerWorld serverWorld) {
         int currentDay = TutorialManager.calculateCurrentDay(serverWorld);
         NbtCompound questData = getQuestData(player);
         NbtList activeQuests = questData.method_10554("activeQuests", 10);

         for (int i = 0; i < activeQuests.size(); i++) {
            NbtCompound quest = activeQuests.method_10602(i);
            if (quest.method_10550("status") == 1) {
               String questId = quest.method_10558("id");
               if ("newbie_world_days".equals(questId)) {
                  NbtList objectives = quest.method_10554("objectives", 10);

                  for (int j = 0; j < objectives.size(); j++) {
                     NbtCompound objective = objectives.method_10602(j);
                     String objectiveId = objective.method_10558("id");
                     if ("survive_5_days".equals(objectiveId)) {
                        int target = objective.method_10550("target");
                        int currentProgress = objective.method_10550("progress");
                        if (currentDay > currentProgress && currentDay <= target) {
                           objective.method_10569("progress", currentDay);
                           objectives.method_10606(j, objective);
                           quest.method_10566("objectives", objectives);
                           activeQuests.method_10606(i, quest);
                        }
                        break;
                     }
                  }
               }
            }
         }

         questData.method_10566("activeQuests", activeQuests);
         saveQuestData(player, questData);
      }
   }

   public static void abandonQuest(PlayerEntity player, String questId) {
      NbtCompound questData = getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId) && quest.method_10550("status") == 1) {
            activeQuests.method_10536(i);
            LOGGER.debug("玩家 {} 放弃任务: {}", player.method_5477().getString(), quest.method_10558("title"));
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§e任务已放弃: " + quest.method_10558("title")), false);
            }

            questData.method_10566("activeQuests", activeQuests);
            saveQuestData(player, questData);
            break;
         }
      }
   }

   public static void failQuest(PlayerEntity player, String questId) {
      NbtCompound questData = getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (int i = 0; i < activeQuests.size(); i++) {
         NbtCompound quest = activeQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId) && quest.method_10550("status") == 1) {
            NbtCompound failedQuest = quest.method_10553();
            failedQuest.method_10569("status", 3);
            failedQuest.method_10544("failTime", System.currentTimeMillis());
            completedQuests.add(failedQuest);
            activeQuests.method_10536(i);
            LOGGER.debug("玩家 {} 任务失败: {}", player.method_5477().getString(), quest.method_10558("title"));
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c任务失败: " + quest.method_10558("title")), false);
               if ("seller_basic_collection".equals(questId)) {
                  serverPlayer.method_7353(Text.method_43470("§4由于任务超时，你受到了卖货郎的致命袭击！"), false);
                  InstantKillUtil.executePlayerSelfKill(serverPlayer);
                  LOGGER.debug("玩家 {} 因卖货郎任务超时受到致命惩罚", player.method_5477().getString());
               }
            }

            questData.method_10566("activeQuests", activeQuests);
            questData.method_10566("completedQuests", completedQuests);
            saveQuestData(player, questData);
            break;
         }
      }
   }

   private static String getGhostTypeForEvent(String objectiveId) {
      switch (objectiveId) {
         case "ghost_knock_event":
            return "qiaomen_ghost";
         case "ghost_look_up_event":
            return "taitou_ghost";
         case "ghost_look_down_event":
            return "ditou_ghost";
         case "invisible_event":
            return "death_sight_ghost";
         case "dense_fog_event":
            return "fog_ghost";
         default:
            return "未知鬼类型";
      }
   }

   public static void updateClientQuestData(NbtCompound updatedQuestData) {
      if (updatedQuestData == null) {
         LOGGER.warn("updateClientQuestData: 更新数据为null，无法更新任务数据");
      } else {
         try {
            MinecraftClient client = MinecraftClient.method_1551();
            if (client.field_1724 == null) {
               LOGGER.warn("updateClientQuestData: 客户端玩家为null，无法更新任务数据");
               return;
            }

            IPlayerData playerData = (IPlayerData)client.field_1724;
            playerData.setQuestData(updatedQuestData);
            LOGGER.debug(
               "客户端任务数据已更新，活跃任务数: {}, 已完成任务数: {}",
               updatedQuestData.method_10554("activeQuests", 10).size(),
               updatedQuestData.method_10554("completedQuests", 10).size()
            );
         } catch (Exception e) {
            LOGGER.error("更新客户端任务数据时发生错误: {}", e.getMessage());
         }
      }
   }

   public static void updateClientQuestData(PlayerEntity player, NbtCompound updatedQuestData) {
      updateClientQuestData(updatedQuestData);
   }

   private static String getGhostDisplayName(String ghostType) {
      return GhostUtils.getGhostDisplayName(ghostType);
   }

   public static String getRandomGhostTypeForSellerQuest() {
      List<String> ghostTypes = GhostUtils.getAllGhostEntityTypes();
      ghostTypes.removeIf("xinkai_ghost"::equals);
      ghostTypes.removeIf("ghost_fire"::equals);
      if (ghostTypes.isEmpty()) {
         return null;
      }

      Random random = new Random();
      return ghostTypes.get(random.nextInt(ghostTypes.size()));
   }

   static {
      initializeQuestTemplates();
   }

   public static class QuestObjective {
      public final String id;
      public final String description;
      public final int target;

      public QuestObjective(String id, String description, int target) {
         this.id = id;
         this.description = description;
         this.target = target;
      }

      public String getDescription() {
         return this.description;
      }

      public String getType() {
         return this.id;
      }

      public int getTargetCount() {
         return this.target;
      }
   }

   public static class QuestTemplate {
      public final String id;
      public final String title;
      public final String description;
      public final String type;
      public final List<QuestManager.QuestObjective> objectives;
      public final Map<String, Object> rewards;
      public final List<String> prerequisites;
      public final boolean isNewbieQuest;
      public final int newbieOrder;
      public final int timeLimit;

      public QuestTemplate(
         String id,
         String title,
         String description,
         String type,
         List<QuestManager.QuestObjective> objectives,
         Map<String, Object> rewards,
         List<String> prerequisites,
         boolean isNewbieQuest,
         int newbieOrder
      ) {
         this(id, title, description, type, objectives, rewards, prerequisites, isNewbieQuest, newbieOrder, 0);
      }

      public QuestTemplate(
         String id,
         String title,
         String description,
         String type,
         List<QuestManager.QuestObjective> objectives,
         Map<String, Object> rewards,
         List<String> prerequisites,
         boolean isNewbieQuest,
         int newbieOrder,
         int timeLimit
      ) {
         this.id = id;
         this.title = title;
         this.description = description;
         this.type = type;
         this.objectives = objectives;
         this.rewards = rewards;
         this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
         this.isNewbieQuest = isNewbieQuest;
         this.newbieOrder = newbieOrder;
         this.timeLimit = timeLimit;
      }

      public String getName() {
         return this.title;
      }

      public String getTitle() {
         return this.title;
      }

      public String getId() {
         return this.id;
      }

      public String getDescription() {
         return this.description;
      }

      public String getType() {
         return this.type;
      }

      public List<QuestManager.QuestObjective> getObjectives() {
         return this.objectives;
      }

      public Map<String, Object> getRewards() {
         return this.rewards;
      }

      public List<String> getPrerequisites() {
         return this.prerequisites;
      }

      public boolean isNewbieQuest() {
         return this.isNewbieQuest;
      }

      public int getNewbieOrder() {
         return this.newbieOrder;
      }
   }
}
