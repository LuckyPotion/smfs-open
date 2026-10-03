package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.faction.PlayerFaction;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerInfoScreen extends Screen {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/PlayerInfoScreen");
   private static final int TEXTURE_WIDTH = 300;
   private static final int TEXTURE_HEIGHT = 200;
   private static final int TEXT_COLOR = 16777215;
   private static final int VALUE_COLOR = 8900331;
   private static final int TITLE_COLOR = 16766720;
   private final PlayerEntity player;

   public PlayerInfoScreen(PlayerEntity player) {
      super(Text.method_43470("玩家信息"));
      this.player = player;
   }

   protected void method_25426() {
      super.method_25426();
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      int panelLeft = centerX - 150;
      int panelTop = centerY - 100;
      context.method_25294(panelLeft, panelTop, panelLeft + 300, panelTop + 200, -872415232);
      context.method_49601(panelLeft, panelTop, 300, 200, -11184811);
      int modelX = panelLeft + 100;
      int modelY = panelTop + 170;
      int modelSize = 50;
      this.drawPlayerModel(context, modelX, modelY, modelSize, mouseX, mouseY);
      int statsX = centerX;
      int statsY = panelTop + 25;
      Text titleText = Text.method_43470("玩家信息");
      context.method_51439(this.field_22793, titleText, panelLeft + 150 - this.field_22793.method_1727("玩家信息") / 2, panelTop + 8, 16766720, false);
      float maxHealth = this.player.method_6063();
      float currentHealth = this.player.method_6032();
      int armor = this.player.method_6096();
      NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(this.player);
      float spiritResistance = this.getAttr(spiritAttributes, "spiritResistance", 0.0F);
      float tempSpiritResistance = this.getAttr(spiritAttributes, "tempSpiritResistance", 0.0F);
      float spiritDamage = this.getAttr(spiritAttributes, "spiritDamage", 0.0F);
      float tempSpiritDamage = this.getAttr(spiritAttributes, "tempSpiritDamage", 0.0F);
      float currentSpirit = this.getAttr(spiritAttributes, "currentSpirit", 0.0F);
      float maxSpirit = this.getAttr(spiritAttributes, "maxSpirit", 0.0F);
      float tempMaxSpirit = this.getAttr(spiritAttributes, "tempMaxSpirit", 0.0F);
      float revivalFactor = this.getAttr(spiritAttributes, "revivalFactor", 0.0F);
      float sanity = this.getAttr(spiritAttributes, "sanity", 100.0F);
      float tempSanity = this.getAttr(spiritAttributes, "tempSanity", 0.0F);
      float maxSanity = this.getAttr(spiritAttributes, "maxSanity", 100.0F);
      float totalSpiritResistance = spiritResistance + tempSpiritResistance;
      float totalSpiritDamage = spiritDamage + tempSpiritDamage;
      float totalMaxSpirit = maxSpirit + tempMaxSpirit;
      float totalSanity = sanity + tempSanity;
      PlayerFaction faction = FactionManager.getFaction(this.player);
      int reputation = FactionManager.getReputation(this.player);
      int goldSalary = FactionManager.getGoldSalary(this.player);
      String factionName = faction.getDisplayName();
      String codename = FactionManager.getCodename(this.player);
      String displayCodename = codename != null && !codename.isEmpty() ? codename : "暂无";
      long survivalTicks = PlayerEvents.getSurvivalTime(this.player);
      long totalTicks = PlayerEvents.getTotalTime(this.player);
      String survivalDays = String.format("%.1f", survivalTicks / 24000.0);
      String totalDays = String.format("%.1f", totalTicks / 24000.0);
      String playerName = this.player.method_5477().getString();
      int lineHeight = 12;
      int maxLabelWidth = this.calculateMaxLabelWidth();
      int gap = 5;
      this.drawInfoRow(context, statsX, statsY, "玩家名称:", playerName, maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight, "代号:", displayCodename, maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 2, "阵营:", factionName, maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 3, "声望:", String.valueOf(reputation), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 4, "黄金薪资:", String.valueOf(goldSalary), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 5, "存活时长:", survivalDays + " / " + totalDays + "天", maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 6, "生命值:", String.format("%.0f / %.0f", currentHealth, maxHealth), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 7, "护甲值:", String.valueOf(armor), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 8, "灵异抗性:", String.format("%.0f", totalSpiritResistance), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 9, "灵异力量:", String.format("%.0f", totalSpiritDamage), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 10, "灵异强度:", String.format("%.0f / %.0f", currentSpirit, totalMaxSpirit), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 11, "理智值:", String.format("%.0f / %.0f", totalSanity, maxSanity), maxLabelWidth, gap);
      this.drawInfoRow(context, statsX, statsY + lineHeight * 12, "复苏因子:", String.format("%.0f%%", revivalFactor), maxLabelWidth, gap);
   }

   private int calculateMaxLabelWidth() {
      String[] labels = new String[]{"玩家名称:", "代号:", "阵营:", "声望:", "黄金薪资:", "存活时长:", "生命值:", "护甲值:", "灵异抗性:", "灵异力量:", "灵异强度:", "理智值:", "复苏因子:"};
      int max = 0;

      for (String label : labels) {
         int w = this.field_22793.method_1727(label);
         if (w > max) {
            max = w;
         }
      }

      return max;
   }

   private void drawInfoRow(DrawContext context, int x, int y, String label, String value, int maxLabelWidth, int gap) {
      int labelWidth = this.field_22793.method_1727(label);
      if (labelWidth < maxLabelWidth && label.length() > 1) {
         float charSpacing = (float)(maxLabelWidth - labelWidth) / (label.length() - 1);

         for (int i = 0; i < label.length(); i++) {
            String charStr = String.valueOf(label.charAt(i));
            int charX = (int)(i * charSpacing);
            if (i > 0) {
               charX += this.field_22793.method_1727(label.substring(0, i));
            }

            context.method_51439(this.field_22793, Text.method_43470(charStr), x + charX, y, 16777215, false);
         }
      } else {
         context.method_51439(this.field_22793, Text.method_43470(label), x, y, 16777215, false);
      }

      context.method_51439(this.field_22793, Text.method_43470(value), x + maxLabelWidth + gap, y, 8900331, false);
   }

   private void drawPlayerModel(DrawContext context, int x, int y, int size, int mouseX, int mouseY) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         InventoryScreen.method_2486(
            context, x - size / 2, y - size / 2, size, (float)(x - size / 2) - mouseX, (float)(y - size / 2 - 50) - mouseY, this.field_22787.field_1724
         );
      }
   }

   private float getAttr(NbtCompound attrs, String key, float defaultValue) {
      if (attrs == null) {
         return defaultValue;
      } else if (attrs.method_10573(key, 3)) {
         return attrs.method_10550(key);
      } else if (attrs.method_10573(key, 5)) {
         return attrs.method_10583(key);
      } else {
         return attrs.method_10573(key, 6) ? (float)attrs.method_10574(key) : defaultValue;
      }
   }

   public boolean method_25421() {
      return false;
   }
}
