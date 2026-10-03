package com.xie.smfs.manager;

import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GhostLimitManager {
   private static final int KEY_A = 500;
   private static final int KEY_B = 494;
   private static final int TICK_INTERVAL = 100;
   private static int tickCounter = 0;
   private static boolean triggered = false;
   private static int currentMax = 6;

   public static int getMaxPlayers() {
      return currentMax;
   }

   public static boolean isOverLimit(MinecraftServer server) {
      return server.method_3760().method_14571().size() > getMaxPlayers();
   }

   public static void kickExcessPlayers(MinecraftServer server) {
      List<ServerPlayerEntity> players = server.method_3760().method_14571();
      int max = getMaxPlayers();
      int count = players.size();
      if (count > max) {
         int excess = count - max;

         for (int i = players.size() - 1; i >= 0 && excess > 0; i--) {
            ServerPlayerEntity player = players.get(i);
            if (player != null) {
               player.field_13987.method_14367(Text.method_43471("message.smfs.server_full"));
               excess--;
            }
         }
      }
   }

   private static void fluctuate() {
      currentMax = 4 + (int)(Math.random() * 3.0);
   }

   public static void register() {
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> {
         if (isOverLimit(server)) {
            triggered = true;
            ServerPlayerEntity player = handler.method_32311();
            player.field_13987.method_14367(Text.method_43471("message.smfs.server_full"));
         }
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         tickCounter++;
         if (tickCounter >= 100) {
            tickCounter = 0;
            if (triggered) {
               fluctuate();
            }

            kickExcessPlayers(server);
            if (!triggered && isOverLimit(server)) {
               triggered = true;
            }
         }
      });
   }
}
