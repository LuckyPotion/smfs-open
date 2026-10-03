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
      super(settings.maxDamage(20));
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (world.isClient) {
         return TypedActionResult.success(stack);
      } else if (stack.getDamage() >= stack.getMaxDamage()) {
         user.sendMessage(Text.translatable("item.smfs.deception_ghost_necklace.broken"), true);
         return TypedActionResult.fail(stack);
      } else {
         this.applyHealingEffect(user);
         stack.damage(1, user, p -> p.sendToolBreakStatus(hand));
         user.sendMessage(Text.translatable("item.smfs.deception_ghost_necklace.used"), true);
         return TypedActionResult.success(stack);
      }
   }

   private void applyHealingEffect(PlayerEntity player) {
      this.clearNegativeEffects(player);
      float currentMaxHealth = player.getMaxHealth();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
      } else {
         targetHealth = currentMaxHealth;
      }

      player.setHealth(targetHealth);
   }

   private void clearNegativeEffects(PlayerEntity player) {
      for (StatusEffectInstance effect : player.getStatusEffects()) {
         StatusEffect statusEffect = effect.getEffectType();
         if (this.isNegativeEffect(statusEffect)) {
            player.removeStatusEffect(statusEffect);
         }
      }
   }

   private boolean isNegativeEffect(StatusEffect effect) {
      return !effect.isBeneficial();
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.deception_ghost_necklace.description.source"));
      tooltip.add(Text.translatable("item.smfs.deception_ghost_necklace.description.desc"));
      tooltip.add(Text.translatable("item.smfs.deception_ghost_necklace.description.type"));
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public int getEnchantability() {
      return 0;
   }
}
