package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.GhostLotFlipOverlay;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GhostLotFlipS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_lot_flip");
   public static final float ANIMATION_DURATION = 2.0F;

   public static void send(ServerPlayerEntity player, boolean isLifeLot) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeBoolean(isLifeLot);
      ServerPlayNetworking.send(player, ID, buf);
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         boolean isLifeLot = buf.readBoolean();
         client.execute(() -> GhostLotFlipOverlay.triggerFlip(isLifeLot, 2.0F));
      });
   }
}
