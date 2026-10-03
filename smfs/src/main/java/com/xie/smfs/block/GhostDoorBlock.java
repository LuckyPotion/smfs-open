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

public class GhostDoorBlock extends GhostFurnitureBlock {
   private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0.0, 0.0, 6.5, 16.0, 32.0, 9.5);
   private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0.0, 0.0, 6.5, 16.0, 32.0, 9.5);
   private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(6.5, 0.0, 0.0, 9.5, 32.0, 16.0);
   private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(6.5, 0.0, 0.0, 9.5, 32.0, 16.0);

   public GhostDoorBlock(Settings settings) {
      super(settings);
   }

   @Override
   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
   }

   @Nullable
   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GHOST_DOOR_BLOCK_ENTITY, pos, state);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
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
}
