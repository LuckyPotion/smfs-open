package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
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

public class ClientGhostBloodSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_blood_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostBloodSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostBloodSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            try {
               GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "ghost_blood", ModItems.GHOST_BLOOD, -1);
               if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
                  player.sendMessage(Text.literal("§c您没有驾驭鬼血，无法使用此技能"), true);
                  return;
               }

               handleGhostBloodSkill(player);
               PlayerEvents.balanceRevivalDegree(player);
            } catch (Exception e) {
               LOGGER.error("处理鬼血J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleGhostBloodSkill(ServerPlayerEntity player) {
      int radius = 8;
      List<LivingEntity> entitiesInRange = player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity);
      if (entitiesInRange.isEmpty()) {
         player.sendMessage(Text.literal("§c范围内没有可攻击的生物"), true);
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
            nearestEntity.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 600, 0));
            player.sendMessage(Text.literal("§a成功压制 " + nearestEntity.getName().getString()), true);
            if (nearestEntity instanceof PlayerEntity targetPlayer) {
               targetPlayer.sendMessage(Text.literal("§c被一股强大的力量压制！"), true);
            }
         }

         spawnSkillParticles(player);
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player) {
      World world = player.getWorld();

      for (int i = 0; i < 15; i++) {
         world.addParticle(
            ParticleTypes.DRIPPING_LAVA,
            player.getX() + (world.random.nextDouble() - 0.5) * 3.0,
            player.getY() + world.random.nextDouble() * 2.5,
            player.getZ() + (world.random.nextDouble() - 0.5) * 3.0,
            0.0,
            0.05,
            0.0
         );
      }
   }
}
