package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEntities;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientVillagerGhostSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "villager_ghost_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientVillagerGhostSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientVillagerGhostSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            try {
               LOGGER.info("玩家 {} 执行村民鬼J键技能：鬼奴控制", player.method_5477().getString());
               PlayerEvents.balanceRevivalDegree(player);
               handleVillagerGhostSkill(player);
            } catch (Exception e) {
               LOGGER.error("处理村民鬼J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleVillagerGhostSkill(ServerPlayerEntity player) {
      List<VillagerEntity> villagers = player.method_37908()
         .method_8390(VillagerEntity.class, player.method_5829().method_1014(10.0), villager -> villager.method_5805() && villager.method_5739(player) <= 10.0);
      if (villagers.isEmpty()) {
         LOGGER.warn("玩家 {} 周围没有村民，无法使用鬼奴控制技能", player.method_5477().getString());
      } else {
         VillagerEntity nearestVillager = villagers.stream().min((v1, v2) -> Float.compare(v1.method_5739(player), v2.method_5739(player))).orElse(null);
         if (nearestVillager == null) {
            LOGGER.warn("玩家 {} 周围没有有效的村民", player.method_5477().getString());
         } else {
            convertVillagerToGhostSlave(player, nearestVillager);
         }
      }
   }

   private static void convertVillagerToGhostSlave(ServerPlayerEntity player, VillagerEntity villager) {
      try {
         World world = player.method_37908();
         GhostSlaveEntity ghostSlave = new GhostSlaveEntity(ModEntities.GHOST_SLAVE, world);
         ghostSlave.method_5808(villager.method_23317(), villager.method_23318(), villager.method_23321(), world.field_9229.method_43057() * 360.0F, 0.0F);
         ghostSlave.method_6033(ghostSlave.method_6063());
         ghostSlave.setMaster(player);
         villager.method_5650(RemovalReason.field_26999);
         world.method_8649(ghostSlave);
         syncHatredTargets(player, ghostSlave);
         LOGGER.info("玩家 {} 成功将村民转换为鬼奴，仇恨目标已同步", player.method_5477().getString());
         spawnSkillParticles(player, ghostSlave);
      } catch (Exception e) {
         LOGGER.error("转换村民为鬼奴时发生错误", e);
      }
   }

   private static void syncHatredTargets(ServerPlayerEntity player, GhostSlaveEntity ghostSlave) {
      LivingEntity playerTarget = player.method_6052();
      if (playerTarget != null) {
         if (isValidTarget(ghostSlave, playerTarget)) {
            ghostSlave.method_5980(playerTarget);
         } else {
            LOGGER.warn("鬼奴 {} 无法攻击无效目标: {}", ghostSlave.method_5477().getString(), playerTarget.method_5477().getString());
         }
      }

      syncAITargets(player, ghostSlave);
   }

   private static void syncAITargets(ServerPlayerEntity player, GhostSlaveEntity ghostSlave) {
      for (HostileEntity hostile : player.method_37908()
         .method_8390(HostileEntity.class, player.method_5829().method_1014(15.0), entity -> entity.method_5805() && entity.method_5739(player) <= 15.0)) {
         if (isValidTarget(ghostSlave, hostile)) {
            ghostSlave.method_5980(hostile);
         }
      }
   }

   private static boolean isValidTarget(GhostSlaveEntity ghostSlave, LivingEntity target) {
      if (target == ghostSlave.getMaster()) {
         return false;
      }

      if (target == ghostSlave) {
         return false;
      }

      if (target instanceof GhostSlaveEntity otherSlave) {
         Entity otherMaster = otherSlave.getMaster();
         Entity myMaster = ghostSlave.getMaster();
         if (otherMaster != null && myMaster != null && otherMaster.equals(myMaster)) {
            return false;
         }
      }

      if (target.method_6052() == ghostSlave) {
         return false;
      } else if (ghostSlave.getMaster() instanceof ServerPlayerEntity playerMaster) {
         LivingEntity playerTarget = playerMaster.method_6052();
         return playerTarget != null && target == playerTarget ? true : target instanceof HostileEntity && !(target instanceof GhostSlaveEntity);
      } else {
         return target instanceof ServerPlayerEntity;
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player, GhostSlaveEntity ghostSlave) {
      World world = player.method_37908();

      for (int i = 0; i < 10; i++) {
         world.method_8406(
            ParticleTypes.field_22246,
            player.method_23317() + (world.field_9229.method_43058() - 0.5) * 2.0,
            player.method_23318() + world.field_9229.method_43058() * 2.0,
            player.method_23321() + (world.field_9229.method_43058() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }

      for (int i = 0; i < 10; i++) {
         world.method_8406(
            ParticleTypes.field_22246,
            ghostSlave.method_23317() + (world.field_9229.method_43058() - 0.5) * 2.0,
            ghostSlave.method_23318() + world.field_9229.method_43058() * 2.0,
            ghostSlave.method_23321() + (world.field_9229.method_43058() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
