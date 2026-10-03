package com.xie.smfs.client.preset;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ScreenPresetRenderer implements HudRenderCallback {
   private static final ScreenPresetRenderer.PresetType[] VALUES = ScreenPresetRenderer.PresetType.values();
   private static ScreenPresetRenderer.PresetType currentPreset = ScreenPresetRenderer.PresetType.NONE;
   private static float intensity = 1.0F;
   private static long sequenceEndTime = 0L;
   private static final long TEXT_HALLUCINATION_DURATION_MS = 1000L;
   private static final long GLITCH_DURATION_MS = 1000L;
   private static final Random RANDOM = new Random();
   private int glitchRefreshTimer = 0;
   private static final int GLITCH_BARS = 30;
   private final int[] glitchY = new int[30];
   private final int[] glitchH = new int[30];
   private final int[] glitchOffset = new int[30];
   private final float[] glitchRGB = new float[90];
   private int glitchNoiseTimer = 0;
   private final boolean[][] glitchNoise = new boolean[80][45];
   private static final String[] HALLUCINATION_TEXTS = new String[]{
      "死", "逃", "救", "来", "看", "在", "你", "后", "面", "走", "别", "回", "头", "跑", "快", "跑", "它", "来", "了"
   };
   private static final int MAX_TEXTS = 8;
   private final float[] textX = new float[8];
   private final float[] textY = new float[8];
   private final int[] textIndex = new int[8];
   private final float[] textLife = new float[8];
   private final float[] textMaxLife = new float[8];
   private final float[] textScale = new float[8];
   private boolean textInit = false;

   public static void nextPreset() {
      int next = (currentPreset.ordinal() + 1) % VALUES.length;
      currentPreset = VALUES[next];
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         client.player.sendMessage(Text.literal("§e[预设] §f" + currentPreset.displayName + " §7强度: " + (int)(intensity * 100.0F) + "%"), true);
      }
   }

   public static void setPreset(ScreenPresetRenderer.PresetType preset) {
      currentPreset = preset;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         client.player.sendMessage(Text.literal("§e[预设] §f" + currentPreset.displayName + " §7强度: " + (int)(intensity * 100.0F) + "%"), true);
      }
   }

   public static void setIntensity(float i) {
      intensity = MathHelper.clamp(i, 0.0F, 1.0F);
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         client.player.sendMessage(Text.literal("§e[强度] §f" + (int)(intensity * 100.0F) + "%"), true);
      }
   }

   public static void increaseIntensity() {
      setIntensity(intensity + 0.1F);
   }

   public static void decreaseIntensity() {
      setIntensity(intensity - 0.1F);
   }

   public static ScreenPresetRenderer.PresetType getCurrentPreset() {
      return currentPreset;
   }

   public static float getIntensity() {
      return intensity;
   }

   public static void playTextHallucination() {
      sequenceEndTime = System.currentTimeMillis() + 1000L;
      currentPreset = ScreenPresetRenderer.PresetType.TEXT_HALLUCINATION;
      intensity = 1.0F;
   }

   public static void playGlitch() {
      sequenceEndTime = System.currentTimeMillis() + 1000L;
      currentPreset = ScreenPresetRenderer.PresetType.GLITCH;
      intensity = 1.0F;
   }

   public void onHudRender(DrawContext context, float tickDelta) {
      if (sequenceEndTime > 0L && System.currentTimeMillis() >= sequenceEndTime) {
         currentPreset = ScreenPresetRenderer.PresetType.NONE;
         sequenceEndTime = 0L;
      }

      if (currentPreset != ScreenPresetRenderer.PresetType.NONE && !(intensity <= 0.005F)) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            MatrixStack matrices = context.getMatrices();
            switch (currentPreset) {
               case GLITCH:
                  this.renderGlitch(context, width, height, matrices);
                  break;
               case TEXT_HALLUCINATION:
                  this.renderTextHallucination(context, width, height, matrices);
            }
         }
      }
   }

   private void renderGlitch(DrawContext context, int width, int height, MatrixStack matrices) {
      this.glitchRefreshTimer--;
      if (this.glitchRefreshTimer <= 0) {
         this.glitchRefreshTimer = 2 + RANDOM.nextInt(3);

         for (int i = 0; i < 30; i++) {
            this.glitchY[i] = RANDOM.nextInt(height);
            this.glitchH[i] = 1 + RANDOM.nextInt(12);
            this.glitchOffset[i] = (RANDOM.nextInt(31) - 15) * 3;
            this.glitchRGB[i * 3] = 0.2F + RANDOM.nextFloat() * 0.8F;
            this.glitchRGB[i * 3 + 1] = 0.2F + RANDOM.nextFloat() * 0.8F;
            this.glitchRGB[i * 3 + 2] = 0.2F + RANDOM.nextFloat() * 0.8F;
         }
      }

      this.glitchNoiseTimer--;
      if (this.glitchNoiseTimer <= 0) {
         this.glitchNoiseTimer = 1;

         for (int x = 0; x < 80; x++) {
            for (int y = 0; y < 45; y++) {
               this.glitchNoise[x][y] = RANDOM.nextFloat() < 0.12F * intensity;
            }
         }
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      float alphaBase = intensity * 0.55F;
      float alphaEdge = intensity * 0.8F;

      for (int i = 0; i < 30; i++) {
         int y = this.glitchY[i];
         int h = this.glitchH[i];
         int ox = this.glitchOffset[i];
         int r = (int)(this.glitchRGB[i * 3] * 255.0F);
         int g = (int)(this.glitchRGB[i * 3 + 1] * 255.0F);
         int b = (int)(this.glitchRGB[i * 3 + 2] * 255.0F);
         int mainColor = argb((int)(alphaBase * 255.0F), r, g, b);
         context.fill(ox, y, width + ox, y + h, mainColor);
         int edgeColor = argb((int)(alphaEdge * 255.0F), Math.min(255, r + 60), Math.min(255, g + 60), Math.min(255, b + 60));
         context.fill(ox, y, ox + 2, y + h, edgeColor);
         context.fill(width + ox - 2, y, width + ox, y + h, edgeColor);
      }

      float cellW = width / 80.0F;
      float cellH = height / 45.0F;
      int noiseAlpha = (int)(intensity * 0.3F * 255.0F);
      int nWhite = argb(noiseAlpha, 255, 255, 255);
      int nBlack = argb((int)(noiseAlpha * 0.6F), 0, 0, 0);

      for (int x = 0; x < 80; x++) {
         for (int y = 0; y < 45; y++) {
            if (this.glitchNoise[x][y]) {
               int sx = (int)(x * cellW);
               int sy = (int)(y * cellH);
               int ex = (int)((x + 1) * cellW) + 1;
               int ey = (int)((y + 1) * cellH) + 1;
               context.fill(sx, sy, ex, ey, RANDOM.nextBoolean() ? nWhite : nBlack);
            }
         }
      }

      int scanAlpha = (int)(intensity * 0.08F * 255.0F);
      int scanColor = argb(scanAlpha, 0, 0, 0);

      for (int ly = 0; ly < height; ly += 3) {
         context.fill(0, ly, width, ly + 1, scanColor);
      }

      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   private void renderTextHallucination(DrawContext context, int width, int height, MatrixStack matrices) {
      if (!this.textInit) {
         this.textInit = true;

         for (int i = 0; i < 8; i++) {
            this.textLife[i] = -RANDOM.nextFloat() * 5.0F;
         }
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      int darkBg = argb((int)(intensity * 0.08F * 255.0F), 0, 0, 0);
      context.fill(0, 0, width, height, darkBg);

      for (int i = 0; i < 8; i++) {
         this.textLife[i] = this.textLife[i] - 0.05F;
         if (this.textLife[i] <= 0.0F) {
            this.textX[i] = width * 0.1F + RANDOM.nextFloat() * width * 0.8F;
            this.textY[i] = height * 0.1F + RANDOM.nextFloat() * height * 0.8F;
            this.textIndex[i] = RANDOM.nextInt(HALLUCINATION_TEXTS.length);
            this.textMaxLife[i] = 0.5F + RANDOM.nextFloat() * 2.0F;
            this.textLife[i] = this.textMaxLife[i];
            this.textScale[i] = 1.5F + RANDOM.nextFloat() * 2.5F;
         }

         float lifeRatio = this.textLife[i] / this.textMaxLife[i];
         float fadeIn = Math.min(1.0F, (1.0F - lifeRatio) * 3.0F);
         float fadeOut = Math.min(1.0F, lifeRatio * 2.0F);
         float fade = Math.min(fadeIn, fadeOut) * intensity;
         if (!(fade < 0.02F)) {
            String msg = HALLUCINATION_TEXTS[this.textIndex[i]];
            int red = argb((int)(fade * 255.0F), 200, 10, 10);
            int glow = argb((int)(fade * 0.3F * 255.0F), 255, 50, 50);
            matrices.push();
            matrices.translate(this.textX[i], this.textY[i], 0.0F);
            matrices.scale(this.textScale[i], this.textScale[i], 1.0F);
            float shakeX = (RANDOM.nextFloat() - 0.5F) * 3.0F * intensity;
            float shakeY = (RANDOM.nextFloat() - 0.5F) * 3.0F * intensity;
            matrices.translate(shakeX, shakeY, 0.0F);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(msg);
            context.fill(-textWidth / 2 - 4, -6, textWidth / 2 + 4, 6, glow);
            context.drawText(MinecraftClient.getInstance().textRenderer, msg, -textWidth / 2, -5, red, false);
            matrices.pop();
         }
      }

      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   private static int argb(int a, int r, int g, int b) {
      return Math.max(0, Math.min(255, a)) << 24 | Math.max(0, Math.min(255, r)) << 16 | Math.max(0, Math.min(255, g)) << 8 | Math.max(0, Math.min(255, b));
   }

   public enum PresetType {
      NONE("无"),
      GLITCH("画面故障"),
      TEXT_HALLUCINATION("文字幻觉");

      public final String displayName;

      PresetType(String displayName) {
         this.displayName = displayName;
      }
   }
}
