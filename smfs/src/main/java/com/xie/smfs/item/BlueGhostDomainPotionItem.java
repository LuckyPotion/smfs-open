package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class BlueGhostDomainPotionItem extends SpiritPotionItem {
   public BlueGhostDomainPotionItem(Settings settings) {
      super(settings, ModEffects.BLUE_GHOST_DOMAIN, 600, 0, 255);
   }
}
