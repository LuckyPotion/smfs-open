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
            boolean shouldClearGhosts = SpectateModePacket.isInSpectatorMode(player.method_5667());
            if (shouldClearGhosts) {
               int occupiedSlots = PlayerEvents.countOccupiedGhostSlots(player);
               boolean loseGhostsOnDeath = ModConfig.getInstance().loseGhostsOnDeath;
               if (loseGhostsOnDeath) {
                  PlayerDeathHandler.clearAllGhostSlotsAndAttributes(player);
                  int remainingSlots = PlayerEvents.countOccupiedGhostSlots(player);
                  player.method_7353(Text.method_43470("§c§l所有灵异已被清除"), true);
               } else {
                  PlayerEvents.recalculateAllSpiritAttributesFromSlots(player);
                  player.method_7353(Text.method_43470("§a§l已转世，保留了所有灵异能力"), true);
               }

               SpectateModePacket.removeSpectatorFlag(player.method_5667());
            } else {
               LOGGER.warn("⚠️ 步骤2：没有旁观者标志，跳过清空鬼数据");
               LOGGER.warn("这可能意味着：1) 玩家不是通过'成为亡魂'进入旁观者的，2) 标志被提前移除了");
            }

            GameMode currentMode = player.field_13974.method_14257();
            boolean success = player.method_7336(GameMode.field_9215);
            if (!success && currentMode == GameMode.field_9215) {
               LOGGER.warn("玩家已经是生存模式，但客户端认为是旁观者！强制重新同步");
            }

            PlayerAbilities abilities = player.method_31549();
            abilities.field_7479 = false;
            abilities.field_7478 = false;
            abilities.field_7480 = false;
            abilities.field_7477 = false;
            player.field_5960 = false;
            player.method_7355();
            player.field_13987.method_14364(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.field_25648, GameMode.field_9215.method_8379()));
            player.field_13987.method_14364(new PlayerListS2CPacket(Action.field_29137, player));
            player.field_13987.method_14364(new EntityTrackerUpdateS2CPacket(player.method_5628(), player.method_5841().method_46357()));
            if (player.method_18376() != EntityPose.field_18076) {
               player.method_18380(EntityPose.field_18076);
            }

            player.method_5660(false);
            player.method_5728(false);
            player.method_5796(false);
            player.method_18800(0.0, player.method_18798().field_1351, 0.0);
            player.field_6017 = 0.0F;
            double x = player.method_23317();
            double y = player.method_23318();
            double z = player.method_23321();
            float yaw = player.method_36454();
            float pitch = player.method_36455();
            player.method_14251(player.method_51469(), x, y, z, yaw, pitch);
            EntityAttributeInstance speedAttribute = player.method_5996(EntityAttributes.field_23719);
            EntityAttributeInstance flyingSpeedAttribute = player.method_5996(EntityAttributes.field_23720);
            if (speedAttribute != null) {
               double currentSpeed = speedAttribute.method_6194();
               double baseSpeed = speedAttribute.method_6201();
               if (currentSpeed < 0.05 || baseSpeed < 0.05) {
                  speedAttribute.method_6192(0.1);
                  speedAttribute.method_6203();
               }
            } else {
               LOGGER.warn("无法获取玩家移动速度属性！");
            }

            if (flyingSpeedAttribute != null) {
               double currentFlySpeed = flyingSpeedAttribute.method_6194();
               double baseFlySpeed = flyingSpeedAttribute.method_6201();
               if (currentFlySpeed > 0.01 || baseFlySpeed > 0.01) {
                  LOGGER.warn("检测到残留的飞行速度！重置为 0");
                  flyingSpeedAttribute.method_6192(0.0);
                  flyingSpeedAttribute.method_6203();
               }
            } else {
               LOGGER.info("飞行速度属性不存在（1.20.1版本正常）");
            }

            if (player.method_6032() <= 0.0F || player.method_29504()) {
               player.method_6033(player.method_6063());
               LOGGER.info("已恢复生命值: {}/{}", player.method_6032(), player.method_6063());
            }

            player.method_7344().method_7580(20);
            player.method_7344().method_7581(20.0F);
            float maxSanity = PlayerEvents.getMaxSanity(player);
            PlayerEvents.setCurrentSanity(player, maxSanity);
            player.method_7353(Text.method_43471("message.smfs.switch_to_survival"), true);
         } catch (Exception e) {
            LOGGER.error("处理 SwitchToSurvivalPacket 时发生错误", e);
         }
      });
   }

   public void write(PacketByteBuf buf) {
   }
}
