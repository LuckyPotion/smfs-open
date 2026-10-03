package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class SpiritPotionItem extends Item {
   protected final StatusEffect effect;
   protected final int duration;
   protected final int amplifier;
   protected final int potionColor;

   public SpiritPotionItem(Settings settings, StatusEffect effect, int duration, int amplifier) {
      this(settings, effect, duration, amplifier, effect.getColor());
   }

   public SpiritPotionItem(Settings settings, StatusEffect effect, int duration, int amplifier, int potionColor) {
      super(settings);
      this.effect = effect;
      this.duration = duration;
      this.amplifier = amplifier;
      this.potionColor = potionColor;
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.DRINK;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 32;
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (user.canConsume(true)) {
         user.setCurrentHand(hand);
         return TypedActionResult.consume(stack);
      } else {
         return TypedActionResult.fail(stack);
      }
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      if (!world.isClient && user instanceof PlayerEntity player) {
         player.addStatusEffect(new StatusEffectInstance(this.effect, this.duration, this.amplifier, false, true));
         if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
         }
      }

      return stack;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
   }

   public int getPotionColor() {
      return this.potionColor;
   }
}
