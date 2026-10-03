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

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!world.isClient) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof GhostFurnaceBlockEntity) {
            player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
            player.incrementStat(Stats.INTERACT_WITH_FURNACE);
         }
      }

      return ActionResult.SUCCESS;
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new GhostFurnaceBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return world.isClient ? null : checkType(type, ModBlockEntities.GHOST_FURNACE_BLOCK_ENTITY, GhostFurnaceBlockEntity::tick);
   }

   protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof GhostFurnaceBlockEntity) {
         player.openHandledScreen((NamedScreenHandlerFactory)blockEntity);
         player.incrementStat(Stats.INTERACT_WITH_FURNACE);
      }
   }

   public List<ItemStack> getDroppedStacks(BlockState state, Builder builder) {
      ItemStack tool = (ItemStack)builder.getOptional(LootContextParameters.TOOL);
      return tool != null && tool.isOf(Items.GOLDEN_PICKAXE) ? List.of(new ItemStack(this)) : List.of();
   }

   public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
      ItemStack tool = player.getMainHandStack();
      float baseSpeed = super.calcBlockBreakingDelta(state, player, world, pos);
      return tool != null && tool.isOf(Items.GOLDEN_PICKAXE) ? baseSpeed * 2.0F : baseSpeed;
   }
}
