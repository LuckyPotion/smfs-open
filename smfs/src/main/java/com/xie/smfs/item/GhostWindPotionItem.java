package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class GhostWindPotionItem extends SpiritPotionItem {
   public GhostWindPotionItem(Settings settings) {
      super(settings, ModEffects.GHOST_WIND_EFFECT, 600, 0, 8900331);
   }
}
