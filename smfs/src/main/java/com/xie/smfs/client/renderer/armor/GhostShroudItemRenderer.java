package com.xie.smfs.client.renderer.armor;

import com.xie.smfs.client.model.armor.GhostShroudArmorModel;
import com.xie.smfs.item.GhostShroudArmorItem;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GhostShroudItemRenderer extends GeoItemRenderer<GhostShroudArmorItem> {
   public GhostShroudItemRenderer() {
      super(new GhostShroudArmorModel());
   }

   public void preRender(
      MatrixStack matrices,
      GhostShroudArmorItem animatable,
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
         if (this.renderPerspective == ModelTransformationMode.field_4317) {
            matrices.method_22904(0.0, -0.65, 0.0);
            matrices.method_22905(0.58F, 0.58F, 0.58F);
         } else if (this.renderPerspective == ModelTransformationMode.field_4320
            || this.renderPerspective == ModelTransformationMode.field_4323
            || this.renderPerspective == ModelTransformationMode.field_4322
            || this.renderPerspective == ModelTransformationMode.field_4321) {
            matrices.method_22904(0.0, -0.25, 0.0);
            matrices.method_22905(0.5F, 0.5F, 0.5F);
         }
      }
   }
}
