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
   public static final DirectionProperty FACING = DirectionProperty.method_11845(
      "facing", new Direction[]{Direction.field_11043, Direction.field_11035, Direction.field_11034, Direction.field_11039}
   );
   public static final BooleanProperty OPEN = BooleanProperty.method_11825("open");
   public static final BooleanProperty OCCUPIED = BooleanProperty.method_11825("occupied");
   private static final WeakHashMap<PlayerEntity, Boolean> EXITING_PLAYERS = new WeakHashMap<>();
   private static final WeakHashMap<PlayerEntity, Boolean> LYING_PLAYERS = new WeakHashMap<>();
   private static final VoxelShape SHAPE_NORTH_SOUTH = Block.method_9541(0.0, 0.0, -8.0, 16.0, 16.0, 24.0);
   private static final VoxelShape SHAPE_EAST_WEST = Block.method_9541(-8.0, 0.0, 0.0, 24.0, 16.0, 16.0);

   public GoldCoffinBlock(Settings settings) {
      super(settings.method_22488());
      this.method_9590(
         (BlockState)((BlockState)((BlockState)((BlockState)this.field_10647.method_11664()).method_11657(FACING, Direction.field_11043))
               .method_11657(OPEN, false))
            .method_11657(OCCUPIED, false)
      );
   }

   protected void method_9515(Builder<Block, BlockState> builder) {
      builder.method_11667(new Property[]{FACING, OPEN, OCCUPIED});
   }

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      boolean isPlayerInCoffin = this.isPlayerInCoffin(world, pos, player);
      if (isPlayerInCoffin) {
         LOGGER.debug("检测到玩家在棺材内部 - 玩家: {}, 位置: {}", player.method_5477().getString(), pos);
         if (world.field_9236) {
            LOGGER.debug("客户端内部操作 - 返回SUCCESS");
            return ActionResult.field_5812;
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过操作");
            return ActionResult.field_5812;
         }

         if (player.method_5715() && hand == Hand.field_5808) {
            LOGGER.debug("玩家潜行右键 - 弹出玩家");
            this.setPlayerExiting(player, true);
            this.setLyingFlag(player, false);
            this.ejectPlayer(player, pos, state);
            world.method_8652(pos, (BlockState)state.method_11657(OCCUPIED, false), 3);
            this.setPlayerExiting(player, false);
            return ActionResult.field_5812;
         }

         if (!player.method_5715() && hand == Hand.field_5808) {
            boolean isOpen = (Boolean)state.method_11654(OPEN);
            LOGGER.debug("玩家普通右键 - 切换开关状态: {} -> {}", isOpen, !isOpen);
            BlockState newState = (BlockState)state.method_11657(OPEN, !isOpen);
            world.method_8652(pos, newState, 3);
            if (isOpen) {
               player.method_7353(Text.method_43471("block.smfs.gold_coffin.close"), true);
            } else {
               player.method_7353(Text.method_43471("block.smfs.gold_coffin.open"), true);
            }

            return ActionResult.field_5812;
         } else {
            LOGGER.debug("内部操作条件未满足 - 返回SUCCESS");
            return ActionResult.field_5812;
         }
      } else {
         if (world.field_9236) {
            return ActionResult.field_5812;
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过外部操作");
            return ActionResult.field_5812;
         }

         if (player.method_5715() && hand == Hand.field_5808) {
            boolean isOpen = (Boolean)state.method_11654(OPEN);
            BlockState newState = (BlockState)state.method_11657(OPEN, !isOpen);
            world.method_8652(pos, newState, 3);
            if (isOpen) {
               player.method_7353(Text.method_43471("block.smfs.gold_coffin.close"), true);
            } else {
               player.method_7353(Text.method_43471("block.smfs.gold_coffin.open"), true);
            }

            return ActionResult.field_5812;
         } else if (player.method_5715() || hand != Hand.field_5808) {
            return ActionResult.field_5811;
         } else if (!(Boolean)state.method_11654(OPEN)) {
            player.method_7353(Text.method_43471("block.smfs.gold_coffin.closed"), true);
            return ActionResult.field_5812;
         } else if ((Boolean)state.method_11654(OCCUPIED)) {
            player.method_7353(Text.method_43471("block.smfs.gold_coffin.occupied"), true);
            return ActionResult.field_5812;
         } else {
            this.layPlayerInCoffin(player, pos, state);
            world.method_8652(pos, (BlockState)state.method_11657(OCCUPIED, true), 3);
            this.setLyingFlag(player, true);
            this.scheduleTick(world, pos);
            return ActionResult.field_5812;
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

      LOGGER.debug("设置玩家退出状态 - 玩家: {}, 状态: {}", player.method_5477().getString(), exiting);
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

      LOGGER.debug("设置玩家躺下标志位 - 玩家: {}, 状态: {}", player.method_5477().getString(), lying);
   }

   private boolean isPlayerInCoffin(World world, BlockPos pos, PlayerEntity player) {
      Box coffinBox = this.getInnerBoundingBox(pos);
      boolean positionMatch = coffinBox.method_1006(player.method_19538());
      boolean poseMatch = player.method_18376() == EntityPose.field_18078;
      boolean lyingFlag = this.isLyingFlagSet(player);
      LOGGER.debug(
         "检查玩家是否在棺材中 - 玩家位置: {}, 棺材位置: {}, 姿势: {}, 位置匹配: {}, 姿势匹配: {}, 躺下标志位: {}",
         player.method_24515(),
         pos,
         player.method_18376(),
         positionMatch,
         poseMatch,
         lyingFlag
      );
      return positionMatch && poseMatch && lyingFlag;
   }

   private Box getInnerBoundingBox(BlockPos pos) {
      return new Box(
         pos.method_10263() + 0.125,
         pos.method_10264(),
         pos.method_10260() + 0.125,
         pos.method_10263() + 0.875,
         pos.method_10264() + 0.5,
         pos.method_10260() + 0.875
      );
   }

   private void layPlayerInCoffin(PlayerEntity player, BlockPos pos, BlockState state) {
      LOGGER.debug("将玩家放入棺材 - 玩家: {}, 位置: {}", player.method_5477().getString(), pos);
      Direction facing = (Direction)state.method_11654(FACING);
      Vec3d center = Vec3d.method_24953(pos);
      double x = center.field_1352;
      double y = pos.method_10264() + 0.2;
      double z = center.field_1350;
      LOGGER.debug("目标位置 - X: {}, Y: {}, Z: {}, 朝向: {}", x, y, z, facing);
      player.method_20620(x, y, z);
      float yaw = this.getCorrectYawFromDirection(facing);
      LOGGER.debug("设置玩家朝向: {}", yaw);
      player.method_36456(yaw);
      player.method_36457(0.0F);
      player.method_5847(yaw);
      player.method_5636(yaw);
      player.method_18380(EntityPose.field_18078);
      LOGGER.debug("玩家姿势设置为: {}", player.method_18376());
      if (player instanceof ServerPlayerEntity serverPlayer) {
         LOGGER.debug("同步位置和姿势到客户端");
         serverPlayer.field_13987.method_14360(x, y, z, yaw, 0.0F, Set.of());
         serverPlayer.method_5660(false);
      }

      CoffinEffectManager.setPlayerInGoldCoffin(player, pos);
      player.method_7353(Text.method_43471("block.smfs.gold_coffin.enter"), true);
   }

   private void ejectPlayer(PlayerEntity player, BlockPos pos, BlockState state) {
      LOGGER.debug("弹出玩家 - 玩家: {}, 位置: {}", player.method_5477().getString(), pos);
      Direction facing = (Direction)state.method_11654(FACING);
      double x = pos.method_10263() + 0.5 + facing.method_10148() * 1.5;
      double z = pos.method_10260() + 0.5 + facing.method_10165() * 1.5;
      double y = pos.method_10264() + 0.5;
      LOGGER.debug("弹出位置 - X: {}, Y: {}, Z: {}", x, y, z);
      player.method_20620(x, y, z);
      player.method_18380(EntityPose.field_18076);
      LOGGER.debug("玩家姿势设置为: {}", player.method_18376());
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_5660(false);
      }

      CoffinEffectManager.removePlayerFromCoffin(player);
      player.method_7353(Text.method_43471("block.smfs.gold_coffin.exit"), true);
   }

   private float getCorrectYawFromDirection(Direction direction) {
      float yaw = switch (direction) {
         case field_11035 -> 0.0F;
         case field_11039 -> 90.0F;
         case field_11034 -> 270.0F;
         case field_11043 -> 180.0F;
         default -> 0.0F;
      };
      LOGGER.debug("计算朝向 - 输入: {}, 输出: {}", direction, yaw);
      return yaw;
   }

   public void method_9548(BlockState state, World world, BlockPos pos, Entity entity) {
      if ((Boolean)state.method_11654(OCCUPIED) && entity instanceof PlayerEntity player && player.method_18376() == EntityPose.field_18078) {
         if (this.isPlayerExiting(player) || !this.isLyingFlagSet(player)) {
            LOGGER.debug("玩家正在退出或躺下标志位关闭，跳过碰撞检测");
            return;
         }

         Box innerBox = this.getInnerBoundingBox(pos);
         if (!innerBox.method_1006(player.method_19538())) {
            LOGGER.debug("检测到玩家被推出棺材 - 重新定位");
            Vec3d center = Vec3d.method_24953(pos);
            player.method_20620(center.field_1352, pos.method_10264() + 0.2, center.field_1350);
            player.method_36456(this.getCorrectYawFromDirection((Direction)state.method_11654(FACING)));
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.field_13987
                  .method_14360(
                     center.field_1352,
                     pos.method_10264() + 0.2,
                     center.field_1350,
                     this.getCorrectYawFromDirection((Direction)state.method_11654(FACING)),
                     0.0F,
                     Set.of()
                  );
            }
         }
      }
   }

   public void method_9536(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.method_27852(newState.method_26204()) && (Boolean)state.method_11654(OCCUPIED)) {
         LOGGER.debug("方块被破坏 - 弹出玩家");
         Box searchBox = this.getInnerBoundingBox(pos);
         List<PlayerEntity> players = world.method_8390(PlayerEntity.class, searchBox, p -> p.method_18376() == EntityPose.field_18078);
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

      super.method_9536(state, world, pos, newState, moved);
   }

   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction direction = (Direction)state.method_11654(FACING);
      return direction != Direction.field_11043 && direction != Direction.field_11035 ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
   }

   public VoxelShape method_9571(BlockState state, BlockView world, BlockPos pos) {
      return this.method_9530(state, world, pos, ShapeContext.method_16194());
   }

   public void method_9615(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!world.field_9236 && (Boolean)state.method_11654(OCCUPIED)) {
         this.scheduleTick(world, pos);
      }
   }

   private void scheduleTick(World world, BlockPos pos) {
      if (!world.field_9236) {
         world.method_39279(pos, this, 20);
      }
   }

   public void method_9588(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!world.field_9236 && (Boolean)state.method_11654(OCCUPIED)) {
         Box searchBox = this.getInnerBoundingBox(pos);
         List<PlayerEntity> players = world.method_8390(
            PlayerEntity.class, searchBox, p -> p.method_18376() == EntityPose.field_18078 && this.isLyingFlagSet(p)
         );
         if (players.isEmpty()) {
            LOGGER.debug("定期检测 - 没有玩家在棺材中，更新状态");
            world.method_8652(pos, (BlockState)state.method_11657(OCCUPIED, false), 3);
         } else {
            this.scheduleTick(world, pos);
         }
      }
   }

   public BlockRenderType method_9604(BlockState state) {
      return BlockRenderType.field_11456;
   }

   public List<ItemStack> method_9560(BlockState state, net.minecraft.loot.context.LootContextParameterSet.Builder builder) {
      ItemStack tool = (ItemStack)builder.method_51876(LootContextParameters.field_1229);
      return tool == null
            || !(tool.method_7909() instanceof AxeItem)
               && !tool.method_31574(Items.field_8475)
               && !tool.method_31574(Items.field_8825)
               && !tool.method_31574(Items.field_8556)
               && !tool.method_31574(Items.field_22025)
               && !tool.method_31574(Items.field_8406)
               && !tool.method_31574(Items.field_8062)
         ? List.of()
         : List.of(new ItemStack(this));
   }

   public float getHardness(BlockState state, BlockView world, BlockPos pos) {
      return 3.0F;
   }

   public float method_9594(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      ItemStack tool = player.method_6047();
      if (tool.method_7909() instanceof AxeItem) {
         float baseSpeed = super.method_9594(state, player, world, pos);
         if (tool.method_31574(Items.field_22025)) {
            return baseSpeed * 3.0F;
         }

         if (tool.method_31574(Items.field_8556)) {
            return baseSpeed * 2.5F;
         }

         if (tool.method_31574(Items.field_8475)) {
            return baseSpeed * 2.0F;
         }

         if (tool.method_31574(Items.field_8825)) {
            return baseSpeed * 1.5F;
         }

         if (tool.method_31574(Items.field_8062)) {
            return baseSpeed * 1.2F;
         }

         if (tool.method_31574(Items.field_8406)) {
            return baseSpeed * 1.0F;
         }
      }

      return super.method_9594(state, player, world, pos);
   }

   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return ModBlockEntities.GOLD_COFFIN_BLOCK_ENTITY.method_11032(pos, state);
   }
}
