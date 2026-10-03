package com.xie.smfs.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xie.smfs.Smfs;
import com.xie.smfs.client.EffectRenderHandler;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.manager.GhostDreamManager;
import com.xie.smfs.registry.ModEffects;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.SkyRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class GhostDomainSkyRenderer implements SkyRenderer {
   private static final float TOP_COLOR_FACTOR = 0.6F;
   private static final float BOTTOM_COLOR_FACTOR = 0.6F;

   private static boolean shouldSkipRender() {
      return ModConfig.getInstance().irisCompatibilityMode == 0 ? false : isIrisShaderPackActive();
   }

   public static boolean isIrisShaderPackActive() {
      try {
         Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
         return (Boolean)irisClass.getMethod("isPackInUseQuick").invoke(null);
      } catch (Exception e) {
         return false;
      }
   }

   public static void renderAtLast(WorldRenderContext context) {
      if (isIrisShaderPackActive()) {
         if (ModConfig.getInstance().irisCompatibilityMode != 1) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
               float[] tint = computeGhostDomainTint();
               if (!(tint[3] <= 0.001F)) {
                  int oldDepthFunc = GL11.glGetInteger(2932);
                  boolean depthTestWasEnabled = GL11.glIsEnabled(2929);
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthFunc(515);
                  renderSkyDome(context, tint);
                  RenderSystem.depthFunc(oldDepthFunc);
                  if (!depthTestWasEnabled) {
                     RenderSystem.disableDepthTest();
                  }
               }
            }
         }
      }
   }

   private static void renderSkyDome(WorldRenderContext context, float[] tint) {
      MatrixStack matrices = context.matrixStack();
      float tickDelta = context.tickDelta();
      if (context.world() != null) {
         float[] finalTint = adjustTintForDimension(context, tint);
         float mix = finalTint[3];
         Vec3d worldSky = context.world().getSkyColor(context.camera().getPos(), tickDelta);
         float baseR = (float)worldSky.x;
         float baseG = (float)worldSky.y;
         float baseB = (float)worldSky.z;
         float finalR;
         float finalG;
         float finalB;
         if (mix >= 0.999F) {
            finalR = finalTint[0];
            finalG = finalTint[1];
            finalB = finalTint[2];
         } else {
            finalR = MathHelper.clamp(baseR * (1.0F - mix) + finalTint[0] * mix, 0.0F, 1.0F);
            finalG = MathHelper.clamp(baseG * (1.0F - mix) + finalTint[1] * mix, 0.0F, 1.0F);
            finalB = MathHelper.clamp(baseB * (1.0F - mix) + finalTint[2] * mix, 0.0F, 1.0F);
         }

         Tessellator tess = Tessellator.getInstance();
         BufferBuilder buffer = tess.getBuffer();
         matrices.push();
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         float topR = finalR * 0.6F;
         float topG = finalG * 0.6F;
         float topB = finalB * 0.6F;
         float bottomR = finalR * 0.6F;
         float bottomG = finalG * 0.6F;
         float bottomB = finalB * 0.6F;
         RenderSystem.setShader(GameRenderer::getPositionColorProgram);
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         int renderDistance = (Integer)MinecraftClient.getInstance().options.getViewDistance().getValue();
         float size = Math.max(renderDistance * 32.0F, 256.0F);
         float yTop = size * 0.1F;
         float yBottom = -size * 0.1F;
         buffer.vertex(matrix, -size, yTop, -size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, size, yTop, -size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, size, yTop, size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, -size, yTop, size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, -size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, -size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, -size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, size, yTop, -size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, -size, yTop, -size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, -size, yTop, size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, size, yTop, size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, -size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, -size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, -size, yTop, -size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, -size, yTop, size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, -size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
         buffer.vertex(matrix, size, yTop, size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, size, yTop, -size).color(topR, topG, topB, 1.0F).next();
         buffer.vertex(matrix, size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
         tess.draw();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         matrices.pop();
      }
   }

   private static float[] adjustTintForDimension(WorldRenderContext context, float[] tint) {
      if (context.world().getRegistryKey() == Smfs.SPIRIT_REALM_DIMENSION) {
         boolean hasEffect = EffectRenderHandler.hasAnyGhostDomainEffect(MinecraftClient.getInstance().player);
         if (!hasEffect) {
            return new float[]{0.0F, 0.0F, 0.0F, 1.0F};
         }
      }

      if (context.world().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
         boolean hasEffect = EffectRenderHandler.hasAnyGhostDomainEffect(MinecraftClient.getInstance().player);
         if (!hasEffect) {
            float[] skyColor = getGhostDreamSkyColor();
            return new float[]{skyColor[0], skyColor[1], skyColor[2], 1.0F};
         }
      }

      return tint;
   }

   public void render(WorldRenderContext context) {
      if (!shouldSkipRender()) {
         MatrixStack matrices = context.matrixStack();
         float tickDelta = context.tickDelta();
         if (context.world() != null) {
            float[] tint = computeGhostDomainTint();
            if (context.world().getRegistryKey() == Smfs.SPIRIT_REALM_DIMENSION) {
               boolean hasGhostDomainEffect = false;
               PlayerEntity player = MinecraftClient.getInstance().player;
               if (player != null) {
                  hasGhostDomainEffect = EffectRenderHandler.hasAnyGhostDomainEffect(player);
               }

               if (!hasGhostDomainEffect) {
                  tint = new float[]{0.0F, 0.0F, 0.0F, 1.0F};
               }
            }

            if (context.world().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
               boolean hasGhostDomainEffect = false;
               PlayerEntity player = MinecraftClient.getInstance().player;
               if (player != null) {
                  hasGhostDomainEffect = EffectRenderHandler.hasAnyGhostDomainEffect(player);
               }

               if (!hasGhostDomainEffect) {
                  float[] skyColor = getGhostDreamSkyColor();
                  tint = new float[]{skyColor[0], skyColor[1], skyColor[2], 1.0F};
               }
            }

            Vec3d worldSky = context.world().getSkyColor(context.camera().getPos(), tickDelta);
            float baseR = (float)worldSky.x;
            float baseG = (float)worldSky.y;
            float baseB = (float)worldSky.z;
            float mix = tint[3];
            float finalR;
            float finalG;
            float finalB;
            if (mix >= 0.999F) {
               finalR = tint[0];
               finalG = tint[1];
               finalB = tint[2];
            } else {
               finalR = MathHelper.clamp(baseR * (1.0F - mix) + tint[0] * mix, 0.0F, 1.0F);
               finalG = MathHelper.clamp(baseG * (1.0F - mix) + tint[1] * mix, 0.0F, 1.0F);
               finalB = MathHelper.clamp(baseB * (1.0F - mix) + tint[2] * mix, 0.0F, 1.0F);
            }

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            matrices.push();
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float topR = finalR * 0.6F;
            float topG = finalG * 0.6F;
            float topB = finalB * 0.6F;
            float bottomR = finalR * 0.6F;
            float bottomG = finalG * 0.6F;
            float bottomB = finalB * 0.6F;
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            int renderDistance = (Integer)MinecraftClient.getInstance().options.getViewDistance().getValue();
            float size = Math.max(renderDistance * 32.0F, 256.0F);
            float yTop = size * 0.1F;
            float yBottom = -size * 0.1F;
            buffer.vertex(matrix, -size, yTop, -size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, size, yTop, -size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, size, yTop, size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, -size, yTop, size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, -size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, -size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, -size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, size, yTop, -size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, -size, yTop, -size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, -size, yTop, size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, size, yTop, size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, -size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, -size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, -size, yTop, -size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, -size, yTop, size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, -size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, size, yBottom, size).color(bottomR, bottomG, bottomB, 1.0F).next();
            buffer.vertex(matrix, size, yTop, size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, size, yTop, -size).color(topR, topG, topB, 1.0F).next();
            buffer.vertex(matrix, size, yBottom, -size).color(bottomR, bottomG, bottomB, 1.0F).next();
            tess.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            matrices.pop();
         }
      }
   }

   private static float[] computeGhostDomainTint() {
      PlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return new float[]{0.0F, 0.0F, 0.0F, 0.0F};
      }

      StatusEffectInstance e = player.getStatusEffect(ModEffects.RED_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
      }

      if (e != null) {
         return new float[]{0.9F, 0.0F, 0.0F, 1.0F};
      }

      e = player.getStatusEffect(ModEffects.GREEN_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
      }

      if (e != null) {
         return new float[]{0.1F, 1.0F, 0.1F, 0.65F};
      }

      e = player.getStatusEffect(ModEffects.BLUE_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
      }

      if (e != null) {
         return new float[]{0.1F, 0.1F, 1.0F, 0.65F};
      }

      e = player.getStatusEffect(ModEffects.GRAY_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
      }

      if (e != null) {
         return new float[]{0.6F, 0.6F, 0.6F, 1.0F};
      }

      e = player.getStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
      }

      if (e != null) {
         return new float[]{1.0F, 0.84F, 0.0F, 0.6F};
      }

      e = player.getStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
      }

      if (e == null) {
         e = player.getStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_VISUAL);
      }

      if (e != null) {
         return new float[]{0.6F, 0.0F, 0.6F, 0.6F};
      }

      e = player.getStatusEffect(ModEffects.BLACK_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
      }

      if (e != null) {
         return new float[]{0.0F, 0.0F, 0.0F, 1.0F};
      }

      e = player.getStatusEffect(ModEffects.CYAN_GHOST_DOMAIN);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
      }

      if (e != null) {
         return new float[]{0.0F, 1.0F, 1.0F, 0.6F};
      }

      e = player.getStatusEffect(ModEffects.THICK_FOG);
      if (e == null) {
         e = player.getStatusEffect(ModEffects.THICK_FOG_TARGET);
      }

      if (e != null) {
         return new float[]{0.8F, 0.8F, 0.8F, 0.6F};
      }

      e = player.getStatusEffect(StatusEffects.DARKNESS);
      return e != null ? new float[]{0.0F, 0.0F, 0.0F, 1.0F} : new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   }

   private static float[] getGhostDreamSkyColor() {
      PlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return new float[]{0.4F, 0.4F, 0.4F};
      }

      long remainingTime = GhostDreamManager.getRemainingGameTime(player);
      long totalTime = GhostDreamManager.getTotalGameTime();
      long elapsedTime = totalTime - remainingTime;
      long gameTimeOfDay = (20000L + elapsedTime * 2L) % 24000L;
      float grayR = 0.4F;
      float grayG = 0.4F;
      float grayB = 0.4F;
      float blackR = 0.0F;
      float blackG = 0.0F;
      float blackB = 0.0F;
      if (gameTimeOfDay < 20000L && gameTimeOfDay >= 8000L) {
         return new float[]{grayR, grayG, grayB};
      }

      if (gameTimeOfDay >= 20000L) {
         float progress = (float)(gameTimeOfDay - 20000L) / 4000.0F;
         return interpolateColor(grayR, grayG, grayB, blackR, blackG, blackB, progress);
      }

      if (gameTimeOfDay < 2000L) {
         return new float[]{blackR, blackG, blackB};
      }

      if (gameTimeOfDay < 3000L) {
         return new float[]{blackR, blackG, blackB};
      }

      if (gameTimeOfDay < 5000L) {
         return new float[]{blackR, blackG, blackB};
      }

      float progress = (float)(gameTimeOfDay - 5000L) / 3000.0F;
      return interpolateColor(blackR, blackG, blackB, grayR, grayG, grayB, progress);
   }

   private static float[] interpolateColor(float r1, float g1, float b1, float r2, float g2, float b2, float progress) {
      float r = r1 + (r2 - r1) * progress;
      float g = g1 + (g2 - g1) * progress;
      float b = b1 + (b2 - b1) * progress;
      return new float[]{r, g, b};
   }
}
