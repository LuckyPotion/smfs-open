package com.xie.smfs.event;

import com.xie.smfs.Smfs;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.GhostUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents.Modify;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;
import net.minecraft.util.Identifier;

public class ModLootTableEvents {
   public static void register() {
      Smfs.LOGGER.info("[ModLootTableEvents] 注册战利品表事件");
      LootTableEvents.MODIFY.register((Modify)(resourceManager, lootManager, id, tableBuilder, source) -> {
         if (source.isBuiltin()) {
            if (id.toString().contains("smfs:chests")) {
               Smfs.LOGGER.info("[ModLootTableEvents] 处理战利品表: {}", id);
            }

            for (int i = 1; i <= 12; i++) {
               if (Identifier.of("smfs", "chests/home_" + i).equals(id)) {
                  addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.12F);
                  addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.06F);
                  addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
                  addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.24F);
                  addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.12F);
                  addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
                  addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
                  addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.4F);
                  addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.4F);
                  addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.4F);
                  addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.1F);
                  addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.02F);
                  addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.01F);
                  addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.01F);
                  addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.12F);
                  addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.02F);
                  addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.01F);
                  addItemToLootTable(tableBuilder, ModItems.GOLDEN_PISTOL, 0.01F);
                  addItemToLootTable(tableBuilder, ModItems.CHINESE_MEDICINE_SHOP_MYSTERY_INFO, 0.02F);
                  addRandomGhostControlItemToLootTable(tableBuilder, 0.001F);
                  break;
               }
            }

            if (Identifier.of("smfs", "chests/chinese_medicine_shop").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.03F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.025F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.015F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.015F);
               addItemToLootTable(tableBuilder, ModItems.FUREN_MALL_MYSTERY_INFO, 0.6F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CHINESE_MEDICINE, 0.05F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/furen_mall").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.05F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.CAESAR_HOTEL_MYSTERY_INFO, 0.6F);
               addItemToLootTable(tableBuilder, ModItems.DECEPTION_GHOST_NECKLACE, 0.05F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/caesar_hotel").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.07F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.RUSTY_FIREWOOD_KNIFE, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.GRAVEYARD_MYSTERY_INFO, 0.6F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/graveyard").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.07F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.COFFIN_NAIL, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.LUO_QIAN_SUMMON_INCENSE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.ANCIENT_HOUSE_MYSTERY_INFO, 0.5F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/ancient_house").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.1F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.25F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.DECEPTION_GHOST_NECKLACE, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CHINESE_MEDICINE, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.RUSTY_FIREWOOD_KNIFE, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.RUSTY_OLD_BROADSWORD, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.TERRIFYING_CORPSE_SKIN, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.COFFIN_NAIL, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BUDDHA_BEADS, 0.01F);
               addItemToLootTable(tableBuilder, ModItems.BAISHUI_TOWN_MYSTERY_INFO, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.ANCIENT_HOUSE_DIARY, 0.06F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.01F);
            }

            if (Identifier.of("smfs", "chests/ghost_post_office").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.07F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.CHINESE_MEDICINE_SHOP_MYSTERY_INFO, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.CAESAR_HOTEL_MYSTERY_INFO, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.FUREN_MALL_MYSTERY_INFO, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.GRAVEYARD_MYSTERY_INFO, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.ANCIENT_HOUSE_MYSTERY_INFO, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.MYSTERY_INFO, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.SCHOOL_MYSTERY_INFO, 0.08F);
               addItemToLootTable(tableBuilder, ModItems.BAISHUI_TOWN_MYSTERY_INFO, 0.08F);
               addItemToLootTable(tableBuilder, ModItems.MANOR_MYSTERY_INFO, 0.08F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_LAKE_MYSTERY_INFO, 0.08F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_POST_OFFICE_DIARY, 0.06F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/baishui_town").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.03F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.025F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.015F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.015F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_FAMILY_PORTRAIT, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_SHROUD_CHESTPLATE, 0.03F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/school").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.07F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.07F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.02F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_SHROUD_CHESTPLATE, 0.03F);
               addItemToLootTable(tableBuilder, ModItems.MYSTERY_INFO, 0.2F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/manor").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_SCISSORS, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_AXE, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BOW, 0.1F);
               addMutuallyExclusiveItemsToLootTable(tableBuilder, ModItems.RUSTY_FRUIT_KNIFE, ModItems.GHOST_WOOD_HAMMER, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.FILTHY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.DIRTY_SEED, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_3, 0.1F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_MONEY_7, 0.15F);
               addItemToLootTable(tableBuilder, ModItems.PHOTO, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_OIL, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.CORPSE_PIECE, 0.9F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_CANDY, 0.4F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_RECORD, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.GOLDEN_BULLET, 0.2F);
               addItemToLootTable(tableBuilder, ModItems.EERIE_YELLOW_PAPER, 0.25F);
               addItemToLootTable(tableBuilder, ModItems.WORM_EATEN_BONE, 0.05F);
               addItemToLootTable(tableBuilder, ModItems.VISCOUS_BLOOD, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.BLACKENED_TOOTH, 0.04F);
               addItemToLootTable(tableBuilder, ModItems.GHOST_BUDDHA_BEADS, 0.01F);
               addRandomGhostControlItemToLootTable(tableBuilder, 0.005F);
            }

            if (Identifier.of("smfs", "chests/bone_tree").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.SILENT_GHOST_EYE, 1.0F);
            }

            if (Identifier.of("smfs", "chests/ghost_lake").equals(id)) {
               addItemToLootTable(tableBuilder, ModItems.GHOST_LAKE, 1.0F);
            }

            addGhostInfoBooksToVanillaChests(id, tableBuilder);
            addFishingLoot(id, tableBuilder);
         }
      });
      ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted)server -> {});
   }

   private static void addItemToLootTable(Builder tableBuilder, Item item, float chance) {
      net.minecraft.loot.LootPool.Builder poolBuilder = LootPool.builder()
         .rolls(ConstantLootNumberProvider.create(1.0F))
         .conditionally(RandomChanceLootCondition.builder(chance).build())
         .with(ItemEntry.builder(item));
      tableBuilder.pool(poolBuilder);
   }

   private static void addItemToLootTable(Builder tableBuilder, Item item, float chance, int minCount, int maxCount) {
      net.minecraft.loot.LootPool.Builder poolBuilder = LootPool.builder()
         .rolls(ConstantLootNumberProvider.create(1.0F))
         .conditionally(RandomChanceLootCondition.builder(chance).build())
         .with(ItemEntry.builder(item).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(minCount, maxCount))));
      tableBuilder.pool(poolBuilder);
   }

   private static void addMutuallyExclusiveItemsToLootTable(Builder tableBuilder, Item item1, Item item2, float chance) {
      net.minecraft.loot.LootPool.Builder poolBuilder = LootPool.builder()
         .rolls(ConstantLootNumberProvider.create(1.0F))
         .conditionally(RandomChanceLootCondition.builder(chance).build())
         .with(ItemEntry.builder(item1))
         .with(ItemEntry.builder(item2));
      tableBuilder.pool(poolBuilder);
   }

   private static void addRandomGhostControlItemToLootTable(Builder tableBuilder, float chance) {
      Item randomGhostItem = GhostUtils.getRandomGhostControlItem();
      addItemToLootTable(tableBuilder, randomGhostItem, chance);
   }

   private static void addVanillaItemToLootTable(Builder tableBuilder, String itemId, int weight, int minCount, int maxCount) {
      String[] parts = itemId.split(":");
      String namespace = parts[0];
      String path = parts[1];
      net.minecraft.loot.LootPool.Builder poolBuilder = LootPool.builder()
         .rolls(ConstantLootNumberProvider.create(1.0F))
         .with(
            ItemEntry.builder((ItemConvertible)Identifier.of(namespace, path))
               .weight(weight)
               .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(minCount, maxCount)))
         );
      tableBuilder.pool(poolBuilder);
   }

   private static void addGhostInfoBooksToVanillaChests(Identifier id, Builder tableBuilder) {
      if (id.getNamespace().equals("minecraft") && id.getPath().startsWith("chests/")) {
         addGhostInfoBookToLootTable(tableBuilder, 0.05F);
      }
   }

   private static void addGhostInfoBookToLootTable(Builder tableBuilder, float chance) {
      net.minecraft.loot.LootPool.Builder poolBuilder = LootPool.builder()
         .rolls(ConstantLootNumberProvider.create(1.0F))
         .conditionally(RandomChanceLootCondition.builder(chance).build())
         .with(ItemEntry.builder(Items.WRITTEN_BOOK).apply(createRandomGhostBookNbtFunction()));
      tableBuilder.pool(poolBuilder);
   }

   private static net.minecraft.loot.function.ConditionalLootFunction.Builder createRandomGhostBookNbtFunction() {
      ModLootTableEvents.GhostBookInfo[] ghostBooks = new ModLootTableEvents.GhostBookInfo[]{
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（其一）",
            "总部",
            new String[]{
               "代号：敲门鬼。\n\n危害等级：A。\n\n恐怖等级：S。",
               "外貌特征：\n\n身穿黑色长杉，浑身满布尸斑的恐怖老人，疑似生前为民国时期顶尖驭鬼者。",
               "生成规则：\n\n一般在第五天过后的午夜时分，敲门鬼会进行敲门。\n所有听到敲门声的人都会被敲门鬼盯上。\n此后，敲门鬼会在一定时间内出现。",
               "杀人规律：\n\n被盯上的玩家会附带鬼敲门buff，敲门鬼会杀死所有携带鬼敲门buff的玩家，每杀死一个玩家会再次进行敲门，直到鬼蜮内没有活人存在，敲门鬼会离开。",
               "应对方法：\n\n尽量在夜晚远离所有房门。如被困鬼域内，可躲进黄金棺材，或手持红色鬼烛，等待敲门鬼自行离开。",
               "研究：\n\n目前资料来看，敲门鬼或许与[]有着联系，其身上可能存在的直接线索，但要小心其附带的尸band。"
            }
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（其二）",
            "总部",
            new String[]{
               "代号：鬼商。\n\n危害等级：C。\n\n恐怖等级：C。", "外貌特征：\n\n鬼商通常呈现为一名身着西装的无脸男。", "杀人规律：\n\n鬼商人本身不主动攻击，危害性小。但是交易期间请确保背包有足够的余额。", "应对方法：\n\n不做超额交易，始终保证背包内留有鬼钱。"
            }
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（其三）",
            "总部",
            new String[]{"代号：抬头鬼。\n\n危害等级：C。\n\n恐怖等级：C。", "外貌特征：\n\n抬头鬼呈现为一名始终抬着头的民国男子，眼神空洞，散发着阴冷气息。", "杀人规律：\n\n抬头鬼会杀死所有抬头的人。", "应对方法：\n\n避免头部上仰角度过大。"}
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（其四）", "总部", new String[]{"代号：开箱鬼。\n\n危害等级：C。\n\n恐怖等级：C。", "杀人规律：\n\n开箱鬼会攻击正在与箱子交互的玩家。", "应对方法：\n\n直接破坏箱子即可。"}
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（其五）", "总部", new String[]{"代号：村民鬼。\n\n危害等级：B。\n\n恐怖等级：B。", "杀人规律：\n\n所有被村民鬼袭击过的人都曾经攻击过村民，且村民鬼会同化村民为鬼奴，自身带有鬼蜮。", "应对方法：\n\n避免伤害村民，同时远离村民。"}
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（其六）", "总部", new String[]{"代号：不可触摸鬼。\n\n危害等级：C。\n\n恐怖等级：C。", "杀人规律：\n\n任何接触不可触摸鬼的生物都会受到致命的开裂诅咒。", "应对方法：\n\n远离，不主动招惹即可。"}
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（其七）", "总部", new String[]{"代号：鬼雾。\n\n危害等级：B。\n\n恐怖等级：B。", "杀人规律：\n\n鬼雾会袭击所有扰动雾气的人，且拥有鬼域。", "应对方法：\n\n被困鬼雾中，尽量避免长时间移动。"}
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（异类）",
            "总部",
            new String[]{
               "成为异类",
               "细则：\n\n驭鬼者身体与鬼高度融合以后会产生一种神奇的变化，此时其身体已经完全与鬼融合，但是意识仍然存在。",
               "研究：\n\n成为异类以后继承了鬼的部分特性，表现为不受任何物理伤害影响，目前研究来看，当一个人驾驭的鬼超过六只可能会达到这个状态。",
               "备注：\n\n请确保已开启驾驭鬼增强肉体相关配置。"
            }
         ),
         new ModLootTableEvents.GhostBookInfo(
            "灵异档案（死机）",
            "总部",
            new String[]{
               "厉鬼死机",
               "细则：\n\n驾驭一些规则相悖的鬼可能会在体内产生灵异对抗，从而发生神奇的反应。一般将其称之为死机状态。",
               "研究：\n\n死机状态下的鬼不会在体内复苏，但是这种平衡很微妙，目前已知的有抬头鬼与低头鬼，烧死鬼（鬼火）与淹死鬼以及同样具有重启权能的鬼眼和许愿鬼能够达到这个效果。"
            }
         )
      };
      int bookIndex = (int)(System.currentTimeMillis() % ghostBooks.length);
      ModLootTableEvents.GhostBookInfo selectedBook = ghostBooks[bookIndex];
      NbtCompound bookNbt = new NbtCompound();
      bookNbt.putString("title", selectedBook.title);
      bookNbt.putString("author", selectedBook.author);
      bookNbt.putBoolean("resolved", true);
      NbtList pages = new NbtList();

      for (String pageContent : selectedBook.pages) {
         String jsonText = Serializer.toJson(Text.literal(pageContent));
         pages.add(NbtString.of(jsonText));
      }

      bookNbt.put("pages", pages);
      return SetNbtLootFunction.builder(bookNbt);
   }

   private static void addFishingLoot(Identifier id, Builder tableBuilder) {
      if (id.getNamespace().equals("minecraft") && id.getPath().equals("gameplay/fishing")) {
         addItemToLootTable(tableBuilder, ModItems.GHOST_BUDDHA_BEADS, 0.05F);
      }

      if (id.getNamespace().equals("minecraft") && id.getPath().equals("gameplay/fishing/junk")) {
         addItemToLootTable(tableBuilder, ModItems.GHOST_BUDDHA_BEADS, 0.02F);
      }

      if (id.getNamespace().equals("minecraft") && id.getPath().equals("gameplay/fishing/treasure")) {
         addItemToLootTable(tableBuilder, ModItems.GHOST_BUDDHA_BEADS, 0.1F);
      }
   }

   private static class GhostBookInfo {
      public final String title;
      public final String author;
      public final String[] pages;

      public GhostBookInfo(String title, String author, String[] pages) {
         this.title = title;
         this.author = author;
         this.pages = pages;
      }
   }
}
