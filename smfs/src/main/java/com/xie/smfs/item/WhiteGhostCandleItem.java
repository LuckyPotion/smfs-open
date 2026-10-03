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
      super(settings.method_7895(100));
   }

   public void method_7888(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.method_7888(stack, world, entity, slot, selected);
      if (!world.field_9236 && entity instanceof PlayerEntity player && (player.method_6047() == stack || player.method_6079() == stack)) {
         this.attractNearbyGhosts(stack, player);
         Hand hand = player.method_6047() == stack ? Hand.field_5808 : Hand.field_5810;
         if (world.method_8510() % 20L == 0L && !stack.method_7960()) {
            stack.method_7956(1, player, p -> p.method_20236(hand));
         }
      }
   }

   private void attractNearbyGhosts(ItemStack stack, PlayerEntity player) {
      World world = player.method_37908();
      List<GhostEntity> nearbyGhosts = world.method_8390(GhostEntity.class, player.method_5829().method_1014(20.0), ghostx -> true);
      if (!nearbyGhosts.isEmpty()) {
         for (GhostEntity ghost : nearbyGhosts) {
            ghost.method_5980(player);
         }
      }
   }

   public static boolean isHoldingWhiteCandle(PlayerEntity player) {
      return player.method_6047().method_31574(ModItems.WHITE_GHOST_CANDLE) || player.method_6079().method_31574(ModItems.WHITE_GHOST_CANDLE);
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.white_ghost_candle.description.source"));
      tooltip.add(Text.method_43471("item.smfs.white_ghost_candle.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.white_ghost_candle.description.type"));
   }

   public boolean method_7870(ItemStack stack) {
      return false;
   }
}
