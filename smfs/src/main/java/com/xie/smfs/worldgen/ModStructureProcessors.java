package com.xie.smfs.worldgen;

import com.xie.smfs.Smfs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;

public class ModStructureProcessors {
   public static final StructureProcessorType<ChestLootProcessor> CHEST_LOOT_PROCESSOR = (StructureProcessorType<ChestLootProcessor>)Registry.register(
      Registries.STRUCTURE_PROCESSOR, new Identifier("smfs", "chest_loot_processor"), (StructureProcessorType)() -> ChestLootProcessor.CODEC
   );
   public static final StructureProcessorType<GhostSpawnProcessor> GHOST_SPAWN_PROCESSOR = (StructureProcessorType<GhostSpawnProcessor>)Registry.register(
      Registries.STRUCTURE_PROCESSOR, new Identifier("smfs", "ghost_spawn"), (StructureProcessorType)() -> GhostSpawnProcessor.CODEC
   );

   public static void register() {
      Smfs.LOGGER.info("CHEST_LOOT_PROCESSOR 已注册: {}", CHEST_LOOT_PROCESSOR);
      Smfs.LOGGER.info("GHOST_SPAWN_PROCESSOR 已注册: {}", GHOST_SPAWN_PROCESSOR);
   }
}
