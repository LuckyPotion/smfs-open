package com.xie.smfs.client.preset;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xie.smfs.item.RustyOldBroadswordItem;
import com.xie.smfs.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ScreenTearRenderer implements HudRenderCallback {
   private static final boolean ENABLE_SCREEN_TEAR = true;
   private static final boolean ENABLE_CRACK_GLOW = true;
   private static final boolean ENABLE_CRACK_SHADOW = true;
   private static final boolean ENABLE_CRACK_HIGHLIGHT = true;
   private static final boolean ENABLE_CRACK_CORE = true;
   private static final boolean ENABLE_CRACK_REFRACT = true;
   private static final boolean ENABLE_SHARDS = false;
   private static final boolean ENABLE_VIGNETTE = true;
   private static final Random RANDOM = new Random();
   private static final int MAX_CRACKS = 5;
   private static final int MAX_SEGMENTS = 40;
   private static final float BRANCH_CHANCE = 0.25F;
   private static final float CRACK_WIDTH = 3.0F;
   private static final float GLOW_WIDTH = 8.0F;
   private boolean crackFixed = false;
   private static long hitCount = 0L;
   private static long forceRefreshCount = 0L;
   private final List<ScreenTearRenderer.CrackSegment> crackSegments = new ArrayList<>();
   private long lastHitCount = 0L;
   private long lastForceRefreshCount = 0L;
   private long seed = 0L;

   public static void onRustyBladeHit() {
      hitCount++;
   }

   public static void forceRefresh() {
      forceRefreshCount++;
   }

   public void onHudRender(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         if (isHoldingRustyBroadsword(client.player)) {
            this.loadConfigFromItem(client.player);
            ScreenTearRenderer.TearParams params = new ScreenTearRenderer.TearParams(0.6F, 0.1F, 0.05F, 1.0F);
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            if (this.seed != client.player.getUuid().getLeastSignificantBits() || forceRefreshCount != this.lastForceRefreshCount) {
               this.seed = client.player.getUuid().getLeastSignificantBits();
               this.lastHitCount = hitCount;
               this.lastForceRefreshCount = forceRefreshCount;
               this.generateCracks(width, height, this.seed + forceRefreshCount);
            } else if (!this.crackFixed && hitCount != this.lastHitCount) {
               this.lastHitCount = hitCount;
               this.generateCracks(width, height, this.seed + hitCount);
            }

            if (!this.crackSegments.isEmpty()) {
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableDepthTest();
               MatrixStack matrices = context.getMatrices();
               this.drawScreenTear(context, client, width, height);

               for (ScreenTearRenderer.CrackSegment seg : this.crackSegments) {
                  float dx = seg.x2 - seg.x1;
                  float dy = seg.y2 - seg.y1;
                  float len = (float)Math.sqrt(dx * dx + dy * dy);
                  if (!(len < 1.0F)) {
                     float angle = (float)Math.atan2(dy, dx);
                     int iLen = (int)len;
                     int glowHW = Math.max(1, (int)(8.0F * seg.intensity / 2.0F));
                     int crackHW = Math.max(1, (int)(3.0F * seg.intensity / 2.0F));
                     matrices.push();
                     matrices.translate(seg.x1, seg.y1, 0.0F);
                     matrices.multiply(RotationAxis.POSITIVE_Z.rotation(angle));
                     int glowAlpha = (int)(0.25F * seg.intensity * 255.0F);
                     int glowOuter = argb(glowAlpha, (int)(params.r * 255.0F), (int)(params.g * 255.0F), (int)(params.b * 255.0F));
                     int glowInner = argb(glowAlpha / 2, (int)(params.r * 255.0F), (int)(params.g * 255.0F), (int)(params.b * 255.0F));
                     context.fill(0, -glowHW, iLen, glowHW, glowOuter);
                     context.fill(0, -glowHW / 2, iLen, glowHW / 2, glowInner);
                     glowAlpha = (int)(0.6F * seg.intensity * 255.0F);
                     glowOuter = argb(glowAlpha, 0, 0, 0);
                     glowInner = crackHW + 1;
                     context.fill(0, -glowInner - 1, iLen, -glowInner, glowOuter);
                     glowAlpha = (int)(0.3F * seg.intensity * 255.0F);
                     glowOuter = argb(glowAlpha, 255, 255, 255);
                     context.fill(0, crackHW, iLen, crackHW + 1, glowOuter);
                     glowAlpha = (int)(0.9F * seg.intensity * 255.0F);
                     glowOuter = argb(glowAlpha, 2, 2, 2);
                     context.fill(0, -crackHW, iLen, crackHW, glowOuter);
                     glowAlpha = (int)(0.12F * seg.intensity * 255.0F);
                     glowOuter = argb(glowAlpha, (int)(params.r * 255.0F), (int)(params.g * 255.0F), (int)(params.b * 255.0F));
                     glowInner = crackHW + 2;
                     context.fill(0, crackHW + 2, iLen, glowInner + 2, glowOuter);
                     matrices.pop();
                  }
               }

               this.drawVignette(context, width, height, params);
               RenderSystem.enableDepthTest();
               RenderSystem.disableBlend();
            }
         }
      }
   }

   private void drawVignette(DrawContext context, int width, int height, ScreenTearRenderer.TearParams params) {
      int alpha = (int)(0.35F * params.intensity * 255.0F);
      int color = argb(alpha, (int)(params.r * 255.0F), (int)(params.g * 255.0F), (int)(params.b * 255.0F));
      int bands = 20;
      int margin = 40;

      for (int i = 0; i < bands; i++) {
         float t = (float)i / bands;
         float ease = t * t;
         int a = (int)(alpha * (1.0F - ease));
         int d = margin * (i + 1) / bands;
         int c = argb(Math.max(0, a), (int)(params.r * 255.0F), (int)(params.g * 255.0F), (int)(params.b * 255.0F));
         context.fill(0, d, width, d + 1, c);
         context.fill(0, height - d - 1, width, height - d, c);
         context.fill(d, 0, d + 1, height, c);
         context.fill(width - d - 1, 0, width - d, height, c);
      }
   }

   private void drawShards(DrawContext context, MatrixStack matrices, ScreenTearRenderer.TearParams params) {
      if (!this.crackSegments.isEmpty()) {
         int shardColor = argb(45, (int)(params.r * 255.0F), (int)(params.g * 255.0F), (int)(params.b * 255.0F));
         int shardEdge = argb(89, (int)(params.r * 255.0F), (int)(params.g * 255.0F), (int)(params.b * 255.0F));
         int shardCount = this.crackSegments.size() / 2;

         for (int i = 0; i < shardCount; i++) {
            ScreenTearRenderer.CrackSegment seg = this.crackSegments.get(i * 2);
            float t = 0.3F + (float)Math.random() * 0.4F;
            float cx = seg.x1 + (seg.x2 - seg.x1) * t;
            float cy = seg.y1 + (seg.y2 - seg.y1) * t;
            float perpX = -(seg.y2 - seg.y1);
            float perpY = seg.x2 - seg.x1;
            float perpLen = (float)Math.sqrt(perpX * perpX + perpY * perpY);
            if (!(perpLen < 0.1F)) {
               perpX /= perpLen;
               perpY /= perpLen;
               float offset = (3.0F + (float)Math.random() * 8.0F) * ((float)Math.random() > 0.5F ? 1 : -1);
               float sx = cx + perpX * offset;
               float sy = cy + perpY * offset;
               float shardAngle = (float)(Math.random() * Math.PI * 2.0);
               int shardW = 2 + (int)(Math.random() * 4.0);
               int shardH = 2 + (int)(Math.random() * 3.0);
               matrices.push();
               matrices.translate(sx, sy, 0.0F);
               matrices.multiply(RotationAxis.POSITIVE_Z.rotation(shardAngle));
               context.fill(0, 0, shardW, shardH, shardColor);
               context.fill(0, 0, shardW, 1, shardEdge);
               matrices.pop();
            }
         }
      }
   }

   private void drawScreenTear(DrawContext context, MinecraftClient client, int screenW, int screenH) {
      Framebuffer fb = client.getFramebuffer();
      if (fb != null) {
         int fbTex = fb.getColorAttachment();
         if (fbTex != 0) {
            int fbW = fb.textureWidth;
            int fbH = fb.textureHeight;
            RenderSystem.setShaderTexture(0, fbTex);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            float uScale = (float)screenW / fbW;
            float vScale = (float)screenH / fbH;

            for (ScreenTearRenderer.CrackSegment seg : this.crackSegments) {
               float dx = seg.x2 - seg.x1;
               float dy = seg.y2 - seg.y1;
               float len = (float)Math.sqrt(dx * dx + dy * dy);
               if (!(len < 1.0F)) {
                  float nx = -dy / len;
                  float ny = dx / len;
                  float tearWidth = 8.0F * seg.intensity;
                  float displace = 4.0F * seg.intensity;
                  float x1 = seg.x1 + nx * 2.0F;
                  float y1 = seg.y1 + ny * 2.0F;
                  float x2 = seg.x2 + nx * 2.0F;
                  float y2 = seg.y2 + ny * 2.0F;
                  float dx1 = x1 + nx * tearWidth;
                  float dy1 = y1 + ny * tearWidth;
                  float dx2 = x2 + nx * tearWidth;
                  float dy2 = y2 + ny * tearWidth;
                  float sx1 = x1 + nx * displace;
                  float sy1 = y1 + ny * displace;
                  float sx2 = x2 + nx * displace;
                  float sy2 = y2 + ny * displace;
                  float sdx1 = dx1 + nx * displace;
                  float sdy1 = dy1 + ny * displace;
                  float sdx2 = dx2 + nx * displace;
                  float sdy2 = dy2 + ny * displace;
                  float u1 = x1 / fbW * uScale;
                  float v1 = y1 / fbH * vScale;
                  float u2 = x2 / fbW * uScale;
                  float v2 = y2 / fbH * vScale;
                  float du1 = dx1 / fbW * uScale;
                  float dv1 = dy1 / fbH * vScale;
                  float du2 = dx2 / fbW * uScale;
                  float dv2 = dy2 / fbH * vScale;
                  buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                  buffer.vertex(matrix, sx1, sy1, 0.0F).texture(u1, v1).next();
                  buffer.vertex(matrix, sdx1, sdy1, 0.0F).texture(du1, dv1).next();
                  buffer.vertex(matrix, sdx2, sdy2, 0.0F).texture(du2, dv2).next();
                  buffer.vertex(matrix, sx2, sy2, 0.0F).texture(u2, v2).next();
                  tess.draw();
               }
            }
         }
      }
   }

   private static int argb(int a, int r, int g, int b) {
      return Math.max(0, Math.min(255, a)) << 24 | Math.max(0, Math.min(255, r)) << 16 | Math.max(0, Math.min(255, g)) << 8 | Math.max(0, Math.min(255, b));
   }

   private void generateCracks(int width, int height, long generationSeed) {
      this.crackSegments.clear();
      Random rand = new Random(generationSeed);
      int numCracks = 3 + rand.nextInt(3);

      for (int i = 0; i < numCracks; i++) {
         int edge = rand.nextInt(4);
         float startX;
         float startY;
         switch (edge) {
            case 0:
               startX = rand.nextFloat() * width;
               startY = 0.0F;
               break;
            case 1:
               startX = rand.nextFloat() * width;
               startY = height;
               break;
            case 2:
               startX = 0.0F;
               startY = rand.nextFloat() * height;
               break;
            default:
               startX = width;
               startY = rand.nextFloat() * height;
         }

         float angle = (float)Math.atan2(height / 2.0F - startY, width / 2.0F - startX) + (rand.nextFloat() - 0.5F) * 1.2F;
         this.generateCrackBranch(startX, startY, angle, 0, rand, width, height, 1.0F);
      }
   }

   private void generateCrackBranch(float x, float y, float angle, int depth, Random rand, int width, int height, float parentIntensity) {
      if (depth <= 40) {
         if (!(x < -50.0F) && !(x > width + 50) && !(y < -50.0F) && !(y > height + 50)) {
            float segmentLength = 15.0F + rand.nextFloat() * 50.0F;
            float angleJitter = (rand.nextFloat() - 0.5F) * 0.8F;
            float newAngle = angle + angleJitter;
            float newX = x + (float)Math.cos(newAngle) * segmentLength;
            float newY = y + (float)Math.sin(newAngle) * segmentLength;
            float intensity = parentIntensity * (1.0F - depth / 40.0F * 0.6F);
            if (intensity < 0.15F) {
               intensity = 0.15F;
            }

            this.crackSegments.add(new ScreenTearRenderer.CrackSegment(x, y, newX, newY, intensity));
            if (rand.nextFloat() < 0.25F && depth > 2) {
               float branchAngle = newAngle + (rand.nextBoolean() ? 1 : -1) * (0.5F + rand.nextFloat() * 1.0F);
               this.generateCrackBranch(x, y, branchAngle, depth + 3, rand, width, height, intensity * 0.7F);
            }

            this.generateCrackBranch(newX, newY, newAngle, depth + 1, rand, width, height, intensity);
         }
      }
   }

   private static boolean isHoldingRustyBroadsword(PlayerEntity player) {
      ItemStack mainHand = player.getMainHandStack();
      ItemStack offHand = player.getOffHandStack();
      return mainHand.isOf(ModItems.RUSTY_OLD_BROADSWORD) && RustyOldBroadswordItem.isRangedMode(mainHand)
         ? true
         : offHand.isOf(ModItems.RUSTY_OLD_BROADSWORD) && RustyOldBroadswordItem.isRangedMode(offHand);
   }

   private void loadConfigFromItem(PlayerEntity player) {
      ItemStack stack = player.getMainHandStack();
      if (!stack.isOf(ModItems.RUSTY_OLD_BROADSWORD) || !RustyOldBroadswordItem.isRangedMode(stack)) {
         stack = player.getOffHandStack();
      }

      if (stack.isOf(ModItems.RUSTY_OLD_BROADSWORD) && RustyOldBroadswordItem.isRangedMode(stack)) {
         NbtCompound nbt = stack.getNbt();
         if (nbt != null) {
            if (nbt.contains("broadsword_crack_fixed")) {
               this.crackFixed = nbt.getBoolean("broadsword_crack_fixed");
            }
         }
      }
   }

   private static class CrackSegment {
      final float x1;
      final float y1;
      final float x2;
      final float y2;
      final float intensity;

      CrackSegment(float x1, float y1, float x2, float y2, float intensity) {
         this.x1 = x1;
         this.y1 = y1;
         this.x2 = x2;
         this.y2 = y2;
         this.intensity = intensity;
      }
   }

   private static class TearParams {
      final float r;
      final float g;
      final float b;
      final float intensity;

      TearParams(float r, float g, float b, float intensity) {
         this.r = r;
         this.g = g;
         this.b = b;
         this.intensity = intensity;
      }
   }
}
