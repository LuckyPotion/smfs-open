package com.xie.smfs.event;

import com.xie.smfs.api.TameableItemAPI;
import com.xie.smfs.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowChatMessage;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowCommandMessage;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SilentGhostTeleportHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(SilentGhostTeleportHandler.class);
   private static final int CONFIRMATION_TIMEOUT = 5;
   private static final Map<UUID, SilentGhostTeleportHandler.MentionInfo> playerMentionInfo = new HashMap<>();

   public static void register() {
      ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((AllowChatMessage)(message, sender, params) -> {
         String content = message.getSignedContent();
         checkForPlayerMentions(sender, content);
         return true;
      });
      ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((AllowCommandMessage)(message, sender, params) -> {
         String content = message.getSignedContent();
         ServerPlayerEntity player = sender.getPlayer();
         if (player != null) {
            checkForPlayerMentions(player, content);
         }

         return true;
      });
      LOGGER.info("静悄悄传送处理器注册完成");
   }

   private static void checkForPlayerMentions(ServerPlayerEntity sender, String content) {
      World world = sender.getWorld();
      if (!world.isClient()) {
         for (ServerPlayerEntity targetPlayer : world.getServer().getPlayerManager().getPlayerList()) {
            if (!targetPlayer.getUuid().equals(sender.getUuid())) {
               String targetName = targetPlayer.getName().getString();
               if (content.toLowerCase().contains(targetName.toLowerCase())
                  && hasSilentGhostEquipped(targetPlayer)
                  && !targetPlayer.hasStatusEffect(ModEffects.SILENCE)
                  && !targetPlayer.hasStatusEffect(ModEffects.DREAM)) {
                  SilentGhostTeleportHandler.MentionInfo mentionInfo = new SilentGhostTeleportHandler.MentionInfo(
                     System.currentTimeMillis(), sender.getUuid(), sender.getName().getString()
                  );
                  playerMentionInfo.put(targetPlayer.getUuid(), mentionInfo);
                  sendJKeySkillHint(targetPlayer, sender);
                  LOGGER.info("静悄悄J键技能触发：{} 在聊天中提及了驾驭者 {}，驾驭者可以按J键突袭", sender.getName().getString(), targetName);
                  break;
               }
            }
         }
      }
   }

   private static boolean hasSilentGhostEquipped(ServerPlayerEntity player) {
      TameableItemAPI api = TameableItemAPI.getInstance();
      return api.hasTamedGhost(player, "silent_ghost");
   }

   private static void performTeleport(ServerPlayerEntity requester, ServerPlayerEntity target) {
      World world = target.getWorld();
      Vec3d targetPos = target.getPos();
      if (world instanceof ServerWorld serverWorld) {
         requester.teleport(serverWorld, targetPos.x, targetPos.y, targetPos.z, target.getYaw(), target.getPitch());
      } else {
         requester.teleport(targetPos.x, targetPos.y, targetPos.z);
      }

      world.playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

      for (int i = 0; i < 10; i++) {
         world.addParticle(
            ParticleTypes.PORTAL,
            targetPos.x + (world.random.nextDouble() - 0.5) * 2.0,
            targetPos.y + world.random.nextDouble() * 2.0,
            targetPos.z + (world.random.nextDouble() - 0.5) * 2.0,
            (world.random.nextDouble() - 0.5) * 0.2,
            world.random.nextDouble() * 0.2,
            (world.random.nextDouble() - 0.5) * 0.2
         );
      }
   }

   private static MinecraftServer getServerFromThread() {
      LOGGER.warn("在异步线程中无法直接获取服务器实例");
      return null;
   }

   private static void sendJKeySkillHint(ServerPlayerEntity driver, ServerPlayerEntity mentioner) {
      driver.sendMessage(Text.literal("§6==========================================="), false);
      driver.sendMessage(Text.literal("§c静悄悄被动技能触发"), false);
      driver.sendMessage(Text.literal("§a检测到玩家 §f" + mentioner.getName().getString() + " §a在聊天中提及了你"), false);
      driver.sendMessage(Text.literal("§e按 §6J键 §e立即出现在对方位置"), false);
      driver.sendMessage(Text.literal("§7(J键技能将在 5 秒后过期)"), false);
      driver.sendMessage(Text.literal("§6==========================================="), false);
   }

   public static void handleJKeyTeleport(ServerPlayerEntity attacker) {
      SilentGhostTeleportHandler.MentionInfo mentionInfo = playerMentionInfo.get(attacker.getUuid());
      if (mentionInfo == null) {
         attacker.sendMessage(Text.literal("§c没有人正在呼唤你"), true);
      } else {
         long currentTime = System.currentTimeMillis();
         if (currentTime - mentionInfo.getTimestamp() > 5000L) {
            attacker.sendMessage(Text.literal("§c技能已过期"), true);
            playerMentionInfo.remove(attacker.getUuid());
         } else {
            ServerPlayerEntity target = findTargetPlayerFromMention(attacker, mentionInfo);
            if (target == null) {
               attacker.sendMessage(Text.literal("§c目标玩家 " + mentionInfo.getTargetPlayerName() + " 已离线"), false);
               playerMentionInfo.remove(attacker.getUuid());
            } else {
               performTeleport(attacker, target);
               playerMentionInfo.remove(attacker.getUuid());
               attacker.sendMessage(Text.literal("§a已成功出现在 " + target.getName().getString() + " 的位置"), false);
               target.sendMessage(Text.literal("§c玩家 " + attacker.getName().getString() + " 被呼唤过来！"), false);
               LOGGER.info("静悄悄J键技能传送完成：{} 突袭 -> {}", attacker.getName().getString(), target.getName().getString());
            }
         }
      }
   }

   private static ServerPlayerEntity findTargetPlayerFromMention(ServerPlayerEntity attacker, SilentGhostTeleportHandler.MentionInfo mentionInfo) {
      World world = attacker.getWorld();
      if (world.isClient()) {
         return null;
      }

      ServerPlayerEntity target = world.getServer().getPlayerManager().getPlayer(mentionInfo.getTargetPlayerUuid());
      return target != null && target.isAlive() ? target : null;
   }

   private static class MentionInfo {
      private final long timestamp;
      private final UUID targetPlayerUuid;
      private final String targetPlayerName;

      public MentionInfo(long timestamp, UUID targetPlayerUuid, String targetPlayerName) {
         this.timestamp = timestamp;
         this.targetPlayerUuid = targetPlayerUuid;
         this.targetPlayerName = targetPlayerName;
      }

      public long getTimestamp() {
         return this.timestamp;
      }

      public UUID getTargetPlayerUuid() {
         return this.targetPlayerUuid;
      }

      public String getTargetPlayerName() {
         return this.targetPlayerName;
      }
   }
}
