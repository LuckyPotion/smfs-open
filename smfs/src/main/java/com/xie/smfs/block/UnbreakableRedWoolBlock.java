package com.xie.smfs.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UnbreakableRedWoolBlock extends Block {
   public UnbreakableRedWoolBlock(Settings settings) {
      super(settings);
   }

   public ActionResult method_9534(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (!world.field_9236) {
         world.method_8396(null, pos, SoundEvents.field_14927, SoundCategory.field_15245, 1.0F, 1.0F);
      }

      return ActionResult.field_21466;
   }

   public void method_9576(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      world.method_8396(null, pos, SoundEvents.field_14927, SoundCategory.field_15245, 1.0F, 1.0F);
   }
}
