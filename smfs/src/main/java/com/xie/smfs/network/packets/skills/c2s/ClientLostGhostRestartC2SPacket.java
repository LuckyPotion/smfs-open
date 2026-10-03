package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLostGhostRestartC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "lost_ghost_restart");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientLostGhostRestartC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientLostGhostRestartC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "lost_ghost", ModItems.LOST_GHOST, -1);
         if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
            player.method_7353(Text.method_43470("§c您没有驾驭遗忘鬼，无法使用此技能"), true);
         } else {
            GhostDomainManager.handleLostGhostRestart(player);
            PlayerEvents.balanceRevivalDegree(player);
         }
      });
   }
}
