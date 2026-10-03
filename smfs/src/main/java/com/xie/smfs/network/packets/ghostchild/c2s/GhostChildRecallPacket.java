package com.xie.smfs.network.packets.ghostchild.c2s;

import com.xie.smfs.data.PlayerGhostChildManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostChildRecallPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_child_recall");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostChildRecallPacket");

   public static void handleServer(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            PlayerGhostChildManager.recallGhostChild(player);
         } catch (Exception e) {
            LOGGER.error("服务端处理鬼童收回时发生错误: {}", e.getMessage());
         }
      });
   }

   public static GhostChildRecallPacket create() {
      return new GhostChildRecallPacket();
   }

   public void write(PacketByteBuf buf) {
   }

   public static GhostChildRecallPacket decode(PacketByteBuf buf) {
      return new GhostChildRecallPacket();
   }
}
