package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.TargetingUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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

public class ClientGhostOfficerGSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_officer_g_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostOfficerGSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostOfficerGSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "ghost_officer", ModItems.GHOST_OFFICER, 5);
            if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
               player.sendMessage(Text.literal("§c您没有驾驭鬼差，无法使用此技能"), true);
               return;
            }

            if (result == GhostDomainManager.SkillCheckResult.LEVEL_TOO_LOW) {
               player.sendMessage(Text.literal(GhostDomainManager.getInsufficientLevelMessage(player)), true);
               return;
            }

            handleGhostOfficerGSkill(player);
            PlayerEvents.balanceRevivalDegree(player);
         } catch (Exception e) {
            LOGGER.error("处理鬼差G键技能时发生错误", e);
         }
      });
   }

   private static void handleGhostOfficerGSkill(ServerPlayerEntity player) {
      LivingEntity targetEntity = TargetingUtil.findEntityInLookDirection(player, 8.0, 0.7);
      if (targetEntity != null) {
         if (targetEntity instanceof ServerPlayerEntity targetPlayer) {
            if (targetPlayer.hasStatusEffect(ModEffects.SILENCE)) {
               if (targetPlayer.hasStatusEffect(ModEffects.MUSIC_BOX_CURSE)) {
                  player.sendMessage(Text.literal("§c鬼差G键：秒杀失败"), true);
               } else {
                  executeInstantKill(targetPlayer, player);
                  player.sendMessage(Text.literal("§a鬼差G键：成功秒杀沉寂状态下的玩家 " + targetPlayer.getName().getString()), true);
                  LOGGER.debug("玩家 {} 使用鬼差G键技能秒杀沉寂状态玩家：{}", player.getName().getString(), targetPlayer.getName().getString());
               }
            } else {
               player.sendMessage(Text.literal("§c鬼差G键：目标玩家不处于沉寂状态"), true);
            }
         } else if (targetEntity instanceof GhostMasterEntity ghostMaster) {
            if (ghostMaster.hasStatusEffect(ModEffects.SILENCE)) {
               executeInstantKill(ghostMaster, player);
               player.sendMessage(Text.literal("§a鬼差G键：成功秒杀沉寂状态下的遇鬼者 " + ghostMaster.getName().getString()), true);
               LOGGER.debug("玩家 {} 使用鬼差G键技能秒杀沉寂状态遇鬼者：{}", player.getName().getString(), ghostMaster.getName().getString());
            } else {
               player.sendMessage(Text.literal("§c鬼差G键：目标遇鬼者不处于沉寂状态"), true);
            }
         } else if (targetEntity instanceof LuoQianGhostEntity) {
            player.sendMessage(Text.literal("§c鬼差G键：无法吞噬"), true);
         } else if (targetEntity instanceof GhostEntity ghostEntity) {
            if (!AdvancementManager.hasAdvancement(player, "smfs:become_god")) {
               player.sendMessage(Text.literal("§c鬼差G键：需要成神后才能吞噬厉鬼"), true);
               return;
            }

            executeGhostEntityKill(ghostEntity, player);
            GhostSkillManager.addBonusSuppressionSlot(player, 1);
            player.sendMessage(Text.literal("§a鬼差G键：成功压制并吞噬 " + ghostEntity.getName().getString()), true);
            LOGGER.debug("玩家 {} 使用鬼差G键技能压制鬼实体：{}，获得额外压制名额", player.getName().getString(), ghostEntity.getName().getString());
         } else {
            player.sendMessage(Text.literal("§c鬼差G键：只能对玩家、遇鬼者或鬼实体使用"), true);
         }

         spawnSkillParticles(player);
      } else {
         player.sendMessage(Text.literal("§c鬼差G键：准星位置没有可攻击的目标"), true);
      }
   }

   private static void executeInstantKill(ServerPlayerEntity targetPlayer, ServerPlayerEntity attacker) {
      ModEvents.processingSpiritDamage.set(true);

      try {
         float lethalDamage = targetPlayer.getHealth() * 2.0F;
         DamageSource damageSource = attacker.getDamageSources().playerAttack(attacker);
         targetPlayer.damage(damageSource, lethalDamage);
         if (targetPlayer.isAlive()) {
            targetPlayer.setHealth(0.0F);
         }

         spawnKillParticles(targetPlayer);
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }

   private static void executeInstantKill(GhostMasterEntity ghostMaster, ServerPlayerEntity attacker) {
      ModEvents.processingSpiritDamage.set(true);

      try {
         float lethalDamage = ghostMaster.getHealth() * 2.0F;
         DamageSource damageSource = attacker.getDamageSources().playerAttack(attacker);
         ghostMaster.damage(damageSource, lethalDamage);
         if (ghostMaster.isAlive()) {
            ghostMaster.setHealth(0.0F);
         }

         spawnKillParticles(ghostMaster);
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }

   private static void executeGhostEntityKill(GhostEntity ghostEntity, ServerPlayerEntity attacker) {
      GhostDeathHandler.markLegitimateRemoval(ghostEntity);
      ghostEntity.discard();
   }

   private static void spawnKillParticles(ServerPlayerEntity targetPlayer) {
      World world = targetPlayer.getWorld();

      for (int i = 0; i < 50; i++) {
         world.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            targetPlayer.getX() + (world.random.nextDouble() - 0.5) * 3.0,
            targetPlayer.getY() + world.random.nextDouble() * 3.0,
            targetPlayer.getZ() + (world.random.nextDouble() - 0.5) * 3.0,
            (world.random.nextDouble() - 0.5) * 0.5,
            world.random.nextDouble() * 0.5,
            (world.random.nextDouble() - 0.5) * 0.5
         );
      }

      for (int i = 0; i < 20; i++) {
         world.addParticle(
            ParticleTypes.EXPLOSION,
            targetPlayer.getX() + (world.random.nextDouble() - 0.5) * 2.0,
            targetPlayer.getY() + world.random.nextDouble() * 2.0,
            targetPlayer.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
            0.0,
            0.0,
            0.0
         );
      }
   }

   private static void spawnKillParticles(GhostMasterEntity ghostMaster) {
      World world = ghostMaster.getWorld();

      for (int i = 0; i < 50; i++) {
         world.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            ghostMaster.getX() + (world.random.nextDouble() - 0.5) * 3.0,
            ghostMaster.getY() + world.random.nextDouble() * 3.0,
            ghostMaster.getZ() + (world.random.nextDouble() - 0.5) * 3.0,
            (world.random.nextDouble() - 0.5) * 0.5,
            world.random.nextDouble() * 0.5,
            (world.random.nextDouble() - 0.5) * 0.5
         );
      }

      for (int i = 0; i < 20; i++) {
         world.addParticle(
            ParticleTypes.EXPLOSION,
            ghostMaster.getX() + (world.random.nextDouble() - 0.5) * 2.0,
            ghostMaster.getY() + world.random.nextDouble() * 2.0,
            ghostMaster.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
            0.0,
            0.0,
            0.0
         );
      }
   }

   private static void spawnSkillParticles(ServerPlayerEntity player) {
      World world = player.getWorld();

      for (int i = 0; i < 15; i++) {
         world.addParticle(
            ParticleTypes.SOUL_FIRE_FLAME,
            player.getX() + (world.random.nextDouble() - 0.5) * 3.0,
            player.getY() + world.random.nextDouble() * 3.0,
            player.getZ() + (world.random.nextDouble() - 0.5) * 3.0,
            0.0,
            0.1,
            0.0
         );
      }
   }
}
