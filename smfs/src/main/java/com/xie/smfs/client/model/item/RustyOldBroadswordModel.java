package com.xie.smfs.client.model.item;

import com.xie.smfs.item.RustyOldBroadswordItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RustyOldBroadswordModel extends GeoModel<RustyOldBroadswordItem> {
   public Identifier getModelResource(RustyOldBroadswordItem animatable) {
      return new Identifier("smfs", "geo/rusty_old_broadsword.geo.json");
   }

   public Identifier getTextureResource(RustyOldBroadswordItem animatable) {
      return new Identifier("smfs", "textures/item/rusty_old_broadsword.png");
   }

   public Identifier getAnimationResource(RustyOldBroadswordItem animatable) {
      return null;
   }
}
