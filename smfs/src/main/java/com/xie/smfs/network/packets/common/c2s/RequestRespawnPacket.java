package com.xie.smfs.network.packets.common.c2s;

import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestRespawnPacket {
   public static final Identifier ID = new Identifier("smfs", "request_respawn");

   public RequestRespawnPacket() {
   }

   public RequestRespawnPacket(PacketByteBuf buf) {
   }

   public void write(PacketByteBuf buf) {
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      Logger LOGGER = LoggerFactory.getLogger(RequestRespawnPacket.class);
      server.execute(() -> {
         LOGGER.debug("=== 收到玩家 {} 的立即复活请求 ===", player.getName().getString());
         UUID playerUuid = player.getUuid();
         if (SpectateModePacket.isInSpectatorMode(playerUuid)) {
            LOGGER.warn("玩家 {} 在立即复活时仍有旁观者标志，清除它", playerUuid);
            SpectateModePacket.removeSpectatorFlag(playerUuid);
         }

         server.execute(() -> server.execute(() -> {
            responseSender.sendPacket(ID, PacketByteBufs.empty());
            LOGGER.debug("已发送立即复活确认包");
         }));
      });
   }
}
