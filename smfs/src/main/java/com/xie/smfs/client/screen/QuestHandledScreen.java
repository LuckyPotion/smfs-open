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
      this.field_2792 = 256;
      this.field_2779 = 166;
      this.field_25268 = 10000;
      this.field_25270 = 10000;
   }

   protected void method_25426() {
      super.method_25426();
      int tabX = this.field_2776 + 20;
      int tabY = this.field_2800 + 8;
      int tabWidth = 60;
      int tabHeight = 18;
      int totalTabWidth = tabWidth * 3;
      int availableSpace = this.field_2792 - 20;
      int tabSpacing = Math.max(5, (availableSpace - totalTabWidth) / 4);
      this.activeTabButton = ButtonWidget.method_46430(Text.method_43471("quest.tab.active"), button -> this.updateTabSelection(this.activeTabButton))
         .method_46434(tabX, tabY, tabWidth, tabHeight)
         .method_46431();
      this.completedTabButton = ButtonWidget.method_46430(Text.method_43471("quest.tab.completed"), button -> this.updateTabSelection(this.completedTabButton))
         .method_46434(tabX + tabWidth + tabSpacing, tabY, tabWidth, tabHeight)
         .method_46431();
      this.availableTabButton = ButtonWidget.method_46430(Text.method_43471("quest.tab.available"), button -> this.updateTabSelection(this.availableTabButton))
         .method_46434(tabX + (tabWidth + tabSpacing) * 2, tabY, tabWidth, tabHeight)
         .method_46431();
      this.method_37063(this.activeTabButton);
      this.method_37063(this.completedTabButton);
      this.method_37063(this.availableTabButton);
      int scrollButtonX = this.field_2776 + this.field_2792 - 5;
      int scrollButtonY = this.field_2800 + 40;
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
      this.updateTabSelection(this.activeTabButton);
   }

   public void method_25419() {
      super.method_25419();
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
         this.method_37066(element);
      }

      this.dynamicElements.clear();
      this.activeTabButton.method_25355(Text.method_43471("quest.tab.active"));
      this.completedTabButton.method_25355(Text.method_43471("quest.tab.completed"));
      this.availableTabButton.method_25355(Text.method_43471("quest.tab.available"));
      this.scrollOffset = 0;
      if (selectedTab == this.activeTabButton) {
         this.activeTabButton.method_25355(Text.method_43470("▶ ").method_10852(Text.method_43471("quest.tab.active")));
         PlayerEntity player = this.field_22787.field_1724;
         if (player != null) {
            NbtCompound questData = QuestManager.getQuestData(player);
            if (questData == null) {
               return;
            }

            NbtList activeQuests = questData.method_10554("activeQuests", 10);
            int x = (this.field_22789 - this.field_2792) / 2;
            int y = (this.field_22790 - this.field_2779) / 2;
            int startIndex = Math.min(this.scrollOffset, Math.max(0, activeQuests.size() - 3));
            int endIndex = Math.min(startIndex + 3, activeQuests.size());
            this.maxScrollOffset = Math.max(0, activeQuests.size() - 3);

            for (int r = startIndex; r < endIndex; r++) {
               int questY = y + 40 + (r - startIndex) * 35;
               NbtCompound quest = activeQuests.method_10602(r);
               if (quest != null && quest.method_10545("status")) {
                  int status = quest.method_10550("status");
                  if (status == 1) {
                     ButtonWidget submitButton = ButtonWidget.method_46430(Text.method_43471("screen.smfs.quest.complete"), button -> {
                        String questId = quest.method_10558("id");
                        String questType = quest.method_10558("type");
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
                     }).method_46434(x + 160, questY + 20, 40, 14).method_46431();
                     this.method_37063(submitButton);
                     this.dynamicElements.add(submitButton);
                  }
               }
            }
         }
      } else if (selectedTab == this.completedTabButton) {
         this.completedTabButton.method_25355(Text.method_43470("▶ ").method_10852(Text.method_43471("quest.tab.completed")));
         PlayerEntity player = this.field_22787.field_1724;
         if (player != null) {
            NbtCompound questData = QuestManager.getQuestData(player);
            if (questData == null) {
               return;
            }

            NbtList completedQuests = questData.method_10554("completedQuests", 10);
            this.maxScrollOffset = Math.max(0, completedQuests.size() - 3);
         }
      } else if (selectedTab == this.availableTabButton) {
         this.availableTabButton.method_25355(Text.method_43470("▶ ").method_10852(Text.method_43471("quest.tab.available")));
         PlayerEntity player = this.field_22787.field_1724;
         if (player != null) {
            List<String[]> availableQuests = this.getAvailableQuests(player);
            int x = (this.field_22789 - this.field_2792) / 2;
            int y = (this.field_22790 - this.field_2779) / 2;
            int startIndex = Math.min(this.scrollOffset, Math.max(0, availableQuests.size() - 3));
            int endIndex = Math.min(startIndex + 3, availableQuests.size());
            this.maxScrollOffset = Math.max(0, availableQuests.size() - 3);

            for (int k = startIndex; k < endIndex; k++) {
               int questY = y + 40 + (k - startIndex) * 35;
               String[] questInfo = availableQuests.get(k);
               if (questInfo != null && questInfo.length >= 2) {
                  ButtonWidget acceptButton = ButtonWidget.method_46430(Text.method_43471("screen.smfs.quest.accept"), button -> {
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
                  }).method_46434(x + 160, questY + 20, 40, 14).method_46431();
                  this.method_37063(acceptButton);
                  this.dynamicElements.add(acceptButton);
               }
            }
         }
      }
   }

   public void refreshQuestData() {
      this.dynamicElements.clear();
      this.method_25426();
   }

   private List<String[]> getAvailableQuests(PlayerEntity player) {
      List<String[]> availableQuests = new ArrayList<>();
      NbtCompound questData = QuestManager.getQuestData(player);
      List<String> completedQuestIds = new ArrayList<>();
      if (questData != null) {
         NbtList completedQuests = questData.method_10554("completedQuests", 10);

         for (int m = 0; m < completedQuests.size(); m++) {
            NbtCompound quest = completedQuests.method_10602(m);
            completedQuestIds.add(quest.method_10558("id"));
         }
      }

      List<String> activeQuestIds = new ArrayList<>();
      if (questData != null) {
         NbtList activeQuests = questData.method_10554("activeQuests", 10);

         for (int n = 0; n < activeQuests.size(); n++) {
            NbtCompound quest = activeQuests.method_10602(n);
            activeQuestIds.add(quest.method_10558("id"));
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
         NbtList availableQuestsList = questData.method_10554("availableQuests", 10);

         for (int i = 0; i < availableQuestsList.size(); i++) {
            NbtCompound quest = availableQuestsList.method_10602(i);
            String questId = quest.method_10558("id");
            String questType = quest.method_10558("type");
            if ("daily".equals(questType) && !completedQuestIds.contains(questId)) {
               QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
               if (template != null) {
                  availableQuests.add(new String[]{template.getTitle(), template.getId(), "日常任务", questId});
               }
            }
         }
      }

      if (DailyQuestManager.hasUnlockedEventQuests(player)) {
         NbtList availableQuestsList = questData.method_10554("availableQuests", 10);

         for (int i = 0; i < availableQuestsList.size(); i++) {
            NbtCompound quest = availableQuestsList.method_10602(i);
            String questId = quest.method_10558("id");
            String questType = quest.method_10558("type");
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
      NbtList availableQuests = questData.method_10554("availableQuests", 10);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtCompound acceptedQuest = null;

      for (int i = 0; i < availableQuests.size(); i++) {
         NbtCompound quest = availableQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId)) {
            acceptedQuest = quest;
            availableQuests.method_10536(i);
            break;
         }
      }

      if (acceptedQuest != null) {
         acceptedQuest.method_10569("status", 1);
         long startTime = this.field_22787.field_1687 != null ? this.field_22787.field_1687.method_8510() : 0L;
         acceptedQuest.method_10544("startTime", startTime);
         activeQuests.add(acceptedQuest);
         questData.method_10566("availableQuests", availableQuests);
         questData.method_10566("activeQuests", activeQuests);
         QuestManager.saveQuestData(player, questData);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
            if (template != null) {
               serverPlayer.method_7353(Text.method_43470("§a日常任务开始: " + template.title), false);
            }
         }
      }
   }

   private void acceptEventQuest(PlayerEntity player, String questId) {
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList availableQuests = questData.method_10554("availableQuests", 10);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtCompound acceptedQuest = null;

      for (int i = 0; i < availableQuests.size(); i++) {
         NbtCompound quest = availableQuests.method_10602(i);
         if (quest.method_10558("id").equals(questId)) {
            acceptedQuest = quest;
            availableQuests.method_10536(i);
            break;
         }
      }

      if (acceptedQuest != null) {
         acceptedQuest.method_10569("status", 1);
         long startTime = this.field_22787.field_1687 != null ? this.field_22787.field_1687.method_8510() : 0L;
         acceptedQuest.method_10544("startTime", startTime);
         activeQuests.add(acceptedQuest);
         questData.method_10566("availableQuests", availableQuests);
         questData.method_10566("activeQuests", activeQuests);
         QuestManager.saveQuestData(player, questData);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questId);
            if (template != null) {
               serverPlayer.method_7353(Text.method_43470("§a事件任务开始: " + template.title), false);
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
      PlayerInventory inventory = player.method_31548();

      for (int p = 0; p < inventory.method_5439(); p++) {
         ItemStack stack = inventory.method_5438(p);
         if (!stack.method_7960() && stack.method_7909() == ModItems.GOLDEN_CONTAINER && (!stack.method_7985() || !stack.method_7969().method_10577("IsHeavy"))
            )
          {
            return true;
         }
      }

      return false;
   }

   private void handleItemSubmission(PlayerEntity player, NbtCompound quest, String itemId, int amount, boolean consume) {
      String questId = quest.method_10558("id");
      if ("newbie_world_days".equals(questId)) {
         this.handleQuestCompletion(player, quest, questId);
      } else {
         String strategyType = QuestConfig.getStrategyTypeForObjective(itemId);
         QuestDetectionPacket packet = new QuestDetectionPacket(questId, itemId, strategyType, amount, consume);
         ClientModNetwork.sendToServer(packet);
      }
   }

   public void handleAcceptResult(String questId, boolean success, NbtCompound updatedQuestData) {
      PlayerEntity player = this.field_22787.field_1724;
      if (player != null) {
         if (success) {
            if (updatedQuestData != null) {
               QuestManager.updateClientQuestData(player, updatedQuestData);
            }

            this.selectedTab = 0;
            this.refreshUI();
            if (this.field_22787.field_1724 != null) {
               this.field_22787.field_1724.method_17356(ModSounds.QUEST_SUCCESS, SoundCategory.field_15250, 1.0F, 1.0F);
            }
         } else if (this.field_22787.field_1724 != null) {
            this.field_22787.field_1724.method_17356(ModSounds.QUEST_FAILURE, SoundCategory.field_15250, 1.0F, 1.0F);
         }
      }
   }

   public void handleDetectionResult(String questId, String objectiveId, boolean success) {
      PlayerEntity player = this.field_22787.field_1724;
      if (player != null) {
         NbtCompound questData = QuestManager.getQuestData(player);
         if (questData != null) {
            NbtList activeQuests = questData.method_10554("activeQuests", 10);
            NbtCompound targetQuest = null;

            for (int i = 0; i < activeQuests.size(); i++) {
               NbtCompound quest = activeQuests.method_10602(i);
               if (quest.method_10558("id").equals(questId)) {
                  targetQuest = quest;
                  break;
               }
            }

            if (targetQuest == null) {
               NbtList completedQuests = questData.method_10554("completedQuests", 10);

               for (int i = 0; i < completedQuests.size(); i++) {
                  NbtCompound quest = completedQuests.method_10602(i);
                  if (quest.method_10558("id").equals(questId)) {
                     if (this.field_22787 != null) {
                        this.field_22787.execute(() -> this.updateTabSelection(this.activeTabButton));
                     }

                     return;
                  }
               }
            } else {
               if (success) {
                  this.handleQuestCompletion(player, targetQuest, questId);
                  if (this.field_22787 != null) {
                     this.field_22787.execute(() -> {
                        if (this.field_22787.field_1724 != null) {
                           this.field_22787.field_1724.method_17356(ModSounds.QUEST_SUCCESS, SoundCategory.field_15250, 1.0F, 1.0F);
                        }
                     });
                  }
               } else if (this.field_22787 != null) {
                  this.field_22787.execute(() -> {
                     this.field_22787.method_1507(null);
                     if (player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.method_7353(Text.method_43470("§c任务条件未满足，无法提交任务！"), false);
                     }

                     if (this.field_22787.field_1724 != null) {
                        this.field_22787.field_1724.method_17356(ModSounds.QUEST_FAILURE, SoundCategory.field_15250, 1.0F, 1.0F);
                     }
                  });
               }
            }
         }
      }
   }

   public void handleQuestCompletion(PlayerEntity player, NbtCompound quest, String questId) {
      LOGGER.info("处理任务完成逻辑，questId: {}", questId);
      quest.method_10569("status", 2);
      quest.method_10544("completeTime", System.currentTimeMillis());
      NbtCompound questData = QuestManager.getQuestData(player);
      NbtList activeQuests = questData.method_10554("activeQuests", 10);
      NbtList completedQuests = questData.method_10554("completedQuests", 10);

      for (int q = 0; q < activeQuests.size(); q++) {
         NbtCompound activeQuest = activeQuests.method_10602(q);
         if (activeQuest.method_10558("id").equals(questId)) {
            activeQuests.method_10536(q);
            break;
         }
      }

      completedQuests.add(quest);
      questData.method_10566("activeQuests", activeQuests);
      questData.method_10566("completedQuests", completedQuests);
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
         serverPlayer.method_7353(Text.method_43470("§a成功提交物品！任务已完成！奖励已发放！"), false);

         for (QuestManager.QuestTemplate nextTemplate : QuestManager.QUEST_TEMPLATES.values()) {
            if (nextTemplate.prerequisites.contains(questId)) {
               hasUnlockedNextQuest = true;
               break;
            }
         }

         if (hasUnlockedNextQuest) {
            serverPlayer.method_7353(Text.method_43470("§b已解锁后续任务！"), false);
         }

         if ("newbie_craft_gold_container".equals(questId)) {
            LOGGER.info("玩家完成黄金容器任务，立即分配日常任务");
            DailyQuestManager.assignNewDailyQuests(player);
            serverPlayer.method_7353(Text.method_43470("§a日常任务系统已解锁！"), false);
            LOGGER.info("黄金容器任务完成后已强制分配日常任务");
         }
      }

      if (this.field_22787 != null) {
         this.field_22787.execute(() -> this.updateTabSelection(this.activeTabButton));
      }
   }

   protected void method_2389(DrawContext context, float delta, int mouseX, int mouseY) {
      RenderSystem.setShader(GameRenderer::method_34542);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, TEXTURE);
      int x = (this.field_22789 - this.field_2792) / 2;
      int y = (this.field_22790 - this.field_2779) / 2;
      context.method_25302(TEXTURE, x, y, 0, 0, this.field_2792, this.field_2779);
      this.scrollUpButton.field_22764 = this.maxScrollOffset > 0;
      this.scrollDownButton.field_22764 = this.maxScrollOffset > 0;
      int separatorY = y + 32;
      context.method_25294(x + 10, separatorY, x + this.field_2792 - 10, separatorY + 1, -12566464);
      PlayerEntity player = this.field_22787.field_1724;
      if (player != null) {
         NbtCompound questData = QuestManager.getQuestData(player);
         if (questData == null) {
            return;
         }

         NbtList activeQuests = questData.method_10554("activeQuests", 10);
         NbtList completedQuests = questData.method_10554("completedQuests", 10);
         if (this.activeTabButton.method_25369().getString().startsWith("▶")) {
            if (activeQuests.size() == 0) {
               int noQuestX = x + (this.field_2792 - this.field_22793.method_27525(Text.method_43471("screen.smfs.quest.no_quests"))) / 2;
               int noQuestY = y + this.field_2779 / 2;
               context.method_51439(this.field_22793, Text.method_43471("screen.smfs.quest.no_quests"), noQuestX, noQuestY, 8421504, false);
            } else {
               int startIndex = Math.min(this.scrollOffset, Math.max(0, activeQuests.size() - 3));
               int endIndex = Math.min(startIndex + 3, activeQuests.size());
               this.maxScrollOffset = Math.max(0, activeQuests.size() - 3);

               for (int m = startIndex; m < endIndex; m++) {
                  int questY = y + 40 + (m - startIndex) * 35;
                  NbtCompound quest = activeQuests.method_10602(m);
                  if (quest != null && quest.method_10545("title") && quest.method_10545("status")) {
                     String title = quest.method_10558("title");
                     int status = quest.method_10550("status");
                     RenderSystem.setShaderTexture(0, CARD_TEXTURE);
                     int cardType = status == 1 ? 0 : 1;
                     int cardX = x + (this.field_2792 - 205) / 2;
                     context.method_25290(CARD_TEXTURE, cardX, questY - 2, cardType * 205, 0.0F, 205, 40, 256, 256);
                     int textX = cardX + 5;
                     context.method_51439(this.field_22793, Text.method_43470(title), textX, questY + 10, 16777215, false);
                     if (quest.method_10545("description")) {
                        String description = quest.method_10558("description");
                        if (description.length() > 20) {
                           description = description.substring(0, 17) + "...";
                        }

                        context.method_51439(this.field_22793, Text.method_43470(description), textX, questY + 20, 11184810, false);
                     }

                     String questTypeText = "";
                     if (quest.method_10545("type")) {
                        String questType = quest.method_10558("type");
                        if ("main".equals(questType) && quest.method_10545("isNewbieQuest") && quest.method_10577("isNewbieQuest")) {
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
                     context.method_51439(this.field_22793, Text.method_43470(questTypeText), typeX, questY + 10, 16776960, false);
                  }
               }
            }
         } else if (this.completedTabButton.method_25369().getString().startsWith("▶")) {
            if (completedQuests.size() == 0) {
               int noQuestX = x + (this.field_2792 - this.field_22793.method_27525(Text.method_43471("screen.smfs.quest.no_completed"))) / 2;
               int noQuestY = y + this.field_2779 / 2;
               context.method_51439(this.field_22793, Text.method_43471("screen.smfs.quest.no_completed"), noQuestX, noQuestY, 8421504, false);
            } else {
               int startIndex = Math.min(this.scrollOffset, Math.max(0, completedQuests.size() - 3));
               int endIndex = Math.min(startIndex + 3, completedQuests.size());
               this.maxScrollOffset = Math.max(0, completedQuests.size() - 3);

               for (int n = startIndex; n < endIndex; n++) {
                  int questY = y + 40 + (n - startIndex) * 35;
                  NbtCompound quest = completedQuests.method_10602(n);
                  if (quest != null && quest.method_10545("title") && quest.method_10545("completeTime")) {
                     String title = quest.method_10558("title");
                     long completeTime = quest.method_10537("completeTime");
                     RenderSystem.setShaderTexture(0, CARD_TEXTURE);
                     int cardX = x + (this.field_2792 - 205) / 2;
                     context.method_25290(CARD_TEXTURE, cardX, questY - 2, 0.0F, 82.0F, 205, 40, 256, 256);
                     int textX = cardX + 5;
                     context.method_51439(this.field_22793, Text.method_43470(title), textX, questY + 10, 16777215, false);
                     if (quest.method_10545("description")) {
                        String description = quest.method_10558("description");
                        if (description.length() > 20) {
                           description = description.substring(0, 17) + "...";
                        }

                        context.method_51439(this.field_22793, Text.method_43470(description), textX, questY + 20, 11184810, false);
                     }

                     String timeText = "已完成";
                     context.method_51439(this.field_22793, Text.method_43470(timeText), x + 165, questY + 20, 65280, false);
                  }
               }
            }
         } else if (this.availableTabButton.method_25369().getString().startsWith("▶") && player != null) {
            List<String[]> availableQuests = this.getAvailableQuests(player);
            if (availableQuests.size() == 0) {
               int noQuestX = x + (this.field_2792 - this.field_22793.method_27525(Text.method_43471("screen.smfs.quest.no_available"))) / 2;
               int noQuestY = y + this.field_2779 / 2;
               context.method_51439(this.field_22793, Text.method_43471("screen.smfs.quest.no_available"), noQuestX, noQuestY, 8421504, false);
            } else {
               int startIndex = Math.min(this.scrollOffset, Math.max(0, availableQuests.size() - 3));
               int endIndex = Math.min(startIndex + 3, availableQuests.size());
               this.maxScrollOffset = Math.max(0, availableQuests.size() - 3);

               for (int p = startIndex; p < endIndex; p++) {
                  int questY = y + 40 + (p - startIndex) * 35;
                  String[] questInfo = availableQuests.get(p);
                  if (questInfo != null && questInfo.length >= 3) {
                     RenderSystem.setShaderTexture(0, CARD_TEXTURE);
                     int cardX = x + (this.field_2792 - 205) / 2;
                     context.method_25290(CARD_TEXTURE, cardX, questY - 2, 0.0F, 41.0F, 205, 40, 256, 256);
                     QuestManager.QuestTemplate template = QuestManager.QUEST_TEMPLATES.get(questInfo[1]);
                     int textX = cardX + 5;
                     if (template != null) {
                        context.method_51439(this.field_22793, Text.method_43470(template.getTitle()), textX, questY + 10, 16777215, false);
                        String description = template.getDescription();
                        if (description.length() > 20) {
                           description = description.substring(0, 17) + "...";
                        }

                        context.method_51439(this.field_22793, Text.method_43470(description), textX, questY + 20, 11184810, false);
                     } else {
                        context.method_51439(this.field_22793, Text.method_43470(questInfo[0]), textX, questY + 10, 16777215, false);
                     }

                     int typeX = x + 160;
                     context.method_51439(this.field_22793, Text.method_43470(questInfo[2]), typeX, questY + 10, 16776960, false);
                  }
               }
            }
         }
      }
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      this.method_2380(context, mouseX, mouseY);
      if (this.activeTabButton.method_25405(mouseX, mouseY)) {
         context.method_51438(this.field_22793, Text.method_43471("quest.tab.active.tooltip"), mouseX, mouseY);
      } else if (this.completedTabButton.method_25405(mouseX, mouseY)) {
         context.method_51438(this.field_22793, Text.method_43471("quest.tab.completed.tooltip"), mouseX, mouseY);
      } else if (this.availableTabButton.method_25405(mouseX, mouseY)) {
         context.method_51438(this.field_22793, Text.method_43471("quest.tab.available.tooltip"), mouseX, mouseY);
      }
   }

   protected void method_2388(DrawContext context, int mouseX, int mouseY) {
   }
}
