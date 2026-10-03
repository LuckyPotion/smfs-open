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
      super(settings.method_7895(100));
   }

   public void method_7888(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.method_7888(stack, world, entity, slot, selected);
      if (!world.field_9236 && entity instanceof PlayerEntity player && (player.method_6047() == stack || player.method_6079() == stack)) {
         this.checkAndConsumeDurability(stack, player);
      }
   }

   private void checkAndConsumeDurability(ItemStack stack, PlayerEntity player) {
      World world = player.method_37908();
      List<GhostEntity> nearbyGhosts = world.method_8390(GhostEntity.class, player.method_5829().method_1014(20.0), ghostx -> true);
      if (!nearbyGhosts.isEmpty()) {
         float maxConsumptionRate = 0.0F;

         for (GhostEntity ghost : nearbyGhosts) {
            double distance = player.method_5858(ghost);
            if (distance <= 400.0) {
               double actualDistance = Math.sqrt(distance);
               float consumptionRate = this.calculateConsumptionRate(actualDistance, ghost.getTerrorLevel());
               if (consumptionRate > maxConsumptionRate) {
                  maxConsumptionRate = consumptionRate;
               }
            }
         }

         if (maxConsumptionRate > 0.0F && world.field_9229.method_43057() < maxConsumptionRate) {
            stack.method_7956(1, player, p -> p.method_20236(p.method_6058()));
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
      return player.method_6047().method_31574(ModItems.RED_GHOST_CANDLE) || player.method_6079().method_31574(ModItems.RED_GHOST_CANDLE);
   }

   public static void consumeDurabilityOnSpiritDamage(PlayerEntity player, float spiritDamage) {
      if (!(spiritDamage <= 0.0F) && !player.method_37908().field_9236) {
         int damageAmount = 10;
         ItemStack mainHand = player.method_6047();
         ItemStack offHand = player.method_6079();
         if (mainHand.method_31574(ModItems.RED_GHOST_CANDLE)) {
            mainHand.method_7956(damageAmount, player, p -> p.method_20236(Hand.field_5808));
         }

         if (offHand.method_31574(ModItems.RED_GHOST_CANDLE)) {
            offHand.method_7956(damageAmount, player, p -> p.method_20236(Hand.field_5810));
         }
      }
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.red_ghost_candle.description.source"));
      tooltip.add(Text.method_43471("item.smfs.red_ghost_candle.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.red_ghost_candle.description.type"));
   }

   public boolean method_7870(ItemStack stack) {
      return false;
   }
}
