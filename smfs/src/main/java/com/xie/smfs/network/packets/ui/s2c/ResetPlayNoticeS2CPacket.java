package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.data.ClientDataManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ResetPlayNoticeS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "reset_play_notice");

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(
         ID, (client, handler, buf, responseSender) -> client.execute(() -> ClientDataManager.setHasSeenPlayNotice(false))
      );
   }

   public static void send(ServerPlayerEntity player) {
      ServerPlayNetworking.send(player, ID, PacketByteBufs.empty());
   }
}
