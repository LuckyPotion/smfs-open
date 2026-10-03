package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.event.SilentGhostTeleportHandler;
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

public class ClientSilentGhostJSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "silent_ghost_j_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientSilentGhostJSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientSilentGhostJSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            SilentGhostTeleportHandler.handleJKeyTeleport(player);
            PlayerEvents.balanceRevivalDegree(player);
         }
      });
   }
}
