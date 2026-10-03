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
      this.method_7621(new SpiritBrewingStandScreenHandler.GoldenContainerSlot(inventory, 3, 40, 35, propertyDelegate));
      this.method_7621(new SpiritBrewingStandScreenHandler.ModMaterialSlot(inventory, 0, 24, 19, propertyDelegate));
      this.method_7621(new SpiritBrewingStandScreenHandler.ModMaterialSlot(inventory, 1, 56, 19, propertyDelegate));
      this.method_7621(new SpiritBrewingStandScreenHandler.VanillaMaterialSlot(inventory, 2, 40, 53, propertyDelegate));
      this.method_7621(new SpiritBrewingStandScreenHandler.GlassBottleSlot(inventory, 4, 116, 35, propertyDelegate));

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.method_7621(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.method_7621(new Slot(playerInventory, i, 8 + i * 18, 142));
      }

      this.method_17360(propertyDelegate);
   }

   public boolean method_7597(PlayerEntity player) {
      return this.inventory.method_5443(player);
   }

   public ItemStack method_7601(PlayerEntity player, int slotIndex) {
      ItemStack itemStack = ItemStack.field_8037;
      Slot slot = (Slot)this.field_7761.get(slotIndex);
      boolean isBrewing = this.propertyDelegate.method_17390(2) == 1;
      if (isBrewing && slotIndex < 5) {
         return ItemStack.field_8037;
      }

      if (slot != null && slot.method_7681()) {
         ItemStack itemStack2 = slot.method_7677();
         itemStack = itemStack2.method_7972();
         if (slotIndex < 5) {
            if (!this.method_7616(itemStack2, 5, 41, true)) {
               return ItemStack.field_8037;
            }
         } else if (itemStack2.method_31574(Items.field_8469)) {
            if (!this.method_7616(itemStack2, 4, 5, false)) {
               return ItemStack.field_8037;
            }
         } else if (ModItems.isModMaterial(itemStack2.method_7909())) {
            if (!this.method_7616(itemStack2, 0, 2, false)) {
               return ItemStack.field_8037;
            }
         } else if (!this.method_7616(itemStack2, 2, 3, false)) {
            return ItemStack.field_8037;
         }

         if (itemStack2.method_7960()) {
            slot.method_48931(ItemStack.field_8037);
         } else {
            slot.method_7668();
         }

         if (itemStack2.method_7947() == itemStack.method_7947()) {
            return ItemStack.field_8037;
         }

         slot.method_7667(player, itemStack2);
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

      public int method_17390(int index) {
         return this.values[index];
      }

      public void method_17391(int index, int value) {
         this.values[index] = value;
      }

      public int method_17389() {
         return this.values.length;
      }
   }

   private abstract static class BrewingSlot extends Slot {
      protected final PropertyDelegate propertyDelegate;

      public BrewingSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y);
         this.propertyDelegate = propertyDelegate;
      }

      public boolean method_7674(PlayerEntity player) {
         boolean isBrewing = this.propertyDelegate.method_17390(2) == 1;
         return !isBrewing;
      }
   }

   private static class GlassBottleSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public GlassBottleSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean method_7680(ItemStack stack) {
         return stack.method_31574(Items.field_8469);
      }

      public int method_7675() {
         return 1;
      }

      public int method_7676(ItemStack stack) {
         return 1;
      }
   }

   private static class GoldenContainerSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public GoldenContainerSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean method_7680(ItemStack stack) {
         return stack.method_31574(ModItems.GOLDEN_CONTAINER)
            && stack.method_7948().method_10577("HasGhost")
            && stack.method_7948().method_10545("ContainedGhost");
      }
   }

   private static class ModMaterialSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public ModMaterialSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean method_7680(ItemStack stack) {
         return ModItems.isModMaterial(stack.method_7909());
      }
   }

   private static class SimpleInventory implements Inventory {
      private final ItemStack[] stacks;

      public SimpleInventory(int size) {
         this.stacks = new ItemStack[size];

         for (int i = 0; i < size; i++) {
            this.stacks[i] = ItemStack.field_8037;
         }
      }

      public int method_5439() {
         return this.stacks.length;
      }

      public boolean method_5442() {
         for (ItemStack stack : this.stacks) {
            if (!stack.method_7960()) {
               return false;
            }
         }

         return true;
      }

      public ItemStack method_5438(int slot) {
         return this.stacks[slot];
      }

      public ItemStack method_5434(int slot, int amount) {
         return Inventories.method_5430(Arrays.asList(this.stacks), slot, amount);
      }

      public ItemStack method_5441(int slot) {
         ItemStack result = this.stacks[slot];
         this.stacks[slot] = ItemStack.field_8037;
         return result;
      }

      public void method_5447(int slot, ItemStack stack) {
         this.stacks[slot] = stack;
         if (stack.method_7947() > this.method_5444()) {
            stack.method_7939(this.method_5444());
         }
      }

      public void method_5431() {
      }

      public boolean method_5443(PlayerEntity player) {
         return true;
      }

      public void method_5448() {
         for (int i = 0; i < this.stacks.length; i++) {
            this.stacks[i] = ItemStack.field_8037;
         }
      }
   }

   private static class VanillaMaterialSlot extends SpiritBrewingStandScreenHandler.BrewingSlot {
      public VanillaMaterialSlot(Inventory inventory, int index, int x, int y, PropertyDelegate propertyDelegate) {
         super(inventory, index, x, y, propertyDelegate);
      }

      public boolean method_7680(ItemStack stack) {
         return !ModItems.isModMaterial(stack.method_7909());
      }
   }
}
