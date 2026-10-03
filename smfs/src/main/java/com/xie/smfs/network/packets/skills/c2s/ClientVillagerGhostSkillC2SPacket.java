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
               LOGGER.info("玩家 {} 执行村民鬼J键技能：鬼奴控制", player.getName().getString());
               PlayerEvents.balanceRevivalDegree(player);
               handleVillagerGhostSkill(player);
            } catch (Exception e) {
               LOGGER.error("处理村民鬼J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleVillagerGhostSkill(ServerPlayerEntity player) {
      List<VillagerEntity> villagers = player.getWorld()
         .getEntitiesByClass(VillagerEntity.class, player.getBoundingBox().expand(10.0), villager -> villager.isAlive() && villager.distanceTo(player) <= 10.0);
      if (villagers.isEmpty()) {
         LOGGER.warn("玩家 {} 周围没有村民，无法使用鬼奴控制技能", player.getName().getString());
      } else {
         VillagerEntity nearestVillager = villagers.stream().min((v1, v2) -> Float.compare(v1.distanceTo(player), v2.distanceTo(player))).orElse(null);
         if (nearestVillager == null) {
            LOGGER.warn("玩家 {} 周围没有有效的村民", player.getName().getString());
         } else {
            convertVillagerToGhostSlave(player, nearestVillager);
         }
      }
   }

   private static void convertVillagerToGhostSlave(ServerPlayerEntity player, VillagerEntity villager) {
      try {
         World world = player.getWorld();
         GhostSlaveEntity ghostSlave = new GhostSlaveEntity(ModEntities.GHOST_SLAVE, world);
         ghostSlave.refreshPositionAndAngles(villager.getX(), villager.getY(), villager.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
         ghostSlave.setHealth(ghostSlave.getMaxHealth());
         ghostSlave.setMaster(player);
         villager.remove(RemovalReason.DISCARDED);
         world.spawnEntity(ghostSlave);
         syncHatredTargets(player, ghostSlave);
         LOGGER.info("玩家 {} 成功将村民转换为鬼奴，仇恨目标已同步", player.getName().getString());
         spawnSkillParticles(player, ghostSlave);
      } catch (Exception e) {
         LOGGER.error("转换村民为鬼奴时发生错误", e);
      }
   }

   private static void syncHatredTargets(ServerPlayerEntity player, GhostSlaveEntity ghostSlave) {
      LivingEntity playerTarget = player.getAttacking();
      if (playerTarget != null) {
         if (isValidTarget(ghostSlave, playerTarget)) {
            ghostSlave.setTarget(playerTarget);
         } else {
            LOGGER.warn("鬼奴 {} 无法攻击无效目标: {}", ghostSlave.getName().getString(), playerTarget.getName().getString());
         }
      }

      syncAITargets(player, ghostSlave);
   }

   private static void syncAITargets(ServerPlayerEntity player, GhostSlaveEntity ghostSlave) {
      for (HostileEntity hostile : player.getWorld()
         .getEntitiesByClass(HostileEntity.class, player.getBoundingBox().expand(15.0), entity -> entity.isAlive() && entity.distanceTo(player) <= 15.0)) {
         if (isValidTarget(ghostSlave, hostile)) {
            ghostSlave.setTarget(hostile);
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

      if (target.getAttacking() == ghostSlave) {
         return false;
      } else if (ghostSlave.getMaster() instanceof ServerPlayerEntity playerMaster) {
         LivingEntity playerTarget = playerMaster.getAttacking();
         return playerTarget != null && target == playerTarget ? true : target instanceof HostileEntity && !(target instanceof GhostSlaveEntity);
      } else {
         return target instanceof ServerPlayerEntity;
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player, GhostSlaveEntity ghostSlave) {
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
            ghostSlave.getX() + (world.random.nextDouble() - 0.5) * 2.0,
            ghostSlave.getY() + world.random.nextDouble() * 2.0,
            ghostSlave.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
