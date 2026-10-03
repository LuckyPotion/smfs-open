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
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.getUuid());
      return data != null && data.hasTalkedToYangXiao;
   }

   public static boolean isNaturalTrigger(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.getUuid());
      return data != null && data.isNaturalTrigger;
   }

   public static void markTalkedToYangXiao(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.getUuid());
      if (data != null) {
         data.hasTalkedToYangXiao = true;
      }
   }

   public static void resetYangXiaoTalkStatus(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.getUuid());
      if (data != null) {
         data.hasTalkedToYangXiao = false;
      }
   }

   public static void enterGhostDream(ServerPlayerEntity player) {
      enterGhostDream(player, false);
   }

   public static void enterGhostDream(ServerPlayerEntity player, boolean isNaturalTrigger) {
      Vec3d originalPos = player.getPos();
      ServerWorld originalWorld = player.getServerWorld();
      GhostDreamManager.GhostDreamPlayerData data = new GhostDreamManager.GhostDreamPlayerData(originalPos, originalWorld, isNaturalTrigger);
      PLAYER_DATA_MAP.put(player.getUuid(), data);
      resetYangXiaoTalkStatus(player);
      ServerWorld ghostDreamWorld = player.getServer().getWorld(Smfs.GHOST_DREAM_DIMENSION);
      if (ghostDreamWorld != null) {
         int x = (int)(player.getX() + (player.getRandom().nextDouble() - 0.5) * 100.0);
         int z = (int)(player.getZ() + (player.getRandom().nextDouble() - 0.5) * 100.0);
         int minY = ghostDreamWorld.getBottomY();
         int maxY = ghostDreamWorld.getTopY();
         BlockPos grassPos = findGrassBlockPosition(ghostDreamWorld, x, z, minY, maxY);
         BlockPos targetPos;
         if (grassPos != null) {
            targetPos = createSafePlatform(ghostDreamWorld, grassPos);
         } else {
            int surfaceY = ghostDreamWorld.getTopY(Type.MOTION_BLOCKING, x, z);
            targetPos = createSafePlatform(ghostDreamWorld, new BlockPos(x, surfaceY, z));
         }

         player.teleport(ghostDreamWorld, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, player.getYaw(), player.getPitch());
         player.sendMessage(Text.literal("§c你进入了鬼梦之中...").formatted(Formatting.RED), true);
         Smfs.LOGGER.debug("玩家 {} 进入了鬼梦维度", player.getName().getString());
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
         ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
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
            && world.getBlockState(new BlockPos(x, y + 1, z)).isAir()
            && world.getBlockState(new BlockPos(x, y + 2, z)).isAir()) {
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
                  && world.getBlockState(new BlockPos(newX, y + 1, newZ)).isAir()
                  && world.getBlockState(new BlockPos(newX, y + 2, newZ)).isAir()) {
                  return pos;
               }
            }

            newZ = z - radius;

            for (int y = minY; y < maxY; y++) {
               BlockPos pos = new BlockPos(newX, y, newZ);
               if (isGrassBlock(world, pos)
                  && y + 2 < maxY
                  && world.getBlockState(new BlockPos(newX, y + 1, newZ)).isAir()
                  && world.getBlockState(new BlockPos(newX, y + 2, newZ)).isAir()) {
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
                  && world.getBlockState(new BlockPos(newX, y + 1, newZ)).isAir()
                  && world.getBlockState(new BlockPos(newX, y + 2, newZ)).isAir()) {
                  return pos;
               }
            }

            newX = x - radius;

            for (int y = minY; y < maxY; y++) {
               BlockPos pos = new BlockPos(newX, y, newZ);
               if (isGrassBlock(world, pos)
                  && y + 2 < maxY
                  && world.getBlockState(new BlockPos(newX, y + 1, newZ)).isAir()
                  && world.getBlockState(new BlockPos(newX, y + 2, newZ)).isAir()) {
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
            BlockPos currentPos = pos.add(x, 0, z);
            world.setBlockState(currentPos, Blocks.OBSIDIAN.getDefaultState());
         }
      }

      for (int y = 1; y <= 2; y++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               BlockPos airPos = pos.add(x, y, z);
               world.setBlockState(airPos, Blocks.AIR.getDefaultState());
            }
         }
      }

      return new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
   }

   private static boolean isGrassBlock(ServerWorld world, BlockPos pos) {
      Block block = world.getBlockState(pos).getBlock();
      return block instanceof GrassBlock;
   }

   public static void exitGhostDream(ServerPlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.remove(player.getUuid());
      if (data != null) {
         ServerWorld originalWorld = player.getServer().getWorld(data.originalWorldKey.toRegistryKey());
         if (originalWorld != null) {
            player.teleport(originalWorld, data.originalPos.x, data.originalPos.y, data.originalPos.z, player.getYaw(), player.getPitch());
            player.sendMessage(Text.literal("§a你从鬼梦中醒来...").formatted(Formatting.GREEN), true);
            Smfs.LOGGER.debug("玩家 {} 从鬼梦维度返回", player.getName().getString());
            if (data.isNaturalTrigger) {
               if (data.hasTalkedToYangXiao) {
                  ItemStack ghostDreamItem = new ItemStack(ModItems.GHOST_DREAM);
                  if (!player.getInventory().insertStack(ghostDreamItem)) {
                     player.dropItem(ghostDreamItem, false);
                  }

                  player.sendMessage(Text.literal("§a你获得了鬼梦驾驭物品！").formatted(Formatting.GREEN), true);
                  Smfs.LOGGER.debug("玩家 {} 与杨孝对话过，获得了鬼梦驾驭物品", player.getName().getString());
               } else {
                  Smfs.LOGGER.debug("玩家 {} 未与杨孝对话，无法驾驭鬼梦", player.getName().getString());
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
      return PLAYER_DATA_MAP.containsKey(player.getUuid());
   }

   public static GhostDreamManager.GhostDreamPlayerData getPlayerData(PlayerEntity player) {
      return PLAYER_DATA_MAP.get(player.getUuid());
   }

   public static int getRemainingMinutes(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.getUuid());
      if (data == null) {
         return 0;
      }

      long ticksRemaining = data.remainingGameTime;
      return (int)(ticksRemaining / 100L);
   }

   public static String getFormattedTime(PlayerEntity player) {
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.getUuid());
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
      GhostDreamManager.GhostDreamPlayerData data = PLAYER_DATA_MAP.get(player.getUuid());
      return data == null ? 0L : data.remainingGameTime;
   }

   public static long getTotalGameTime() {
      return 12000L;
   }

   public static void onPlayerDisconnect(ServerPlayerEntity player) {
      PLAYER_DATA_MAP.remove(player.getUuid());
      ModNetwork.sendGhostDreamTimeToClient(0L, player);
   }

   public static void onPlayerDeath(ServerPlayerEntity player) {
      if (PLAYER_DATA_MAP.containsKey(player.getUuid())) {
         PLAYER_DATA_MAP.remove(player.getUuid());
         ModNetwork.sendGhostDreamTimeToClient(0L, player);
         Smfs.LOGGER.debug("玩家 {} 在鬼梦中死亡，已清空计时数据", player.getName().getString());
      }
   }

   public static void checkPlayerDimension(ServerPlayerEntity player) {
      if (isInGhostDream(player) && player.getWorld().getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
         PLAYER_DATA_MAP.remove(player.getUuid());
         ModNetwork.sendGhostDreamTimeToClient(0L, player);
         Smfs.LOGGER.debug("玩家 {} 离开了鬼梦维度，已清空计时数据", player.getName().getString());
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
         this.originalWorldKey = new GhostDreamManager.RegistryKeyWrapper(world.getRegistryKey());
         this.startTime = System.currentTimeMillis();
         this.remainingGameTime = 12000L;
         this.isNaturalTrigger = isNaturalTrigger;
      }
   }

   public static class RegistryKeyWrapper {
      private final String namespace;
      private final String path;

      public RegistryKeyWrapper(RegistryKey<World> key) {
         this.namespace = key.getValue().getNamespace();
         this.path = key.getValue().getPath();
      }

      public RegistryKey<World> toRegistryKey() {
         return RegistryKey.of(RegistryKeys.WORLD, new Identifier(this.namespace, this.path));
      }
   }
}
