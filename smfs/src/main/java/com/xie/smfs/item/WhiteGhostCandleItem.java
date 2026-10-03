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

public class WhiteGhostCandleItem extends Item {
   private static final int BASE_DURABILITY = 100;
   private static final double DISTANCE_THRESHOLD = 20.0;

   public WhiteGhostCandleItem(Settings settings) {
      super(settings.maxDamage(100));
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, world, entity, slot, selected);
      if (!world.isClient && entity instanceof PlayerEntity player && (player.getMainHandStack() == stack || player.getOffHandStack() == stack)) {
         this.attractNearbyGhosts(stack, player);
         Hand hand = player.getMainHandStack() == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
         if (world.getTime() % 20L == 0L && !stack.isEmpty()) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
         }
      }
   }

   private void attractNearbyGhosts(ItemStack stack, PlayerEntity player) {
      World world = player.getWorld();
      List<GhostEntity> nearbyGhosts = world.getEntitiesByClass(GhostEntity.class, player.getBoundingBox().expand(20.0), ghostx -> true);
      if (!nearbyGhosts.isEmpty()) {
         for (GhostEntity ghost : nearbyGhosts) {
            ghost.setTarget(player);
         }
      }
   }

   public static boolean isHoldingWhiteCandle(PlayerEntity player) {
      return player.getMainHandStack().isOf(ModItems.WHITE_GHOST_CANDLE) || player.getOffHandStack().isOf(ModItems.WHITE_GHOST_CANDLE);
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.white_ghost_candle.description.source"));
      tooltip.add(Text.translatable("item.smfs.white_ghost_candle.description.desc"));
      tooltip.add(Text.translatable("item.smfs.white_ghost_candle.description.type"));
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }
}
