package com.xie.smfs.network.packets.skills.c2s;

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

public class ClientGhostDomainTeleportC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghostdomain_teleport");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostDomainTeleportC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostDomainTeleportC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         LOGGER.info("收到玩家 {} 的鬼域瞬移请求", player.method_5477().getString());
         GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "silent_ghost_eye", ModItems.SILENT_GHOST_EYE, 3);
         if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
            player.method_7353(Text.method_43470("§c您没有驾驭鬼眼，无法使用此技能"), true);
         } else if (result == GhostDomainManager.SkillCheckResult.LEVEL_TOO_LOW) {
            player.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
         } else {
            GhostDomainManager.handleGhostDomainTeleport(player);
         }
      });
   }
}
