package com.xie.smfs.mixin.server;

import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(UpdateStructureBlockC2SPacket.class)
public abstract class UpdateStructureBlockC2SPacketMixin {
   @ModifyArg(
      method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 0),
      index = 2
   )
   private int modifyOffsetMax(int max) {
      return 350;
   }

   @ModifyArg(
      method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 0),
      index = 1
   )
   private int modifyOffsetMin(int min) {
      return -350;
   }

   @ModifyArg(
      method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I", ordinal = 1),
      index = 2
   )
   private int modifySizeMax(int max) {
      return 350;
   }
}
