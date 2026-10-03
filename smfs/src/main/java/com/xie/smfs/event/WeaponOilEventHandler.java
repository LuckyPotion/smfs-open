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
      if (!world.field_9236 && hand == Hand.field_5808) {
         ItemStack mainHand = player.method_5998(Hand.field_5808);
         ItemStack offHand = player.method_5998(Hand.field_5810);
         if (offHand.method_7909() == ModItems.CORPSE_OIL && mainHand.method_7909() instanceof SwordItem && WeaponOilHandler.applyCorpseOil(player)) {
            return TypedActionResult.method_22427(mainHand);
         }
      }

      return TypedActionResult.method_22430(ItemStack.field_8037);
   }
}
