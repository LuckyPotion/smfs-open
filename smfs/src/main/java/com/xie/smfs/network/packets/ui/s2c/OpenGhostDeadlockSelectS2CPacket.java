package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.screen.GhostDeadlockSelectScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class OpenGhostDeadlockSelectS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "open_ghost_deadlock_select");

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(
         ID, (client, handler, buf, responseSender) -> client.execute(() -> client.method_1507(new GhostDeadlockSelectScreen()))
      );
   }
}
