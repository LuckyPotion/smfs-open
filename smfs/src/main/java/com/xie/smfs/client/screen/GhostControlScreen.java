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
      this.backgroundWidth = 177;
      this.backgroundHeight = 183;
      this.playerInventoryTitleY = -1000;
   }

   protected void init() {
      super.init();
      this.x = (this.width - this.backgroundWidth) / 2;
      this.y = (this.height - this.backgroundHeight) / 2;
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      LOGGER.debug("界面开始渲染 - 鼠标位置: ({}, {})", mouseX, mouseY);
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.drawGhostChildButton(context, mouseX, mouseY);
      this.drawInfoButton(context, mouseX, mouseY);
      this.drawTaskButton(context, mouseX, mouseY);
      this.drawControlButton(context, mouseX, mouseY);
      this.drawRoyalCurseButton(context, mouseX, mouseY);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   private boolean isPlayerAberration() {
      if (this.client != null && this.client.player != null) {
         WorldConfig worldConfig = WorldConfig.loadDefault();
         if ("linear".equals(worldConfig.endingMode)) {
            return false;
         }

         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
         String slot0Key = "Slot0";
         String slot3Key = "Slot3";
         boolean slot0Unlocked = false;
         boolean slot3Unlocked = false;
         if (ghostSlots.contains(slot0Key)) {
            NbtCompound slotData = ghostSlots.getCompound(slot0Key);
            slot0Unlocked = slotData.getBoolean("unlocked");
         }

         if (ghostSlots.contains(slot3Key)) {
            NbtCompound slotData = ghostSlots.getCompound(slot3Key);
            slot3Unlocked = slotData.getBoolean("unlocked");
         }

         return slot0Unlocked && slot3Unlocked;
      } else {
         return false;
      }
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      LOGGER.debug("绘制背景 - delta: {}, 鼠标: ({}, {})", delta, mouseX, mouseY);
      Identifier backgroundToUse = this.isPlayerAberration() ? BACKGROUND_TEXTURE : BACKGROUND_TEXTURE2;
      context.drawTexture(backgroundToUse, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
      this.drawGhostSlotsBackground(context);
   }

   private void drawGhostSlotsBackground(DrawContext context) {
      if (this.client != null && this.client.player != null) {
         int centerX = this.x + 87;
         int centerY = this.y + 73;
         LOGGER.debug("开始绘制槽位，玩家: {}", this.client.player.getName().getString());

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int x = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int y = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
            String slotKey = "Slot" + i;
            boolean isUnlocked = true;
            if (ghostSlots.contains(slotKey)) {
               NbtCompound slotData = ghostSlots.getCompound(slotKey);
               isUnlocked = slotData.getBoolean("unlocked");
            }

            if (!isUnlocked) {
               context.drawTexture(LOCK_TEXTURE, x, y, 0, 0, 18, 18);
            } else {
               ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.client.player, i);
               boolean isOccupied = PlayerEvents.isGhostSlotOccupied(this.client.player, i);
               if (isOccupied && !itemStack.isEmpty()) {
                  context.drawItem(itemStack, x + 1, y + 1);
                  context.drawItemInSlot(this.textRenderer, itemStack, x + 1, y + 1);
                  int level = PlayerEvents.getGhostSlotLevel(this.client.player, i);
                  String levelText = "Lv." + level;
                  int textWidth = this.textRenderer.getWidth(levelText);
                  context.getMatrices().push();
                  float scale = 0.5F;
                  context.getMatrices().translate(x + (18.0F - textWidth * scale) / 2.0F, y + 18 + 2, 0.0F);
                  context.getMatrices().scale(scale, scale, 1.0F);
                  int bgPadding = 1;
                  context.fill(-bgPadding, -bgPadding, textWidth + bgPadding * 2, 9 + bgPadding * 2, -2130706433);
                  context.drawText(this.textRenderer, levelText, 0, 0, 8388736, false);
                  context.getMatrices().pop();
                  this.drawSlotRevivalBar(context, x, y, i);
               }
            }
         }

         if (this.isPlayerAberration()) {
            int[] customX = new int[]{32, 129, 21, 140};
            int[] customY = new int[]{29, 29, 73, 73};

            for (int i = 6; i < 10; i++) {
               int index = i - 6;
               int x = this.x + customX[index];
               int y = this.y + customY[index];
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
               String slotKey = "Slot" + i;
               boolean isUnlocked = false;
               if (ghostSlots.contains(slotKey)) {
                  NbtCompound slotData = ghostSlots.getCompound(slotKey);
                  isUnlocked = slotData.getBoolean("unlocked");
               }

               if (!isUnlocked) {
                  context.drawTexture(LOCK_TEXTURE, x, y, 0, 0, 18, 18);
               } else {
                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.client.player, i);
                  boolean isOccupied = PlayerEvents.isGhostSlotOccupied(this.client.player, i);
                  if (isOccupied && !itemStack.isEmpty()) {
                     context.drawItem(itemStack, x + 1, y + 1);
                     context.drawItemInSlot(this.textRenderer, itemStack, x + 1, y + 1);
                     int level = PlayerEvents.getGhostSlotLevel(this.client.player, i);
                     String levelText = "Lv." + level;
                     int textWidth = this.textRenderer.getWidth(levelText);
                     context.getMatrices().push();
                     float scale = 0.5F;
                     context.getMatrices().translate(x + (18.0F - textWidth * scale) / 2.0F, y + 18 + 2, 0.0F);
                     context.getMatrices().scale(scale, scale, 1.0F);
                     int bgPadding = 1;
                     context.fill(-bgPadding, -bgPadding, textWidth + bgPadding * 2, 9 + bgPadding * 2, -2130706433);
                     context.drawText(this.textRenderer, levelText, 0, 0, 8388736, false);
                     context.getMatrices().pop();
                     this.drawSlotRevivalBar(context, x, y, i);
                  }
               }
            }
         }
      }
   }

   private void drawSlotRevivalBar(DrawContext context, int slotX, int slotY, int slotIndex) {
      if (this.client != null && this.client.player != null) {
         NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
         String slotKey = "Slot" + slotIndex;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(this.client.player, slotIndex);
               int requiredRevivalDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(this.client.player, slotIndex);
               LOGGER.debug("槽位 {}: 复苏程度={}, 最大复苏={}", slotIndex, revivalDegree, requiredRevivalDegree);
               if (requiredRevivalDegree > 0) {
                  float progress = (float)revivalDegree / requiredRevivalDegree;
                  int barWidth = 16;
                  int barHeight = 2;
                  int barX = slotX + 1;
                  int barY = slotY + 18 - 3;
                  context.fill(barX, barY, barX + barWidth, barY + barHeight, -16777216);
                  int progressWidth = (int)(barWidth * progress);
                  if (progressWidth > 0) {
                     context.fill(barX, barY, barX + progressWidth, barY + barHeight, -16711936);
                  }
               }
            }
         }
      }
   }

   private NbtCompound getGhostSlots(PlayerEntity player) {
      return PlayerEvents.getGhostSlots(player);
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      PlayerEntity player = this.client.player;
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
         if (spiritAttributes.contains(attributeName)) {
            if (spiritAttributes.contains(attributeName, 3)) {
               return spiritAttributes.getInt(attributeName);
            }

            if (spiritAttributes.contains(attributeName, 6)) {
               return (float)spiritAttributes.getDouble(attributeName);
            }

            return spiritAttributes.getFloat(attributeName);
         }
      } catch (Exception e) {
         LOGGER.error("获取灵异属性 {} 时出错: {}", attributeName, e.getMessage());
         LOGGER.warn("由于错误，属性 {} 使用默认值: {}", attributeName, defaultValue);
      }

      return defaultValue;
   }

   private void drawAttributeText(DrawContext context, String label, String value, int x, int y) {
      context.getMatrices().push();
      float scale = 0.7F;
      context.getMatrices().translate(x, y, 0.0F);
      context.getMatrices().scale(scale, scale, 1.0F);
      int labelWidth = this.textRenderer.getWidth(label);
      int valueWidth = this.textRenderer.getWidth(value);
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
            int charWidth = this.textRenderer.getWidth(charStr);
            int charX = (int)(i * charSpacing);
            if (i > 0) {
               charX += this.textRenderer.getWidth(label.substring(0, i));
            }

            context.drawText(this.textRenderer, Text.literal(charStr), charX, 0, 16777215, false);
         }
      } else {
         context.drawText(this.textRenderer, Text.literal(label), 0, 0, 16777215, false);
      }

      int valueX = maxLabelWidth + 5;
      context.drawText(this.textRenderer, Text.literal(value), valueX, 0, 8900331, false);
      context.getMatrices().pop();
   }

   protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
      super.drawMouseoverTooltip(context, x, y);
      if (this.client != null && this.client.player != null) {
         int centerX = this.x + 87;
         int centerY = this.y + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int slotX = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int slotY = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (x >= slotX && x <= slotX + 18 && y >= slotY && y <= slotY + 18) {
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
               String slotKey = "Slot" + i;
               boolean isUnlocked = true;
               if (ghostSlots.contains(slotKey)) {
                  NbtCompound slotData = ghostSlots.getCompound(slotKey);
                  isUnlocked = slotData.getBoolean("unlocked");
               }

               if (isUnlocked) {
                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.client.player, i);
                  if (!itemStack.isEmpty()) {
                     int level = PlayerEvents.getGhostSlotLevel(this.client.player, i);
                     if (ghostSlots.contains(slotKey)) {
                        int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(this.client.player, i);
                        int requiredRevivalDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(this.client.player, i);
                        Text levelInfo = Text.literal("等级: " + level + " | 复苏程度: " + revivalDegree + "/" + requiredRevivalDegree)
                           .formatted(Formatting.DARK_PURPLE);
                        context.drawTooltip(this.textRenderer, levelInfo, x, y + 20);
                        if (itemStack.getItem() instanceof BaseGhostEyeItem ghostEyeItem) {
                           List<Text> bonusTexts = new ArrayList<>();
                           String ghostName = itemStack.getName().getString();
                           bonusTexts.add(Text.literal(ghostName + "属性加成:"));
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
                                 Text.literal("  • 灵异强度: +" + currentBonus + "（" + ghostEyeItem.getMaxSpiritBonus() + "）").formatted(Formatting.GREEN)
                              );
                           }

                           if (ghostEyeItem.getSpiritResistanceBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritResistanceBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.literal("  • 灵异抗性: +" + currentBonus + "（" + ghostEyeItem.getSpiritResistanceBonus() + "）").formatted(Formatting.GREEN)
                              );
                           }

                           if (ghostEyeItem.getSpiritDamageBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritDamageBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.literal("  • 灵异力量: +" + currentBonus + "（" + ghostEyeItem.getSpiritDamageBonus() + "）").formatted(Formatting.GREEN)
                              );
                           }

                           if (ghostEyeItem.getSanityBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSanityBonus() * levelBonusMultiplier);
                              bonusTexts.add(Text.literal("  • 玩家理智: +" + currentBonus + "（" + ghostEyeItem.getSanityBonus() + "）").formatted(Formatting.GREEN));
                           }

                           if (ghostEyeItem.getRevivalFactor() > 0.0) {
                              float currentBonus = (float)(ghostEyeItem.getRevivalFactor() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.literal(
                                       "  • 复苏因子: +"
                                          + String.format("%.1f%%", currentBonus)
                                          + "（"
                                          + String.format("%.1f%%", ghostEyeItem.getRevivalFactor())
                                          + "）"
                                    )
                                    .formatted(Formatting.GREEN)
                              );
                           }

                           bonusTexts.add(Text.literal("• 点击查看详情").formatted(Formatting.DARK_GRAY));
                           context.drawTooltip(this.textRenderer, bonusTexts, x, y + 40);
                        } else {
                           Text ghostName = itemStack.getName().copy().formatted(Formatting.AQUA);
                           context.drawTooltip(this.textRenderer, ghostName, x, y + 40);
                        }
                     }
                  } else {
                     context.drawTooltip(this.textRenderer, Text.literal("槽位 " + (i + 1) + " (空闲)"), x, y);
                  }

                  return;
               }

               if (i != 0 && i != 3) {
                  context.drawTooltip(this.textRenderer, Text.literal("槽位 " + (i + 1) + " (未解锁)"), x, y);
               } else {
                  context.drawTooltip(this.textRenderer, Text.literal("成为异类后解锁"), x, y);
               }

               return;
            }
         }

         if (this.isPlayerAberration()) {
            int[] customX = new int[]{32, 129, 21, 140};
            int[] customY = new int[]{29, 29, 73, 73};

            for (int i = 6; i < 10; i++) {
               int index = i - 6;
               int slotX = this.x + customX[index];
               int slotY = this.y + customY[index];
               if (x >= slotX && x <= slotX + 18 && y >= slotY && y <= slotY + 18) {
                  NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
                  String slotKey = "Slot" + i;
                  boolean isUnlocked = false;
                  if (ghostSlots.contains(slotKey)) {
                     NbtCompound slotData = ghostSlots.getCompound(slotKey);
                     isUnlocked = slotData.getBoolean("unlocked");
                  }

                  if (!isUnlocked) {
                     context.drawTooltip(this.textRenderer, Text.literal("与鬼童融合后解锁"), x, y);
                     return;
                  }

                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.client.player, i);
                  if (!itemStack.isEmpty()) {
                     int level = PlayerEvents.getGhostSlotLevel(this.client.player, i);
                     if (ghostSlots.contains(slotKey)) {
                        int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(this.client.player, i);
                        int requiredRevivalDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(this.client.player, i);
                        Text levelInfo = Text.literal("等级: " + level + " | 复苏程度: " + revivalDegree + "/" + requiredRevivalDegree)
                           .formatted(Formatting.DARK_PURPLE);
                        context.drawTooltip(this.textRenderer, levelInfo, x, y + 20);
                        if (itemStack.getItem() instanceof BaseGhostEyeItem ghostEyeItem) {
                           List<Text> bonusTexts = new ArrayList<>();
                           String ghostName = itemStack.getName().getString();
                           bonusTexts.add(Text.literal(ghostName + "属性加成:"));
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
                                 Text.literal("  • 灵异强度: +" + currentBonus + "（" + ghostEyeItem.getMaxSpiritBonus() + "）").formatted(Formatting.GREEN)
                              );
                           }

                           if (ghostEyeItem.getSpiritResistanceBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritResistanceBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.literal("  • 灵异抗性: +" + currentBonus + "（" + ghostEyeItem.getSpiritResistanceBonus() + "）").formatted(Formatting.GREEN)
                              );
                           }

                           if (ghostEyeItem.getSpiritDamageBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSpiritDamageBonus() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.literal("  • 灵异力量: +" + currentBonus + "（" + ghostEyeItem.getSpiritDamageBonus() + "）").formatted(Formatting.GREEN)
                              );
                           }

                           if (ghostEyeItem.getSanityBonus() > 0) {
                              int currentBonus = (int)(ghostEyeItem.getSanityBonus() * levelBonusMultiplier);
                              bonusTexts.add(Text.literal("  • 玩家理智: +" + currentBonus + "（" + ghostEyeItem.getSanityBonus() + "）").formatted(Formatting.GREEN));
                           }

                           if (ghostEyeItem.getRevivalFactor() > 0.0) {
                              float currentBonus = (float)(ghostEyeItem.getRevivalFactor() * levelBonusMultiplier);
                              bonusTexts.add(
                                 Text.literal(
                                       "  • 复苏因子: +"
                                          + String.format("%.1f%%", currentBonus)
                                          + "（"
                                          + String.format("%.1f%%", ghostEyeItem.getRevivalFactor())
                                          + "）"
                                    )
                                    .formatted(Formatting.GREEN)
                              );
                           }

                           bonusTexts.add(Text.literal("• 点击查看详情").formatted(Formatting.DARK_GRAY));
                           context.drawTooltip(this.textRenderer, bonusTexts, x, y + 40);
                        } else {
                           Text ghostName = itemStack.getName().copy().formatted(Formatting.AQUA);
                           context.drawTooltip(this.textRenderer, ghostName, x, y + 40);
                        }
                     }
                  } else {
                     context.drawTooltip(this.textRenderer, Text.literal("槽位 " + (i + 1) + " (空闲)"), x, y);
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
         int width = this.textRenderer.getWidth(label);
         if (width > maxWidth) {
            maxWidth = width;
         }
      }

      return maxWidth;
   }

   private void drawGhostChildButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.x + 200;
      int buttonY = this.y + 113;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.fill(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.drawBorder(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = this.isPlayerGod() ? "神" : "鬼童";
      int textWidth = this.textRenderer.getWidth(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      int textColor = this.isPlayerGod() ? -10496 : 16777215;
      context.drawText(this.textRenderer, buttonText, textX, textY, textColor, false);
   }

   private boolean isPlayerGod() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         try {
            NbtCompound data = PlayerEvents.getCachedData(client.player);
            if (data != null && data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               if (ghostSlots.contains("Slot6")) {
                  return ghostSlots.getCompound("Slot6").getBoolean("unlocked");
               }
            }
         } catch (Exception var4) {
         }
      }

      return false;
   }

   private void drawTaskButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.x + 200;
      int buttonY = this.y + 8;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.fill(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.drawBorder(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "任务";
      int textWidth = this.textRenderer.getWidth(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.drawText(this.textRenderer, buttonText, textX, textY, 16777215, false);
   }

   private void drawControlButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.x + 200;
      int buttonY = this.y + 43;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.fill(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.drawBorder(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "驾驭";
      int textWidth = this.textRenderer.getWidth(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.drawText(this.textRenderer, buttonText, textX, textY, 16777215, false);
   }

   private void drawRoyalCurseButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.x + 200;
      int buttonY = this.y + 78;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.fill(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.drawBorder(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "诅咒";
      int textWidth = this.textRenderer.getWidth(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.drawText(this.textRenderer, buttonText, textX, textY, 16777215, false);
   }

   private void drawInfoButton(DrawContext context, int mouseX, int mouseY) {
      int buttonX = this.x + 200;
      int buttonY = this.y + 148;
      boolean isHovered = mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25;
      int backgroundColor = isHovered ? -2143272896 : -2145378272;
      context.fill(buttonX, buttonY, buttonX + 25, buttonY + 25, backgroundColor);
      int borderColor = isHovered ? -1 : -8355712;
      context.drawBorder(buttonX, buttonY, 25, 25, borderColor);
      String buttonText = "信息";
      int textWidth = this.textRenderer.getWidth(buttonText);
      int textX = buttonX + (25 - textWidth) / 2;
      int textY = buttonY + (25 - 9) / 2;
      context.drawText(this.textRenderer, buttonText, textX, textY, 16777215, false);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.client != null && this.client.player != null) {
         int buttonX = this.x + 200;
         int buttonY = this.y + 113;
         if (mouseX >= buttonX && mouseX <= buttonX + 25 && mouseY >= buttonY && mouseY <= buttonY + 25) {
            if (this.isPlayerGod()) {
               this.client.setScreen(new GodScreen());
               return true;
            }

            this.client.player.networkHandler.sendChatCommand("xie gui ghostchild");
            this.close();
            return true;
         }

         int infoButtonX = this.x + 200;
         int infoButtonY = this.y + 148;
         if (mouseX >= infoButtonX && mouseX <= infoButtonX + 25 && mouseY >= infoButtonY && mouseY <= infoButtonY + 25) {
            this.close();
            if (this.client != null) {
               this.client.setScreen(new PlayerInfoScreen(this.client.player));
            }

            return true;
         }

         int taskButtonX = this.x + 200;
         int taskButtonY = this.y + 8;
         if (mouseX >= taskButtonX && mouseX <= taskButtonX + 25 && mouseY >= taskButtonY && mouseY <= taskButtonY + 25) {
            this.client.player.networkHandler.sendChatCommand("xie gui quest");
            this.close();
            return true;
         }

         int controlButtonX = this.x + 200;
         int controlButtonY = this.y + 43;
         if (mouseX >= controlButtonX && mouseX <= controlButtonX + 25 && mouseY >= controlButtonY && mouseY <= controlButtonY + 25) {
            this.client.player.networkHandler.sendChatCommand("xie gui taming");
            this.close();
            return true;
         }

         int royalCurseButtonX = this.x + 200;
         int royalCurseButtonY = this.y + 78;
         if (mouseX >= royalCurseButtonX && mouseX <= royalCurseButtonX + 25 && mouseY >= royalCurseButtonY && mouseY <= royalCurseButtonY + 25) {
            if (!PlayerRoyalCurseManager.hasRoyalCurseUnlocked(this.client.player)) {
               this.client.player.sendMessage(Text.literal("§c你还未开启王家诅咒！"), true);
               this.close();
               return true;
            }

            this.client.player.networkHandler.sendChatCommand("xie gui royalcurse");
            this.close();
            return true;
         }

         int centerX = this.x + 87;
         int centerY = this.y + 73;

         for (int i = 0; i < 6; i++) {
            double angle = (Math.PI * 2) * i / 6.0;
            int slotX = centerX + (int)(30.0 * Math.cos(angle)) - 8;
            int slotY = centerY + (int)(30.0 * Math.sin(angle)) - 8;
            if (mouseX >= slotX && mouseX <= slotX + 18 && mouseY >= slotY && mouseY <= slotY + 18) {
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
               String slotKey = "Slot" + i;
               boolean isUnlocked = true;
               if (ghostSlots.contains(slotKey)) {
                  NbtCompound slotData = ghostSlots.getCompound(slotKey);
                  isUnlocked = slotData.getBoolean("unlocked");
               }

               if (!isUnlocked) {
                  return super.mouseClicked(mouseX, mouseY, button);
               }

               if (PlayerEvents.isGhostSlotOccupied(this.client.player, i)) {
                  ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.client.player, i);
                  if (!itemStack.isEmpty()) {
                     String ghostType = PlayerEvents.getGhostTypeInSlot(this.client.player, i);
                     if (ghostType != null) {
                        String ghostName = GhostUtils.getGhostDisplayName(ghostType);
                        List<String> abilityDescriptions = GhostUtils.getGhostAbilityDescriptions(ghostType);
                        this.close();
                        GhostAbilityPopupScreen.show(ghostName, abilityDescriptions);
                        return true;
                     }
                  }
               }

               return super.mouseClicked(mouseX, mouseY, button);
            }
         }

         if (this.isPlayerAberration()) {
            int[] customX = new int[]{32, 129, 21, 140};
            int[] customY = new int[]{29, 29, 73, 73};

            for (int i = 6; i < 10; i++) {
               int index = i - 6;
               int slotX = this.x + customX[index];
               int slotY = this.y + customY[index];
               if (mouseX >= slotX && mouseX <= slotX + 18 && mouseY >= slotY && mouseY <= slotY + 18) {
                  NbtCompound ghostSlots = PlayerEvents.getGhostSlots(this.client.player);
                  String slotKey = "Slot" + i;
                  boolean isUnlocked = false;
                  if (ghostSlots.contains(slotKey)) {
                     NbtCompound slotData = ghostSlots.getCompound(slotKey);
                     isUnlocked = slotData.getBoolean("unlocked");
                  }

                  if (!isUnlocked) {
                     return super.mouseClicked(mouseX, mouseY, button);
                  }

                  if (PlayerEvents.isGhostSlotOccupied(this.client.player, i)) {
                     ItemStack itemStack = PlayerEvents.getGhostSlotItem(this.client.player, i);
                     if (!itemStack.isEmpty()) {
                        String ghostType = PlayerEvents.getGhostTypeInSlot(this.client.player, i);
                        if (ghostType != null) {
                           String ghostName = GhostUtils.getGhostDisplayName(ghostType);
                           List<String> abilityDescriptions = GhostUtils.getGhostAbilityDescriptions(ghostType);
                           this.close();
                           GhostAbilityPopupScreen.show(ghostName, abilityDescriptions);
                           return true;
                        }
                     }
                  }

                  return super.mouseClicked(mouseX, mouseY, button);
               }
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean shouldCloseOnEsc() {
      LOGGER.info("ESC键按下，准备关闭界面");
      return true;
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(null);
      } else {
         LOGGER.warn("关闭界面失败: client为null");
      }
   }

   protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
      return mouseX < left || mouseY < top || mouseX >= left + this.backgroundWidth || mouseY >= top + this.backgroundHeight;
   }

   public void handledScreenTick() {
      super.handledScreenTick();
      if (this.client != null && this.client.player != null) {
         this.checkDataConsistency();
      }
   }

   private void checkDataConsistency() {
      NbtCompound serverSlots = PlayerEvents.getGhostSlots(this.client.player);

      for (int i = 0; i < 10; i++) {
         ItemStack serverItem = PlayerEvents.getGhostSlotItem(this.client.player, i);
         boolean serverOccupied = PlayerEvents.isGhostSlotOccupied(this.client.player, i);
         LOGGER.debug("2.槽位 {} - 服务器: occupied={}, item={}", i, serverOccupied, serverItem.isEmpty() ? "空" : serverItem.getItem().getName().getString());
      }
   }
}
