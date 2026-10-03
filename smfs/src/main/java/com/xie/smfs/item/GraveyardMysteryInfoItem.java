package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class GraveyardMysteryInfoItem extends StructureMysteryInfoItem {
   public GraveyardMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "graveyard", World.field_25179);
   }
}
