package com.xie.smfs.registry;

import com.xie.smfs.block.BloodLakeBlock;
import com.xie.smfs.block.GhostLakeBlock;
import com.xie.smfs.fluid.BloodLakeFluid;
import com.xie.smfs.fluid.GhostLakeFluid;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModFluids {
   public static FlowableFluid BLOOD_LAKE_STILL;
   public static FlowableFluid BLOOD_LAKE_FLOWING;
   public static Item BLOOD_LAKE_BUCKET;
   public static Block BLOOD_LAKE_BLOCK;
   public static FlowableFluid GHOST_LAKE_STILL;
   public static FlowableFluid GHOST_LAKE_FLOWING;
   public static Item GHOST_LAKE_BUCKET;
   public static Block GHOST_LAKE_BLOCK;

   public static void registerFluids() {
      BLOOD_LAKE_STILL = (FlowableFluid)Registry.method_10230(Registries.field_41173, new Identifier("smfs", "blood_lake_still"), new BloodLakeFluid.Still());
      BLOOD_LAKE_FLOWING = (FlowableFluid)Registry.method_10230(
         Registries.field_41173, new Identifier("smfs", "blood_lake_flowing"), new BloodLakeFluid.Flowing()
      );
      BLOOD_LAKE_BLOCK = (Block)Registry.method_10230(
         Registries.field_41175, new Identifier("smfs", "blood_lake"), new BloodLakeBlock(FabricBlockSettings.method_9630(Blocks.field_10382))
      );
      BLOOD_LAKE_BUCKET = (Item)Registry.method_10230(
         Registries.field_41178,
         new Identifier("smfs", "blood_lake_bucket"),
         new BucketItem(BLOOD_LAKE_STILL, new FabricItemSettings().recipeRemainder(Items.field_8550).maxCount(1))
      );
      GHOST_LAKE_STILL = (FlowableFluid)Registry.method_10230(Registries.field_41173, new Identifier("smfs", "ghost_lake_still"), new GhostLakeFluid.Still());
      GHOST_LAKE_FLOWING = (FlowableFluid)Registry.method_10230(
         Registries.field_41173, new Identifier("smfs", "ghost_lake_flowing"), new GhostLakeFluid.Flowing()
      );
      GHOST_LAKE_BLOCK = (Block)Registry.method_10230(
         Registries.field_41175, new Identifier("smfs", "ghost_lake"), new GhostLakeBlock(FabricBlockSettings.method_9630(Blocks.field_10382))
      );
      GHOST_LAKE_BUCKET = (Item)Registry.method_10230(
         Registries.field_41178,
         new Identifier("smfs", "ghost_lake_bucket"),
         new BucketItem(GHOST_LAKE_STILL, new FabricItemSettings().recipeRemainder(Items.field_8550).maxCount(1))
      );
   }
}
