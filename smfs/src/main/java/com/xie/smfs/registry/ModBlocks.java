package com.xie.smfs.registry;

import com.xie.smfs.Smfs;
import com.xie.smfs.block.DeepDefiledOreBlock;
import com.xie.smfs.block.DefiledOreBlock;
import com.xie.smfs.block.DirtyCropBlock;
import com.xie.smfs.block.FilthyCropBlock;
import com.xie.smfs.block.Footprint2Block;
import com.xie.smfs.block.FootprintBlock;
import com.xie.smfs.block.GhostBedBlock;
import com.xie.smfs.block.GhostCandleBlock;
import com.xie.smfs.block.GhostCoffinBlock;
import com.xie.smfs.block.GhostDoorBlock;
import com.xie.smfs.block.GhostFurnaceBlock;
import com.xie.smfs.block.GhostMirrorBlock;
import com.xie.smfs.block.GhostPianoBlock;
import com.xie.smfs.block.GhostPortraitBlock;
import com.xie.smfs.block.GhostScreenBlock;
import com.xie.smfs.block.GhostSkeletonBlock;
import com.xie.smfs.block.GhostTable2Block;
import com.xie.smfs.block.GhostTableBlock;
import com.xie.smfs.block.GoldCoffinBlock;
import com.xie.smfs.block.GraveMoundBlock;
import com.xie.smfs.block.NewGhostDoorBlock;
import com.xie.smfs.block.RedCoffinBlock;
import com.xie.smfs.block.SpiritBrewingStandBlock;
import com.xie.smfs.block.UnbreakableRedWoolBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
   public static final Block GHOST_COFFIN = register(
      "ghost_coffin", new GhostCoffinBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block RED_COFFIN = register(
      "red_coffin", new RedCoffinBlock(Settings.method_9630(Blocks.field_22126).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GOLD_COFFIN = register(
      "gold_coffin", new GoldCoffinBlock(Settings.method_9630(Blocks.field_10205).method_22488().method_9626(BlockSoundGroup.field_11533))
   );
   public static final Block GHOST_PIANO = register(
      "ghost_piano", new GhostPianoBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_DOOR = register(
      "ghost_door", new GhostDoorBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block NEW_GHOST_DOOR = register(
      "new_ghost_door", new NewGhostDoorBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_BED = register(
      "ghost_bed", new GhostBedBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_TABLE = register(
      "ghost_table", new GhostTableBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_TABLE2 = register(
      "ghost_table2", new GhostTable2Block(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_SCREEN = register(
      "ghost_screen", new GhostScreenBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_CANDLE = register(
      "ghost_candle", new GhostCandleBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_SKELETON = register(
      "ghost_skeleton", new GhostSkeletonBlock(Settings.method_9630(Blocks.field_10166).method_22488().method_9626(BlockSoundGroup.field_22149))
   );
   public static final Block GHOST_PORTRAIT = register(
      "ghost_portrait", new GhostPortraitBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block GHOST_MIRROR = register(
      "ghost_mirror", new GhostMirrorBlock(Settings.method_9630(Blocks.field_10033).method_22488().method_9626(BlockSoundGroup.field_11537))
   );
   public static final Block FOOTPRINT = register(
      "footprint", new FootprintBlock(Settings.method_9630(Blocks.field_10340).method_22488().method_9634().method_9626(BlockSoundGroup.field_11544))
   );
   public static final Block FOOTPRINT2 = register(
      "footprint2", new Footprint2Block(Settings.method_9630(Blocks.field_10340).method_22488().method_9634().method_9626(BlockSoundGroup.field_11544))
   );
   public static final Block DEFILED_ORE = register(
      "defiled_ore", new DefiledOreBlock(Settings.method_9630(Blocks.field_10161).method_9629(3.0F, 3.0F).method_9626(BlockSoundGroup.field_11544))
   );
   public static final Block DEEP_DEFILED_ORE = register(
      "deep_defiled_ore", new DeepDefiledOreBlock(Settings.method_9630(Blocks.field_10161).method_9629(4.0F, 4.0F).method_9626(BlockSoundGroup.field_29033))
   );
   public static final Block GHOST_FURNACE = register(
      "ghost_furnace", new GhostFurnaceBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block SPIRIT_BREWING_STAND = register(
      "spirit_brewing_stand", new SpiritBrewingStandBlock(Settings.method_9630(Blocks.field_10161).method_22488().method_9626(BlockSoundGroup.field_11547))
   );
   public static final Block DIRTY_CROP = register(
      "dirty_crop",
      new DirtyCropBlock(
         Settings.method_9630(Blocks.field_10293).method_22488().method_9634().method_9640().method_9618().method_9626(BlockSoundGroup.field_17580)
      )
   );
   public static final Block FILTHY_CROP = register(
      "filthy_crop",
      new FilthyCropBlock(
         Settings.method_9630(Blocks.field_10293).method_22488().method_9634().method_9640().method_9618().method_9626(BlockSoundGroup.field_17580)
      )
   );
   public static final Block UNBREAKABLE_RED_WOOL = register(
      "unbreakable_red_wool",
      new UnbreakableRedWoolBlock(Settings.method_9630(Blocks.field_10314).method_9629(-1.0F, 3600000.0F).method_9626(BlockSoundGroup.field_11543))
   );
   public static final Block GRAVE_MOUND = register(
      "grave_mound", new GraveMoundBlock(Settings.method_9630(Blocks.field_10566).method_22488().method_9634().method_9626(BlockSoundGroup.field_11529))
   );

   private static Block register(String name, Block block) {
      return (Block)Registry.method_10230(Registries.field_41175, new Identifier("smfs", name), block);
   }

   public static void registerBlocks() {
      Smfs.LOGGER.info("Registering Mod Blocks for {}", "smfs");
   }
}
