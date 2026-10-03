package com.xie.smfs.block;

import com.xie.smfs.Smfs;
import com.xie.smfs.block.entity.NewGhostDoorBlockEntity;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.manager.GhostSpawnManager;
import com.xie.smfs.registry.ModEntities;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NewGhostDoorBlock extends GhostFurnitureBlock {
   public static final BooleanProperty OPEN = BooleanProperty.method_11825("open");
   private static final VoxelShape SHAPE_NORTH = Block.method_9541(0.0, 0.0, 6.5, 16.0, 32.0, 9.5);
   private static final VoxelShape SHAPE_SOUTH = Block.method_9541(0.0, 0.0, 6.5, 16.0, 32.0, 9.5);
   private static final VoxelShape SHAPE_EAST = Block.method_9541(6.5, 0.0, 0.0, 9.5, 32.0, 16.0);
   private static final VoxelShape SHAPE_WEST = Block.method_9541(6.5, 0.0, 0.0, 9.5, 32.0, 16.0);

   public NewGhostDoorBlock(Settings settings) {
      super(settings);
      this.method_9590(
         (BlockState)((BlockState)((BlockState)this.field_10647.method_11664()).method_11657(FACING, Direction.field_11043)).method_11657(OPEN, false)
      );
   }

   @Override
   protected void method_9515(Builder<Block, BlockState> builder) {
      builder.method_11667(new Property[]{FACING, OPEN});
   }

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (player.method_5715() && hand == Hand.field_5808) {
         boolean isOpen = (Boolean)state.method_11654(OPEN);
         BlockState newState = (BlockState)state.method_11657(OPEN, !isOpen);
         world.method_8652(pos, newState, 3);
         BlockEntity blockEntity = world.method_8321(pos);
         if (blockEntity != null) {
            blockEntity.method_5431();
         }

         if (!world.field_9236) {
            if (isOpen) {
               player.method_7353(Text.method_43471("block.smfs.new_ghost_door.close"), true);
            } else {
               player.method_7353(Text.method_43471("block.smfs.new_ghost_door.open"), true);
            }
         }

         return ActionResult.field_5812;
      } else if (!player.method_5715() && hand == Hand.field_5808 && (Boolean)state.method_11654(OPEN)) {
         ItemStack mainHandStack = player.method_6047();
         ItemStack offHandStack = player.method_6079();
         if (!(mainHandStack.method_7909() instanceof GoldenContainerItem) && !(offHandStack.method_7909() instanceof GoldenContainerItem)) {
            if (!world.field_9236 && player instanceof ServerPlayerEntity serverPlayer) {
               if (world.method_27983() == World.field_25179) {
                  ServerWorld targetWorld = serverPlayer.method_5682().method_3847(Smfs.SPIRIT_REALM_DIMENSION);
                  if (targetWorld != null) {
                     BlockPos targetBlockPos = new BlockPos(pos.method_10263(), pos.method_10264(), pos.method_10260());
                     BlockState existingState = targetWorld.method_8320(targetBlockPos);
                     boolean hasGhostDoor = existingState.method_26204() instanceof NewGhostDoorBlock || existingState.method_26204() instanceof GhostDoorBlock;
                     if (!hasGhostDoor) {
                        for (int x = -6; x <= 6; x++) {
                           for (int y = -6; y <= 6; y++) {
                              for (int z = -6; z <= 6; z++) {
                                 BlockPos checkPos = targetBlockPos.method_10069(x, y, z);
                                 BlockState checkState = targetWorld.method_8320(checkPos);
                                 if (checkState.method_26204() instanceof NewGhostDoorBlock || checkState.method_26204() instanceof GhostDoorBlock) {
                                    hasGhostDoor = true;
                                    targetBlockPos = checkPos;
                                    break;
                                 }
                              }

                              if (hasGhostDoor) {
                                 break;
                              }
                           }

                           if (hasGhostDoor) {
                              break;
                           }
                        }
                     }

                     BlockPos platformPos = hasGhostDoor ? targetBlockPos.method_10074() : targetBlockPos;

                     for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                           BlockPos currentPos = platformPos.method_10069(x, 0, z);
                           targetWorld.method_8501(currentPos, Blocks.field_10540.method_9564());
                        }
                     }

                     for (int y = 1; y <= 2; y++) {
                        for (int x = -1; x <= 1; x++) {
                           for (int z = -1; z <= 1; z++) {
                              BlockPos airPos = platformPos.method_10069(x, y, z);
                              if (!airPos.equals(targetBlockPos) || !hasGhostDoor) {
                                 targetWorld.method_8501(airPos, Blocks.field_10124.method_9564());
                              }
                           }
                        }
                     }

                     Vec3d targetPos;
                     if (hasGhostDoor) {
                        targetPos = new Vec3d(targetBlockPos.method_10263() + 0.5, targetBlockPos.method_10264() + 0.5, targetBlockPos.method_10260() + 0.5);
                     } else {
                        targetPos = new Vec3d(platformPos.method_10263() + 0.5, platformPos.method_10264() + 1, platformPos.method_10260() + 0.5);
                     }

                     serverPlayer.method_14251(
                        targetWorld, targetPos.field_1352, targetPos.field_1351, targetPos.field_1350, player.method_36454(), player.method_36455()
                     );
                     player.method_7353(Text.method_43470("§a你进入了灵异之地"), true);
                  }
               } else if (world.method_27983() == Smfs.SPIRIT_REALM_DIMENSION) {
                  ServerWorld targetWorld = serverPlayer.method_5682().method_3847(World.field_25179);
                  if (targetWorld != null) {
                     BlockPos targetBlockPos = new BlockPos(pos.method_10263(), pos.method_10264(), pos.method_10260());
                     BlockState existingState = targetWorld.method_8320(targetBlockPos);
                     boolean hasGhostDoor = existingState.method_26204() instanceof NewGhostDoorBlock || existingState.method_26204() instanceof GhostDoorBlock;
                     if (!hasGhostDoor) {
                        for (int x = -6; x <= 6; x++) {
                           for (int y = -6; y <= 6; y++) {
                              for (int z = -6; z <= 6; z++) {
                                 BlockPos checkPos = targetBlockPos.method_10069(x, y, z);
                                 BlockState checkState = targetWorld.method_8320(checkPos);
                                 if (checkState.method_26204() instanceof NewGhostDoorBlock || checkState.method_26204() instanceof GhostDoorBlock) {
                                    hasGhostDoor = true;
                                    targetBlockPos = checkPos;
                                    break;
                                 }
                              }

                              if (hasGhostDoor) {
                                 break;
                              }
                           }

                           if (hasGhostDoor) {
                              break;
                           }
                        }
                     }

                     BlockPos platformPos = hasGhostDoor ? targetBlockPos.method_10074() : targetBlockPos;

                     for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                           BlockPos currentPos = platformPos.method_10069(x, 0, z);
                           targetWorld.method_8501(currentPos, Blocks.field_10540.method_9564());
                        }
                     }

                     for (int y = 1; y <= 2; y++) {
                        for (int x = -1; x <= 1; x++) {
                           for (int z = -1; z <= 1; z++) {
                              BlockPos airPos = platformPos.method_10069(x, y, z);
                              if (!airPos.equals(targetBlockPos) || !hasGhostDoor) {
                                 targetWorld.method_8501(airPos, Blocks.field_10124.method_9564());
                              }
                           }
                        }
                     }

                     Vec3d targetPos;
                     if (hasGhostDoor) {
                        targetPos = new Vec3d(targetBlockPos.method_10263() + 0.5, targetBlockPos.method_10264() + 0.5, targetBlockPos.method_10260() + 0.5);
                     } else {
                        targetPos = new Vec3d(platformPos.method_10263() + 0.5, platformPos.method_10264() + 1, platformPos.method_10260() + 0.5);
                     }

                     serverPlayer.method_14251(
                        targetWorld, targetPos.field_1352, targetPos.field_1351, targetPos.field_1350, player.method_36454(), player.method_36455()
                     );
                     player.method_7353(Text.method_43470("§a你回到了现实世界"), true);
                  }
               }
            }

            return ActionResult.field_5812;
         } else {
            return ActionResult.field_5811;
         }
      } else {
         return ActionResult.field_5811;
      }
   }

   @Override
   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.method_11654(FACING));
   }

   @Nullable
   @Override
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new NewGhostDoorBlockEntity(pos, state);
   }

   public VoxelShape method_9549(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.method_11654(FACING));
   }

   public void method_9567(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      super.method_9567(world, pos, state, placer, itemStack);
      if ((Boolean)state.method_11654(OPEN)) {
         world.method_39279(pos, this, 1200);
      }
   }

   public void method_9588(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.method_11654(OPEN)) {
         if (this.hasPlayerNearby(world, pos)) {
            this.spawnRandomGhost(world, pos);
         }

         world.method_39279(pos, this, 1200);
      }
   }

   public void method_9536(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved) {
      super.method_9536(oldState, world, pos, newState, moved);
      if (oldState.method_28498(OPEN) && newState.method_28498(OPEN) && !(Boolean)oldState.method_11654(OPEN) && (Boolean)newState.method_11654(OPEN)) {
         world.method_39279(pos, this, 1200);
      }
   }

   private boolean hasPlayerNearby(ServerWorld world, BlockPos pos) {
      Box box = new Box(pos).method_1014(10.0);
      List<PlayerEntity> players = world.method_8390(PlayerEntity.class, box, player -> player.method_5805());
      return !players.isEmpty();
   }

   private void spawnRandomGhost(ServerWorld world, BlockPos pos) {
      if (world.method_27983().equals(World.field_25179)) {
         EntityType<?> ghostType = null;
         Set<EntityType<?>> lockedGhostTypes = GhostSpawnManager.getLockedGhostTypes();
         if (!lockedGhostTypes.isEmpty()) {
            Random random = world.method_8409();
            EntityType<?>[] lockedTypesArray = lockedGhostTypes.toArray(new EntityType[0]);
            ghostType = lockedTypesArray[random.method_43048(lockedTypesArray.length)];
            GhostSpawnManager.unlockGhostType(world, ghostType);
            Smfs.LOGGER.debug("{}从灵异之地逃出来了", ghostType);
         } else {
            Set<EntityType<?>> ghostTypeSet = GhostSpawnManager.getSpawnedGhostTypes();
            ghostTypeSet.remove(ModEntities.YE_ZHEN);
            ghostTypeSet.remove(ModEntities.FENG_QUAN);
            ghostTypeSet.remove(ModEntities.CAO_YANG);
            ghostTypeSet.remove(ModEntities.FANG_SHI_MIN);
            ghostTypeSet.remove(ModEntities.YAN_LI);
            ghostTypeSet.remove(ModEntities.LI_LE_PING);
            ghostTypeSet.remove(ModEntities.WANG_XIAO_MING);
            ghostTypeSet.remove(ModEntities.CHEN_DOCTOR);
            ghostTypeSet.remove(ModEntities.LI_JUN);
            ghostTypeSet.remove(ModEntities.ZHAO_KAI_MING);
            ghostTypeSet.remove(ModEntities.NPC1);
            ghostTypeSet.remove(ModEntities.NPC2);
            ghostTypeSet.remove(ModEntities.NPC3);
            ghostTypeSet.remove(ModEntities.NPC4);
            ghostTypeSet.remove(ModEntities.NPC5);
            ghostTypeSet.remove(ModEntities.NPC6);
            EntityType<?>[] ghostTypes = ghostTypeSet.toArray(new EntityType[0]);
            if (ghostTypes.length == 0) {
               return;
            }

            Random random = world.method_8409();
            ghostType = ghostTypes[random.method_43048(ghostTypes.length)];
         }

         Random random = world.method_8409();
         double x = pos.method_10263() + random.method_43058() * 6.0 - 3.0;
         double y = pos.method_10264() + 1;
         double z = pos.method_10260() + random.method_43058() * 6.0 - 3.0;
         BlockPos spawnPos = new BlockPos((int)x, (int)y, (int)z);
         ghostType.method_5899(world, null, null, spawnPos, SpawnReason.field_16469, true, false);
         Smfs.LOGGER.debug("鬼门刷新出厉鬼: {} 在位置: {}", ghostType, spawnPos);
         Box box = new Box(pos).method_1014(10.0);

         for (PlayerEntity player : world.method_8390(PlayerEntity.class, box, playerx -> playerx.method_5805())) {
            player.method_7353(Text.method_43470("§c有东西从门内跑出来了..."), true);
         }
      }
   }

   private VoxelShape getShapeForDirection(Direction direction) {
      return switch (direction) {
         case field_11043 -> SHAPE_NORTH;
         case field_11035 -> SHAPE_SOUTH;
         case field_11034 -> SHAPE_EAST;
         case field_11039 -> SHAPE_WEST;
         default -> SHAPE_NORTH;
      };
   }
}
