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
         player.method_7353(Text.method_43470(GhostDomainManager.getInsufficientLevelMessage(player)), true);
      } else {
         BlockPos playerPos = player.method_24515();
         player.method_7353(Text.method_43470("§a在表面生成火焰！"), true);
         int radius = 10;

         for (int x = playerPos.method_10263() - radius; x <= playerPos.method_10263() + radius; x++) {
            for (int z = playerPos.method_10260() - radius; z <= playerPos.method_10260() + radius; z++) {
               BlockPos groundPos = findGroundPosition(player, new BlockPos(x, playerPos.method_10264(), z));
               if (canPlaceFireAt(player, groundPos)) {
                  player.method_37908().method_8501(groundPos, Blocks.field_10036.method_9564());
               }
            }
         }
      }
   }

   private static boolean canPlaceFireAt(ServerPlayerEntity player, BlockPos pos) {
      BlockState blockState = player.method_37908().method_8320(pos);
      if (!blockState.method_26215()) {
         return false;
      }

      BlockPos belowPos = pos.method_10074();
      BlockState belowState = player.method_37908().method_8320(belowPos);
      return belowState.method_26212(player.method_37908(), belowPos);
   }

   private static BlockPos findGroundPosition(ServerPlayerEntity player, BlockPos startPos) {
      for (BlockPos currentPos = startPos; currentPos.method_10264() > player.method_37908().method_31607(); currentPos = currentPos.method_10074()) {
         BlockState currentState = player.method_37908().method_8320(currentPos);
         BlockState belowState = player.method_37908().method_8320(currentPos.method_10074());
         if (currentState.method_26215() && belowState.method_26212(player.method_37908(), currentPos.method_10074())) {
            return currentPos;
         }
      }

      return startPos;
   }

   private static boolean canIgniteBlock(BlockState blockState) {
      return !blockState.method_26215() && blockState.method_26204() != Blocks.field_10036 && blockState.method_50011();
   }
}
