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

public class ClientGraveEarthGhostSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "grave_earth_ghost_skill");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGraveEarthGhostSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "grave_earth_ghost", ModItems.GRAVE_EARTH_GHOST, -1);
            if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
               player.sendMessage(Text.literal("§c您没有驾驭坟土鬼，无法使用此技能"), true);
            } else {
               GhostDomainManager.handleGraveEarthGhostSkill(player);
               PlayerEvents.balanceRevivalDegree(player);
            }
         }
      });
   }
}
