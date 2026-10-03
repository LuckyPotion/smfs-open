package com.xie.smfs.structure;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.JigsawBlock;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.structure.pool.EmptyPoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.pool.StructurePool.Projection;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.structure.Structure.Context;
import net.minecraft.world.gen.structure.Structure.StructurePosition;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class CustomStructurePoolBasedGenerator {
   private static final Logger LOGGER = LogUtils.getLogger();

   private CustomStructurePoolBasedGenerator() {
   }

   public static Optional<StructurePosition> generate(
      Context context,
      RegistryEntry<StructurePool> structurePool,
      Optional<Identifier> id,
      int size,
      BlockPos pos,
      boolean useExpansionHack,
      Optional<Type> projectStartToHeightmap,
      int maxDistanceFromCenter
   ) {
      DynamicRegistryManager dynamicRegistryManager = context.dynamicRegistryManager();
      ChunkGenerator chunkGenerator = context.chunkGenerator();
      StructureTemplateManager structureTemplateManager = context.structureTemplateManager();
      HeightLimitView heightLimitView = context.world();
      ChunkRandom chunkRandom = context.random();
      Registry<StructurePool> registry = dynamicRegistryManager.get(RegistryKeys.TEMPLATE_POOL);
      BlockRotation blockRotation = BlockRotation.random(chunkRandom);
      StructurePool structurePool2 = (StructurePool)structurePool.value();
      StructurePoolElement structurePoolElement = structurePool2.getRandomElement(chunkRandom);
      if (structurePoolElement == EmptyPoolElement.INSTANCE) {
         return Optional.empty();
      }

      BlockPos blockPos;
      if (id.isPresent()) {
         Identifier identifier = id.get();
         Optional<BlockPos> optional = findStartingJigsawPos(structurePoolElement, identifier, pos, blockRotation, structureTemplateManager, chunkRandom);
         if (optional.isEmpty()) {
            LOGGER.error(
               "No starting jigsaw {} found in start pool {}",
               identifier,
               structurePool.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>")
            );
            return Optional.empty();
         }

         blockPos = optional.get();
      } else {
         blockPos = pos;
      }

      Vec3i vec3i = blockPos.subtract(pos);
      BlockPos blockPos2 = pos.subtract(vec3i);
      PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
         structureTemplateManager,
         structurePoolElement,
         blockPos2,
         structurePoolElement.getGroundLevelDelta(),
         blockRotation,
         structurePoolElement.getBoundingBox(structureTemplateManager, blockPos2, blockRotation)
      );
      BlockBox blockBox = poolStructurePiece.getBoundingBox();
      int i = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
      int j = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
      int k;
      if (projectStartToHeightmap.isPresent()) {
         k = pos.getY() + chunkGenerator.getHeightOnGround(i, j, projectStartToHeightmap.get(), heightLimitView, context.noiseConfig());
      } else {
         k = blockPos2.getY();
      }

      int l = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
      poolStructurePiece.translate(0, k - l, 0);
      int m = k + vec3i.getY();
      return Optional.of(
         new StructurePosition(
            new BlockPos(i, m, j),
            collector -> {
               List<PoolStructurePiece> list = Lists.newArrayList();
               list.add(poolStructurePiece);
               if (size > 0) {
                  Box box = new Box(
                     i - maxDistanceFromCenter,
                     m - maxDistanceFromCenter,
                     j - maxDistanceFromCenter,
                     i + maxDistanceFromCenter + 1,
                     m + maxDistanceFromCenter + 1,
                     j + maxDistanceFromCenter + 1
                  );
                  VoxelShape voxelShape = VoxelShapes.combineAndSimplify(
                     VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST
                  );
                  generate(
                     context.noiseConfig(),
                     size,
                     useExpansionHack,
                     chunkGenerator,
                     structureTemplateManager,
                     heightLimitView,
                     chunkRandom,
                     registry,
                     poolStructurePiece,
                     list,
                     voxelShape,
                     maxDistanceFromCenter
                  );
                  list.forEach(collector::addPiece);
               }
            }
         )
      );
   }

   public static boolean generate(
      ServerWorld world, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws, int maxDistanceFromCenter
   ) {
      ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
      StructureTemplateManager structureTemplateManager = world.getStructureTemplateManager();
      StructureAccessor structureAccessor = world.getStructureAccessor();
      Random random = world.getRandom();
      Context context = new Context(
         world.getRegistryManager(),
         chunkGenerator,
         chunkGenerator.getBiomeSource(),
         world.getChunkManager().getNoiseConfig(),
         structureTemplateManager,
         world.getSeed(),
         new ChunkPos(pos),
         world,
         biome -> true
      );
      Optional<StructurePosition> optional = generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), maxDistanceFromCenter);
      if (optional.isPresent()) {
         StructurePiecesCollector structurePiecesCollector = optional.get().generate();

         for (StructurePiece structurePiece : structurePiecesCollector.toList().pieces()) {
            if (structurePiece instanceof PoolStructurePiece poolStructurePiece) {
               poolStructurePiece.generate(world, structureAccessor, chunkGenerator, random, BlockBox.infinite(), pos, keepJigsaws);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static Optional<BlockPos> findStartingJigsawPos(
      StructurePoolElement pool, Identifier id, BlockPos pos, BlockRotation rotation, StructureTemplateManager structureManager, ChunkRandom random
   ) {
      List<StructureBlockInfo> list = pool.getStructureBlockInfos(structureManager, pos, rotation, random);
      Optional<BlockPos> optional = Optional.empty();

      for (StructureBlockInfo structureBlockInfo : list) {
         Identifier identifier = Identifier.tryParse(structureBlockInfo.nbt().getString("name"));
         if (id.equals(identifier)) {
            optional = Optional.of(structureBlockInfo.pos());
            break;
         }
      }

      return optional;
   }

   private static void generate(
      NoiseConfig noiseConfig,
      int maxSize,
      boolean modifyBoundingBox,
      ChunkGenerator chunkGenerator,
      StructureTemplateManager structureTemplateManager,
      HeightLimitView heightLimitView,
      Random random,
      Registry<StructurePool> structurePoolRegistry,
      PoolStructurePiece firstPiece,
      List<PoolStructurePiece> pieces,
      VoxelShape pieceShape,
      int maxDistanceFromCenter
   ) {
      CustomStructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator = new CustomStructurePoolBasedGenerator.StructurePoolGenerator(
         structurePoolRegistry, maxSize, chunkGenerator, structureTemplateManager, pieces, random, maxDistanceFromCenter
      );
      structurePoolGenerator.structurePieces
         .addLast(new CustomStructurePoolBasedGenerator.ShapedPoolStructurePiece(firstPiece, new MutableObject<>(pieceShape), 0));

      while (!structurePoolGenerator.structurePieces.isEmpty()) {
         CustomStructurePoolBasedGenerator.ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
         structurePoolGenerator.generatePiece(
            shapedPoolStructurePiece.piece,
            shapedPoolStructurePiece.pieceShape,
            shapedPoolStructurePiece.currentSize,
            modifyBoundingBox,
            heightLimitView,
            noiseConfig
         );
      }
   }

   static final class ShapedPoolStructurePiece {
      final PoolStructurePiece piece;
      final MutableObject<VoxelShape> pieceShape;
      final int currentSize;

      ShapedPoolStructurePiece(PoolStructurePiece piece, MutableObject<VoxelShape> pieceShape, int currentSize) {
         this.piece = piece;
         this.pieceShape = pieceShape;
         this.currentSize = currentSize;
      }
   }

   static final class StructurePoolGenerator {
      private final Registry<StructurePool> registry;
      private final int maxSize;
      private final ChunkGenerator chunkGenerator;
      private final StructureTemplateManager structureTemplateManager;
      private final List<? super PoolStructurePiece> children;
      private final Random random;
      private final int maxDistanceFromCenter;
      final Deque<CustomStructurePoolBasedGenerator.ShapedPoolStructurePiece> structurePieces = Queues.newArrayDeque();

      StructurePoolGenerator(
         Registry<StructurePool> registry,
         int maxSize,
         ChunkGenerator chunkGenerator,
         StructureTemplateManager structureTemplateManager,
         List<? super PoolStructurePiece> children,
         Random random,
         int maxDistanceFromCenter
      ) {
         this.registry = registry;
         this.maxSize = maxSize;
         this.chunkGenerator = chunkGenerator;
         this.structureTemplateManager = structureTemplateManager;
         this.children = children;
         this.random = random;
         this.maxDistanceFromCenter = maxDistanceFromCenter;
      }

      void generatePiece(
         PoolStructurePiece piece,
         MutableObject<VoxelShape> pieceShape,
         int currentSize,
         boolean modifyBoundingBox,
         HeightLimitView world,
         NoiseConfig noiseConfig
      ) {
         StructurePoolElement structurePoolElement = piece.getPoolElement();
         BlockPos blockPos = piece.getPos();
         BlockRotation blockRotation = piece.getRotation();
         Projection projection = structurePoolElement.getProjection();
         boolean rigid = projection == Projection.RIGID;
         MutableObject<VoxelShape> mutableObject = new MutableObject<>();
         BlockBox blockBox = piece.getBoundingBox();
         int pieceMinY = blockBox.getMinY();

         for (StructureBlockInfo structureBlockInfo : structurePoolElement.getStructureBlockInfos(
            this.structureTemplateManager, blockPos, blockRotation, this.random
         )) {
            Direction direction = JigsawBlock.getFacing(structureBlockInfo.state());
            BlockPos jointPos = structureBlockInfo.pos();
            BlockPos nextJointPos = jointPos.offset(direction);
            int deltaY = jointPos.getY() - pieceMinY;
            RegistryKey<StructurePool> poolKey = getPoolKey(structureBlockInfo);
            Optional<? extends RegistryEntry<StructurePool>> poolOptional = this.registry.getEntry(poolKey);
            if (poolOptional.isEmpty()) {
               CustomStructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", poolKey.getValue());
            } else {
               RegistryEntry<StructurePool> pool = (RegistryEntry<StructurePool>)poolOptional.get();
               if (((StructurePool)pool.value()).getElementCount() == 0 && !pool.matchesKey(StructurePools.EMPTY)) {
                  CustomStructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", poolKey.getValue());
               } else {
                  RegistryEntry<StructurePool> fallbackPool = ((StructurePool)pool.value()).getFallback();
                  if (((StructurePool)fallbackPool.value()).getElementCount() == 0 && !fallbackPool.matchesKey(StructurePools.EMPTY)) {
                     CustomStructurePoolBasedGenerator.LOGGER
                        .warn("Empty or non-existent fallback pool: {}", fallbackPool.getKey().map(key -> key.getValue().toString()).orElse("<unregistered>"));
                  } else {
                     boolean isInsideOriginalBox = blockBox.contains(nextJointPos);
                     MutableObject<VoxelShape> targetShape;
                     if (isInsideOriginalBox) {
                        targetShape = mutableObject;
                        if (targetShape.getValue() == null) {
                           targetShape.setValue(VoxelShapes.cuboid(Box.from(blockBox)));
                        }
                     } else {
                        targetShape = pieceShape;
                     }

                     List<StructurePoolElement> elements = Lists.newArrayList();
                     if (currentSize < this.maxSize) {
                        elements.addAll(((StructurePool)pool.value()).getElementIndicesInRandomOrder(this.random));
                     }

                     elements.addAll(((StructurePool)fallbackPool.value()).getElementIndicesInRandomOrder(this.random));

                     for (StructurePoolElement element : elements) {
                        if (element == EmptyPoolElement.INSTANCE) {
                           break;
                        }

                        for (BlockRotation rotation : BlockRotation.randomRotationOrder(this.random)) {
                           List<StructureBlockInfo> targets = element.getStructureBlockInfos(
                              this.structureTemplateManager, BlockPos.ORIGIN, rotation, this.random
                           );
                           BlockBox targetBox = element.getBoundingBox(this.structureTemplateManager, BlockPos.ORIGIN, rotation);
                           int groundLevelDelta = 0;
                           if (modifyBoundingBox && targetBox.getBlockCountY() <= 16) {
                              groundLevelDelta = targets.stream()
                                 .mapToInt(
                                    targetInfox -> {
                                       if (!targetBox.contains(targetInfox.pos().offset(JigsawBlock.getFacing(targetInfox.state())))) {
                                          return 0;
                                       }

                                       RegistryKey<StructurePool> targetPoolKey = getPoolKey(targetInfox);
                                       Optional<? extends RegistryEntry<StructurePool>> targetPoolOptional = this.registry.getEntry(targetPoolKey);
                                       Optional<RegistryEntry<StructurePool>> fallbackOptional = targetPoolOptional.map(
                                          entry -> ((StructurePool)entry.value()).getFallback()
                                       );
                                       int primaryHeight = targetPoolOptional.<Integer>map(
                                             entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)
                                          )
                                          .orElse(0);
                                       int fallbackHeight = fallbackOptional.<Integer>map(
                                             entry -> ((StructurePool)entry.value()).getHighestY(this.structureTemplateManager)
                                          )
                                          .orElse(0);
                                       return Math.max(primaryHeight, fallbackHeight);
                                    }
                                 )
                                 .max()
                                 .orElse(0);
                           }

                           for (StructureBlockInfo targetInfo : targets) {
                              if (JigsawBlock.attachmentMatches(structureBlockInfo, targetInfo)) {
                                 BlockPos targetJointPos = targetInfo.pos();
                                 BlockPos targetPos = nextJointPos.subtract(targetJointPos);
                                 BlockBox targetBounds = targetBox.offset(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                 int targetMinY = targetPos.getY();
                                 int centerX = (targetBounds.getMaxX() + targetBounds.getMinX()) / 2;
                                 int centerZ = (targetBounds.getMaxZ() + targetBounds.getMinZ()) / 2;
                                 int distanceToCenter = Math.max(
                                    Math.abs(centerX - (piece.getBoundingBox().getMinX() + piece.getBoundingBox().getMaxX()) / 2),
                                    Math.abs(centerZ - (piece.getBoundingBox().getMinZ() + piece.getBoundingBox().getMaxZ()) / 2)
                                 );
                                 if (distanceToCenter <= this.maxDistanceFromCenter) {
                                    BlockBox combinedBounds = new BlockBox(
                                       Math.min(blockBox.getMinX(), targetBounds.getMinX()),
                                       Math.min(blockBox.getMinY(), targetBounds.getMinY()),
                                       Math.min(blockBox.getMinZ(), targetBounds.getMinZ()),
                                       Math.max(blockBox.getMaxX(), targetBounds.getMaxX()),
                                       Math.max(blockBox.getMaxY(), targetBounds.getMaxY()),
                                       Math.max(blockBox.getMaxZ(), targetBounds.getMaxZ())
                                    );
                                    VoxelShape combinedShape = VoxelShapes.combineAndSimplify(
                                       targetShape.getValue(), VoxelShapes.cuboid(Box.from(combinedBounds)), BooleanBiFunction.ONLY_SECOND
                                    );
                                    if (!VoxelShapes.matchesAnywhere(combinedShape, pieceShape.getValue(), BooleanBiFunction.ONLY_SECOND)) {
                                       BlockBox finalBounds;
                                       if (rigid) {
                                          finalBounds = targetBounds.offset(0, -deltaY - groundLevelDelta, 0);
                                       } else {
                                          int adjustedTargetMinY = targetMinY - deltaY;
                                          int yShift = targetBounds.getMinY() - adjustedTargetMinY;
                                          finalBounds = targetBounds.offset(0, -yShift, 0);
                                       }

                                       int nextSize = currentSize + 1;
                                       PoolStructurePiece nextPiece = new PoolStructurePiece(
                                          this.structureTemplateManager, element, targetPos, element.getGroundLevelDelta(), rotation, finalBounds
                                       );
                                       MutableObject<VoxelShape> nextShape = new MutableObject<>(combinedShape);
                                       this.children.add(nextPiece);
                                       this.structurePieces
                                          .addLast(new CustomStructurePoolBasedGenerator.ShapedPoolStructurePiece(nextPiece, nextShape, nextSize));
                                       break;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private static RegistryKey<StructurePool> getPoolKey(StructureBlockInfo jigsaw) {
         return RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.tryParse(jigsaw.nbt().getString("pool")));
      }
   }
}
