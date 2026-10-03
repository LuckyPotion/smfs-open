package com.xie.smfs.client.screen;

import com.xie.smfs.api.IPlayerData;
import com.xie.smfs.manager.QuestManager;
import com.xie.smfs.manager.QuestUIManager;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class QuestScreen extends Screen {
   private static final Identifier TEXTURE = new Identifier("smfs", "textures/gui/quest.png");
   private static final Identifier BACKGROUND = new Identifier("textures/gui/demo_background.png");
   private final PlayerEntity player;
   private int currentTab = 0;
   private int scrollOffset = 0;
   private int maxScrollOffset = 0;
   private static final int VISIBLE_TASKS = 3;
   private ButtonWidget scrollUpButton;
   private ButtonWidget scrollDownButton;

   public QuestScreen(PlayerEntity player) {
      super(Text.method_43470("任务系统"));
      this.player = player;
   }

   protected void method_25426() {
      super.method_25426();
      int buttonWidth = 80;
      int buttonHeight = 20;
      int buttonSpacing = 5;
      int startX = (this.field_22789 - (buttonWidth * 3 + buttonSpacing * 2)) / 2;
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43470("进行中"), button -> this.currentTab = 0).method_46434(startX, 30, buttonWidth, buttonHeight).method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43470("已完成"), button -> this.currentTab = 1)
            .method_46434(startX + buttonWidth + buttonSpacing, 30, buttonWidth, buttonHeight)
            .method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43470("可接受"), button -> this.currentTab = 2)
            .method_46434(startX + (buttonWidth + buttonSpacing) * 2, 30, buttonWidth, buttonHeight)
            .method_46431()
      );
      int scrollButtonX = this.field_22789 - 30;
      int scrollButtonY = 80;
      int scrollButtonSize = 16;
      this.scrollUpButton = ButtonWidget.method_46430(Text.method_43470("↑"), button -> {
         if (this.scrollOffset > 0) {
            this.scrollOffset--;
         }
      }).method_46434(scrollButtonX, scrollButtonY, scrollButtonSize, scrollButtonSize).method_46431();
      this.scrollDownButton = ButtonWidget.method_46430(Text.method_43470("↓"), button -> {
         if (this.scrollOffset < this.maxScrollOffset) {
            this.scrollOffset++;
         }
      }).method_46434(scrollButtonX, scrollButtonY + scrollButtonSize + 2, scrollButtonSize, scrollButtonSize).method_46431();
      this.method_37063(this.scrollUpButton);
      this.method_37063(this.scrollDownButton);
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43470("关闭"), button -> this.method_25419())
            .method_46434(this.field_22789 / 2 - 50, this.field_22790 - 30, 100, 20)
            .method_46431()
      );
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      context.method_25296(0, 0, this.field_22789, this.field_22790, -2146365167, -2145246686);
      context.method_27534(this.field_22793, Text.method_43470("§6§l任务系统").method_27692(Formatting.field_1067), this.field_22789 / 2, 15, 16777215);
      String[] tabTitles = new String[]{"进行中的任务", "已完成的任务", "可接受的任务"};
      context.method_27534(this.field_22793, Text.method_43470("§e" + tabTitles[this.currentTab]), this.field_22789 / 2, 55, 16777215);
      if (this.player instanceof IPlayerData playerData) {
         int contentY = 75;
         int contentWidth = this.field_22789 - 40;
         int contentX = 20;
         switch (this.currentTab) {
            case 0:
               this.renderActiveQuests(context, playerData, contentX, contentY, contentWidth);
               break;
            case 1:
               this.renderCompletedQuests(context, playerData, contentX, contentY, contentWidth);
               break;
            case 2:
               this.renderAvailableQuests(context, contentX, contentY, contentWidth);
         }
      }

      this.scrollUpButton.field_22764 = this.maxScrollOffset > 0;
      this.scrollDownButton.field_22764 = this.maxScrollOffset > 0;
      super.method_25394(context, mouseX, mouseY, delta);
   }

   private void renderActiveQuests(DrawContext context, IPlayerData playerData, int x, int y, int width) {
      List<NbtCompound> activeQuests = QuestManager.getActiveQuests(this.player);
      if (activeQuests.isEmpty()) {
         context.method_27534(this.field_22793, Text.method_43470("§7暂无进行中的任务"), this.field_22789 / 2, y + 20, 16777215);
      } else {
         int startIndex = Math.min(this.scrollOffset, Math.max(0, activeQuests.size() - 3));
         int endIndex = Math.min(startIndex + 3, activeQuests.size());
         this.maxScrollOffset = Math.max(0, activeQuests.size() - 3);
         int questY = y;

         for (int i = startIndex; i < endIndex; i++) {
            NbtCompound quest = activeQuests.get(i);
            context.method_25294(x, questY, x + width, questY + 60, -2144128205);
            context.method_25292(x, x + width, questY, -11184811);
            context.method_25292(x, x + width, questY + 60, -11184811);
            String title = quest.method_10558("title");
            String description = quest.method_10558("description");
            context.method_51439(this.field_22793, Text.method_43470("§a§l" + title).method_27692(Formatting.field_1067), x + 10, questY + 5, 16777215, false);
            context.method_51439(this.field_22793, Text.method_43470("§7" + description), x + 10, questY + 20, 16777215, false);
            NbtList objectives = quest.method_10554("objectives", 10);
            int objectiveY = questY + 35;

            for (int k = 0; k < objectives.size(); k++) {
               NbtCompound objective = objectives.method_10602(k);
               String objDesc = objective.method_10558("description");
               int progress = objective.method_10550("progress");
               int target = objective.method_10550("target");
               context.method_51439(this.field_22793, Text.method_43470("§f• " + objDesc), x + 10, objectiveY, 16777215, false);
               context.method_51439(this.field_22793, Text.method_43470("§e" + progress + "/" + target), x + width - 50, objectiveY, 16777215, false);
               objectiveY += 12;
            }

            boolean isCompleted = true;

            for (int k = 0; k < objectives.size(); k++) {
               NbtCompound objective = objectives.method_10602(k);
               int progress = objective.method_10550("progress");
               int target = objective.method_10550("target");
               if (progress < target) {
                  isCompleted = false;
                  break;
               }
            }

            boolean isEventQuest = false;
            String questId = quest.method_10558("id");

            for (int k = 0; k < objectives.size(); k++) {
               NbtCompound objective = objectives.method_10602(k);
               String objectiveId = objective.method_10558("id");
               if (objectiveId.endsWith("_event")) {
                  isEventQuest = true;
                  break;
               }
            }

            if (isCompleted) {
               if (isEventQuest) {
                  this.method_37063(ButtonWidget.method_46430(Text.method_43470("提交容器"), button -> {
                     boolean success = QuestManager.submitEventQuest(this.player, questId);
                     if (success) {
                        if (this.player instanceof ServerPlayerEntity serverPlayer) {
                           serverPlayer.method_7353(Text.method_43470("§a事件任务提交成功！奖励已发放"), false);
                        }
                     } else if (this.player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.method_7353(Text.method_43470("§c提交失败，请检查是否拥有符合条件的黄金容器"), false);
                     }

                     this.method_25419();
                  }).method_46434(x + width - 60, questY + 40, 50, 16).method_46431());
               } else {
                  this.method_37063(ButtonWidget.method_46430(Text.method_43470("提交"), button -> {
                     QuestManager.checkQuestCompletion(this.player, quest);
                     this.method_25419();
                  }).method_46434(x + width - 60, questY + 40, 50, 16).method_46431());
               }
            } else {
               this.method_37063(ButtonWidget.method_46430(Text.method_43470("放弃"), button -> {
                  QuestManager.abandonQuest(this.player, questId);
                  this.method_25419();
               }).method_46434(x + width - 60, questY + 40, 50, 16).method_46431());
            }

            questY += 70;
         }
      }
   }

   private void renderCompletedQuests(DrawContext context, IPlayerData playerData, int x, int y, int width) {
      context.method_27534(this.field_22793, Text.method_43470("§7已完成的任务功能暂未实现"), this.field_22789 / 2, y + 20, 16777215);
      context.method_27534(this.field_22793, Text.method_43470("§7请查看任务日志了解完成情况"), this.field_22789 / 2, y + 40, 16777215);
      this.scrollOffset = 0;
      this.maxScrollOffset = 0;
   }

   private void renderAvailableQuests(DrawContext context, int x, int y, int width) {
      int currentY = y;
      List<QuestUIManager.QuestInfo> mainQuests = QuestUIManager.getAvailableMainQuests(this.player);
      if (!mainQuests.isEmpty()) {
         context.method_51439(this.field_22793, Text.method_43470("§e§l主线任务").method_27692(Formatting.field_1067), x, currentY, 16777215, false);
         currentY += 20;

         for (QuestUIManager.QuestInfo questInfo : mainQuests) {
            this.renderQuestCard(context, questInfo, x, currentY, width, false);
            currentY += 60;
         }

         currentY += 10;
      }

      boolean dailyUnlocked = QuestUIManager.isDailyQuestSystemUnlocked(this.player);
      if (dailyUnlocked) {
         List<QuestUIManager.QuestInfo> dailyQuests = QuestUIManager.getAvailableDailyQuests(this.player);
         context.method_51439(this.field_22793, Text.method_43470("§a§l日常任务").method_27692(Formatting.field_1067), x, currentY, 16777215, false);
         currentY += 20;
         if (!dailyQuests.isEmpty()) {
            for (QuestUIManager.QuestInfo questInfo : dailyQuests) {
               this.renderQuestCard(context, questInfo, x, currentY, width, true);
               currentY += 60;
            }
         } else {
            context.method_51439(this.field_22793, Text.method_43470("§7今日暂无日常任务"), x + 10, currentY, 16777215, false);
            currentY += 30;
         }

         currentY += 10;
      } else {
         context.method_51439(this.field_22793, Text.method_43470("§7§l日常任务系统（未解锁）").method_27692(Formatting.field_1067), x, currentY, 16777215, false);
         currentY += 20;
         context.method_51439(this.field_22793, Text.method_43470("§7完成前置任务后解锁日常任务系统"), x + 10, currentY, 16777215, false);
         currentY += 40;
      }

      boolean eventUnlocked = QuestUIManager.isEventQuestSystemUnlocked(this.player);
      if (eventUnlocked) {
         List<QuestUIManager.QuestInfo> eventQuests = QuestUIManager.getAvailableEventQuests(this.player);
         context.method_51439(this.field_22793, Text.method_43470("§6§l事件任务").method_27692(Formatting.field_1067), x, currentY, 16777215, false);
         currentY += 20;
         if (!eventQuests.isEmpty()) {
            for (QuestUIManager.QuestInfo questInfo : eventQuests) {
               this.renderQuestCard(context, questInfo, x, currentY, width, true);
               currentY += 60;
            }
         } else {
            context.method_51439(this.field_22793, Text.method_43470("§7当前暂无事件任务"), x + 10, currentY, 16777215, false);
            currentY += 30;
         }
      } else {
         context.method_51439(this.field_22793, Text.method_43470("§7§l事件任务系统（未解锁）").method_27692(Formatting.field_1067), x, currentY, 16777215, false);
         currentY += 20;
         context.method_51439(this.field_22793, Text.method_43470("§7完成前置任务后解锁事件任务系统"), x + 10, currentY, 16777215, false);
         currentY += 40;
      }

      if (mainQuests.isEmpty() && !dailyUnlocked && !eventUnlocked) {
         context.method_27534(this.field_22793, Text.method_43470("§7暂无可接受的新任务"), this.field_22789 / 2, y + 20, 16777215);
      }
   }

   private void renderQuestCard(DrawContext context, QuestUIManager.QuestInfo questInfo, int x, int y, int width, boolean isDynamic) {
      int bgColor = -2144115047;
      int borderColor = -10053172;
      if ("daily".equals(questInfo.type)) {
         bgColor = -2144102042;
         borderColor = -10040167;
      } else if ("event".equals(questInfo.type)) {
         bgColor = -2137430477;
         borderColor = -3368602;
      }

      context.method_25294(x, y, x + width, y + 50, bgColor);
      context.method_25292(x, x + width, y, borderColor);
      context.method_25292(x, x + width, y + 50, borderColor);
      String typeLabel = "";
      if ("daily".equals(questInfo.type)) {
         typeLabel = "§a[日常]";
      } else if ("event".equals(questInfo.type)) {
         typeLabel = "§6[事件]";
      }

      String dynamicMark = isDynamic ? "§d⚡" : "";
      context.method_51439(
         this.field_22793,
         Text.method_43470("§b§l" + questInfo.title + " " + typeLabel + dynamicMark).method_27692(Formatting.field_1067),
         x + 10,
         y + 5,
         16777215,
         false
      );
      context.method_51439(this.field_22793, Text.method_43470("§7" + questInfo.description), x + 10, y + 20, 16777215, false);
      this.method_37063(ButtonWidget.method_46430(Text.method_43470("接受"), button -> {
         QuestManager.startQuest(this.player, questInfo.id);
         this.refreshScreen();
      }).method_46434(x + width - 60, y + 30, 50, 16).method_46431());
   }

   private void refreshScreen() {
      this.method_37067();
      this.method_25426();
   }

   public boolean method_25421() {
      return false;
   }
}
