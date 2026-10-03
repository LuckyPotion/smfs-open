package com.xie.smfs.item;

import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DeceptionGhostNecklaceItem extends Item {
   private static final int BASE_DURABILITY = 20;

   public DeceptionGhostNecklaceItem(Settings settings) {
      super(settings.method_7895(20));
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (world.field_9236) {
         return TypedActionResult.method_22427(stack);
      } else if (stack.method_7919() >= stack.method_7936()) {
         user.method_7353(Text.method_43471("item.smfs.deception_ghost_necklace.broken"), true);
         return TypedActionResult.method_22431(stack);
      } else {
         this.applyHealingEffect(user);
         stack.method_7956(1, user, p -> p.method_20236(hand));
         user.method_7353(Text.method_43471("item.smfs.deception_ghost_necklace.used"), true);
         return TypedActionResult.method_22427(stack);
      }
   }

   private void applyHealingEffect(PlayerEntity player) {
      this.clearNegativeEffects(player);
      float currentMaxHealth = player.method_6063();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.method_5996(EntityAttributes.field_23716).method_6192(20.0);
      } else {
         targetHealth = currentMaxHealth;
      }

      player.method_6033(targetHealth);
   }

   private void clearNegativeEffects(PlayerEntity player) {
      for (StatusEffectInstance effect : player.method_6026()) {
         StatusEffect statusEffect = effect.method_5579();
         if (this.isNegativeEffect(statusEffect)) {
            player.method_6016(statusEffect);
         }
      }
   }

   private boolean isNegativeEffect(StatusEffect effect) {
      return !effect.method_5573();
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.deception_ghost_necklace.description.source"));
      tooltip.add(Text.method_43471("item.smfs.deception_ghost_necklace.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.deception_ghost_necklace.description.type"));
   }

   public boolean method_7870(ItemStack stack) {
      return false;
   }

   public int method_7837() {
      return 0;
   }
}
