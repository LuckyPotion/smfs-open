package com.xie.smfs.block;

import com.xie.smfs.client.renderer.StaticAnimatable;
import com.xie.smfs.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class GhostCandleBlock extends GhostFurnitureBlock {
   protected static final VoxelShape SHAPE = VoxelShapes.method_1081(0.375, 0.0, 0.375, 0.625, 0.625, 0.625);

   public GhostCandleBlock(Settings settings) {
      super(settings);
   }

   @Override
   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public VoxelShape method_9549(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   @Nullable
   @Override
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GHOST_CANDLE_BLOCK_ENTITY, pos, state);
   }
}
