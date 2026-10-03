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

public class ClientQiaomenGhostGSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "qiaomen_ghost_g_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientQiaomenGhostGSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientQiaomenGhostGSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "qiaomen_ghost", ModItems.QIAOMEN_GHOST, -1);
         if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
            player.sendMessage(Text.literal("§c您没有驾驭敲门鬼，无法使用此技能"), true);
         } else {
            GhostDomainManager.handleQiaomenGhostGSkill(player);
            PlayerEvents.balanceRevivalDegree(player);
         }
      });
   }
}
