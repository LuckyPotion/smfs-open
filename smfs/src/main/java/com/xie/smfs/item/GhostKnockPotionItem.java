package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class GhostKnockPotionItem extends SpiritPotionItem {
   public GhostKnockPotionItem(Settings settings) {
      super(settings, ModEffects.GHOST_KNOCK, 600, 0, 9109504);
   }
}
