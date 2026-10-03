package com.xie.smfs.client.preset;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;

public class FilterRenderer {
   public static void renderEdgeGradient(DrawContext context, int width, int height, FilterRenderer.FilterPreset preset) {
      renderEdgeGradient(context, width, height, preset, 1.0F);
   }

   public static void renderEdgeGradient(DrawContext context, int width, int height, FilterRenderer.FilterPreset preset, float fadeProgress) {
      if (!(fadeProgress <= 0.005F)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         int bandSize = Math.max(width, height) / preset.bandDivisor;

         for (int y = 0; y < bandSize; y++) {
            float progress = 1.0F - (float)y / bandSize;
            float alpha = (float)Math.sin(progress * Math.PI / 2.0) * preset.maxAlpha * fadeProgress;
            if (!(alpha <= 0.005F)) {
               int color = (int)(alpha * 255.0F) << 24 | preset.red << 16 | preset.green << 8 | preset.blue;
               context.fill(0, y, width, y + 1, color);
            }
         }

         for (int y = height - 1; y >= height - bandSize; y--) {
            float progress = 1.0F - (float)(height - 1 - y) / bandSize;
            float alpha = (float)Math.sin(progress * Math.PI / 2.0) * preset.maxAlpha * fadeProgress;
            if (!(alpha <= 0.005F)) {
               int color = (int)(alpha * 255.0F) << 24 | preset.red << 16 | preset.green << 8 | preset.blue;
               context.fill(0, y, width, y + 1, color);
            }
         }

         for (int x = 0; x < bandSize; x++) {
            float progress = 1.0F - (float)x / bandSize;
            float alpha = (float)Math.sin(progress * Math.PI / 2.0) * preset.maxAlpha * fadeProgress;
            if (!(alpha <= 0.005F)) {
               int color = (int)(alpha * 255.0F) << 24 | preset.red << 16 | preset.green << 8 | preset.blue;
               context.fill(x, 0, x + 1, height, color);
            }
         }

         for (int x = width - 1; x >= width - bandSize; x--) {
            float progress = 1.0F - (float)(width - 1 - x) / bandSize;
            float alpha = (float)Math.sin(progress * Math.PI / 2.0) * preset.maxAlpha * fadeProgress;
            if (!(alpha <= 0.005F)) {
               int color = (int)(alpha * 255.0F) << 24 | preset.red << 16 | preset.green << 8 | preset.blue;
               context.fill(x, 0, x + 1, height, color);
            }
         }

         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   public enum FilterPreset {
      RED_CURSE(255, 30, 30, 0.35F, 7),
      BLUE_CURSE(30, 30, 255, 0.35F, 7),
      GREEN_CURSE(30, 255, 30, 0.35F, 7),
      PURPLE_CURSE(150, 30, 255, 0.35F, 7),
      GOLD_CURSE(255, 200, 30, 0.35F, 7),
      BLACK_CURSE(50, 50, 50, 0.35F, 6),
      CYAN_CURSE(30, 200, 200, 0.35F, 7),
      PINK_CURSE(255, 30, 150, 0.35F, 7),
      ORANGE_CURSE(255, 100, 30, 0.35F, 7),
      FAINT_RED(255, 50, 50, 0.2F, 8),
      STRONG_RED(255, 20, 20, 0.5F, 6);

      public final int red;
      public final int green;
      public final int blue;
      public final float maxAlpha;
      public final int bandDivisor;

      FilterPreset(int red, int green, int blue, float maxAlpha, int bandDivisor) {
         this.red = red;
         this.green = green;
         this.blue = blue;
         this.maxAlpha = maxAlpha;
         this.bandDivisor = bandDivisor;
      }
   }
}
