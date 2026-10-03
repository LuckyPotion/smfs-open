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

public class GhostChildSummonPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_child_summon");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostChildSummonPacket");

   public static void handleServer(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            PlayerGhostChildManager.summonGhostChild(player);
         } catch (Exception e) {
            LOGGER.error("服务端处理鬼童召唤时发生错误: {}", e.getMessage());
         }
      });
   }

   public static GhostChildSummonPacket create() {
      return new GhostChildSummonPacket();
   }

   public void write(PacketByteBuf buf) {
   }

   public static GhostChildSummonPacket decode(PacketByteBuf buf) {
      return new GhostChildSummonPacket();
   }
}
