package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.network.packets.common.s2c.StructureCoordinatesS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestStructureCoordinatesC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "request_structure_coordinates");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/RequestStructureCoordinatesC2SPacket");
   private final String structureType;

   public RequestStructureCoordinatesC2SPacket(String structureType) {
      this.structureType = structureType;
   }

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, RequestStructureCoordinatesC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      String structureType = buf.method_19772();
      server.execute(() -> {
         LOGGER.info("收到玩家 {} 的{}坐标请求", player.method_5477().getString(), structureType);
         StructureCoordinatesS2CPacket.sendCoordinates(player, structureType);
      });
   }

   public void write(PacketByteBuf buf) {
      buf.method_10814(this.structureType);
   }

   public String getStructureType() {
      return this.structureType;
   }
}
