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
         String content = message.method_44862();
         checkForPlayerMentions(sender, content);
         return true;
      });
      ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((AllowCommandMessage)(message, sender, params) -> {
         String content = message.method_44862();
         ServerPlayerEntity player = sender.method_44023();
         if (player != null) {
            checkForPlayerMentions(player, content);
         }

         return true;
      });
      LOGGER.info("静悄悄传送处理器注册完成");
   }

   private static void checkForPlayerMentions(ServerPlayerEntity sender, String content) {
      World world = sender.method_37908();
      if (!world.method_8608()) {
         for (ServerPlayerEntity targetPlayer : world.method_8503().method_3760().method_14571()) {
            if (!targetPlayer.method_5667().equals(sender.method_5667())) {
               String targetName = targetPlayer.method_5477().getString();
               if (content.toLowerCase().contains(targetName.toLowerCase())
                  && hasSilentGhostEquipped(targetPlayer)
                  && !targetPlayer.method_6059(ModEffects.SILENCE)
                  && !targetPlayer.method_6059(ModEffects.DREAM)) {
                  SilentGhostTeleportHandler.MentionInfo mentionInfo = new SilentGhostTeleportHandler.MentionInfo(
                     System.currentTimeMillis(), sender.method_5667(), sender.method_5477().getString()
                  );
                  playerMentionInfo.put(targetPlayer.method_5667(), mentionInfo);
                  sendJKeySkillHint(targetPlayer, sender);
                  LOGGER.info("静悄悄J键技能触发：{} 在聊天中提及了驾驭者 {}，驾驭者可以按J键突袭", sender.method_5477().getString(), targetName);
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
      World world = target.method_37908();
      Vec3d targetPos = target.method_19538();
      if (world instanceof ServerWorld serverWorld) {
         requester.method_14251(serverWorld, targetPos.field_1352, targetPos.field_1351, targetPos.field_1350, target.method_36454(), target.method_36455());
      } else {
         requester.method_20620(targetPos.field_1352, targetPos.field_1351, targetPos.field_1350);
      }

      world.method_43128(null, targetPos.field_1352, targetPos.field_1351, targetPos.field_1350, SoundEvents.field_14879, SoundCategory.field_15248, 1.0F, 1.0F);

      for (int i = 0; i < 10; i++) {
         world.method_8406(
            ParticleTypes.field_11214,
            targetPos.field_1352 + (world.field_9229.method_43058() - 0.5) * 2.0,
            targetPos.field_1351 + world.field_9229.method_43058() * 2.0,
            targetPos.field_1350 + (world.field_9229.method_43058() - 0.5) * 2.0,
            (world.field_9229.method_43058() - 0.5) * 0.2,
            world.field_9229.method_43058() * 0.2,
            (world.field_9229.method_43058() - 0.5) * 0.2
         );
      }
   }

   private static MinecraftServer getServerFromThread() {
      LOGGER.warn("在异步线程中无法直接获取服务器实例");
      return null;
   }

   private static void sendJKeySkillHint(ServerPlayerEntity driver, ServerPlayerEntity mentioner) {
      driver.method_7353(Text.method_43470("§6==========================================="), false);
      driver.method_7353(Text.method_43470("§c静悄悄被动技能触发"), false);
      driver.method_7353(Text.method_43470("§a检测到玩家 §f" + mentioner.method_5477().getString() + " §a在聊天中提及了你"), false);
      driver.method_7353(Text.method_43470("§e按 §6J键 §e立即出现在对方位置"), false);
      driver.method_7353(Text.method_43470("§7(J键技能将在 5 秒后过期)"), false);
      driver.method_7353(Text.method_43470("§6==========================================="), false);
   }

   public static void handleJKeyTeleport(ServerPlayerEntity attacker) {
      SilentGhostTeleportHandler.MentionInfo mentionInfo = playerMentionInfo.get(attacker.method_5667());
      if (mentionInfo == null) {
         attacker.method_7353(Text.method_43470("§c没有人正在呼唤你"), true);
      } else {
         long currentTime = System.currentTimeMillis();
         if (currentTime - mentionInfo.getTimestamp() > 5000L) {
            attacker.method_7353(Text.method_43470("§c技能已过期"), true);
            playerMentionInfo.remove(attacker.method_5667());
         } else {
            ServerPlayerEntity target = findTargetPlayerFromMention(attacker, mentionInfo);
            if (target == null) {
               attacker.method_7353(Text.method_43470("§c目标玩家 " + mentionInfo.getTargetPlayerName() + " 已离线"), false);
               playerMentionInfo.remove(attacker.method_5667());
            } else {
               performTeleport(attacker, target);
               playerMentionInfo.remove(attacker.method_5667());
               attacker.method_7353(Text.method_43470("§a已成功出现在 " + target.method_5477().getString() + " 的位置"), false);
               target.method_7353(Text.method_43470("§c玩家 " + attacker.method_5477().getString() + " 被呼唤过来！"), false);
               LOGGER.info("静悄悄J键技能传送完成：{} 突袭 -> {}", attacker.method_5477().getString(), target.method_5477().getString());
            }
         }
      }
   }

   private static ServerPlayerEntity findTargetPlayerFromMention(ServerPlayerEntity attacker, SilentGhostTeleportHandler.MentionInfo mentionInfo) {
      World world = attacker.method_37908();
      if (world.method_8608()) {
         return null;
      }

      ServerPlayerEntity target = world.method_8503().method_3760().method_14602(mentionInfo.getTargetPlayerUuid());
      return target != null && target.method_5805() ? target : null;
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
