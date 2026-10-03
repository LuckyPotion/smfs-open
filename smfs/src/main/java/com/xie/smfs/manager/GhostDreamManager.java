package com.xie.smfs.manager;

import com.xie.smfs.Smfs;
import com.xie.smfs.network.ModNetwork;
import com.xie.smfs.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;

public class GhostDreamManager {
   private static final Map<UUID, GhostDreamManager.GhostDreamPlayerData> PLAYER_DATA_MAP = new HashMap<>();
   private static final long GAME_TIME_12H = 12000L;
   private static final long REAL_TIME_15MIN = 900000L;

   public static boolean hasTalkedToYangXiao(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.method_5667());
      return data != null && data.hasTalkedToYangXiao;
   }

   public static boolean isNaturalTrigger(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.method_5667());
      return data != null && data.isNaturalTrigger;
   }

   public static void markTalkedToYangXiao(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.method_5667());
      if (data != null) {
         data.hasTalkedToYangXiao = true;
      }
   }

   public static void resetYangXiaoTalkStatus(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.method_5667());
      if (data != null) {
         data.hasTalkedToYangXiao = false;
      }
   }

   public static void enterGhostDream(ServerPlayerEntity player) {
      enterGhostDream(player, false);
   }

   public static void enterGhostDream(ServerPlayerEntity player, boolean isNaturalTrigger) {
      Vec3d originalPos = player.method_19538();
      ServerWorld originalWorld = player.method_51469();
      GhostDreamManager.GhostDreamPlayerData data = new GhostDreamManager.GhostDreamPlayerData(originalPos, originalWorld, isNaturalTrigger);
      PLAYER_DATA_MAP.put(player.method_5667(), data);
      resetYangXiaoTalkStatus(player);
      ServerWorld ghostDreamWorld = player.method_5682().method_3847(Smfs.GHOST_DREAM_DIMENSION);
      if (ghostDreamWorld != null) {
         int x = (int)(player.method_23317() + (player.method_6051().method_43058() - 0.5) * 100.0);
         int z = (int)(player.method_23321() + (player.method_6051().method_43058() - 0.5) * 100.0);
         int minY = ghostDreamWorld.method_31607();
         int maxY = ghostDreamWorld.method_31600();
         BlockPos grassPos = findGrassBlockPosition(ghostDreamWorld, x, z, minY, maxY);
         BlockPos targetPos;
         if (grassPos != null) {
            targetPos = createSafePlatform(ghostDreamWorld, grassPos);
         } else {
            int surfaceY = ghostDreamWorld.method_8624(Type.field_13197, x, z);
            targetPos = createSafePlatform(ghostDreamWorld, new BlockPos(x, surfaceY, z));
         }

         player.method_14251(
            ghostDreamWorld,
            targetPos.method_10263() + 0.5,
            targetPos.method_10264(),
            targetPos.method_10260() + 0.5,
            player.method_36454(),
            player.method_36455()
         );
         player.method_7353(Text.method_43470("§c你进入了鬼梦之中...").method_27692(Formatting.field_1061), true);
         Smfs.LOGGER.debug("玩家 {} 进入了鬼梦维度", player.method_5477().getString());
         ModNetwork.sendGhostDreamTimeToClient(data.remainingGameTime, player);
      }
   }

   public static void tick(MinecraftServer server) {
      long currentTime = System.currentTimeMillis();

      for (Entry<UUID, GhostDreamManager.GhostDreamPlayerData> entry : PLAYER_DATA_MAP.entrySet()) {
         UUID playerId = entry.getKey();
         GhostDreamManager.GhostDreamPlayerData data = entry.getValue();
         long elapsedRealTime = currentTime - data.startTime;
         double progress = elapsedRealTime / 900000.0;
         data.remainingGameTime = 12000L - (long)(progress * 12000.0);
         ServerPlayerEntity player = server.method_3760().method_14602(playerId);
         if (player != null) {
            ModNetwork.sendGhostDreamTimeToClient(data.remainingGameTime, player);
         }

         if (data.remainingGameTime <= 0L) {
            if (player != null) {
               exitGhostDream(player);
            }

            PLAYER_DATA_MAP.remove(playerId);
         }
      }
   }

   private static BlockPos findGrassBlockPosition(ServerWorld world, int x, int z, int minY, int maxY) {
      for (int y = minY; y < maxY; y++) {
         BlockPos pos = new BlockPos(x, y, z);
         if (isGrassBlock(world, pos)
            && y + 2 < maxY
            && world.method_8320(new BlockPos(x, y + 1, z)).method_26215()
            && world.method_8320(new BlockPos(x, y + 2, z)).method_26215()) {
            return pos;
         }
      }

      int searchRadius = 50;

      for (int radius = 1; radius <= searchRadius; radius++) {
         for (int dx = -radius; dx <= radius; dx++) {
            int newX = x + dx;
            int newZ = z + radius;

            for (int y = minY; y < maxY; y++) {
               BlockPos pos = new BlockPos(newX, y, newZ);
               if (isGrassBlock(world, pos)
                  && y + 2 < maxY
                  && world.method_8320(new BlockPos(newX, y + 1, newZ)).method_26215()
                  && world.method_8320(new BlockPos(newX, y + 2, newZ)).method_26215()) {
                  return pos;
               }
            }

            newZ = z - radius;

            for (int y = minY; y < maxY; y++) {
               BlockPos pos = new BlockPos(newX, y, newZ);
               if (isGrassBlock(world, pos)
                  && y + 2 < maxY
                  && world.method_8320(new BlockPos(newX, y + 1, newZ)).method_26215()
                  && world.method_8320(new BlockPos(newX, y + 2, newZ)).method_26215()) {
                  return pos;
               }
            }
         }

         for (int dz = -radius + 1; dz <= radius - 1; dz++) {
            int newX = x + radius;
            int newZ = z + dz;

            for (int y = minY; y < maxY; y++) {
               BlockPos pos = new BlockPos(newX, y, newZ);
               if (isGrassBlock(world, pos)
                  && y + 2 < maxY
                  && world.method_8320(new BlockPos(newX, y + 1, newZ)).method_26215()
                  && world.method_8320(new BlockPos(newX, y + 2, newZ)).method_26215()) {
                  return pos;
               }
            }

            newX = x - radius;

            for (int y = minY; y < maxY; y++) {
               BlockPos pos = new BlockPos(newX, y, newZ);
               if (isGrassBlock(world, pos)
                  && y + 2 < maxY
                  && world.method_8320(new BlockPos(newX, y + 1, newZ)).method_26215()
                  && world.method_8320(new BlockPos(newX, y + 2, newZ)).method_26215()) {
                  return pos;
               }
            }
         }
      }

      return null;
   }

   private static BlockPos createSafePlatform(ServerWorld world, BlockPos pos) {
      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            BlockPos currentPos = pos.method_10069(x, 0, z);
            world.method_8501(currentPos, Blocks.field_10540.method_9564());
         }
      }

      for (int y = 1; y <= 2; y++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               BlockPos airPos = pos.method_10069(x, y, z);
               world.method_8501(airPos, Blocks.field_10124.method_9564());
            }
         }
      }

      return new BlockPos(pos.method_10263(), pos.method_10264() + 1, pos.method_10260());
   }

   private static boolean isGrassBlock(ServerWorld world, BlockPos pos) {
      Block block = world.method_8320(pos).method_26204();
      return block instanceof GrassBlock;
   }

   public static void exitGhostDream(ServerPlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.remove(player.method_5667());
      if (data != null) {
         ServerWorld originalWorld = player.method_5682().method_3847(data.originalWorldKey.toRegistryKey());
         if (originalWorld != null) {
            player.method_14251(
               originalWorld,
               data.originalPos.field_1352,
               data.originalPos.field_1351,
               data.originalPos.field_1350,
               player.method_36454(),
               player.method_36455()
            );
            player.method_7353(Text.method_43470("§a你从鬼梦中醒来...").method_27692(Formatting.field_1060), true);
            Smfs.LOGGER.debug("玩家 {} 从鬼梦维度返回", player.method_5477().getString());
            if (data.isNaturalTrigger) {
               if (data.hasTalkedToYangXiao) {
                  ItemStack ghostDreamItem = new ItemStack(ModItems.GHOST_DREAM);
                  if (!player.method_31548().method_7394(ghostDreamItem)) {
                     player.method_7328(ghostDreamItem, false);
                  }

                  player.method_7353(Text.method_43470("§a你获得了鬼梦驾驭物品！").method_27692(Formatting.field_1060), true);
                  Smfs.LOGGER.debug("玩家 {} 与杨孝对话过，获得了鬼梦驾驭物品", player.method_5477().getString());
               } else {
                  Smfs.LOGGER.debug("玩家 {} 未与杨孝对话，无法驾驭鬼梦", player.method_5477().getString());
               }
            }

            ModNetwork.sendGhostDreamTimeToClient(0L, player);
         }
      }
   }

   public static void forceExit(ServerPlayerEntity player) {
      exitGhostDream(player);
   }

   public static boolean isInGhostDream(PlayerEntity player) {
      return PLAYER_DATA_MAP.containsKey(player.method_5667());
   }

   public static GhostDreamManager.GhostDreamPlayerData getPlayerData(PlayerEntity player) {
      return PLAYER_DATA_MAP.get(player.method_5667());
   }

   public static int getRemainingMinutes(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.method_5667());
      if (data == null) {
         return 0;
      }

      long ticksRemaining = data.remainingGameTime;
      return (int)(ticksRemaining / 100L);
   }

   public static String getFormattedTime(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.method_5667());
      if (data == null) {
         return "08:00";
      }

      long totalTicks = 12000L;
      long elapsedTicks = totalTicks - data.remainingGameTime;
      long gameTimeOfDay = (20000L + elapsedTicks) % 24000L;
      int hours = (int)(gameTimeOfDay / 1000L);
      int minutes = (int)(gameTimeOfDay % 1000L / 16.67);
      if (hours >= 24) {
         hours -= 24;
      }

      return String.format("%02d:%02d", hours, minutes);
   }

   public static long getRemainingGameTime(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.method_5667());
      return data == null ? 0L : data.remainingGameTime;
   }

   public static long getTotalGameTime() {
      return 12000L;
   }

   public static void onPlayerDisconnect(ServerPlayerEntity player) {
      PLAYER_DATA_MAP.remove(player.method_5667());
      ModNetwork.sendGhostDreamTimeToClient(0L, player);
   }

   public static void onPlayerDeath(ServerPlayerEntity player) {
      if (PLAYER_DATA_MAP.containsKey(player.method_5667())) {
         PLAYER_DATA_MAP.remove(player.method_5667());
         ModNetwork.sendGhostDreamTimeToClient(0L, player);
         Smfs.LOGGER.debug("玩家 {} 在鬼梦中死亡，已清空计时数据", player.method_5477().getString());
      }
   }

   public static void checkPlayerDimension(ServerPlayerEntity player) {
      if (isInGhostDream(player) && player.method_37908().method_27983() != Smfs.GHOST_DREAM_DIMENSION) {
         PLAYER_DATA_MAP.remove(player.method_5667());
         ModNetwork.sendGhostDreamTimeToClient(0L, player);
         Smfs.LOGGER.debug("玩家 {} 离开了鬼梦维度，已清空计时数据", player.method_5477().getString());
      }
   }

   public static class GhostDreamPlayerData {
      public Vec3d originalPos;
      public GhostDreamManager.RegistryKeyWrapper originalWorldKey;
      public long startTime;
      public long remainingGameTime;
      public boolean isNaturalTrigger;
      public boolean hasTalkedToYangXiao = false;

      public GhostDreamPlayerData(Vec3d pos, World world) {
         this(pos, world, false);
      }

      public GhostDreamPlayerData(Vec3d pos, World world, boolean isNaturalTrigger) {
         this.originalPos = pos;
         this.originalWorldKey = new GhostDreamManager.RegistryKeyWrapper(world.method_27983());
         this.startTime = System.currentTimeMillis();
         this.remainingGameTime = 12000L;
         this.isNaturalTrigger = isNaturalTrigger;
      }
   }

   public static class RegistryKeyWrapper {
      private final String namespace;
      private final String path;

      public RegistryKeyWrapper(RegistryKey<World> key) {
         this.namespace = key.method_29177().method_12836();
         this.path = key.method_29177().method_12832();
      }

      public RegistryKey<World> toRegistryKey() {
         return RegistryKey.method_29179(RegistryKeys.field_41223, new Identifier(this.namespace, this.path));
      }
   }
}
