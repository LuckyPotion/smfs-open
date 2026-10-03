package com.xie.smfs.network;

import com.xie.smfs.event.screen.GhostTamingScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GhostTamingProgressPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_taming_progress");
   private final float totalProgress;
   private final int syncId;

   public GhostTamingProgressPacket(float totalProgress, int syncId) {
      this.totalProgress = totalProgress;
      this.syncId = syncId;
   }

   public float getTotalProgress() {
      return this.totalProgress;
   }

   public int getSyncId() {
      return this.syncId;
   }

   public void write(PacketByteBuf buf) {
      buf.writeFloat(this.totalProgress);
      buf.writeInt(this.syncId);
   }

   public static GhostTamingProgressPacket read(PacketByteBuf buf) {
      float totalProgress = buf.readFloat();
      int syncId = buf.readInt();
      return new GhostTamingProgressPacket(totalProgress, syncId);
   }

   public static void handleServer(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      GhostTamingProgressPacket packet = read(buf);
      server.execute(() -> {
         if (player.currentScreenHandler instanceof GhostTamingScreenHandler tamingHandler && tamingHandler.syncId == packet.getSyncId()) {
            float progress = packet.getTotalProgress();
            if (progress > 0.0F) {
               player.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20, 1));
               player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20, 0));
            }
         }
      });
   }
}
