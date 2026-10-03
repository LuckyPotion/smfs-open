package com.xie.smfs.network.packets.skills.s2c;

import com.xie.smfs.client.render.MineralGhostMineralRenderer;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineralGhostMineralPositionsS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "mineral_ghost_mineral_positions");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/MineralGhostMineralPositionsS2CPacket");
   private final List<BlockPos> mineralPositions;
   private final List<Integer> oreTypes;

   public MineralGhostMineralPositionsS2CPacket(List<BlockPos> mineralPositions, List<Integer> oreTypes) {
      this.mineralPositions = mineralPositions;
      this.oreTypes = oreTypes;
   }

   public static void encode(MineralGhostMineralPositionsS2CPacket packet, PacketByteBuf buf) {
      buf.writeInt(packet.mineralPositions.size());

      for (int i = 0; i < packet.mineralPositions.size(); i++) {
         buf.method_10807(packet.mineralPositions.get(i));
         buf.writeByte(packet.oreTypes.get(i));
      }
   }

   public static MineralGhostMineralPositionsS2CPacket decode(PacketByteBuf buf) {
      int size = buf.readInt();
      List<BlockPos> positions = new ArrayList<>();
      List<Integer> oreTypes = new ArrayList<>();

      for (int i = 0; i < size; i++) {
         positions.add(buf.method_10811());
         oreTypes.add(Integer.valueOf(buf.readByte()));
      }

      return new MineralGhostMineralPositionsS2CPacket(positions, oreTypes);
   }

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      MineralGhostMineralPositionsS2CPacket packet = decode(buf);
      client.execute(() -> {
         try {
            LOGGER.debug("接收到矿物鬼被动技能矿物位置信息，共 {} 个矿物", packet.mineralPositions.size());
            if (client.field_1724 != null) {
               MineralGhostMineralRenderer.setMineralPositions(packet.mineralPositions, packet.oreTypes);
            }
         } catch (Exception e) {
            LOGGER.error("处理矿物鬼矿物位置数据包时发生错误: {}", e.getMessage());
         }
      });
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, MineralGhostMineralPositionsS2CPacket::handle);
   }

   public static void sendToClient(ServerPlayerEntity player, List<BlockPos> mineralPositions, List<Integer> oreTypes) {
      if (player != null) {
         PacketByteBuf buf = PacketByteBufs.create();
         encode(new MineralGhostMineralPositionsS2CPacket(mineralPositions, oreTypes), buf);
         ServerPlayNetworking.send(player, ID, buf);
         LOGGER.debug("向玩家 {} 发送矿物鬼矿物位置信息，共 {} 个矿物", player.method_5477().getString(), mineralPositions.size());
      }
   }
}
