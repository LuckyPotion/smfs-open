package com.xie.smfs.block;

import com.xie.smfs.block.entity.SpiritBrewingStandBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpiritBrewingStandBlock extends BlockWithEntity {
   public static final BooleanProperty LIT = Properties.field_12548;

   public SpiritBrewingStandBlock(Settings settings) {
      super(settings);
      this.method_9590((BlockState)((BlockState)this.field_10647.method_11664()).method_11657(LIT, false));
   }

   protected void method_9515(Builder<Block, BlockState> builder) {
      builder.method_11667(new Property[]{LIT});
   }

   @Nullable
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new SpiritBrewingStandBlockEntity(pos, state);
   }

   public BlockRenderType method_9604(BlockState state) {
      return BlockRenderType.field_11456;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> method_31645(World world, BlockState state, BlockEntityType<T> type) {
      return world.field_9236 ? null : method_31618(type, SpiritBrewingStandBlockEntity.TYPE, SpiritBrewingStandBlockEntity::tick);
   }

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!world.field_9236) {
         NamedScreenHandlerFactory screenHandlerFactory = state.method_26196(world, pos);
         if (screenHandlerFactory != null) {
            player.method_17355(screenHandlerFactory);
         }

         return ActionResult.field_21466;
      } else {
         return ActionResult.field_5811;
      }
   }
}
