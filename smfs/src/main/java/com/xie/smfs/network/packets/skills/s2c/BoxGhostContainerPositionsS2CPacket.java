package com.xie.smfs.network.packets.skills.s2c;

import com.xie.smfs.client.render.BoxGhostContainerRenderer;
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

public class BoxGhostContainerPositionsS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "box_ghost_container_positions");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/BoxGhostContainerPositionsS2CPacket");
   private final List<BlockPos> containerPositions;

   public BoxGhostContainerPositionsS2CPacket(List<BlockPos> containerPositions) {
      this.containerPositions = containerPositions;
   }

   public static void encode(BoxGhostContainerPositionsS2CPacket packet, PacketByteBuf buf) {
      buf.writeInt(packet.containerPositions.size());

      for (BlockPos pos : packet.containerPositions) {
         buf.method_10807(pos);
      }
   }

   public static BoxGhostContainerPositionsS2CPacket decode(PacketByteBuf buf) {
      int size = buf.readInt();
      List<BlockPos> positions = new ArrayList<>();

      for (int i = 0; i < size; i++) {
         positions.add(buf.method_10811());
      }

      return new BoxGhostContainerPositionsS2CPacket(positions);
   }

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      BoxGhostContainerPositionsS2CPacket packet = decode(buf);
      client.execute(() -> {
         try {
            LOGGER.debug("接收到开箱鬼被动技能箱子位置信息，共 {} 个箱子", packet.containerPositions.size());
            if (client.field_1724 != null) {
               BoxGhostContainerRenderer.setContainerPositions(packet.containerPositions);
            }
         } catch (Exception e) {
            LOGGER.error("处理开箱鬼箱子位置数据包时发生错误: {}", e.getMessage());
         }
      });
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, BoxGhostContainerPositionsS2CPacket::handle);
   }

   public static void sendToClient(ServerPlayerEntity player, List<BlockPos> containerPositions) {
      if (player != null) {
         PacketByteBuf buf = PacketByteBufs.create();
         encode(new BoxGhostContainerPositionsS2CPacket(containerPositions), buf);
         ServerPlayNetworking.send(player, ID, buf);
         LOGGER.debug("向玩家 {} 发送开箱鬼箱子位置信息，共 {} 个箱子", player.method_5477().getString(), containerPositions.size());
      }
   }
}
