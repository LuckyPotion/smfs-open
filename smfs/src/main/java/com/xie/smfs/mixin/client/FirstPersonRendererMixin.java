package com.xie.smfs.mixin.client;

import com.xie.smfs.client.util.FusionCameraManager;
import com.xie.smfs.client.util.GhostShadowHeadCameraManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public abstract class FirstPersonRendererMixin {
   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void smfs$cancelRenderItem(CallbackInfo ci) {
      if (GhostShadowHeadCameraManager.isCameraBound() || FusionCameraManager.isZooming()) {
         ci.cancel();
      }
   }
}
