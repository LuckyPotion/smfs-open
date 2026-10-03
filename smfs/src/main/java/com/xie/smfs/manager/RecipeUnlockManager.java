package com.xie.smfs.manager;

import com.xie.smfs.Smfs;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RecipeUnlockManager {
   private static final HashMap<String, RecipeUnlockManager.UnlockCondition> UNLOCK_CONDITIONS = new HashMap<>();
   private static final HashMap<String, Set<String>> PLAYER_UNLOCKED_RECIPES = new HashMap<>();

   public static void registerUnlockCondition(String recipeId, RecipeUnlockManager.UnlockCondition condition) {
      UNLOCK_CONDITIONS.put(recipeId, condition);
      Smfs.LOGGER.debug("注册配方解锁条件: {}", recipeId);
   }

   public static boolean isRecipeUnlocked(PlayerEntity player, String recipeId) {
      String playerId = player.getUuidAsString();
      Set<String> unlockedRecipes = PLAYER_UNLOCKED_RECIPES.get(playerId);
      return !UNLOCK_CONDITIONS.containsKey(recipeId) ? true : unlockedRecipes != null && unlockedRecipes.contains(recipeId);
   }

   public static String getUnlockHint(String recipeId) {
      RecipeUnlockManager.UnlockCondition condition = UNLOCK_CONDITIONS.get(recipeId);
      return condition != null ? condition.getHint() : "";
   }

   public static void unlockRecipe(PlayerEntity player, String recipeId) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         String playerId = player.getUuidAsString();
         Set<String> unlockedRecipes = PLAYER_UNLOCKED_RECIPES.computeIfAbsent(playerId, k -> new HashSet<>());
         if (!unlockedRecipes.contains(recipeId)) {
            unlockedRecipes.add(recipeId);
            addToVanillaRecipeBook(serverPlayer, recipeId);
            Smfs.LOGGER.debug("玩家 {} 解锁配方: {}", player.getName().getString(), recipeId);
         }
      }
   }

   private static void addToVanillaRecipeBook(ServerPlayerEntity player, String recipeId) {
      try {
         Identifier recipeIdentifier = new Identifier(recipeId);
         Recipe<?> recipe = (Recipe<?>)player.getWorld().getRecipeManager().get(recipeIdentifier).orElse(null);
         if (recipe != null) {
            player.unlockRecipes(List.of(recipe));
            Smfs.LOGGER.debug("已将配方 {} 添加到玩家 {} 的原版配方书中", recipeId, player.getName().getString());
         }
      } catch (Exception e) {
         Smfs.LOGGER.warn("无法将配方 {} 添加到原版配方书: {}", recipeId, e.getMessage());
      }
   }

   private static String getRecipeDisplayName(String recipeId) {
      String[] parts = recipeId.split(":");
      if (parts.length > 1) {
         String itemName = parts[1];
         return itemName.replace("_", " ").toLowerCase();
      } else {
         return recipeId;
      }
   }

   public static void initializeDefaultConditions() {
      registerUnlockCondition(
         "smfs:ghost_faction",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("corpse_oil", 1), new RecipeUnlockManager.ItemOwnershipCondition("corpse_piece", 1)
         )
      );
      registerUnlockCondition("smfs:gold_coffin", new RecipeUnlockManager.ItemOwnershipCondition("gold_block", 1));
      registerUnlockCondition("smfs:golden_container", new RecipeUnlockManager.ItemOwnershipCondition("gold_ingot", 1));
      registerUnlockCondition("smfs:photo", new RecipeUnlockManager.ItemOwnershipCondition("paper", 1));
      registerUnlockCondition("smfs:golden_bullet", new RecipeUnlockManager.ItemOwnershipCondition("gold_ingot", 1));
      registerUnlockCondition("smfs:zhenwu_sword", new RecipeUnlockManager.ItemOwnershipCondition("iron_sword", 1));
      registerUnlockCondition("smfs:defiled_fragment_from_ghost_furnace", new RecipeUnlockManager.ItemOwnershipCondition("defiled_ore", 1));
      registerUnlockCondition("smfs:deep_defiled_fragment_from_ghost_furnace", new RecipeUnlockManager.ItemOwnershipCondition("deep_defiled_ore", 1));
      registerUnlockCondition("smfs:defiled_ingot_from_fragments", new RecipeUnlockManager.ItemOwnershipCondition("defiled_fragment", 9));
      registerUnlockCondition(
         "smfs:ghost_soup",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("underworld_fruit", 1),
            new RecipeUnlockManager.ItemOwnershipCondition("filthy_fruit", 1),
            new RecipeUnlockManager.ItemOwnershipCondition("corpse_oil", 1)
         )
      );
      registerUnlockCondition(
         "smfs:defiled_helmet_smithing",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("defiled_ingot", 1), new RecipeUnlockManager.ItemOwnershipCondition("gold_ingot", 1)
         )
      );
      registerUnlockCondition(
         "smfs:defiled_chestplate_smithing",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("defiled_ingot", 1), new RecipeUnlockManager.ItemOwnershipCondition("gold_ingot", 1)
         )
      );
      registerUnlockCondition(
         "smfs:defiled_leggings_smithing",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("defiled_ingot", 1), new RecipeUnlockManager.ItemOwnershipCondition("gold_ingot", 1)
         )
      );
      registerUnlockCondition(
         "smfs:defiled_boots_smithing",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("defiled_ingot", 1), new RecipeUnlockManager.ItemOwnershipCondition("gold_ingot", 1)
         )
      );
      registerUnlockCondition(
         "smfs:defiled_sword_smithing",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("defiled_ingot", 1), new RecipeUnlockManager.ItemOwnershipCondition("gold_ingot", 1)
         )
      );
      registerUnlockCondition(
         "smfs:funeral_music_ghost",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("gong_ghost", 1),
            new RecipeUnlockManager.ItemOwnershipCondition("suona_ghost", 1),
            new RecipeUnlockManager.ItemOwnershipCondition("crying_ghost", 1)
         )
      );
      registerUnlockCondition(
         "smfs:complete_shadow_ghost",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("giant_shadow_ghost", 1), new RecipeUnlockManager.ItemOwnershipCondition("ghost_shadow_head", 1)
         )
      );
      registerUnlockCondition(
         "smfs:fissured_spear_purple",
         new RecipeUnlockManager.OrCondition(
            new RecipeUnlockManager.ItemOwnershipCondition("rusty_firewood_knife", 1), new RecipeUnlockManager.ItemOwnershipCondition("coffin_nail", 1)
         )
      );
      registerUnlockCondition("smfs:fissured_spear_red", new RecipeUnlockManager.ItemOwnershipCondition("fissured_spear_purple", 1));
      registerUnlockCondition("smfs:wish_spear", new RecipeUnlockManager.ItemOwnershipCondition("fissured_spear_red", 1));
      Smfs.LOGGER.info("配方解锁管理器初始化完成，已注册 {} 个解锁条件", UNLOCK_CONDITIONS.size());
   }

   public static HashMap<String, RecipeUnlockManager.UnlockCondition> getUnlockConditions() {
      return UNLOCK_CONDITIONS;
   }

   public static RecipeUnlockManager.UnlockCondition getUnlockCondition(String recipeId) {
      return UNLOCK_CONDITIONS.get(recipeId);
   }

   public static Set<String> getUnlockedRecipes(PlayerEntity player) {
      String playerId = player.getUuidAsString();
      return PLAYER_UNLOCKED_RECIPES.getOrDefault(playerId, new HashSet<>());
   }

   public static class AdvancementCondition implements RecipeUnlockManager.UnlockCondition {
      private final String advancementId;

      public AdvancementCondition(String advancementId) {
         this.advancementId = advancementId;
      }

      @Override
      public boolean isMet(PlayerEntity player) {
         if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return false;
         } else {
            Advancement advancement = serverPlayer.server.getAdvancementLoader().get(new Identifier(this.advancementId));
            return advancement != null && serverPlayer.getAdvancementTracker().getProgress(advancement).isDone();
         }
      }

      @Override
      public String getHint() {
         return "需要完成进度: " + this.advancementId;
      }
   }

   public static class CombinedCondition implements RecipeUnlockManager.UnlockCondition {
      private final RecipeUnlockManager.UnlockCondition[] conditions;

      public CombinedCondition(RecipeUnlockManager.UnlockCondition... conditions) {
         this.conditions = conditions;
      }

      @Override
      public boolean isMet(PlayerEntity player) {
         for (RecipeUnlockManager.UnlockCondition condition : this.conditions) {
            if (!condition.isMet(player)) {
               return false;
            }
         }

         return true;
      }

      @Override
      public String getHint() {
         StringBuilder hint = new StringBuilder();

         for (int i = 0; i < this.conditions.length; i++) {
            if (i > 0) {
               hint.append(" 且 ");
            }

            hint.append(this.conditions[i].getHint());
         }

         return hint.toString();
      }
   }

   public static class ItemOwnershipCondition implements RecipeUnlockManager.UnlockCondition {
      private final String itemId;
      private final int count;

      public ItemOwnershipCondition(String itemId, int count) {
         this.itemId = itemId;
         this.count = count;
      }

      @Override
      public boolean isMet(PlayerEntity player) {
         int foundCount = 0;

         for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem().getTranslationKey().contains(this.itemId)) {
               foundCount += stack.getCount();
            }
         }

         return foundCount >= this.count;
      }

      @Override
      public String getHint() {
         return "需要拥有 " + this.count + " 个 " + this.itemId;
      }
   }

   public static class LevelCondition implements RecipeUnlockManager.UnlockCondition {
      private final int requiredLevel;

      public LevelCondition(int requiredLevel) {
         this.requiredLevel = requiredLevel;
      }

      @Override
      public boolean isMet(PlayerEntity player) {
         return player.experienceLevel >= this.requiredLevel;
      }

      @Override
      public String getHint() {
         return "需要达到等级 " + this.requiredLevel;
      }
   }

   public static class OrCondition implements RecipeUnlockManager.UnlockCondition {
      private final RecipeUnlockManager.UnlockCondition[] conditions;

      public OrCondition(RecipeUnlockManager.UnlockCondition... conditions) {
         this.conditions = conditions;
      }

      @Override
      public boolean isMet(PlayerEntity player) {
         for (RecipeUnlockManager.UnlockCondition condition : this.conditions) {
            if (condition.isMet(player)) {
               return true;
            }
         }

         return false;
      }

      @Override
      public String getHint() {
         StringBuilder hint = new StringBuilder();

         for (int i = 0; i < this.conditions.length; i++) {
            if (i > 0) {
               hint.append(" 或 ");
            }

            hint.append(this.conditions[i].getHint());
         }

         return hint.toString();
      }
   }

   public interface UnlockCondition {
      boolean isMet(PlayerEntity playerEntity);

      String getHint();
   }
}
