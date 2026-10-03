package com.xie.smfs.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.World;

public class ChineseMedicineShopMysteryInfoItem extends StructureMysteryInfoItem {
   public ChineseMedicineShopMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "chinese_medicine_shop", World.field_25179);
   }
}
