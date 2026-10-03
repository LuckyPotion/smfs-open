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
      this.blockRenderer = context.method_43337();
      this.field_4673 = 0.2F;
   }

   public void render(FloatingDirtEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      super.method_3936(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      matrices.method_22903();
      matrices.method_22905(0.9F, 0.9F, 0.9F);
      BakedModel model = this.blockRenderer.method_3349(Blocks.field_10566.method_9564());
      this.blockRenderer.method_3353(Blocks.field_10566.method_9564(), matrices, vertexConsumers, light, OverlayTexture.field_21444);
      matrices.method_22909();
   }

   public Identifier getTexture(FloatingDirtEntity entity) {
      return null;
   }
}
