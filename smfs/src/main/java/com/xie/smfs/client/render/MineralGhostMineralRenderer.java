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

public class MineralGhostMineralRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/MineralGhostMineralRenderer");
   private static final List<BlockPos> mineralPositions = new ArrayList<>();
   private static final List<Integer> oreTypes = new ArrayList<>();
   private static long lastUpdateTime = 0L;
   private static final long POSITION_VALID_TIME = 5000L;

   public static void setMineralPositions(List<BlockPos> positions, List<Integer> types) {
      mineralPositions.clear();
      oreTypes.clear();
      mineralPositions.addAll(positions);
      oreTypes.addAll(types);
      lastUpdateTime = System.currentTimeMillis();
      LOGGER.debug("更新矿物鬼矿物位置信息，共 {} 个矿物", positions.size());
   }

   public static void render(MatrixStack matrices, Camera camera) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         if (System.currentTimeMillis() - lastUpdateTime > 5000L) {
            mineralPositions.clear();
            oreTypes.clear();
         } else if (!mineralPositions.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            Vec3d cameraPos = camera.getPos();

            for (int i = 0; i < mineralPositions.size(); i++) {
               BlockPos pos = mineralPositions.get(i);
               int type = i < oreTypes.size() ? oreTypes.get(i) : 0;
               renderMineralMarker(matrices, buffer, cameraPos, pos, type);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
         }
      }
   }

   private static void renderMineralMarker(MatrixStack matrices, BufferBuilder buffer, Vec3d cameraPos, BlockPos pos, int oreType) {
      double x = pos.getX() + 0.5 - cameraPos.x;
      double y = pos.getY() + 0.5 - cameraPos.y;
      double z = pos.getZ() + 0.5 - cameraPos.z;
      float r;
      float g;
      float b;
      switch (oreType) {
         case 1:
            r = 1.0F;
            g = 0.85F;
            b = 0.0F;
            break;
         case 2:
            r = 0.7F;
            g = 0.2F;
            b = 1.0F;
            break;
         default:
            r = 0.0F;
            g = 0.5F;
            b = 1.0F;
      }

      float a = 0.8F;
      buffer.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      float size = 0.4F;
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
      mineralPositions.clear();
      oreTypes.clear();
      lastUpdateTime = 0L;
   }

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES.register((AfterEntities)context -> {
         MatrixStack matrices = context.matrixStack();
         Camera camera = context.camera();
         render(matrices, camera);
      });
      LOGGER.info("矿物鬼矿物位置渲染器注册完成");
   }
}
