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

   public ActionResult method_7884(ItemUsageContext context) {
      World world = context.method_8045();
      BlockPos blockPos = context.method_8037();
      BlockState blockState = world.method_8320(blockPos);
      if (blockState.method_27852(Blocks.field_10362)) {
         BlockPos cropPos = blockPos.method_10084();
         if (world.method_8320(cropPos).method_26215()) {
            world.method_8501(cropPos, ModBlocks.FILTHY_CROP.method_9564());
            if (!context.method_8036().method_31549().field_7477) {
               context.method_8041().method_7934(1);
            }

            return ActionResult.field_5812;
         }
      }

      return super.method_7884(context);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.filthy_seed.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.filthy_seed.description.type"));
   }
}
