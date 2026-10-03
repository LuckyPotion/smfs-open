package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.block.GhostLakeBlock;
import com.xie.smfs.common.events.PlayerEvents;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostLakeVSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_lake_v_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostLakeVSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostLakeVSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(
         () -> {
            if (player != null && player.isAlive()) {
               World world = player.getWorld();
               BlockPos playerPos = player.getBlockPos();
               List<BlockPos> ghostLakePositions = new ArrayList<>();
               int searchRadius = 32;

               for (int x = -searchRadius; x <= searchRadius; x++) {
                  for (int y = -searchRadius; y <= searchRadius; y++) {
                     for (int z = -searchRadius; z <= searchRadius; z++) {
                        BlockPos checkPos = playerPos.add(x, y, z);
                        if (world.getBlockState(checkPos).getBlock() instanceof GhostLakeBlock
                           && !checkPos.equals(playerPos)
                           && !checkPos.equals(playerPos.up())) {
                           ghostLakePositions.add(checkPos);
                        }
                     }
                  }
               }

               if (!ghostLakePositions.isEmpty()) {
                  BlockPos targetPos = null;
                  double maxDistance = 0.0;

                  for (BlockPos pos : ghostLakePositions) {
                     double distance = playerPos.getSquaredDistance(pos);
                     if (distance > maxDistance) {
                        maxDistance = distance;
                        targetPos = pos;
                     }
                  }

                  if (targetPos != null && world instanceof ServerWorld serverWorld) {
                     player.teleport(serverWorld, targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5, player.getYaw(), player.getPitch());
                     player.playSound(SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                  }
               } else {
                  LOGGER.warn("玩家 {} 执行鬼湖V键技能，但周围没有其他鬼湖方块", player.getName().getString());
                  player.sendMessage(Text.literal("§c周围没有其他鬼湖方块"), true);
               }

               PlayerEvents.balanceRevivalDegree(player);
            } else {
               LOGGER.warn("玩家无效，无法执行鬼湖V键技能");
            }
         }
      );
   }
}
