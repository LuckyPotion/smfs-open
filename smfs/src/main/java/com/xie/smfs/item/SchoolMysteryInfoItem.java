package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class SchoolMysteryInfoItem extends StructureMysteryInfoItem {
   public SchoolMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "school", World.OVERWORLD);
   }
}
