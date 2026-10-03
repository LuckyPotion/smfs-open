package com.xie.smfs.network.packets.ghostchild.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GhostChildFeedSuccessPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_child_feed_success");

   public void write(PacketByteBuf buf) {
   }

   public static GhostChildFeedSuccessPacket read(PacketByteBuf buf) {
      return new GhostChildFeedSuccessPacket();
   }

   public static void sendToClient(ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      ServerPlayNetworking.send(player, ID, buf);
   }
}
