package com.xie.smfs.mixin.client;

import com.xie.smfs.client.util.FusionCameraManager;
import com.xie.smfs.client.util.GhostShadowHeadCameraManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
   @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
   private void smfs$cancelHotbar(CallbackInfo ci) {
      if (GhostShadowHeadCameraManager.isCameraBound() || FusionCameraManager.isZooming()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
   private void smfs$cancelStatusBars(DrawContext context, CallbackInfo ci) {
      if (GhostShadowHeadCameraManager.isCameraBound()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
   private void smfs$cancelExperienceBar(DrawContext context, int x, CallbackInfo ci) {
      if (GhostShadowHeadCameraManager.isCameraBound()) {
         ci.cancel();
      }
   }
}
