package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class GhostSuppressionPotionItem extends SpiritPotionItem {
   public GhostSuppressionPotionItem(Settings settings) {
      super(settings, ModEffects.GHOST_SUPPRESSION, 1200, 0, 9127187);
   }
}
