package com.xie.smfs.mixin.client;

import com.xie.smfs.client.util.FusionCameraManager;
import com.xie.smfs.client.util.GhostShadowHeadCameraManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {
   @Shadow
   private Vec3d field_18712;
   @Shadow
   private float field_18718;
   @Shadow
   private float field_18717;
   @Shadow
   private boolean field_18719;
   @Shadow
   private boolean field_18709;

   @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private Entity smfs$redirectFocusedEntity(Entity focusedEntity) {
      if (GhostShadowHeadCameraManager.isCameraBound()) {
         Entity bound = GhostShadowHeadCameraManager.getBoundEntity();
         if (bound != null && !bound.method_31481()) {
            return bound;
         }
      }

      return focusedEntity;
   }

   @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private boolean smfs$forceThirdPersonCamera(boolean thirdPerson) {
      if (GhostShadowHeadCameraManager.isCameraBound()) {
         return false;
      } else {
         return FusionCameraManager.isZooming() ? true : thirdPerson;
      }
   }

   @Inject(method = "update", at = @At("HEAD"), cancellable = true)
   private void smfs$fusionCameraOverride(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
      if (!GhostShadowHeadCameraManager.isCameraBound()) {
         if (FusionCameraManager.isZooming()) {
            ci.cancel();
            Vec3d targetPos = FusionCameraManager.calculateCameraPos(focusedEntity);
            if (targetPos != null) {
               this.field_18719 = true;
               this.field_18712 = targetPos;
               this.field_18718 = FusionCameraManager.getCameraYaw();
               this.field_18717 = FusionCameraManager.getCameraPitch();
               this.field_18709 = true;
            }
         }
      }
   }
}
