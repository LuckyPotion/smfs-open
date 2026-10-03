package com.xie.smfs.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xie.smfs.registry.ModStructureType;
import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.world.gen.structure.Structure.Config;
import net.minecraft.world.gen.structure.Structure.Context;
import net.minecraft.world.gen.structure.Structure.StructurePosition;

public class CustomJigsawStructure extends Structure {
   public static final Codec<CustomJigsawStructure> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            configCodecBuilder(instance),
            StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
            Codec.intRange(0, 7).fieldOf("size").forGetter(structure -> structure.size),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack),
            Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.intRange(1, 512).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
         )
         .apply(instance, CustomJigsawStructure::new)
   );
   private final RegistryEntry<StructurePool> startPool;
   private final Optional<Identifier> startJigsawName;
   private final int size;
   private final HeightProvider startHeight;
   private final boolean useExpansionHack;
   private final Optional<Type> projectStartToHeightmap;
   private final int maxDistanceFromCenter;

   public CustomJigsawStructure(
      Config config,
      RegistryEntry<StructurePool> startPool,
      Optional<Identifier> startJigsawName,
      int size,
      HeightProvider startHeight,
      boolean useExpansionHack,
      Optional<Type> projectStartToHeightmap,
      int maxDistanceFromCenter
   ) {
      super(config);
      this.startPool = startPool;
      this.startJigsawName = startJigsawName;
      this.size = size;
      this.startHeight = startHeight;
      this.useExpansionHack = useExpansionHack;
      this.projectStartToHeightmap = projectStartToHeightmap;
      this.maxDistanceFromCenter = maxDistanceFromCenter;
   }

   public Optional<StructurePosition> getStructurePosition(Context context) {
      ChunkPos chunkPos = context.chunkPos();
      int i = this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world()));
      BlockPos blockPos = new BlockPos(chunkPos.getStartX(), i, chunkPos.getStartZ());
      return CustomStructurePoolBasedGenerator.generate(
         context, this.startPool, this.startJigsawName, this.size, blockPos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter
      );
   }

   public StructureType<?> getType() {
      return ModStructureType.CUSTOM_JIGSAW;
   }
}
