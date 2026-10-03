package com.xie.smfs.network.packets.skills.s2c;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SyncGhostOfficerQuotaS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "sync_ghost_officer_quota");
   private final int remainingQuota;
   private final int maxQuota;

   public SyncGhostOfficerQuotaS2CPacket(int remainingQuota, int maxQuota) {
      this.remainingQuota = remainingQuota;
      this.maxQuota = maxQuota;
   }

   public void send(ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeInt(this.remainingQuota);
      buf.writeInt(this.maxQuota);
      ServerPlayNetworking.send(player, ID, buf);
   }

   public static void sendToPlayer(ServerPlayerEntity player, int remainingQuota, int maxQuota) {
      new SyncGhostOfficerQuotaS2CPacket(remainingQuota, maxQuota).send(player);
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         int remainingQuota = buf.readInt();
         int maxQuota = buf.readInt();
         client.execute(() -> ClientGhostOfficerQuotaHandler.updateQuota(remainingQuota, maxQuota));
      });
   }
}
