package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class GhostPostOfficeMysteryInfoItem extends StructureMysteryInfoItem {
   public GhostPostOfficeMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "ghost_post_office", World.field_25179);
   }
}
