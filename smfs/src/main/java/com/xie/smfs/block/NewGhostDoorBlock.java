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
   public static final BooleanProperty OPEN = BooleanProperty.of("open");
   private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0.0, 0.0, 6.5, 16.0, 32.0, 9.5);
   private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0.0, 0.0, 6.5, 16.0, 32.0, 9.5);
   private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(6.5, 0.0, 0.0, 9.5, 32.0, 16.0);
   private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(6.5, 0.0, 0.0, 9.5, 32.0, 16.0);

   public NewGhostDoorBlock(Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false));
   }

   @Override
   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, OPEN});
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (player.isSneaking() && hand == Hand.MAIN_HAND) {
         boolean isOpen = (Boolean)state.get(OPEN);
         BlockState newState = (BlockState)state.with(OPEN, !isOpen);
         world.setBlockState(pos, newState, 3);
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity != null) {
            blockEntity.markDirty();
         }

         if (!world.isClient) {
            if (isOpen) {
               player.sendMessage(Text.translatable("block.smfs.new_ghost_door.close"), true);
            } else {
               player.sendMessage(Text.translatable("block.smfs.new_ghost_door.open"), true);
            }
         }

         return ActionResult.SUCCESS;
      } else if (!player.isSneaking() && hand == Hand.MAIN_HAND && (Boolean)state.get(OPEN)) {
         ItemStack mainHandStack = player.getMainHandStack();
         ItemStack offHandStack = player.getOffHandStack();
         if (!(mainHandStack.getItem() instanceof GoldenContainerItem) && !(offHandStack.getItem() instanceof GoldenContainerItem)) {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
               if (world.getRegistryKey() == World.OVERWORLD) {
                  ServerWorld targetWorld = serverPlayer.getServer().getWorld(Smfs.SPIRIT_REALM_DIMENSION);
                  if (targetWorld != null) {
                     BlockPos targetBlockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                     BlockState existingState = targetWorld.getBlockState(targetBlockPos);
                     boolean hasGhostDoor = existingState.getBlock() instanceof NewGhostDoorBlock || existingState.getBlock() instanceof GhostDoorBlock;
                     if (!hasGhostDoor) {
                        for (int x = -6; x <= 6; x++) {
                           for (int y = -6; y <= 6; y++) {
                              for (int z = -6; z <= 6; z++) {
                                 BlockPos checkPos = targetBlockPos.add(x, y, z);
                                 BlockState checkState = targetWorld.getBlockState(checkPos);
                                 if (checkState.getBlock() instanceof NewGhostDoorBlock || checkState.getBlock() instanceof GhostDoorBlock) {
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

                     BlockPos platformPos = hasGhostDoor ? targetBlockPos.down() : targetBlockPos;

                     for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                           BlockPos currentPos = platformPos.add(x, 0, z);
                           targetWorld.setBlockState(currentPos, Blocks.OBSIDIAN.getDefaultState());
                        }
                     }

                     for (int y = 1; y <= 2; y++) {
                        for (int x = -1; x <= 1; x++) {
                           for (int z = -1; z <= 1; z++) {
                              BlockPos airPos = platformPos.add(x, y, z);
                              if (!airPos.equals(targetBlockPos) || !hasGhostDoor) {
                                 targetWorld.setBlockState(airPos, Blocks.AIR.getDefaultState());
                              }
                           }
                        }
                     }

                     Vec3d targetPos;
                     if (hasGhostDoor) {
                        targetPos = new Vec3d(targetBlockPos.getX() + 0.5, targetBlockPos.getY() + 0.5, targetBlockPos.getZ() + 0.5);
                     } else {
                        targetPos = new Vec3d(platformPos.getX() + 0.5, platformPos.getY() + 1, platformPos.getZ() + 0.5);
                     }

                     serverPlayer.teleport(targetWorld, targetPos.x, targetPos.y, targetPos.z, player.getYaw(), player.getPitch());
                     player.sendMessage(Text.literal("§a你进入了灵异之地"), true);
                  }
               } else if (world.getRegistryKey() == Smfs.SPIRIT_REALM_DIMENSION) {
                  ServerWorld targetWorld = serverPlayer.getServer().getWorld(World.OVERWORLD);
                  if (targetWorld != null) {
                     BlockPos targetBlockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                     BlockState existingState = targetWorld.getBlockState(targetBlockPos);
                     boolean hasGhostDoor = existingState.getBlock() instanceof NewGhostDoorBlock || existingState.getBlock() instanceof GhostDoorBlock;
                     if (!hasGhostDoor) {
                        for (int x = -6; x <= 6; x++) {
                           for (int y = -6; y <= 6; y++) {
                              for (int z = -6; z <= 6; z++) {
                                 BlockPos checkPos = targetBlockPos.add(x, y, z);
                                 BlockState checkState = targetWorld.getBlockState(checkPos);
                                 if (checkState.getBlock() instanceof NewGhostDoorBlock || checkState.getBlock() instanceof GhostDoorBlock) {
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

                     BlockPos platformPos = hasGhostDoor ? targetBlockPos.down() : targetBlockPos;

                     for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                           BlockPos currentPos = platformPos.add(x, 0, z);
                           targetWorld.setBlockState(currentPos, Blocks.OBSIDIAN.getDefaultState());
                        }
                     }

                     for (int y = 1; y <= 2; y++) {
                        for (int x = -1; x <= 1; x++) {
                           for (int z = -1; z <= 1; z++) {
                              BlockPos airPos = platformPos.add(x, y, z);
                              if (!airPos.equals(targetBlockPos) || !hasGhostDoor) {
                                 targetWorld.setBlockState(airPos, Blocks.AIR.getDefaultState());
                              }
                           }
                        }
                     }

                     Vec3d targetPos;
                     if (hasGhostDoor) {
                        targetPos = new Vec3d(targetBlockPos.getX() + 0.5, targetBlockPos.getY() + 0.5, targetBlockPos.getZ() + 0.5);
                     } else {
                        targetPos = new Vec3d(platformPos.getX() + 0.5, platformPos.getY() + 1, platformPos.getZ() + 0.5);
                     }

                     serverPlayer.teleport(targetWorld, targetPos.x, targetPos.y, targetPos.z, player.getYaw(), player.getPitch());
                     player.sendMessage(Text.literal("§a你回到了现实世界"), true);
                  }
               }
            }

            return ActionResult.SUCCESS;
         } else {
            return ActionResult.PASS;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   @Override
   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
   }

   @Nullable
   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new NewGhostDoorBlockEntity(pos, state);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      if ((Boolean)state.get(OPEN)) {
         world.scheduleBlockTick(pos, this, 1200);
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(OPEN)) {
         if (this.hasPlayerNearby(world, pos)) {
            this.spawnRandomGhost(world, pos);
         }

         world.scheduleBlockTick(pos, this, 1200);
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      super.onStateReplaced(state, world, pos, newState, moved);
      if (state.contains(OPEN) && newState.contains(OPEN) && !(Boolean)state.get(OPEN) && (Boolean)newState.get(OPEN)) {
         world.scheduleBlockTick(pos, this, 1200);
      }
   }

   private boolean hasPlayerNearby(ServerWorld world, BlockPos pos) {
      Box box = new Box(pos).expand(10.0);
      List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, box, player -> player.isAlive());
      return !players.isEmpty();
   }

   private void spawnRandomGhost(ServerWorld world, BlockPos pos) {
      if (world.getRegistryKey().equals(World.OVERWORLD)) {
         EntityType<?> ghostType = null;
         Set<EntityType<?>> lockedGhostTypes = GhostSpawnManager.getLockedGhostTypes();
         if (!lockedGhostTypes.isEmpty()) {
            Random random = world.getRandom();
            EntityType<?>[] lockedTypesArray = lockedGhostTypes.toArray(new EntityType[0]);
            ghostType = lockedTypesArray[random.nextInt(lockedTypesArray.length)];
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

            Random random = world.getRandom();
            ghostType = ghostTypes[random.nextInt(ghostTypes.length)];
         }

         Random random = world.getRandom();
         double x = pos.getX() + random.nextDouble() * 6.0 - 3.0;
         double y = pos.getY() + 1;
         double z = pos.getZ() + random.nextDouble() * 6.0 - 3.0;
         BlockPos spawnPos = new BlockPos((int)x, (int)y, (int)z);
         ghostType.spawn(world, null, null, spawnPos, SpawnReason.SPAWNER, true, false);
         Smfs.LOGGER.debug("鬼门刷新出厉鬼: {} 在位置: {}", ghostType, spawnPos);
         Box box = new Box(pos).expand(10.0);

         for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, box, playerx -> playerx.isAlive())) {
            player.sendMessage(Text.literal("§c有东西从门内跑出来了..."), true);
         }
      }
   }

   private VoxelShape getShapeForDirection(Direction direction) {
      return switch (direction) {
         case NORTH -> SHAPE_NORTH;
         case SOUTH -> SHAPE_SOUTH;
         case EAST -> SHAPE_EAST;
         case WEST -> SHAPE_WEST;
         default -> SHAPE_NORTH;
      };
   }
}
