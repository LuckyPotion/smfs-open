package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class GhostLakeMysteryInfoItem extends StructureMysteryInfoItem {
   public GhostLakeMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "ghost_lake", World.OVERWORLD);
   }
}
