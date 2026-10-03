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
      super(Text.literal("任务系统"));
      this.player = player;
   }

   protected void init() {
      super.init();
      int buttonWidth = 80;
      int buttonHeight = 20;
      int buttonSpacing = 5;
      int startX = (this.width - (buttonWidth * 3 + buttonSpacing * 2)) / 2;
      this.addDrawableChild(ButtonWidget.builder(Text.literal("进行中"), button -> this.currentTab = 0).dimensions(startX, 30, buttonWidth, buttonHeight).build());
      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("已完成"), button -> this.currentTab = 1)
            .dimensions(startX + buttonWidth + buttonSpacing, 30, buttonWidth, buttonHeight)
            .build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(Text.literal("可接受"), button -> this.currentTab = 2)
            .dimensions(startX + (buttonWidth + buttonSpacing) * 2, 30, buttonWidth, buttonHeight)
            .build()
      );
      int scrollButtonX = this.width - 30;
      int scrollButtonY = 80;
      int scrollButtonSize = 16;
      this.scrollUpButton = ButtonWidget.builder(Text.literal("↑"), button -> {
         if (this.scrollOffset > 0) {
            this.scrollOffset--;
         }
      }).dimensions(scrollButtonX, scrollButtonY, scrollButtonSize, scrollButtonSize).build();
      this.scrollDownButton = ButtonWidget.builder(Text.literal("↓"), button -> {
         if (this.scrollOffset < this.maxScrollOffset) {
            this.scrollOffset++;
         }
      }).dimensions(scrollButtonX, scrollButtonY + scrollButtonSize + 2, scrollButtonSize, scrollButtonSize).build();
      this.addDrawableChild(this.scrollUpButton);
      this.addDrawableChild(this.scrollDownButton);
      this.addDrawableChild(ButtonWidget.builder(Text.literal("关闭"), button -> this.close()).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.fillGradient(0, 0, this.width, this.height, -2146365167, -2145246686);
      context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§6§l任务系统").formatted(Formatting.BOLD), this.width / 2, 15, 16777215);
      String[] tabTitles = new String[]{"进行中的任务", "已完成的任务", "可接受的任务"};
      context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§e" + tabTitles[this.currentTab]), this.width / 2, 55, 16777215);
      if (this.player instanceof IPlayerData playerData) {
         int contentY = 75;
         int contentWidth = this.width - 40;
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

      this.scrollUpButton.visible = this.maxScrollOffset > 0;
      this.scrollDownButton.visible = this.maxScrollOffset > 0;
      super.render(context, mouseX, mouseY, delta);
   }

   private void renderActiveQuests(DrawContext context, IPlayerData playerData, int x, int y, int width) {
      List<NbtCompound> activeQuests = QuestManager.getActiveQuests(this.player);
      if (activeQuests.isEmpty()) {
         context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7暂无进行中的任务"), this.width / 2, y + 20, 16777215);
      } else {
         int startIndex = Math.min(this.scrollOffset, Math.max(0, activeQuests.size() - 3));
         int endIndex = Math.min(startIndex + 3, activeQuests.size());
         this.maxScrollOffset = Math.max(0, activeQuests.size() - 3);
         int questY = y;

         for (int i = startIndex; i < endIndex; i++) {
            NbtCompound quest = activeQuests.get(i);
            context.fill(x, questY, x + width, questY + 60, -2144128205);
            context.drawHorizontalLine(x, x + width, questY, -11184811);
            context.drawHorizontalLine(x, x + width, questY + 60, -11184811);
            String title = quest.getString("title");
            String description = quest.getString("description");
            context.drawText(this.textRenderer, Text.literal("§a§l" + title).formatted(Formatting.BOLD), x + 10, questY + 5, 16777215, false);
            context.drawText(this.textRenderer, Text.literal("§7" + description), x + 10, questY + 20, 16777215, false);
            NbtList objectives = quest.getList("objectives", 10);
            int objectiveY = questY + 35;

            for (int k = 0; k < objectives.size(); k++) {
               NbtCompound objective = objectives.getCompound(k);
               String objDesc = objective.getString("description");
               int progress = objective.getInt("progress");
               int target = objective.getInt("target");
               context.drawText(this.textRenderer, Text.literal("§f• " + objDesc), x + 10, objectiveY, 16777215, false);
               context.drawText(this.textRenderer, Text.literal("§e" + progress + "/" + target), x + width - 50, objectiveY, 16777215, false);
               objectiveY += 12;
            }

            boolean isCompleted = true;

            for (int k = 0; k < objectives.size(); k++) {
               NbtCompound objective = objectives.getCompound(k);
               int progress = objective.getInt("progress");
               int target = objective.getInt("target");
               if (progress < target) {
                  isCompleted = false;
                  break;
               }
            }

            boolean isEventQuest = false;
            String questId = quest.getString("id");

            for (int k = 0; k < objectives.size(); k++) {
               NbtCompound objective = objectives.getCompound(k);
               String objectiveId = objective.getString("id");
               if (objectiveId.endsWith("_event")) {
                  isEventQuest = true;
                  break;
               }
            }

            if (isCompleted) {
               if (isEventQuest) {
                  this.addDrawableChild(ButtonWidget.builder(Text.literal("提交容器"), button -> {
                     boolean success = QuestManager.submitEventQuest(this.player, questId);
                     if (success) {
                        if (this.player instanceof ServerPlayerEntity serverPlayer) {
                           serverPlayer.sendMessage(Text.literal("§a事件任务提交成功！奖励已发放"), false);
                        }
                     } else if (this.player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.sendMessage(Text.literal("§c提交失败，请检查是否拥有符合条件的黄金容器"), false);
                     }

                     this.close();
                  }).dimensions(x + width - 60, questY + 40, 50, 16).build());
               } else {
                  this.addDrawableChild(ButtonWidget.builder(Text.literal("提交"), button -> {
                     QuestManager.checkQuestCompletion(this.player, quest);
                     this.close();
                  }).dimensions(x + width - 60, questY + 40, 50, 16).build());
               }
            } else {
               this.addDrawableChild(ButtonWidget.builder(Text.literal("放弃"), button -> {
                  QuestManager.abandonQuest(this.player, questId);
                  this.close();
               }).dimensions(x + width - 60, questY + 40, 50, 16).build());
            }

            questY += 70;
         }
      }
   }

   private void renderCompletedQuests(DrawContext context, IPlayerData playerData, int x, int y, int width) {
      context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7已完成的任务功能暂未实现"), this.width / 2, y + 20, 16777215);
      context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7请查看任务日志了解完成情况"), this.width / 2, y + 40, 16777215);
      this.scrollOffset = 0;
      this.maxScrollOffset = 0;
   }

   private void renderAvailableQuests(DrawContext context, int x, int y, int width) {
      int currentY = y;
      List<QuestUIManager.QuestInfo> mainQuests = QuestUIManager.getAvailableMainQuests(this.player);
      if (!mainQuests.isEmpty()) {
         context.drawText(this.textRenderer, Text.literal("§e§l主线任务").formatted(Formatting.BOLD), x, currentY, 16777215, false);
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
         context.drawText(this.textRenderer, Text.literal("§a§l日常任务").formatted(Formatting.BOLD), x, currentY, 16777215, false);
         currentY += 20;
         if (!dailyQuests.isEmpty()) {
            for (QuestUIManager.QuestInfo questInfo : dailyQuests) {
               this.renderQuestCard(context, questInfo, x, currentY, width, true);
               currentY += 60;
            }
         } else {
            context.drawText(this.textRenderer, Text.literal("§7今日暂无日常任务"), x + 10, currentY, 16777215, false);
            currentY += 30;
         }

         currentY += 10;
      } else {
         context.drawText(this.textRenderer, Text.literal("§7§l日常任务系统（未解锁）").formatted(Formatting.BOLD), x, currentY, 16777215, false);
         currentY += 20;
         context.drawText(this.textRenderer, Text.literal("§7完成前置任务后解锁日常任务系统"), x + 10, currentY, 16777215, false);
         currentY += 40;
      }

      boolean eventUnlocked = QuestUIManager.isEventQuestSystemUnlocked(this.player);
      if (eventUnlocked) {
         List<QuestUIManager.QuestInfo> eventQuests = QuestUIManager.getAvailableEventQuests(this.player);
         context.drawText(this.textRenderer, Text.literal("§6§l事件任务").formatted(Formatting.BOLD), x, currentY, 16777215, false);
         currentY += 20;
         if (!eventQuests.isEmpty()) {
            for (QuestUIManager.QuestInfo questInfo : eventQuests) {
               this.renderQuestCard(context, questInfo, x, currentY, width, true);
               currentY += 60;
            }
         } else {
            context.drawText(this.textRenderer, Text.literal("§7当前暂无事件任务"), x + 10, currentY, 16777215, false);
            currentY += 30;
         }
      } else {
         context.drawText(this.textRenderer, Text.literal("§7§l事件任务系统（未解锁）").formatted(Formatting.BOLD), x, currentY, 16777215, false);
         currentY += 20;
         context.drawText(this.textRenderer, Text.literal("§7完成前置任务后解锁事件任务系统"), x + 10, currentY, 16777215, false);
         currentY += 40;
      }

      if (mainQuests.isEmpty() && !dailyUnlocked && !eventUnlocked) {
         context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7暂无可接受的新任务"), this.width / 2, y + 20, 16777215);
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

      context.fill(x, y, x + width, y + 50, bgColor);
      context.drawHorizontalLine(x, x + width, y, borderColor);
      context.drawHorizontalLine(x, x + width, y + 50, borderColor);
      String typeLabel = "";
      if ("daily".equals(questInfo.type)) {
         typeLabel = "§a[日常]";
      } else if ("event".equals(questInfo.type)) {
         typeLabel = "§6[事件]";
      }

      String dynamicMark = isDynamic ? "§d⚡" : "";
      context.drawText(
         this.textRenderer, Text.literal("§b§l" + questInfo.title + " " + typeLabel + dynamicMark).formatted(Formatting.BOLD), x + 10, y + 5, 16777215, false
      );
      context.drawText(this.textRenderer, Text.literal("§7" + questInfo.description), x + 10, y + 20, 16777215, false);
      this.addDrawableChild(ButtonWidget.builder(Text.literal("接受"), button -> {
         QuestManager.startQuest(this.player, questInfo.id);
         this.refreshScreen();
      }).dimensions(x + width - 60, y + 30, 50, 16).build());
   }

   private void refreshScreen() {
      this.clearChildren();
      this.init();
   }

   public boolean shouldPause() {
      return false;
   }
}
