package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class BaishuiTownMysteryInfoItem extends StructureMysteryInfoItem {
   public BaishuiTownMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "baishui_town", World.field_25179);
   }
}
