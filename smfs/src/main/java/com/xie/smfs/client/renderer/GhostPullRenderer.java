package com.xie.smfs.client.renderer;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.network.packets.common.s2c.GhostPressureDetectionPacket;
import com.xie.smfs.registry.ModItems;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhostPullRenderer extends GeoObjectRenderer<GhostPullRenderer.GhostPullAnimatable> {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostPullRenderer");
   private static final Map<UUID, Boolean> serverDetectionCache = new HashMap<>();
   private static final Map<UUID, Long> lastRequestTime = new HashMap<>();
   private static final long REQUEST_COOLDOWN = 1000L;
   private static double smoothedPullX = Double.NaN;
   private static double smoothedPullY = Double.NaN;
   private static double smoothedPullZ = Double.NaN;
   private static float smoothedPullYaw = Float.NaN;
   private static final float FOLLOW_LERP = 0.08F;

   public GhostPullRenderer() {
      super(new GhostPullRenderer.GhostPullGeoModel());
   }

   public void render(
      MatrixStack matrices,
      GhostPullRenderer.GhostPullAnimatable animatable,
      VertexConsumerProvider vertexConsumers,
      RenderLayer light,
      VertexConsumer partialTick,
      int packedOverlay
   ) {
      PlayerEntity player = this.getCurrentPlayer();
      if (player != null) {
         ClientWorld world = MinecraftClient.method_1551().field_1687;
         if (world != null) {
            this.renderAllGhostPressureTargets(matrices, animatable, vertexConsumers, light, partialTick, packedOverlay, world, player);
            this.renderGiantShadowGhost(matrices, vertexConsumers, world, player);
         }
      }
   }

   private void renderGiantShadowGhost(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ClientWorld world, PlayerEntity player) {
      if (hasGiantShadowGhost(player)) {
         float tickDelta = MinecraftClient.method_1551().method_1488();
         Vec3d targetPos = player.method_30950(tickDelta);
         float targetYaw = player.method_5705(tickDelta);
         if (Double.isNaN(smoothedPullX)) {
            smoothedPullX = targetPos.field_1352;
            smoothedPullY = targetPos.field_1351;
            smoothedPullZ = targetPos.field_1350;
            smoothedPullYaw = targetYaw;
         } else {
            smoothedPullX = smoothedPullX + (targetPos.field_1352 - smoothedPullX) * 0.08F;
            smoothedPullY = smoothedPullY + (targetPos.field_1351 - smoothedPullY) * 0.08F;
            smoothedPullZ = smoothedPullZ + (targetPos.field_1350 - smoothedPullZ) * 0.08F;
            float yawDiff = targetYaw - smoothedPullYaw;

            while (yawDiff > 180.0F) {
               yawDiff -= 360.0F;
            }

            while (yawDiff < -180.0F) {
               yawDiff += 360.0F;
            }

            smoothedPullYaw += yawDiff * 0.08F;
         }

         if (!player.method_5715()) {
            Camera camera = MinecraftClient.method_1551().field_1773.method_19418();
            Vec3d cameraPos = camera.method_19326();
            int mainSlot = MainGhostManager.getMainGhostSlot(player);
            String ghostType = PlayerEvents.getGhostTypeInSlot(player, mainSlot);
            boolean isCompleteShadowGhost = "complete_shadow_ghost".equals(ghostType);
            GeoObjectRenderer<GhostPullRenderer.GiantShadowGhostAnimatable> renderer = new GeoObjectRenderer<>(
               new GhostPullRenderer.GiantShadowGhostGeoModel(isCompleteShadowGhost)
            );
            GhostPullRenderer.GiantShadowGhostAnimatable giantShadowGhostAnimatable = new GhostPullRenderer.GiantShadowGhostAnimatable();
            matrices.method_22903();
            double backOffsetX = Math.sin(Math.toRadians(smoothedPullYaw)) * 0.5;
            double backOffsetZ = -Math.cos(Math.toRadians(smoothedPullYaw)) * 0.5;
            double rightOffsetX = Math.cos(Math.toRadians(smoothedPullYaw)) * 0.4;
            double rightOffsetZ = Math.sin(Math.toRadians(smoothedPullYaw)) * 0.4;
            double offsetY = -0.4;
            double worldX = smoothedPullX + backOffsetX + rightOffsetX;
            double worldY = smoothedPullY + offsetY;
            double worldZ = smoothedPullZ + backOffsetZ + rightOffsetZ;
            matrices.method_22904(worldX - cameraPos.field_1352, worldY - cameraPos.field_1351, worldZ - cameraPos.field_1350);
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(180.0F - smoothedPullYaw));
            matrices.method_22905(0.85F, 0.85F, 0.85F);
            Identifier textureId = isCompleteShadowGhost
               ? new Identifier("smfs", "textures/entity/complete_shadow_ghost.png")
               : new Identifier("smfs", "textures/entity/giant_shadow_ghost.png");
            RenderLayer giantShadowGhostLayer = RenderLayer.method_23580(textureId);
            renderer.render(matrices, giantShadowGhostAnimatable, vertexConsumers, giantShadowGhostLayer, vertexConsumers.getBuffer(giantShadowGhostLayer), 0);
            matrices.method_22909();
         }
      }
   }

   private void renderAllGhostPressureTargets(
      MatrixStack matrices,
      GhostPullRenderer.GhostPullAnimatable animatable,
      VertexConsumerProvider vertexConsumers,
      RenderLayer light,
      VertexConsumer partialTick,
      int packedOverlay,
      ClientWorld world,
      PlayerEntity currentPlayer
   ) {
      Camera camera = MinecraftClient.method_1551().field_1773.method_19418();
      Vec3d cameraPos = camera.method_19326();
      Collection<LivingEntity> allEntities = world.method_8390(LivingEntity.class, currentPlayer.method_5829().method_1014(64.0), e -> true);
      boolean playerHasGhostPressure = hasGhostPressureItem(currentPlayer);
      boolean playerNotOnCooldown = !isGhostPressureSkillOnCooldown(currentPlayer);
      if (playerHasGhostPressure && playerNotOnCooldown) {
         boolean isTarget = false;
         this.renderGhostPullOnEntity(matrices, animatable, vertexConsumers, light, partialTick, packedOverlay, currentPlayer, cameraPos, isTarget);
      }

      for (LivingEntity entity : allEntities) {
         boolean hasGhostPressureEffect = this.checkGhostPressureEffect(entity);
         if (hasGhostPressureEffect) {
            boolean isTarget = true;
            this.renderGhostPullOnEntity(matrices, animatable, vertexConsumers, light, partialTick, packedOverlay, entity, cameraPos, isTarget);
         }
      }
   }

   private boolean checkGhostPressureEffect(LivingEntity entity) {
      UUID entityId = entity.method_5667();
      if (serverDetectionCache.containsKey(entityId)) {
         boolean result = serverDetectionCache.get(entityId);
         if (result) {
            return true;
         }
      }

      this.requestServerDetection(entity);
      return false;
   }

   private void requestServerDetection(LivingEntity entity) {
      UUID entityId = entity.method_5667();
      long currentTime = System.currentTimeMillis();
      if (lastRequestTime.containsKey(entityId)) {
         long lastTime = lastRequestTime.get(entityId);
         if (currentTime - lastTime < 1000L) {
            return;
         }
      }

      if (MinecraftClient.method_1551().method_1562() != null) {
         PacketByteBuf buf = GhostPressureDetectionPacket.createClientRequestPacket(entityId);
         ClientPlayNetworking.send(GhostPressureDetectionPacket.PACKET_ID, buf);
         lastRequestTime.put(entityId, currentTime);
      }
   }

   public static void updateServerDetectionResult(UUID entityId, boolean hasGhostPressure) {
      serverDetectionCache.put(entityId, hasGhostPressure);
      if (serverDetectionCache.size() > 100) {
         int toRemove = serverDetectionCache.size() - 50;
         if (toRemove > 0) {
            Iterator<Entry<UUID, Boolean>> iterator = serverDetectionCache.entrySet().iterator();

            for (int i = 0; i < toRemove && iterator.hasNext(); i++) {
               iterator.next();
               iterator.remove();
            }
         }
      }
   }

   private void renderGhostPullOnEntity(
      MatrixStack matrices,
      GhostPullRenderer.GhostPullAnimatable animatable,
      VertexConsumerProvider vertexConsumers,
      RenderLayer light,
      VertexConsumer partialTick,
      int packedOverlay,
      LivingEntity entity,
      Vec3d cameraPos,
      boolean isTarget
   ) {
      matrices.method_22903();
      float tickDelta = MinecraftClient.method_1551().method_1488();
      Vec3d targetPos = entity.method_30950(tickDelta);
      float targetYaw = entity.method_5705(tickDelta);
      double rightOffsetX = Math.cos(Math.toRadians(targetYaw)) * 0.3;
      double rightOffsetZ = Math.sin(Math.toRadians(targetYaw)) * 0.3;
      double backOffsetX = Math.sin(Math.toRadians(targetYaw)) * 0.4;
      double backOffsetZ = -Math.cos(Math.toRadians(targetYaw)) * 0.4;
      double offsetX = rightOffsetX + backOffsetX;
      double offsetY = -0.8;
      double offsetZ = rightOffsetZ + backOffsetZ;
      double worldX = targetPos.field_1352 + offsetX;
      double worldY = targetPos.field_1351 + offsetY;
      double worldZ = targetPos.field_1350 + offsetZ;
      matrices.method_22904(worldX - cameraPos.field_1352, worldY - cameraPos.field_1351, worldZ - cameraPos.field_1350);
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees(180.0F - targetYaw));
      matrices.method_22905(0.5F, 0.5F, 0.5F);
      super.render(matrices, animatable, vertexConsumers, light, partialTick, packedOverlay);
      matrices.method_22909();
   }

   private PlayerEntity getCurrentPlayer() {
      return MinecraftClient.method_1551().field_1687 != null && MinecraftClient.method_1551().field_1724 != null
         ? MinecraftClient.method_1551().field_1724
         : null;
   }

   private static boolean hasGhostPressureItem(PlayerEntity player) {
      if (player == null) {
         return false;
      }

      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, mainSlot);
      return !ghostItem.method_7960() && ghostItem.method_7909() == ModItems.GHOST_PRESSURE;
   }

   private static boolean isGhostPressureSkillOnCooldown(PlayerEntity player) {
      return player == null ? false : GhostDomainManager.isSkillOnCooldown(player, "ghost_pressure");
   }

   private static boolean hasGiantShadowGhost(PlayerEntity player) {
      if (player == null) {
         return false;
      }

      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, mainSlot);
      return !ghostItem.method_7960() && (ghostItem.method_7909() == ModItems.GIANT_SHADOW_GHOST || ghostItem.method_7909() == ModItems.COMPLETE_SHADOW_GHOST);
   }

   public static class GhostPullAnimatable implements GeoAnimatable {
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

   public static class GhostPullGeoModel extends GeoModel<GhostPullRenderer.GhostPullAnimatable> {
      public Identifier getModelResource(GhostPullRenderer.GhostPullAnimatable animatable) {
         return new Identifier("smfs", "geo/ghost_pull.geo.json");
      }

      public Identifier getTextureResource(GhostPullRenderer.GhostPullAnimatable animatable) {
         return new Identifier("smfs", "textures/entity/ghost_pull.png");
      }

      public Identifier getAnimationResource(GhostPullRenderer.GhostPullAnimatable animatable) {
         return null;
      }
   }

   public static class GiantShadowGhostAnimatable implements GeoAnimatable {
      private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

      @Override
      public void registerControllers(ControllerRegistrar controllers) {
         controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            return PlayState.CONTINUE;
         }));
      }

      @Override
      public AnimatableInstanceCache getAnimatableInstanceCache() {
         return this.cache;
      }

      @Override
      public double getTick(Object object) {
         return System.currentTimeMillis() / 1000.0;
      }
   }

   public static class GiantShadowGhostGeoModel extends GeoModel<GhostPullRenderer.GiantShadowGhostAnimatable> {
      private final boolean isCompleteShadowGhost;

      public GiantShadowGhostGeoModel() {
         this.isCompleteShadowGhost = false;
      }

      public GiantShadowGhostGeoModel(boolean isCompleteShadowGhost) {
         this.isCompleteShadowGhost = isCompleteShadowGhost;
      }

      public Identifier getModelResource(GhostPullRenderer.GiantShadowGhostAnimatable animatable) {
         return this.isCompleteShadowGhost
            ? new Identifier("smfs", "geo/complete_shadow_ghost.geo.json")
            : new Identifier("smfs", "geo/giant_shadow_ghost.geo.json");
      }

      public Identifier getTextureResource(GhostPullRenderer.GiantShadowGhostAnimatable animatable) {
         return this.isCompleteShadowGhost
            ? new Identifier("smfs", "textures/entity/complete_shadow_ghost.png")
            : new Identifier("smfs", "textures/entity/giant_shadow_ghost.png");
      }

      public Identifier getAnimationResource(GhostPullRenderer.GiantShadowGhostAnimatable animatable) {
         return this.isCompleteShadowGhost
            ? new Identifier("smfs", "animations/complete_shadow_ghost.animation.json")
            : new Identifier("smfs", "animations/giant_shadow_ghost.animation.json");
      }
   }
}
