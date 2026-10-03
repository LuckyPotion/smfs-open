package com.xie.smfs.client.renderer.item;

import com.xie.smfs.client.model.item.RustyOldBroadswordModel;
import com.xie.smfs.item.RustyOldBroadswordItem;
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

public class RustyOldBroadswordRenderer extends GeoItemRenderer<RustyOldBroadswordItem> implements RenderProvider {
   private ModelTransformationMode currentMode = ModelTransformationMode.field_4315;

   public RustyOldBroadswordRenderer() {
      super(new RustyOldBroadswordModel());
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
      RustyOldBroadswordItem animatable,
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
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees(180.0F));
         if (this.currentMode == ModelTransformationMode.field_4321 || this.currentMode == ModelTransformationMode.field_4322) {
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(180.0F));
            matrices.method_22905(1.0F, 1.0F, 1.0F);
         } else if (this.currentMode != ModelTransformationMode.field_4323 && this.currentMode != ModelTransformationMode.field_4320) {
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(225.0F));
            matrices.method_22904(0.0, 0.2, 0.0);
            matrices.method_22905(0.75F, 0.75F, 0.75F);
         } else {
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(15.0F));
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(180.0F));
            matrices.method_22905(1.0F, 1.0F, 1.0F);
         }
      }
   }
}
