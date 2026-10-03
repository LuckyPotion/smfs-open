package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class ManorMysteryInfoItem extends StructureMysteryInfoItem {
   public ManorMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "manor", World.field_25179);
   }
}
