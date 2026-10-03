package com.xie.smfs.client.model.item;

import com.xie.smfs.item.GraveMoundBlockItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GraveMoundItemModel extends GeoModel<GraveMoundBlockItem> {
   public Identifier getModelResource(GraveMoundBlockItem animatable) {
      return new Identifier("smfs", "geo/grave_mound.geo.json");
   }

   public Identifier getTextureResource(GraveMoundBlockItem animatable) {
      return new Identifier("smfs", "textures/block/grave_mound.png");
   }

   public Identifier getAnimationResource(GraveMoundBlockItem animatable) {
      return null;
   }
}
