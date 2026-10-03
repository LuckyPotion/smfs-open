package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.FoodComponent.Builder;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostCandyItem extends Item {
   public GhostCandyItem(Settings settings) {
      super(settings.food(new Builder().hunger(2).saturationModifier(0.1F).alwaysEdible().build()));
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      if (!world.isClient && user instanceof PlayerEntity player) {
         EntityAttributeInstance healthAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
         if (healthAttribute != null) {
            double currentMaxHealth = healthAttribute.getBaseValue();
            healthAttribute.setBaseValue(Math.max(1.0, currentMaxHealth - 3.0));
            if (player.getHealth() > healthAttribute.getBaseValue()) {
               player.setHealth((float)healthAttribute.getBaseValue());
            }
         }

         player.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_SURGE, 1200, 0, false, false));
         if (world.random.nextFloat() < 0.02F && !this.hasCandyGhost(player)) {
            ItemStack candyGhostStack = new ItemStack(ModItems.CANDY_GHOST, 1);
            if (!player.giveItemStack(candyGhostStack)) {
               ItemEntity candyGhostEntity = new ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), candyGhostStack);
               player.getWorld().spawnEntity(candyGhostEntity);
            }

            player.sendMessage(Text.translatable("item.smfs.candy_ghost.obtained").formatted(Formatting.GOLD), false);
         }
      }

      return super.finishUsing(stack, world, user);
   }

   private boolean hasCandyGhost(PlayerEntity player) {
      for (int i = 0; i < player.getInventory().size(); i++) {
         ItemStack stack = player.getInventory().getStack(i);
         if (stack.getItem() instanceof CandyGhostItem) {
            return true;
         }
      }

      return false;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_candy.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_candy.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_candy.description.type"));
   }
}
