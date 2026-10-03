package com.xie.smfs.client.renderer;

import com.xie.smfs.entity.other.GoldenBulletEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class GoldenBulletRenderer extends EntityRenderer<GoldenBulletEntity> {
   private static final Identifier TEXTURE = new Identifier("smfs", "textures/entity/projectile/golden_bullet.png");

   public GoldenBulletRenderer(Context context) {
      super(context);
   }

   public void render(GoldenBulletEntity bullet, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      matrices.method_22903();
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees(MathHelper.method_16439(tickDelta, bullet.field_5982, bullet.method_36454()) - 90.0F));
      matrices.method_22907(RotationAxis.field_40718.rotationDegrees(MathHelper.method_16439(tickDelta, bullet.field_6004, bullet.method_36455())));
      matrices.method_22905(0.5F, 0.5F, 0.5F);
      super.method_3936(bullet, yaw, tickDelta, matrices, vertexConsumers, light);
      matrices.method_22909();
   }

   public Identifier getTexture(GoldenBulletEntity bullet) {
      return TEXTURE;
   }
}
