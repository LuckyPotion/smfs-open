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
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                              "quest"
                           )
                           .then(CommandManager.method_9247("list").executes(QuestCommand::listQuests)))
                        .then(
                           CommandManager.method_9247("start")
                              .then(CommandManager.method_9244("questId", StringArgumentType.string()).executes(QuestCommand::startQuest))
                        ))
                     .then(
                        CommandManager.method_9247("abandon")
                           .then(CommandManager.method_9244("questId", StringArgumentType.string()).executes(QuestCommand::abandonQuest))
                     ))
                  .then(
                     ((LiteralArgumentBuilder)CommandManager.method_9247("info").executes(QuestCommand::questInfoActive))
                        .then(CommandManager.method_9244("questId", StringArgumentType.string()).executes(QuestCommand::questInfo))
                  ))
               .then(
                  ((LiteralArgumentBuilder)CommandManager.method_9247("complete").executes(QuestCommand::completeActiveQuest))
                     .then(CommandManager.method_9244("questId", StringArgumentType.string()).executes(QuestCommand::completeQuest))
               ))
            .then(CommandManager.method_9247("reset").executes(QuestCommand::resetAllQuests))
      );
   }

   public static int listQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      boolean hasActiveQuests = false;
      boolean hasCompletedQuests = false;
      if (questData != null && questData.method_10545("activeQuests")) {
         NbtList activeList = questData.method_10554("activeQuests", 10);
         if (!activeList.isEmpty()) {
            hasActiveQuests = true;
            ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("=== 进行中的任务 ===").method_27692(Formatting.field_1065), false);

            for (int i = 0; i < activeList.size(); i++) {
               NbtCompound questInfo = activeList.method_10602(i);
               String questId = questInfo.method_10558("id");
               String questName = questInfo.method_10558("name");
               ((ServerCommandSource)context.getSource())
                  .method_9226(() -> Text.method_43470("• " + questName + " (ID: " + questId + ")").method_27692(Formatting.field_1054), false);
            }
         }
      }

      if (questData != null && questData.method_10545("completedQuests")) {
         NbtList completedList = questData.method_10554("completedQuests", 8);
         if (!completedList.isEmpty()) {
            hasCompletedQuests = true;
            ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("=== 已完成的任务 ===").method_27692(Formatting.field_1060), false);

            for (int i = 0; i < completedList.size(); i++) {
               String questId = completedList.method_10608(i);
               ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("• " + questId).method_27692(Formatting.field_1060), false);
            }
         }
      }

      if (!hasActiveQuests && !hasCompletedQuests) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("暂无任务记录").method_27692(Formatting.field_1080), false);
      }

      return 1;
   }

   public static int startQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      String questId = StringArgumentType.getString(context, "questId");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("任务不存在: " + questId).method_27692(Formatting.field_1061), false);
         return 0;
      }

      IPlayerData playerData = (IPlayerData)player;
      if (playerData.hasCompletedQuest(questId)) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("你已经完成过这个任务了").method_27692(Formatting.field_1061), false);
         return 0;
      }

      NbtCompound questData = playerData.getQuestData();
      if (questData.method_10545("activeQuests")) {
         NbtList activeList = questData.method_10554("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.method_10602(i);
            if (questInfo.method_10558("id").equals(questId)) {
               ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("你已经在进行这个任务了").method_27692(Formatting.field_1061), false);
               return 0;
            }
         }
      }

      QuestManager.startQuest(player, questId);
      ((ServerCommandSource)context.getSource())
         .method_9226(() -> Text.method_43470("任务开始: " + QuestManager.getQuest(questId).get().getName()).method_27692(Formatting.field_1060), false);
      return 1;
   }

   public static int abandonQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      String questId = StringArgumentType.getString(context, "questId");
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      boolean hasActiveQuest = false;
      if (questData.method_10545("activeQuests")) {
         NbtList activeList = questData.method_10554("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.method_10602(i);
            if (questInfo.method_10558("id").equals(questId)) {
               hasActiveQuest = true;
               break;
            }
         }
      }

      if (!hasActiveQuest) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("你没有在进行这个任务").method_27692(Formatting.field_1061), false);
         return 0;
      } else {
         QuestManager.abandonQuest(player, questId);
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("任务已放弃").method_27692(Formatting.field_1054), false);
         return 1;
      }
   }

   public static int questInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      String questId = StringArgumentType.getString(context, "questId");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("任务不存在: " + questId).method_27692(Formatting.field_1061), false);
         return 0;
      }

      QuestManager.QuestTemplate quest = QuestManager.getQuest(questId).get();
      ((ServerCommandSource)context.getSource())
         .method_9226(() -> Text.method_43470("=== " + quest.getName() + " ===").method_27692(Formatting.field_1065), false);
      ((ServerCommandSource)context.getSource())
         .method_9226(() -> Text.method_43470("描述: " + quest.getDescription()).method_27692(Formatting.field_1068), false);
      ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("目标:").method_27692(Formatting.field_1054), false);

      for (QuestManager.QuestObjective objective : quest.getObjectives()) {
         ((ServerCommandSource)context.getSource())
            .method_9226(() -> Text.method_43470("• " + objective.getDescription()).method_27692(Formatting.field_1080), false);
      }

      ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("奖励:").method_27692(Formatting.field_1060), false);

      for (Entry<String, Object> entry : quest.getRewards().entrySet()) {
         String rewardDesc = getRewardDescription(entry.getKey(), entry.getValue());
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("• " + rewardDesc).method_27692(Formatting.field_1060), false);
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
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      String questId = StringArgumentType.getString(context, "questId");
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("任务不存在: " + questId).method_27692(Formatting.field_1061), false);
         return 0;
      }

      boolean isActive = false;
      NbtCompound activeQuest = null;
      if (questData.method_10545("activeQuests")) {
         NbtList activeList = questData.method_10554("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.method_10602(i);
            if (questInfo.method_10558("id").equals(questId)) {
               isActive = true;
               activeQuest = questInfo;
               break;
            }
         }
      }

      if (!isActive) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("你没有在进行这个任务").method_27692(Formatting.field_1061), false);
         return 0;
      }

      if (questData.method_10545("completedQuests")) {
         NbtList completedList = questData.method_10554("completedQuests", 8);

         for (int i = 0; i < completedList.size(); i++) {
            if (completedList.method_10608(i).equals(questId)) {
               ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("你已经完成过这个任务了").method_27692(Formatting.field_1061), false);
               return 0;
            }
         }
      }

      try {
         activeQuest.method_10569("status", 2);
         NbtList activeList = questData.method_10554("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            NbtCompound questInfo = activeList.method_10602(i);
            if (questInfo.method_10558("id").equals(questId)) {
               activeList.method_10536(i);
               break;
            }
         }

         questData.method_10566("activeQuests", activeList);
         NbtList completedList;
         if (questData.method_10545("completedQuests")) {
            completedList = questData.method_10554("completedQuests", 8);
         } else {
            completedList = new NbtList();
         }

         completedList.add(NbtString.method_23256(questId));
         questData.method_10566("completedQuests", completedList);
         QuestManager.saveQuestData(player, questData);
         QuestManager.giveQuestRewards(player, questId);
         QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
         if (template != null) {
            player.method_7353(Text.method_43470("§6任务完成: " + template.title + " - 奖励已发放"), false);
            ((ServerCommandSource)context.getSource())
               .method_9226(() -> Text.method_43470("任务完成: " + template.title).method_27692(Formatting.field_1060), false);
         }

         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource())
            .method_9226(() -> Text.method_43470("完成任务时发生错误: " + e.getMessage()).method_27692(Formatting.field_1061), false);
         return 0;
      }
   }

   public static int completeAllActiveQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!questData.method_10545("activeQuests")) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("没有进行中的任务").method_27692(Formatting.field_1054), false);
         return 0;
      }

      NbtList activeList = questData.method_10554("activeQuests", 10);
      if (activeList.isEmpty()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("没有进行中的任务").method_27692(Formatting.field_1054), false);
         return 0;
      }

      int completedCount = 0;

      for (int i = activeList.size() - 1; i >= 0; i--) {
         NbtCompound questInfo = activeList.method_10602(i);
         String questId = questInfo.method_10558("id");
         if (!QuestManager.getQuest(questId).isPresent()) {
            ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("任务不存在，跳过: " + questId).method_27692(Formatting.field_1061), false);
         } else {
            if (questData.method_10545("completedQuests")) {
               NbtList completedList = questData.method_10554("completedQuests", 8);
               boolean alreadyCompleted = false;

               for (int j = 0; j < completedList.size(); j++) {
                  if (completedList.method_10608(j).equals(questId)) {
                     alreadyCompleted = true;
                     break;
                  }
               }

               if (alreadyCompleted) {
                  ((ServerCommandSource)context.getSource())
                     .method_9226(() -> Text.method_43470("任务已完成，跳过: " + questId).method_27692(Formatting.field_1054), false);
                  continue;
               }
            }

            try {
               questInfo.method_10569("status", 2);
               activeList.method_10536(i);
               NbtList completedList;
               if (questData.method_10545("completedQuests")) {
                  completedList = questData.method_10554("completedQuests", 8);
               } else {
                  completedList = new NbtList();
               }

               completedList.add(NbtString.method_23256(questId));
               questData.method_10566("completedQuests", completedList);
               QuestManager.giveQuestRewards(player, questId);
               completedCount++;
               QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
               if (template != null) {
                  ((ServerCommandSource)context.getSource())
                     .method_9226(() -> Text.method_43470("任务完成: " + template.title).method_27692(Formatting.field_1060), false);
               }
            } catch (Exception e) {
               ((ServerCommandSource)context.getSource())
                  .method_9226(() -> Text.method_43470("完成任务时发生错误: " + questId + " - " + e.getMessage()).method_27692(Formatting.field_1061), false);
            }
         }
      }

      questData.method_10566("activeQuests", activeList);
      QuestManager.saveQuestData(player, questData);
      int finalCompletedCount = completedCount;
      if (finalCompletedCount > 0) {
         ((ServerCommandSource)context.getSource())
            .method_9226(() -> Text.method_43470("§a成功完成 " + finalCompletedCount + " 个任务").method_27692(Formatting.field_1060), false);
         player.method_7353(Text.method_43470("§6所有进行中的任务已完成，奖励已发放"), false);
      } else {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("没有可完成的任务").method_27692(Formatting.field_1054), false);
      }

      return 1;
   }

   public static int resetAllQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      int activeCount = 0;
      int completedCount = 0;
      if (questData.method_10545("activeQuests")) {
         NbtList activeList = questData.method_10554("activeQuests", 10);
         activeCount = activeList.size();
      }

      if (questData.method_10545("completedQuests")) {
         NbtList completedList = questData.method_10554("completedQuests", 8);
         completedCount = completedList.size();
      }

      questData.method_10566("activeQuests", new NbtList());
      questData.method_10566("completedQuests", new NbtList());
      questData.method_10551("dailyQuests");
      questData.method_10551("lastDailyAssignDay");
      questData.method_10551("eventQuests");
      questData.method_10551("lastEventAssignDay");
      QuestManager.saveQuestData(player, questData);
      ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("§a所有任务已重置").method_27692(Formatting.field_1060), false);
      int finalActiveCount = activeCount;
      int finalCompletedCount = completedCount;
      ((ServerCommandSource)context.getSource())
         .method_9226(
            () -> Text.method_43470("§7清除了 " + finalActiveCount + " 个进行中任务和 " + finalCompletedCount + " 个已完成任务").method_27692(Formatting.field_1080), false
         );
      player.method_7353(Text.method_43470("§a所有任务数据已重置，你可以重新开始任务了"), false);
      return 1;
   }

   public static int questInfoActive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!questData.method_10545("activeQuests")) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("没有进行中的任务").method_27692(Formatting.field_1054), false);
         return 0;
      }

      NbtList activeList = questData.method_10554("activeQuests", 10);
      if (activeList.isEmpty()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("没有进行中的任务").method_27692(Formatting.field_1054), false);
         return 0;
      }

      NbtCompound firstQuest = activeList.method_10602(0);
      String questId = firstQuest.method_10558("id");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("任务不存在: " + questId).method_27692(Formatting.field_1061), false);
         return 0;
      }

      QuestManager.QuestTemplate quest = QuestManager.getQuest(questId).get();
      ((ServerCommandSource)context.getSource())
         .method_9226(() -> Text.method_43470("=== " + quest.getName() + " ===").method_27692(Formatting.field_1065), false);
      ((ServerCommandSource)context.getSource())
         .method_9226(() -> Text.method_43470("描述: " + quest.getDescription()).method_27692(Formatting.field_1068), false);
      ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("目标:").method_27692(Formatting.field_1054), false);

      for (QuestManager.QuestObjective objective : quest.getObjectives()) {
         ((ServerCommandSource)context.getSource())
            .method_9226(() -> Text.method_43470("• " + objective.getDescription()).method_27692(Formatting.field_1080), false);
      }

      ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("奖励:").method_27692(Formatting.field_1060), false);

      for (Entry<String, Object> entry : quest.getRewards().entrySet()) {
         String rewardDesc = getRewardDescription(entry.getKey(), entry.getValue());
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("• " + rewardDesc).method_27692(Formatting.field_1060), false);
      }

      if (firstQuest.method_10545("progress")) {
         NbtCompound progress = firstQuest.method_10562("progress");
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("当前进度:").method_27692(Formatting.field_1078), false);

         for (String key : progress.method_10541()) {
            int current = progress.method_10550(key);

            for (QuestManager.QuestObjective objective : quest.getObjectives()) {
               if (objective.getType().equals(key)) {
                  int target = objective.getTargetCount();
                  ((ServerCommandSource)context.getSource())
                     .method_9226(
                        () -> Text.method_43470("• " + objective.getDescription() + ": " + current + "/" + target).method_27692(Formatting.field_1078), false
                     );
                  break;
               }
            }
         }
      }

      return 1;
   }

   public static int completeActiveQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      IPlayerData playerData = (IPlayerData)player;
      NbtCompound questData = playerData.getQuestData();
      if (!questData.method_10545("activeQuests")) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("没有进行中的任务").method_27692(Formatting.field_1054), false);
         return 0;
      }

      NbtList activeList = questData.method_10554("activeQuests", 10);
      if (activeList.isEmpty()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("没有进行中的任务").method_27692(Formatting.field_1054), false);
         return 0;
      }

      NbtCompound firstQuest = activeList.method_10602(0);
      String questId = firstQuest.method_10558("id");
      if (!QuestManager.getQuest(questId).isPresent()) {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("任务不存在: " + questId).method_27692(Formatting.field_1061), false);
         return 0;
      }

      if (questData.method_10545("completedQuests")) {
         NbtList completedList = questData.method_10554("completedQuests", 8);

         for (int i = 0; i < completedList.size(); i++) {
            if (completedList.method_10608(i).equals(questId)) {
               ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("你已经完成过这个任务了").method_27692(Formatting.field_1061), false);
               return 0;
            }
         }
      }

      try {
         firstQuest.method_10569("status", 2);
         activeList.method_10536(0);
         questData.method_10566("activeQuests", activeList);
         NbtList completedList;
         if (questData.method_10545("completedQuests")) {
            completedList = questData.method_10554("completedQuests", 8);
         } else {
            completedList = new NbtList();
         }

         completedList.add(NbtString.method_23256(questId));
         questData.method_10566("completedQuests", completedList);
         QuestManager.saveQuestData(player, questData);
         QuestManager.giveQuestRewards(player, questId);
         QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
         if (template != null) {
            player.method_7353(Text.method_43470("§6任务完成: " + template.title + " - 奖励已发放"), false);
            ((ServerCommandSource)context.getSource())
               .method_9226(() -> Text.method_43470("任务完成: " + template.title).method_27692(Formatting.field_1060), false);
         }

         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource())
            .method_9226(() -> Text.method_43470("完成任务时发生错误: " + e.getMessage()).method_27692(Formatting.field_1061), false);
         return 0;
      }
   }
}
