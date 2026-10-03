package com.xie.smfs.event.network;

import com.xie.smfs.network.packets.common.s2c.GhostPressureDetectionPacket;
import com.xie.smfs.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostPressureDetectionHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostPressureDetectionHandler.class);
   private static final Map<UUID, Boolean> lastDetectionResults = new HashMap<>();

   public static void register() {
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (world.method_8510() % 10L == 0L) {
            detectGhostPressureEffects(world);
         }
      });
   }

   private static void detectGhostPressureEffects(World world) {
      if (!world.method_8608()) {
         MinecraftServer server = world.method_8503();
         if (server != null) {
            for (ServerPlayerEntity player : server.method_3760().method_14571()) {
               Box detectionBox = new Box(player.method_24515()).method_1014(32.0);

               for (LivingEntity entity : world.method_8390(LivingEntity.class, detectionBox, e -> true)) {
                  if (entity != player) {
                     boolean hasGhostPressure = checkEntityHasGhostPressure(entity);
                     UUID entityId = entity.method_5667();
                     boolean lastResult = lastDetectionResults.getOrDefault(entityId, false);
                     if (hasGhostPressure != lastResult) {
                        GhostPressureDetectionPacket.sendToClient(player, entityId, hasGhostPressure);
                        lastDetectionResults.put(entityId, hasGhostPressure);
                        if (hasGhostPressure) {
                           LOGGER.debug("服务端检测到实体 {} 有鬼压人buff，通知玩家 {}", entity.method_5477().getString(), player.method_5477().getString());
                        }
                     }
                  }
               }
            }

            cleanupExpiredCache(world);
         }
      }
   }

   public static boolean checkEntityHasGhostPressure(LivingEntity entity) {
      if (entity.method_6059(ModEffects.GHOST_PRESSURE)) {
         return true;
      }

      if (entity.method_6112(ModEffects.GHOST_PRESSURE) != null) {
         return true;
      }

      for (StatusEffectInstance effectInstance : entity.method_6026()) {
         if (effectInstance.method_5579() == ModEffects.GHOST_PRESSURE) {
            return true;
         }
      }

      return false;
   }

   private static void cleanupExpiredCache(World world) {
      lastDetectionResults.entrySet().removeIf(entry -> {
         UUID entityId = entry.getKey();
         if (!(world instanceof ServerWorld serverWorld)) {
            return true;
         } else {
            LivingEntity entity = (LivingEntity)serverWorld.method_14190(entityId);
            return entity == null || !entity.method_5805();
         }
      });
   }

   public static void forceCheckAndSend(PlayerEntity player, LivingEntity entity) {
      if (!player.method_37908().method_8608()) {
         boolean hasGhostPressure = checkEntityHasGhostPressure(entity);
         UUID entityId = entity.method_5667();
         if (player instanceof ServerPlayerEntity serverPlayer) {
            GhostPressureDetectionPacket.sendToClient(serverPlayer, entityId, hasGhostPressure);
            lastDetectionResults.put(entityId, hasGhostPressure);
            LOGGER.debug("强制检查实体 {} 鬼压人buff状态：{}，通知玩家 {}", entity.method_5477().getString(), hasGhostPressure, player.method_5477().getString());
         }
      }
   }

   public static boolean getEntityGhostPressureStatus(LivingEntity entity) {
      return checkEntityHasGhostPressure(entity);
   }
}
