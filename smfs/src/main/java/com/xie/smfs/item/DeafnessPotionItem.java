package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class DeafnessPotionItem extends SpiritPotionItem {
   public DeafnessPotionItem(Settings settings) {
      super(settings, ModEffects.DEAFNESS, 600, 0, 8421504);
   }
}
