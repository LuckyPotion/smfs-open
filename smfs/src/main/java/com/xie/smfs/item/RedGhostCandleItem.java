package com.xie.smfs.item;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RedGhostCandleItem extends Item {
   private static final int BASE_DURABILITY = 100;
   private static final double DISTANCE_THRESHOLD = 20.0;

   public RedGhostCandleItem(Settings settings) {
      super(settings.maxDamage(100));
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, world, entity, slot, selected);
      if (!world.isClient && entity instanceof PlayerEntity player && (player.getMainHandStack() == stack || player.getOffHandStack() == stack)) {
         this.checkAndConsumeDurability(stack, player);
      }
   }

   private void checkAndConsumeDurability(ItemStack stack, PlayerEntity player) {
      World world = player.getWorld();
      List<GhostEntity> nearbyGhosts = world.getEntitiesByClass(GhostEntity.class, player.getBoundingBox().expand(20.0), ghostx -> true);
      if (!nearbyGhosts.isEmpty()) {
         float maxConsumptionRate = 0.0F;

         for (GhostEntity ghost : nearbyGhosts) {
            double distance = player.squaredDistanceTo(ghost);
            if (distance <= 400.0) {
               double actualDistance = Math.sqrt(distance);
               float consumptionRate = this.calculateConsumptionRate(actualDistance, ghost.getTerrorLevel());
               if (consumptionRate > maxConsumptionRate) {
                  maxConsumptionRate = consumptionRate;
               }
            }
         }

         if (maxConsumptionRate > 0.0F && world.random.nextFloat() < maxConsumptionRate) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));
         }
      }
   }

   private float calculateConsumptionRate(double actualDistance, char terrorLevel) {
      float distanceFactor = actualDistance < 20.0 ? (float)(1.0 - actualDistance / 20.0) : 0.0F;
      float levelFactor = this.getLevelFactor(terrorLevel);
      float baseRate = 0.1F;
      return Math.min(baseRate * distanceFactor * levelFactor, 0.8F);
   }

   private float getLevelFactor(char level) {
      return switch (level) {
         case 'A' -> 1.5F;
         case 'B' -> 1.2F;
         case 'C' -> 1.0F;
         case 'D' -> 0.8F;
         case 'S' -> 2.0F;
         default -> 1.0F;
      };
   }

   public static boolean isHoldingCandle(PlayerEntity player) {
      return player.getMainHandStack().isOf(ModItems.RED_GHOST_CANDLE) || player.getOffHandStack().isOf(ModItems.RED_GHOST_CANDLE);
   }

   public static void consumeDurabilityOnSpiritDamage(PlayerEntity player, float spiritDamage) {
      if (!(spiritDamage <= 0.0F) && !player.getWorld().isClient) {
         int damageAmount = 10;
         ItemStack mainHand = player.getMainHandStack();
         ItemStack offHand = player.getOffHandStack();
         if (mainHand.isOf(ModItems.RED_GHOST_CANDLE)) {
            mainHand.damage(damageAmount, player, p -> p.sendToolBreakStatus(Hand.MAIN_HAND));
         }

         if (offHand.isOf(ModItems.RED_GHOST_CANDLE)) {
            offHand.damage(damageAmount, player, p -> p.sendToolBreakStatus(Hand.OFF_HAND));
         }
      }
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.red_ghost_candle.description.source"));
      tooltip.add(Text.translatable("item.smfs.red_ghost_candle.description.desc"));
      tooltip.add(Text.translatable("item.smfs.red_ghost_candle.description.type"));
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }
}
