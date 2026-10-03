package com.xie.smfs.item;

import com.xie.smfs.Smfs;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public class AncientHouseMysteryInfoItem extends StructureMysteryInfoItem {
   public AncientHouseMysteryInfoItem() {
      super(new FabricItemSettings().maxCount(16), "ancient_house", Smfs.SPIRIT_REALM_DIMENSION);
   }
}
