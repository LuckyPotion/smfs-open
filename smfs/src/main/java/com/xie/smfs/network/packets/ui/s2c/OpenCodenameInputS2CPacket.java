package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.screen.CodenameInputScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class OpenCodenameInputS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "open_codename_input");
   private final int entityId;

   public OpenCodenameInputS2CPacket(int entityId) {
      this.entityId = entityId;
   }

   public OpenCodenameInputS2CPacket(PacketByteBuf buf) {
      this.entityId = buf.readInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeInt(this.entityId);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         OpenCodenameInputS2CPacket packet = new OpenCodenameInputS2CPacket(buf);
         client.execute(() -> {
            if (client.currentScreen == null) {
               client.setScreen(new CodenameInputScreen(packet.getEntityId()));
            }
         });
      });
   }
}
