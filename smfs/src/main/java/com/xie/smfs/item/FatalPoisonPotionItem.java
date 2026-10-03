package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class FatalPoisonPotionItem extends SpiritPotionItem {
   public FatalPoisonPotionItem(Settings settings) {
      super(settings, ModEffects.FATAL_POISON, 600, 0, 4915330);
   }
}
