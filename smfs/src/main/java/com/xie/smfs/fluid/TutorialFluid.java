package com.xie.smfs.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class TutorialFluid extends FlowableFluid {
   public boolean matchesType(Fluid fluid) {
      return fluid == this.getStill() || fluid == this.getFlowing();
   }

   protected boolean isInfinite(World world) {
      return false;
   }

   protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
      BlockEntity blockEntity = state.getBlock() instanceof BlockEntityProvider ? world.getBlockEntity(pos) : null;
      Block.dropStacks(state, world, pos, blockEntity);
   }

   protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
      return false;
   }

   protected int getFlowSpeed(WorldView world) {
      return 4;
   }

   protected int getLevelDecreasePerBlock(WorldView world) {
      return 1;
   }

   public int getTickRate(WorldView world) {
      return 5;
   }

   protected float getBlastResistance() {
      return 100.0F;
   }
}
