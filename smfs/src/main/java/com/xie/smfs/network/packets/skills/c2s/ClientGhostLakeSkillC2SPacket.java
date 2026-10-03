package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.block.GhostLakeBlock;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModFluids;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostLakeSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_lake_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostLakeSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostLakeSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            if (player != null && player.method_5805()) {
               World world = player.method_37908();
               HitResult hitResult = player.method_5745(20.0, 0.0F, false);
               if (hitResult.method_17783() == Type.field_1333) {
                  player.method_7353(Text.method_43470("§c请对准一个位置使用鬼湖技能"), true);
               } else {
                  BlockPos targetPos;
                  if (hitResult.method_17783() == Type.field_1332) {
                     BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                     BlockPos blockPos = blockHitResult.method_17777();
                     Direction side = blockHitResult.method_17780();
                     targetPos = blockPos.method_10093(side);
                  } else {
                     Vec3d pos = hitResult.method_17784();
                     targetPos = new BlockPos((int)pos.field_1352, (int)pos.field_1351, (int)pos.field_1350);
                  }

                  boolean hasGhostLake = false;

                  for (int x = -1; x <= 1; x++) {
                     for (int z = -1; z <= 1; z++) {
                        BlockPos lakePos = targetPos.method_10069(x, 0, z);
                        if (world.method_8320(lakePos).method_26204() instanceof GhostLakeBlock) {
                           hasGhostLake = true;
                           break;
                        }
                     }

                     if (hasGhostLake) {
                        break;
                     }
                  }

                  int placedBlocks = 0;
                  int removedBlocks = 0;
                  if (hasGhostLake) {
                     for (int x = -4; x <= 4; x++) {
                        for (int z = -4; z <= 4; z++) {
                           BlockPos lakePos = targetPos.method_10069(x, 0, z);
                           if (world.method_8320(lakePos).method_26204() instanceof GhostLakeBlock) {
                              world.method_8501(lakePos, Blocks.field_10124.method_9564());
                              removedBlocks++;
                           }
                        }
                     }
                  } else {
                     for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                           BlockPos lakePos = targetPos.method_10069(x, 0, z);
                           if (world.method_8320(lakePos).method_26215() || world.method_8320(lakePos).method_45474()) {
                              world.method_8501(lakePos, ModFluids.GHOST_LAKE_BLOCK.method_9564());
                              placedBlocks++;
                           }
                        }
                     }
                  }

                  player.method_17356(SoundEvents.field_15237, SoundCategory.field_15248, 1.0F, 1.0F);
                  if (!hasGhostLake && world.field_9229.method_43057() < 0.05F) {
                     spawnGhostSlave(world, targetPos, player);
                  }

                  PlayerEvents.balanceRevivalDegree(player);
               }
            } else {
               LOGGER.warn("玩家无效，无法执行鬼湖技能");
            }
         }
      });
   }

   private static void spawnGhostSlave(World world, BlockPos targetPos, ServerPlayerEntity player) {
      try {
         GhostSlaveEntity ghostSlave = new GhostSlaveEntity(ModEntities.GHOST_SLAVE, world);
         ghostSlave.method_5808(
            targetPos.method_10263() + 0.5 + (world.field_9229.method_43058() - 0.5) * 2.0,
            targetPos.method_10264() + 1.0,
            targetPos.method_10260() + 0.5 + (world.field_9229.method_43058() - 0.5) * 2.0,
            world.field_9229.method_43057() * 360.0F,
            0.0F
         );
         ghostSlave.method_6033(ghostSlave.method_6063());
         world.method_8649(ghostSlave);
         LOGGER.info("玩家 {} 使用鬼湖技能时触发5%概率，成功生成野生鬼奴", player.method_5477().getString());
      } catch (Exception e) {
         LOGGER.error("生成野生鬼奴时发生错误", e);
      }
   }
}
