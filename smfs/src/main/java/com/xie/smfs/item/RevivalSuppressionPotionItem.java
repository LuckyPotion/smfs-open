package com.xie.smfs.item;

import com.xie.smfs.registry.ModEffects;
import net.minecraft.item.Item.Settings;

public class RevivalSuppressionPotionItem extends SpiritPotionItem {
   public RevivalSuppressionPotionItem(Settings settings) {
      super(settings, ModEffects.REVIVAL_SUPPRESSION, 1200, 0, 4286945);
   }
}
