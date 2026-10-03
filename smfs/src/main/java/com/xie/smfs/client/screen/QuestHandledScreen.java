package com.xie.smfs.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xie.smfs.event.screen.QuestScreenHandler;
import com.xie.smfs.manager.DailyQuestManager;
import com.xie.smfs.manager.QuestConfig;
import com.xie.smfs.manager.QuestManager;
import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.packets.quests.c2s.QuestAcceptPacket;
import com.xie.smfs.network.packets.quests.c2s.QuestDetectionPacket;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestHandledScreen extends HandledScreen<QuestScreenHandler> {
   private static final Logger LOGGER = LoggerFactory.getLogger(QuestHandledScreen.class);
   private static final Identifier TEXTURE = new Identifier("smfs", "textures/gui/quest_background.png");
   private static final Identifier CARD_TEXTURE = new Identifier("smfs", "textures/gui/card.png");
   private ButtonWidget activeTabButton;
   private ButtonWidget completedTabButton;
   private ButtonWidget availableTabButton;
   private int selectedTab = 0;
   private int scrollOffset = 0;
   private int maxScrollOffset = 0;
   private boolean isScrolling = false;
   private ButtonWidget scrollUpButton;
   private ButtonWidget scrollDownButton;
   private ButtonWidget closeButton;
   private static final int VISIBLE_TASKS = 3;
   private List<ButtonWidget> dynamicElements = new ArrayList<>();

   public QuestHandledScreen(QuestScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 256;
      this.backgroundHeight = 166;
      this.titleY = 10000;
      this.playerInventoryTitleY = 10000;
   }

   protected void init() {
      super.init();
      int tabX = this.x + 20;
      int tabY = this.y + 8;
      int tabWidth = 60;
      int tabHeight = 18;
      int totalTabWidth = tabWidth * 3;
      int availableSpace = this.backgroundWidth - 20;
      int tabSpacing = Math.max(5, (availableSpace - totalTabWidth) / 4);
      this.activeTabButton = ButtonWidget.builder(Text.translatable("quest.tab.active"), button -> this.updateTabSelection(this.activeTabButton))
         .dimensions(tabX, tabY, tabWidth, tabHeight)
         .build();
      this.completedTabButton = ButtonWidget.builder(Text.translatable("quest.tab.completed"), button -> this.updateTabSelection(this.completedTabButton))
         .dimensions(tabX + tabWidth + tabSpacing, tabY, tabWidth, tabHeight)
         .build();
      this.availableTabButton = ButtonWidget.builder(Text.translatable("quest.tab.available"), button -> this.updateTabSelection(this.availableTabButton))
         .dimensions(tabX + (tabWidth + tabSpacing) * 2, tabY, tabWidth, tabHeight)
         .build();
      this.addDrawableChild(this.activeTabButton);
      this.addDrawableChild(this.completedTabButton);
      this.addDrawableChild(this.availableTabButton);
      int scrollButtonX = this.x + this.backgroundWidth - 5;
      int scrollButtonY = this.y + 40;
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
      this.updateTabSelection(this.activeTabButton);
   }

   public void close() {
      super.close();
   }

   public void refreshUI() {
      if (this.selectedTab == 0 && this.activeTabButton != null) {
         this.updateTabSelection(this.activeTabButton);
      } else if (this.selectedTab == 1 && this.completedTabButton != null) {
         this.updateTabSelection(this.completedTabButton);
      } else if (this.selectedTab == 2 && this.availableTabButton != null) {
         this.updateTabSelection(this.availableTabButton);
      }
   }

   private void updateTabSelection(ButtonWidget selectedTab) {
      for (ButtonWidget element : this.dynamicElements) {
         this.remove(element);
      }

      this.dynamicElements.clear();
      this.activeTabButton.setMessage(Text.translatable("quest.tab.active"));
      this.completedTabButton.setMessage(Text.translatable("quest.tab.completed"));
      this.availableTabButton.setMessage(Text.translatable("quest.tab.available"));
      this.scrollOffset = 0;
      if (selectedTab == this.activeTabButton) {
         this.activeTabButton.setMessage(Text.literal("▶ ").append(Text.translatable("quest.tab.active")));
         PlayerEntity player = this.client.player;
         if (player != null) {
            NbtCompound questData = QuestManager.getQuestData(player);
            if (questData == null) {
               return;
            }

            NbtList activeQuests = questData.getList("activeQuests", 10);
            int x = (this.width - this.backgroundWidth) / 2;
            int y = (this.height - this.backgroundHeight) / 2;
            int startIndex = Math.min(this.scrollOffset, Math.max(0, activeQuests.size() - 3));
            int endIndex = Math.min(startIndex + 3, activeQuests.size());
            this.maxScrollOffset = Math.max(0, activeQuests.size() - 3);

            for (int r = startIndex; r < endIndex; r++) {
               int questY = y + 40 + (r - startIndex) * 35;
               NbtCompound quest = activeQuests.getCompound(r);
               if (quest != null && quest.contains("status")) {
                  int status = quest.getInt("status");
                  if (status == 1) {
                     ButtonWidget submitButton = ButtonWidget.builder(Text.translatable("screen.smfs.quest.complete"), button -> {
                        String questId = quest.getString("id");
                        String questType = quest.getString("type");
                        if ("seller_basic_collection".equals(questId)) {
                           this.handleSellerQuestSubmission(player, quest, questId);
                        } else if ("newbie".equals(questType)) {
                           this.handleNewbieQuestSubmission(player, quest, questId);
                        } else if ("main".equals(questType)) {
                           this.handleMainQuestSubmission(player, quest, questId);
                        } else if ("daily".equals(questType)) {
                           this.handleDailyQuestSubmission(player, quest, questId);
                        } else if ("side".equals(questType)) {
                           this.handleEventQuestSubmission(player, quest, questId);
                        } else {
                           QuestManager.checkQuestCompletion(player, quest);
                        }

                        this.updateTabSelection(this.activeTabButton);
                     }).dimensions(x + 160, questY + 20, 40, 14).build();
                     this.addDrawableChild(submitButton);
                     this.dynamicElements.add(submitButton);
                  }
               }
            }
         }
      } else if (selectedTab == this.completedTabButton) {
         this.completedTabButton.setMessage(Text.literal("▶ ").append(Text.translatable("quest.tab.completed")));
         PlayerEntity player = this.client.player;
         if (player != null) {
            NbtCompound questData = QuestManager.getQuestData(player);
            if (questData == null) {
               return;
            }

            NbtList completedQuests = questData.getList("completedQuests", 10);
            this.maxScrollOffset = Math.max(0, completedQuests.size() - 3);
         }
      } else if (selectedTab == this.availableTabButton) {
         this.availableTabButton.setMessage(Text.literal("▶ ").append(Text.translatable("quest.tab.available")));
         PlayerEntity player = this.client.player;
         if (player != null) {
            List<String[]> availableQuests = this.getAvailableQuests(player);
            int x = (this.width - this.backgroundWidth) / 2;
            int y = (this.height - this.backgroundHeight) / 2;
            int startIndex = Math.min(this.scrollOffset, Math.max(0, availableQuests.size() - 3));
            int endIndex = Math.min(startIndex + 3, availableQuests.size());
            this.maxScrollOffset = Math.max(0, availableQuests.size() - 3);

            for (int k = startIndex; k < endIndex; k++) {
               int questY = y + 40 + (k - startIndex) * 35;
               String[] questInfo = availableQuests.get(k);
               if (questInfo != null && questInfo.length >= 2) {
                  ButtonWidget acceptButton = ButtonWidget.builder(Text.translatable("screen.smfs.quest.accept"), button -> {
                     String taskId = questInfo[1];
                     String taskType = questInfo[2];
                     String questTypeParam;
                     if ("日常任务".equals(taskType)) {
                        questTypeParam = "daily";
                     } else if ("突发事件".equals(taskType)) {
                        questTypeParam = "event";
                     } else {
                        questTypeParam = "normal";
                     }

                     QuestAcceptPacket packet = new QuestAcceptPacket(taskId, questTypeParam);
                     ClientModNetwork.sendToServer(packet);
                     this.selectedTab = 0;
                     this.updateTabSelection(this.activeTabButton);
                  }).dimensions(x + 160, questY + 20, 40, 14).build();
                  this.addDrawableChild(acceptButton);
                  this.dynamicElements.add(acceptButton);
               }
            }
         }
      }
   }

   public void refreshQuestData() {
      this.dynamicElements.clear();
      this.init();
   }

   private List<String[]> getAvailableQuests(PlayerEntity player) {
      List<String[]> availableQuests = new ArrayList<>();
      NbtCompound questData = QuestManager.getQuestData(player);
      List<String> completedQuestIds = new ArrayList<>();
      if (questData != null) {
         NbtList completedQuests = questData.getList("completedQuests", 10);

         for (int m = 0; m < completedQuests.size(); m++) {
            NbtCompound quest = completedQuests.getCompound(m);
            completedQuestIds.add(quest.getString("id"));
         }
      }

      List<String> activeQuestIds = new ArrayList<>();
      if (questData != null) {
         NbtList activeQuests = questData.getList("activeQuests", 10);

         for (int n = 0; n < activeQuests.size(); n++) {
            NbtCompound quest = activeQuests.getCompound(n);
            activeQuestIds.add(quest.getString("id"));
         }
      }

      for (Entry<String, QuestManager.QuestTemplate> entry : QuestManager.QUEST_TEMPLATES.entrySet()) {
         QuestManager.QuestTemplate template = entry.getValue();
         String questId = entry.getKey();
         String questType = template.getType();
         if (!"daily".equals(questType) && !"side".equals(questType)) {
            boolean isCompleted = completedQuestIds.contains(questId);
            boolean isActive = activeQuestIds.contains(questId);
            if (!isCompleted && !isActive) {
               boolean prerequisitesMet = true;

               for (String prerequisite : template.getPrerequisites()) {
                  if (!completedQuestIds.contains(prerequisite)) {
                     prerequisitesMet = false;
                     break;
                  }
               }

               if (prerequisitesMet) {
                  String typeName = QuestConfig.getQuestTypeName(template.getType(), template.isNewbieQuest());
                  availableQuests.add(new String[]{template.getTitle(), template.getId(), typeName});
               }
            }
         }
      }

      if (DailyQuestManager.hasUnlockedDailyQuests(player)) {
         NbtList availableQuestsList = questData.getList("availableQuests", 10);

         for (int i = 0; i < availableQuestsList.size(); i++) {
            NbtCompound quest = availableQuestsList.getCompound(i);
            String questId = quest.getString("id");
            String questType = quest.getString("type");
            if ("daily".equals(questType) && !completedQuestIds.contains(questId)) {
               QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
               if (template != null) {
                  availableQuests.add(new String[]{template.getTitle(), template.getId(), "日常任务", questId});
               }
            }
         }
      }

      if (DailyQuestManager.hasUnlockedEventQuests(player)) {
         NbtList availableQuestsList = questData.getList("availableQuests", 10);

         for (int i = 0; i < availableQuestsList.size(); i++) {
            NbtCompound quest = availableQuestsList.getCompound(i);
            String questId = quest.getString("id");
            String questType = quest.getString("type");
            if ("side".equals(questType) && !completedQuestIds.contains(questId)) {
               QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
               if (template != null) {
                  availableQuests.add(new String[]{template.getTitle(), template.getId(), "突发事件", questId});
               }
            }
         }
      }

      availableQuests.sort((a, b) -> {
         QuestManager.QuestTemplate templateA = QuestManager.QUEST_TEMPLATES.get(a[1]);
         QuestManager.QuestTemplate templateB = QuestManager.QUEST_TEMPLATES.get(b[1]);
         if (templateA.isNewbieQuest() && templateB.isNewbieQuest()) {
            return Integer.compare(templateA.getNewbieOrder(), templateB.getNewbieOrder());
         } else if (templateA.isNewbieQuest()) {
            return -1;
         } else {
            return templateB.isNewbieQuest() ? 1 : a[2].compareTo(b[2]);
         }
      });
      return availableQuests;
   }

   private void acceptDailyQuest(PlayerEntity player, String questId) {
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList availableQuests = questData.getList("availableQuests", 10);
      NbtList activeQuests = questData.getList("activeQuests", 10);
      NbtCompound acceptedQuest = null;

      for (int i = 0; i < availableQuests.size(); i++) {
         NbtCompound quest = availableQuests.getCompound(i);
         if (quest.getString("id").equals(questId)) {
            acceptedQuest = quest;
            availableQuests.remove(i);
            break;
         }
      }

      if (acceptedQuest != null) {
         acceptedQuest.putInt("status", 1);
         long startTime = this.client.world != null ? this.client.world.getTime() : 0L;
         acceptedQuest.putLong("startTime", startTime);
         activeQuests.add(acceptedQuest);
         questData.put("availableQuests", availableQuests);
         questData.put("activeQuests", activeQuests);
         QuestManager.saveQuestData(player, questData);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
            if (template != null) {
               serverPlayer.sendMessage(Text.literal("§a日常任务开始: " + template.title), false);
            }
         }
      }
   }

   private void acceptEventQuest(PlayerEntity player, String questId) {
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList availableQuests = questData.getList("availableQuests", 10);
      NbtList activeQuests = questData.getList("activeQuests", 10);
      NbtCompound acceptedQuest = null;

      for (int i = 0; i < availableQuests.size(); i++) {
         NbtCompound quest = availableQuests.getCompound(i);
         if (quest.getString("id").equals(questId)) {
            acceptedQuest = quest;
            availableQuests.remove(i);
            break;
         }
      }

      if (acceptedQuest != null) {
         acceptedQuest.putInt("status", 1);
         long startTime = this.client.world != null ? this.client.world.getTime() : 0L;
         acceptedQuest.putLong("startTime", startTime);
         activeQuests.add(acceptedQuest);
         questData.put("availableQuests", availableQuests);
         questData.put("activeQuests", activeQuests);
         QuestManager.saveQuestData(player, questData);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
            if (template != null) {
               serverPlayer.sendMessage(Text.literal("§a事件任务开始: " + template.title), false);
            }
         }
      }
   }

   private void handleNewbieQuestSubmission(PlayerEntity player, NbtCompound quest, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template != null && !template.getObjectives().isEmpty()) {
         QuestManager.QuestObjective objective = template.getObjectives().get(0);
         this.handleItemSubmission(player, quest, objective.getType(), objective.getTargetCount(), false);
      } else {
         QuestManager.checkQuestCompletion(player, quest);
      }
   }

   private void handleMainQuestSubmission(PlayerEntity player, NbtCompound quest, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template != null && !template.getObjectives().isEmpty()) {
         QuestManager.QuestObjective objective = template.getObjectives().get(0);
         this.handleItemSubmission(player, quest, objective.getType(), objective.getTargetCount(), false);
      } else {
         QuestManager.checkQuestCompletion(player, quest);
      }
   }

   private void handleDailyQuestSubmission(PlayerEntity player, NbtCompound quest, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template != null && !template.getObjectives().isEmpty()) {
         QuestManager.QuestObjective objective = template.getObjectives().get(0);
         boolean consumeItems = QuestConfig.shouldConsumeItemsForObjective(objective.getType());
         this.handleItemSubmission(player, quest, objective.getType(), objective.getTargetCount(), consumeItems);
      } else {
         QuestManager.checkQuestCompletion(player, quest);
      }
   }

   private void handleEventQuestSubmission(PlayerEntity player, NbtCompound quest, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template != null && !template.getObjectives().isEmpty()) {
         QuestManager.QuestObjective objective = template.getObjectives().get(0);
         this.handleItemSubmission(player, quest, objective.getType(), objective.getTargetCount(), false);
      } else {
         QuestManager.checkQuestCompletion(player, quest);
      }
   }

   private void handleSellerQuestSubmission(PlayerEntity player, NbtCompound quest, String questId) {
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
      if (template != null && !template.getObjectives().isEmpty()) {
         QuestManager.QuestObjective objective = template.getObjectives().get(0);
         this.handleItemSubmission(player, quest, objective.getType(), objective.getTargetCount(), true);
      } else {
         QuestManager.checkQuestCompletion(player, quest);
      }
   }

   private boolean hasGoldContainer(PlayerEntity player) {
      PlayerInventory inventory = player.getInventory();

      for (int p = 0; p < inventory.size(); p++) {
         ItemStack stack = inventory.getStack(p);
         if (!stack.isEmpty() && stack.getItem() == ModItems.GOLDEN_CONTAINER && (!stack.hasNbt() || !stack.getNbt().getBoolean("IsHeavy"))) {
            return true;
         }
      }

      return false;
   }

   private void handleItemSubmission(PlayerEntity player, NbtCompound quest, String itemId, int amount, boolean consume) {
      String questId = quest.getString("id");
      if ("newbie_world_days".equals(questId)) {
         this.handleQuestCompletion(player, quest, questId);
      } else {
         String strategyType = QuestConfig.getStrategyTypeForObjective(itemId);
         QuestDetectionPacket packet = new QuestDetectionPacket(questId, itemId, strategyType, amount, consume);
         ClientModNetwork.sendToServer(packet);
      }
   }

   public void handleAcceptResult(String questId, boolean success, NbtCompound updatedQuestData) {
      PlayerEntity player = this.client.player;
      if (player != null) {
         if (success) {
            if (updatedQuestData != null) {
               QuestManager.updateClientQuestData(player, updatedQuestData);
            }

            this.selectedTab = 0;
            this.refreshUI();
            if (this.client.player != null) {
               this.client.player.playSound(ModSounds.QUEST_SUCCESS, SoundCategory.MASTER, 1.0F, 1.0F);
            }
         } else if (this.client.player != null) {
            this.client.player.playSound(ModSounds.QUEST_FAILURE, SoundCategory.MASTER, 1.0F, 1.0F);
         }
      }
   }

   public void handleDetectionResult(String questId, String objectiveId, boolean success) {
      PlayerEntity player = this.client.player;
      if (player != null) {
         NbtCompound questData = QuestManager.getQuestData(player);
         if (questData != null) {
            NbtList activeQuests = questData.getList("activeQuests", 10);
            NbtCompound targetQuest = null;

            for (int i = 0; i < activeQuests.size(); i++) {
               NbtCompound quest = activeQuests.getCompound(i);
               if (quest.getString("id").equals(questId)) {
                  targetQuest = quest;
                  break;
               }
            }

            if (targetQuest == null) {
               NbtList completedQuests = questData.getList("completedQuests", 10);

               for (int i = 0; i < completedQuests.size(); i++) {
                  NbtCompound quest = completedQuests.getCompound(i);
                  if (quest.getString("id").equals(questId)) {
                     if (this.client != null) {
                        this.client.execute(() -> this.updateTabSelection(this.activeTabButton));
                     }

                     return;
                  }
               }
            } else {
               if (success) {
                  this.handleQuestCompletion(player, targetQuest, questId);
                  if (this.client != null) {
                     this.client.execute(() -> {
                        if (this.client.player != null) {
                           this.client.player.playSound(ModSounds.QUEST_SUCCESS, SoundCategory.MASTER, 1.0F, 1.0F);
                        }
                     });
                  }
               } else if (this.client != null) {
                  this.client.execute(() -> {
                     this.client.setScreen(null);
                     if (player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.sendMessage(Text.literal("§c任务条件未满足，无法提交任务！"), false);
                     }

                     if (this.client.player != null) {
                        this.client.player.playSound(ModSounds.QUEST_FAILURE, SoundCategory.MASTER, 1.0F, 1.0F);
                     }
                  });
               }
            }
         }
      }
   }

   public void handleQuestCompletion(PlayerEntity player, NbtCompound quest, String questId) {
      LOGGER.info("处理任务完成逻辑，questId: {}", questId);
      quest.putInt("status", 2);
      quest.putLong("completeTime", System.currentTimeMillis());
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.getList("activeQuests", 10);
      NbtList completedQuests = questData.getList("completedQuests", 10);

      for (int q = 0; q < activeQuests.size(); q++) {
         NbtCompound activeQuest = activeQuests.getCompound(q);
         if (activeQuest.getString("id").equals(questId)) {
            activeQuests.remove(q);
            break;
         }
      }

      completedQuests.add(quest);
      questData.put("activeQuests", activeQuests);
      questData.put("completedQuests", completedQuests);
      QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);

      for (QuestManager.QuestTemplate nextTemplate : QuestManager.QUEST_TEMPLATES.values()) {
         if (nextTemplate.prerequisites.contains(questId)) {
            LOGGER.info("解锁后续任务: {}", nextTemplate.id);
         }
      }

      QuestManager.saveQuestData(player, questData);
      QuestManager.giveQuestRewards(player, questId);
      boolean hasUnlockedNextQuest = false;
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.sendMessage(Text.literal("§a成功提交物品！任务已完成！奖励已发放！"), false);

         for (QuestManager.QuestTemplate nextTemplate : QuestManager.QUEST_TEMPLATES.values()) {
            if (nextTemplate.prerequisites.contains(questId)) {
               hasUnlockedNextQuest = true;
               break;
            }
         }

         if (hasUnlockedNextQuest) {
            serverPlayer.sendMessage(Text.literal("§b已解锁后续任务！"), false);
         }

         if ("newbie_craft_gold_container".equals(questId)) {
            LOGGER.info("玩家完成黄金容器任务，立即分配日常任务");
            DailyQuestManager.assignNewDailyQuests(player);
            serverPlayer.sendMessage(Text.literal("§a日常任务系统已解锁！"), false);
            LOGGER.info("黄金容器任务完成后已强制分配日常任务");
         }
      }

      if (this.client != null) {
         this.client.execute(() -> this.updateTabSelection(this.activeTabButton));
      }
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::getPositionTexProgram);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, TEXTURE);
      int x = (this.width - this.backgroundWidth) / 2;
      int y = (this.height - this.backgroundHeight) / 2;
      context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
      this.scrollUpButton.visible = this.maxScrollOffset > 0;
      this.scrollDownButton.visible = this.maxScrollOffset > 0;
      int separatorY = y + 32;
      context.fill(x + 10, separatorY, x + this.backgroundWidth - 10, separatorY + 1, -12566464);
      PlayerEntity player = this.client.player;
      if (player != null) {
         NbtCompound questData = QuestManager.getQuestData(player);
         if (questData == null) {
            return;
         }

         NbtList activeQuests = questData.getList("activeQuests", 10);
         NbtList completedQuests = questData.getList("completedQuests", 10);
         if (this.activeTabButton.getMessage().getString().startsWith("▶")) {
            if (activeQuests.size() == 0) {
               int noQuestX = x + (this.backgroundWidth - this.textRenderer.getWidth(Text.translatable("screen.smfs.quest.no_quests"))) / 2;
               int noQuestY = y + this.backgroundHeight / 2;
               context.drawText(this.textRenderer, Text.translatable("screen.smfs.quest.no_quests"), noQuestX, noQuestY, 8421504, false);
            } else {
               int startIndex = Math.min(this.scrollOffset, Math.max(0, activeQuests.size() - 3));
               int endIndex = Math.min(startIndex + 3, activeQuests.size());
               this.maxScrollOffset = Math.max(0, activeQuests.size() - 3);

               for (int m = startIndex; m < endIndex; m++) {
                  int questY = y + 40 + (m - startIndex) * 35;
                  NbtCompound quest = activeQuests.getCompound(m);
                  if (quest != null && quest.contains("title") && quest.contains("status")) {
                     String title = quest.getString("title");
                     int status = quest.getInt("status");
                     RenderSystem.setShaderTexture(0, CARD_TEXTURE);
                     int cardType = status == 1 ? 0 : 1;
                     int cardX = x + (this.backgroundWidth - 205) / 2;
                     context.drawTexture(CARD_TEXTURE, cardX, questY - 2, cardType * 205, 0.0F, 205, 40, 256, 256);
                     int textX = cardX + 5;
                     context.drawText(this.textRenderer, Text.literal(title), textX, questY + 10, 16777215, false);
                     if (quest.contains("description")) {
                        String description = quest.getString("description");
                        if (description.length() > 20) {
                           description = description.substring(0, 17) + "...";
                        }

                        context.drawText(this.textRenderer, Text.literal(description), textX, questY + 20, 11184810, false);
                     }

                     String questTypeText = "";
                     if (quest.contains("type")) {
                        String questType = quest.getString("type");
                        if ("main".equals(questType) && quest.contains("isNewbieQuest") && quest.getBoolean("isNewbieQuest")) {
                           questTypeText = "新手任务";
                        } else {
                           switch (questType) {
                              case "newbie":
                                 questTypeText = "新手任务";
                                 break;
                              case "main":
                                 questTypeText = "主线任务";
                                 break;
                              case "side":
                                 questTypeText = "突发事件";
                                 break;
                              case "daily":
                                 questTypeText = "日常任务";
                                 break;
                              default:
                                 questTypeText = questType;
                           }
                        }
                     }

                     int typeX = x + 160;
                     context.drawText(this.textRenderer, Text.literal(questTypeText), typeX, questY + 10, 16776960, false);
                  }
               }
            }
         } else if (this.completedTabButton.getMessage().getString().startsWith("▶")) {
            if (completedQuests.size() == 0) {
               int noQuestX = x + (this.backgroundWidth - this.textRenderer.getWidth(Text.translatable("screen.smfs.quest.no_completed"))) / 2;
               int noQuestY = y + this.backgroundHeight / 2;
               context.drawText(this.textRenderer, Text.translatable("screen.smfs.quest.no_completed"), noQuestX, noQuestY, 8421504, false);
            } else {
               int startIndex = Math.min(this.scrollOffset, Math.max(0, completedQuests.size() - 3));
               int endIndex = Math.min(startIndex + 3, completedQuests.size());
               this.maxScrollOffset = Math.max(0, completedQuests.size() - 3);

               for (int n = startIndex; n < endIndex; n++) {
                  int questY = y + 40 + (n - startIndex) * 35;
                  NbtCompound quest = completedQuests.getCompound(n);
                  if (quest != null && quest.contains("title") && quest.contains("completeTime")) {
                     String title = quest.getString("title");
                     long completeTime = quest.getLong("completeTime");
                     RenderSystem.setShaderTexture(0, CARD_TEXTURE);
                     int cardX = x + (this.backgroundWidth - 205) / 2;
                     context.drawTexture(CARD_TEXTURE, cardX, questY - 2, 0.0F, 82.0F, 205, 40, 256, 256);
                     int textX = cardX + 5;
                     context.drawText(this.textRenderer, Text.literal(title), textX, questY + 10, 16777215, false);
                     if (quest.contains("description")) {
                        String description = quest.getString("description");
                        if (description.length() > 20) {
                           description = description.substring(0, 17) + "...";
                        }

                        context.drawText(this.textRenderer, Text.literal(description), textX, questY + 20, 11184810, false);
                     }

                     String timeText = "已完成";
                     context.drawText(this.textRenderer, Text.literal(timeText), x + 165, questY + 20, 65280, false);
                  }
               }
            }
         } else if (this.availableTabButton.getMessage().getString().startsWith("▶") && player != null) {
            List<String[]> availableQuests = this.getAvailableQuests(player);
            if (availableQuests.size() == 0) {
               int noQuestX = x + (this.backgroundWidth - this.textRenderer.getWidth(Text.translatable("screen.smfs.quest.no_available"))) / 2;
               int noQuestY = y + this.backgroundHeight / 2;
               context.drawText(this.textRenderer, Text.translatable("screen.smfs.quest.no_available"), noQuestX, noQuestY, 8421504, false);
            } else {
               int startIndex = Math.min(this.scrollOffset, Math.max(0, availableQuests.size() - 3));
               int endIndex = Math.min(startIndex + 3, availableQuests.size());
               this.maxScrollOffset = Math.max(0, availableQuests.size() - 3);

               for (int p = startIndex; p < endIndex; p++) {
                  int questY = y + 40 + (p - startIndex) * 35;
                  String[] questInfo = availableQuests.get(p);
                  if (questInfo != null && questInfo.length >= 3) {
                     RenderSystem.setShaderTexture(0, CARD_TEXTURE);
                     int cardX = x + (this.backgroundWidth - 205) / 2;
                     context.drawTexture(CARD_TEXTURE, cardX, questY - 2, 0.0F, 41.0F, 205, 40, 256, 256);
                     QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questInfo[1]);
                     int textX = cardX + 5;
                     if (template != null) {
                        context.drawText(this.textRenderer, Text.literal(template.getTitle()), textX, questY + 10, 16777215, false);
                        String description = template.getDescription();
                        if (description.length() > 20) {
                           description = description.substring(0, 17) + "...";
                        }

                        context.drawText(this.textRenderer, Text.literal(description), textX, questY + 20, 11184810, false);
                     } else {
                        context.drawText(this.textRenderer, Text.literal(questInfo[0]), textX, questY + 10, 16777215, false);
                     }

                     int typeX = x + 160;
                     context.drawText(this.textRenderer, Text.literal(questInfo[2]), typeX, questY + 10, 16776960, false);
                  }
               }
            }
         }
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
      if (this.activeTabButton.isMouseOver(mouseX, mouseY)) {
         context.drawTooltip(this.textRenderer, Text.translatable("quest.tab.active.tooltip"), mouseX, mouseY);
      } else if (this.completedTabButton.isMouseOver(mouseX, mouseY)) {
         context.drawTooltip(this.textRenderer, Text.translatable("quest.tab.completed.tooltip"), mouseX, mouseY);
      } else if (this.availableTabButton.isMouseOver(mouseX, mouseY)) {
         context.drawTooltip(this.textRenderer, Text.translatable("quest.tab.available.tooltip"), mouseX, mouseY);
      }
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
   }
}
