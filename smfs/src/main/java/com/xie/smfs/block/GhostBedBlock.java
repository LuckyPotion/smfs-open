package com.xie.smfs.block;

import com.xie.smfs.client.renderer.StaticAnimatable;
import com.xie.smfs.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class GhostBedBlock extends GhostFurnitureBlock {
   protected static final VoxelShape SHAPE = Block.method_9541(0.0, 0.0, 0.0, 32.0, 8.0, 32.0);
   private static final VoxelShape SHAPE_NORTH = Block.method_9541(0.0, 0.0, 0.0, 32.0, 8.0, 32.0);
   private static final VoxelShape SHAPE_SOUTH = Block.method_9541(-16.0, 0.0, -16.0, 16.0, 8.0, 16.0);
   private static final VoxelShape SHAPE_EAST = Block.method_9541(-16.0, 0.0, 0.0, 16.0, 8.0, 32.0);
   private static final VoxelShape SHAPE_WEST = Block.method_9541(0.0, 0.0, -16.0, 32.0, 8.0, 16.0);

   public GhostBedBlock(Settings settings) {
      super(settings);
   }

   @Override
   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.method_11654(FACING));
   }

   public VoxelShape method_9549(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.method_11654(FACING));
   }

   @Nullable
   @Override
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GHOST_BED_BLOCK_ENTITY, pos, state);
   }

   private VoxelShape getShapeForDirection(Direction direction) {
      return switch (direction) {
         case field_11043 -> SHAPE_NORTH;
         case field_11035 -> SHAPE_SOUTH;
         case field_11034 -> SHAPE_EAST;
         case field_11039 -> SHAPE_WEST;
         default -> SHAPE_NORTH;
      };
   }
}
