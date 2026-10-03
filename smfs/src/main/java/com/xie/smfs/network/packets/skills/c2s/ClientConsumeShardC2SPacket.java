package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.GhostDomainManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ClientConsumeShardC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "consume_shard");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientConsumeShardC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      String ghostType = buf.method_19772();
      server.execute(() -> GhostDomainManager.consumeShardItem(player, ghostType));
   }
}
