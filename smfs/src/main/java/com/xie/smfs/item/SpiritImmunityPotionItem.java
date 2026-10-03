package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class SpiritImmunityPotionItem extends SpiritPotionItem {
   public SpiritImmunityPotionItem(Settings settings) {
      super(settings, ModEffects.SPIRIT_IMMUNITY, 1200, 0, 16766720);
   }
}
