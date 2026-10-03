package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.TargetingUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostOfficerSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_officer_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostOfficerSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostOfficerSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            try {
               GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "ghost_officer", ModItems.GHOST_OFFICER, -1);
               if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
                  player.method_7353(Text.method_43470("§c您没有驾驭鬼差，无法使用此技能"), true);
                  return;
               }

               handleGhostOfficerSkill(player);
               PlayerEvents.balanceRevivalDegree(player);
            } catch (Exception e) {
               LOGGER.error("处理鬼差J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleGhostOfficerSkill(ServerPlayerEntity player) {
      LivingEntity targetEntity = TargetingUtil.findEntityInLookDirection(player, 8.0, 0.7);
      if (targetEntity != null) {
         boolean success = GhostSkillManager.setPrioritySuppressionTarget(player, targetEntity);
         if (success) {
            player.method_7353(Text.method_43470("§a鬼差J键：已设置" + targetEntity.method_5477().getString() + "为优先压制目标"), true);
            LOGGER.info("玩家 {} 使用鬼差J键技能设置优先压制目标：{}", player.method_5477().getString(), targetEntity.method_5477().getString());
         } else {
            player.method_7353(Text.method_43470("§c鬼差J键：无法设置该实体为优先压制目标"), true);
         }

         spawnSkillParticles(player);
      } else {
         player.method_7353(Text.method_43470("§c鬼差J键：准星位置没有可压制的目标"), true);
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player) {
      World world = player.method_37908();

      for (int i = 0; i < 15; i++) {
         world.method_8406(
            ParticleTypes.field_22246,
            player.method_23317() + (world.field_9229.method_43058() - 0.5) * 3.0,
            player.method_23318() + world.field_9229.method_43058() * 3.0,
            player.method_23321() + (world.field_9229.method_43058() - 0.5) * 3.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
