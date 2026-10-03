package com.xie.smfs.block;

import com.xie.smfs.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DirtyCropBlock extends CropBlock {
   public static final int MAX_AGE = 2;
   public static final IntProperty AGE = IntProperty.of("age", 0, 2);

   public DirtyCropBlock(Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   protected ItemConvertible getSeedsItem() {
      return ModItems.DIRTY_SEED;
   }

   public IntProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 2;
   }

   protected void appendProperties(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{AGE});
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(ModItems.UNDERWORLD_FRUIT);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack itemStack = player.getStackInHand(hand);
      if (itemStack.isOf(Items.BONE_MEAL)) {
         if (!world.isClient) {
            player.sendMessage(Text.translatable("message.smfs.bone_meal_invalid.dirty_crop"), true);
         }

         return ActionResult.SUCCESS;
      } else {
         return super.onUse(state, world, pos, player, hand, hit);
      }
   }
}
