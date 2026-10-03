package com.xie.smfs.network.packets.common.c2s;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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

public class SpectateModePacket {
   private static final Logger LOGGER = LoggerFactory.getLogger(SpectateModePacket.class);
   public static final Identifier ID = new Identifier("smfs", "spectate_mode");
   private static final Set<UUID> SPECTATING_PLAYERS = new HashSet<>();

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, SpectateModePacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      LOGGER.info("收到玩家 {} 的旁观者模式请求", player.method_5477().getString());
      server.execute(() -> {
         try {
            LOGGER.info("=== 收到旁观者模式请求 ===");
            UUID playerUuid = player.method_5667();
            SPECTATING_PLAYERS.add(playerUuid);
            LOGGER.info("已添加旁观者标志，UUID: {}, 标志验证: {}", playerUuid, SPECTATING_PLAYERS.contains(playerUuid));
            LOGGER.info("等待 AFTER_RESPAWN 事件切换到旁观者模式");
            server.execute(() -> server.execute(() -> {
               responseSender.sendPacket(ID, PacketByteBufs.empty());
               LOGGER.info("已发送旁观者模式确认包");
            }));
         } catch (Exception e) {
            LOGGER.error("处理旁观者模式请求时出错", e);
            SPECTATING_PLAYERS.remove(player.method_5667());
         }
      });
   }

   public static boolean isInSpectatorMode(UUID playerUuid) {
      return SPECTATING_PLAYERS.contains(playerUuid);
   }

   public static void removeSpectatorFlag(UUID playerUuid) {
      SPECTATING_PLAYERS.remove(playerUuid);
      LOGGER.info("已移除玩家 {} 的旁观者模式标志", playerUuid);
   }

   public void write(PacketByteBuf buf) {
   }
}
