package com.xie.smfs.client;

import com.xie.smfs.client.renderer.GhostPullRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterEntities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class GhostPullRenderHandler {
   private static GhostPullRenderer ghostPullRenderer;
   private static GhostPullRenderer.GhostPullAnimatable ghostPullAnimatable;

   public static void register() {
      WorldRenderEvents.AFTER_ENTITIES
         .register(
            (AfterEntities)context -> {
               MinecraftClient client = MinecraftClient.getInstance();
               if (client.player != null && client.world != null) {
                  if (ghostPullRenderer == null) {
                     ghostPullRenderer = new GhostPullRenderer();
                     ghostPullAnimatable = new GhostPullRenderer.GhostPullAnimatable();
                  }

                  MatrixStack matrices = context.matrixStack();
                  VertexConsumerProvider vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
                  matrices.push();
                  ghostPullRenderer.render(
                     matrices,
                     ghostPullAnimatable,
                     vertexConsumers,
                     RenderLayer.getEntityTranslucent(new Identifier("smfs", "textures/entity/ghost_pull.png")),
                     vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("smfs", "textures/entity/ghost_pull.png"))),
                     0
                  );
                  matrices.pop();
               }
            }
         );
   }
}
