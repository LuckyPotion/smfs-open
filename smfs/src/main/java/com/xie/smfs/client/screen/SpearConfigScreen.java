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

   protected void method_25426() {
      super.method_25426();
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
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
         this.method_37063(
            CyclingButtonWidget.method_32606(value -> {
                  return switch (value) {
                     case "suppress" -> Text.method_43470("§d压制模式");
                     case "attack" -> Text.method_43470("§c攻击模式");
                     default -> Text.method_43470("§7未知");
                  };
               })
               .method_32620(suppressAttackValues)
               .method_32619(suppressAttackInitial)
               .method_32617(centerX - 80, startY, 160, 20, Text.method_43470("常规"), (button, value) -> {
                  if (!value.equals(this.currentMode)) {
                     SpearModeConfigC2SPacket.sendModeUpdateToServer(value);
                     this.currentMode = value;
                     this.refreshWidgets();
                  }
               })
         );
         this.method_37063(
            CyclingButtonWidget.method_32606(value -> {
                  return switch (value) {
                     case "medium" -> Text.method_43470("§b媒介模式");
                     case "wish" -> Text.method_43470("§d许愿模式");
                     default -> Text.method_43470("§7未知");
                  };
               })
               .method_32620(mediumWishValues)
               .method_32619(mediumWishInitial)
               .method_32617(centerX - 80, startY + 20 + 10, 160, 20, Text.method_43470("技能"), (button, value) -> {
                  if (!value.equals(this.currentMode)) {
                     SpearModeConfigC2SPacket.sendModeUpdateToServer(value);
                     this.currentMode = value;
                     this.refreshWidgets();
                  }
               })
         );
         this.method_37063(
            CyclingButtonWidget.method_32606(value -> value ? Text.method_43470("§a显示许愿文本") : Text.method_43470("§7隐藏许愿文本"))
               .method_32620(Arrays.asList(true, false))
               .method_32619(this.showWishText)
               .method_32617(centerX - 80, startY + 60, 160, 20, Text.method_43470("聊天输出"), (button, value) -> {
                  if (value != this.showWishText) {
                     SpearModeConfigC2SPacket.sendShowWishTextUpdateToServer(value);
                     this.showWishText = value;
                  }
               })
         );
         if (this.currentMode.equals("wish")) {
            List<String> presetValues = Arrays.asList("", "tracking", "remote_attack", "ghost_silence", "strength", "escape", "full_heal");
            this.method_37063(
               CyclingButtonWidget.method_32606(value -> {
                     return switch (value) {
                        case "tracking" -> Text.method_43470("§e我说长枪投出必定命中眼前生物");
                        case "remote_attack" -> Text.method_43470("§e我说这一刀砍下必定命中眼前生物");
                        case "ghost_silence" -> Text.method_43470("§e我说眼前灵异必将退散");
                        case "strength" -> Text.method_43470("§e我说我行不可摧，志不可改，力可至极限");
                        case "escape" -> Text.method_43470("§e我说我必离开这片鬼域");
                        case "full_heal" -> Text.method_43470("§e我说我身强体壮，灾病全无");
                        default -> Text.method_43470("§7无");
                     };
                  })
                  .method_32620(presetValues)
                  .method_32619(this.currentWishPreset)
                  .method_32617(centerX - 80, startY + 90, 160, 20, Text.method_43470("许愿预设"), (button, value) -> {
                     if (!value.equals(this.currentWishPreset)) {
                        SpearModeConfigC2SPacket.sendWishPresetUpdateToServer(value);
                        this.currentWishPreset = value;
                     }
                  })
            );
         }
      } else {
         List<String> modeValues = Arrays.asList("suppress", "attack");
         this.method_37063(
            CyclingButtonWidget.method_32606(value -> {
                  return switch (value) {
                     case "suppress" -> Text.method_43470("§d压制模式");
                     case "attack" -> Text.method_43470("§c攻击模式");
                     default -> Text.method_43470("§7未知");
                  };
               })
               .method_32620(modeValues)
               .method_32619(this.currentMode)
               .method_32617(centerX - 80, centerY - 10, 160, 20, Text.method_43470("常规"), (button, value) -> {
                  if (!value.equals(this.currentMode)) {
                     SpearModeConfigC2SPacket.sendModeUpdateToServer(value);
                  }
               })
         );
      }
   }

   private void refreshWidgets() {
      this.method_37067();
      this.method_25426();
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      if (this.isWishSpear) {
         context.method_25300(this.field_22793, "§b许愿长枪配置", centerX, 20, 16777215);
      } else {
         context.method_25300(this.field_22793, "长枪配置", centerX, 30, 16777215);
      }

      context.method_25300(this.field_22793, "§7按 ESC 关闭", centerX, this.field_22790 - 20, 11184810);
   }

   public boolean method_25422() {
      return true;
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(keyCode, scanCode, modifiers);
      }
   }
}
