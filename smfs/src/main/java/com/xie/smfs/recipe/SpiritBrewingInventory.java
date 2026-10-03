package com.xie.smfs.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface SpiritBrewingInventory extends Inventory {
   ItemStack getMaterial(int i);

   ItemStack getCore();
}
