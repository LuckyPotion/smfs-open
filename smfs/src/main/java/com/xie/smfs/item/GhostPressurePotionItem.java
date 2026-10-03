package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class GhostPressurePotionItem extends SpiritPotionItem {
   public GhostPressurePotionItem(Settings settings) {
      super(settings, ModEffects.GHOST_PRESSURE, 600, 0, 3100495);
   }
}
