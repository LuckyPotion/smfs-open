package com.xie.smfs.network.packets.common.s2c;

import com.xie.smfs.client.data.ClientDataManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class GhostDreamTimeS2CPacket implements Packet<ClientPlayPacketListener> {
   private final long remainingTime;

   public GhostDreamTimeS2CPacket(long remainingTime) {
      this.remainingTime = remainingTime;
   }

   public GhostDreamTimeS2CPacket(PacketByteBuf buf) {
      this.remainingTime = buf.readLong();
   }

   public void method_11052(PacketByteBuf buf) {
      buf.writeLong(this.remainingTime);
   }

   public void apply(ClientPlayPacketListener listener) {
      ClientDataManager.updateGhostDreamTime(this.remainingTime);
   }

   public long getRemainingTime() {
      return this.remainingTime;
   }
}
