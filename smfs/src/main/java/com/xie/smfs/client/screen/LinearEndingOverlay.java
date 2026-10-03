package com.xie.smfs.client.screen;

import com.xie.smfs.client.util.FusionCameraManager;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class LinearEndingOverlay extends Screen {
   private static final int CHAR_INTERVAL_MS = 80;
   private static final int DISPLAY_DURATION_MS = 2000;
   private static final int FADE_OUT_DURATION_MS = 500;
   private static final int FADE_IN_DURATION_MS = 300;
   private static final int TEXT_COLOR = 16777215;
   private static final int TEXT_SIZE = 2;
   private static final int BUTTON_WIDTH = 200;
   private static final int BUTTON_HEIGHT = 20;
   private final List<String> texts;
   private int currentTextIndex = 0;
   private LinearEndingOverlay.Phase phase = LinearEndingOverlay.Phase.FADE_IN;
   private long phaseStartTime;
   private int revealedChars = 0;
   private long lastCharTime = 0L;
   private float fadeAlpha = 0.0F;

   public LinearEndingOverlay() {
      super(Text.method_43470(""));
      this.texts = Arrays.asList("六十年以后...", "鬼童继承你的意志，带着你的记忆，行走世间；", "灵异时代被成功终结");
   }

   protected void method_25426() {
      super.method_25426();
      FusionCameraManager.stopZoom();
      this.phase = LinearEndingOverlay.Phase.FADE_IN;
      this.phaseStartTime = System.currentTimeMillis();
      this.currentTextIndex = 0;
      this.revealedChars = 0;
      this.lastCharTime = 0L;
      this.fadeAlpha = 0.0F;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      long now = System.currentTimeMillis();
      long elapsed = now - this.phaseStartTime;
      context.method_25294(0, 0, this.field_22789, this.field_22790, -16777216);
      if (this.phase == LinearEndingOverlay.Phase.BUTTON) {
         this.renderButtonPhase(context, mouseX, mouseY, delta);
      } else {
         String currentText = this.texts.get(this.currentTextIndex);
         switch (this.phase) {
            case FADE_IN:
               this.handleFadeIn(elapsed, now);
               break;
            case TYPING:
               this.handleTyping(elapsed, now);
               break;
            case DISPLAY:
               this.handleDisplay(elapsed);
               break;
            case FADE_OUT:
               this.handleFadeOut(elapsed);
         }

         if (this.phase != LinearEndingOverlay.Phase.BUTTON && !currentText.isEmpty()) {
            this.renderText(context, currentText);
         }

         super.method_25394(context, mouseX, mouseY, delta);
      }
   }

   private void handleFadeIn(long elapsed, long now) {
      float progress = (float)elapsed / 300.0F;
      this.fadeAlpha = MathHelper.method_15363(progress, 0.0F, 1.0F);
      if (elapsed >= 300L) {
         this.fadeAlpha = 1.0F;
         this.phase = LinearEndingOverlay.Phase.TYPING;
         this.phaseStartTime = now;
         this.revealedChars = 0;
         this.lastCharTime = now;
      }
   }

   private void handleTyping(long elapsed, long now) {
      if (now - this.lastCharTime >= 80L && this.revealedChars < this.texts.get(this.currentTextIndex).length()) {
         this.revealedChars++;
         this.lastCharTime = now;
      }

      if (this.revealedChars >= this.texts.get(this.currentTextIndex).length()) {
         this.phase = LinearEndingOverlay.Phase.DISPLAY;
         this.phaseStartTime = now;
      }
   }

   private void handleDisplay(long elapsed) {
      if (elapsed >= 2000L) {
         if (this.currentTextIndex < this.texts.size() - 1) {
            this.phase = LinearEndingOverlay.Phase.FADE_OUT;
            this.phaseStartTime = System.currentTimeMillis();
         } else {
            this.phase = LinearEndingOverlay.Phase.BUTTON;
            this.phaseStartTime = System.currentTimeMillis();
            this.addReturnButton();
         }
      }
   }

   private void handleFadeOut(long elapsed) {
      float progress = (float)elapsed / 500.0F;
      this.fadeAlpha = MathHelper.method_15363(1.0F - progress, 0.0F, 1.0F);
      if (elapsed >= 500L) {
         this.fadeAlpha = 0.0F;
         this.currentTextIndex++;
         this.revealedChars = 0;
         this.phase = LinearEndingOverlay.Phase.FADE_IN;
         this.phaseStartTime = System.currentTimeMillis();
      }
   }

   private void renderText(DrawContext context, String fullText) {
      if (!(this.fadeAlpha <= 0.01F)) {
         String visibleText = fullText.substring(0, Math.min(this.revealedChars, fullText.length()));
         int textAlpha = (int)(this.fadeAlpha * 255.0F);
         int color = textAlpha << 24 | 16777215;
         int textWidth = this.field_22793.method_1727(visibleText);
         int textY = this.field_22790 / 2 - 9 / 2;
         context.method_51448().method_22903();
         context.method_51448().method_46416(this.field_22789 / 2.0F, textY, 0.0F);
         context.method_51448().method_22905(2.0F, 2.0F, 1.0F);
         context.method_51439(this.field_22793, Text.method_43470(visibleText), -textWidth / 2, 0, color, false);
         context.method_51448().method_22909();
      }
   }

   private void renderButtonPhase(DrawContext context, int mouseX, int mouseY, float delta) {
      String currentText = this.texts.get(this.texts.size() - 1);
      int textAlpha = 255;
      int color = textAlpha << 24 | 16777215;
      int textWidth = this.field_22793.method_1727(currentText);
      int textY = this.field_22790 / 2 - 30;
      context.method_51448().method_22903();
      context.method_51448().method_46416(this.field_22789 / 2.0F, textY, 0.0F);
      context.method_51448().method_22905(2.0F, 2.0F, 1.0F);
      context.method_51439(this.field_22793, Text.method_43470(currentText), -textWidth / 2, 0, color, false);
      context.method_51448().method_22909();
      super.method_25394(context, mouseX, mouseY, delta);
   }

   private void addReturnButton() {
      int centerX = this.field_22789 / 2;
      int buttonY = this.field_22790 / 2 + 40;
      ButtonWidget button = ButtonWidget.method_46430(Text.method_43471("smfs.ending.return_to_title"), btn -> {
         if (this.field_22787 != null) {
            if (this.field_22787.field_1687 != null) {
               this.field_22787.field_1687.method_8525();
            }

            this.field_22787.method_18099();
            this.field_22787.method_1507(new TitleScreen());
         }
      }).method_46434(centerX - 100, buttonY, 200, 20).method_46431();
      this.method_37063(button);
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25422() {
      return false;
   }

   private enum Phase {
      FADE_IN,
      TYPING,
      DISPLAY,
      FADE_OUT,
      BUTTON;
   }
}
