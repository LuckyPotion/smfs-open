package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSuonaGhostSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "suona_ghost_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientSuonaGhostSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientSuonaGhostSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            try {
               LOGGER.info("玩家 {} 执行唢呐鬼J键技能：吹唢呐标记", player.getName().getString());
               PlayerEvents.balanceRevivalDegree(player);
               handleSuonaGhostSkill(player);
            } catch (Exception e) {
               LOGGER.error("处理唢呐鬼J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleSuonaGhostSkill(ServerPlayerEntity player) {
      int radius = 10;
      List<LivingEntity> entitiesInRange = player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity);
      if (entitiesInRange.isEmpty()) {
         player.sendMessage(Text.literal("§c唢呐鬼J键：范围内没有可攻击的生物"), true);
      } else {
         LivingEntity nearestEntity = null;
         double minDistance = Double.MAX_VALUE;

         for (LivingEntity entity : entitiesInRange) {
            double distance = player.distanceTo(entity);
            if (distance < minDistance) {
               minDistance = distance;
               nearestEntity = entity;
            }
         }

         if (nearestEntity != null) {
            GhostDomainManager.executeSkillSpiritAttack(player, nearestEntity);
            LOGGER.info("玩家 {} 使用唢呐鬼J键技能攻击生物 {}，距离: {}", player.getName().getString(), nearestEntity.getName().getString(), minDistance);
         }
      }
   }
}
