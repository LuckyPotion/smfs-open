package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.FoodComponent.Builder;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostChineseMedicineItem extends Item {
   public GhostChineseMedicineItem(Settings settings) {
      super(settings.method_19265(new Builder().method_19238(1).method_19237(0.1F).method_19240().method_19242()));
   }

   public ItemStack method_7861(ItemStack stack, World world, LivingEntity user) {
      if (!world.field_9236 && user instanceof PlayerEntity player) {
         player.method_6092(new StatusEffectInstance(ModEffects.GHOST_SUPPRESSION, 12000, 0, false, false));
      }

      return super.method_7861(stack, world, user);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_chinese_medicine.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_chinese_medicine.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_chinese_medicine.description.type"));
      tooltip.add(Text.method_43471("item.smfs.ghost_chinese_medicine.description.side_effect"));
   }
}
