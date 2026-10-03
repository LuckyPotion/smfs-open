package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class FurenMallMysteryInfoItem extends StructureMysteryInfoItem {
   public FurenMallMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "furen_mall", World.field_25179);
   }
}
