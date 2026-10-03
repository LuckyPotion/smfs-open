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
      this(settings, effect, duration, amplifier, effect.method_5556());
   }

   public SpiritPotionItem(Settings settings, StatusEffect effect, int duration, int amplifier, int potionColor) {
      super(settings);
      this.effect = effect;
      this.duration = duration;
      this.amplifier = amplifier;
      this.potionColor = potionColor;
   }

   public UseAction method_7853(ItemStack stack) {
      return UseAction.field_8946;
   }

   public int method_7881(ItemStack stack) {
      return 32;
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (user.method_7332(true)) {
         user.method_6019(hand);
         return TypedActionResult.method_22428(stack);
      } else {
         return TypedActionResult.method_22431(stack);
      }
   }

   public ItemStack method_7861(ItemStack stack, World world, LivingEntity user) {
      if (!world.field_9236 && user instanceof PlayerEntity player) {
         player.method_6092(new StatusEffectInstance(this.effect, this.duration, this.amplifier, false, true));
         if (!player.method_31549().field_7477) {
            stack.method_7934(1);
         }
      }

      return stack;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
   }

   public int getPotionColor() {
      return this.potionColor;
   }
}
