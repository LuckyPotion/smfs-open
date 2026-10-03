package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.data.PlayerRoyalCurseManager;
import com.xie.smfs.event.screen.GhostControlScreenHandler;
import com.xie.smfs.item.BaseGhostEyeItem;
import com.xie.smfs.util.GhostUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostControlScreen extends HandledScreen<GhostControlScreenHandler> {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostControlScreen");
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/ghost_control_background.png");
   private static final Identifier BACKGROUND_TEXTURE2 = new Identifier("smfs", "textures/gui/ghost_control_background2.png");
   private static final Identifier SLOT_TEXTURE = new Identifier("smfs", "textures/gui/slot.png");
   private static final Identifier LOCK_TEXTURE = new Identifier("smfs", "textures/gui/lock.png");
   private static final int TEXTURE_WIDTH = 177;
   private static final int TEXTURE_HEIGHT = 183;
   private static final int CIRCLE_CENTER_X = 87;
   private static final int CIRCLE_CENTER_Y = 73;
   private static final int CIRCLE_RADIUS = 30;
   private static final int SLOT_SIZE = 18;
   private static final int SLOT_OFFSET = 8;
   private static final int ATTRIBUTE_LEFT_COLUMN = 20;
   private static final int ATTRIBUTE_RIGHT_COLUMN = 90;
   private static final int ATTRIBUTE_START_Y = 120;
   private static final int ATTRIBUTE_SPACING = 10;
   private static final int TEXT_COLOR = 16777215;
   private static final int VALUE_COLOR = 8900331;
   private static final int LABEL_WIDTH = 50;
   private static final int GHOST_CHILD_BUTTON_X = 200;
   private static final int GHOST_CHILD_BUTTON_Y = 113;
   private static final int GHOST_CHILD_BUTTON_WIDTH = 25;
   private static final int GHOST_CHILD_BUTTON_HEIGHT = 25;
   private static final int INFO_BUTTON_X = 200;
   private static final int INFO_BUTTON_Y = 148;
   private static final int INFO_BUTTON_WIDTH = 25;
   private static final int INFO_BUTTON_HEIGHT = 25;
   private static final int ROYAL_CURSE_BUTTON_X = 200;
   private static final int ROYAL_CURSE_BUTTON_Y = 78;
   private static final int ROYAL_CURSE_BUTTON_WIDTH = 25;
   private static final int ROYAL_CURSE_BUTTON_HEIGHT = 25;
   private static final int CONTROL_BUTTON_X = 200;
   private static final int CONTROL_BUTTON_Y = 43;
   private static final int CONTROL_BUTTON_WIDTH = 25;
   private static final int CONTROL_BUTTON_HEIGHT = 25;
   private static final int TASK_BUTTON_X = 200;
   private static final int TASK_BUTTON_Y = 8;
   private static final int TASK_BUTTON_WIDTH = 25;
   private static final int TASK_BUTTON_HEIGHT = 25;

   public GhostControlScreen(GhostControlScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.field_2792 = 177;
      this.field_2779 = 183;
      this.field_25270 = -1000;
   }

   protected void method_25426() {
      super.method_25426();
      this.field_2776 = (this.field_22789 - this.field_2792) / 2;
      this.field_2800 = (this.field_22790 - this.field_2779) / 2;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      LOGGER.debug("界面开始渲染 - 鼠标位置: ({}, {})", mouseX, mouseY);
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      this.drawGhostChildButton(context, mouseX, mouseY);
      this.drawInfoButton(context, mouseX, mouseY);
      this.drawTaskButton(context, mouseX, mouseY);
      this.drawControlButton(context, mouseX, mouseY);
      this.drawRoyalCurseButton(context, mouseX, mouseY);
      this.method_2380(context, mouseX, mouseY);
   }

   private boolean isPlayerAberration() {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         WorldConfig worldConfig = WorldConfig.loadDefault();
         if ("linear".equals(worldConfig.endingMode)) {
            return false;
         }

         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
         String slot0Key = "Slot0";
         String slot3Key = "Slot3";
         boolean slot0Unlocked = false;
         boolean slot3Unlocked = false;
         if (ghostSlots.method_10545(slot0Key)) {
            NbtCompound slotData = ghostSlots.method_10562(slot0Key);
            slot0Unlocked = slotData.method_10577("unlocked");
         }

         if (ghostSlots.method_10545(slot3Key)) {
            NbtCompound slotData = ghostSlots.method_10562(slot3Key);
            slot3Unlocked = slotData.method_10577("unlocked");
         }

         return slot0Unlocked && slot3Unlocked;
      } else {
         return false;
      }
   }

   protected void method_2389(DrawContext context, float delta, int mouseX, int mouseY) {
      LOGGER.debug("绘制背景 - delta: {}, 鼠标: ({}, {})", delta, mouseX, mouseY);
      Identifier backgroundToUse = this.isPlayerAberration() ? BACKGROUND_TEXTURE : BACKGROUND_TEXTURE2;
      context.method_25302(backgroundToUse, this.field_2776, this.field_2800, 0, 0, this.field_2792, this.field_2779);
      this.drawGhostSlotsBackground(context);
   }

   private void drawGhostSlotsBackground(DrawContext context) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         int centerX = this.field_2776 + 87;
         int centerY = this.field_2800 + 73;
         LOGGER.debug("开始绘制槽位，玩家: {}", this.field_22787.field_1724.method_5477().getString());

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int x = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int y = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
            String slotKey = "Slot" + i;
            boolean isUnlocked = true;
            if (ghostSlots.method_10545(slotKey)) {
               NbtCompound slotData = ghostSlots.method_10562(slotKey);
               isUnlocked = slotData.method_10577("unlocked");
            }

            if (!isUnlocked) {
               context.method_25302(LOCK_TEXTURE, x, y, 0, 0, 18, 18);
            } else {
               ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, i);
               boolean isOccupied = PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, i);
               if (isOccupied && !itemStack.method_7960()) {
                  context.method_51427(itemStack, x + 1, y + 1);
                  context.method_51431(this.field_22793, itemStack, x + 1, y + 1);
                  int level = PlayerEvents.getGhostSlotLevel(this.field_22787.field_1724, i);
                  String levelText = "Lv." + level;
                  int textWidth = this.field_22793.method_1727(levelText);
                  context.method_51448().method_22903();
                  float scale = 0.5F;
                  context.method_51448().method_46416(x + (18.0F - textWidth * scale) / 2.0F, y + 18 + 2, 0.0F);
                  context.method_51448().method_22905(scale, scale, 1.0F);
                  int bgPadding = 1;
                  context.method_25294(-bgPadding, -bgPadding, textWidth + bgPadding * 2, 9 + bgPadding * 2, -2130706433);
                  context.method_51433(this.field_22793, levelText, 0, 0, 8388736, false);
                  context.method_51448().method_22909();
                  this.drawSlotRevivalBar(context, x, y, i);
               }
            }
         }

         if (this.isPlayerAberration()) {
            int[] customX = new int[]{32, 129, 21, 140};
            int[] customY = new int[]{29, 29, 73, 73};

            for (int i = 6; i < 10; i++) {
               int index = i - 6;
               int x = this.field_2776 + customX[index];
               int y = this.field_2800 + customY[index];
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
               String slotKey = "Slot" + i;
               boolean isUnlocked = false;
               if (ghostSlots.method_10545(slotKey)) {
                  NbtCompound slotData = ghostSlots.method_10562(slotKey);
                  isUnlocked = slotData.method_10577("unlocked");
               }

               if (!isUnlocked) {
                  context.method_25302(LOCK_TEXTURE, x, y, 0, 0, 18, 18);
               } else {
                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, i);
                  boolean isOccupied = PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, i);
                  if (isOccupied && !itemStack.method_7960()) {
                     context.method_51427(itemStack, x + 1, y + 1);
                     context.method_51431(this.field_22793, itemStack, x + 1, y + 1);
                     int level = PlayerEvents.getGhostSlotLevel(this.field_22787.field_1724, i);
                     String levelText = "Lv." + level;
                     int textWidth = this.field_22793.method_1727(levelText);
                     context.method_51448().method_22903();
                     float scale = 0.5F;
                     context.method_51448().method_46416(x + (18.0F - textWidth * scale) / 2.0F, y + 18 + 2, 0.0F);
                     context.method_51448().method_22905(scale, scale, 1.0F);
                     int bgPadding = 1;
                     context.method_25294(-bgPadding, -bgPadding, textWidth + bgPadding * 2, 9 + bgPadding * 2, -2130706433);
                     context.method_51433(this.field_22793, levelText, 0, 0, 8388736, false);
                     context.method_51448().method_22909();
                     this.drawSlotRevivalBar(context, x, y, i);
                  }
               }
            }
         }
      }
   }

   private void drawSlotRevivalBar(DrawContext context, int slotX, int slotY, int slotIndex) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
         String slotKey = "Slot" + slotIndex;
         if (ghostSlots.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlots.method_10562(slotKey);
            if (slotData.method_10577("occupied")) {
               int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(this.field_22787.field_1724, slotIndex);
               int requiredRevivalDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(this.field_22787.field_1724, slotIndex);
               LOGGER.debug("槽位 {}: 复苏程度={}, 最大复苏={}", slotIndex, revivalDegree, requiredRevivalDegree);
               if (requiredRevivalDegree > 0) {
                  float progress = (float)revivalDegree / requiredRevivalDegree;
                  int barWidth = 16;
                  int barHeight = 2;
                  int barX = slotX + 1;
                  int barY = slotY + 18 - 3;
                  context.method_25294(barX, barY, barX + barWidth, barY + barHeight, -16777216);
                  int progressWidth = (int)(barWidth * progress);
                  if (progressWidth > 0) {
                     context.method_25294(barX, barY, barX + progressWidth, barY + barHeight, -16711936);
                  }
               }
            }
         }
      }
   }

   private NbtCompound getGhostSlots(PlayerEntity player) {
      return PlayerEvents.getGhostSlots(player);
   }

   protected void method_2388(DrawContext context, int mouseX, int mouseY) {
      PlayerEntity player = this.field_22787.field_1724;
      if (player != null) {
         float spiritResistance = this.getSpiritAttribute(player, "spiritResistance", 0.0F);
         float tempSpiritResistance = this.getSpiritAttribute(player, "tempSpiritResistance", 0.0F);
         float spiritDamage = this.getSpiritAttribute(player, "spiritDamage", 0.0F);
         float tempSpiritDamage = this.getSpiritAttribute(player, "tempSpiritDamage", 0.0F);
         float currentSpirit = this.getSpiritAttribute(player, "currentSpirit", 0.0F);
         float maxSpirit = this.getSpiritAttribute(player, "maxSpirit", 0.0F);
         float tempMaxSpirit = this.getSpiritAttribute(player, "tempMaxSpirit", 0.0F);
         float revivalFactor = this.getSpiritAttribute(player, "revivalFactor", 0.0F);
         float sanity = this.getSpiritAttribute(player, "sanity", 100.0F);
         float tempSanity = this.getSpiritAttribute(player, "tempSanity", 0.0F);
         float maxSanity = this.getSpiritAttribute(player, "maxSanity", 100.0F);
         float maxHealth = this.getSpiritAttribute(player, "maxHealth", 20.0F);
         float totalSpiritResistance = spiritResistance + tempSpiritResistance;
         float totalSpiritDamage = spiritDamage + tempSpiritDamage;
         float totalMaxSpirit = maxSpirit + tempMaxSpirit;
         float totalSanity = sanity + tempSanity;
         this.drawAttributeText(context, "最大理智值:", String.format("%.0f", maxSanity), 20, 120);
         this.drawAttributeText(context, "当前理智值:", String.format("%.0f", totalSanity), 20, 130);
         this.drawAttributeText(context, "灵异抗性:", String.format("%.0f", totalSpiritResistance), 20, 140);
         this.drawAttributeText(context, "复苏因子:", String.format("%.0f%%", revivalFactor), 20, 150);
         this.drawAttributeText(context, "最大灵异强度:", String.format("%.0f", totalMaxSpirit), 90, 120);
         this.drawAttributeText(context, "当前灵异强度:", String.format("%.0f", currentSpirit), 90, 130);
         this.drawAttributeText(context, "灵异力量:", String.format("%.0f", totalSpiritDamage), 90, 140);
         this.drawAttributeText(context, "最大生命值:", String.format("%.0f", maxHealth), 90, 150);
      }
   }

   private float getSpiritAttribute(PlayerEntity player, String attributeName, float defaultValue) {
      try {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         if (spiritAttributes.method_10545(attributeName)) {
            if (spiritAttributes.method_10573(attributeName, 3)) {
               return spiritAttributes.method_10550(attributeName);
            }

            if (spiritAttributes.method_10573(attributeName, 6)) {
               return (float)spiritAttributes.method_10574(attributeName);
            }

            return spiritAttributes.method_10583(attributeName);
         }
      } catch (Exception e) {
         LOGGER.error("获取灵异属性 {} 时出错: {}", attributeName, e.getMessage());
         LOGGER.warn("由于错误，属性 {} 使用默认值: {}", attributeName, defaultValue);
      }

      return defaultValue;
   }

   private void drawAttributeText(DrawContext context, String label, String value, int x, int y) {
      context.method_51448().method_22903();
      float scale = 0.7F;
      context.method_51448().method_46416(x, y, 0.0F);
      context.method_51448().method_22905(scale, scale, 1.0F);
      int labelWidth = this.field_22793.method_1727(label);
      int valueWidth = this.field_22793.method_1727(value);
      int maxLabelWidth = this.calculateMaxLabelWidth();
      int totalAvailableWidth = maxLabelWidth + 5 + valueWidth;
      float charSpacing = 0.0F;
      if (label.length() > 1) {
         int extraSpace = maxLabelWidth - labelWidth;
         charSpacing = (float)extraSpace / (label.length() - 1);
      }

      if (charSpacing > 0.0F) {
         for (int i = 0; i < label.length(); i++) {
            String charStr = String.valueOf(label.charAt(i));
            int charWidth = this.field_22793.method_1727(charStr);
            int charX = (int)(i * charSpacing);
            if (i > 0) {
               charX += this.field_22793.method_1727(label.substring(0, i));
            }

            context.method_51439(this.field_22793, Text.method_43470(charStr), charX, 0, 16777215, false);
         }
      } else {
         context.method_51439(this.field_22793, Text.method_43470(label), 0, 0, 16777215, false);
      }

      int valueX = maxLabelWidth + 5;
      context.method_51439(this.field_22793, Text.method_43470(value), valueX, 0, 8900331, false);
      context.method_51448().method_22909();
   }

   protected void method_2380(DrawContext context, int x, int y) {
      super.method_2380(context, x, y);
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         int centerX = this.field_2776 + 87;
         int centerY = this.field_2800 + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int slotX = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int slotY = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (x >= slotX && x <= slotX + 18 && y >= slotY && y <= slotY + 18) {
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
               String slotKey = "Slot" + i;
               boolean isUnlocked = true;
               if (ghostSlots.method_10545(slotKey)) {
                  NbtCompound slotData = ghostSlots.method_10562(slotKey);
                  isUnlocked = slotData.method_10577("unlocked");
               }

               if (isUnlocked) {
                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, i);
                  if (!itemStack.method_7960()) {
                     int level = PlayerEvents.getGhostSlotLevel(this.field_22787.field_1724, i);
                     if (ghostSlots.method_10545(slotKey)) {
                        int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(this.field_22787.field_1724, i);
                        int requiredRevivalDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(this.field_22787.field_1724, i);
                        Text levelInfo = Text.method_43470("等级: " + level + " | 复苏程度: " + revivalDegree + "/" + requiredRevivalDegree)
                           .method_27692(Formatting.field_1064);
                        context.method_51438(this.field_22793, levelInfo, x, y + 20);
                        if (itemStack.method_7909() instanceof BaseGhostEyeItem ghostEyeItem) {
                           List<Text> bonusTexts = new ArrayList<>();
                           String ghostName = itemStack.method_7964().getString();
                           bonusTexts.add(Text.method_43470(ghostName + "属性加成:"));
                           float levelBonusMultiplier;
                           if (level < 10) {
                              levelBonusMultiplier = 0.5F + (level - 1) * 0.05F;
                           } else {
                              levelBonusMultiplier = 1.0F;
                           }

                           if (levelBonusMultiplier > 1.0F) {
                              levelBonusMultiplier = 1.0F;
                           }

                           if (ghostEyeItem.getMaxSpiritBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getMaxSpiritBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 灵异强度: +" + currentBonus + "（" + ghostEyeItem.getMaxSpiritBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getSpiritResistanceBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritResistanceBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 灵异抗性: +" + currentBonus + "（" + ghostEyeItem.getSpiritResistanceBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getSpiritDamageBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritDamageBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 灵异力量: +" + currentBonus + "（" + ghostEyeItem.getSpiritDamageBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getSanityBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSanityBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 玩家理智: +" + currentBonus + "（" + ghostEyeItem.getSanityBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getRevivalFactor() > 0.0) {
                              float currentBonus = (float)(ghostEyeItem.getRevivalFactor() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470(
                                       "  • 复苏因子: +"
                                          + String.format("%.1f%%", currentBonus)
                                          + "（"
                                          + String.format("%.1f%%", ghostEyeItem.getRevivalFactor())
                                          + "）"
                                    )
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           bonusTexts.add(Text.method_43470("• 点击查看详情").method_27692(Formatting.field_1063));
                           context.method_51434(this.field_22793, bonusTexts, x, y + 40);
                        } else {
                           Text ghostName = itemStack.method_7964().method_27661().method_27692(Formatting.field_1075);
                           context.method_51438(this.field_22793, ghostName, x, y + 40);
                        }
                     }
                  } else {
                     context.method_51438(this.field_22793, Text.method_43470("槽位 " + (i + 1) + " (空闲)"), x, y);
                  }

                  return;
               }

               if (i != 0 && i != 3) {
                  context.method_51438(this.field_22793, Text.method_43470("槽位 " + (i + 1) + " (未解锁)"), x, y);
               } else {
                  context.method_51438(this.field_22793, Text.method_43470("成为异类后解锁"), x, y);
               }

               return;
            }
         }

         if (this.isPlayerAberration()) {
            int[] customX = new int[]{32, 129, 21, 140};
            int[] customY = new int[]{29, 29, 73, 73};

            for (int i = 6; i < 10; i++) {
               int index = i - 6;
               int slotX = this.field_2776 + customX[index];
               int slotY = this.field_2800 + customY[index];
               if (x >= slotX && x <= slotX + 18 && y >= slotY && y <= slotY + 18) {
                  NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
                  String slotKey = "Slot" + i;
                  boolean isUnlocked = false;
                  if (ghostSlots.method_10545(slotKey)) {
                     NbtCompound slotData = ghostSlots.method_10562(slotKey);
                     isUnlocked = slotData.method_10577("unlocked");
                  }

                  if (!isUnlocked) {
                     context.method_51438(this.field_22793, Text.method_43470("与鬼童融合后解锁"), x, y);
                     return;
                  }

                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, i);
                  if (!itemStack.method_7960()) {
                     int level = PlayerEvents.getGhostSlotLevel(this.field_22787.field_1724, i);
                     if (ghostSlots.method_10545(slotKey)) {
                        int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(this.field_22787.field_1724, i);
                        int requiredRevivalDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(this.field_22787.field_1724, i);
                        Text levelInfo = Text.method_43470("等级: " + level + " | 复苏程度: " + revivalDegree + "/" + requiredRevivalDegree)
                           .method_27692(Formatting.field_1064);
                        context.method_51438(this.field_22793, levelInfo, x, y + 20);
                        if (itemStack.method_7909() instanceof BaseGhostEyeItem ghostEyeItem) {
                           List<Text> bonusTexts = new ArrayList<>();
                           String ghostName = itemStack.method_7964().getString();
                           bonusTexts.add(Text.method_43470(ghostName + "属性加成:"));
                           float levelBonusMultiplier;
                           if (level < 10) {
                              levelBonusMultiplier = 0.5F + (level - 1) * 0.05F;
                           } else {
                              levelBonusMultiplier = 1.0F;
                           }

                           if (levelBonusMultiplier > 1.0F) {
                              levelBonusMultiplier = 1.0F;
                           }

                           if (ghostEyeItem.getMaxSpiritBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getMaxSpiritBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 灵异强度: +" + currentBonus + "（" + ghostEyeItem.getMaxSpiritBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getSpiritResistanceBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritResistanceBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 灵异抗性: +" + currentBonus + "（" + ghostEyeItem.getSpiritResistanceBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getSpiritDamageBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritDamageBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 灵异力量: +" + currentBonus + "（" + ghostEyeItem.getSpiritDamageBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getSanityBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSanityBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470("  • 玩家理智: +" + currentBonus + "（" + ghostEyeItem.getSanityBonus() + "）")
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           if (ghostEyeItem.getRevivalFactor() > 0.0) {
                              float currentBonus = (float)(ghostEyeItem.getRevivalFactor() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.method_43470(
                                       "  • 复苏因子: +"
                                          + String.format("%.1f%%", currentBonus)
                                          + "（"
                                          + String.format("%.1f%%", ghostEyeItem.getRevivalFactor())
                                          + "）"
                                    )
                                    .method_27692(Formatting.field_1060)
                              );
                           }

                           bonusTexts.add(Text.method_43470("• 点击查看详情").method_27692(Formatting.field_1063));
                           context.method_51434(this.field_22793, bonusTexts, x, y + 40);
                        } else {
                           Text ghostName = itemStack.method_7964().method_27661().method_27692(Formatting.field_1075);
                           context.method_51438(this.field_22793, ghostName, x, y + 40);
                        }
                     }
                  } else {
                     context.method_51438(this.field_22793, Text.method_43470("槽位 " + (i + 1) + " (空闲)"), x, y);
                  }

                  return;
               }
            }
         }
      }
   }

   private int calculateMaxLabelWidth() {
      String[] labels = new String[]{"灵异抗性:", "最大灵异强度:", "当前灵异强度:", "复苏因子:", "灵异力量:", "最大理智值:", "当前理智值:", "最大生命值:"};
      int maxWidth = 0;

      for (String label : labels) {
         int width = this.field_22793.method_1727(label);
         if (width > maxWidth) {
            maxWidth = width;
         }
      }

      return maxWidth;
   }

   private void drawGhostChildButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.field_2776 + 200;
      int buttonY = this.field_2800 + 113;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.method_25294(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.method_49601(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = this.isPlayerGod() ? "神" : "鬼童";
      int textWidth = this.field_22793.method_1727(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      int textColor = this.isPlayerGod() ? -10496 : 16777215;
      context.method_51433(this.field_22793, buttonText, textX, textY, textColor, false);
   }

   private boolean isPlayerGod() {
      MinecraftClient client = MinecraftClient.method_1551();
      if (client.field_1724 != null) {
         try {
            NbtCompound data = PlayerEvents.getCachedData(client.field_1724);
            if (data != null && data.method_10545("GhostSlots")) {
               NbtCompound ghostSlots = data.method_10562("GhostSlots");
               if (ghostSlots.method_10545("Slot6")) {
                  return ghostSlots.method_10562("Slot6").method_10577("unlocked");
               }
            }
         } catch (Exception var4) {
         }
      }

      return false;
   }

   private void drawTaskButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.field_2776 + 200;
      int buttonY = this.field_2800 + 8;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.method_25294(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.method_49601(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "任务";
      int textWidth = this.field_22793.method_1727(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.method_51433(this.field_22793, buttonText, textX, textY, 16777215, false);
   }

   private void drawControlButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.field_2776 + 200;
      int buttonY = this.field_2800 + 43;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.method_25294(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.method_49601(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "驾驭";
      int textWidth = this.field_22793.method_1727(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.method_51433(this.field_22793, buttonText, textX, textY, 16777215, false);
   }

   private void drawRoyalCurseButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.field_2776 + 200;
      int buttonY = this.field_2800 + 78;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.method_25294(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.method_49601(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "诅咒";
      int textWidth = this.field_22793.method_1727(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.method_51433(this.field_22793, buttonText, textX, textY, 16777215, false);
   }

   private void drawInfoButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.field_2776 + 200;
      int buttonY = this.field_2800 + 148;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.method_25294(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.method_49601(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "信息";
      int textWidth = this.field_22793.method_1727(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.method_51433(this.field_22793, buttonText, textX, textY, 16777215, false);
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (button == 0 && this.field_22787 != null && this.field_22787.field_1724 != null) {
         int buttonX = this.field_2776 + 200;
         int buttonY = this.field_2800 + 113;
         if (mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25) {
            if (this.isPlayerGod()) {
               this.field_22787.method_1507(new GodScreen());
               return true;
            }

            this.field_22787.field_1724.field_3944.method_45730("xie gui ghostchild");
            this.method_25419();
            return true;
         }

         int infoButtonX = this.field_2776 + 200;
         int infoButtonY = this.field_2800 + 148;
         if (mouseX >= infoButtonX && mouseX <= infoButtonX + 25 && mouseY >= infoButtonY && mouseY <= infoButtonY + 25) {
            this.method_25419();
            if (this.field_22787 != null) {
               this.field_22787.method_1507(new PlayerInfoScreen(this.field_22787.field_1724));
            }

            return true;
         }

         int taskButtonX = this.field_2776 + 200;
         int taskButtonY = this.field_2800 + 8;
         if (mouseX >= taskButtonX && mouseX <= taskButtonX + 25 && mouseY >= taskButtonY && mouseY <= taskButtonY + 25) {
            this.field_22787.field_1724.field_3944.method_45730("xie gui quest");
            this.method_25419();
            return true;
         }

         int controlButtonX = this.field_2776 + 200;
         int controlButtonY = this.field_2800 + 43;
         if (mouseX >= controlButtonX && mouseX <= controlButtonX + 25 && mouseY >= controlButtonY && mouseY <= controlButtonY + 25) {
            this.field_22787.field_1724.field_3944.method_45730("xie gui taming");
            this.method_25419();
            return true;
         }

         int royalCurseButtonX = this.field_2776 + 200;
         int royalCurseButtonY = this.field_2800 + 78;
         if (mouseX >= royalCurseButtonX && mouseX <= royalCurseButtonX + 25 && mouseY >= royalCurseButtonY && mouseY <= royalCurseButtonY + 25) {
            if (!PlayerRoyalCurseManager.hasRoyalCurseUnlocked(this.field_22787.field_1724)) {
               this.field_22787.field_1724.method_7353(Text.method_43470("§c你还未开启王家诅咒！"), true);
               this.method_25419();
               return true;
            }

            this.field_22787.field_1724.field_3944.method_45730("xie gui royalcurse");
            this.method_25419();
            return true;
         }

         int centerX = this.field_2776 + 87;
         int centerY = this.field_2800 + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int slotX = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int slotY = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (mouseX >= slotX && mouseX <= slotX + 18 && mouseY >= slotY && mouseY <= slotY + 18) {
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
               String slotKey = "Slot" + i;
               boolean isUnlocked = true;
               if (ghostSlots.method_10545(slotKey)) {
                  NbtCompound slotData = ghostSlots.method_10562(slotKey);
                  isUnlocked = slotData.method_10577("unlocked");
               }

               if (!isUnlocked) {
                  return super.method_25402(mouseX, mouseY, button);
               }

               if (PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, i)) {
                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, i);
                  if (!itemStack.method_7960()) {
                     String ghostType = PlayerEvents.getGhostTypeInSlot(this.field_22787.field_1724, i);
                     if (ghostType != null) {
                        String ghostName = GhostUtils.getGhostDisplayName(ghostType);
                        List<String> abilityDescriptions = GhostUtils.getGhostAbilityDescriptions(ghostType);
                        this.method_25419();
                        GhostAbilityPopupScreen.show(ghostName, abilityDescriptions);
                        return true;
                     }
                  }
               }

               return super.method_25402(mouseX, mouseY, button);
            }
         }

         if (this.isPlayerAberration()) {
            int[] customX = new int[]{32, 129, 21, 140};
            int[] customY = new int[]{29, 29, 73, 73};

            for (int i = 6; i < 10; i++) {
               int index = i - 6;
               int slotX = this.field_2776 + customX[index];
               int slotY = this.field_2800 + customY[index];
               if (mouseX >= slotX && mouseX <= slotX + 18 && mouseY >= slotY && mouseY <= slotY + 18) {
                  NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);
                  String slotKey = "Slot" + i;
                  boolean isUnlocked = false;
                  if (ghostSlots.method_10545(slotKey)) {
                     NbtCompound slotData = ghostSlots.method_10562(slotKey);
                     isUnlocked = slotData.method_10577("unlocked");
                  }

                  if (!isUnlocked) {
                     return super.method_25402(mouseX, mouseY, button);
                  }

                  if (PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, i)) {
                     ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, i);
                     if (!itemStack.method_7960()) {
                        String ghostType = PlayerEvents.getGhostTypeInSlot(this.field_22787.field_1724, i);
                        if (ghostType != null) {
                           String ghostName = GhostUtils.getGhostDisplayName(ghostType);
                           List<String> abilityDescriptions = GhostUtils.getGhostAbilityDescriptions(ghostType);
                           this.method_25419();
                           GhostAbilityPopupScreen.show(ghostName, abilityDescriptions);
                           return true;
                        }
                     }
                  }

                  return super.method_25402(mouseX, mouseY, button);
               }
            }
         }
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   public boolean method_25422() {
      LOGGER.info("ESC键按下，准备关闭界面");
      return true;
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(null);
      } else {
         LOGGER.warn("关闭界面失败: client为null");
      }
   }

   protected boolean method_2381(double mouseX, double mouseY, int left, int top, int button) {
      return mouseX < left || mouseY < top || mouseX >= left + this.field_2792 || mouseY >= top + this.field_2779;
   }

   public void method_37432() {
      super.method_37432();
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         this.checkDataConsistency();
      }
   }

   private void checkDataConsistency() {
      NbtCompound serverSlots = PlayerEvents.getGhostSlots(this.field_22787.field_1724);

      for (int i = 0; i < 10; i++) {
         ItemStack serverItem = PlayerEvents.getGhostSlotItem(this.field_22787.field_1724, i);
         boolean serverOccupied = PlayerEvents.isGhostSlotOccupied(this.field_22787.field_1724, i);
         LOGGER.debug(
            "2.槽位 {} - 服务器: occupied={}, item={}", i, serverOccupied, serverItem.method_7960() ? "空" : serverItem.method_7909().method_7848().getString()
         );
      }
   }
}
