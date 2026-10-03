package com.xie.smfs.network.packets.ghostchild.s2c;

import com.xie.smfs.client.screen.FusionSequenceScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GhostChildFusionBeginS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_child_fusion_begin");

   public static void send(ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      ServerPlayNetworking.send(player, ID, buf);
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(
         ID, (client, handler, buf, responseSender) -> client.execute(() -> client.setScreen(new FusionSequenceScreen()))
      );
   }
}
