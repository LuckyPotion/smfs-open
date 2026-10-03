package com.xie.smfs.block;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class GhostFurnitureBlock extends BlockWithEntity {
   public static final DirectionProperty FACING = Properties.field_12481;
   protected static final VoxelShape SHAPE = Block.method_9541(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   public GhostFurnitureBlock(Settings settings) {
      super(settings.method_22488());
      this.method_9590((BlockState)((BlockState)this.field_10647.method_11664()).method_11657(FACING, Direction.field_11043));
   }

   protected void method_9515(Builder<Block, BlockState> builder) {
      builder.method_11667(new Property[]{FACING});
   }

   @Nullable
   public BlockState method_9605(ItemPlacementContext ctx) {
      return (BlockState)this.method_9564().method_11657(FACING, ctx.method_8042().method_10153());
   }

   public VoxelShape method_9530(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   @Nullable
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return null;
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
      float baseSpeed = super.method_9594(state, player, world, pos);
      if (tool != null && tool.method_7909() instanceof AxeItem) {
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

      return baseSpeed;
   }
}
