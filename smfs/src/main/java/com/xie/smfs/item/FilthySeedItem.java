package com.xie.smfs.item;

import com.xie.smfs.registry.ModBlocks;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilthySeedItem extends Item {
   public FilthySeedItem(Settings settings) {
      super(settings);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World world = context.getWorld();
      BlockPos blockPos = context.getBlockPos();
      BlockState blockState = world.getBlockState(blockPos);
      if (blockState.isOf(Blocks.FARMLAND)) {
         BlockPos cropPos = blockPos.up();
         if (world.getBlockState(cropPos).isAir()) {
            world.setBlockState(cropPos, ModBlocks.FILTHY_CROP.getDefaultState());
            if (!context.getPlayer().getAbilities().creativeMode) {
               context.getStack().decrement(1);
            }

            return ActionResult.SUCCESS;
         }
      }

      return super.useOnBlock(context);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.filthy_seed.description.desc"));
      tooltip.add(Text.translatable("item.smfs.filthy_seed.description.type"));
   }
}
