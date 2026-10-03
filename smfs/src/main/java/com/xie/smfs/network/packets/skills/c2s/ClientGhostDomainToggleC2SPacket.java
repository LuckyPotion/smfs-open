package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.GhostDomainManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostDomainToggleC2SPacket {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostDomainToggleC2SPacket");
   public static final Identifier ID = new Identifier("smfs", "ghost_domain_toggle");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostDomainToggleC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> GhostDomainManager.toggleGhostDomain(player));
   }
}
