package com.xie.smfs.client.renderer;

import com.xie.smfs.entity.other.GhostSlaveEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class GhostSlaveRenderer extends BipedEntityRenderer<GhostSlaveEntity, BipedEntityModel<GhostSlaveEntity>> {
   private static final Identifier TEXTURE = new Identifier("smfs", "textures/entity/ghost_slave.png");

   public GhostSlaveRenderer(Context ctx) {
      super(ctx, new BipedEntityModel(getModelPart(ctx)), 0.5F);
   }

   private static ModelPart getModelPart(Context ctx) {
      return ctx.getPart(EntityModelLayers.PLAYER);
   }

   public Identifier getTexture(GhostSlaveEntity entity) {
      return TEXTURE;
   }

   public void render(GhostSlaveEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (entity.isVisible()) {
         matrices.push();
         matrices.scale(1.2F, 1.2F, 1.2F);
         Identifier texture = this.getTexture(entity);
         VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
         super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
         matrices.pop();
      }
   }
}
