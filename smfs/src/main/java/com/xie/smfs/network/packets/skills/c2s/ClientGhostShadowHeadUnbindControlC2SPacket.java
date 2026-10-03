package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.GhostDomainManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostShadowHeadUnbindControlC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_shadow_head_unbind_control");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostShadowHeadUnbindControlC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostShadowHeadUnbindControlC2SPacket::receive);
   }

   public static void sendToServer(int targetId) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeInt(targetId);
      ClientPlayNetworking.send(ID, buf);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      int targetId = buf.readInt();
      server.execute(() -> GhostDomainManager.handleGhostShadowHeadUnbindControl(player, targetId));
   }
}
