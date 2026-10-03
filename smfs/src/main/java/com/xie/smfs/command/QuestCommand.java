package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.api.IPlayerData;
import com.xie.smfs.manager.QuestManager;
import java.util.Map.Entry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class QuestCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                              "quest"
                           )
                           .then(CommandManager.literal("list").executes(QuestCommand::listQuests)))
                        .then(
                           CommandManager.literal("start")
                              .then(CommandManager.argument("questId", StringArgumentType.string()).executes(QuestCommand::startQuest))
                        ))
                     .then(
                        CommandManager.literal("abandon")
                           .then(CommandManager.argument("questId", StringArgumentType.string()).executes(QuestCommand::abandonQuest))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)CommandManager.literal("info").executes(QuestCommand::questInfoActive))
                        .then(CommandManager.argument("questId", StringArgumentType.string()).executes(QuestCommand::questInfo))
                  ))
               .then(
                  ((LiteralArgumentBuilder)CommandManager.literal("complete").executes(QuestCommand::completeActiveQuest))
                     .then(CommandManager.argument("questId", StringArgumentType.string()).executes(QuestCommand::completeQuest))
               ))
            .then(CommandManager.literal("reset").executes(QuestCommand::resetAllQuests))
      );
   }

   public static int listQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      boolean hasActiveQuests = false;
      boolean hasCompletedQuests = false;
      if (questData != null && questData.contains("activeQuests")) {
         NbtList activeList = questData.getList("activeQuests", 10);
         if (!activeList.isEmpty()) {
            hasActiveQuests = true;
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("=== 进行中的任务 ===").formatted(Formatting.GOLD), false);

            for (int i = 0; i < activeList.size(); i++) {
               NbtCompound questInfo = activeList.getCompound(i);
               String questId = questInfo.getString("id");
               String questName = questInfo.getString("name");
               ((ServerCommandSource)context.getSource())
                  .sendFeedback(() -> Text.literal("• " + questName + " (ID: " + questId + ")").formatted(Formatting.YELLOW), false);
            }
         }
      }

      if (questData != null && questData.contains("completedQuests")) {
         NbtList completedList = questData.getList("completedQuests", 8);
         if (!completedList.isEmpty()) {
            hasCompletedQuests = true;
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("=== 已完成的任务 ===").formatted(Formatting.GREEN), false);

            for (int i = 0; i < completedList.size(); i++) {
               String questId = completedList.getString(i);
               ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("• " + questId).formatted(Formatting.GREEN), false);
            }
         }
      }

      if (!hasActiveQuests && !hasCompletedQuests) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("暂无任务记录").formatted(Formatting.GRAY), false);
      }

      return 1;
   }

   public static int startQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      String questId = StringArgumentType.getString(context, "questId");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务不存在: " + questId).formatted(Formatting.RED), false);
         return 0;
      }

      IPlayerData playerData = (IPlayerData)player;
      if (playerData.hasCompletedQuest(questId)) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("你已经完成过这个任务了").formatted(Formatting.RED), false);
         return 0;
      }

      NbtCompound questData = playerData.getQuestData();
      if (questData.contains("activeQuests")) {
         NbtList activeList = questData.getList("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.getCompound(i);
            if (questInfo.getString("id").equals(questId)) {
               ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("你已经在进行这个任务了").formatted(Formatting.RED), false);
               return 0;
            }
         }
      }

      QuestManager.startQuest(player, questId);
      ((ServerCommandSource)context.getSource())
         .sendFeedback(() -> Text.literal("任务开始: " + QuestManager.getQuest(questId).get().getName()).formatted(Formatting.GREEN), false);
      return 1;
   }

   public static int abandonQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      String questId = StringArgumentType.getString(context, "questId");
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      boolean hasActiveQuest = false;
      if (questData.contains("activeQuests")) {
         NbtList activeList = questData.getList("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.getCompound(i);
            if (questInfo.getString("id").equals(questId)) {
               hasActiveQuest = true;
               break;
            }
         }
      }

      if (!hasActiveQuest) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("你没有在进行这个任务").formatted(Formatting.RED), false);
         return 0;
      } else {
         QuestManager.abandonQuest(player, questId);
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务已放弃").formatted(Formatting.YELLOW), false);
         return 1;
      }
   }

   public static int questInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      String questId = StringArgumentType.getString(context, "questId");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务不存在: " + questId).formatted(Formatting.RED), false);
         return 0;
      }

      QuestManager.QuestTemplate quest = QuestManager.getQuest(questId).get();
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("=== " + quest.getName() + " ===").formatted(Formatting.GOLD), false);
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("描述: " + quest.getDescription()).formatted(Formatting.WHITE), false);
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("目标:").formatted(Formatting.YELLOW), false);

      for (QuestManager.QuestObjective objective : quest.getObjectives()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("• " + objective.getDescription()).formatted(Formatting.GRAY), false);
      }

      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("奖励:").formatted(Formatting.GREEN), false);

      for (Entry<String, Object> entry : quest.getRewards().entrySet()) {
         String rewardDesc = getRewardDescription(entry.getKey(), entry.getValue());
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("• " + rewardDesc).formatted(Formatting.GREEN), false);
      }

      return 1;
   }

   private static String getRewardDescription(String rewardType, Object rewardValue) {
      switch (rewardType) {
         case "items":
            return "物品奖励: " + rewardValue.toString();
         case "experience":
            return "经验值: " + rewardValue + "点";
         case "money":
            return "金钱: " + rewardValue + "元";
         case "spirit_power":
            return "灵异力量: +" + rewardValue;
         default:
            return rewardType + ": " + rewardValue;
      }
   }

   public static int completeQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      String questId = StringArgumentType.getString(context, "questId");
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务不存在: " + questId).formatted(Formatting.RED), false);
         return 0;
      }

      boolean isActive = false;
      NbtCompound activeQuest = null;
      if (questData.contains("activeQuests")) {
         NbtList activeList = questData.getList("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.getCompound(i);
            if (questInfo.getString("id").equals(questId)) {
               isActive = true;
               activeQuest = questInfo;
               break;
            }
         }
      }

      if (!isActive) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("你没有在进行这个任务").formatted(Formatting.RED), false);
         return 0;
      }

      if (questData.contains("completedQuests")) {
         NbtList completedList = questData.getList("completedQuests", 8);

         for (int i = 0; i < completedList.size(); i++) {
            if (completedList.getString(i).equals(questId)) {
               ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("你已经完成过这个任务了").formatted(Formatting.RED), false);
               return 0;
            }
         }
      }

      try {
         activeQuest.putInt("status", 2);
         NbtList activeList = questData.getList("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.getCompound(i);
            if (questInfo.getString("id").equals(questId)) {
               activeList.remove(i);
               break;
            }
         }

         questData.put("activeQuests", activeList);
         NbtList completedList;
         if (questData.contains("completedQuests")) {
            completedList = questData.getList("completedQuests", 8);
         } else {
            completedList = new NbtList();
         }

         completedList.add(NbtString.of(questId));
         questData.put("completedQuests", completedList);
         QuestManager.saveQuestData(player, questData);
         QuestManager.giveQuestRewards(player, questId);
         QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
         if (template != null) {
            player.sendMessage(Text.literal("§6任务完成: " + template.title + " - 奖励已发放"), false);
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务完成: " + template.title).formatted(Formatting.GREEN), false);
         }

         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("完成任务时发生错误: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }

   public static int completeAllActiveQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!questData.contains("activeQuests")) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("没有进行中的任务").formatted(Formatting.YELLOW), false);
         return 0;
      }

      NbtList activeList = questData.getList("activeQuests", 10);
      if (activeList.isEmpty()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("没有进行中的任务").formatted(Formatting.YELLOW), false);
         return 0;
      }

      int completedCount = 0;

      for (int i = activeList.size() - 1; i >= 0; i--) {
         NbtCompound questInfo = activeList.getCompound(i);
         String questId = questInfo.getString("id");
         if (!QuestManager.getQuest(questId).isPresent()) {
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务不存在，跳过: " + questId).formatted(Formatting.RED), false);
         } else {
            if (questData.contains("completedQuests")) {
               NbtList completedList = questData.getList("completedQuests", 8);
               boolean alreadyCompleted = false;

               for (int j = 0; j < completedList.size(); j++) {
                  if (completedList.getString(j).equals(questId)) {
                     alreadyCompleted = true;
                     break;
                  }
               }

               if (alreadyCompleted) {
                  ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务已完成，跳过: " + questId).formatted(Formatting.YELLOW), false);
                  continue;
               }
            }

            try {
               questInfo.putInt("status", 2);
               activeList.remove(i);
               NbtList completedList;
               if (questData.contains("completedQuests")) {
                  completedList = questData.getList("completedQuests", 8);
               } else {
                  completedList = new NbtList();
               }

               completedList.add(NbtString.of(questId));
               questData.put("completedQuests", completedList);
               QuestManager.giveQuestRewards(player, questId);
               completedCount++;
               QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
               if (template != null) {
                  ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务完成: " + template.title).formatted(Formatting.GREEN), false);
               }
            } catch (Exception e) {
               ((ServerCommandSource)context.getSource())
                  .sendFeedback(() -> Text.literal("完成任务时发生错误: " + questId + " - " + e.getMessage()).formatted(Formatting.RED), false);
            }
         }
      }

      questData.put("activeQuests", activeList);
      QuestManager.saveQuestData(player, questData);
      int finalCompletedCount = completedCount;
      if (finalCompletedCount > 0) {
         ((ServerCommandSource)context.getSource())
            .sendFeedback(() -> Text.literal("§a成功完成 " + finalCompletedCount + " 个任务").formatted(Formatting.GREEN), false);
         player.sendMessage(Text.literal("§6所有进行中的任务已完成，奖励已发放"), false);
      } else {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("没有可完成的任务").formatted(Formatting.YELLOW), false);
      }

      return 1;
   }

   public static int resetAllQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      int activeCount = 0;
      int completedCount = 0;
      if (questData.contains("activeQuests")) {
         NbtList activeList = questData.getList("activeQuests", 10);
         activeCount = activeList.size();
      }

      if (questData.contains("completedQuests")) {
         NbtList completedList = questData.getList("completedQuests", 8);
         completedCount = completedList.size();
      }

      questData.put("activeQuests", new NbtList());
      questData.put("completedQuests", new NbtList());
      questData.remove("dailyQuests");
      questData.remove("lastDailyAssignDay");
      questData.remove("eventQuests");
      questData.remove("lastEventAssignDay");
      QuestManager.saveQuestData(player, questData);
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("§a所有任务已重置").formatted(Formatting.GREEN), false);
      int finalActiveCount = activeCount;
      int finalCompletedCount = completedCount;
      ((ServerCommandSource)context.getSource())
         .sendFeedback(() -> Text.literal("§7清除了 " + finalActiveCount + " 个进行中任务和 " + finalCompletedCount + " 个已完成任务").formatted(Formatting.GRAY), false);
      player.sendMessage(Text.literal("§a所有任务数据已重置，你可以重新开始任务了"), false);
      return 1;
   }

   public static int questInfoActive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!questData.contains("activeQuests")) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("没有进行中的任务").formatted(Formatting.YELLOW), false);
         return 0;
      }

      NbtList activeList = questData.getList("activeQuests", 10);
      if (activeList.isEmpty()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("没有进行中的任务").formatted(Formatting.YELLOW), false);
         return 0;
      }

      NbtCompound firstQuest = activeList.getCompound(0);
      String questId = firstQuest.getString("id");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务不存在: " + questId).formatted(Formatting.RED), false);
         return 0;
      }

      QuestManager.QuestTemplate quest = QuestManager.getQuest(questId).get();
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("=== " + quest.getName() + " ===").formatted(Formatting.GOLD), false);
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("描述: " + quest.getDescription()).formatted(Formatting.WHITE), false);
      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("目标:").formatted(Formatting.YELLOW), false);

      for (QuestManager.QuestObjective objective : quest.getObjectives()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("• " + objective.getDescription()).formatted(Formatting.GRAY), false);
      }

      ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("奖励:").formatted(Formatting.GREEN), false);

      for (Entry<String, Object> entry : quest.getRewards().entrySet()) {
         String rewardDesc = getRewardDescription(entry.getKey(), entry.getValue());
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("• " + rewardDesc).formatted(Formatting.GREEN), false);
      }

      if (firstQuest.contains("progress")) {
         NbtCompound progress = firstQuest.getCompound("progress");
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("当前进度:").formatted(Formatting.BLUE), false);

         for (String key : progress.getKeys()) {
            int current = progress.getInt(key);

            for (QuestManager.QuestObjective objective : quest.getObjectives()) {
               if (objective.getType().equals(key)) {
                  int target = objective.getTargetCount();
                  ((ServerCommandSource)context.getSource())
                     .sendFeedback(() -> Text.literal("• " + objective.getDescription() + ": " + current + "/" + target).formatted(Formatting.BLUE), false);
                  break;
               }
            }
         }
      }

      return 1;
   }

   public static int completeActiveQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!questData.contains("activeQuests")) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("没有进行中的任务").formatted(Formatting.YELLOW), false);
         return 0;
      }

      NbtList activeList = questData.getList("activeQuests", 10);
      if (activeList.isEmpty()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("没有进行中的任务").formatted(Formatting.YELLOW), false);
         return 0;
      }

      NbtCompound firstQuest = activeList.getCompound(0);
      String questId = firstQuest.getString("id");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务不存在: " + questId).formatted(Formatting.RED), false);
         return 0;
      }

      if (questData.contains("completedQuests")) {
         NbtList completedList = questData.getList("completedQuests", 8);

         for (int i = 0; i < completedList.size(); i++) {
            if (completedList.getString(i).equals(questId)) {
               ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("你已经完成过这个任务了").formatted(Formatting.RED), false);
               return 0;
            }
         }
      }

      try {
         firstQuest.putInt("status", 2);
         activeList.remove(0);
         questData.put("activeQuests", activeList);
         NbtList completedList;
         if (questData.contains("completedQuests")) {
            completedList = questData.getList("completedQuests", 8);
         } else {
            completedList = new NbtList();
         }

         completedList.add(NbtString.of(questId));
         questData.put("completedQuests", completedList);
         QuestManager.saveQuestData(player, questData);
         QuestManager.giveQuestRewards(player, questId);
         QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
         if (template != null) {
            player.sendMessage(Text.literal("§6任务完成: " + template.title + " - 奖励已发放"), false);
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("任务完成: " + template.title).formatted(Formatting.GREEN), false);
         }

         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("完成任务时发生错误: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }
}
