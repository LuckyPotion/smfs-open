package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.MainGhostManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSwitchMainGhostC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "switch_main_ghost");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientSwitchMainGhostC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientSwitchMainGhostC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> MainGhostManager.switchToNextMainGhost(player));
   }
}
