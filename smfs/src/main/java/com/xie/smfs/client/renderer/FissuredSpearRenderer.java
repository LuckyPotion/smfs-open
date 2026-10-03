package com.xie.smfs.client.renderer;

import com.xie.smfs.entity.other.FissuredSpearEntity;
import com.xie.smfs.item.FissuredSpearRedItem;
import com.xie.smfs.item.WishSpearItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FissuredSpearRenderer extends GeoEntityRenderer<FissuredSpearEntity> {
   public FissuredSpearRenderer(Context context) {
      super(context, new FissuredSpearRenderer.FissuredSpearModel());
   }

   public void render(FissuredSpearEntity spear, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      matrices.method_22903();
      float renderYaw;
      float renderPitch;
      if (spear.isStuck()) {
         renderYaw = spear.getStuckYaw();
         renderPitch = spear.getStuckPitch();
      } else {
         renderYaw = MathHelper.method_16439(tickDelta, spear.field_5982, spear.method_36454());
         renderPitch = MathHelper.method_16439(tickDelta, spear.field_6004, spear.method_36455());
      }

      matrices.method_22907(RotationAxis.field_40716.rotationDegrees(renderYaw - 90.0F));
      matrices.method_22907(RotationAxis.field_40718.rotationDegrees(renderPitch + 90.0F));
      if (spear.isStuck()) {
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees(180.0F));
         matrices.method_22904(0.0, 0.0, 0.35);
      }

      matrices.method_22905(0.5F, 0.5F, 0.5F);
      super.method_3936(spear, yaw, tickDelta, matrices, vertexConsumers, light);
      matrices.method_22909();
   }

   public static class FissuredSpearModel extends GeoModel<FissuredSpearEntity> {
      public Identifier getModelResource(FissuredSpearEntity animatable) {
         return new Identifier("smfs", "geo/fissured_spear.geo.json");
      }

      public Identifier getTextureResource(FissuredSpearEntity animatable) {
         ItemStack stack = animatable.getOriginalStack();
         if (stack.method_7909() instanceof WishSpearItem) {
            return new Identifier("smfs", "textures/item/wish_spear.png");
         } else {
            return stack.method_7909() instanceof FissuredSpearRedItem
               ? new Identifier("smfs", "textures/item/fissured_spear_red.png")
               : new Identifier("smfs", "textures/item/fissured_spear_purple.png");
         }
      }

      public Identifier getAnimationResource(FissuredSpearEntity animatable) {
         return null;
      }
   }
}
