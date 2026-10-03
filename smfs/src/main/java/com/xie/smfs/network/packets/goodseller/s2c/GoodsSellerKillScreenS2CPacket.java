package com.xie.smfs.network.packets.goodseller.s2c;

import com.xie.smfs.client.preset.BlackScreenOverlay;
import com.xie.smfs.network.packets.goodseller.c2s.GoodsSellerKillC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GoodsSellerKillScreenS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "goods_seller_kill_screen");

   public static void send(ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      ServerPlayNetworking.send(player, ID, buf);
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(
         ID, (client, handler, buf, responseSender) -> client.execute(() -> BlackScreenOverlay.showOpaque("你被抓住了！", () -> GoodsSellerKillC2SPacket.send()))
      );
   }
}
