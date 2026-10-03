package com.xie.smfs.client.screen;

import com.xie.smfs.network.packets.common.c2s.SpearModeConfigC2SPacket;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class SpearConfigScreen extends Screen {
   private String currentMode;
   private final boolean isWishSpear;
   private String currentWishPreset;
   private boolean showWishText;
   private static final int BUTTON_WIDTH = 160;
   private static final int BUTTON_HEIGHT = 20;
   private static final int BUTTON_SPACING = 10;

   public SpearConfigScreen(Text title, String currentMode, boolean isWishSpear, String currentWishPreset, boolean showWishText) {
      super(title);
      this.currentMode = currentMode;
      this.isWishSpear = isWishSpear;
      this.currentWishPreset = currentWishPreset;
      this.showWishText = showWishText;
   }

   protected void init() {
      super.init();
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      if (this.isWishSpear) {
         List<String> mediumWishValues = Arrays.asList("medium", "wish");
         String mediumWishInitial = this.currentMode.equals("wish") ? "wish" : "medium";
         List<String> suppressAttackValues = Arrays.asList("suppress", "attack");
         String suppressAttackInitial = this.currentMode.equals("attack") ? "attack" : "suppress";
         int totalWidgets = 3;
         if (this.currentMode.equals("wish")) {
            totalWidgets = 4;
         }

         int totalHeight = 20 * totalWidgets + 10 * (totalWidgets - 1);
         int startY = centerY - totalHeight / 2;
         this.addDrawableChild(CyclingButtonWidget.builder(value -> {
            return switch (value) {
               case "suppress" -> Text.literal("§d压制模式");
               case "attack" -> Text.literal("§c攻击模式");
               default -> Text.literal("§7未知");
            };
         }).values(suppressAttackValues).initially(suppressAttackInitial).build(centerX - 80, startY, 160, 20, Text.literal("常规"), (button, value) -> {
            if (!value.equals(this.currentMode)) {
               SpearModeConfigC2SPacket.sendModeUpdateToServer(value);
               this.currentMode = value;
               this.refreshWidgets();
            }
         }));
         this.addDrawableChild(CyclingButtonWidget.builder(value -> {
            return switch (value) {
               case "medium" -> Text.literal("§b媒介模式");
               case "wish" -> Text.literal("§d许愿模式");
               default -> Text.literal("§7未知");
            };
         }).values(mediumWishValues).initially(mediumWishInitial).build(centerX - 80, startY + 20 + 10, 160, 20, Text.literal("技能"), (button, value) -> {
            if (!value.equals(this.currentMode)) {
               SpearModeConfigC2SPacket.sendModeUpdateToServer(value);
               this.currentMode = value;
               this.refreshWidgets();
            }
         }));
         this.addDrawableChild(
            CyclingButtonWidget.builder(value -> value ? Text.literal("§a显示许愿文本") : Text.literal("§7隐藏许愿文本"))
               .values(Arrays.asList(true, false))
               .initially(this.showWishText)
               .build(centerX - 80, startY + 60, 160, 20, Text.literal("聊天输出"), (button, value) -> {
                  if (value != this.showWishText) {
                     SpearModeConfigC2SPacket.sendShowWishTextUpdateToServer(value);
                     this.showWishText = value;
                  }
               })
         );
         if (this.currentMode.equals("wish")) {
            List<String> presetValues = Arrays.asList("", "tracking", "remote_attack", "ghost_silence", "strength", "escape", "full_heal");
            this.addDrawableChild(CyclingButtonWidget.builder(value -> {
               return switch (value) {
                  case "tracking" -> Text.literal("§e我说长枪投出必定命中眼前生物");
                  case "remote_attack" -> Text.literal("§e我说这一刀砍下必定命中眼前生物");
                  case "ghost_silence" -> Text.literal("§e我说眼前灵异必将退散");
                  case "strength" -> Text.literal("§e我说我行不可摧，志不可改，力可至极限");
                  case "escape" -> Text.literal("§e我说我必离开这片鬼域");
                  case "full_heal" -> Text.literal("§e我说我身强体壮，灾病全无");
                  default -> Text.literal("§7无");
               };
            }).values(presetValues).initially(this.currentWishPreset).build(centerX - 80, startY + 90, 160, 20, Text.literal("许愿预设"), (button, value) -> {
               if (!value.equals(this.currentWishPreset)) {
                  SpearModeConfigC2SPacket.sendWishPresetUpdateToServer(value);
                  this.currentWishPreset = value;
               }
            }));
         }
      } else {
         List<String> modeValues = Arrays.asList("suppress", "attack");
         this.addDrawableChild(CyclingButtonWidget.builder(value -> {
            return switch (value) {
               case "suppress" -> Text.literal("§d压制模式");
               case "attack" -> Text.literal("§c攻击模式");
               default -> Text.literal("§7未知");
            };
         }).values(modeValues).initially(this.currentMode).build(centerX - 80, centerY - 10, 160, 20, Text.literal("常规"), (button, value) -> {
            if (!value.equals(this.currentMode)) {
               SpearModeConfigC2SPacket.sendModeUpdateToServer(value);
            }
         }));
      }
   }

   private void refreshWidgets() {
      this.clearChildren();
      this.init();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      int centerX = this.width / 2;
      if (this.isWishSpear) {
         context.drawCenteredTextWithShadow(this.textRenderer, "§b许愿长枪配置", centerX, 20, 16777215);
      } else {
         context.drawCenteredTextWithShadow(this.textRenderer, "长枪配置", centerX, 30, 16777215);
      }

      context.drawCenteredTextWithShadow(this.textRenderer, "§7按 ESC 关闭", centerX, this.height - 20, 11184810);
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
