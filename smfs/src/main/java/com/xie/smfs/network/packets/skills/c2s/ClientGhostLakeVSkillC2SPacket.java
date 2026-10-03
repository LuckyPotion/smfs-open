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
            if (player != null && player.method_5805()) {
               World world = player.method_37908();
               BlockPos playerPos = player.method_24515();
               List<BlockPos> ghostLakePositions = new ArrayList<>();
               int searchRadius = 32;

               for (int x = -searchRadius; x <= searchRadius; x++) {
                  for (int y = -searchRadius; y <= searchRadius; y++) {
                     for (int z = -searchRadius; z <= searchRadius; z++) {
                        BlockPos checkPos = playerPos.method_10069(x, y, z);
                        if (world.method_8320(checkPos).method_26204() instanceof GhostLakeBlock
                           && !checkPos.equals(playerPos)
                           && !checkPos.equals(playerPos.method_10084())) {
                           ghostLakePositions.add(checkPos);
                        }
                     }
                  }
               }

               if (!ghostLakePositions.isEmpty()) {
                  BlockPos targetPos = null;
                  double maxDistance = 0.0;

                  for (BlockPos pos : ghostLakePositions) {
                     double distance = playerPos.method_10262(pos);
                     if (distance > maxDistance) {
                        maxDistance = distance;
                        targetPos = pos;
                     }
                  }

                  if (targetPos != null && world instanceof ServerWorld serverWorld) {
                     player.method_14251(
                        serverWorld,
                        targetPos.method_10263() + 0.5,
                        targetPos.method_10264() + 1,
                        targetPos.method_10260() + 0.5,
                        player.method_36454(),
                        player.method_36455()
                     );
                     player.method_17356(SoundEvents.field_15237, SoundCategory.field_15248, 1.0F, 1.0F);
                  }
               } else {
                  LOGGER.warn("玩家 {} 执行鬼湖V键技能，但周围没有其他鬼湖方块", player.method_5477().getString());
                  player.method_7353(Text.method_43470("§c周围没有其他鬼湖方块"), true);
               }

               PlayerEvents.balanceRevivalDegree(player);
            } else {
               LOGGER.warn("玩家无效，无法执行鬼湖V键技能");
            }
         }
      );
   }
}
