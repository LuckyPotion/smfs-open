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
   public static final BooleanProperty LIT = Properties.LIT;

   public SpiritBrewingStandBlock(Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(LIT, false));
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{LIT});
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SpiritBrewingStandBlockEntity(pos, state);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return world.isClient ? null : checkType(type, SpiritBrewingStandBlockEntity.TYPE, SpiritBrewingStandBlockEntity::tick);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!world.isClient) {
         NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
         if (screenHandlerFactory != null) {
            player.openHandledScreen(screenHandlerFactory);
         }

         return ActionResult.CONSUME;
      } else {
         return ActionResult.PASS;
      }
   }
}
