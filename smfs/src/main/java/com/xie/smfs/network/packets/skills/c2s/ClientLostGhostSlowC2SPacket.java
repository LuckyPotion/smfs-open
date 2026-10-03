package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.util.TargetingUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLostGhostSlowC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "lost_ghost_slow_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientLostGhostSlowC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientLostGhostSlowC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            LOGGER.info("玩家 {} 执行遗忘鬼V键技能：让目标玩家缓慢255", player.getName().getString());
            PlayerEvents.balanceRevivalDegree(player);
            handleLostGhostSlowSkill(player);
         } catch (Exception e) {
            LOGGER.error("处理遗忘鬼V键技能时发生错误", e);
         }
      });
   }

   private static void handleLostGhostSlowSkill(ServerPlayerEntity player) {
      LivingEntity bestTarget = TargetingUtil.findEntityInLookDirection(player, 4.0, 0.5, e -> e instanceof ServerPlayerEntity);
      if (bestTarget == null) {
         LOGGER.warn("玩家 {} 准星位置没有其他玩家，无法使用缓慢技能", player.getName().getString());
      } else {
         ((ServerPlayerEntity)bestTarget).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 255, false, true));
         LOGGER.debug("玩家 {} 成功对玩家 {} 使用缓慢255技能，距离: {}", player.getName().getString(), bestTarget.getName().getString(), player.distanceTo(bestTarget));
         spawnSkillParticles(player, (ServerPlayerEntity)bestTarget);
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player, ServerPlayerEntity targetPlayer) {
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

      for (int i = 0; i < 10; i++) {
         world.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            targetPlayer.getX() + (world.random.nextDouble() - 0.5) * 2.0,
            targetPlayer.getY() + world.random.nextDouble() * 2.0,
            targetPlayer.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
