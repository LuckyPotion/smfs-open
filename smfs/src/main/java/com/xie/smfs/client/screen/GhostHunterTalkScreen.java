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
      super(Text.literal("对话"));
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
      Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
      String portraitName = entityId.getPath();
      return new Identifier("smfs", "textures/gui/portraits/" + portraitName + ".png");
   }

   private Text getCharacterName(Entity entity) {
      return entity.getName();
   }

   private String getRandomDialogue() {
      Random random = new Random();
      return this.dialogues != null && !this.dialogues.isEmpty() ? this.dialogues.get(random.nextInt(this.dialogues.size())) : "你好...";
   }

   protected void init() {
      super.init();
      int buttonY = this.height - 30;
      if (this.showingQuests) {
         Map<String, String> questList = this.getQuestListFromEntity();
         boolean hasQuests = !questList.isEmpty();
         int totalButtonWidth = 288;
         int startX = this.width - totalButtonWidth - 20;
         if (hasQuests) {
            ButtonWidget acceptButton = ButtonWidget.builder(Text.literal("接受"), button -> this.onAcceptQuest())
               .dimensions(startX + 120 + 32, buttonY, 60, 14)
               .build();
            ButtonWidget leaveButton = ButtonWidget.builder(Text.literal("再见"), button -> this.onLeaveClicked())
               .dimensions(startX + 180 + 48, buttonY, 60, 14)
               .build();
            this.addDrawableChild(acceptButton);
            this.addDrawableChild(leaveButton);
         } else {
            ButtonWidget leaveButton = ButtonWidget.builder(Text.literal("再见"), button -> this.onLeaveClicked())
               .dimensions(startX + 180 + 48, buttonY, 60, 14)
               .build();
            this.addDrawableChild(leaveButton);
         }
      } else if (this.isStoryMode()) {
         if (this.storyCompleted) {
            ButtonWidget leaveButton = ButtonWidget.builder(Text.literal("关闭"), button -> this.onLeaveClicked())
               .dimensions(this.width - 60 - 20, buttonY, 60, 14)
               .build();
            this.addDrawableChild(leaveButton);
         } else {
            int startX = this.width - 120 - 16 - 20;
            ButtonWidget talkButton = ButtonWidget.builder(Text.literal("交谈"), button -> this.onStoryTalkClicked()).dimensions(startX, buttonY, 60, 14).build();
            ButtonWidget leaveButton = ButtonWidget.builder(Text.literal("关闭"), button -> this.onLeaveClicked())
               .dimensions(startX + 60 + 16, buttonY, 60, 14)
               .build();
            this.addDrawableChild(talkButton);
            this.addDrawableChild(leaveButton);
         }
      } else if (this.showingJoinConfirm) {
         String factionName = FactionManager.getFactionDisplayName(this.targetEntity);
         int totalButtonWidth = 136;
         int startX = this.width - totalButtonWidth - 20;
         ButtonWidget confirmButton = ButtonWidget.builder(Text.literal("确定加入"), button -> this.onConfirmJoin()).dimensions(startX, buttonY, 60, 14).build();
         ButtonWidget cancelButton = ButtonWidget.builder(Text.literal("取消"), button -> this.onCancelJoin())
            .dimensions(startX + 60 + 16, buttonY, 60, 14)
            .build();
         this.addDrawableChild(confirmButton);
         this.addDrawableChild(cancelButton);
      } else {
         int totalButtonWidth = 288;
         int startX = this.width - totalButtonWidth - 20;
         ButtonWidget talkButton = ButtonWidget.builder(Text.literal("交谈"), button -> this.onTalkClicked()).dimensions(startX, buttonY, 60, 14).build();
         ButtonWidget tradeButton = ButtonWidget.builder(Text.literal("交易"), button -> this.onTradeClicked())
            .dimensions(startX + 60 + 16, buttonY, 60, 14)
            .build();
         ButtonWidget questButton = ButtonWidget.builder(this.isFactionNpc ? Text.literal("组织") : Text.literal("委托"), button -> {
            if (this.isFactionNpc) {
               this.onJoinClicked();
            } else {
               this.onQuestClicked();
            }
         }).dimensions(startX + 120 + 32, buttonY, 60, 14).build();
         ButtonWidget leaveButton = ButtonWidget.builder(Text.literal("再见"), button -> this.onLeaveClicked())
            .dimensions(startX + 180 + 48, buttonY, 60, 14)
            .build();
         this.addDrawableChild(talkButton);
         this.addDrawableChild(tradeButton);
         this.addDrawableChild(questButton);
         this.addDrawableChild(leaveButton);
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

      this.clearChildren();
      this.init();
   }

   private void onTradeClicked() {
      if (this.targetEntity != null) {
         RequestTradeScreenC2SPacket.send(this.targetEntity.getId());
      }

      this.close();
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

      this.clearChildren();
      this.init();
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
      this.close();
   }

   private void onLeaveClicked() {
      this.close();
   }

   private void onJoinClicked() {
      String factionName = FactionManager.getFactionDisplayName(this.targetEntity);
      this.currentDialogue = "确定要加入" + factionName + "吗？";
      this.showingJoinConfirm = true;
      this.clearChildren();
      this.init();
   }

   private void onConfirmJoin() {
      if (this.targetEntity != null) {
         RequestJoinFactionC2SPacket.send(this.targetEntity.getId());
      }

      this.close();
   }

   private void onCancelJoin() {
      this.currentDialogue = this.getRandomDialogue();
      this.showingJoinConfirm = false;
      this.clearChildren();
      this.init();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
      if (this.hasPortraitTexture()) {
         int portraitWidth = this.width / 4;
         int portraitHeight = portraitWidth * 2;
         int portraitX = this.width - portraitWidth - 20;
         int portraitY = this.height - portraitHeight;
         context.drawTexture(this.portraitTexture, portraitX, portraitY, 0.0F, 0.0F, portraitWidth, portraitHeight, portraitWidth, portraitHeight);
      }

      int blackBarHeight = 100;
      int blackBarY = this.height - blackBarHeight;
      context.fill(0, blackBarY, this.width, this.height, -872415232);
      int characterNameX = 20;
      int characterNameY = blackBarY + 10;
      context.drawTextWithShadow(this.textRenderer, this.characterName, characterNameX, characterNameY, 16777215);
      int textX = 20;
      int textY = blackBarY + 35;
      context.drawText(this.textRenderer, Text.literal(this.currentDialogue), textX, textY, -1, false);
      super.render(context, mouseX, mouseY, delta);
   }

   private boolean hasPortraitTexture() {
      ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
      return resourceManager.getResource(this.portraitTexture).isPresent();
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public void close() {
      super.close();
   }

   public Entity getTargetEntity() {
      return this.targetEntity;
   }
}
