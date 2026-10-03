package com.xie.smfs.network.packets.ui.c2s;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.other.CaoYanHuaEntity;
import com.xie.smfs.entity.other.ChenDoctorEntity;
import com.xie.smfs.entity.other.LiuXiaoYuEntity;
import com.xie.smfs.entity.other.WangXiaoMingEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class RequestTradeScreenC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "request_trade_screen");
   private final int entityId;

   public RequestTradeScreenC2SPacket(int entityId) {
      this.entityId = entityId;
   }

   public static void encode(RequestTradeScreenC2SPacket packet, PacketByteBuf buf) {
      buf.writeInt(packet.entityId);
   }

   public static RequestTradeScreenC2SPacket decode(PacketByteBuf buf) {
      return new RequestTradeScreenC2SPacket(buf.readInt());
   }

   public static void handle(
      RequestTradeScreenC2SPacket packet, MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketSender sender
   ) {
      server.execute(() -> {
         World world = player.method_37908();
         Entity entity = world.method_8469(packet.entityId);
         if (entity instanceof GhostMasterEntity ghostMaster) {
            if (!ghostMaster.isSuppressed() && !ghostMaster.isDeadlocked()) {
               ghostMaster.openTradeScreen(player);
            }
         } else if (entity instanceof WangXiaoMingEntity wangXiaoMing) {
            wangXiaoMing.openTradeScreen(player);
         } else if (entity instanceof ChenDoctorEntity chenDoctor) {
            chenDoctor.openTradeScreen(player);
         } else if (entity instanceof CaoYanHuaEntity caoYanHua) {
            caoYanHua.openTradeScreen(player);
         } else if (entity instanceof LiuXiaoYuEntity liuXiaoYu) {
            liuXiaoYu.openTradeScreen(player);
         }
      });
   }

   public static void send(int entityId) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeInt(entityId);
      ClientPlayNetworking.send(ID, buf);
   }
}
