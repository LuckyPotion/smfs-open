package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class PurificationPotionItem extends SpiritPotionItem {
   public PurificationPotionItem(Settings settings) {
      super(settings, ModEffects.PURIFICATION, 200, 0, 52224);
   }
}
