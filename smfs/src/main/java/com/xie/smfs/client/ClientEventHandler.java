package com.xie.smfs.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;

public class ClientEventHandler {
   public static void register() {
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> LuoQianHudRenderer.reset());
   }
}
