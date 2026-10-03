package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostLakeNSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_lake_n_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostLakeNSkillC2SPacket");
   private static final RegistryKey<World> GHOST_LAKE_FLAT_DIMENSION = RegistryKey.of(RegistryKeys.WORLD, new Identifier("smfs", "ghost_lake_flat"));

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostLakeNSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (player != null && player.isAlive()) {
            boolean isInGhostLakeDimension = player.getWorld().getRegistryKey().equals(GHOST_LAKE_FLAT_DIMENSION);
            ServerWorld targetWorld;
            if (isInGhostLakeDimension) {
               targetWorld = server.getWorld(World.OVERWORLD);
               String dimensionName = "主世界";
            } else {
               targetWorld = server.getWorld(GHOST_LAKE_FLAT_DIMENSION);
               String dimensionName = "鬼湖维度";
            }

            if (targetWorld != null) {
               double x = player.getX();
               double z = player.getZ();
               double y;
               if (isInGhostLakeDimension) {
                  y = findSafeYPosition(targetWorld, (int)x, (int)z);
                  createSafePlatform(targetWorld, new BlockPos((int)x, (int)y - 1, (int)z));
               } else {
                  y = 200.0;
               }

               player.teleport(targetWorld, x, y, z, player.getYaw(), player.getPitch());
               player.playSound(SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
               player.sendMessage(Text.literal("§c目标维度未加载"), true);
            }

            PlayerEvents.balanceRevivalDegree(player);
         } else {
            LOGGER.warn("玩家无效，无法执行鬼湖N键技能");
         }
      });
   }

   private static double findSafeYPosition(ServerWorld world, int x, int z) {
      int surfaceY = world.getTopY(Type.WORLD_SURFACE, x, z);
      if (surfaceY < 64) {
         surfaceY = 64;
      }

      return surfaceY + 2.0;
   }

   private static void createSafePlatform(ServerWorld world, BlockPos platformCenterPos) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            BlockPos currentPos = platformCenterPos.add(dx, 0, dz);
            BlockState currentState = world.getBlockState(currentPos);
            if (currentState.isAir() || currentState.isReplaceable()) {
               world.setBlockState(currentPos, Blocks.OBSIDIAN.getDefaultState());
            }
         }
      }

      for (int dy = 1; dy <= 2; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos airPos = platformCenterPos.add(dx, dy, dz);
               world.setBlockState(airPos, Blocks.AIR.getDefaultState());
            }
         }
      }
   }
}
