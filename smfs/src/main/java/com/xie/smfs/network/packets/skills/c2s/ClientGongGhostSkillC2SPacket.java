package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGongGhostSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "gong_ghost_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGongGhostSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGongGhostSkillC2SPacket::handle);
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "gong_ghost", ModItems.GONG_GHOST, -1);
            if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
               player.method_7353(Text.method_43470("§c您没有驾驭敲锣鬼，无法使用此技能"), true);
            } else {
               PlayerEvents.balanceRevivalDegree(player);
               ServerWorld world = player.method_51469();
               Box searchBox = new Box(player.method_24515()).method_1014(10.0);
               LivingEntity nearestTarget = null;
               double nearestDistance = Double.MAX_VALUE;

               for (LivingEntity entity : world.method_8390(LivingEntity.class, searchBox, e -> e != player && e.method_5805())) {
                  double distance = entity.method_5858(player);
                  if (distance < nearestDistance) {
                     nearestDistance = distance;
                     nearestTarget = entity;
                  }
               }

               if (nearestTarget != null) {
                  LOGGER.info("敲锣鬼技能：玩家 {} 袭击目标 {}", player.method_5477().getString(), nearestTarget.method_5477().getString());
                  GhostDomainManager.executeSkillSpiritAttack(player, nearestTarget);
               } else {
                  LOGGER.info("敲锣鬼技能：玩家 {} 未找到袭击目标", player.method_5477().getString());
                  player.method_7353(Text.method_43470("§c未找到袭击目标"), true);
               }
            }
         }
      });
   }
}
