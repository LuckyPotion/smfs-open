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
         if (this.renderPerspective == ModelTransformationMode.GUI) {
            matrices.translate(0.0, -0.65, 0.0);
            matrices.scale(0.58F, 0.58F, 0.58F);
         } else if (this.renderPerspective == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
            || this.renderPerspective == ModelTransformationMode.THIRD_PERSON_LEFT_HAND
            || this.renderPerspective == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
            || this.renderPerspective == ModelTransformationMode.FIRST_PERSON_LEFT_HAND) {
            matrices.translate(0.0, -0.25, 0.0);
            matrices.scale(0.5F, 0.5F, 0.5F);
         }
      }
   }
}
