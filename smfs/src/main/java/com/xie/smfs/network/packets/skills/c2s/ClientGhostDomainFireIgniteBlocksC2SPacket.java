package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.GhostDomainManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostDomainFireIgniteBlocksC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "client_ghost_domain_fire_ignite_blocks");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostDomainFireIgniteBlocksC2SPacket");

   public ClientGhostDomainFireIgniteBlocksC2SPacket() {
   }

   public ClientGhostDomainFireIgniteBlocksC2SPacket(PacketByteBuf buf) {
   }

   public void write(PacketByteBuf buf) {
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> igniteBlocksAroundPlayer(player));
   }

   private static void igniteBlocksAroundPlayer(ServerPlayerEntity player) {
      int ghostFireLevel = GhostDomainManager.getEffectiveSkillLevel(player, GhostDomainManager.getGhostFireLevel(player));
      if (ghostFireLevel < 7) {
         player.sendMessage(Text.literal(GhostDomainManager.getInsufficientLevelMessage(player)), true);
      } else {
         BlockPos playerPos = player.getBlockPos();
         player.sendMessage(Text.literal("§a在表面生成火焰！"), true);
         int radius = 10;

         for (int x = playerPos.getX() - radius; x <= playerPos.getX() + radius; x++) {
            for (int z = playerPos.getZ() - radius; z <= playerPos.getZ() + radius; z++) {
               BlockPos groundPos = findGroundPosition(player, new BlockPos(x, playerPos.getY(), z));
               if (canPlaceFireAt(player, groundPos)) {
                  player.getWorld().setBlockState(groundPos, Blocks.FIRE.getDefaultState());
               }
            }
         }
      }
   }

   private static boolean canPlaceFireAt(ServerPlayerEntity player, BlockPos pos) {
      BlockState blockState = player.getWorld().getBlockState(pos);
      if (!blockState.isAir()) {
         return false;
      }

      BlockPos belowPos = pos.down();
      BlockState belowState = player.getWorld().getBlockState(belowPos);
      return belowState.isSolidBlock(player.getWorld(), belowPos);
   }

   private static BlockPos findGroundPosition(ServerPlayerEntity player, BlockPos startPos) {
      for (BlockPos currentPos = startPos; currentPos.getY() > player.getWorld().getBottomY(); currentPos = currentPos.down()) {
         BlockState currentState = player.getWorld().getBlockState(currentPos);
         BlockState belowState = player.getWorld().getBlockState(currentPos.down());
         if (currentState.isAir() && belowState.isSolidBlock(player.getWorld(), currentPos.down())) {
            return currentPos;
         }
      }

      return startPos;
   }

   private static boolean canIgniteBlock(BlockState blockState) {
      return !blockState.isAir() && blockState.getBlock() != Blocks.FIRE && blockState.isBurnable();
   }
}
