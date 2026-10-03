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
               MinecraftClient client = MinecraftClient.method_1551();
               if (client.field_1724 != null && client.field_1687 != null) {
                  if (ghostPullRenderer == null) {
                     ghostPullRenderer = new GhostPullRenderer();
                     ghostPullAnimatable = new GhostPullRenderer.GhostPullAnimatable();
                  }

                  MatrixStack matrices = context.matrixStack();
                  VertexConsumerProvider vertexConsumers = client.method_22940().method_23000();
                  matrices.method_22903();
                  ghostPullRenderer.render(
                     matrices,
                     ghostPullAnimatable,
                     vertexConsumers,
                     RenderLayer.method_23580(new Identifier("smfs", "textures/entity/ghost_pull.png")),
                     vertexConsumers.getBuffer(RenderLayer.method_23580(new Identifier("smfs", "textures/entity/ghost_pull.png"))),
                     0
                  );
                  matrices.method_22909();
               }
            }
         );
   }
}
