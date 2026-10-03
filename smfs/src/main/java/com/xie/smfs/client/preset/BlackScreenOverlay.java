package com.xie.smfs.client.preset;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class BlackScreenOverlay extends Screen {
   private final String text;
   private final float fadeInDuration;
   private final float displayDuration;
   private final float fadeOutDuration;
   private final float charDelay;
   private final boolean startOpaque;
   private final Runnable onComplete;
   private final Runnable onShow;
   private final float textDelay;
   private long startTime;
   private float totalDuration;
   private float fadeAlpha;
   private int visibleChars;
   private long lastCharTime;
   private static final int TEXT_COLOR = 16777215;
   private static final int TEXT_SIZE = 2;

   public BlackScreenOverlay(
      String text,
      float fadeInDuration,
      float displayDuration,
      float fadeOutDuration,
      float charDelay,
      boolean startOpaque,
      Runnable onComplete,
      Runnable onShow,
      float textDelay
   ) {
      super(Text.literal(""));
      this.text = text;
      this.fadeInDuration = startOpaque ? 0.0F : fadeInDuration;
      this.displayDuration = displayDuration;
      this.fadeOutDuration = fadeOutDuration;
      this.charDelay = charDelay;
      this.startOpaque = startOpaque;
      this.onComplete = onComplete;
      this.onShow = onShow;
      this.textDelay = textDelay;
      this.totalDuration = this.fadeInDuration + displayDuration + fadeOutDuration;
   }

   public BlackScreenOverlay(String text, float fadeInDuration, float displayDuration, float fadeOutDuration, float charDelay, Runnable onComplete) {
      this(text, fadeInDuration, displayDuration, fadeOutDuration, charDelay, false, onComplete, null, 0.0F);
   }

   public BlackScreenOverlay(String text, Runnable onComplete) {
      this(text, 1.0F, 2.0F, 1.0F, 0.15F, onComplete);
   }

   protected void init() {
      this.startTime = System.currentTimeMillis();
      this.fadeAlpha = this.startOpaque ? 1.0F : 0.0F;
      this.visibleChars = 0;
      this.lastCharTime = 0L;
      if (this.onShow != null) {
         this.onShow.run();
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      long elapsed = System.currentTimeMillis() - this.startTime;
      float elapsedSeconds = (float)elapsed / 1000.0F;
      if (elapsedSeconds >= this.totalDuration) {
         if (this.client != null) {
            this.client.setScreen(null);
         }

         if (this.onComplete != null) {
            this.onComplete.run();
         }
      } else {
         float fadeInEnd = this.fadeInDuration;
         float displayEnd = fadeInEnd + this.displayDuration;
         if (elapsedSeconds < fadeInEnd) {
            this.fadeAlpha = this.startOpaque ? 1.0F : MathHelper.clamp(elapsedSeconds / this.fadeInDuration, 0.0F, 1.0F);
         } else if (elapsedSeconds < displayEnd) {
            this.fadeAlpha = 1.0F;
         } else {
            float fadeOutElapsed = elapsedSeconds - displayEnd;
            this.fadeAlpha = MathHelper.clamp(1.0F - fadeOutElapsed / this.fadeOutDuration, 0.0F, 1.0F);
         }

         int bgAlpha = (int)(this.fadeAlpha * 255.0F);
         context.fill(0, 0, this.width, this.height, bgAlpha << 24 | 0);
         if (this.fadeAlpha > 0.1F && this.text != null && !this.text.isEmpty()) {
            float textElapsed = this.startOpaque ? elapsedSeconds - this.textDelay : elapsedSeconds - this.fadeInDuration * 0.3F;
            if (textElapsed > 0.0F) {
               int targetChars = (int)(textElapsed / this.charDelay);
               if (targetChars > this.visibleChars) {
                  this.visibleChars = Math.min(targetChars, this.text.length());
               }
            }

            String visibleText = this.text.substring(0, this.visibleChars);
            int textAlpha = (int)(Math.min(this.fadeAlpha * 1.3F, 1.0F) * 255.0F);
            int color = textAlpha << 24 | 16777215;
            int textWidth = this.textRenderer.getWidth(visibleText);
            int textX = (this.width - textWidth) / 2;
            int textY = this.height / 2 - 9 / 2;
            context.getMatrices().push();
            context.getMatrices().translate(textX, textY, 0.0F);
            context.getMatrices().scale(2.0F, 2.0F, 1.0F);
            context.drawText(this.textRenderer, Text.literal(visibleText), 0, 0, color, false);
            context.getMatrices().pop();
         }

         super.render(context, mouseX, mouseY, delta);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public static void show(String text, Runnable onComplete) {
      show(text, 1.0F, 2.0F, 1.0F, 0.15F, onComplete);
   }

   public static void showOpaque(String text, Runnable onComplete) {
      show(text, 0.0F, 5.0F, 1.0F, 0.15F, true, onComplete, null, 0.0F);
   }

   public static void showOpaque(String text, Runnable onComplete, Runnable onShow) {
      show(text, 0.0F, 5.0F, 1.0F, 0.15F, true, onComplete, onShow, 0.0F);
   }

   public static void showOpaque(String text, Runnable onComplete, Runnable onShow, float textDelay) {
      show(text, 0.0F, 5.0F, 1.0F, 0.15F, true, onComplete, onShow, textDelay);
   }

   public static void show(String text, float fadeInDuration, float displayDuration, float fadeOutDuration, float charDelay, Runnable onComplete) {
      show(text, fadeInDuration, displayDuration, fadeOutDuration, charDelay, false, onComplete, null, 0.0F);
   }

   private static void show(
      String text,
      float fadeInDuration,
      float displayDuration,
      float fadeOutDuration,
      float charDelay,
      boolean startOpaque,
      Runnable onComplete,
      Runnable onShow,
      float textDelay
   ) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null) {
         client.execute(
            () -> client.setScreen(
               new BlackScreenOverlay(text, fadeInDuration, displayDuration, fadeOutDuration, charDelay, startOpaque, onComplete, onShow, textDelay)
            )
         );
      }
   }
}
