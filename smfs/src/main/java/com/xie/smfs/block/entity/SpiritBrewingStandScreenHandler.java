package com.xie.smfs.block.entity;

import com.xie.smfs.registry.ModItems;
import com.xie.smfs.registry.ModScreenHandlers;
import java.util.Arrays;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SpiritBrewingStandScreenHandler extends ScreenHandler {
   private final Inventory inventory;
   private final PropertyDelegate propertyDelegate;

   public SpiritBrewingStandScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, new SpiritBrewingStandScreenHandler.SimpleInventory(5), new SpiritBrewingStandScreenHandler.ArrayPropertyDelegate(3));
   }

   public SpiritBrewingStandScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
      super(ModScreenHandlers.SPIRIT_BREWING_STAND_SCREEN_HANDLER, syncId);
      this.inventory = inventory;
      this.propertyDelegate = propertyDelegate;
      this.addSlot(new SpiritBrewingStandScreenHandler.GoldenContainerSlot(inventory, 3, 40, 35, propertyDelegate));
      this.addSlot(new SpiritBrewingStandScreenHandler.ModMaterialSlot(inventory, 0, 24, 19, propertyDelegate));
      this.addSlot(new SpiritBrewingStandScreenHandler.ModMaterialSlot(inventory, 1, 56, 19, propertyDelegate));
      this.addSlot(new SpiritBrewingStandScreenHandler.VanillaMaterialSlot(inventory, 2, 40, 53, propertyDelegate));
      this.addSlot(new SpiritBrewingStandScreenHandler.GlassBottleSlot(inventory, 4, 116, 35, propertyDelegate));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
      }

      this.addProperties(propertyDelegate);
   }

   public boolean canUse(PlayerEntity player) {
      return this.inventory.canPlayerUse(player);
   }

   public ItemStack quickMove(PlayerEntity player, int slot) {
      ItemStack itemStack = ItemStack.EMPTY;
      Slot slotx = (Slot)this.slots.get(slot);
      boolean isBrewing = this.propertyDelegate.get(2) == 1;
      if (isBrewing && slot < 5) {
         return ItemStack.EMPTY;
      }

      if (slotx != null && slotx.hasStack()) {
         ItemStack itemStack2 = slotx.getStack();
         itemStack = itemStack2.copy();
         if (slot < 5) {
            if (!this.insertItem(itemStack2, 5, 41, true)) {
               return ItemStack.EMPTY;
            }
         } else if (itemStack2.isOf(Items.GLASS_BOTTLE)) {
            if (!this.insertItem(itemStack2, 4, 5, false)) {
               return ItemStack.EMPTY;
            }
         } else if (ModItems.isModMaterial(itemStack2.getItem())) {
            if (!this.insertItem(itemStack2, 0, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.insertItem(itemStack2, 2, 3, false)) {
            return ItemStack.EMPTY;
         }

         if (itemStack2.isEmpty()) {
            slotx.setStack(ItemStack.EMPTY);
         } else {
            slotx.markDirty();
         }

         if (itemStack2.getCount() == itemStack.getCount()) {
            return ItemStack.EMPTY;
         }

         slotx.onTakeItem(player, itemStack2);
      }

      return itemStack;
   }

   public PropertyDelegate getPropertyDelegate() {
      return this.propertyDelegate;
   }

   private static class ArrayPropertyDelegate implements PropertyDelegate {
      private final int[] values;

      public ArrayPropertyDelegate(int size) {
         this.values = new int[size];
      }

      public int get(int index) {
         return this.values[index];
      }

      public void set(int index, int value) {
         this.values[index] = value;
      }

      public int size() {
         return this.values.length;
      }
   }

   private abstract static class BrewingSlot extends Slot {
      protected final PropertyDelegate propertyDelegate;

      public BrewingSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y);
         this.propertyDelegate = propertyDelegate;
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
         boolean isBrewing = this.propertyDelegate.get(2) == 1;
         return !isBrewing;
      }
   }

   private static class GlassBottleSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public GlassBottleSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean canInsert(ItemStack stack) {
         return stack.isOf(Items.GLASS_BOTTLE);
      }

      public int getMaxItemCount() {
         return 1;
      }

      public int getMaxItemCount(ItemStack stack) {
         return 1;
      }
   }

   private static class GoldenContainerSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public GoldenContainerSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean canInsert(ItemStack stack) {
         return stack.isOf(ModItems.GOLDEN_CONTAINER) && stack.getOrCreateNbt().getBoolean("HasGhost") && stack.getOrCreateNbt().contains("ContainedGhost");
      }
   }

   private static class ModMaterialSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public ModMaterialSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean canInsert(ItemStack stack) {
         return ModItems.isModMaterial(stack.getItem());
      }
   }

   private static class SimpleInventory implements Inventory {
      private final ItemStack[] stacks;

      public SimpleInventory(int size) {
         this.stacks = new ItemStack[size];

         for (int i = 0; i < size; i++) {
            this.stacks[i] = ItemStack.EMPTY;
         }
      }

      public int size() {
         return this.stacks.length;
      }

      public boolean isEmpty() {
         for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
               return false;
            }
         }

         return true;
      }

      public ItemStack getStack(int slot) {
         return this.stacks[slot];
      }

      public ItemStack removeStack(int slot, int amount) {
         return Inventories.splitStack(Arrays.asList(this.stacks), slot, amount);
      }

      public ItemStack removeStack(int slot) {
         ItemStack result = this.stacks[slot];
         this.stacks[slot] = ItemStack.EMPTY;
         return result;
      }

      public void setStack(int slot, ItemStack stack) {
         this.stacks[slot] = stack;
         if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
         }
      }

      public void markDirty() {
      }

      public boolean canPlayerUse(PlayerEntity player) {
         return true;
      }

      public void clear() {
         for (int i = 0; i < this.stacks.length; i++) {
            this.stacks[i] = ItemStack.EMPTY;
         }
      }
   }

   private static class VanillaMaterialSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public VanillaMaterialSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean canInsert(ItemStack stack) {
         return !ModItems.isModMaterial(stack.getItem());
      }
   }
}
