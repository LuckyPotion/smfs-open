package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.network.packets.common.s2c.BoneTreeCoordinatesS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBoneTreeCoordinatesC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "request_bone_tree_coordinates");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/RequestBoneTreeCoordinatesC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, RequestBoneTreeCoordinatesC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         LOGGER.info("收到玩家 {} 的白骨树坐标请求", player.getName().getString());
         BoneTreeCoordinatesS2CPacket.sendCoordinates(player);
      });
   }

   public void write(PacketByteBuf buf) {
   }
}
