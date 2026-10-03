package com.xie.smfs.mixin.server;

import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.util.InstantKillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySetHealthMixin {
   @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
   private void onSetHealth(float health, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)this;
      if (entity instanceof PlayerEntity player
         && player.hasStatusEffect(ModEffects.MUSIC_BOX_CURSE)
         && health <= 0.0F
         && !InstantKillUtil.isMarkedForInstantKill(player)) {
         ci.cancel();
      }
   }
}
