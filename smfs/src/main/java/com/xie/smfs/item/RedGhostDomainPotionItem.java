package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class RedGhostDomainPotionItem extends SpiritPotionItem {
   public RedGhostDomainPotionItem(Settings settings) {
      super(settings, ModEffects.RED_GHOST_DOMAIN, 600, 0, 16711680);
   }
}
