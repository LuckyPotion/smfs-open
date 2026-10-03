package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class KnockingCursePotionItem extends SpiritPotionItem {
   public KnockingCursePotionItem(Settings settings) {
      super(settings, ModEffects.KNOCKING_CURSE, 600, 0, 4860970);
   }
}
