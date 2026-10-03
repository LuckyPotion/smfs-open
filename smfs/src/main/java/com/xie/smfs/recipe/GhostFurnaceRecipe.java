package com.xie.smfs.recipe;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class GhostFurnaceRecipe extends AbstractCookingRecipe {
   public GhostFurnaceRecipe(Identifier id, String group, CookingRecipeCategory category, Ingredient input, ItemStack output, float experience, int cookTime) {
      super(GhostFurnaceRecipeType.INSTANCE, id, group, category, input, output, experience, cookTime);
   }

   public RecipeSerializer<?> method_8119() {
      return GhostFurnaceRecipeType.SERIALIZER;
   }

   public RecipeType<?> method_17716() {
      return GhostFurnaceRecipeType.INSTANCE;
   }

   public ItemStack method_17447() {
      return new ItemStack((ItemConvertible)Registries.field_41178.method_10223(new Identifier("smfs", "ghost_furnace")));
   }
}
