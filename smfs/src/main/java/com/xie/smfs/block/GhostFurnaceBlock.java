package com.xie.smfs.block;

import com.xie.smfs.block.entity.GhostFurnaceBlockEntity;
import com.xie.smfs.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextParameterSet.Builder;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostFurnaceBlock extends AbstractFurnaceBlock {
   public GhostFurnaceBlock(Settings settings) {
      super(settings);
   }

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!world.field_9236) {
         BlockEntity blockEntity = world.method_8321(pos);
         if (blockEntity instanceof GhostFurnaceBlockEntity) {
            player.method_17355((NamedScreenHandlerFactory)blockEntity);
            player.method_7281(Stats.field_15379);
         }
      }

      return ActionResult.field_5812;
   }

   @Nullable
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new GhostFurnaceBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> method_31645(World world, BlockState state, BlockEntityType<T> type) {
      return world.field_9236 ? null : method_31618(type, ModBlockEntities.GHOST_FURNACE_BLOCK_ENTITY, GhostFurnaceBlockEntity::tick);
   }

   protected void method_17025(World world, BlockPos pos, PlayerEntity player) {
      BlockEntity blockEntity = world.method_8321(pos);
      if (blockEntity instanceof GhostFurnaceBlockEntity) {
         player.method_17355((NamedScreenHandlerFactory)blockEntity);
         player.method_7281(Stats.field_15379);
      }
   }

   public List<ItemStack> method_9560(BlockState state, Builder builder) {
      ItemStack tool = (ItemStack)builder.method_51876(LootContextParameters.field_1229);
      return tool != null && tool.method_31574(Items.field_8335) ? List.of(new ItemStack(this)) : List.of();
   }

   public float method_9594(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      ItemStack tool = player.method_6047();
      float baseSpeed = super.method_9594(state, player, world, pos);
      return tool != null && tool.method_31574(Items.field_8335) ? baseSpeed * 2.0F : baseSpeed;
   }
}
