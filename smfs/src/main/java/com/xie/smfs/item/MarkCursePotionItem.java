package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class MarkCursePotionItem extends SpiritPotionItem {
   public MarkCursePotionItem(Settings settings) {
      super(settings, ModEffects.MARK_CURSE, 600, 0, 16766720);
   }
}
