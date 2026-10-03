package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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

public class ClientGhostShadowHeadVSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_shadow_head_v_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostShadowHeadVSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostShadowHeadVSkillC2SPacket::receive);
   }

   public static void sendToServer(int targetId) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeInt(targetId);
      ClientPlayNetworking.send(ID, buf);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      int targetId = buf.readInt();
      server.execute(() -> {
         GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "ghost_shadow_head", ModItems.GHOST_SHADOW_HEAD, 3);
         if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
            result = GhostDomainManager.canUseGhostSkill(player, "complete_shadow_ghost", ModItems.COMPLETE_SHADOW_GHOST, 3);
         }

         if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
            player.method_7353(Text.method_43470("§c您没有驾驭鬼影头，无法使用此技能"), true);
         } else if (result == GhostDomainManager.SkillCheckResult.LEVEL_TOO_LOW) {
            player.method_7353(Text.method_43470("§c复苏程度不足"), true);
         } else {
            GhostDomainManager.handleGhostShadowHeadVSkill(player, targetId);
            PlayerEvents.balanceRevivalDegree(player);
         }
      });
   }
}
