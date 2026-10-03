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

public class ClientGhostOfficerVSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_officer_v_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostOfficerVSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostOfficerVSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "ghost_officer", ModItems.GHOST_OFFICER, 4);
            if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
               player.method_7353(Text.method_43470("§c您没有驾驭鬼差，无法使用此技能"), true);
               return;
            }

            if (result == GhostDomainManager.SkillCheckResult.LEVEL_TOO_LOW) {
               player.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
               return;
            }

            handleGhostOfficerVSkill(player);
            PlayerEvents.balanceRevivalDegree(player);
         } catch (Exception e) {
            LOGGER.error("处理鬼差V键技能时发生错误", e);
         }
      });
   }

   private static void handleGhostOfficerVSkill(ServerPlayerEntity player) {
      LivingEntity targetEntity = TargetingUtil.findEntityInLookDirection(player, 8.0, 0.7);
      if (targetEntity != null) {
         boolean isInWhitelist = GhostSkillManager.isInGhostOfficerWhitelist(player, targetEntity);
         if (isInWhitelist) {
            boolean success = GhostSkillManager.restoreSuppression(player, targetEntity);
            if (success) {
               player.method_7353(Text.method_43470("§a鬼差V键：已恢复对" + targetEntity.method_5477().getString() + "的压制"), true);
               LOGGER.info("玩家 {} 使用鬼差V键技能恢复压制：{}", player.method_5477().getString(), targetEntity.method_5477().getString());
            } else {
               player.method_7353(Text.method_43470("§c鬼差V键：无法恢复对该实体的压制"), true);
            }
         } else {
            boolean success = GhostSkillManager.removeFromSuppressionAndAddToWhitelist(player, targetEntity);
            if (success) {
               player.method_7353(Text.method_43470("§a鬼差V键：已解除" + targetEntity.method_5477().getString() + "的压制并加入白名单"), true);
               LOGGER.info("玩家 {} 使用鬼差V键技能解除压制并加入白名单：{}", player.method_5477().getString(), targetEntity.method_5477().getString());
            } else {
               player.method_7353(Text.method_43470("§c鬼差V键：无法解除该实体的压制"), true);
            }
         }

         spawnSkillParticles(player);
      } else {
         player.method_7353(Text.method_43470("§c鬼差V键：准星位置没有可操作的目标"), true);
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
