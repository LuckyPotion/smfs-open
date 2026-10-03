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
      super(settings.food(new Builder().hunger(1).saturationModifier(0.1F).alwaysEdible().build()));
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      if (!world.isClient && user instanceof PlayerEntity player) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GHOST_SUPPRESSION, 12000, 0, false, false));
      }

      return super.finishUsing(stack, world, user);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_chinese_medicine.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_chinese_medicine.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_chinese_medicine.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_chinese_medicine.description.side_effect"));
   }
}
