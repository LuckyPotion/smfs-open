package com.xie.smfs.mixin.server;

import com.xie.smfs.util.InstantKillUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityInstantKillMixin {
   @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
   private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity entity = (LivingEntity)this;
      if (InstantKillUtil.isMarkedForInstantKill(entity)) {
         entity.method_6033(0.0F);

         try {
            entity.method_6078(source);
         } catch (Exception var6) {
         }

         entity.method_31472();
         cir.setReturnValue(true);
         cir.cancel();
      }
   }

   @Inject(method = "canTakeDamage", at = @At("HEAD"), cancellable = true)
   private void onCanTakeDamage(CallbackInfoReturnable<Boolean> cir) {
      LivingEntity entity = (LivingEntity)this;
      if (InstantKillUtil.isMarkedForInstantKill(entity)) {
         cir.setReturnValue(true);
         cir.cancel();
      }
   }
}
