package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.block.GhostLakeBlock;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.entity.master.YangJianEntity;
import com.xie.smfs.util.TargetingUtil;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostLakeGSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_lake_g_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostLakeGSkillC2SPacket");
   private static final RegistryKey<World> GHOST_LAKE_FLAT_DIMENSION = RegistryKey.of(RegistryKeys.WORLD, new Identifier("smfs", "ghost_lake_flat"));

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostLakeGSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(
         () -> {
            if (player != null && player.isAlive()) {
               LivingEntity targetEntity = TargetingUtil.findEntityInLookDirectionWithOcclusion(
                  player, 32.0, 0.866, e -> !(e instanceof YangJianEntity) && !(e instanceof LuoQianGhostEntity)
               );
               if (targetEntity != null && targetEntity.isAlive()) {
                  boolean targetInGhostLake = isEntityInGhostLake(targetEntity);
                  if (!targetInGhostLake && !targetEntity.getWorld().getRegistryKey().equals(GHOST_LAKE_FLAT_DIMENSION)) {
                     player.sendMessage(Text.literal("§c目标需要在鬼湖中"), true);
                     return;
                  }

                  if (!canTeleportTarget(player, targetEntity)) {
                     player.sendMessage(Text.literal("§c目标灵异强度过高，无法传送"), true);
                     return;
                  }

                  boolean targetInGhostLakeDimension = targetEntity.getWorld().getRegistryKey().equals(GHOST_LAKE_FLAT_DIMENSION);
                  ServerWorld targetWorld;
                  String dimensionName;
                  double y;
                  if (targetInGhostLakeDimension) {
                     targetWorld = server.getWorld(World.OVERWORLD);
                     dimensionName = "主世界";
                     y = findSafeYPosition(targetWorld, (int)targetEntity.getX(), (int)targetEntity.getZ());
                     createSafePlatform(targetWorld, new BlockPos((int)targetEntity.getX(), (int)y - 1, (int)targetEntity.getZ()));
                  } else {
                     targetWorld = server.getWorld(GHOST_LAKE_FLAT_DIMENSION);
                     dimensionName = "鬼湖维度";
                     y = 200.0;
                  }

                  if (targetWorld != null) {
                     double x = targetEntity.getX();
                     double z = targetEntity.getZ();
                     if (targetEntity instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)targetEntity).teleport(targetWorld, x, y, z, targetEntity.getYaw(), targetEntity.getPitch());
                     } else {
                        Set<PositionFlag> flags = new HashSet<>();
                        flags.add(PositionFlag.X);
                        flags.add(PositionFlag.Y);
                        flags.add(PositionFlag.Z);
                        flags.add(PositionFlag.X_ROT);
                        flags.add(PositionFlag.Y_ROT);
                        targetEntity.teleport(targetWorld, x, y, z, flags, targetEntity.getYaw(), targetEntity.getPitch());
                     }

                     player.sendMessage(Text.literal("§a已将" + targetEntity.getName().getString() + "传送至" + dimensionName), true);
                  } else {
                     player.sendMessage(Text.literal("§c目标维度未加载"), true);
                  }
               } else {
                  player.sendMessage(Text.literal("§c请对准一个实体使用此技能"), true);
               }

               PlayerEvents.balanceRevivalDegree(player);
            } else {
               LOGGER.warn("玩家无效，无法执行鬼湖G键技能");
            }
         }
      );
   }

   private static boolean canTeleportTarget(ServerPlayerEntity player, Entity targetEntity) {
      double playerSpirit = getPlayerSpiritStrength(player);
      if (!(targetEntity instanceof GhostEntity) && !(targetEntity instanceof PlayerEntity) && !(targetEntity instanceof GhostMasterEntity)) {
         return true;
      }

      double targetSpirit = getEntitySpiritStrength(targetEntity);
      return targetSpirit < playerSpirit;
   }

   private static double getPlayerSpiritStrength(ServerPlayerEntity player) {
      NbtCompound spiritData = PlayerEvents.getSpiritAttributes(player);
      return spiritData.contains("currentSpirit") ? spiritData.getDouble("currentSpirit") : 0.0;
   }

   private static double getEntitySpiritStrength(Entity entity) {
      if (entity instanceof GhostEntity) {
         return ((GhostEntity)entity).getSpiritualStrength();
      } else if (entity instanceof GhostMasterEntity) {
         return ((GhostMasterEntity)entity).getSpiritualStrength();
      } else {
         return entity instanceof ServerPlayerEntity ? getPlayerSpiritStrength((ServerPlayerEntity)entity) : 0.0;
      }
   }

   private static boolean isEntityInGhostLake(Entity entity) {
      if (entity != null && entity.isAlive()) {
         World world = entity.getWorld();
         Box boundingBox = entity.getBoundingBox();

         for (double x = boundingBox.minX; x <= boundingBox.maxX; x++) {
            for (double y = boundingBox.minY; y <= boundingBox.maxY; y += 0.5) {
               for (double z = boundingBox.minZ; z <= boundingBox.maxZ; z++) {
                  BlockPos pos = BlockPos.ofFloored(x, y, z);
                  BlockState blockState = world.getBlockState(pos);
                  if (blockState.getBlock() instanceof GhostLakeBlock) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
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
