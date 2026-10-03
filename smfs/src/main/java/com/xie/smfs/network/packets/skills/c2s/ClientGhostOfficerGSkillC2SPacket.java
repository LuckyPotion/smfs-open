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
               player.method_7353(Text.method_43470("§c您没有驾驭鬼差，无法使用此技能"), true);
               return;
            }

            if (result == GhostDomainManager.SkillCheckResult.LEVEL_TOO_LOW) {
               player.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
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
            if (targetPlayer.method_6059(ModEffects.SILENCE)) {
               if (targetPlayer.method_6059(ModEffects.MUSIC_BOX_CURSE)) {
                  player.method_7353(Text.method_43470("§c鬼差G键：秒杀失败"), true);
               } else {
                  executeInstantKill(targetPlayer, player);
                  player.method_7353(Text.method_43470("§a鬼差G键：成功秒杀沉寂状态下的玩家 " + targetPlayer.method_5477().getString()), true);
                  LOGGER.debug("玩家 {} 使用鬼差G键技能秒杀沉寂状态玩家：{}", player.method_5477().getString(), targetPlayer.method_5477().getString());
               }
            } else {
               player.method_7353(Text.method_43470("§c鬼差G键：目标玩家不处于沉寂状态"), true);
            }
         } else if (targetEntity instanceof GhostMasterEntity ghostMaster) {
            if (ghostMaster.method_6059(ModEffects.SILENCE)) {
               executeInstantKill(ghostMaster, player);
               player.method_7353(Text.method_43470("§a鬼差G键：成功秒杀沉寂状态下的遇鬼者 " + ghostMaster.method_5477().getString()), true);
               LOGGER.debug("玩家 {} 使用鬼差G键技能秒杀沉寂状态遇鬼者：{}", player.method_5477().getString(), ghostMaster.method_5477().getString());
            } else {
               player.method_7353(Text.method_43470("§c鬼差G键：目标遇鬼者不处于沉寂状态"), true);
            }
         } else if (targetEntity instanceof LuoQianGhostEntity) {
            player.method_7353(Text.method_43470("§c鬼差G键：无法吞噬"), true);
         } else if (targetEntity instanceof GhostEntity ghostEntity) {
            if (!AdvancementManager.hasAdvancement(player, "smfs:become_god")) {
               player.method_7353(Text.method_43470("§c鬼差G键：需要成神后才能吞噬厉鬼"), true);
               return;
            }

            executeGhostEntityKill(ghostEntity, player);
            GhostSkillManager.addBonusSuppressionSlot(player, 1);
            player.method_7353(Text.method_43470("§a鬼差G键：成功压制并吞噬 " + ghostEntity.method_5477().getString()), true);
            LOGGER.debug("玩家 {} 使用鬼差G键技能压制鬼实体：{}，获得额外压制名额", player.method_5477().getString(), ghostEntity.method_5477().getString());
         } else {
            player.method_7353(Text.method_43470("§c鬼差G键：只能对玩家、遇鬼者或鬼实体使用"), true);
         }

         spawnSkillParticles(player);
      } else {
         player.method_7353(Text.method_43470("§c鬼差G键：准星位置没有可攻击的目标"), true);
      }
   }

   private static void executeInstantKill(ServerPlayerEntity targetPlayer, ServerPlayerEntity attacker) {
      ModEvents.processingSpiritDamage.set(true);

      try {
         float lethalDamage = targetPlayer.method_6032() * 2.0F;
         DamageSource damageSource = attacker.method_48923().method_48802(attacker);
         targetPlayer.method_5643(damageSource, lethalDamage);
         if (targetPlayer.method_5805()) {
            targetPlayer.method_6033(0.0F);
         }

         spawnKillParticles(targetPlayer);
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }

   private static void executeInstantKill(GhostMasterEntity ghostMaster, ServerPlayerEntity attacker) {
      ModEvents.processingSpiritDamage.set(true);

      try {
         float lethalDamage = ghostMaster.method_6032() * 2.0F;
         DamageSource damageSource = attacker.method_48923().method_48802(attacker);
         ghostMaster.method_5643(damageSource, lethalDamage);
         if (ghostMaster.method_5805()) {
            ghostMaster.method_6033(0.0F);
         }

         spawnKillParticles(ghostMaster);
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }

   private static void executeGhostEntityKill(GhostEntity ghostEntity, ServerPlayerEntity attacker) {
      GhostDeathHandler.markLegitimateRemoval(ghostEntity);
      ghostEntity.method_31472();
   }

   private static void spawnKillParticles(ServerPlayerEntity targetPlayer) {
      World world = targetPlayer.method_37908();

      for (int i = 0; i < 50; i++) {
         world.method_8406(
            ParticleTypes.field_22246,
            targetPlayer.method_23317() + (world.field_9229.method_43058() - 0.5) * 3.0,
            targetPlayer.method_23318() + world.field_9229.method_43058() * 3.0,
            targetPlayer.method_23321() + (world.field_9229.method_43058() - 0.5) * 3.0,
            (world.field_9229.method_43058() - 0.5) * 0.5,
            world.field_9229.method_43058() * 0.5,
            (world.field_9229.method_43058() - 0.5) * 0.5
         );
      }

      for (int i = 0; i < 20; i++) {
         world.method_8406(
            ParticleTypes.field_11236,
            targetPlayer.method_23317() + (world.field_9229.method_43058() - 0.5) * 2.0,
            targetPlayer.method_23318() + world.field_9229.method_43058() * 2.0,
            targetPlayer.method_23321() + (world.field_9229.method_43058() - 0.5) * 2.0,
            0.0,
            0.0,
            0.0
         );
      }
   }

   private static void spawnKillParticles(GhostMasterEntity ghostMaster) {
      World world = ghostMaster.method_37908();

      for (int i = 0; i < 50; i++) {
         world.method_8406(
            ParticleTypes.field_22246,
            ghostMaster.method_23317() + (world.field_9229.method_43058() - 0.5) * 3.0,
            ghostMaster.method_23318() + world.field_9229.method_43058() * 3.0,
            ghostMaster.method_23321() + (world.field_9229.method_43058() - 0.5) * 3.0,
            (world.field_9229.method_43058() - 0.5) * 0.5,
            world.field_9229.method_43058() * 0.5,
            (world.field_9229.method_43058() - 0.5) * 0.5
         );
      }

      for (int i = 0; i < 20; i++) {
         world.method_8406(
            ParticleTypes.field_11236,
            ghostMaster.method_23317() + (world.field_9229.method_43058() - 0.5) * 2.0,
            ghostMaster.method_23318() + world.field_9229.method_43058() * 2.0,
            ghostMaster.method_23321() + (world.field_9229.method_43058() - 0.5) * 2.0,
            0.0,
            0.0,
            0.0
         );
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
