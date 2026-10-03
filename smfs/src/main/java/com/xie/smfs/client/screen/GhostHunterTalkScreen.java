package com.xie.smfs.client.screen;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.network.packets.ui.c2s.RequestJoinFactionC2SPacket;
import com.xie.smfs.network.packets.ui.c2s.RequestTradeScreenC2SPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GhostHunterTalkScreen extends Screen {
   private static final int BUTTON_WIDTH = 60;
   private static final int BUTTON_HEIGHT = 14;
   private static final int BUTTON_SPACING = 16;
   private static final int RIGHT_MARGIN = 20;
   private final Entity targetEntity;
   private final Identifier portraitTexture;
   private final Text characterName;
   private final List<String> dialogues;
   private String currentDialogue;
   private final List<String> storyDialogues;
   private final boolean alreadyTalkedToYangXiao;
   private int storyDialogueIndex;
   private boolean storyCompleted;
   private boolean showingQuests = false;
   private Map<String, String> questList = new HashMap<>();
   private boolean showingJoinConfirm = false;
   private final boolean isFactionNpc;

   public GhostHunterTalkScreen(Entity targetEntity, List<String> dialogues) {
      this(targetEntity, dialogues, null, false);
   }

   public GhostHunterTalkScreen(Entity targetEntity, List<String> dialogues, List<String> storyDialogues, boolean alreadyTalkedToYangXiao) {
      super(Text.method_43470("对话"));
      this.targetEntity = targetEntity;
      this.portraitTexture = this.getPortraitTexture(targetEntity);
      this.characterName = this.getCharacterName(targetEntity);
      this.dialogues = dialogues;
      this.storyDialogues = storyDialogues;
      this.alreadyTalkedToYangXiao = alreadyTalkedToYangXiao;
      PlayerFaction entityFaction = FactionManager.getFactionForEntity(targetEntity);
      this.isFactionNpc = entityFaction == PlayerFaction.HEADQUARTERS
         || entityFaction == PlayerFaction.SPIRIT_FORUM
         || entityFaction == PlayerFaction.PENGYOU_QUAN;
      if (this.isStoryMode()) {
         if (alreadyTalkedToYangXiao) {
            this.storyCompleted = true;
            this.storyDialogueIndex = storyDialogues.size();
            this.currentDialogue = "祝你好运。";
         } else {
            this.storyCompleted = false;
            this.storyDialogueIndex = 0;
            this.currentDialogue = storyDialogues.get(0);
         }
      } else {
         this.currentDialogue = this.getRandomDialogue();
      }
   }

   private boolean isStoryMode() {
      return this.storyDialogues != null && !this.storyDialogues.isEmpty();
   }

   private Identifier getPortraitTexture(Entity entity) {
      Identifier entityId = Registries.field_41177.method_10221(entity.method_5864());
      String portraitName = entityId.method_12832();
      return new Identifier("smfs", "textures/gui/portraits/" + portraitName + ".png");
   }

   private Text getCharacterName(Entity entity) {
      return entity.method_5477();
   }

   private String getRandomDialogue() {
      Random random = new Random();
      return this.dialogues != null && !this.dialogues.isEmpty() ? this.dialogues.get(random.nextInt(this.dialogues.size())) : "你好...";
   }

   protected void method_25426() {
      super.method_25426();
      int buttonY = this.field_22790 - 30;
      if (this.showingQuests) {
         Map<String, String> questList = this.getQuestListFromEntity();
         boolean hasQuests = !questList.isEmpty();
         int totalButtonWidth = 288;
         int startX = this.field_22789 - totalButtonWidth - 20;
         if (hasQuests) {
            ButtonWidget acceptButton = ButtonWidget.method_46430(Text.method_43470("接受"), button -> this.onAcceptQuest())
               .method_46434(startX + 120 + 32, buttonY, 60, 14)
               .method_46431();
            ButtonWidget leaveButton = ButtonWidget.method_46430(Text.method_43470("再见"), button -> this.onLeaveClicked())
               .method_46434(startX + 180 + 48, buttonY, 60, 14)
               .method_46431();
            this.method_37063(acceptButton);
            this.method_37063(leaveButton);
         } else {
            ButtonWidget leaveButton = ButtonWidget.method_46430(Text.method_43470("再见"), button -> this.onLeaveClicked())
               .method_46434(startX + 180 + 48, buttonY, 60, 14)
               .method_46431();
            this.method_37063(leaveButton);
         }
      } else if (this.isStoryMode()) {
         if (this.storyCompleted) {
            ButtonWidget leaveButton = ButtonWidget.method_46430(Text.method_43470("关闭"), button -> this.onLeaveClicked())
               .method_46434(this.field_22789 - 60 - 20, buttonY, 60, 14)
               .method_46431();
            this.method_37063(leaveButton);
         } else {
            int startX = this.field_22789 - 120 - 16 - 20;
            ButtonWidget talkButton = ButtonWidget.method_46430(Text.method_43470("交谈"), button -> this.onStoryTalkClicked())
               .method_46434(startX, buttonY, 60, 14)
               .method_46431();
            ButtonWidget leaveButton = ButtonWidget.method_46430(Text.method_43470("关闭"), button -> this.onLeaveClicked())
               .method_46434(startX + 60 + 16, buttonY, 60, 14)
               .method_46431();
            this.method_37063(talkButton);
            this.method_37063(leaveButton);
         }
      } else if (this.showingJoinConfirm) {
         String factionName = FactionManager.getFactionDisplayName(this.targetEntity);
         int totalButtonWidth = 136;
         int startX = this.field_22789 - totalButtonWidth - 20;
         ButtonWidget confirmButton = ButtonWidget.method_46430(Text.method_43470("确定加入"), button -> this.onConfirmJoin())
            .method_46434(startX, buttonY, 60, 14)
            .method_46431();
         ButtonWidget cancelButton = ButtonWidget.method_46430(Text.method_43470("取消"), button -> this.onCancelJoin())
            .method_46434(startX + 60 + 16, buttonY, 60, 14)
            .method_46431();
         this.method_37063(confirmButton);
         this.method_37063(cancelButton);
      } else {
         int totalButtonWidth = 288;
         int startX = this.field_22789 - totalButtonWidth - 20;
         ButtonWidget talkButton = ButtonWidget.method_46430(Text.method_43470("交谈"), button -> this.onTalkClicked())
            .method_46434(startX, buttonY, 60, 14)
            .method_46431();
         ButtonWidget tradeButton = ButtonWidget.method_46430(Text.method_43470("交易"), button -> this.onTradeClicked())
            .method_46434(startX + 60 + 16, buttonY, 60, 14)
            .method_46431();
         ButtonWidget questButton = ButtonWidget.method_46430(this.isFactionNpc ? Text.method_43470("组织") : Text.method_43470("委托"), button -> {
            if (this.isFactionNpc) {
               this.onJoinClicked();
            } else {
               this.onQuestClicked();
            }
         }).method_46434(startX + 120 + 32, buttonY, 60, 14).method_46431();
         ButtonWidget leaveButton = ButtonWidget.method_46430(Text.method_43470("再见"), button -> this.onLeaveClicked())
            .method_46434(startX + 180 + 48, buttonY, 60, 14)
            .method_46431();
         this.method_37063(talkButton);
         this.method_37063(tradeButton);
         this.method_37063(questButton);
         this.method_37063(leaveButton);
      }
   }

   private void onTalkClicked() {
      this.currentDialogue = this.getRandomDialogue();
   }

   private void onStoryTalkClicked() {
      this.storyDialogueIndex++;
      if (this.storyDialogueIndex < this.storyDialogues.size()) {
         this.currentDialogue = this.storyDialogues.get(this.storyDialogueIndex);
      } else {
         this.storyCompleted = true;
      }

      this.method_37067();
      this.method_25426();
   }

   private void onTradeClicked() {
      if (this.targetEntity != null) {
         RequestTradeScreenC2SPacket.send(this.targetEntity.method_5628());
      }

      this.method_25419();
   }

   private void onQuestClicked() {
      this.showingQuests = true;
      this.loadQuestList();
      if (this.questList.isEmpty()) {
         this.currentDialogue = "我这里没有你能做的委托。";
      } else {
         List<String> questTexts = new ArrayList<>(this.questList.values());
         this.currentDialogue = questTexts.get(new Random().nextInt(questTexts.size()));
      }

      this.method_37067();
      this.method_25426();
   }

   private void loadQuestList() {
      this.questList.clear();
      if (this.targetEntity instanceof GhostMasterEntity ghostMaster) {
         Map<String, String> entityQuests = ghostMaster.getQuestList();
         if (entityQuests != null) {
            this.questList.putAll(entityQuests);
         }
      }
   }

   private Map<String, String> getQuestListFromEntity() {
      return this.questList;
   }

   private void onAcceptQuest() {
      this.method_25419();
   }

   private void onLeaveClicked() {
      this.method_25419();
   }

   private void onJoinClicked() {
      String factionName = FactionManager.getFactionDisplayName(this.targetEntity);
      this.currentDialogue = "确定要加入" + factionName + "吗？";
      this.showingJoinConfirm = true;
      this.method_37067();
      this.method_25426();
   }

   private void onConfirmJoin() {
      if (this.targetEntity != null) {
         RequestJoinFactionC2SPacket.send(this.targetEntity.method_5628());
      }

      this.method_25419();
   }

   private void onCancelJoin() {
      this.currentDialogue = this.getRandomDialogue();
      this.showingJoinConfirm = false;
      this.method_37067();
      this.method_25426();
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      context.method_25294(0, 0, this.field_22789, this.field_22790, Integer.MIN_VALUE);
      if (this.hasPortraitTexture()) {
         int portraitWidth = this.field_22789 / 4;
         int portraitHeight = portraitWidth * 2;
         int portraitX = this.field_22789 - portraitWidth - 20;
         int portraitY = this.field_22790 - portraitHeight;
         context.method_25290(this.portraitTexture, portraitX, portraitY, 0.0F, 0.0F, portraitWidth, portraitHeight, portraitWidth, portraitHeight);
      }

      int blackBarHeight = 100;
      int blackBarY = this.field_22790 - blackBarHeight;
      context.method_25294(0, blackBarY, this.field_22789, this.field_22790, -872415232);
      int characterNameX = 20;
      int characterNameY = blackBarY + 10;
      context.method_27535(this.field_22793, this.characterName, characterNameX, characterNameY, 16777215);
      int textX = 20;
      int textY = blackBarY + 35;
      context.method_51439(this.field_22793, Text.method_43470(this.currentDialogue), textX, textY, -1, false);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   private boolean hasPortraitTexture() {
      ResourceManager resourceManager = MinecraftClient.method_1551().method_1478();
      return resourceManager.method_14486(this.portraitTexture).isPresent();
   }

   public boolean method_25422() {
      return true;
   }

   public void method_25419() {
      super.method_25419();
   }

   public Entity getTargetEntity() {
      return this.targetEntity;
   }
}
