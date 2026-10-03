package com.xie.smfs.event;

import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.WeaponOilHandler;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class WeaponOilEventHandler {
   public static void register() {
      UseItemCallback.EVENT.register(WeaponOilEventHandler::onItemUse);
   }

   private static TypedActionResult<ItemStack> onItemUse(PlayerEntity player, World world, Hand hand) {
      if (!world.isClient && hand == Hand.MAIN_HAND) {
         ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
         ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
         if (offHand.getItem() == ModItems.CORPSE_OIL && mainHand.getItem() instanceof SwordItem && WeaponOilHandler.applyCorpseOil(player)) {
            return TypedActionResult.success(mainHand);
         }
      }

      return TypedActionResult.pass(ItemStack.EMPTY);
   }
}
