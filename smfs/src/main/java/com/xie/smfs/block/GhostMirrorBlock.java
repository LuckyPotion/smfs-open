package com.xie.smfs.block;

import com.xie.smfs.client.renderer.StaticAnimatable;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.data.PlayerRoyalCurseManager;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.event.PlayerDeathHandler;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import com.xie.smfs.registry.ModBlockEntities;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostMirrorBlock extends GhostFurnitureBlock {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs-ghost-mirror");
   private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(1.0, 0.0, 8.0, 15.0, 32.0, 10.0);
   private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(1.0, 0.0, 8.0, 15.0, 32.0, 10.0);
   private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(8.0, 0.0, 1.0, 10.0, 32.0, 15.0);
   private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(8.0, 0.0, 1.0, 10.0, 32.0, 15.0);

   public GhostMirrorBlock(Settings settings) {
      super(settings);
   }

   @Override
   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
   }

   @Nullable
   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GHOST_MIRROR_BLOCK_ENTITY, pos, state);
   }

   private VoxelShape getShapeForDirection(Direction direction) {
      return switch (direction) {
         case NORTH -> SHAPE_NORTH;
         case SOUTH -> SHAPE_SOUTH;
         case EAST -> SHAPE_EAST;
         case WEST -> SHAPE_WEST;
         default -> SHAPE_NORTH;
      };
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      String placerName = placer != null ? placer.getName().getString() : "未知玩家";
      LOGGER.debug("鬼镜已放置于位置 {} 由玩家 {}，开始安排定时检测", pos, placerName);
      world.scheduleBlockTick(pos, this, 100);
      LOGGER.debug("已安排鬼镜定时检测，位置：{}，间隔：100 tick (5秒)", pos);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!world.isClient()) {
         LOGGER.debug("鬼镜定时检测触发，位置：{}", pos);
         this.checkNearbySpectatorPlayers(world, pos);
         world.scheduleBlockTick(pos, this, 100);
         LOGGER.debug("已重新安排鬼镜定时检测，位置：{}，间隔：100 tick (5秒)", pos);
      } else {
         LOGGER.warn("鬼镜定时检测在客户端触发，位置：{}，这不应该发生", pos);
      }
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      String playerName = player.getName().getString();
      if (world.isClient()) {
         LOGGER.debug("客户端交互：玩家 {} 点击鬼镜，位置：{}", playerName, pos);
         return ActionResult.SUCCESS;
      } else {
         LOGGER.debug("服务端交互：玩家 {} 点击鬼镜，位置：{}，是否旁观者：{}", playerName, pos, player.isSpectator());
         if (player.isSpectator()) {
            LOGGER.debug("玩家 {} 处于旁观者模式，开始复活流程", playerName);
            this.handleSpectatorRespawn((ServerPlayerEntity)player);
            return ActionResult.SUCCESS;
         } else {
            LOGGER.debug("玩家 {} 不是旁观者模式，忽略交互", playerName);
            return ActionResult.PASS;
         }
      }
   }

   private void checkNearbySpectatorPlayers(ServerWorld world, BlockPos mirrorPos) {
      LOGGER.debug("开始检测鬼镜周围的旁观者玩家，位置：{}", mirrorPos);
      Vec3d mirrorCenter = new Vec3d(mirrorPos.getX() + 0.5, mirrorPos.getY() + 0.5, mirrorPos.getZ() + 0.5);
      LOGGER.debug("鬼镜中心位置：{}", mirrorCenter);
      List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
         ServerPlayerEntity.class,
         new Box(mirrorPos).expand(3.0),
         player -> player.isAlive() && player.isSpectator() && player.getPos().distanceTo(mirrorCenter) <= 3.0
      );

      for (ServerPlayerEntity player : nearbyPlayers) {
         double distance = player.getPos().distanceTo(mirrorCenter);
         LOGGER.debug("检测到旁观者玩家：{}，距离鬼镜：{:.2f}格，位置：{}", player.getName().getString(), distance, player.getBlockPos());
      }

      for (ServerPlayerEntity spectator : nearbyPlayers) {
         LOGGER.debug("开始复活旁观者玩家：{}", spectator.getName().getString());
         this.handleSpectatorRespawn(spectator);
      }

      if (nearbyPlayers.isEmpty()) {
         LOGGER.debug("未检测到符合条件的旁观者玩家");
      }
   }

   private void handleSpectatorRespawn(ServerPlayerEntity player) {
      String playerName = player.getName().getString();
      LOGGER.debug("开始处理玩家 {} 的鬼镜复活流程", playerName);

      try {
         WorldConfig config = WorldConfig.getInstance(player.getWorld());
         if (config.modDifficulty == 2) {
            NbtCompound playerData = PlayerEvents.getCachedData(player);
            int revivalCount = playerData.getInt("ghostMirrorRevivalCount");
            if (revivalCount >= 3) {
               player.sendMessage(Text.literal("§c§l鬼镜复活次数已达上限"), true);
               return;
            }

            playerData.putInt("ghostMirrorRevivalCount", revivalCount + 1);
            PlayerEvents.saveDataToPlayer(player, playerData);
            LOGGER.debug("困难模式：玩家 {} 鬼镜复活次数 {}/3", playerName, revivalCount + 1);
         }

         boolean hasWangCurse = PlayerRoyalCurseManager.hasRoyalCurseUnlocked(player);
         boolean keepGhostsAfterMirrorResurrection = ModConfig.getInstance().keepGhostsAfterMirrorResurrection;
         if (hasWangCurse && keepGhostsAfterMirrorResurrection) {
            player.sendMessage(Text.literal("§c§l请关闭鬼镜保留厉鬼，使王家诅咒生效"), true);
            return;
         }

         LOGGER.debug("移除玩家 {} 的旁观者模式标志", playerName);
         SpectateModePacket.removeSpectatorFlag(player.getUuid());
         PlayerEvents.restoreSurvivalTime(player);
         LOGGER.debug("将玩家 {} 切换到生存模式", playerName);
         player.changeGameMode(GameMode.SURVIVAL);
         LOGGER.debug("关闭玩家 {} 的旁观者模式特殊能力", playerName);
         PlayerAbilities abilities = player.getAbilities();
         abilities.flying = false;
         abilities.allowFlying = false;
         abilities.invulnerable = false;
         abilities.creativeMode = false;
         player.noClip = false;
         player.sendAbilitiesUpdate();
         LOGGER.debug("已同步玩家 {} 的能力到客户端", playerName);
         if (player.getPose() != EntityPose.STANDING) {
            LOGGER.debug("重置玩家 {} 的姿势为站立", playerName);
            player.setPose(EntityPose.STANDING);
         }

         player.setSneaking(false);
         player.setSprinting(false);
         player.setSwimming(false);
         LOGGER.debug("已重置玩家 {} 的状态标志", playerName);
         player.setVelocity(0.0, player.getVelocity().y, 0.0);
         player.fallDistance = 0.0F;
         LOGGER.debug("已清除玩家 {} 的速度和坠落距离", playerName);
         LOGGER.debug("开始查找并杀死玩家 {} 相关的玩家鬼实体", playerName);
         this.killPlayerGhostEntities(player);
         LOGGER.debug("已完成玩家 {} 相关的玩家鬼实体清理", playerName);
         if (!keepGhostsAfterMirrorResurrection) {
            LOGGER.debug("配置要求鬼镜复活清空鬼数据，开始清空玩家 {} 的鬼数据", playerName);
            PlayerDeathHandler.clearAllGhostSlotsAndAttributes(player);
            player.sendMessage(Text.literal("§a§l你已通过鬼镜复活，但失去了所有灵异能力"), true);
         } else {
            player.sendMessage(Text.literal("§a§l你已通过鬼镜复活，保留了所有灵异能力"), true);
         }

         LOGGER.debug("恢复玩家 {} 的理智到最大值", playerName);
         float maxSanity = PlayerEvents.getMaxSanity(player);
         PlayerEvents.setCurrentSanity(player, maxSanity);
         LOGGER.info("玩家 {} 通过鬼镜复活，保留鬼数据配置: {}", playerName, keepGhostsAfterMirrorResurrection);
      } catch (Exception e) {
         LOGGER.error("处理玩家 {} 的鬼镜复活时发生错误", playerName, e);
         player.sendMessage(Text.literal("§c§l鬼镜复活失败，请联系管理员"), true);
      }
   }

   private void killPlayerGhostEntities(ServerPlayerEntity player) {
      String playerName = player.getName().getString();
      UUID playerUuid = player.getUuid();
      ServerWorld world = (ServerWorld)player.getWorld();
      Box searchBox = new Box(player.getBlockPos()).expand(100.0);
      List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, searchBox, entity -> {
         if (entity instanceof PlayerGhostEntity playerGhost) {
            String ghostPlayerUuid = playerGhost.getPlayerUuid();
            if (ghostPlayerUuid != null && ghostPlayerUuid.equals(playerUuid.toString())) {
               LOGGER.debug("找到属于玩家 {} 的玩家鬼实体：{}", playerName, playerGhost);
               return true;
            }
         }

         return false;
      });
      LOGGER.debug("在玩家 {} 周围找到 {} 个玩家鬼实体", playerName, nearbyEntities.size());
      if (!nearbyEntities.isEmpty()) {
         Entity latestGhost = nearbyEntities.stream().min((e1, e2) -> {
            int age1 = e1.age;
            int age2 = e2.age;
            return Integer.compare(age1, age2);
         }).orElse(null);
         if (latestGhost != null) {
            try {
               LOGGER.debug("开始杀死最近生成的玩家鬼实体：{}", latestGhost);
               latestGhost.remove(RemovalReason.DISCARDED);
               LOGGER.debug("成功杀死最近生成的玩家鬼实体：{}", latestGhost);
               LOGGER.debug("保留 {} 个历史玩家鬼实体", nearbyEntities.size() - 1);
            } catch (Exception e) {
               LOGGER.error("杀死玩家鬼实体时发生错误：{}", latestGhost, e);
            }
         }
      } else {
         LOGGER.debug("未找到属于玩家 {} 的玩家鬼实体", playerName);
      }
   }
}
