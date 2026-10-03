package com.xie.smfs.network.packets.common.s2c;

import com.xie.smfs.event.network.GhostPressureDetectionHandler;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostPressureDetectionPacket {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostPressureDetectionPacket.class);
   public static final Identifier PACKET_ID = new Identifier("smfs", "ghost_pressure_detection");

   public static void registerServerHandler() {
      ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, GhostPressureDetectionPacket::handleServerPacket);
   }

   public static void registerClientHandler() {
   }

   private static void handleServerPacket(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      UUID entityId = buf.method_10790();
      server.execute(() -> {
         if (player.method_37908() instanceof ServerWorld serverWorld) {
            LivingEntity entity = (LivingEntity)serverWorld.method_14190(entityId);
            if (entity != null) {
               GhostPressureDetectionHandler.forceCheckAndSend(player, entity);
               LOGGER.debug("收到客户端请求检测实体 {} 的鬼压人buff状态", entity.method_5477().getString());
            }
         }
      });
   }

   public static void sendToClient(ServerPlayerEntity player, UUID entityId, boolean hasGhostPressure) {
      PacketByteBuf buf = createDetectionResultPacket(entityId, hasGhostPressure);
      ServerPlayNetworking.send(player, PACKET_ID, buf);
   }

   public static void sendToAllNearby(LivingEntity entity, boolean hasGhostPressure) {
      if (entity.method_37908() instanceof ServerWorld serverWorld) {
         PacketByteBuf var6 = createDetectionResultPacket(entity.method_5667(), hasGhostPressure);

         for (ServerPlayerEntity player : serverWorld.method_18456()) {
            ServerPlayNetworking.send(player, PACKET_ID, var6);
         }
      }
   }

   private static PacketByteBuf createDetectionResultPacket(UUID entityId, boolean hasGhostPressure) {
      PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
      buf.method_10797(entityId);
      buf.writeBoolean(hasGhostPressure);
      return buf;
   }

   public static PacketByteBuf createClientRequestPacket(UUID entityId) {
      PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
      buf.method_10797(entityId);
      return buf;
   }
}
