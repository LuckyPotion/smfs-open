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
   public boolean method_15780(Fluid fluid) {
      return fluid == this.method_15751() || fluid == this.method_15750();
   }

   protected boolean method_15737(World world) {
      return false;
   }

   protected void method_15730(WorldAccess world, BlockPos pos, BlockState state) {
      BlockEntity blockEntity = state.method_26204() instanceof BlockEntityProvider ? world.method_8321(pos) : null;
      Block.method_9610(state, world, pos, blockEntity);
   }

   protected boolean method_15777(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
      return false;
   }

   protected int method_15733(WorldView worldView) {
      return 4;
   }

   protected int method_15739(WorldView worldView) {
      return 1;
   }

   public int method_15789(WorldView worldView) {
      return 5;
   }

   protected float method_15784() {
      return 100.0F;
   }
}
