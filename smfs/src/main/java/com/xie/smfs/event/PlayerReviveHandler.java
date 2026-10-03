package com.xie.smfs.event;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerReviveHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(PlayerReviveHandler.class);

   public static void register() {
      ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> {
         if (!alive) {
            LOGGER.debug("玩家 {} 触发 AFTER_RESPAWN 事件", newPlayer.getName().getString());
            LOGGER.debug("玩家状态 - 游戏模式: {}, 旁观者标志: {}", newPlayer.interactionManager.getGameMode(), SpectateModePacket.isInSpectatorMode(newPlayer.getUuid()));
            NbtCompound playerData = PlayerEvents.getCachedData(newPlayer);
            if (playerData.getBoolean("reinvade_active")) {
               LOGGER.debug("玩家 {} 重新入侵，保留鬼数据不清空", newPlayer.getName().getString());
               PlayerEvents.recalculateAllSpiritAttributesFromSlots(newPlayer);
            } else if (SpectateModePacket.isInSpectatorMode(newPlayer.getUuid())) {
               LOGGER.debug("玩家 {} 点击'成为亡魂'，现在切换到旁观者模式（保留鬼数据）", newPlayer.getName().getString());
               newPlayer.changeGameMode(GameMode.SPECTATOR);
               LOGGER.debug("已切换到旁观者模式: {}", newPlayer.interactionManager.getGameMode());
               newPlayer.sendAbilitiesUpdate();
               newPlayer.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SPECTATOR.getId()));
               LOGGER.debug("✅ 成为亡魂完成，鬼数据已保留，等待按B键转世");
               PlayerEvents.recalculateAllSpiritAttributesFromSlots(newPlayer);
            } else {
               boolean loseGhostsOnDeath = ModConfig.getInstance().loseGhostsOnDeath;
               if (loseGhostsOnDeath) {
                  LOGGER.debug("玩家 {} 正常复活（点击'立即复活'），配置要求清空鬼数据", newPlayer.getName().getString());
                  handlePlayerRevive(newPlayer);
               } else {
                  LOGGER.debug("玩家 {} 正常复活（点击'立即复活'），配置要求保留鬼数据", newPlayer.getName().getString());
                  PlayerEvents.recalculateAllSpiritAttributesFromSlots(newPlayer);
                  newPlayer.sendMessage(Text.literal("§a§l你已复活，保留了所有灵异能力"), true);
               }
            }
         } else {
            LOGGER.debug("玩家 {} 维度传送，保留灵异数据", newPlayer.getName().getString());
         }

         NbtCompound playerData = PlayerEvents.getCachedData(newPlayer);
         if (playerData.getBoolean("reinvade_active")) {
            double x = playerData.getDouble("reinvade_x");
            double y = playerData.getDouble("reinvade_y");
            double z = playerData.getDouble("reinvade_z");
            newPlayer.teleport(x, y, z);
            playerData.remove("reinvade_active");
            playerData.remove("reinvade_x");
            playerData.remove("reinvade_y");
            playerData.remove("reinvade_z");
            PlayerEvents.saveDataToPlayer(newPlayer, playerData);
            newPlayer.sendMessage(Text.literal("§a§l已重新入侵到死亡地点！"), true);
            LOGGER.debug("玩家 {} 重新入侵，传送到死亡位置: ({}, {}, {})", newPlayer.getName().getString(), x, y, z);
         }
      });
      LOGGER.debug("玩家复活事件处理器已注册");
   }

   public static void handlePlayerRevive(ServerPlayerEntity player) {
      if (!player.getWorld().isClient()) {
         try {
            PlayerDeathHandler.clearAllGhostSlotsAndAttributes(player);
            LOGGER.debug("玩家 {} 已复活，所有灵异属性和槽位已清空", player.getName().getString());
         } catch (Exception e) {
            LOGGER.error("处理玩家复活时发生错误", e);
         }
      }
   }
}
