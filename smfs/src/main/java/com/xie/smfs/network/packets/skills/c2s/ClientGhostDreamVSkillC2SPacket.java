package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.Smfs;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDreamManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostDreamVSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_dream_v_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostDreamVSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostDreamVSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (player != null && player.method_5805()) {
            World world = player.method_37908();
            if (world.method_27983() != Smfs.GHOST_DREAM_DIMENSION) {
               teleportToGhostDream(player);
            } else {
               teleportFromGhostDream(player);
            }

            PlayerEvents.balanceRevivalDegree(player);
         } else {
            LOGGER.warn("玩家无效，无法执行鬼梦V键技能");
         }
      });
   }

   private static void teleportToGhostDream(ServerPlayerEntity player) {
      GhostDreamManager.enterGhostDream(player, false);
      LOGGER.debug("玩家 {} 通过V技能进入了鬼梦维度", player.method_5477().getString());
   }

   private static void teleportFromGhostDream(ServerPlayerEntity player) {
      GhostDreamManager.exitGhostDream(player);
      LOGGER.debug("玩家 {} 通过V技能离开了鬼梦维度", player.method_5477().getString());
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
}
