package com.xie.smfs.fluid;

import com.xie.smfs.registry.ModFluids;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;

public abstract class GhostLakeFluid extends TutorialFluid {
   public Fluid method_15751() {
      return ModFluids.GHOST_LAKE_STILL;
   }

   public Fluid method_15750() {
      return ModFluids.GHOST_LAKE_FLOWING;
   }

   public Item method_15774() {
      return ModFluids.GHOST_LAKE_BUCKET;
   }

   protected BlockState method_15790(FluidState fluidState) {
      return (BlockState)ModFluids.GHOST_LAKE_BLOCK.method_9564().method_11657(Properties.field_12538, method_15741(fluidState));
   }

   public static class Flowing extends GhostLakeFluid {
      protected void method_15775(Builder<Fluid, FluidState> builder) {
         super.method_15775(builder);
         builder.method_11667(new Property[]{field_15900});
      }

      public int method_15779(FluidState fluidState) {
         return (Integer)fluidState.method_11654(field_15900);
      }

      public boolean method_15793(FluidState fluidState) {
         return false;
      }
   }

   public static class Still extends GhostLakeFluid {
      public int method_15779(FluidState fluidState) {
         return 8;
      }

      public boolean method_15793(FluidState fluidState) {
         return true;
      }
   }
}
