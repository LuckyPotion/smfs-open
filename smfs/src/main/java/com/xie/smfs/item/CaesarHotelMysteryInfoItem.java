package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class CaesarHotelMysteryInfoItem extends StructureMysteryInfoItem {
   public CaesarHotelMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "caesar_hotel", World.field_25179);
   }
}
