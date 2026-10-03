package com.xie.smfs.block;

import com.xie.smfs.block.entity.Footprint2BlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class Footprint2Block extends Block implements BlockEntityProvider {
   private static final VoxelShape SHAPE = Block.method_9541(0.0, 0.0, 0.0, 16.0, 0.01, 16.0);

   public Footprint2Block(Settings settings) {
      super(settings);
   }

   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public VoxelShape method_9549(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.method_1073();
   }

   public void method_9615(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      super.method_9615(state, world, pos, oldState, notify);
      if (!world.method_8608()) {
         world.method_39279(pos, this, 20);
      }
   }

   public void method_9588(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!world.method_8608()) {
         world.method_8650(pos, false);
      }
   }

   public boolean method_9579(BlockState state, BlockView world, BlockPos pos) {
      return true;
   }

   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new Footprint2BlockEntity(pos, state);
   }
}
