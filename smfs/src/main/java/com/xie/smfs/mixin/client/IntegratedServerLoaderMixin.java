package com.xie.smfs.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServerLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin {
   @Invoker("start")
   abstract void invokeStart(Screen screen, String string, boolean bl, boolean bl2);

   @Inject(method = "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
   private void onStart(Screen screen, String levelName, CallbackInfo ci) {
      this.invokeStart(screen, levelName, true, false);
      ci.cancel();
   }
}
