package com.xie.smfs.mixin.client;

import com.xie.smfs.effect.ICurseEffect;
import com.xie.smfs.item.GhostScissorsItem;
import java.util.Collection;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Unique
   private boolean smfs$hasGhostScissors() {
      LivingEntity self = (LivingEntity)this;
      if (!(self instanceof AbstractClientPlayerEntity)) {
         return false;
      }

      ItemStack mainHand = self.getMainHandStack();
      ItemStack offHand = self.getOffHandStack();
      return mainHand.getItem() instanceof GhostScissorsItem || offHand.getItem() instanceof GhostScissorsItem;
   }

   @Inject(method = "getStatusEffects", at = @At("RETURN"), cancellable = true)
   private void smfs$modifyStatusEffects(CallbackInfoReturnable<Collection<StatusEffectInstance>> cir) {
      if (!this.smfs$hasGhostScissors()) {
         Collection<StatusEffectInstance> originalEffects = cir.getReturnValue();
         if (originalEffects != null && !originalEffects.isEmpty()) {
            Collection<StatusEffectInstance> filteredEffects = originalEffects.stream()
               .filter(effect -> !(effect.getEffectType() instanceof ICurseEffect))
               .collect(Collectors.toList());
            cir.setReturnValue(filteredEffects);
         }
      }
   }
}
