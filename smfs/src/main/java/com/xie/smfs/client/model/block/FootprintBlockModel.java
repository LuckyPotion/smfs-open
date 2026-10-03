package com.xie.smfs.client.model.block;

import com.xie.smfs.block.entity.FootprintBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FootprintBlockModel extends GeoModel<FootprintBlockEntity> {
   public Identifier getModelResource(FootprintBlockEntity animatable) {
      return new Identifier("smfs", "geo/block/footprint.geo.json");
   }

   public Identifier getTextureResource(FootprintBlockEntity animatable) {
      return new Identifier("smfs", "textures/block/footprint.png");
   }

   public Identifier getAnimationResource(FootprintBlockEntity animatable) {
      return null;
   }
}
