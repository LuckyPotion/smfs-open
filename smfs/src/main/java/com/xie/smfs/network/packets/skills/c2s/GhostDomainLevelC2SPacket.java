package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.GhostDomainManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostDomainLevelC2SPacket {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostDomainLevelC2SPacket");
   public static final Identifier INCREASE_ID = new Identifier("smfs", "ghost_domain_level_increase");
   public static final Identifier DECREASE_ID = new Identifier("smfs", "ghost_domain_level_decrease");
   public static final Identifier RANGE_SYNC_ID = new Identifier("smfs", "ghost_domain_range_sync");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(
         INCREASE_ID, (server, player, handler, buf, responseSender) -> server.execute(() -> GhostDomainManager.increaseGhostDomainLevel(player))
      );
      ServerPlayNetworking.registerGlobalReceiver(
         DECREASE_ID, (server, player, handler, buf, responseSender) -> server.execute(() -> GhostDomainManager.decreaseGhostDomainLevel(player))
      );
      ServerPlayNetworking.registerGlobalReceiver(RANGE_SYNC_ID, (server, player, handler, buf, responseSender) -> {
         int range = buf.readInt();
         server.execute(() -> GhostDomainManager.setPlayerDomainRange(player, range));
      });
   }
}
