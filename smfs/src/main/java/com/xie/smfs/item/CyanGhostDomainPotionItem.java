package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class CyanGhostDomainPotionItem extends SpiritPotionItem {
   public CyanGhostDomainPotionItem(Settings settings) {
      super(settings, ModEffects.CYAN_GHOST_DOMAIN, 600, 0, 65535);
   }
}
