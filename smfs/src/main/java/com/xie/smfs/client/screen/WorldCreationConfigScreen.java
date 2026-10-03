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
      super(Text.translatable("smfs.config.world_config_title"));
      this.parent = parent;
      this.loadDefaults();
   }

   private void loadDefaults() {
      WorldConfig defaultConfig = WorldConfig.loadDefault();
      this.modDifficulty = defaultConfig.modDifficulty;
      this.endingMode = defaultConfig.endingMode != null ? defaultConfig.endingMode : "open";
   }

   protected void init() {
      super.init();
      int centerX = this.width / 2;
      int startY = this.height / 2 - 40;
      int widgetWidth = 200;
      int leftX = centerX - widgetWidth / 2;
      int y = startY;
      y += 10;
      this.addDrawableChild(
         CyclingButtonWidget.builder(index -> {
               switch (index) {
                  case 0:
                     return Text.translatable("smfs.config.difficulty.low");
                  case 2:
                     return Text.translatable("smfs.config.difficulty.high");
                  default:
                     return Text.translatable("smfs.config.difficulty.medium");
               }
            })
            .values(Arrays.asList(0, 1, 2))
            .initially(this.modDifficulty)
            .build(leftX, y, widgetWidth, 20, Text.translatable("smfs.config.world.mod_difficulty"), (button, value) -> this.modDifficulty = value)
      );
      y += 28;
      y += 10;
      this.addDrawableChild(
         CyclingButtonWidget.builder(
               mode -> "linear".equals(mode) ? Text.translatable("smfs.config.ending_mode.linear") : Text.translatable("smfs.config.ending_mode.open")
            )
            .values(Arrays.asList("open", "linear"))
            .initially(this.endingMode)
            .build(leftX, y, widgetWidth, 20, Text.translatable("smfs.config.world.ending_mode"), (button, value) -> this.endingMode = value)
      );
      y += 28;
      y += 20;
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("smfs.config.save"), button -> this.saveAndClose())
            .dimensions(centerX - 154, this.height - 28, 100, 20)
            .build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("smfs.config.reset"), button -> this.resetToDefaults())
            .dimensions(centerX - 50, this.height - 28, 100, 20)
            .build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("smfs.config.cancel"), button -> this.close()).dimensions(centerX + 54, this.height - 28, 100, 20).build()
      );
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }

   private void saveAndClose() {
      WorldConfig defaultConfig = WorldConfig.loadDefault();
      defaultConfig.modDifficulty = this.modDifficulty;
      defaultConfig.endingMode = this.endingMode;
      defaultConfig.validate();
      defaultConfig.saveDefault();
      this.close();
   }

   private void resetToDefaults() {
      this.modDifficulty = 1;
      this.endingMode = "open";
      this.clearAndInit();
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }
}
