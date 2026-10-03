package com.xie.smfs.client.renderer.item;

import com.xie.smfs.client.model.item.GraveMoundItemModel;
import com.xie.smfs.item.GraveMoundBlockItem;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GraveMoundItemRenderer extends GeoItemRenderer<GraveMoundBlockItem> implements RenderProvider {
   private ModelTransformationMode currentMode = ModelTransformationMode.NONE;

   public GraveMoundItemRenderer() {
      super(new GraveMoundItemModel());
   }

   public BuiltinModelItemRenderer getCustomRenderer() {
      return this;
   }

   public void method_3166(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
      this.currentMode = mode;
      super.method_3166(stack, mode, matrices, vertexConsumers, light, overlay);
   }

   public void preRender(
      MatrixStack matrices,
      GraveMoundBlockItem animatable,
      BakedGeoModel model,
      VertexConsumerProvider vertexConsumers,
      VertexConsumer buffer,
      boolean isReRender,
      float tickDelta,
      int light,
      int overlay,
      float red,
      float green,
      float blue,
      float alpha
   ) {
      super.preRender(matrices, animatable, model, vertexConsumers, buffer, isReRender, tickDelta, light, overlay, red, green, blue, alpha);
      if (!isReRender) {
         if (this.currentMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || this.currentMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            matrices.scale(0.5F, 0.5F, 0.5F);
         } else if (this.currentMode != ModelTransformationMode.THIRD_PERSON_LEFT_HAND && this.currentMode != ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(225.0F));
            matrices.translate(0.0, -0.2, 0.0);
            matrices.scale(0.5F, 0.5F, 0.5F);
         } else {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            matrices.scale(0.5F, 0.5F, 0.5F);
         }
      }
   }
}
