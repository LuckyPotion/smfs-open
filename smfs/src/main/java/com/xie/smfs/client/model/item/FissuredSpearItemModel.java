package com.xie.smfs.client.model.item;

import com.xie.smfs.item.FissuredSpearPurpleItem;
import com.xie.smfs.item.FissuredSpearRedItem;
import com.xie.smfs.item.WishSpearItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FissuredSpearItemModel extends GeoModel<FissuredSpearPurpleItem> {
   public Identifier getModelResource(FissuredSpearPurpleItem animatable) {
      return new Identifier("smfs", "geo/fissured_spear.geo.json");
   }

   public Identifier getTextureResource(FissuredSpearPurpleItem animatable) {
      if (animatable instanceof WishSpearItem) {
         return new Identifier("smfs", "textures/item/wish_spear.png");
      } else {
         return animatable instanceof FissuredSpearRedItem
            ? new Identifier("smfs", "textures/item/fissured_spear_red.png")
            : new Identifier("smfs", "textures/item/fissured_spear_purple.png");
      }
   }

   public Identifier getAnimationResource(FissuredSpearPurpleItem animatable) {
      return null;
   }
}
