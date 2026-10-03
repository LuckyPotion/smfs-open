package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class SilencePotionItem extends SpiritPotionItem {
   public SilencePotionItem(Settings settings) {
      super(settings, ModEffects.SILENCE, 600, 0, 3100495);
   }
}
