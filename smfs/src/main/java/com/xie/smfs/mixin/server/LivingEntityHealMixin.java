package com.xie.smfs.mixin.server;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHealMixin {
   @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
   private void onHeal(float amount, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)this;
      StatusEffectInstance traumaEffect = entity.getStatusEffect(ModEffects.TRAUMA_CURSE);
      if (traumaEffect != null) {
         ci.cancel();
      }
   }
}
