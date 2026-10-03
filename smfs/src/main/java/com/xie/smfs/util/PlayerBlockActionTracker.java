package com.xie.smfs.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.After;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PlayerBlockActionTracker {
   private static final Map<UUID, Long> lastBlockActionTime = new HashMap<>();

   public static void registerEvents() {
      PlayerBlockBreakEvents.AFTER.register((After)(world, player, pos, state, blockEntity) -> {
         if (player instanceof ServerPlayerEntity) {
            recordBlockAction(player.getUuid());
         }
      });
      UseBlockCallback.EVENT.register((UseBlockCallback)(player, world, hand, hitResult) -> {
         if (player instanceof ServerPlayerEntity && hand == Hand.MAIN_HAND) {
            recordBlockAction(player.getUuid());
         }

         return ActionResult.PASS;
      });
   }

   private static void recordBlockAction(UUID playerUuid) {
      lastBlockActionTime.put(playerUuid, System.currentTimeMillis());
   }

   public static boolean hasRecentBlockAction(UUID playerUuid, int ticks) {
      long currentTime = System.currentTimeMillis();
      long actionTime = lastBlockActionTime.getOrDefault(playerUuid, 0L);
      return currentTime - actionTime < ticks * 50;
   }
}
