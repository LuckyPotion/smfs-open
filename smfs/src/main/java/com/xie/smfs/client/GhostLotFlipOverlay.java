package com.xie.smfs.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class GhostLotFlipOverlay {
   private static final Identifier CARD_BACK = new Identifier("smfs", "textures/gui/ghost_lot_card_back.png");
   private static final Identifier CARD_LIFE = new Identifier("smfs", "textures/gui/ghost_lot_card_life.png");
   private static final Identifier CARD_DEATH = new Identifier("smfs", "textures/gui/ghost_lot_card_death.png");
   private static final int CARD_WIDTH = 120;
   private static final int CARD_HEIGHT = 180;
   private static boolean isActive = false;
   private static boolean isLifeLot = false;
   private static long startTime = 0L;
   private static float duration = 2.0F;
   private static float holdDuration = 2.0F;

   public static void triggerFlip(boolean lifeLot, float animDuration) {
      isActive = true;
      isLifeLot = lifeLot;
      startTime = System.currentTimeMillis();
      duration = animDuration;
   }

   public static void register() {
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, tickDelta) -> {
         if (isActive) {
            MinecraftClient client = MinecraftClient.method_1551();
            if (client.field_1724 != null) {
               long elapsed = System.currentTimeMillis() - startTime;
               float totalDuration = duration + holdDuration;
               float progress = MathHelper.method_15363((float)elapsed / (duration * 1000.0F), 0.0F, 1.0F);
               if ((float)elapsed >= totalDuration * 1000.0F) {
                  isActive = false;
               } else {
                  renderFlipAnimation(context, progress, client);
               }
            }
         }
      });
   }

   private static void renderFlipAnimation(DrawContext context, float progress, MinecraftClient client) {
      int screenWidth = context.method_51421();
      int screenHeight = context.method_51443();
      int centerX = screenWidth / 2;
      int centerY = screenHeight / 2;
      MatrixStack matrices = context.method_51448();
      matrices.method_22903();
      float alpha;
      float horizontalScale;
      boolean showFront;
      if (progress < 0.1F) {
         alpha = progress / 0.1F;
         horizontalScale = 1.0F;
         showFront = false;
      } else if (progress < 0.85F) {
         alpha = 1.0F;
         float flipProgress = (progress - 0.1F) / 0.75F;
         float flipPhase = flipProgress * 3.0F;
         int cycleIndex = (int)Math.floor(flipPhase);
         float cycleProgress = flipPhase - cycleIndex;
         horizontalScale = 1.0F - Math.abs(cycleProgress * 2.0F - 1.0F);
         if (cycleIndex < 2) {
            showFront = false;
         } else {
            showFront = cycleProgress > 0.5F;
         }
      } else {
         alpha = 1.0F;
         horizontalScale = 1.0F;
         showFront = true;
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::method_34542);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      matrices.method_46416(centerX, centerY, 0.0F);
      matrices.method_22905(horizontalScale, 1.0F, 1.0F);
      Identifier texture;
      if (showFront) {
         texture = isLifeLot ? CARD_LIFE : CARD_DEATH;
      } else {
         texture = CARD_BACK;
      }

      RenderSystem.setShaderTexture(0, texture);
      int x = -60;
      int y = -90;
      Matrix4f positionMatrix = matrices.method_23760().method_23761();
      BufferBuilder bufferBuilder = Tessellator.method_1348().method_1349();
      bufferBuilder.method_1328(DrawMode.field_27382, VertexFormats.field_1585);
      bufferBuilder.method_22918(positionMatrix, x, y + 180, 0.0F).method_22913(0.0F, 1.0F).method_1344();
      bufferBuilder.method_22918(positionMatrix, x + 120, y + 180, 0.0F).method_22913(1.0F, 1.0F).method_1344();
      bufferBuilder.method_22918(positionMatrix, x + 120, y, 0.0F).method_22913(1.0F, 0.0F).method_1344();
      bufferBuilder.method_22918(positionMatrix, x, y, 0.0F).method_22913(0.0F, 0.0F).method_1344();
      BufferRenderer.method_43433(bufferBuilder.method_1326());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
      matrices.method_22909();
   }
}
