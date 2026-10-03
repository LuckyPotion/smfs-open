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
   private static final RegistryKey<World> GHOST_LAKE_FLAT_DIMENSION = RegistryKey.method_29179(
      RegistryKeys.field_41223, new Identifier("smfs", "ghost_lake_flat")
   );

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostLakeGSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(
         () -> {
            if (player != null && player.method_5805()) {
               LivingEntity targetEntity = TargetingUtil.findEntityInLookDirectionWithOcclusion(
                  player, 32.0, 0.866, e -> !(e instanceof YangJianEntity) && !(e instanceof LuoQianGhostEntity)
               );
               if (targetEntity != null && targetEntity.method_5805()) {
                  boolean targetInGhostLake = isEntityInGhostLake(targetEntity);
                  if (!targetInGhostLake && !targetEntity.method_37908().method_27983().equals(GHOST_LAKE_FLAT_DIMENSION)) {
                     player.method_7353(Text.method_43470("§c目标需要在鬼湖中"), true);
                     return;
                  }

                  if (!canTeleportTarget(player, targetEntity)) {
                     player.method_7353(Text.method_43470("§c目标灵异强度过高，无法传送"), true);
                     return;
                  }

                  boolean targetInGhostLakeDimension = targetEntity.method_37908().method_27983().equals(GHOST_LAKE_FLAT_DIMENSION);
                  ServerWorld targetWorld;
                  String dimensionName;
                  double y;
                  if (targetInGhostLakeDimension) {
                     targetWorld = server.method_3847(World.field_25179);
                     dimensionName = "主世界";
                     y = findSafeYPosition(targetWorld, (int)targetEntity.method_23317(), (int)targetEntity.method_23321());
                     createSafePlatform(targetWorld, new BlockPos((int)targetEntity.method_23317(), (int)y - 1, (int)targetEntity.method_23321()));
                  } else {
                     targetWorld = server.method_3847(GHOST_LAKE_FLAT_DIMENSION);
                     dimensionName = "鬼湖维度";
                     y = 200.0;
                  }

                  if (targetWorld != null) {
                     double x = targetEntity.method_23317();
                     double z = targetEntity.method_23321();
                     if (targetEntity instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)targetEntity).method_14251(targetWorld, x, y, z, targetEntity.method_36454(), targetEntity.method_36455());
                     } else {
                        Set<PositionFlag> flags = new HashSet<>();
                        flags.add(PositionFlag.field_12400);
                        flags.add(PositionFlag.field_12398);
                        flags.add(PositionFlag.field_12403);
                        flags.add(PositionFlag.field_12397);
                        flags.add(PositionFlag.field_12401);
                        targetEntity.method_48105(targetWorld, x, y, z, flags, targetEntity.method_36454(), targetEntity.method_36455());
                     }

                     player.method_7353(Text.method_43470("§a已将" + targetEntity.method_5477().getString() + "传送至" + dimensionName), true);
                  } else {
                     player.method_7353(Text.method_43470("§c目标维度未加载"), true);
                  }
               } else {
                  player.method_7353(Text.method_43470("§c请对准一个实体使用此技能"), true);
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
      return spiritData.method_10545("currentSpirit") ? spiritData.method_10574("currentSpirit") : 0.0;
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
      if (entity != null && entity.method_5805()) {
         World world = entity.method_37908();
         Box boundingBox = entity.method_5829();

         for (double x = boundingBox.field_1323; x <= boundingBox.field_1320; x++) {
            for (double y = boundingBox.field_1322; y <= boundingBox.field_1325; y += 0.5) {
               for (double z = boundingBox.field_1321; z <= boundingBox.field_1324; z++) {
                  BlockPos pos = BlockPos.method_49637(x, y, z);
                  BlockState blockState = world.method_8320(pos);
                  if (blockState.method_26204() instanceof GhostLakeBlock) {
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
      int surfaceY = world.method_8624(Type.field_13202, x, z);
      if (surfaceY < 64) {
         surfaceY = 64;
      }

      return surfaceY + 2.0;
   }

   private static void createSafePlatform(ServerWorld world, BlockPos platformCenterPos) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            BlockPos currentPos = platformCenterPos.method_10069(dx, 0, dz);
            BlockState currentState = world.method_8320(currentPos);
            if (currentState.method_26215() || currentState.method_45474()) {
               world.method_8501(currentPos, Blocks.field_10540.method_9564());
            }
         }
      }

      for (int dy = 1; dy <= 2; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               BlockPos airPos = platformCenterPos.method_10069(dx, dy, dz);
               world.method_8501(airPos, Blocks.field_10124.method_9564());
            }
         }
      }
   }
}
