package com.xie.smfs.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xie.smfs.Smfs;
import com.xie.smfs.mixin.server.ChunkRegionAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class GhostSpawnProcessor extends StructureProcessor {
   public static final Codec<GhostSpawnProcessor> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.listOf().fieldOf("entity_types").forGetter(p -> p.entityTypeIds),
            Codec.INT.optionalFieldOf("count", 3).forGetter(p -> p.count),
            Codec.INT.optionalFieldOf("spread", 5).forGetter(p -> p.spread),
            Codec.BOOL.optionalFieldOf("from_corner", false).forGetter(p -> p.fromCorner),
            Codec.INT.optionalFieldOf("dir_x", 1).forGetter(p -> p.dirX),
            Codec.INT.optionalFieldOf("dir_z", 1).forGetter(p -> p.dirZ),
            Codec.STRING.optionalFieldOf("height_mode", "relative").forGetter(p -> p.heightMode),
            Codec.INT.optionalFieldOf("height_value", 4).forGetter(p -> p.heightValue),
            GhostSpawnProcessor.SpawnBlockConfig.CODEC.listOf().optionalFieldOf("spawn_blocks", List.of()).forGetter(p -> p.spawnBlocks)
         )
         .apply(instance, GhostSpawnProcessor::new)
   );
   private final List<String> entityTypeIds;
   private final int count;
   private final int spread;
   private final boolean fromCorner;
   private final int dirX;
   private final int dirZ;
   private final String heightMode;
   private final int heightValue;
   private final List<GhostSpawnProcessor.SpawnBlockConfig> spawnBlocks;
   private boolean spawned = false;
   private final List<BlockPos> matchingPositions = new ArrayList<>();
   private BlockPos lastPivot = null;

   public GhostSpawnProcessor(
      List<String> entityTypeIds,
      int count,
      int spread,
      boolean fromCorner,
      int dirX,
      int dirZ,
      String heightMode,
      int heightValue,
      List<GhostSpawnProcessor.SpawnBlockConfig> spawnBlocks
   ) {
      this.entityTypeIds = entityTypeIds;
      this.count = count;
      this.spread = spread;
      this.fromCorner = fromCorner;
      this.dirX = dirX;
      this.dirZ = dirZ;
      this.heightMode = heightMode;
      this.heightValue = heightValue;
      this.spawnBlocks = spawnBlocks;
   }

   @Nullable
   public StructureBlockInfo process(
      WorldView world, BlockPos pos, BlockPos pivot, StructureBlockInfo originalBlockInfo, StructureBlockInfo currentBlockInfo, StructurePlacementData data
   ) {
      if (this.lastPivot == null || !this.lastPivot.equals(pivot)) {
         this.spawned = false;
         this.matchingPositions.clear();
         this.lastPivot = pivot;
      }

      if (!this.spawned && !this.spawnBlocks.isEmpty()) {
         for (GhostSpawnProcessor.SpawnBlockConfig cfg : this.spawnBlocks) {
            if (currentBlockInfo.state().isOf(cfg.block)) {
               this.matchingPositions.add(currentBlockInfo.pos());
               break;
            }
         }
      } else if (!this.spawned && world instanceof ChunkRegion chunkRegion) {
         Random random = new Random();
         int actualDirX = this.dirX;
         int actualDirZ = this.dirZ;
         if (this.fromCorner) {
            BlockRotation rotation = data.getRotation();
            switch (rotation) {
               case CLOCKWISE_90:
                  actualDirX = -this.dirZ;
                  actualDirZ = this.dirX;
                  break;
               case CLOCKWISE_180:
                  actualDirX = -this.dirX;
                  actualDirZ = -this.dirZ;
                  break;
               case COUNTERCLOCKWISE_90:
                  actualDirX = this.dirZ;
                  actualDirZ = -this.dirX;
            }
         }

         for (int i = 0; i < this.count; i++) {
            String typeId = this.entityTypeIds.get(random.nextInt(this.entityTypeIds.size()));
            EntityType<?> type = (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier(typeId));
            if (type != null) {
               Entity entity = type.create(getServerWorld(chunkRegion));
               if (entity != null) {
                  int attempts = 0;

                  int spawnX;
                  int spawnZ;
                  int spawnY;
                  do {
                     int dx = this.fromCorner
                        ? actualDirX * (this.spread > 0 ? random.nextInt(this.spread + 1) : 0)
                        : (this.spread > 0 ? random.nextInt(this.spread * 2 + 1) - this.spread : 0);
                     int dz = this.fromCorner
                        ? actualDirZ * (this.spread > 0 ? random.nextInt(this.spread + 1) : 0)
                        : (this.spread > 0 ? random.nextInt(this.spread * 2 + 1) - this.spread : 0);
                     spawnX = pos.getX() + dx;
                     spawnZ = pos.getZ() + dz;
                     spawnY = pos.getY();
                     Mutable checkPos = new Mutable(spawnX, spawnY, spawnZ);

                     while (checkPos.getY() > world.getBottomY() && !world.getBlockState(checkPos.move(0, -1, 0)).isSolid()) {
                     }

                     spawnY = "absolute".equals(this.heightMode) ? this.heightValue : checkPos.getY() + this.heightValue;
                  } while (spawnY < pos.getY() - 10 && ++attempts < 10);

                  entity.refreshPositionAndAngles(spawnX + 0.5, spawnY, spawnZ + 0.5, 0.0F, 0.0F);
                  chunkRegion.spawnEntityAndPassengers(entity);
                  Smfs.LOGGER.debug("[GhostSpawnProcessor] {} 生成于 [{}, {}, {}]", entity.getName().getString(), spawnX, spawnY, spawnZ);
               }
            }
         }

         this.spawned = true;
      }

      return currentBlockInfo;
   }

   public void finalize(WorldAccess world) {
      if (!this.spawned && !this.spawnBlocks.isEmpty() && !this.matchingPositions.isEmpty()) {
         this.spawned = true;
         Random random = new Random();
         List<BlockPos> shuffled = new ArrayList<>(this.matchingPositions);
         Collections.shuffle(shuffled, random);
         int spawnedCount = 0;

         for (BlockPos matchPos : shuffled) {
            if (spawnedCount >= this.count) {
               break;
            }

            String typeId = this.entityTypeIds.get(random.nextInt(this.entityTypeIds.size()));
            EntityType<?> type = (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier(typeId));
            if (type != null) {
               GhostSpawnProcessor.SpawnBlockConfig matchedCfg = this.spawnBlocks.get(0);
               Iterator entity = this.spawnBlocks.iterator();

               while (true) {
                  if (entity.hasNext()) {
                     GhostSpawnProcessor.SpawnBlockConfig cfg = (GhostSpawnProcessor.SpawnBlockConfig)entity.next();
                     if (!world.getBlockState(matchPos).isOf(cfg.block)) {
                        continue;
                     }

                     matchedCfg = cfg;
                  }

                  Entity entityx = createEntity(world, type);
                  if (entityx != null) {
                     int spawnX = matchPos.getX() + matchedCfg.offsetX;
                     int spawnY = matchPos.getY() + matchedCfg.offsetY;
                     int spawnZ = matchPos.getZ() + matchedCfg.offsetZ;
                     entityx.refreshPositionAndAngles(spawnX + 0.5, spawnY, spawnZ + 0.5, 0.0F, 0.0F);
                     if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnEntityAndPassengers(entityx);
                     } else if (world instanceof ChunkRegion chunkRegion) {
                        chunkRegion.spawnEntityAndPassengers(entityx);
                     }

                     Smfs.LOGGER.debug("[GhostSpawnProcessor] {} 生成于 [{}, {}, {}]", entityx.getName().getString(), spawnX, spawnY, spawnZ);
                     spawnedCount++;
                  }
                  break;
               }
            }
         }
      }
   }

   @Nullable
   private static Entity createEntity(WorldAccess world, EntityType<?> type) {
      if (world instanceof ServerWorld serverWorld) {
         return type.create(serverWorld);
      } else {
         return world instanceof ChunkRegion chunkRegion ? type.create(getServerWorld(chunkRegion)) : null;
      }
   }

   protected StructureProcessorType<?> getType() {
      return ModStructureProcessors.GHOST_SPAWN_PROCESSOR;
   }

   private static ServerWorld getServerWorld(ChunkRegion chunkRegion) {
      return ((ChunkRegionAccessor)chunkRegion).getWorld();
   }

   public record SpawnBlockConfig(Block block, int offsetX, int offsetY, int offsetZ) {
      public static final Codec<GhostSpawnProcessor.SpawnBlockConfig> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Registries.BLOCK.getCodec().fieldOf("block").forGetter(GhostSpawnProcessor.SpawnBlockConfig::block),
               Codec.INT.optionalFieldOf("offset_x", 0).forGetter(GhostSpawnProcessor.SpawnBlockConfig::offsetX),
               Codec.INT.optionalFieldOf("offset_y", 1).forGetter(GhostSpawnProcessor.SpawnBlockConfig::offsetY),
               Codec.INT.optionalFieldOf("offset_z", 0).forGetter(GhostSpawnProcessor.SpawnBlockConfig::offsetZ)
            )
            .apply(instance, GhostSpawnProcessor.SpawnBlockConfig::new)
      );
   }
}
