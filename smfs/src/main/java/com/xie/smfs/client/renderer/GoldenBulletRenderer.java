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
      matrices.push();
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, bullet.prevYaw, bullet.getYaw()) - 90.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, bullet.prevPitch, bullet.getPitch())));
      matrices.scale(0.5F, 0.5F, 0.5F);
      super.render(bullet, yaw, tickDelta, matrices, vertexConsumers, light);
      matrices.pop();
   }

   public Identifier getTexture(GoldenBulletEntity bullet) {
      return TEXTURE;
   }
}
