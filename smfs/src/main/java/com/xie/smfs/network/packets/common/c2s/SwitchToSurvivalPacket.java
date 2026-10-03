package com.xie.smfs.network.packets.common.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.event.PlayerDeathHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwitchToSurvivalPacket {
   private static final Logger LOGGER = LoggerFactory.getLogger(SwitchToSurvivalPacket.class);
   public static final Identifier ID = new Identifier("smfs", "switch_to_survival");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, SwitchToSurvivalPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            boolean shouldClearGhosts = SpectateModePacket.isInSpectatorMode(player.getUuid());
            if (shouldClearGhosts) {
               int occupiedSlots = PlayerEvents.countOccupiedGhostSlots(player);
               boolean loseGhostsOnDeath = ModConfig.getInstance().loseGhostsOnDeath;
               if (loseGhostsOnDeath) {
                  PlayerDeathHandler.clearAllGhostSlotsAndAttributes(player);
                  int remainingSlots = PlayerEvents.countOccupiedGhostSlots(player);
                  player.sendMessage(Text.literal("§c§l所有灵异已被清除"), true);
               } else {
                  PlayerEvents.recalculateAllSpiritAttributesFromSlots(player);
                  player.sendMessage(Text.literal("§a§l已转世，保留了所有灵异能力"), true);
               }

               SpectateModePacket.removeSpectatorFlag(player.getUuid());
            } else {
               LOGGER.warn("⚠️ 步骤2：没有旁观者标志，跳过清空鬼数据");
               LOGGER.warn("这可能意味着：1) 玩家不是通过'成为亡魂'进入旁观者的，2) 标志被提前移除了");
            }

            GameMode currentMode = player.interactionManager.getGameMode();
            boolean success = player.changeGameMode(GameMode.SURVIVAL);
            if (!success && currentMode == GameMode.SURVIVAL) {
               LOGGER.warn("玩家已经是生存模式，但客户端认为是旁观者！强制重新同步");
            }

            PlayerAbilities abilities = player.getAbilities();
            abilities.flying = false;
            abilities.allowFlying = false;
            abilities.invulnerable = false;
            abilities.creativeMode = false;
            player.noClip = false;
            player.sendAbilitiesUpdate();
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, GameMode.SURVIVAL.getId()));
            player.networkHandler.sendPacket(new PlayerListS2CPacket(Action.UPDATE_GAME_MODE, player));
            player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker().getChangedEntries()));
            if (player.getPose() != EntityPose.STANDING) {
               player.setPose(EntityPose.STANDING);
            }

            player.setSneaking(false);
            player.setSprinting(false);
            player.setSwimming(false);
            player.setVelocity(0.0, player.getVelocity().y, 0.0);
            player.fallDistance = 0.0F;
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            player.teleport(player.getServerWorld(), x, y, z, yaw, pitch);
            EntityAttributeInstance speedAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            EntityAttributeInstance flyingSpeedAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_FLYING_SPEED);
            if (speedAttribute != null) {
               double currentSpeed = speedAttribute.getValue();
               double baseSpeed = speedAttribute.getBaseValue();
               if (currentSpeed < 0.05 || baseSpeed < 0.05) {
                  speedAttribute.setBaseValue(0.1);
                  speedAttribute.clearModifiers();
               }
            } else {
               LOGGER.warn("无法获取玩家移动速度属性！");
            }

            if (flyingSpeedAttribute != null) {
               double currentFlySpeed = flyingSpeedAttribute.getValue();
               double baseFlySpeed = flyingSpeedAttribute.getBaseValue();
               if (currentFlySpeed > 0.01 || baseFlySpeed > 0.01) {
                  LOGGER.warn("检测到残留的飞行速度！重置为 0");
                  flyingSpeedAttribute.setBaseValue(0.0);
                  flyingSpeedAttribute.clearModifiers();
               }
            } else {
               LOGGER.info("飞行速度属性不存在（1.20.1版本正常）");
            }

            if (player.getHealth() <= 0.0F || player.isDead()) {
               player.setHealth(player.getMaxHealth());
               LOGGER.info("已恢复生命值: {}/{}", player.getHealth(), player.getMaxHealth());
            }

            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(20.0F);
            float maxSanity = PlayerEvents.getMaxSanity(player);
            PlayerEvents.setCurrentSanity(player, maxSanity);
            player.sendMessage(Text.translatable("message.smfs.switch_to_survival"), true);
         } catch (Exception e) {
            LOGGER.error("处理 SwitchToSurvivalPacket 时发生错误", e);
         }
      });
   }

   public void write(PacketByteBuf buf) {
   }
}
