package com.xie.smfs.client;

import com.xie.smfs.item.SpiritPotionItem;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.ItemConvertible;

public class PotionColorProvider {
   public static void register() {
      ColorProviderRegistry.ITEM
         .register(
            (ItemColorProvider)(stack, tintIndex) -> {
               if (tintIndex == 1 && stack.getItem() instanceof SpiritPotionItem) {
                  SpiritPotionItem potionItem = (SpiritPotionItem)stack.getItem();
                  return potionItem.getPotionColor();
               } else {
                  return -1;
               }
            },
            new ItemConvertible[]{
               ModItems.KNOCKING_CURSE_POTION,
               ModItems.GHOST_KNOCK_POTION,
               ModItems.LOST_POTION,
               ModItems.STARVING_GHOST_CURSE_POTION,
               ModItems.SPIRIT_SURGE_POTION,
               ModItems.RED_GHOST_DOMAIN_POTION,
               ModItems.THICK_FOG_POTION,
               ModItems.FATAL_POISON_POTION,
               ModItems.TRAUMA_CURSE_POTION,
               ModItems.ILLUSION_CURSE_POTION,
               ModItems.SILENCE_POTION,
               ModItems.GREEN_GHOST_DOMAIN_POTION,
               ModItems.BLUE_GHOST_DOMAIN_POTION,
               ModItems.GRAY_GHOST_DOMAIN_POTION,
               ModItems.PURPLE_GHOST_DOMAIN_POTION,
               ModItems.GOLDEN_GHOST_DOMAIN_POTION,
               ModItems.BLACK_GHOST_DOMAIN_POTION,
               ModItems.CYAN_GHOST_DOMAIN_POTION,
               ModItems.MARK_CURSE_POTION,
               ModItems.SPIRIT_EROSION_POTION,
               ModItems.GHOST_WIND_POTION,
               ModItems.GHOST_SUPPRESSION_POTION,
               ModItems.GHOST_PRESSURE_POTION,
               ModItems.SCAPEGOAT_MARK_POTION,
               ModItems.SPIRIT_IMMUNITY_POTION,
               ModItems.PURIFICATION_POTION,
               ModItems.GHOST_DEADLOCK_POTION,
               ModItems.REVIVAL_SUPPRESSION_POTION,
               ModItems.DISGUSTING_LIQUID
            }
         );
   }
}
