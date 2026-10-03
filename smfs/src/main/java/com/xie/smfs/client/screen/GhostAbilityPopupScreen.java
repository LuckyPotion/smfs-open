package com.xie.smfs.client.screen;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class GhostAbilityPopupScreen extends Screen {
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/ability_popup_background.png");
   private static final int WINDOW_WIDTH = 256;
   private static final int WINDOW_HEIGHT = 200;
   private static final int PADDING = 10;
   private static final int TITLE_Y = 0;
   private static final int CONTENT_START_Y = 26;
   private static final int LINE_HEIGHT = 10;
   private static final int BUTTON_WIDTH = 80;
   private static final int BUTTON_HEIGHT = 20;
   private final String title;
   private final int titleColor;
   private final float titleScale;
   private final List<String> contents;
   private final boolean twoColumnLayout;
   private int windowX;
   private int windowY;

   public GhostAbilityPopupScreen(String ghostName, List<String> abilityDescriptions) {
      this("驾驭成功：" + ghostName, 16777215, 0.9F, abilityDescriptions, true);
   }

   public GhostAbilityPopupScreen(String title, int titleColor, float titleScale, List<String> contents, boolean twoColumnLayout) {
      super(Text.translatable("screen.smfs.ghost_ability_popup"));
      this.title = title;
      this.titleColor = titleColor;
      this.titleScale = titleScale;
      this.contents = contents;
      this.twoColumnLayout = twoColumnLayout;
   }

   protected void init() {
      super.init();
      this.windowX = (this.width - 256) / 2;
      this.windowY = (this.height - 200) / 2;
      int buttonX = this.windowX + 88;
      int buttonY = this.windowY + 200 - 20 - 10;
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.smfs.confirm"), button -> this.close()).dimensions(buttonX, buttonY, 80, 20).build());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      context.drawTexture(BACKGROUND_TEXTURE, this.windowX, this.windowY, 0, 0, 256, 200);
      TextRenderer textRenderer = this.textRenderer;
      Text mainTitle = Text.literal(this.title);
      int mainTitleScaledWidth = (int)(textRenderer.getWidth(mainTitle) * this.titleScale);
      int mainTitleX = this.windowX + (256 - mainTitleScaledWidth) / 2;
      context.getMatrices().push();
      context.getMatrices().translate(mainTitleX, this.windowY + 0 + 5, 0.0F);
      context.getMatrices().scale(this.titleScale, this.titleScale, 1.0F);
      context.drawText(textRenderer, mainTitle, 0, 0, this.titleColor, true);
      context.getMatrices().pop();
      int currentY = this.windowY + 26;
      if (this.twoColumnLayout) {
         int columnWidth = 113;
         int leftColumnX = this.windowX + 10 + 5;
         int rightColumnX = leftColumnX + columnWidth + 10;

         for (int i = 0; i < this.contents.size(); i++) {
            String rawContent = this.contents.get(i);
            int textColor = 14540253;
            String displayContent = rawContent;
            if (rawContent.length() >= 2 && rawContent.charAt(0) == 167) {
               Formatting formatting = Formatting.byCode(rawContent.charAt(1));
               if (formatting != null && formatting.isColor()) {
                  textColor = formatting.getColorValue();
                  displayContent = rawContent.substring(2);
               }
            }

            Text contentText = Text.literal(displayContent);
            if (i % 2 == 0) {
               context.getMatrices().push();
               float textScale = 0.8F;
               context.getMatrices().translate(leftColumnX, currentY, 0.0F);
               context.getMatrices().scale(textScale, textScale, 1.0F);
               context.drawText(textRenderer, contentText, 0, 0, textColor, false);
               context.getMatrices().pop();
            } else {
               context.getMatrices().push();
               float textScale = 0.8F;
               context.getMatrices().translate(rightColumnX, currentY, 0.0F);
               context.getMatrices().scale(textScale, textScale, 1.0F);
               context.drawText(textRenderer, contentText, 0, 0, textColor, false);
               context.getMatrices().pop();
               currentY += 10;
            }
         }

         if (this.contents.size() % 2 == 1) {
            currentY += 10;
         }
      } else {
         int lineHeightForSingleColumn = 18;
         float textScale = 0.85F;

         for (String rawContent : this.contents) {
            int textColor = 14540253;
            String displayContent = rawContent;
            if (rawContent.length() >= 2 && rawContent.charAt(0) == 167) {
               Formatting formatting = Formatting.byCode(rawContent.charAt(1));
               if (formatting != null && formatting.isColor()) {
                  textColor = formatting.getColorValue();
                  displayContent = rawContent.substring(2);
               }
            }

            Text contentText = Text.literal(displayContent);
            int contentScaledWidth = (int)(textRenderer.getWidth(contentText) * textScale);
            int contentX = this.windowX + (256 - contentScaledWidth) / 2;
            context.getMatrices().push();
            context.getMatrices().translate(contentX, currentY, 0.0F);
            context.getMatrices().scale(textScale, textScale, 1.0F);
            context.drawText(textRenderer, contentText, 0, 0, textColor, false);
            context.getMatrices().pop();
            currentY += lineHeightForSingleColumn;
         }
      }

      super.render(context, mouseX, mouseY, delta);
   }

   public boolean shouldPause() {
      return false;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(null);
      }
   }

   public static void show(String ghostName, List<String> abilityDescriptions) {
      showPopup("驾驭成功：" + ghostName, 16777215, 0.9F, abilityDescriptions, true);
   }

   public static void showPopup(String title, int titleColor, float titleScale, List<String> contents, boolean twoColumnLayout) {
      try {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null) {
            client.execute(() -> {
               try {
                  client.setScreen(new GhostAbilityPopupScreen(title, titleColor, titleScale, contents, twoColumnLayout));
               } catch (Exception ex) {
                  System.err.println("[SMFS] GhostAbilityPopupScreen: Error setting screen: " + ex.getMessage());
                  ex.printStackTrace();
               }
            });
         } else {
            System.err.println("[SMFS] GhostAbilityPopupScreen: MinecraftClient is null");
         }
      } catch (Exception e) {
         System.err.println("[SMFS] GhostAbilityPopupScreen: Error in showPopup method: " + e.getMessage());
         e.printStackTrace();
      }
   }

   public static void showSingleColumnPopup(String title, int titleColor, float titleScale, List<String> contents) {
      showPopup(title, titleColor, titleScale, contents, false);
   }
}
