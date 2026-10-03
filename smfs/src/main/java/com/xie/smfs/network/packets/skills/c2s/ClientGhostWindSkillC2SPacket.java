package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.registry.ModItems;
import java.util.List;
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

public class ClientGhostWindSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_wind_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostWindSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostWindSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            try {
               GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "ghost_wind", ModItems.GHOST_WIND, -1);
               if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
                  player.sendMessage(Text.literal("§c您没有驾驭鬼风，无法使用此技能"), true);
                  return;
               }

               handleGhostWindSkill(player);
               PlayerEvents.balanceRevivalDegree(player);
            } catch (Exception e) {
               LOGGER.error("处理鬼风J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleGhostWindSkill(ServerPlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = GhostDomainManager.getDomainRadius(player, level);
      List<LivingEntity> entitiesInRange = player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), e -> e != player && !e.isOnGround());
      if (entitiesInRange.isEmpty()) {
         player.sendMessage(Text.literal("§c鬼蜮范围内没有离开地面的生物"), true);
      } else {
         for (LivingEntity targetEntity : entitiesInRange) {
            GhostDomainManager.executeSkillSpiritAttack(player, targetEntity);
         }

         spawnSkillParticles(player);
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player) {
      World world = player.getWorld();

      for (int i = 0; i < 10; i++) {
         world.addParticle(
            ParticleTypes.CLOUD,
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
