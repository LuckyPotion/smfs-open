package com.xie.smfs.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterEntities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxGhostContainerRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/BoxGhostContainerRenderer");
   private static List<BlockPos> containerPositions = new ArrayList<>();
   private static long lastUpdateTime = 0L;
   private static final long POSITION_VALID_TIME = 5000L;

   public static void setContainerPositions(List<BlockPos> positions) {
      containerPositions = new ArrayList<>(positions);
      lastUpdateTime = System.currentTimeMillis();
      LOGGER.debug("更新开箱鬼箱子位置信息，共 {} 个箱子", positions.size());
   }

   public static void render(MatrixStack matrices, Camera camera) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         if (System.currentTimeMillis() - lastUpdateTime > 5000L) {
            containerPositions.clear();
         } else if (!containerPositions.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            Vec3d cameraPos = camera.getPos();

            for (BlockPos pos : containerPositions) {
               renderContainerMarker(matrices, buffer, cameraPos, pos);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
         }
      }
   }

   private static void renderContainerMarker(MatrixStack matrices, BufferBuilder buffer, Vec3d cameraPos, BlockPos pos) {
      double x = pos.getX() + 0.5 - cameraPos.x;
      double y = pos.getY() + 1.2 - cameraPos.y;
      double z = pos.getZ() + 0.5 - cameraPos.z;
      float r = 1.0F;
      float g = 0.8F;
      float b = 0.0F;
      float a = 0.8F;
      buffer.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      float size = 0.3F;
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y - size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y - size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y - size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y - size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y - size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y - size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y - size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y - size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y + size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y + size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y + size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y + size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y + size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y + size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y + size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y + size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y - size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y + size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y - size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y + size), (float)(z - size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y - size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x + size), (float)(y + size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y - size), (float)(z + size)).color(r, g, b, a).next();
      buffer.vertex(matrices.peek().getPositionMatrix(), (float)(x - size), (float)(y + size), (float)(z + size)).color(r, g, b, a).next();
      Tessellator.getInstance().draw();
   }

   public static void clearPositions() {
      containerPositions.clear();
      lastUpdateTime = 0L;
   }

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES.register((AfterEntities)context -> {
         MatrixStack matrices = context.matrixStack();
         Camera camera = context.camera();
         render(matrices, camera);
      });
      LOGGER.info("开箱鬼箱子位置渲染器注册完成");
   }
}
