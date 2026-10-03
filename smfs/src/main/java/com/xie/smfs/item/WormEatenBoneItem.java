package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WormEatenBoneItem extends Item {
   public WormEatenBoneItem(Settings settings) {
      super(settings);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.worm_eaten_bone.description.source"));
      tooltip.add(Text.method_43471("item.smfs.worm_eaten_bone.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.worm_eaten_bone.description.type"));
      tooltip.add(Text.method_43471("item.smfs.brewable_material"));
   }
}
