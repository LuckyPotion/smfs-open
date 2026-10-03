package com.xie.smfs.recipe;

import net.minecraft.recipe.CookingRecipeSerializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GhostFurnaceRecipeType implements RecipeType<GhostFurnaceRecipe> {
   public static final GhostFurnaceRecipeType INSTANCE = new GhostFurnaceRecipeType();
   public static final RecipeSerializer<GhostFurnaceRecipe> SERIALIZER = new CookingRecipeSerializer(GhostFurnaceRecipe::new, 300);

   private GhostFurnaceRecipeType() {
   }

   public static void register() {
      Registry.method_10230(Registries.field_41188, new Identifier("smfs", "ghost_furnace"), INSTANCE);
      Registry.method_10230(Registries.field_41189, new Identifier("smfs", "ghost_furnace"), SERIALIZER);
   }

   @Override
   public String toString() {
      return "smfs:ghost_furnace";
   }
}
