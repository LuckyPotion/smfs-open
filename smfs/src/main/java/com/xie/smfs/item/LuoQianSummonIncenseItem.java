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

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (!world.isClient) {
         LuoQianGhostEntity entity = (LuoQianGhostEntity)ModEntities.LUO_QIAN_GHOST.create(world);
         if (entity != null) {
            entity.setPosition(user.getX(), user.getY(), user.getZ());
            world.spawnEntity(entity);
            if (!user.isCreative()) {
               stack.decrement(1);
            }
         }
      }

      return TypedActionResult.success(stack, world.isClient());
   }
}
