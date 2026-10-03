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

public class FilthyCropBlock extends CropBlock {
   public static final int MAX_AGE = 2;
   public static final IntProperty AGE = IntProperty.method_11867("age", 0, 2);

   public FilthyCropBlock(Settings settings) {
      super(settings);
      this.method_9590((BlockState)((BlockState)this.field_10647.method_11664()).method_11657(AGE, 0));
   }

   protected ItemConvertible method_9832() {
      return ModItems.FILTHY_SEED;
   }

   public IntProperty method_9824() {
      return AGE;
   }

   public int method_9827() {
      return 2;
   }

   protected void method_9515(Builder<Block, BlockState> builder) {
      builder.method_11667(new Property[]{AGE});
   }

   public ItemStack method_9574(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(ModItems.FILTHY_FRUIT);
   }

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack itemStack = player.method_5998(hand);
      if (itemStack.method_31574(Items.field_8324)) {
         if (!world.field_9236) {
            player.method_7353(Text.method_43471("message.smfs.bone_meal_invalid.filthy_crop"), true);
         }

         return ActionResult.field_5812;
      } else {
         return super.method_9534(state, world, pos, player, hand, hit);
      }
   }
}
