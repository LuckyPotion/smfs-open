package com.xie.smfs.mixin.client;

import com.xie.smfs.Smfs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(World.class)
public abstract class WorldMixin {
   @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
   private void smfs$forceGhostDreamRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
      World world = (World)this;
      if (world.getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION) {
         cir.setReturnValue(1.0F);
      }
   }
}
