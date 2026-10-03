package com.xie.smfs.fluid;

import com.xie.smfs.registry.ModFluids;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

public abstract class BloodLakeFluid extends TutorialFluid {
   public Fluid getStill() {
      return ModFluids.BLOOD_LAKE_STILL;
   }

   public Fluid getFlowing() {
      return ModFluids.BLOOD_LAKE_FLOWING;
   }

   public Item getBucketItem() {
      return ModFluids.BLOOD_LAKE_BUCKET;
   }

   protected BlockState toBlockState(FluidState state) {
      return (BlockState)ModFluids.BLOOD_LAKE_BLOCK.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
   }

   public static class Flowing extends BloodLakeFluid {
      protected void appendProperties(Builder<Fluid, FluidState> builder) {
         super.appendProperties(builder);
         builder.add(new Property[]{LEVEL});
      }

      public int getLevel(FluidState state) {
         return (Integer)state.get(LEVEL);
      }

      public boolean isStill(FluidState state) {
         return false;
      }
   }

   public static class Still extends BloodLakeFluid {
      public int getLevel(FluidState state) {
         return 8;
      }

      public boolean isStill(FluidState state) {
         return true;
      }
   }
}
