package com.xie.smfs.client.model.armor;

import com.xie.smfs.item.GhostShroudArmorItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GhostShroudArmorModel extends GeoModel<GhostShroudArmorItem> {
   public Identifier getModelResource(GhostShroudArmorItem animatable) {
      return new Identifier("smfs", "geo/ghost_shroud_chestplate.geo.json");
   }

   public Identifier getTextureResource(GhostShroudArmorItem animatable) {
      return new Identifier("smfs", "textures/item/armor/ghost_shroud_chestplate.png");
   }

   public Identifier getAnimationResource(GhostShroudArmorItem animatable) {
      return null;
   }
}
