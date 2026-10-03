package com.xie.smfs.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xie.smfs.Smfs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ChestLootProcessor extends StructureProcessor {
   public static final Codec<ChestLootProcessor> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(Codec.STRING.fieldOf("loot_table").forGetter(processor -> processor.lootTable)).apply(instance, ChestLootProcessor::new)
   );
   private final String lootTable;
   private static int chestCount = 0;

   public ChestLootProcessor(String lootTable) {
      this.lootTable = lootTable;
   }

   @Nullable
   public StructureBlockInfo method_15110(
      WorldView world, BlockPos pos, BlockPos pivot, StructureBlockInfo originalBlockInfo, StructureBlockInfo currentBlockInfo, StructurePlacementData data
   ) {
      BlockState state = currentBlockInfo.comp_1342();
      if (state.method_27852(Blocks.field_10034) || state.method_27852(Blocks.field_10380)) {
         chestCount++;
         if (chestCount % 100 == 0) {
            Smfs.LOGGER.debug("[ChestLootProcessor] 已处理 {} 个箱子（战利品表: {}）", chestCount, this.lootTable);
         }

         NbtCompound nbt = currentBlockInfo.comp_1343() != null ? currentBlockInfo.comp_1343().method_10553() : new NbtCompound();
         if (this.lootTable != null && !this.lootTable.isEmpty()) {
            nbt.method_10582("LootTable", this.lootTable);
            nbt.method_10544("LootTableSeed", currentBlockInfo.comp_1341().method_10063());
            nbt.method_10582("id", "minecraft:chest");
            return new StructureBlockInfo(currentBlockInfo.comp_1341(), currentBlockInfo.comp_1342(), nbt);
         }
      }

      return currentBlockInfo;
   }

   protected StructureProcessorType<?> method_16772() {
      return ModStructureProcessors.CHEST_LOOT_PROCESSOR;
   }
}
