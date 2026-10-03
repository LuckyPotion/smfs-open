package com.xie.smfs.api;

import com.xie.smfs.manager.GhostSpawnManager;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;

public final class GhostSpawnAPI {
   private GhostSpawnAPI() {
   }

   public static void registerGhostSpawn(EntityType<?> entityType, String configKey, int minDay, int spawnChance, int spawnInterval, boolean nightOnly) {
      GhostSpawnManager.registerGhostSpawnConfig(entityType, configKey, minDay, spawnChance, spawnInterval, nightOnly);
   }

   public static void registerGhostSpawn(EntityType<?> entityType, String configKey, int minDay) {
      GhostSpawnManager.registerGhostSpawnConfig(entityType, configKey, minDay);
   }

   public static void unregisterGhostSpawn(EntityType<?> entityType) {
      GhostSpawnManager.unregisterGhostSpawnConfig(entityType);
   }

   public static boolean isGhostSpawnRegistered(EntityType<?> entityType) {
      return GhostSpawnManager.isGhostSpawnConfigRegistered(entityType);
   }

   public static int getRegisteredGhostSpawnCount() {
      return GhostSpawnManager.getRegisteredGhostSpawnConfigCount();
   }

   public static void lockGhostType(ServerWorld world, EntityType<?> entityType) {
      GhostSpawnManager.lockGhostType(world, entityType);
   }

   public static void unlockGhostType(ServerWorld world, EntityType<?> entityType) {
      GhostSpawnManager.unlockGhostType(world, entityType);
   }

   public static boolean isGhostLocked(EntityType<?> entityType) {
      return GhostSpawnManager.isGhostLocked(entityType);
   }

   public static int getSpawnInterval(EntityType<?> entityType) {
      return GhostSpawnManager.getSpawnInterval(entityType);
   }

   public static int getSpawnChance(EntityType<?> entityType) {
      return GhostSpawnManager.getSpawnChance(entityType);
   }

   public static int getGhostMinDay(EntityType<?> entityType) {
      return GhostSpawnManager.getMinDay(entityType);
   }

   public static boolean updateGhostSpawnConfig(EntityType<?> entityType, int spawnInterval, int spawnChance, int minDay, boolean nightOnly) {
      return GhostSpawnManager.updateGhostSpawnConfig(entityType, spawnInterval, spawnChance, minDay, nightOnly);
   }

   public static boolean reloadAllGhostSpawnConfigs() {
      return GhostSpawnManager.reloadAllGhostSpawnConfigs();
   }
}
