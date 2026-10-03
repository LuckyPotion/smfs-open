package com.xie.smfs.network.packets.skills.s2c;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GiantShadowGhostScaleS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "giant_shadow_ghost_scale");

   public static void sendToClient(ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      ServerPlayNetworking.send(player, ID, buf);
   }
}
