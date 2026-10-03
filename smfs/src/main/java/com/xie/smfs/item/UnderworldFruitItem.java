package com.xie.smfs.item;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.effect.SpiritAttributes;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class UnderworldFruitItem extends Item {
   public UnderworldFruitItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.method_5998(hand);
      if (user.method_7332(true)) {
         user.method_6019(hand);
         return TypedActionResult.method_22428(itemStack);
      } else {
         return TypedActionResult.method_22431(itemStack);
      }
   }

   public ItemStack method_7861(ItemStack stack, World world, LivingEntity user) {
      if (user instanceof PlayerEntity player && !world.field_9236) {
         float currentSanity = PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SANITY);
         float maxSanity = PlayerEvents.getMaxSanity(player);
         float actualIncrease = Math.min(5.0F, maxSanity - currentSanity);
         if (actualIncrease > 0.0F) {
            PlayerEvents.addSpiritAttribute(player, SpiritAttributes.SANITY, actualIncrease);
            player.method_7353(Text.method_43470("冥果效果: 当前理智=" + currentSanity + ", 增加理智=" + actualIncrease), true);
            player.method_7353(Text.method_43469("item.smfs.underworld_fruit.effect.sanity_increased", new Object[]{(int)actualIncrease}), true);
         }

         player.method_6092(new StatusEffectInstance(StatusEffects.field_5903, 200, 1, false, true));
         player.method_5783(SoundEvents.field_19149, 1.0F, 1.0F);
      }

      return super.method_7861(stack, world, user);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.underworld_fruit.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.underworld_fruit.description.type"));
      tooltip.add(Text.method_43471("item.smfs.brewable_material"));
   }
}
