package com.xie.smfs.block;

import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModBlockEntities;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
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
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoldCoffinBlock extends BlockWithEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger(GoldCoffinBlock.class);
   public static final DirectionProperty FACING = DirectionProperty.of(
      "facing", new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}
   );
   public static final BooleanProperty OPEN = BooleanProperty.of("open");
   public static final BooleanProperty OCCUPIED = BooleanProperty.of("occupied");
   private static final WeakHashMap<PlayerEntity, Boolean> EXITING_PLAYERS = new WeakHashMap<>();
   private static final WeakHashMap<PlayerEntity, Boolean> LYING_PLAYERS = new WeakHashMap<>();
   private static final VoxelShape SHAPE_NORTH_SOUTH = Block.createCuboidShape(0.0, 0.0, -8.0, 16.0, 16.0, 24.0);
   private static final VoxelShape SHAPE_EAST_WEST = Block.createCuboidShape(-8.0, 0.0, 0.0, 24.0, 16.0, 16.0);

   public GoldCoffinBlock(Settings settings) {
      super(settings.nonOpaque());
      this.setDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false))
            .with(OCCUPIED, false)
      );
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, OPEN, OCCUPIED});
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      boolean isPlayerInCoffin = this.isPlayerInCoffin(world, pos, player);
      if (isPlayerInCoffin) {
         LOGGER.debug("检测到玩家在棺材内部 - 玩家: {}, 位置: {}", player.getName().getString(), pos);
         if (world.isClient) {
            LOGGER.debug("客户端内部操作 - 返回SUCCESS");
            return ActionResult.SUCCESS;
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过操作");
            return ActionResult.SUCCESS;
         }

         if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            LOGGER.debug("玩家潜行右键 - 弹出玩家");
            this.setPlayerExiting(player, true);
            this.setLyingFlag(player, false);
            this.ejectPlayer(player, pos, state);
            world.setBlockState(pos, (BlockState)state.with(OCCUPIED, false), 3);
            this.setPlayerExiting(player, false);
            return ActionResult.SUCCESS;
         }

         if (!player.isSneaking() && hand == Hand.MAIN_HAND) {
            boolean isOpen = (Boolean)state.get(OPEN);
            LOGGER.debug("玩家普通右键 - 切换开关状态: {} -> {}", isOpen, !isOpen);
            BlockState newState = (BlockState)state.with(OPEN, !isOpen);
            world.setBlockState(pos, newState, 3);
            if (isOpen) {
               player.sendMessage(Text.translatable("block.smfs.gold_coffin.close"), true);
            } else {
               player.sendMessage(Text.translatable("block.smfs.gold_coffin.open"), true);
            }

            return ActionResult.SUCCESS;
         } else {
            LOGGER.debug("内部操作条件未满足 - 返回SUCCESS");
            return ActionResult.SUCCESS;
         }
      } else {
         if (world.isClient) {
            return ActionResult.SUCCESS;
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过外部操作");
            return ActionResult.SUCCESS;
         }

         if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            boolean isOpen = (Boolean)state.get(OPEN);
            BlockState newState = (BlockState)state.with(OPEN, !isOpen);
            world.setBlockState(pos, newState, 3);
            if (isOpen) {
               player.sendMessage(Text.translatable("block.smfs.gold_coffin.close"), true);
            } else {
               player.sendMessage(Text.translatable("block.smfs.gold_coffin.open"), true);
            }

            return ActionResult.SUCCESS;
         } else if (player.isSneaking() || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
         } else if (!(Boolean)state.get(OPEN)) {
            player.sendMessage(Text.translatable("block.smfs.gold_coffin.closed"), true);
            return ActionResult.SUCCESS;
         } else if ((Boolean)state.get(OCCUPIED)) {
            player.sendMessage(Text.translatable("block.smfs.gold_coffin.occupied"), true);
            return ActionResult.SUCCESS;
         } else {
            this.layPlayerInCoffin(player, pos, state);
            world.setBlockState(pos, (BlockState)state.with(OCCUPIED, true), 3);
            this.setLyingFlag(player, true);
            this.scheduleTick(world, pos);
            return ActionResult.SUCCESS;
         }
      }
   }

   private boolean isPlayerExiting(PlayerEntity player) {
      return EXITING_PLAYERS.getOrDefault(player, false);
   }

   private void setPlayerExiting(PlayerEntity player, boolean exiting) {
      if (exiting) {
         EXITING_PLAYERS.put(player, true);
      } else {
         EXITING_PLAYERS.remove(player);
      }

      LOGGER.debug("设置玩家退出状态 - 玩家: {}, 状态: {}", player.getName().getString(), exiting);
   }

   private boolean isLyingFlagSet(PlayerEntity player) {
      return LYING_PLAYERS.getOrDefault(player, false);
   }

   private void setLyingFlag(PlayerEntity player, boolean lying) {
      if (lying) {
         LYING_PLAYERS.put(player, true);
      } else {
         LYING_PLAYERS.remove(player);
      }

      LOGGER.debug("设置玩家躺下标志位 - 玩家: {}, 状态: {}", player.getName().getString(), lying);
   }

   private boolean isPlayerInCoffin(World world, BlockPos pos, PlayerEntity player) {
      Box coffinBox = this.getInnerBoundingBox(pos);
      boolean positionMatch = coffinBox.contains(player.getPos());
      boolean poseMatch = player.getPose() == EntityPose.SLEEPING;
      boolean lyingFlag = this.isLyingFlagSet(player);
      LOGGER.debug(
         "检查玩家是否在棺材中 - 玩家位置: {}, 棺材位置: {}, 姿势: {}, 位置匹配: {}, 姿势匹配: {}, 躺下标志位: {}",
         player.getBlockPos(),
         pos,
         player.getPose(),
         positionMatch,
         poseMatch,
         lyingFlag
      );
      return positionMatch && poseMatch && lyingFlag;
   }

   private Box getInnerBoundingBox(BlockPos pos) {
      return new Box(pos.getX() + 0.125, pos.getY(), pos.getZ() + 0.125, pos.getX() + 0.875, pos.getY() + 0.5, pos.getZ() + 0.875);
   }

   private void layPlayerInCoffin(PlayerEntity player, BlockPos pos, BlockState state) {
      LOGGER.debug("将玩家放入棺材 - 玩家: {}, 位置: {}", player.getName().getString(), pos);
      Direction facing = (Direction)state.get(FACING);
      Vec3d center = Vec3d.ofCenter(pos);
      double x = center.x;
      double y = pos.getY() + 0.2;
      double z = center.z;
      LOGGER.debug("目标位置 - X: {}, Y: {}, Z: {}, 朝向: {}", x, y, z, facing);
      player.teleport(x, y, z);
      float yaw = this.getCorrectYawFromDirection(facing);
      LOGGER.debug("设置玩家朝向: {}", yaw);
      player.setYaw(yaw);
      player.setPitch(0.0F);
      player.setHeadYaw(yaw);
      player.setBodyYaw(yaw);
      player.setPose(EntityPose.SLEEPING);
      LOGGER.debug("玩家姿势设置为: {}", player.getPose());
      if (player instanceof ServerPlayerEntity serverPlayer) {
         LOGGER.debug("同步位置和姿势到客户端");
         serverPlayer.networkHandler.requestTeleport(x, y, z, yaw, 0.0F, Set.of());
         serverPlayer.setSneaking(false);
      }

      CoffinEffectManager.setPlayerInGoldCoffin(player, pos);
      player.sendMessage(Text.translatable("block.smfs.gold_coffin.enter"), true);
   }

   private void ejectPlayer(PlayerEntity player, BlockPos pos, BlockState state) {
      LOGGER.debug("弹出玩家 - 玩家: {}, 位置: {}", player.getName().getString(), pos);
      Direction facing = (Direction)state.get(FACING);
      double x = pos.getX() + 0.5 + facing.getOffsetX() * 1.5;
      double z = pos.getZ() + 0.5 + facing.getOffsetZ() * 1.5;
      double y = pos.getY() + 0.5;
      LOGGER.debug("弹出位置 - X: {}, Y: {}, Z: {}", x, y, z);
      player.teleport(x, y, z);
      player.setPose(EntityPose.STANDING);
      LOGGER.debug("玩家姿势设置为: {}", player.getPose());
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.setSneaking(false);
      }

      CoffinEffectManager.removePlayerFromCoffin(player);
      player.sendMessage(Text.translatable("block.smfs.gold_coffin.exit"), true);
   }

   private float getCorrectYawFromDirection(Direction direction) {
      float yaw = switch (direction) {
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case EAST -> 270.0F;
         case NORTH -> 180.0F;
         default -> 0.0F;
      };
      LOGGER.debug("计算朝向 - 输入: {}, 输出: {}", direction, yaw);
      return yaw;
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if ((Boolean)state.get(OCCUPIED) && entity instanceof PlayerEntity player && player.getPose() == EntityPose.SLEEPING) {
         if (this.isPlayerExiting(player) || !this.isLyingFlagSet(player)) {
            LOGGER.debug("玩家正在退出或躺下标志位关闭，跳过碰撞检测");
            return;
         }

         Box innerBox = this.getInnerBoundingBox(pos);
         if (!innerBox.contains(player.getPos())) {
            LOGGER.debug("检测到玩家被推出棺材 - 重新定位");
            Vec3d center = Vec3d.ofCenter(pos);
            player.teleport(center.x, pos.getY() + 0.2, center.z);
            player.setYaw(this.getCorrectYawFromDirection((Direction)state.get(FACING)));
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.networkHandler
                  .requestTeleport(center.x, pos.getY() + 0.2, center.z, this.getCorrectYawFromDirection((Direction)state.get(FACING)), 0.0F, Set.of());
            }
         }
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock()) && (Boolean)state.get(OCCUPIED)) {
         LOGGER.debug("方块被破坏 - 弹出玩家");
         Box searchBox = this.getInnerBoundingBox(pos);
         List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, p -> p.getPose() == EntityPose.SLEEPING);
         Iterator var8 = players.iterator();
         if (var8.hasNext()) {
            PlayerEntity player = (PlayerEntity)var8.next();
            if (!this.isPlayerExiting(player)) {
               this.setPlayerExiting(player, true);
               this.setLyingFlag(player, false);
               this.ejectPlayer(player, pos, state);
               this.setPlayerExiting(player, false);
            }
         }
      }

      super.onStateReplaced(state, world, pos, newState, moved);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction direction = (Direction)state.get(FACING);
      return direction != Direction.NORTH && direction != Direction.SOUTH ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return this.getOutlineShape(state, world, pos, ShapeContext.absent());
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!world.isClient && (Boolean)state.get(OCCUPIED)) {
         this.scheduleTick(world, pos);
      }
   }

   private void scheduleTick(World world, BlockPos pos) {
      if (!world.isClient) {
         world.scheduleBlockTick(pos, this, 20);
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!world.isClient && (Boolean)state.get(OCCUPIED)) {
         Box searchBox = this.getInnerBoundingBox(pos);
         List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, p -> p.getPose() == EntityPose.SLEEPING && this.isLyingFlagSet(p));
         if (players.isEmpty()) {
            LOGGER.debug("定期检测 - 没有玩家在棺材中，更新状态");
            world.setBlockState(pos, (BlockState)state.with(OCCUPIED, false), 3);
         } else {
            this.scheduleTick(world, pos);
         }
      }
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContextParameterSet.Builder builder) {
      ItemStack tool = (ItemStack)builder.getOptional(LootContextParameters.TOOL);
      return tool == null
            || !(tool.getItem() instanceof AxeItem)
               && !tool.isOf(Items.IRON_AXE)
               && !tool.isOf(Items.GOLDEN_AXE)
               && !tool.isOf(Items.DIAMOND_AXE)
               && !tool.isOf(Items.NETHERITE_AXE)
               && !tool.isOf(Items.WOODEN_AXE)
               && !tool.isOf(Items.STONE_AXE)
         ? List.of()
         : List.of(new ItemStack(this));
   }

   public float getHardness(BlockState state, BlockView world, BlockPos pos) {
      return 3.0F;
   }

   public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      ItemStack tool = player.getMainHandStack();
      if (tool.getItem() instanceof AxeItem) {
         float baseSpeed = super.calcBlockBreakingDelta(state, player, world, pos);
         if (tool.isOf(Items.NETHERITE_AXE)) {
            return baseSpeed * 3.0F;
         }

         if (tool.isOf(Items.DIAMOND_AXE)) {
            return baseSpeed * 2.5F;
         }

         if (tool.isOf(Items.IRON_AXE)) {
            return baseSpeed * 2.0F;
         }

         if (tool.isOf(Items.GOLDEN_AXE)) {
            return baseSpeed * 1.5F;
         }

         if (tool.isOf(Items.STONE_AXE)) {
            return baseSpeed * 1.2F;
         }

         if (tool.isOf(Items.WOODEN_AXE)) {
            return baseSpeed * 1.0F;
         }
      }

      return super.calcBlockBreakingDelta(state, player, world, pos);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return ModBlockEntities.GOLD_COFFIN_BLOCK_ENTITY.instantiate(pos, state);
   }
}
