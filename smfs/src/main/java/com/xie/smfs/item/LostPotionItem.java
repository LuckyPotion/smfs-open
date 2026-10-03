package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class LostPotionItem extends SpiritPotionItem {
   public LostPotionItem(Settings settings) {
      super(settings, ModEffects.LOST, 600, 0, 8421504);
   }
}
