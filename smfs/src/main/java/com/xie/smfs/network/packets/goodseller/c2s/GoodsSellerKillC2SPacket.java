package com.xie.smfs.network.packets.goodseller.c2s;

import com.xie.smfs.util.InstantKillUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class GoodsSellerKillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "goods_seller_kill");

   public static void send() {
      PacketByteBuf buf = PacketByteBufs.create();
      ClientPlayNetworking.send(ID, buf);
   }

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> server.execute(() -> {
         if (player.isAlive()) {
            InstantKillUtil.executePlayerInstantKill(player);
         }
      }));
   }
}
