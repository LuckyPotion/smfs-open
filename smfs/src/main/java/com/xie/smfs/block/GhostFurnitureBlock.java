package com.xie.smfs.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class GhostFurnitureBlock extends BlockWithEntity {
   public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
   protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   public GhostFurnitureBlock(Settings settings) {
      super(settings.nonOpaque());
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH));
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return null;
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
      float baseSpeed = super.calcBlockBreakingDelta(state, player, world, pos);
      if (tool != null && tool.getItem() instanceof AxeItem) {
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

      return baseSpeed;
   }
}
