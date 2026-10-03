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
   public static final DirectionProperty FACING = Properties.field_12481;
   public static final BooleanProperty OPEN = BooleanProperty.method_11825("open");
   public static final BooleanProperty OCCUPIED = BooleanProperty.method_11825("occupied");
   private static final VoxelShape SHAPE_NORTH_SOUTH = Block.method_9541(0.0, 0.0, -8.0, 16.0, 16.0, 24.0);
   private static final VoxelShape SHAPE_EAST_WEST = Block.method_9541(-8.0, 0.0, 0.0, 24.0, 16.0, 16.0);
   private static final WeakHashMap<PlayerEntity, Boolean> EXITING_PLAYERS = new WeakHashMap<>();
   private static final WeakHashMap<PlayerEntity, Boolean> LYING_PLAYERS = new WeakHashMap<>();

   public GhostCoffinBlock(Settings settings) {
      super(settings.method_22488());
      this.method_9590(
         (BlockState)((BlockState)((BlockState)((BlockState)this.field_10647.method_11664()).method_11657(FACING, Direction.field_11043))
               .method_11657(OPEN, false))
            .method_11657(OCCUPIED, false)
      );
   }

   protected void method_9515(Builder<Block, BlockState> builder) {
      builder.method_11667(new Property[]{FACING, OPEN, OCCUPIED});
   }

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      boolean isPlayerInCoffin = this.isPlayerInCoffin(world, pos, player);
      if (isPlayerInCoffin) {
         LOGGER.debug("检测到玩家在棺材内部 - 玩家: {}, 位置: {}", player.method_5477().getString(), pos);
         if (world.field_9236) {
            LOGGER.debug("客户端内部操作 - 返回SUCCESS");
            return ActionResult.field_5812;
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过操作");
            return ActionResult.field_5812;
         }

         if (player.method_5715() && hand == Hand.field_5808) {
            this.setPlayerExiting(player, true);
            this.setLyingFlag(player, false);
            this.ejectPlayer(player, pos, state);
            world.method_8652(pos, (BlockState)state.method_11657(OCCUPIED, false), 3);
            this.setPlayerExiting(player, false);
            return ActionResult.field_5812;
         }

         if (!player.method_5715() && hand == Hand.field_5808) {
            boolean isOpen = (Boolean)state.method_11654(OPEN);
            BlockState newState = (BlockState)state.method_11657(OPEN, !isOpen);
            world.method_8652(pos, newState, 3);
            if (isOpen) {
               player.method_7353(Text.method_43471("block.smfs.ghost_coffin.close"), true);
            } else {
               player.method_7353(Text.method_43471("block.smfs.ghost_coffin.open"), true);
            }

            return ActionResult.field_5812;
         } else {
            LOGGER.debug("内部操作条件未满足 - 返回SUCCESS");
            return ActionResult.field_5812;
         }
      } else {
         if (world.field_9236) {
            return ActionResult.field_5812;
         }

         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.method_8321(pos);
         if (blockEntity == null) {
            return ActionResult.field_5811;
         }

         ItemStack heldItem = player.method_5998(hand);
         if (heldItem.method_7909() instanceof SpawnEggItem) {
            EntityType<?> entityType = this.getEntityTypeFromSpawnEgg((SpawnEggItem)heldItem.method_7909());
            if (entityType != null && this.isGhostEntityType(entityType, world)) {
               if (!(Boolean)state.method_11654(OPEN)) {
                  player.method_7353(Text.method_43471("block.smfs.ghost_coffin.must_be_open"), true);
                  return ActionResult.field_5814;
               }

               if (!blockEntity.hasStoredGhost()) {
                  GhostEntity ghost = this.createGhostEntity(entityType, world, pos);
                  if (ghost != null) {
                     ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5, 0.0F, 0.0F);
                     ghost.setSuppressed(true);
                     ghost.method_5971();
                     ghost.setMovementDisabled(true);
                     blockEntity.storeGhost(ghost);
                     world.method_39279(pos, this, 20);
                     blockEntity.onBlockStateChanged();
                     if (!player.method_7337()) {
                        heldItem.method_7934(1);
                     }

                     player.method_7353(Text.method_43471("block.smfs.ghost_coffin.ghost_stored"), true);
                     return ActionResult.field_5812;
                  }
               } else {
                  player.method_7353(Text.method_43471("block.smfs.ghost_coffin.already_occupied"), true);
               }
            } else {
               player.method_7353(Text.method_43471("block.smfs.ghost_coffin.not_ghost_egg"), true);
            }
         }

         if (heldItem.method_7909() instanceof GoldenContainerItem) {
            GoldenContainerItem containerItem = (GoldenContainerItem)heldItem.method_7909();
            boolean containerHasGhost = GoldenContainerItem.hasGhost(heldItem);
            boolean coffinHasGhost = blockEntity.hasStoredGhost() || blockEntity.hasMarkedGhostType();
            String coffinGhostId = "无";
            if (blockEntity.hasStoredGhost()) {
               Optional<GhostEntity> ghostOptional = blockEntity.getGhost();
               if (ghostOptional.isPresent()) {
                  GhostEntity ghost = ghostOptional.get();
                  coffinGhostId = ghost.method_5864().method_5882() + " (UUID: " + ghost.method_5667() + ")";
               } else if (blockEntity.hasMarkedGhostType()) {
                  EntityType<?> markedType = blockEntity.getMarkedGhostType();
                  coffinGhostId = markedType.method_5882() + " (存储UUID: " + blockEntity.getStoredGhostUuid() + ")";
               }
            } else if (blockEntity.hasMarkedGhostType()) {
               EntityType<?> markedType = blockEntity.getMarkedGhostType();
               coffinGhostId = markedType.method_5882() + " (标记类型)";
            }

            if (containerHasGhost && !coffinHasGhost) {
               NbtCompound ghostNbt = heldItem.method_7948().method_10562("ContainedGhost");
               if (EntityType.method_17842(ghostNbt, world, loadedEntity -> loadedEntity instanceof GhostEntity ? loadedEntity : null) instanceof GhostEntity ghost
                  )
                {
                  ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5, 0.0F, 0.0F);
                  ghost.setSuppressed(true);
                  ghost.method_5971();
                  ghost.setMovementDisabled(true);
                  blockEntity.storeGhost(ghost);
                  blockEntity.onBlockStateChanged();
                  heldItem.method_7948().method_10551("ContainedGhost");
                  heldItem.method_7948().method_10556("HasGhost", false);
                  heldItem.method_7948().method_10551("IsHeavy");
                  player.method_7353(Text.method_43471("block.smfs.ghost_coffin.ghost_transferred_from_container"), true);
                  return ActionResult.field_5812;
               }
            } else {
               if (containerHasGhost || !coffinHasGhost) {
                  if (containerHasGhost && coffinHasGhost) {
                     player.method_7353(Text.method_43471("block.smfs.ghost_coffin.both_containers_have_ghost"), true);
                  } else {
                     player.method_7353(Text.method_43471("block.smfs.ghost_coffin.both_containers_empty"), true);
                  }

                  return ActionResult.field_5814;
               }

               WorldConfig config = WorldConfig.getInstance(world);
               if (config.modDifficulty == 2 && !player.method_7337()) {
                  player.method_7353(Text.method_43470("似乎需要先打开棺材？"), true);
                  return ActionResult.field_5814;
               }

               Optional<GhostEntity> ghostOptional = blockEntity.hasStoredGhost() ? blockEntity.getGhost() : blockEntity.spawnFromMarkedType();
               if (ghostOptional.isPresent()) {
                  GhostEntity ghost = ghostOptional.get();
                  ItemStack containerStack = heldItem;
                  if (containerStack.method_7960()) {
                     containerStack = new ItemStack(ModItems.GOLDEN_CONTAINER);
                  }

                  ItemStack coffinNail = ghost.getCoffinNail();
                  boolean hasNail = coffinNail != null && !coffinNail.method_7960();
                  if (!hasNail) {
                     ghost.setSuppressed(false);
                     ghost.setMovementDisabled(false);
                  } else {
                     ghost.setSuppressed(true);
                  }

                  NbtCompound ghostNbt = new NbtCompound();
                  ghost.method_5647(ghostNbt);
                  if (!hasNail) {
                     ghostNbt.method_10551("CoffinNail");
                  } else {
                     ghostNbt.method_10566("CoffinNail", coffinNail.method_7953(new NbtCompound()));
                  }

                  ghostNbt.method_10556("Deadlocked", ghost.isDeadlocked());
                  ghostNbt.method_10551("UUID");
                  ghostNbt.method_10551("UUIDMost");
                  ghostNbt.method_10551("UUIDLeast");
                  ghostNbt.method_10582("id", EntityType.method_5890(ghost.method_5864()).toString());
                  ghostNbt.method_10549("OriginalX", ghost.method_23317());
                  ghostNbt.method_10549("OriginalY", ghost.method_23318());
                  ghostNbt.method_10549("OriginalZ", ghost.method_23321());
                  ghostNbt.method_10582("OriginalWorld", world.method_27983().method_29177().toString());
                  containerStack.method_7948().method_10566("ContainedGhost", ghostNbt);
                  containerStack.method_7948().method_10556("HasGhost", true);
                  containerStack.method_7948().method_10556("IsHeavy", true);
                  if (heldItem.method_7960()) {
                     player.method_6122(hand, containerStack);
                  }

                  if (blockEntity.hasStoredGhost()) {
                     blockEntity.clearStoredGhost();
                  } else {
                     blockEntity.clearMarkedGhostType();
                  }

                  ghost.method_31472();
                  world.method_8652(pos, (BlockState)state.method_11657(OCCUPIED, false), 3);
                  player.method_7353(Text.method_43471("block.smfs.ghost_coffin.ghost_transferred_to_container"), true);
                  return ActionResult.field_5812;
               }
            }
         }

         if (this.isPlayerExiting(player)) {
            LOGGER.debug("玩家正在退出中，跳过外部操作");
            return ActionResult.field_5812;
         }

         if (player.method_5715() && hand == Hand.field_5808) {
            boolean isOpen = (Boolean)state.method_11654(OPEN);
            BlockState newState = (BlockState)state.method_11657(OPEN, !isOpen);
            world.method_8652(pos, newState, 3);
            blockEntity.onBlockStateChanged();
            if (!isOpen) {
               this.releaseStoredGhost(world, pos, blockEntity);
               if (blockEntity.hasMarkedGhostType()) {
                  this.spawnMarkedGhost(world, pos, blockEntity);
               }

               player.method_7353(Text.method_43471("block.smfs.ghost_coffin.open"), true);
            } else {
               player.method_7353(Text.method_43471("block.smfs.ghost_coffin.close"), true);
            }

            return ActionResult.field_5812;
         } else if (player.method_5715() || hand != Hand.field_5808) {
            return ActionResult.field_5811;
         } else if (!(Boolean)state.method_11654(OPEN)) {
            player.method_7353(Text.method_43471("block.smfs.red_coffin.closed"), true);
            return ActionResult.field_5812;
         } else if ((Boolean)state.method_11654(OCCUPIED)) {
            player.method_7353(Text.method_43471("block.smfs.red_coffin.occupied"), true);
            return ActionResult.field_5812;
         } else {
            this.layPlayerInCoffin(player, pos, state);
            world.method_8652(pos, (BlockState)state.method_11657(OCCUPIED, true), 3);
            this.setLyingFlag(player, true);
            this.scheduleTick(world, pos);
            return ActionResult.field_5812;
         }
      }
   }

   private EntityType<?> getEntityTypeFromSpawnEgg(SpawnEggItem spawnEgg) {
      for (Item item : Registries.field_41178) {
         if (item instanceof SpawnEggItem eggItem && eggItem == spawnEgg) {
            Identifier itemId = Registries.field_41178.method_10221(item);
            if (itemId != null) {
               String entityIdStr = itemId.method_12832().replace("_spawn_egg", "");
               Identifier entityId = new Identifier(itemId.method_12836(), entityIdStr);
               return (EntityType<?>)Registries.field_41177.method_10223(entityId);
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
         Entity entity = entityType.method_5883(world);
         if (entity instanceof GhostEntity ghost) {
            return ghost;
         }

         if (entity != null) {
            entity.method_31472();
         }
      } catch (Exception e) {
         LOGGER.error("创建鬼实体时出错", e);
      }

      return null;
   }

   private void releaseStoredGhost(World world, BlockPos pos, GhostCoffinBlockEntity blockEntity) {
      if (blockEntity.hasStoredGhost()) {
         Optional<GhostEntity> releasedGhost = blockEntity.releaseGhost();
         world.method_8652(pos, (BlockState)world.method_8320(pos).method_11657(OCCUPIED, false), 3);
         if (releasedGhost.isPresent()) {
            GhostEntity ghost = releasedGhost.get();
            ghost.setSuppressed(false);
            ghost.setMovementDisabled(false);
            ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 1.0, pos.method_10260() + 0.5, world.field_9229.method_43057() * 360.0F, 0.0F);
            LOGGER.debug("从棺材中释放鬼: {}", ghost.method_5667());
         } else {
            LOGGER.debug("鬼实体释放失败，但已清除占用状态");
         }
      }
   }

   private void spawnMarkedGhost(World world, BlockPos pos, GhostCoffinBlockEntity blockEntity) {
      if (blockEntity.hasMarkedGhostType()) {
         blockEntity.spawnFromMarkedType().ifPresent(ghost -> {
            ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 1.0, pos.method_10260() + 0.5, world.field_9229.method_43057() * 360.0F, 0.0F);
            blockEntity.clearMarkedGhostType();
            LOGGER.debug("从标记类型生成鬼: {}", Registries.field_41177.method_10221(blockEntity.getMarkedGhostType()));
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
      boolean inBox = innerBox.method_1006(player.method_19538());
      boolean isSleeping = player.method_18376() == EntityPose.field_18078;
      boolean lyingFlagSet = this.isLyingFlagSet(player);
      LOGGER.debug(
         "检测玩家状态 - 玩家: {}, 位置: {}, 边界框内: {}, 躺卧姿势: {}, 躺下标志位: {}", player.method_5477().getString(), player.method_19538(), inBox, isSleeping, lyingFlagSet
      );
      return inBox && (isSleeping || lyingFlagSet) || lyingFlagSet;
   }

   private Box getInnerBoundingBox(BlockPos pos) {
      return new Box(
         pos.method_10263() + 0.05,
         pos.method_10264() + 0.05,
         pos.method_10260() + 0.05,
         pos.method_10263() + 0.95,
         pos.method_10264() + 0.95,
         pos.method_10260() + 0.95
      );
   }

   private void layPlayerInCoffin(PlayerEntity player, BlockPos pos, BlockState state) {
      double x = pos.method_10263() + 0.5;
      double y = pos.method_10264() + 0.1;
      double z = pos.method_10260() + 0.5;
      float yaw = this.getCorrectYawFromDirection((Direction)state.method_11654(FACING));
      player.method_20620(x, y, z);
      player.method_36456(yaw);
      player.method_36457(0.0F);
      player.method_18380(EntityPose.field_18078);
      this.setLyingFlag(player, true);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_18380(EntityPose.field_18078);
         serverPlayer.field_13987.method_14372();
      }

      CoffinEffectManager.setPlayerInGhostCoffin(player, pos);
      LOGGER.debug("玩家躺入棺材: {}, 位置: {}, 姿势: {}, 躺下标志位: {}", player.method_5477().getString(), pos, player.method_18376(), this.isLyingFlagSet(player));
   }

   private void ejectPlayer(PlayerEntity player, BlockPos pos, BlockState state) {
      Direction facing = (Direction)state.method_11654(FACING);
      double x = pos.method_10263() + 0.5 + facing.method_10148() * 1.5;
      double y = pos.method_10264() + 0.5;
      double z = pos.method_10260() + 0.5 + facing.method_10165() * 1.5;
      player.method_20620(x, y, z);
      player.method_18380(EntityPose.field_18076);
      this.setLyingFlag(player, false);
      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_18380(EntityPose.field_18076);
         serverPlayer.field_13987.method_14372();
      }

      CoffinEffectManager.removePlayerFromCoffin(player);
      LOGGER.debug("弹出玩家: {}, 位置: {}, 姿势: {}, 躺下标志位: {}", player.method_5477().getString(), pos, player.method_18376(), this.isLyingFlagSet(player));
   }

   private float getCorrectYawFromDirection(Direction direction) {
      switch (direction) {
         case field_11043:
            return 180.0F;
         case field_11035:
            return 0.0F;
         case field_11039:
            return 90.0F;
         case field_11034:
            return -90.0F;
         default:
            return 0.0F;
      }
   }

   private void scheduleTick(World world, BlockPos pos) {
      world.method_39279(pos, this, 20);
   }

   public void method_9588(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      super.method_9588(state, world, pos, random);
      if ((Boolean)state.method_11654(OCCUPIED)) {
         Box searchBox = this.getInnerBoundingBox(pos);
         List<PlayerEntity> players = world.method_8390(PlayerEntity.class, searchBox, p -> p.method_18376() == EntityPose.field_18078);
         if (players.isEmpty()) {
            world.method_8652(pos, (BlockState)state.method_11657(OCCUPIED, false), 3);
            LOGGER.debug("定期检测：玩家已离开棺材，更新占用状态");
         } else {
            world.method_39279(pos, this, 20);
         }
      }
   }

   public void method_9548(BlockState state, World world, BlockPos pos, Entity entity) {
      if ((Boolean)state.method_11654(OCCUPIED)
         && entity instanceof PlayerEntity player
         && player.method_18376() == EntityPose.field_18078
         && this.isPlayerInCoffin(world, pos, player)
         && !this.getInnerBoundingBox(pos).method_1006(player.method_19538())) {
         double x = pos.method_10263() + 0.5;
         double y = pos.method_10264() + 0.1;
         double z = pos.method_10260() + 0.5;
         player.method_20620(x, y, z);
      }
   }

   public void method_9536(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.method_27852(newState.method_26204())) {
         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.method_8321(pos);
         if (blockEntity != null) {
            if (blockEntity.hasStoredGhost()) {
               LOGGER.debug("方块被破坏 - 释放储存的鬼");
               Optional<GhostEntity> releasedGhost = blockEntity.releaseGhost();
               if (releasedGhost.isPresent()) {
                  GhostEntity ghost = releasedGhost.get();
                  ghost.setSuppressed(false);
                  ghost.setMovementDisabled(false);
                  ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5, 0.0F, 0.0F);
                  LOGGER.debug("成功释放鬼实体: {}", ghost.method_5667());
               } else {
                  LOGGER.debug("鬼实体引用无效，已自动清理");
                  if (blockEntity.hasMarkedGhostType()) {
                     LOGGER.debug("尝试从标记类型生成鬼实体作为备用");
                     Optional<GhostEntity> spawnedGhost = blockEntity.spawnFromMarkedType();
                     if (spawnedGhost.isPresent()) {
                        GhostEntity ghost = spawnedGhost.get();
                        ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5, 0.0F, 0.0F);
                        blockEntity.clearMarkedGhostType();
                        LOGGER.debug("成功从标记类型生成鬼实体: {}", ghost.method_5667());
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
                  ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5, 0.0F, 0.0F);
                  blockEntity.clearMarkedGhostType();
                  LOGGER.debug("成功从标记类型生成鬼实体: {}", ghost.method_5667());
               } else {
                  LOGGER.warn("从标记类型生成鬼实体失败");
               }
            }
         }

         if ((Boolean)state.method_11654(OCCUPIED)) {
            LOGGER.debug("方块被破坏 - 弹出玩家");
            Box searchBox = this.getInnerBoundingBox(pos);
            List<PlayerEntity> players = world.method_8390(PlayerEntity.class, searchBox, p -> p.method_18376() == EntityPose.field_18078);
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

      super.method_9536(state, world, pos, newState, moved);
   }

   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction direction = (Direction)state.method_11654(FACING);
      return direction != Direction.field_11043 && direction != Direction.field_11035 ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
   }

   public VoxelShape method_9571(BlockState state, BlockView world, BlockPos pos) {
      return this.method_9530(state, world, pos, ShapeContext.method_16194());
   }

   public BlockRenderType method_9604(BlockState state) {
      return BlockRenderType.field_11456;
   }

   public List<ItemStack> method_9560(BlockState state, net.minecraft.loot.context.LootContextParameterSet.Builder builder) {
      ItemStack tool = (ItemStack)builder.method_51876(LootContextParameters.field_1229);
      return tool == null
            || !(tool.method_7909() instanceof AxeItem)
               && !tool.method_31574(Items.field_8475)
               && !tool.method_31574(Items.field_8825)
               && !tool.method_31574(Items.field_8556)
               && !tool.method_31574(Items.field_22025)
               && !tool.method_31574(Items.field_8406)
               && !tool.method_31574(Items.field_8062)
         ? List.of()
         : List.of(new ItemStack(this));
   }

   public float getHardness(BlockState state, BlockView world, BlockPos pos) {
      return 3.0F;
   }

   public float method_9594(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      ItemStack tool = player.method_6047();
      if (tool.method_7909() instanceof AxeItem) {
         float baseSpeed = super.method_9594(state, player, world, pos);
         if (tool.method_31574(Items.field_22025)) {
            return baseSpeed * 3.0F;
         }

         if (tool.method_31574(Items.field_8556)) {
            return baseSpeed * 2.5F;
         }

         if (tool.method_31574(Items.field_8475)) {
            return baseSpeed * 2.0F;
         }

         if (tool.method_31574(Items.field_8825)) {
            return baseSpeed * 1.5F;
         }

         if (tool.method_31574(Items.field_8062)) {
            return baseSpeed * 1.2F;
         }

         if (tool.method_31574(Items.field_8406)) {
            return baseSpeed * 1.0F;
         }
      }

      return super.method_9594(state, player, world, pos);
   }

   @Nullable
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return ModBlockEntities.GHOST_COFFIN_BLOCK_ENTITY.method_11032(pos, state);
   }

   public void method_9567(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      super.method_9567(world, pos, state, placer, itemStack);
      if (itemStack.method_7985() && itemStack.method_7969().method_10545("BlockEntityTag")) {
         NbtCompound blockEntityTag = itemStack.method_7969().method_10562("BlockEntityTag");
         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.method_8321(pos);
         if (blockEntity != null && blockEntityTag.method_10545("MarkedGhostType")) {
            String typeId = blockEntityTag.method_10558("MarkedGhostType");
            EntityType<?> ghostType = (EntityType<?>)Registries.field_41177.method_10223(new Identifier(typeId));
            if (ghostType != null && this.isGhostEntityType(ghostType, world)) {
               blockEntity.markGhostType(ghostType);
            }
         }
      }
   }

   public static boolean enterCoffin(World world, BlockPos pos, GhostEntity ghost) {
      if (world != null && pos != null && ghost != null && !ghost.method_31481()) {
         BlockState state = world.method_8320(pos);
         if (!(state.method_26204() instanceof GhostCoffinBlock)) {
            LOGGER.warn("目标位置不是鬼棺材方块");
            return false;
         }

         if (!(Boolean)state.method_11654(OPEN)) {
            LOGGER.warn("棺材状态不满足进入条件：打开={}", state.method_11654(OPEN));
            return false;
         }

         GhostCoffinBlockEntity blockEntity = (GhostCoffinBlockEntity)world.method_8321(pos);
         if (blockEntity == null) {
            LOGGER.warn("棺材方块实体不存在");
            return false;
         }

         try {
            Box searchBox = getInnerBoundingBoxStatic(pos);
            List<PlayerEntity> playersInCoffin = world.method_8390(PlayerEntity.class, searchBox, p -> isPlayerInCoffinStatic(world, pos, p));
            if (!playersInCoffin.isEmpty() && ghost instanceof GhostOfficerEntity) {
               PlayerEntity player = playersInCoffin.get(0);
               LOGGER.debug("检测到玩家 {} 在棺材内，鬼差尝试进入，执行特殊逻辑", player.method_5477().getString());
               BlockState newState = (BlockState)((BlockState)state.method_11657(OPEN, false)).method_11657(OCCUPIED, true);
               world.method_8652(pos, newState, 3);
               giveGhostOfficerItemToPlayer(player);
               GhostDeathHandler.markLegitimateRemoval(ghost);
               ghost.method_31472();
               LOGGER.debug("鬼差进入棺材特殊逻辑执行完成：关闭棺材，给予玩家鬼差驾驭物品，移除鬼差");
               return true;
            }

            if ((Boolean)state.method_11654(OCCUPIED)) {
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
            blockEntity.method_11007(nbt);
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
      return new Box(
         pos.method_10263() + 0.05,
         pos.method_10264() + 0.05,
         pos.method_10260() + 0.05,
         pos.method_10263() + 0.95,
         pos.method_10264() + 0.95,
         pos.method_10260() + 0.95
      );
   }

   private static boolean isPlayerInCoffinStatic(World world, BlockPos pos, PlayerEntity player) {
      Box innerBox = getInnerBoundingBoxStatic(pos);
      boolean inBox = innerBox.method_1006(player.method_19538());
      boolean isSleeping = player.method_18376() == EntityPose.field_18078;
      boolean lyingFlagSet = isLyingFlagSetStatic(player);
      LOGGER.debug(
         "检测玩家状态 - 玩家: {}, 位置: {}, 边界框内: {}, 躺卧姿势: {}, 躺下标志位: {}", player.method_5477().getString(), player.method_19538(), inBox, isSleeping, lyingFlagSet
      );
      return inBox && (isSleeping || lyingFlagSet) || lyingFlagSet;
   }

   private static boolean isLyingFlagSetStatic(PlayerEntity player) {
      return LYING_PLAYERS.getOrDefault(player, false);
   }

   private static void giveGhostOfficerItemToPlayer(PlayerEntity player) {
      if (player != null && !player.method_37908().method_8608()) {
         try {
            ItemStack ghostOfficerItem = GhostUtils.createTamedItem("ghost_officer");
            if (ghostOfficerItem.method_7960()) {
               LOGGER.warn("无法创建鬼差驾驭物品");
               return;
            }

            if (!player.method_31548().method_7394(ghostOfficerItem)) {
               player.method_7328(ghostOfficerItem, false);
               LOGGER.debug("鬼差驾驭物品掉落在地上，玩家: {}", player.method_5477().getString());
            } else {
               LOGGER.debug("成功给予玩家鬼差驾驭物品，玩家: {}", player.method_5477().getString());
            }

            player.method_7353(Text.method_43470("§a成功驾驭了鬼差！"), true);
         } catch (Exception e) {
            LOGGER.error("给予玩家鬼差驾驭物品时发生错误", e);
         }
      }
   }
}
