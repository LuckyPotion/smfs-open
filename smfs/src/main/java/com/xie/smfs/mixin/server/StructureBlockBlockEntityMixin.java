package com.xie.smfs.mixin.server;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(StructureBlockBlockEntity.class)
public abstract class StructureBlockBlockEntityMixin {
   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 0), index = 2)
   private int modifyOffsetXMax(int max) {
      return 350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 0), index = 1)
   private int modifyOffsetXMin(int min) {
      return -350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 1), index = 2)
   private int modifyOffsetYMax(int max) {
      return 350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 1), index = 1)
   private int modifyOffsetYMin(int min) {
      return -350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 2), index = 2)
   private int modifyOffsetZMax(int max) {
      return 350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 2), index = 1)
   private int modifyOffsetZMin(int min) {
      return -350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 3), index = 2)
   private int modifySizeXMax(int max) {
      return 350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 4), index = 2)
   private int modifySizeYMax(int max) {
      return 350;
   }

   @ModifyArg(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 5), index = 2)
   private int modifySizeZMax(int max) {
      return 350;
   }
}
