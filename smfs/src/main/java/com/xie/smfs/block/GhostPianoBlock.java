package com.xie.smfs.block;

import com.xie.smfs.api.TameableItemAPI;
import com.xie.smfs.client.renderer.StaticAnimatable;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.effect.MusicBoxCurseEffect;
import com.xie.smfs.registry.ModBlockEntities;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostPianoBlock extends GhostFurnitureBlock {
   private static final String PIANO_PROFICIENCY_KEY = "pianoProficiency";
   private static final String LAST_PIANO_DAY_KEY = "lastPianoDay";
   private static final int MAX_PROFICIENCY = 100;
   private static final int DAILY_PROFICIENCY_GAIN = 10;
   private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(-16.0, 0.0, 0.0, 32.0, 32.0, 16.0);
   private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(-16.0, 0.0, 0.0, 32.0, 32.0, 16.0);
   private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0.0, 0.0, -16.0, 16.0, 32.0, 32.0);
   private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(0.0, 0.0, -16.0, 16.0, 32.0, 32.0);

   public GhostPianoBlock(Settings settings) {
      super(settings);
   }

   @Override
   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.get(FACING));
   }

   @Nullable
   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GHOST_PIANO_BLOCK_ENTITY, pos, state);
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

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.SUCCESS;
      }

      if (player.hasStatusEffect(ModEffects.MUSIC_BOX_CURSE)) {
         int proficiency = getPianoProficiency(player);
         if (proficiency >= 100) {
            MusicBoxCurseEffect.safelyRemoveEffect(player);
            player.sendMessage(Text.literal("§a尝试将脑海的旋律反过来弹奏，八音盒的诅咒被解除了...").formatted(Formatting.GREEN), true);
            return ActionResult.SUCCESS;
         } else {
            player.sendMessage(Text.literal("§c我的钢琴技术还不足以解除诅咒").formatted(Formatting.RED), true);
            return ActionResult.SUCCESS;
         }
      } else {
         this.tryPracticePiano(player);
         return super.onUse(state, world, pos, player, hand, hit);
      }
   }

   private void tryPracticePiano(PlayerEntity player) {
      if (getPianoProficiency(player) < 100) {
         long currentDay = player.getWorld().getTime() / 24000L;
         NbtCompound data = PlayerEvents.getCachedData(player);
         int proficiency = data.contains("pianoProficiency") ? data.getInt("pianoProficiency") : 0;
         long lastDay = data.contains("lastPianoDay") ? data.getLong("lastPianoDay") : -1L;
         if (proficiency < 100) {
            if (lastDay != currentDay) {
               int newProficiency = Math.min(proficiency + 10, 100);
               data.putInt("pianoProficiency", newProficiency);
               data.putLong("lastPianoDay", currentDay);
               PlayerEvents.saveDataToPlayer(player, data);
               player.sendMessage(Text.literal("§a今天的钢琴技术又精进了不少").formatted(Formatting.GREEN), true);
            }
         }
      }
   }

   public static int getPianoProficiency(PlayerEntity player) {
      if (!TameableItemAPI.getInstance().hasTamedGhost(player, "ghost_shadow_head")
         && !TameableItemAPI.getInstance().hasTamedGhost(player, "complete_shadow_ghost")) {
         NbtCompound data = PlayerEvents.getCachedData(player);
         return data.contains("pianoProficiency") ? data.getInt("pianoProficiency") : 0;
      } else {
         return 100;
      }
   }
}
