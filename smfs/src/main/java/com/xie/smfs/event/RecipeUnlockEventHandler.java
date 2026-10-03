package com.xie.smfs.event;

import com.xie.smfs.Smfs;
import com.xie.smfs.manager.RecipeUnlockManager;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.CopyFrom;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.StartTick;
import net.minecraft.server.network.ServerPlayerEntity;

public class RecipeUnlockEventHandler {
   private static final Set<String> PLAYERS_CHECKED_THIS_TICK = new HashSet<>();

   public static void register() {
      ServerPlayerEvents.AFTER_RESPAWN.register((AfterRespawn)(oldPlayer, newPlayer, alive) -> checkRecipeUnlocks(newPlayer));
      ServerTickEvents.START_SERVER_TICK.register((StartTick)server -> {
         PLAYERS_CHECKED_THIS_TICK.clear();
         server.method_3760().method_14571().forEach(player -> {
            if (!PLAYERS_CHECKED_THIS_TICK.contains(player.method_5845())) {
               checkRecipeUnlocks(player);
               PLAYERS_CHECKED_THIS_TICK.add(player.method_5845());
            }
         });
      });
      ServerPlayerEvents.COPY_FROM.register((CopyFrom)(oldPlayer, newPlayer, alive) -> checkInventoryChanges(oldPlayer, newPlayer));
      Smfs.LOGGER.info("配方解锁事件处理器已注册");
   }

   private static void checkRecipeUnlocks(ServerPlayerEntity player) {
      for (String recipeId : RecipeUnlockManager.getUnlockConditions().keySet()) {
         if (!RecipeUnlockManager.isRecipeUnlocked(player, recipeId)) {
            RecipeUnlockManager.UnlockCondition condition = RecipeUnlockManager.getUnlockCondition(recipeId);
            if (condition != null && condition.isMet(player)) {
               RecipeUnlockManager.unlockRecipe(player, recipeId);
            }
         }
      }
   }

   private static void checkInventoryChanges(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
      for (String recipeId : RecipeUnlockManager.getUnlockConditions().keySet()) {
         if (!RecipeUnlockManager.isRecipeUnlocked(newPlayer, recipeId)) {
            RecipeUnlockManager.UnlockCondition condition = RecipeUnlockManager.getUnlockCondition(recipeId);
            if (condition != null && condition.isMet(newPlayer)) {
               RecipeUnlockManager.unlockRecipe(newPlayer, recipeId);
            }
         }
      }
   }

   public static void forceCheckPlayer(ServerPlayerEntity player) {
      checkRecipeUnlocks(player);
   }

   public static boolean shouldRecipeUnlock(ServerPlayerEntity player, String recipeId) {
      RecipeUnlockManager.UnlockCondition condition = RecipeUnlockManager.getUnlockCondition(recipeId);
      return condition != null && condition.isMet(player);
   }

   public static Set<String> getLockedRecipes(ServerPlayerEntity player) {
      Set<String> lockedRecipes = new HashSet<>();

      for (String recipeId : RecipeUnlockManager.getUnlockConditions().keySet()) {
         if (!RecipeUnlockManager.isRecipeUnlocked(player, recipeId)) {
            lockedRecipes.add(recipeId);
         }
      }

      return lockedRecipes;
   }

   public static Set<String> getUnlockedRecipes(ServerPlayerEntity player) {
      return RecipeUnlockManager.getUnlockedRecipes(player);
   }
}
