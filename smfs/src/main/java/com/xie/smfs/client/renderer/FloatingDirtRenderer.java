package com.xie.smfs.client.renderer;

import com.xie.smfs.entity.other.FloatingDirtEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FloatingDirtRenderer extends EntityRenderer<FloatingDirtEntity> {
   private final BlockRenderManager blockRenderer;

   public FloatingDirtRenderer(Context context) {
      super(context);
      this.blockRenderer = context.getBlockRenderManager();
      this.shadowRadius = 0.2F;
   }

   public void render(FloatingDirtEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      matrices.push();
      matrices.scale(0.9F, 0.9F, 0.9F);
      BakedModel model = this.blockRenderer.getModel(Blocks.DIRT.getDefaultState());
      this.blockRenderer.renderBlockAsEntity(Blocks.DIRT.getDefaultState(), matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
      matrices.pop();
   }

   public Identifier getTexture(FloatingDirtEntity entity) {
      return null;
   }
}
