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

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (user.canConsume(true)) {
         user.setCurrentHand(hand);
         return TypedActionResult.consume(itemStack);
      } else {
         return TypedActionResult.fail(itemStack);
      }
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      if (user instanceof PlayerEntity player && !world.isClient) {
         float currentSanity = PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SANITY);
         float maxSanity = PlayerEvents.getMaxSanity(player);
         float actualIncrease = Math.min(5.0F, maxSanity - currentSanity);
         if (actualIncrease > 0.0F) {
            PlayerEvents.addSpiritAttribute(player, SpiritAttributes.SANITY, actualIncrease);
            player.sendMessage(Text.literal("冥果效果: 当前理智=" + currentSanity + ", 增加理智=" + actualIncrease), true);
            player.sendMessage(Text.translatable("item.smfs.underworld_fruit.effect.sanity_increased", new Object[]{(int)actualIncrease}), true);
         }

         player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 1, false, true));
         player.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1.0F, 1.0F);
      }

      return super.finishUsing(stack, world, user);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.underworld_fruit.description.desc"));
      tooltip.add(Text.translatable("item.smfs.underworld_fruit.description.type"));
      tooltip.add(Text.translatable("item.smfs.brewable_material"));
   }
}
