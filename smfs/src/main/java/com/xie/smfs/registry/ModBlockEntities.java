package com.xie.smfs.registry;

import com.xie.smfs.Smfs;
import com.xie.smfs.block.entity.Footprint2BlockEntity;
import com.xie.smfs.block.entity.FootprintBlockEntity;
import com.xie.smfs.block.entity.GhostCoffinBlockEntity;
import com.xie.smfs.block.entity.GhostFurnaceBlockEntity;
import com.xie.smfs.block.entity.GoldCoffinBlockEntity;
import com.xie.smfs.block.entity.NewGhostDoorBlockEntity;
import com.xie.smfs.block.entity.RedCoffinBlockEntity;
import com.xie.smfs.block.entity.SpiritBrewingStandBlockEntity;
import com.xie.smfs.client.renderer.StaticAnimatable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
   public static BlockEntityType<GhostCoffinBlockEntity> GHOST_COFFIN_BLOCK_ENTITY;
   public static BlockEntityType<RedCoffinBlockEntity> RED_COFFIN_BLOCK_ENTITY;
   public static BlockEntityType<GoldCoffinBlockEntity> GOLD_COFFIN_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_PIANO_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_DOOR_BLOCK_ENTITY;
   public static BlockEntityType<NewGhostDoorBlockEntity> NEW_GHOST_DOOR_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_BED_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_TABLE_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_TABLE2_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_SCREEN_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_CANDLE_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_SKELETON_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_PORTRAIT_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GHOST_MIRROR_BLOCK_ENTITY;
   public static BlockEntityType<GhostFurnaceBlockEntity> GHOST_FURNACE_BLOCK_ENTITY;
   public static BlockEntityType<FootprintBlockEntity> FOOTPRINT_BLOCK_ENTITY;
   public static BlockEntityType<Footprint2BlockEntity> FOOTPRINT2_BLOCK_ENTITY;
   public static BlockEntityType<SpiritBrewingStandBlockEntity> SPIRIT_BREWING_STAND_BLOCK_ENTITY;
   public static BlockEntityType<StaticAnimatable> GRAVE_MOUND_BLOCK_ENTITY;

   public static void registerBlockEntities() {
      GHOST_COFFIN_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(GhostCoffinBlockEntity::new, new Block[]{ModBlocks.GHOST_COFFIN}).build();
      RED_COFFIN_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(RedCoffinBlockEntity::new, new Block[]{ModBlocks.RED_COFFIN}).build();
      GOLD_COFFIN_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(GoldCoffinBlockEntity::new, new Block[]{ModBlocks.GOLD_COFFIN}).build();
      GHOST_PIANO_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_PIANO}).build();
      GHOST_DOOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_DOOR}).build();
      NEW_GHOST_DOOR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(NewGhostDoorBlockEntity::new, new Block[]{ModBlocks.NEW_GHOST_DOOR}).build();
      GHOST_BED_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_BED}).build();
      GHOST_TABLE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_TABLE}).build();
      GHOST_TABLE2_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_TABLE2}).build();
      GHOST_SCREEN_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_SCREEN}).build();
      GHOST_CANDLE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_CANDLE}).build();
      GHOST_SKELETON_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_SKELETON}).build();
      GHOST_PORTRAIT_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_PORTRAIT}).build();
      GHOST_MIRROR_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GHOST_MIRROR}).build();
      FOOTPRINT_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(FootprintBlockEntity::new, new Block[]{ModBlocks.FOOTPRINT}).build();
      FOOTPRINT2_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(Footprint2BlockEntity::new, new Block[]{ModBlocks.FOOTPRINT2}).build();
      GHOST_FURNACE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(GhostFurnaceBlockEntity::new, new Block[]{ModBlocks.GHOST_FURNACE}).build();
      SPIRIT_BREWING_STAND_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(SpiritBrewingStandBlockEntity::new, new Block[]{ModBlocks.SPIRIT_BREWING_STAND})
         .build();
      GRAVE_MOUND_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(StaticAnimatable::new, new Block[]{ModBlocks.GRAVE_MOUND}).build();
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_coffin_block_entity"), GHOST_COFFIN_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "red_coffin_block_entity"), RED_COFFIN_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "gold_coffin_block_entity"), GOLD_COFFIN_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_piano_block_entity"), GHOST_PIANO_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_door_block_entity"), GHOST_DOOR_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "new_ghost_door_block_entity"), NEW_GHOST_DOOR_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_bed_block_entity"), GHOST_BED_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_table_block_entity"), GHOST_TABLE_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_table2_block_entity"), GHOST_TABLE2_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_screen_block_entity"), GHOST_SCREEN_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_candle_block_entity"), GHOST_CANDLE_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_skeleton_block_entity"), GHOST_SKELETON_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_portrait_block_entity"), GHOST_PORTRAIT_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_mirror_block_entity"), GHOST_MIRROR_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "footprint_block_entity"), FOOTPRINT_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "footprint2_block_entity"), FOOTPRINT2_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "ghost_furnace_block_entity"), GHOST_FURNACE_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "spirit_brewing_stand_block_entity"), SPIRIT_BREWING_STAND_BLOCK_ENTITY);
      Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("smfs", "grave_mound_block_entity"), GRAVE_MOUND_BLOCK_ENTITY);
      Smfs.LOGGER.info("Registered all coffin block entities");
   }
}
