package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.preset.ScreenPresetRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ScreenEffectS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "screen_effect");
   public static final byte TEXT_HALLUCINATION = 0;
   public static final byte GLITCH = 1;

   public static void sendTextHallucination(ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeByte(0);
      ServerPlayNetworking.send(player, ID, buf);
   }

   public static void sendGlitch(ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeByte(1);
      ServerPlayNetworking.send(player, ID, buf);
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         byte effectType = buf.readByte();
         client.execute(() -> {
            switch (effectType) {
               case 0:
                  ScreenPresetRenderer.playTextHallucination();
                  break;
               case 1:
                  ScreenPresetRenderer.playGlitch();
            }
         });
      });
   }
}
