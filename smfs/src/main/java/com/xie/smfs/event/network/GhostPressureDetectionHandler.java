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
         if (world.getTime() % 10L == 0L) {
            detectGhostPressureEffects(world);
         }
      });
   }

   private static void detectGhostPressureEffects(World world) {
      if (!world.isClient()) {
         MinecraftServer server = world.getServer();
         if (server != null) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
               Box detectionBox = new Box(player.getBlockPos()).expand(32.0);

               for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, detectionBox, e -> true)) {
                  if (entity != player) {
                     boolean hasGhostPressure = checkEntityHasGhostPressure(entity);
                     UUID entityId = entity.getUuid();
                     boolean lastResult = lastDetectionResults.getOrDefault(entityId, false);
                     if (hasGhostPressure != lastResult) {
                        GhostPressureDetectionPacket.sendToClient(player, entityId, hasGhostPressure);
                        lastDetectionResults.put(entityId, hasGhostPressure);
                        if (hasGhostPressure) {
                           LOGGER.debug("服务端检测到实体 {} 有鬼压人buff，通知玩家 {}", entity.getName().getString(), player.getName().getString());
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
      if (entity.hasStatusEffect(ModEffects.GHOST_PRESSURE)) {
         return true;
      }

      if (entity.getStatusEffect(ModEffects.GHOST_PRESSURE) != null) {
         return true;
      }

      for (StatusEffectInstance effectInstance : entity.getStatusEffects()) {
         if (effectInstance.getEffectType() == ModEffects.GHOST_PRESSURE) {
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
            LivingEntity entity = (LivingEntity)serverWorld.getEntity(entityId);
            return entity == null || !entity.isAlive();
         }
      });
   }

   public static void forceCheckAndSend(PlayerEntity player, LivingEntity entity) {
      if (!player.getWorld().isClient()) {
         boolean hasGhostPressure = checkEntityHasGhostPressure(entity);
         UUID entityId = entity.getUuid();
         if (player instanceof ServerPlayerEntity serverPlayer) {
            GhostPressureDetectionPacket.sendToClient(serverPlayer, entityId, hasGhostPressure);
            lastDetectionResults.put(entityId, hasGhostPressure);
            LOGGER.debug("强制检查实体 {} 鬼压人buff状态：{}，通知玩家 {}", entity.getName().getString(), hasGhostPressure, player.getName().getString());
         }
      }
   }

   public static boolean getEntityGhostPressureStatus(LivingEntity entity) {
      return checkEntityHasGhostPressure(entity);
   }
}
