package com.xie.smfs.event;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.data.PlayerRoyalCurseManager;
import com.xie.smfs.data.RoyalCurseData;
import com.xie.smfs.data.RoyalCurseServantData;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.network.ModNetwork;
import com.xie.smfs.network.packets.ui.s2c.ShowCustomDeathScreenPacket;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.util.InstantKillUtil;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDeath;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerDeathHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(PlayerDeathHandler.class);

   public static void register() {
      ServerLivingEntityEvents.ALLOW_DEATH.register((AllowDeath)(entity, damageSource, damageAmount) -> {
         if (entity instanceof ServerPlayerEntity serverPlayer) {
            if (InstantKillUtil.isMarkedForInstantKill(serverPlayer)) {
               return true;
            }

            StatusEffectInstance curseEffect = serverPlayer.getStatusEffect(ModEffects.MUSIC_BOX_CURSE);
            if (curseEffect != null) {
               serverPlayer.setHealth(1.0F);
               return false;
            }

            handlePlayerDeath(serverPlayer, damageSource);
         }

         return true;
      });
      LOGGER.info("玩家死亡事件处理器已注册");
   }

   public static void handlePlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
      World world = player.getWorld();
      if (!world.isClient()) {
         PlayerGhostChildManager.recallGhostChild(player);
         boolean hasTamedGhost = false;

         for (int i = 0; i < 10; i++) {
            if (PlayerEvents.isGhostSlotOccupied(player, i)) {
               hasTamedGhost = true;
               break;
            }
         }

         if (!hasTamedGhost) {
            LOGGER.info("玩家 {} 死亡，但未驾驭任何鬼，不厉鬼复苏", player.getName().getString());
         } else {
            ModConfig config = ModConfig.getInstance();
            if (!config.enableEvilGhostRevival) {
               LOGGER.info("玩家 {} 死亡，但厉鬼复苏功能已禁用，不生成玩家鬼魂", player.getName().getString());
            } else if (PlayerRoyalCurseManager.hasRoyalCurseUnlocked(player)) {
               PlayerEvents.saveLastSurvivalTime(player);
               PlayerEvents.resetSurvivalTime(player);
               storePlayerAsServant(player);
               PlayerRoyalCurseManager.recallAllServants(player);
               Text deathMessage = damageSource.getDeathMessage(player);
               ModNetwork.sendToClient(new ShowCustomDeathScreenPacket(deathMessage), player);
               LOGGER.info("玩家 {} 死亡，已解锁王家诅咒，将鬼存储", player.getName().getString());
            } else {
               for (int i = 0; i < 10; i++) {
                  String ghostType = PlayerEvents.getGhostTypeInSlot(player, i);
                  if ("silent_ghost".equals(ghostType)) {
                     if (AdvancementManager.hasAdvancement(player, "smfs:become_god")) {
                        Text deathMessage = damageSource.getDeathMessage(player);
                        ModNetwork.sendToClient(new ShowCustomDeathScreenPacket(deathMessage, true), player);
                        LOGGER.info("玩家 {} 驾驭静悄悄且成神，发送重新入侵死亡界面", player.getName().getString());
                        return;
                     }
                     break;
                  }
               }

               Vec3d deathPos = player.getPos();
               PlayerEvents.saveLastSurvivalTime(player);
               PlayerEvents.resetSurvivalTime(player);

               try {
                  PlayerGhostEntity playerGhost = PlayerGhostEntity.createFromPlayer(player, world);
                  playerGhost.refreshPositionAndAngles(deathPos.x, deathPos.y, deathPos.z, player.getYaw(), player.getPitch());
                  world.spawnEntity(playerGhost);
                  Text deathMessage = damageSource.getDeathMessage(player);
                  ModNetwork.sendToClient(new ShowCustomDeathScreenPacket(deathMessage), player);
                  LOGGER.info("玩家 {} 死亡，已生成玩家鬼魂", player.getName().getString());
               } catch (Exception e) {
                  LOGGER.error("生成玩家鬼魂时发生错误", e);
               }
            }
         }
      }
   }

   private static void storePlayerAsServant(ServerPlayerEntity player) {
      World world = player.getWorld();

      try {
         PlayerGhostEntity playerGhost = PlayerGhostEntity.createFromPlayer(player, world);
         UUID servantUuid = playerGhost.getUuid();
         RoyalCurseData royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(player);
         RoyalCurseServantData servantData = new RoyalCurseServantData(servantUuid);
         servantData.setPlayerName(playerGhost.getPlayerName());
         servantData.setPlayerUuid(playerGhost.getPlayerUuid());
         servantData.setGhostDomainColor(playerGhost.getGhostDomainColor());
         servantData.setGhostDomainLevel(playerGhost.getGhostDomainLevel());
         servantData.setGhostDomainRadius(playerGhost.getGhostDomainRadius());
         servantData.setSpiritualResistance(playerGhost.getSpiritualResistance());
         servantData.setSpiritualDamage(playerGhost.getSpiritualDamage());
         servantData.setSpiritualStrength(playerGhost.getSpiritualStrength());
         servantData.setMaxSpiritualStrength(playerGhost.getMaxSpiritualStrength());
         servantData.setRecoveryFactor(playerGhost.getRecoveryFactor());
         servantData.setPlayerGhosts(playerGhost.getPlayerGhosts());
         if (royalCurseData.getServantCount() < 6) {
            royalCurseData.addServant(servantData);
            PlayerRoyalCurseManager.saveRoyalCurseData(player, royalCurseData);
            LOGGER.debug("玩家 {} 已被存储为奴仆，UUID: {}", player.getName().getString(), servantUuid);
         } else {
            LOGGER.debug("玩家 {} 死亡，已达到最大奴仆数量上限(6个)，无法存储为奴仆", player.getName().getString());
         }
      } catch (Exception e) {
         LOGGER.error("存储玩家鬼魂为奴仆时发生错误", e);
      }
   }

   public static void clearAllGhostSlotsAndAttributes(ServerPlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         PlayerEvents.clearGhostSlot(player, i);
      }

      PlayerEvents.clearSpiritAttributes(player);
      LOGGER.info("玩家 {} 的所有鬼眼槽位和灵异属性已清空", player.getName().getString());
   }
}
