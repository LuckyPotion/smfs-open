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
   private static final VoxelShape SHAPE_NORTH = Block.method_9541(-16.0, 0.0, 0.0, 32.0, 32.0, 16.0);
   private static final VoxelShape SHAPE_SOUTH = Block.method_9541(-16.0, 0.0, 0.0, 32.0, 32.0, 16.0);
   private static final VoxelShape SHAPE_EAST = Block.method_9541(0.0, 0.0, -16.0, 16.0, 32.0, 32.0);
   private static final VoxelShape SHAPE_WEST = Block.method_9541(0.0, 0.0, -16.0, 16.0, 32.0, 32.0);

   public GhostPianoBlock(Settings settings) {
      super(settings);
   }

   @Override
   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.method_11654(FACING));
   }

   public VoxelShape method_9549(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getShapeForDirection((Direction)state.method_11654(FACING));
   }

   @Nullable
   @Override
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GHOST_PIANO_BLOCK_ENTITY, pos, state);
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

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.field_9236) {
         return ActionResult.field_5812;
      }

      if (player.method_6059(ModEffects.MUSIC_BOX_CURSE)) {
         int proficiency = getPianoProficiency(player);
         if (proficiency >= 100) {
            MusicBoxCurseEffect.safelyRemoveEffect(player);
            player.method_7353(Text.method_43470("§a尝试将脑海的旋律反过来弹奏，八音盒的诅咒被解除了...").method_27692(Formatting.field_1060), true);
            return ActionResult.field_5812;
         } else {
            player.method_7353(Text.method_43470("§c我的钢琴技术还不足以解除诅咒").method_27692(Formatting.field_1061), true);
            return ActionResult.field_5812;
         }
      } else {
         this.tryPracticePiano(player);
         return super.method_9534(state, world, pos, player, hand, hit);
      }
   }

   private void tryPracticePiano(PlayerEntity player) {
      if (getPianoProficiency(player) < 100) {
         long currentDay = player.method_37908().method_8510() / 24000L;
         NbtCompound data = PlayerEvents.getCachedData(player);
         int proficiency = data.method_10545("pianoProficiency") ? data.method_10550("pianoProficiency") : 0;
         long lastDay = data.method_10545("lastPianoDay") ? data.method_10537("lastPianoDay") : -1L;
         if (proficiency < 100) {
            if (lastDay != currentDay) {
               int newProficiency = Math.min(proficiency + 10, 100);
               data.method_10569("pianoProficiency", newProficiency);
               data.method_10544("lastPianoDay", currentDay);
               PlayerEvents.saveDataToPlayer(player, data);
               player.method_7353(Text.method_43470("§a今天的钢琴技术又精进了不少").method_27692(Formatting.field_1060), true);
            }
         }
      }
   }

   public static int getPianoProficiency(PlayerEntity player) {
      if (!TameableItemAPI.getInstance().hasTamedGhost(player, "ghost_shadow_head")
         && !TameableItemAPI.getInstance().hasTamedGhost(player, "complete_shadow_ghost")) {
         NbtCompound data = PlayerEvents.getCachedData(player);
         return data.method_10545("pianoProficiency") ? data.method_10550("pianoProficiency") : 0;
      } else {
         return 100;
      }
   }
}
