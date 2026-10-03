package com.xie.smfs.client.renderer;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.registry.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GiantShadowGhostRenderer extends GeoObjectRenderer<GiantShadowGhostRenderer.GiantShadowGhostAnimatable> {
   private static double smoothedX = Double.NaN;
   private static double smoothedY = Double.NaN;
   private static double smoothedZ = Double.NaN;
   private static float smoothedYaw = Float.NaN;
   private static final float FOLLOW_LERP = 0.08F;

   public GiantShadowGhostRenderer() {
      super(new GiantShadowGhostRenderer.GiantShadowGhostGeoModel());
   }

   public void render(
      MatrixStack matrices,
      GiantShadowGhostRenderer.GiantShadowGhostAnimatable animatable,
      VertexConsumerProvider vertexConsumers,
      RenderLayer light,
      VertexConsumer partialTick,
      int packedOverlay
   ) {
      PlayerEntity player = this.getCurrentPlayer();
      if (player != null) {
         ClientWorld world = MinecraftClient.getInstance().world;
         if (world != null) {
            if (hasGiantShadowGhost(player)) {
               this.renderGiantShadowGhostOnPlayer(matrices, animatable, vertexConsumers, light, partialTick, packedOverlay, player);
            }
         }
      }
   }

   private void renderGiantShadowGhostOnPlayer(
      MatrixStack matrices,
      GiantShadowGhostRenderer.GiantShadowGhostAnimatable animatable,
      VertexConsumerProvider vertexConsumers,
      RenderLayer light,
      VertexConsumer partialTick,
      int packedOverlay,
      PlayerEntity player
   ) {
      matrices.push();
      Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      float tickDelta = MinecraftClient.getInstance().getTickDelta();
      Vec3d targetPos = player.getLerpedPos(tickDelta);
      float targetYaw = player.getYaw(tickDelta);
      if (Double.isNaN(smoothedX)) {
         smoothedX = targetPos.x;
         smoothedY = targetPos.y;
         smoothedZ = targetPos.z;
         smoothedYaw = targetYaw;
      } else {
         smoothedX = smoothedX + (targetPos.x - smoothedX) * 0.08F;
         smoothedY = smoothedY + (targetPos.y - smoothedY) * 0.08F;
         smoothedZ = smoothedZ + (targetPos.z - smoothedZ) * 0.08F;
         float yawDiff = targetYaw - smoothedYaw;

         while (yawDiff > 180.0F) {
            yawDiff -= 360.0F;
         }

         while (yawDiff < -180.0F) {
            yawDiff += 360.0F;
         }

         smoothedYaw += yawDiff * 0.08F;
      }

      double backOffsetX = Math.sin(Math.toRadians(smoothedYaw)) * 0.5;
      double backOffsetZ = -Math.cos(Math.toRadians(smoothedYaw)) * 0.5;
      double offsetY = -1.0;
      double worldX = smoothedX + backOffsetX;
      double worldY = smoothedY + offsetY;
      double worldZ = smoothedZ + backOffsetZ;
      matrices.translate(worldX - cameraPos.x, worldY - cameraPos.y, worldZ - cameraPos.z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - smoothedYaw));
      matrices.scale(0.6F, 0.6F, 0.6F);
      super.render(matrices, animatable, vertexConsumers, light, partialTick, packedOverlay);
      matrices.pop();
   }

   private PlayerEntity getCurrentPlayer() {
      return MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player : null;
   }

   private static boolean hasGiantShadowGhost(PlayerEntity player) {
      if (player == null) {
         return false;
      }

      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, mainSlot);
      return !ghostItem.isEmpty() && ghostItem.getItem() == ModItems.GIANT_SHADOW_GHOST;
   }

   public static class GiantShadowGhostAnimatable implements GeoAnimatable {
      private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

      @Override
      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public AnimatableInstanceCache getAnimatableInstanceCache() {
         return this.cache;
      }

      @Override
      public double getTick(Object object) {
         return 0.0;
      }
   }

   public static class GiantShadowGhostGeoModel extends GeoModel<GiantShadowGhostRenderer.GiantShadowGhostAnimatable> {
      public Identifier getModelResource(GiantShadowGhostRenderer.GiantShadowGhostAnimatable animatable) {
         return new Identifier("smfs", "geo/giant_shadow_ghost.geo.json");
      }

      public Identifier getTextureResource(GiantShadowGhostRenderer.GiantShadowGhostAnimatable animatable) {
         return new Identifier("smfs", "textures/entity/giant_shadow_ghost.png");
      }

      public Identifier getAnimationResource(GiantShadowGhostRenderer.GiantShadowGhostAnimatable animatable) {
         return new Identifier("smfs", "animations/giant_shadow_ghost.animation.json");
      }
   }
}
