package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
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

public class ClientLostGhostSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "lost_ghost_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientLostGhostSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientLostGhostSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            try {
               PlayerEvents.balanceRevivalDegree(player);
               handleLostGhostSkill(player);
            } catch (Exception e) {
               LOGGER.error("处理遗忘鬼J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleLostGhostSkill(ServerPlayerEntity player) {
      LivingEntity bestTarget = TargetingUtil.findEntityInLookDirection(player, 4.0, 0.5);
      if (bestTarget != null) {
         GhostDomainManager.executeSkillSpiritAttack(player, bestTarget);
         LOGGER.debug("玩家 {} 使用遗忘鬼J键技能攻击目标 {}，距离: {}", player.getName().getString(), bestTarget.getName().getString(), player.distanceTo(bestTarget));
         spawnSkillParticles(player);
      } else {
         player.sendMessage(Text.literal("§c遗忘鬼J键：准星位置没有可攻击的目标"), true);
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player) {
      World world = player.getWorld();

      for (int i = 0; i < 10; i++) {
         world.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            player.getX() + (world.random.nextDouble() - 0.5) * 2.0,
            player.getY() + world.random.nextDouble() * 2.0,
            player.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
