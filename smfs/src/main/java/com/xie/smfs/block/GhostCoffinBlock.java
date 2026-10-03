package com.xie.smfs.block;

import com.xie.smfs.block.entity.GhostCoffinBlockEntity;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.GhostOfficerEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModBlockEntities;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.GhostUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostCoffinBlock extends BlockWithEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostCoffinBlock.class);
   public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
   public static final BooleanProperty OPEN = BooleanProperty.of("open");
   public static final BooleanProperty OCCUPIED = BooleanProperty.of("occupied");
   private static final VoxelShape SHAPE_NORTH_SOUTH = Block.createCuboidShape(0.0, 0.0, -8.0, 16.0, 16.0, 24.0);
   private static final VoxelShape SHAPE_EAST_WEST = Block.createCuboidShape(-8.0, 0.0, 0.0, 24.0, 16.0, 16.0);
   private static final WeakHashMap<PlayerEntity, Boolean> EXITING_PLAYERS = new WeakHashMap<>();
   private static final WeakHashMap<PlayerEntity, Boolean> LYING_PLAYERS = new WeakHashMap<>();

   public GhostCoffinBlock(Settings settings) {
      super(settings.nonOpaque());
      this.setDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(OPEN, false))
            .with(OCCUPIED, false)
      );
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, OPEN, OCCUPIED});
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      boolean isPlayerInCoffin = this.isPlayerInCoffin(world, pos, player);
      if (isPlayerInCoffin) {
         LOGGER.debug("检测到玩家在棺材内部 - 玩家: {}, 位置: {}", player.getName().getString(), pos);
         if (world.isClient) {
            LOGGER.debug("客户端内部操作 - 返回SUCCESS");
            return ActionResult.SUCCESS;
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过操作");
            return ActionResult.SUCCESS;
         }

         if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            this.setPlayerExiting(player, true);
            this.setLyingFlag(player, false);
            this.ejectPlayer(player, pos, state);
            world.setBlockState(pos, (BlockState)state.with(OCCUPIED, false), 3);
            this.setPlayerExiting(player, false);
            return ActionResult.SUCCESS;
         }

         if (!player.isSneaking() && hand == Hand.MAIN_HAND) {
            boolean isOpen = (Boolean)state.get(OPEN);
            BlockState newState = (BlockState)state.with(OPEN, !isOpen);
            world.setBlockState(pos, newState, 3);
            if (isOpen) {
               player.sendMessage(Text.translatable("block.smfs.ghost_coffin.close"), true);
            } else {
               player.sendMessage(Text.translatable("block.smfs.ghost_coffin.open"), true);
            }

            return ActionResult.SUCCESS;
         } else {
            LOGGER.debug("内部操作条件未满足 - 返回SUCCESS");
            return ActionResult.SUCCESS;
         }
      } else {
         if (world.isClient) {
            return ActionResult.SUCCESS;
         }

         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.getBlockEntity(pos);
         if (blockEntity == null) {
            return ActionResult.PASS;
         }

         ItemStack heldItem = player.getStackInHand(hand);
         if (heldItem.getItem() instanceof SpawnEggItem) {
            EntityType<?> entityType = this.getEntityTypeFromSpawnEgg((SpawnEggItem)heldItem.getItem());
            if (entityType != null && this.isGhostEntityType(entityType, world)) {
               if (!(Boolean)state.get(OPEN)) {
                  player.sendMessage(Text.translatable("block.smfs.ghost_coffin.must_be_open"), true);
                  return ActionResult.FAIL;
               }

               if (!blockEntity.hasStoredGhost()) {
                  GhostEntity ghost = this.createGhostEntity(entityType, world, pos);
                  if (ghost != null) {
                     ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
                     ghost.setSuppressed(true);
                     ghost.setPersistent();
                     ghost.setMovementDisabled(true);
                     blockEntity.storeGhost(ghost);
                     world.scheduleBlockTick(pos, this, 20);
                     blockEntity.onBlockStateChanged();
                     if (!player.isCreative()) {
                        heldItem.decrement(1);
                     }

                     player.sendMessage(Text.translatable("block.smfs.ghost_coffin.ghost_stored"), true);
                     return ActionResult.SUCCESS;
                  }
               } else {
                  player.sendMessage(Text.translatable("block.smfs.ghost_coffin.already_occupied"), true);
               }
            } else {
               player.sendMessage(Text.translatable("block.smfs.ghost_coffin.not_ghost_egg"), true);
            }
         }

         if (heldItem.getItem() instanceof GoldenContainerItem) {
            GoldenContainerItem containerItem = (GoldenContainerItem)heldItem.getItem();
            boolean containerHasGhost = GoldenContainerItem.hasGhost(heldItem);
            boolean coffinHasGhost = blockEntity.hasStoredGhost() || blockEntity.hasMarkedGhostType();
            String coffinGhostId = "无";
            if (blockEntity.hasStoredGhost()) {
               Optional<GhostEntity> ghostOptional = blockEntity.getGhost();
               if (ghostOptional.isPresent()) {
                  GhostEntity ghost = ghostOptional.get();
                  coffinGhostId = ghost.getType().getTranslationKey() + " (UUID: " + ghost.getUuid() + ")";
               } else if (blockEntity.hasMarkedGhostType()) {
                  EntityType<?> markedType = blockEntity.getMarkedGhostType();
                  coffinGhostId = markedType.getTranslationKey() + " (存储UUID: " + blockEntity.getStoredGhostUuid() + ")";
               }
            } else if (blockEntity.hasMarkedGhostType()) {
               EntityType<?> markedType = blockEntity.getMarkedGhostType();
               coffinGhostId = markedType.getTranslationKey() + " (标记类型)";
            }

            if (containerHasGhost && !coffinHasGhost) {
               NbtCompound ghostNbt = heldItem.getOrCreateNbt().getCompound("ContainedGhost");
               if (EntityType.loadEntityWithPassengers(ghostNbt, world, loadedEntity -> loadedEntity instanceof GhostEntity ? loadedEntity : null) instanceof GhostEntity ghost
                  )
                {
                  ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
                  ghost.setSuppressed(true);
                  ghost.setPersistent();
                  ghost.setMovementDisabled(true);
                  blockEntity.storeGhost(ghost);
                  blockEntity.onBlockStateChanged();
                  heldItem.getOrCreateNbt().remove("ContainedGhost");
                  heldItem.getOrCreateNbt().putBoolean("HasGhost", false);
                  heldItem.getOrCreateNbt().remove("IsHeavy");
                  player.sendMessage(Text.translatable("block.smfs.ghost_coffin.ghost_transferred_from_container"), true);
                  return ActionResult.SUCCESS;
               }
            } else {
               if (containerHasGhost || !coffinHasGhost) {
                  if (containerHasGhost && coffinHasGhost) {
                     player.sendMessage(Text.translatable("block.smfs.ghost_coffin.both_containers_have_ghost"), true);
                  } else {
                     player.sendMessage(Text.translatable("block.smfs.ghost_coffin.both_containers_empty"), true);
                  }

                  return ActionResult.FAIL;
               }

               WorldConfig config = WorldConfig.getInstance(world);
               if (config.modDifficulty == 2 && !player.isCreative()) {
                  player.sendMessage(Text.literal("似乎需要先打开棺材？"), true);
                  return ActionResult.FAIL;
               }

               Optional<GhostEntity> ghostOptional = blockEntity.hasStoredGhost() ? blockEntity.getGhost() : blockEntity.spawnFromMarkedType();
               if (ghostOptional.isPresent()) {
                  GhostEntity ghost = ghostOptional.get();
                  ItemStack containerStack = heldItem;
                  if (containerStack.isEmpty()) {
                     containerStack = new ItemStack(ModItems.GOLDEN_CONTAINER);
                  }

                  ItemStack coffinNail = ghost.getCoffinNail();
                  boolean hasNail = coffinNail != null && !coffinNail.isEmpty();
                  if (!hasNail) {
                     ghost.setSuppressed(false);
                     ghost.setMovementDisabled(false);
                  } else {
                     ghost.setSuppressed(true);
                  }

                  NbtCompound ghostNbt = new NbtCompound();
                  ghost.writeNbt(ghostNbt);
                  if (!hasNail) {
                     ghostNbt.remove("CoffinNail");
                  } else {
                     ghostNbt.put("CoffinNail", coffinNail.writeNbt(new NbtCompound()));
                  }

                  ghostNbt.putBoolean("Deadlocked", ghost.isDeadlocked());
                  ghostNbt.remove("UUID");
                  ghostNbt.remove("UUIDMost");
                  ghostNbt.remove("UUIDLeast");
                  ghostNbt.putString("id", EntityType.getId(ghost.getType()).toString());
                  ghostNbt.putDouble("OriginalX", ghost.getX());
                  ghostNbt.putDouble("OriginalY", ghost.getY());
                  ghostNbt.putDouble("OriginalZ", ghost.getZ());
                  ghostNbt.putString("OriginalWorld", world.getRegistryKey().getValue().toString());
                  containerStack.getOrCreateNbt().put("ContainedGhost", ghostNbt);
                  containerStack.getOrCreateNbt().putBoolean("HasGhost", true);
                  containerStack.getOrCreateNbt().putBoolean("IsHeavy", true);
                  if (heldItem.isEmpty()) {
                     player.setStackInHand(hand, containerStack);
                  }

                  if (blockEntity.hasStoredGhost()) {
                     blockEntity.clearStoredGhost();
                  } else {
                     blockEntity.clearMarkedGhostType();
                  }

                  ghost.discard();
                  world.setBlockState(pos, (BlockState)state.with(OCCUPIED, false), 3);
                  player.sendMessage(Text.translatable("block.smfs.ghost_coffin.ghost_transferred_to_container"), true);
                  return ActionResult.SUCCESS;
               }
            }
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过外部操作");
            return ActionResult.SUCCESS;
         }

         if (player.isSneaking() && hand == Hand.MAIN_HAND) {
            boolean isOpen = (Boolean)state.get(OPEN);
            BlockState newState = (BlockState)state.with(OPEN, !isOpen);
            world.setBlockState(pos, newState, 3);
            blockEntity.onBlockStateChanged();
            if (!isOpen) {
               this.releaseStoredGhost(world, pos, blockEntity);
               if (blockEntity.hasMarkedGhostType()) {
                  this.spawnMarkedGhost(world, pos, blockEntity);
               }

               player.sendMessage(Text.translatable("block.smfs.ghost_coffin.open"), true);
            } else {
               player.sendMessage(Text.translatable("block.smfs.ghost_coffin.close"), true);
            }

            return ActionResult.SUCCESS;
         } else if (player.isSneaking() || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
         } else if (!(Boolean)state.get(OPEN)) {
            player.sendMessage(Text.translatable("block.smfs.red_coffin.closed"), true);
            return ActionResult.SUCCESS;
         } else if ((Boolean)state.get(OCCUPIED)) {
            player.sendMessage(Text.translatable("block.smfs.red_coffin.occupied"), true);
            return ActionResult.SUCCESS;
         } else {
            this.layPlayerInCoffin(player, pos, state);
            world.setBlockState(pos, (BlockState)state.with(OCCUPIED, true), 3);
            this.setLyingFlag(player, true);
            this.scheduleTick(world, pos);
            return ActionResult.SUCCESS;
         }
      }
   }

   private EntityType<?> getEntityTypeFromSpawnEgg(SpawnEggItem spawnEgg) {
      for (Item item : Registries.ITEM) {
         if (item instanceof SpawnEggItem eggItem && eggItem == spawnEgg) {
            Identifier itemId = Registries.ITEM.getId(item);
            if (itemId != null) {
               String entityIdStr = itemId.getPath().replace("_spawn_egg", "");
               Identifier entityId = new Identifier(itemId.getNamespace(), entityIdStr);
               return (EntityType<?>)Registries.ENTITY_TYPE.get(entityId);
            }
         }
      }

      return null;
   }

   private boolean isGhostEntityType(EntityType<?> entityType, World world) {
      return AdvancementManager.isGhostEntityType(entityType);
   }

   private GhostEntity createGhostEntity(EntityType<?> entityType, World world, BlockPos pos) {
      try {
         Entity entity = entityType.create(world);
         if (entity instanceof GhostEntity ghost) {
            return ghost;
         }

         if (entity != null) {
            entity.discard();
         }
      } catch (Exception e) {
         LOGGER.error("创建鬼实体时出错", e);
      }

      return null;
   }

   private void releaseStoredGhost(World world, BlockPos pos, GhostCoffinBlockEntity blockEntity) {
      if (blockEntity.hasStoredGhost()) {
         Optional<GhostEntity> releasedGhost = blockEntity.releaseGhost();
         world.setBlockState(pos, (BlockState)world.getBlockState(pos).with(OCCUPIED, false), 3);
         if (releasedGhost.isPresent()) {
            GhostEntity ghost = releasedGhost.get();
            ghost.setSuppressed(false);
            ghost.setMovementDisabled(false);
            ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, world.random.nextFloat() * 360.0F, 0.0F);
            LOGGER.debug("从棺材中释放鬼: {}", ghost.getUuid());
         } else {
            LOGGER.debug("鬼实体释放失败，但已清除占用状态");
         }
      }
   }

   private void spawnMarkedGhost(World world, BlockPos pos, GhostCoffinBlockEntity blockEntity) {
      if (blockEntity.hasMarkedGhostType()) {
         blockEntity.spawnFromMarkedType().ifPresent(ghost -> {
            ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, world.random.nextFloat() * 360.0F, 0.0F);
            blockEntity.clearMarkedGhostType();
            LOGGER.debug("从标记类型生成鬼: {}", Registries.ENTITY_TYPE.getId(blockEntity.getMarkedGhostType()));
         });
      }
   }

   private boolean isPlayerExiting(PlayerEntity player) {
      return EXITING_PLAYERS.getOrDefault(player, false);
   }

   private void setPlayerExiting(PlayerEntity player, boolean exiting) {
      EXITING_PLAYERS.put(player, exiting);
   }

   private boolean isLyingFlagSet(PlayerEntity player) {
      return LYING_PLAYERS.getOrDefault(player, false);
   }

   private void setLyingFlag(PlayerEntity player, boolean lying) {
      LYING_PLAYERS.put(player, lying);
   }

   private boolean isPlayerInCoffin(World world, BlockPos pos, PlayerEntity player) {
      Box innerBox = this.getInnerBoundingBox(pos);
      boolean inBox = innerBox.contains(player.getPos());
      boolean isSleeping = player.getPose() == EntityPose.SLEEPING;
      boolean lyingFlagSet = this.isLyingFlagSet(player);
      LOGGER.debug("检测玩家状态 - 玩家: {}, 位置: {}, 边界框内: {}, 躺卧姿势: {}, 躺下标志位: {}", player.getName().getString(), player.getPos(), inBox, isSleeping, lyingFlagSet);
      return inBox && (isSleeping || lyingFlagSet) || lyingFlagSet;
   }

   private Box getInnerBoundingBox(BlockPos pos) {
      return new Box(pos.getX() + 0.05, pos.getY() + 0.05, pos.getZ() + 0.05, pos.getX() + 0.95, pos.getY() + 0.95, pos.getZ() + 0.95);
   }

   private void layPlayerInCoffin(PlayerEntity player, BlockPos pos, BlockState state) {
      double x = pos.getX() + 0.5;
      double y = pos.getY() + 0.1;
      double z = pos.getZ() + 0.5;
      float yaw = this.getCorrectYawFromDirection((Direction)state.get(FACING));
      player.teleport(x, y, z);
      player.setYaw(yaw);
      player.setPitch(0.0F);
      player.setPose(EntityPose.SLEEPING);
      this.setLyingFlag(player, true);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.setPose(EntityPose.SLEEPING);
         serverPlayer.networkHandler.syncWithPlayerPosition();
      }

      CoffinEffectManager.setPlayerInGhostCoffin(player, pos);
      LOGGER.debug("玩家躺入棺材: {}, 位置: {}, 姿势: {}, 躺下标志位: {}", player.getName().getString(), pos, player.getPose(), this.isLyingFlagSet(player));
   }

   private void ejectPlayer(PlayerEntity player, BlockPos pos, BlockState state) {
      Direction facing = (Direction)state.get(FACING);
      double x = pos.getX() + 0.5 + facing.getOffsetX() * 1.5;
      double y = pos.getY() + 0.5;
      double z = pos.getZ() + 0.5 + facing.getOffsetZ() * 1.5;
      player.teleport(x, y, z);
      player.setPose(EntityPose.STANDING);
      this.setLyingFlag(player, false);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.setPose(EntityPose.STANDING);
         serverPlayer.networkHandler.syncWithPlayerPosition();
      }

      CoffinEffectManager.removePlayerFromCoffin(player);
      LOGGER.debug("弹出玩家: {}, 位置: {}, 姿势: {}, 躺下标志位: {}", player.getName().getString(), pos, player.getPose(), this.isLyingFlagSet(player));
   }

   private float getCorrectYawFromDirection(Direction direction) {
      switch (direction) {
         case NORTH:
            return 180.0F;
         case SOUTH:
            return 0.0F;
         case WEST:
            return 90.0F;
         case EAST:
            return -90.0F;
         default:
            return 0.0F;
      }
   }

   private void scheduleTick(World world, BlockPos pos) {
      world.scheduleBlockTick(pos, this, 20);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      super.scheduledTick(state, world, pos, random);
      if ((Boolean)state.get(OCCUPIED)) {
         Box searchBox = this.getInnerBoundingBox(pos);
         List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, p -> p.getPose() == EntityPose.SLEEPING);
         if (players.isEmpty()) {
            world.setBlockState(pos, (BlockState)state.with(OCCUPIED, false), 3);
            LOGGER.debug("定期检测：玩家已离开棺材，更新占用状态");
         } else {
            world.scheduleBlockTick(pos, this, 20);
         }
      }
   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if ((Boolean)state.get(OCCUPIED)
         && entity instanceof PlayerEntity player
         && player.getPose() == EntityPose.SLEEPING
         && this.isPlayerInCoffin(world, pos, player)
         && !this.getInnerBoundingBox(pos).contains(player.getPos())) {
         double x = pos.getX() + 0.5;
         double y = pos.getY() + 0.1;
         double z = pos.getZ() + 0.5;
         player.teleport(x, y, z);
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.getBlockEntity(pos);
         if (blockEntity != null) {
            if (blockEntity.hasStoredGhost()) {
               LOGGER.debug("方块被破坏 - 释放储存的鬼");
               Optional<GhostEntity> releasedGhost = blockEntity.releaseGhost();
               if (releasedGhost.isPresent()) {
                  GhostEntity ghost = releasedGhost.get();
                  ghost.setSuppressed(false);
                  ghost.setMovementDisabled(false);
                  ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
                  LOGGER.debug("成功释放鬼实体: {}", ghost.getUuid());
               } else {
                  LOGGER.debug("鬼实体引用无效，已自动清理");
                  if (blockEntity.hasMarkedGhostType()) {
                     LOGGER.debug("尝试从标记类型生成鬼实体作为备用");
                     Optional<GhostEntity> spawnedGhost = blockEntity.spawnFromMarkedType();
                     if (spawnedGhost.isPresent()) {
                        GhostEntity ghost = spawnedGhost.get();
                        ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
                        blockEntity.clearMarkedGhostType();
                        LOGGER.debug("成功从标记类型生成鬼实体: {}", ghost.getUuid());
                     } else {
                        LOGGER.warn("从标记类型生成鬼实体失败");
                     }
                  }
               }
            } else if (blockEntity.hasMarkedGhostType()) {
               LOGGER.debug("方块被破坏 - 从标记类型生成鬼");
               Optional<GhostEntity> spawnedGhost = blockEntity.spawnFromMarkedType();
               if (spawnedGhost.isPresent()) {
                  GhostEntity ghost = spawnedGhost.get();
                  ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
                  blockEntity.clearMarkedGhostType();
                  LOGGER.debug("成功从标记类型生成鬼实体: {}", ghost.getUuid());
               } else {
                  LOGGER.warn("从标记类型生成鬼实体失败");
               }
            }
         }

         if ((Boolean)state.get(OCCUPIED)) {
            LOGGER.debug("方块被破坏 - 弹出玩家");
            Box searchBox = this.getInnerBoundingBox(pos);
            List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, searchBox, p -> p.getPose() == EntityPose.SLEEPING);
            Iterator var16 = players.iterator();
            if (var16.hasNext()) {
               PlayerEntity player = (PlayerEntity)var16.next();
               if (!this.isPlayerExiting(player)) {
                  this.setPlayerExiting(player, true);
                  this.setLyingFlag(player, false);
                  this.ejectPlayer(player, pos, state);
                  this.setPlayerExiting(player, false);
               }
            }
         }
      }

      super.onStateReplaced(state, world, pos, newState, moved);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction direction = (Direction)state.get(FACING);
      return direction != Direction.NORTH && direction != Direction.SOUTH ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
   }

   public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
      return this.getOutlineShape(state, world, pos, ShapeContext.absent());
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public List<ItemStack> getDroppedStacks(BlockState state, net.minecraft.loot.context.LootContextParameterSet.Builder builder) {
      ItemStack tool = (ItemStack)builder.getOptional(LootContextParameters.TOOL);
      return tool == null
            || !(tool.getItem() instanceof AxeItem)
               && !tool.isOf(Items.IRON_AXE)
               && !tool.isOf(Items.GOLDEN_AXE)
               && !tool.isOf(Items.DIAMOND_AXE)
               && !tool.isOf(Items.NETHERITE_AXE)
               && !tool.isOf(Items.WOODEN_AXE)
               && !tool.isOf(Items.STONE_AXE)
         ? List.of()
         : List.of(new ItemStack(this));
   }

   public float getHardness(BlockState state, BlockView world, BlockPos pos) {
      return 3.0F;
   }

   public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      ItemStack tool = player.getMainHandStack();
      if (tool.getItem() instanceof AxeItem) {
         float baseSpeed = super.calcBlockBreakingDelta(state, player, world, pos);
         if (tool.isOf(Items.NETHERITE_AXE)) {
            return baseSpeed * 3.0F;
         }

         if (tool.isOf(Items.DIAMOND_AXE)) {
            return baseSpeed * 2.5F;
         }

         if (tool.isOf(Items.IRON_AXE)) {
            return baseSpeed * 2.0F;
         }

         if (tool.isOf(Items.GOLDEN_AXE)) {
            return baseSpeed * 1.5F;
         }

         if (tool.isOf(Items.STONE_AXE)) {
            return baseSpeed * 1.2F;
         }

         if (tool.isOf(Items.WOODEN_AXE)) {
            return baseSpeed * 1.0F;
         }
      }

      return super.calcBlockBreakingDelta(state, player, world, pos);
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return ModBlockEntities.GHOST_COFFIN_BLOCK_ENTITY.instantiate(pos, state);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      if (itemStack.hasNbt() && itemStack.getNbt().contains("BlockEntityTag")) {
         NbtCompound blockEntityTag = itemStack.getNbt().getCompound("BlockEntityTag");
         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.getBlockEntity(pos);
         if (blockEntity != null && blockEntityTag.contains("MarkedGhostType")) {
            String typeId = blockEntityTag.getString("MarkedGhostType");
            EntityType<?> ghostType = (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier(typeId));
            if (ghostType != null && this.isGhostEntityType(ghostType, world)) {
               blockEntity.markGhostType(ghostType);
            }
         }
      }
   }

   public static boolean enterCoffin(World world, BlockPos pos, GhostEntity ghost) {
      if (world != null && pos != null && ghost != null && !ghost.isRemoved()) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof GhostCoffinBlock)) {
            LOGGER.warn("目标位置不是鬼棺材方块");
            return false;
         }

         if (!(Boolean)state.get(OPEN)) {
            LOGGER.warn("棺材状态不满足进入条件：打开={}", state.get(OPEN));
            return false;
         }

         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.getBlockEntity(pos);
         if (blockEntity == null) {
            LOGGER.warn("棺材方块实体不存在");
            return false;
         }

         try {
            Box searchBox = getInnerBoundingBoxStatic(pos);
            List<PlayerEntity> playersInCoffin = world.getEntitiesByClass(PlayerEntity.class, searchBox, p -> isPlayerInCoffinStatic(world, pos, p));
            if (!playersInCoffin.isEmpty() && ghost instanceof GhostOfficerEntity) {
               PlayerEntity player = playersInCoffin.get(0);
               LOGGER.debug("检测到玩家 {} 在棺材内，鬼差尝试进入，执行特殊逻辑", player.getName().getString());
               BlockState newState = (BlockState)((BlockState)state.with(OPEN, false)).with(OCCUPIED, true);
               world.setBlockState(pos, newState, 3);
               giveGhostOfficerItemToPlayer(player);
               GhostDeathHandler.markLegitimateRemoval(ghost);
               ghost.discard();
               LOGGER.debug("鬼差进入棺材特殊逻辑执行完成：关闭棺材，给予玩家鬼差驾驭物品，移除鬼差");
               return true;
            }

            if ((Boolean)state.get(OCCUPIED)) {
               LOGGER.warn("棺材已被占用，无法进入");
               return false;
            }

            blockEntity.storeGhost(ghost);
            if (ghost instanceof GhostOfficerEntity ghostOfficer) {
               int suppressionQuota = ghostOfficer.getSuppressionQuota();
               blockEntity.setSuppressionQuota(suppressionQuota);
            }

            blockEntity.onBlockStateChanged();
            NbtCompound nbt = new NbtCompound();
            blockEntity.writeNbt(nbt);
            LOGGER.debug("鬼实体进入鬼棺成功，棺材NBT信息: {}", nbt.toString());
            return true;
         } catch (Exception e) {
            LOGGER.error("鬼实体进入棺材时发生错误", e);
            return false;
         }
      } else {
         LOGGER.warn("鬼实体进入棺材参数无效");
         return false;
      }
   }

   private static Box getInnerBoundingBoxStatic(BlockPos pos) {
      return new Box(pos.getX() + 0.05, pos.getY() + 0.05, pos.getZ() + 0.05, pos.getX() + 0.95, pos.getY() + 0.95, pos.getZ() + 0.95);
   }

   private static boolean isPlayerInCoffinStatic(World world, BlockPos pos, PlayerEntity player) {
      Box innerBox = getInnerBoundingBoxStatic(pos);
      boolean inBox = innerBox.contains(player.getPos());
      boolean isSleeping = player.getPose() == EntityPose.SLEEPING;
      boolean lyingFlagSet = isLyingFlagSetStatic(player);
      LOGGER.debug("检测玩家状态 - 玩家: {}, 位置: {}, 边界框内: {}, 躺卧姿势: {}, 躺下标志位: {}", player.getName().getString(), player.getPos(), inBox, isSleeping, lyingFlagSet);
      return inBox && (isSleeping || lyingFlagSet) || lyingFlagSet;
   }

   private static boolean isLyingFlagSetStatic(PlayerEntity player) {
      return LYING_PLAYERS.getOrDefault(player, false);
   }

   private static void giveGhostOfficerItemToPlayer(PlayerEntity player) {
      if (player != null && !player.getWorld().isClient()) {
         try {
            ItemStack ghostOfficerItem = GhostUtils.createTamedItem("ghost_officer");
            if (ghostOfficerItem.isEmpty()) {
               LOGGER.warn("无法创建鬼差驾驭物品");
               return;
            }

            if (!player.getInventory().insertStack(ghostOfficerItem)) {
               player.dropItem(ghostOfficerItem, false);
               LOGGER.debug("鬼差驾驭物品掉落在地上，玩家: {}", player.getName().getString());
            } else {
               LOGGER.debug("成功给予玩家鬼差驾驭物品，玩家: {}", player.getName().getString());
            }

            player.sendMessage(Text.literal("§a成功驾驭了鬼差！"), true);
         } catch (Exception e) {
            LOGGER.error("给予玩家鬼差驾驭物品时发生错误", e);
         }
      }
   }
}
