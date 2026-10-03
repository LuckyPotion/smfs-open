package com.xie.smfs.block;

import com.xie.smfs.registry.ModBlockEntities;
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
import org.jetbrains.annotations.Nullable;

public class FootprintBlock extends Block implements BlockEntityProvider {
   private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 0.01, 16.0);
   public static final int DEFAULT_DECAY_TICKS = 240;
   public static final int MIN_DECAY_TICKS = 20;
   public static final int MAX_DECAY_TICKS = 12000;

   public FootprintBlock(Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return VoxelShapes.empty();
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      super.onBlockAdded(state, world, pos, oldState, notify);
      if (!world.isClient()) {
         world.getBlockEntity(pos, ModBlockEntities.FOOTPRINT_BLOCK_ENTITY).ifPresent(entity -> {
            int decayTime = entity.getDecayTime();
            world.scheduleBlockTick(pos, this, decayTime);
         });
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!world.isClient()) {
         world.removeBlock(pos, false);
      }
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return true;
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new com.xie.smfs.block.entity.FootprintBlockEntity(pos, state);
   }

   public static int clampDecayTime(int ticks) {
      return Math.max(20, Math.min(12000, ticks));
   }

   public static int getDefaultDecayTicks() {
      return 240;
   }

   public static int getMinDecayTicks() {
      return 20;
   }

   public static int getMaxDecayTicks() {
      return 12000;
   }
}
