package com.xie.smfs.client.screen;

import com.xie.smfs.config.WorldConfig;
import java.util.Arrays;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class WorldCreationConfigScreen extends Screen {
   private final Screen parent;
   private int modDifficulty = 1;
   private String endingMode = "open";

   public WorldCreationConfigScreen(Screen parent) {
      super(Text.method_43471("smfs.config.world_config_title"));
      this.parent = parent;
      this.loadDefaults();
   }

   private void loadDefaults() {
      WorldConfig defaultConfig = WorldConfig.loadDefault();
      this.modDifficulty = defaultConfig.modDifficulty;
      this.endingMode = defaultConfig.endingMode != null ? defaultConfig.endingMode : "open";
   }

   protected void method_25426() {
      super.method_25426();
      int centerX = this.field_22789 / 2;
      int startY = this.field_22790 / 2 - 40;
      int widgetWidth = 200;
      int leftX = centerX - widgetWidth / 2;
      int y = startY;
      y += 10;
      this.method_37063(
         CyclingButtonWidget.method_32606(index -> {
               switch (index) {
                  case 0:
                     return Text.method_43471("smfs.config.difficulty.low");
                  case 2:
                     return Text.method_43471("smfs.config.difficulty.high");
                  default:
                     return Text.method_43471("smfs.config.difficulty.medium");
               }
            })
            .method_32620(Arrays.asList(0, 1, 2))
            .method_32619(this.modDifficulty)
            .method_32617(leftX, y, widgetWidth, 20, Text.method_43471("smfs.config.world.mod_difficulty"), (button, value) -> this.modDifficulty = value)
      );
      y += 28;
      y += 10;
      this.method_37063(
         CyclingButtonWidget.method_32606(
               mode -> "linear".equals(mode) ? Text.method_43471("smfs.config.ending_mode.linear") : Text.method_43471("smfs.config.ending_mode.open")
            )
            .method_32620(Arrays.asList("open", "linear"))
            .method_32619(this.endingMode)
            .method_32617(leftX, y, widgetWidth, 20, Text.method_43471("smfs.config.world.ending_mode"), (button, value) -> this.endingMode = value)
      );
      y += 28;
      y += 20;
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.save"), button -> this.saveAndClose())
            .method_46434(centerX - 154, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.reset"), button -> this.resetToDefaults())
            .method_46434(centerX - 50, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
      this.method_37063(
         ButtonWidget.method_46430(Text.method_43471("smfs.config.cancel"), button -> this.method_25419())
            .method_46434(centerX + 54, this.field_22790 - 28, 100, 20)
            .method_46431()
      );
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 15, 16777215);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   private void saveAndClose() {
      WorldConfig defaultConfig = WorldConfig.loadDefault();
      defaultConfig.modDifficulty = this.modDifficulty;
      defaultConfig.endingMode = this.endingMode;
      defaultConfig.validate();
      defaultConfig.saveDefault();
      this.method_25419();
   }

   private void resetToDefaults() {
      this.modDifficulty = 1;
      this.endingMode = "open";
      this.method_41843();
   }

   public void method_25419() {
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
