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
            if (player != null && player.isAlive()) {
               World world = player.getWorld();
               HitResult hitResult = player.raycast(20.0, 0.0F, false);
               if (hitResult.getType() == Type.MISS) {
                  player.sendMessage(Text.literal("§c请对准一个位置使用鬼湖技能"), true);
               } else {
                  BlockPos targetPos;
                  if (hitResult.getType() == Type.BLOCK) {
                     BlockHitResult blockHitResult = (BlockHitResult)hitResult;
                     BlockPos blockPos = blockHitResult.getBlockPos();
                     Direction side = blockHitResult.getSide();
                     targetPos = blockPos.offset(side);
                  } else {
                     Vec3d pos = hitResult.getPos();
                     targetPos = new BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
                  }

                  boolean hasGhostLake = false;

                  for (int x = -1; x <= 1; x++) {
                     for (int z = -1; z <= 1; z++) {
                        BlockPos lakePos = targetPos.add(x, 0, z);
                        if (world.getBlockState(lakePos).getBlock() instanceof GhostLakeBlock) {
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
                           BlockPos lakePos = targetPos.add(x, 0, z);
                           if (world.getBlockState(lakePos).getBlock() instanceof GhostLakeBlock) {
                              world.setBlockState(lakePos, Blocks.AIR.getDefaultState());
                              removedBlocks++;
                           }
                        }
                     }
                  } else {
                     for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                           BlockPos lakePos = targetPos.add(x, 0, z);
                           if (world.getBlockState(lakePos).isAir() || world.getBlockState(lakePos).isReplaceable()) {
                              world.setBlockState(lakePos, ModFluids.GHOST_LAKE_BLOCK.getDefaultState());
                              placedBlocks++;
                           }
                        }
                     }
                  }

                  player.playSound(SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                  if (!hasGhostLake && world.random.nextFloat() < 0.05F) {
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
         ghostSlave.refreshPositionAndAngles(
            targetPos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 2.0,
            targetPos.getY() + 1.0,
            targetPos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 2.0,
            world.random.nextFloat() * 360.0F,
            0.0F
         );
         ghostSlave.setHealth(ghostSlave.getMaxHealth());
         world.spawnEntity(ghostSlave);
         LOGGER.info("玩家 {} 使用鬼湖技能时触发5%概率，成功生成野生鬼奴", player.getName().getString());
      } catch (Exception e) {
         LOGGER.error("生成野生鬼奴时发生错误", e);
      }
   }
}
