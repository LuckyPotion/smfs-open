package com.xie.smfs.item;

import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.registry.ModEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class LuoQianSummonIncenseItem extends Item {
   public LuoQianSummonIncenseItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (!world.field_9236) {
         LuoQianGhostEntity entity = (LuoQianGhostEntity)ModEntities.LUO_QIAN_GHOST.method_5883(world);
         if (entity != null) {
            entity.method_5814(user.method_23317(), user.method_23318(), user.method_23321());
            world.method_8649(entity);
            if (!user.method_7337()) {
               stack.method_7934(1);
            }
         }
      }

      return TypedActionResult.method_29237(stack, world.method_8608());
   }
}
