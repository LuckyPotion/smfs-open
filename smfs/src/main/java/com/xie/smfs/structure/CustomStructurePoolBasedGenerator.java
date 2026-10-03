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
      DynamicRegistryManager dynamicRegistryManager = context.comp_561();
      ChunkGenerator chunkGenerator = context.comp_562();
      StructureTemplateManager structureTemplateManager = context.comp_565();
      HeightLimitView heightLimitView = context.comp_569();
      ChunkRandom chunkRandom = context.comp_566();
      Registry<StructurePool> registry = dynamicRegistryManager.method_30530(RegistryKeys.field_41249);
      BlockRotation blockRotation = BlockRotation.method_16548(chunkRandom);
      StructurePool structurePool2 = (StructurePool)structurePool.comp_349();
      StructurePoolElement structurePoolElement = structurePool2.method_16631(chunkRandom);
      if (structurePoolElement == EmptyPoolElement.field_16663) {
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
               structurePool.method_40230().map(key -> key.method_29177().toString()).orElse("<unregistered>")
            );
            return Optional.empty();
         }

         blockPos = optional.get();
      } else {
         blockPos = pos;
      }

      Vec3i vec3i = blockPos.method_10059(pos);
      BlockPos blockPos2 = pos.method_10059(vec3i);
      PoolStructurePiece poolStructurePiece = new PoolStructurePiece(
         structureTemplateManager,
         structurePoolElement,
         blockPos2,
         structurePoolElement.method_19308(),
         blockRotation,
         structurePoolElement.method_16628(structureTemplateManager, blockPos2, blockRotation)
      );
      BlockBox blockBox = poolStructurePiece.method_14935();
      int i = (blockBox.method_35418() + blockBox.method_35415()) / 2;
      int j = (blockBox.method_35420() + blockBox.method_35417()) / 2;
      int k;
      if (projectStartToHeightmap.isPresent()) {
         k = pos.method_10264() + chunkGenerator.method_20402(i, j, projectStartToHeightmap.get(), heightLimitView, context.comp_564());
      } else {
         k = blockPos2.method_10264();
      }

      int l = blockBox.method_35416() + poolStructurePiece.method_16646();
      poolStructurePiece.method_14922(0, k - l, 0);
      int m = k + vec3i.method_10264();
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
                  VoxelShape voxelShape = VoxelShapes.method_1072(
                     VoxelShapes.method_1078(box), VoxelShapes.method_1078(Box.method_19316(blockBox)), BooleanBiFunction.field_16886
                  );
                  generate(
                     context.comp_564(),
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
                  list.forEach(collector::method_35462);
               }
            }
         )
      );
   }

   public static boolean generate(
      ServerWorld world, RegistryEntry<StructurePool> structurePool, Identifier id, int size, BlockPos pos, boolean keepJigsaws, int maxDistanceFromCenter
   ) {
      ChunkGenerator chunkGenerator = world.method_14178().method_12129();
      StructureTemplateManager structureTemplateManager = world.method_14183();
      StructureAccessor structureAccessor = world.method_27056();
      Random random = world.method_8409();
      Context context = new Context(
         world.method_30349(),
         chunkGenerator,
         chunkGenerator.method_12098(),
         world.method_14178().method_41248(),
         structureTemplateManager,
         world.method_8412(),
         new ChunkPos(pos),
         world,
         biome -> true
      );
      Optional<StructurePosition> optional = generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), maxDistanceFromCenter);
      if (optional.isPresent()) {
         StructurePiecesCollector structurePiecesCollector = optional.get().method_44019();

         for (StructurePiece structurePiece : structurePiecesCollector.method_38714().comp_132()) {
            if (structurePiece instanceof PoolStructurePiece poolStructurePiece) {
               poolStructurePiece.method_27236(world, structureAccessor, chunkGenerator, random, BlockBox.method_14665(), pos, keepJigsaws);
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
      List<StructureBlockInfo> list = pool.method_16627(structureManager, pos, rotation, random);
      Optional<BlockPos> optional = Optional.empty();

      for (StructureBlockInfo structureBlockInfo : list) {
         Identifier identifier = Identifier.method_12829(structureBlockInfo.comp_1343().method_10558("name"));
         if (id.equals(identifier)) {
            optional = Optional.of(structureBlockInfo.comp_1341());
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
         StructurePoolElement structurePoolElement = piece.method_16644();
         BlockPos blockPos = piece.method_16648();
         BlockRotation blockRotation = piece.method_16888();
         Projection projection = structurePoolElement.method_16624();
         boolean rigid = projection == Projection.field_16687;
         MutableObject<VoxelShape> mutableObject = new MutableObject<>();
         BlockBox blockBox = piece.method_14935();
         int pieceMinY = blockBox.method_35416();

         for (StructureBlockInfo structureBlockInfo : structurePoolElement.method_16627(this.structureTemplateManager, blockPos, blockRotation, this.random)) {
            Direction direction = JigsawBlock.method_26378(structureBlockInfo.comp_1342());
            BlockPos jointPos = structureBlockInfo.comp_1341();
            BlockPos nextJointPos = jointPos.method_10093(direction);
            int deltaY = jointPos.method_10264() - pieceMinY;
            RegistryKey<StructurePool> poolKey = getPoolKey(structureBlockInfo);
            Optional<? extends RegistryEntry<StructurePool>> poolOptional = this.registry.method_40264(poolKey);
            if (poolOptional.isEmpty()) {
               CustomStructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", poolKey.method_29177());
            } else {
               RegistryEntry<StructurePool> pool = (RegistryEntry<StructurePool>)poolOptional.get();
               if (((StructurePool)pool.comp_349()).method_16632() == 0 && !pool.method_40225(StructurePools.field_26254)) {
                  CustomStructurePoolBasedGenerator.LOGGER.warn("Empty or non-existent pool: {}", poolKey.method_29177());
               } else {
                  RegistryEntry<StructurePool> fallbackPool = ((StructurePool)pool.comp_349()).method_46736();
                  if (((StructurePool)fallbackPool.comp_349()).method_16632() == 0 && !fallbackPool.method_40225(StructurePools.field_26254)) {
                     CustomStructurePoolBasedGenerator.LOGGER
                        .warn(
                           "Empty or non-existent fallback pool: {}",
                           fallbackPool.method_40230().map(key -> key.method_29177().toString()).orElse("<unregistered>")
                        );
                  } else {
                     boolean isInsideOriginalBox = blockBox.method_14662(nextJointPos);
                     MutableObject<VoxelShape> targetShape;
                     if (isInsideOriginalBox) {
                        targetShape = mutableObject;
                        if (targetShape.getValue() == null) {
                           targetShape.setValue(VoxelShapes.method_1078(Box.method_19316(blockBox)));
                        }
                     } else {
                        targetShape = pieceShape;
                     }

                     List<StructurePoolElement> elements = Lists.newArrayList();
                     if (currentSize < this.maxSize) {
                        elements.addAll(((StructurePool)pool.comp_349()).method_16633(this.random));
                     }

                     elements.addAll(((StructurePool)fallbackPool.comp_349()).method_16633(this.random));

                     for (StructurePoolElement element : elements) {
                        if (element == EmptyPoolElement.field_16663) {
                           break;
                        }

                        for (BlockRotation rotation : BlockRotation.method_16547(this.random)) {
                           List<StructureBlockInfo> targets = element.method_16627(this.structureTemplateManager, BlockPos.field_10980, rotation, this.random);
                           BlockBox targetBox = element.method_16628(this.structureTemplateManager, BlockPos.field_10980, rotation);
                           int groundLevelDelta = 0;
                           if (modifyBoundingBox && targetBox.method_14660() <= 16) {
                              groundLevelDelta = targets.stream()
                                 .mapToInt(
                                    targetInfox -> {
                                       if (!targetBox.method_14662(targetInfox.comp_1341().method_10093(JigsawBlock.method_26378(targetInfox.comp_1342())))) {
                                          return 0;
                                       }

                                       RegistryKey<StructurePool> targetPoolKey = getPoolKey(targetInfox);
                                       Optional<? extends RegistryEntry<StructurePool>> targetPoolOptional = this.registry.method_40264(targetPoolKey);
                                       Optional<RegistryEntry<StructurePool>> fallbackOptional = targetPoolOptional.map(
                                          entry -> ((StructurePool)entry.comp_349()).method_46736()
                                       );
                                       int primaryHeight = targetPoolOptional.<Integer>map(
                                             entry -> ((StructurePool)entry.comp_349()).method_19309(this.structureTemplateManager)
                                          )
                                          .orElse(0);
                                       int fallbackHeight = fallbackOptional.<Integer>map(
                                             entry -> ((StructurePool)entry.comp_349()).method_19309(this.structureTemplateManager)
                                          )
                                          .orElse(0);
                                       return Math.max(primaryHeight, fallbackHeight);
                                    }
                                 )
                                 .max()
                                 .orElse(0);
                           }

                           for (StructureBlockInfo targetInfo : targets) {
                              if (JigsawBlock.method_16546(structureBlockInfo, targetInfo)) {
                                 BlockPos targetJointPos = targetInfo.comp_1341();
                                 BlockPos targetPos = nextJointPos.method_10059(targetJointPos);
                                 BlockBox targetBounds = targetBox.method_19311(targetPos.method_10263(), targetPos.method_10264(), targetPos.method_10260());
                                 int targetMinY = targetPos.method_10264();
                                 int centerX = (targetBounds.method_35418() + targetBounds.method_35415()) / 2;
                                 int centerZ = (targetBounds.method_35420() + targetBounds.method_35417()) / 2;
                                 int distanceToCenter = Math.max(
                                    Math.abs(centerX - (piece.method_14935().method_35415() + piece.method_14935().method_35418()) / 2),
                                    Math.abs(centerZ - (piece.method_14935().method_35417() + piece.method_14935().method_35420()) / 2)
                                 );
                                 if (distanceToCenter <= this.maxDistanceFromCenter) {
                                    BlockBox combinedBounds = new BlockBox(
                                       Math.min(blockBox.method_35415(), targetBounds.method_35415()),
                                       Math.min(blockBox.method_35416(), targetBounds.method_35416()),
                                       Math.min(blockBox.method_35417(), targetBounds.method_35417()),
                                       Math.max(blockBox.method_35418(), targetBounds.method_35418()),
                                       Math.max(blockBox.method_35419(), targetBounds.method_35419()),
                                       Math.max(blockBox.method_35420(), targetBounds.method_35420())
                                    );
                                    VoxelShape combinedShape = VoxelShapes.method_1072(
                                       targetShape.getValue(), VoxelShapes.method_1078(Box.method_19316(combinedBounds)), BooleanBiFunction.field_16893
                                    );
                                    if (!VoxelShapes.method_1074(combinedShape, pieceShape.getValue(), BooleanBiFunction.field_16893)) {
                                       BlockBox finalBounds;
                                       if (rigid) {
                                          finalBounds = targetBounds.method_19311(0, -deltaY - groundLevelDelta, 0);
                                       } else {
                                          int adjustedTargetMinY = targetMinY - deltaY;
                                          int yShift = targetBounds.method_35416() - adjustedTargetMinY;
                                          finalBounds = targetBounds.method_19311(0, -yShift, 0);
                                       }

                                       int nextSize = currentSize + 1;
                                       PoolStructurePiece nextPiece = new PoolStructurePiece(
                                          this.structureTemplateManager, element, targetPos, element.method_19308(), rotation, finalBounds
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
         return RegistryKey.method_29179(RegistryKeys.field_41249, Identifier.method_12829(jigsaw.comp_1343().method_10558("pool")));
      }
   }
}
