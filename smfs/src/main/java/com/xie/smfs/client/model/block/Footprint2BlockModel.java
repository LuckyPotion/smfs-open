package com.xie.smfs.client.model.block;

import com.xie.smfs.block.entity.Footprint2BlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class Footprint2BlockModel extends GeoModel<Footprint2BlockEntity> {
   public Identifier getModelResource(Footprint2BlockEntity animatable) {
      return new Identifier("smfs", "geo/block/footprint2.geo.json");
   }

   public Identifier getTextureResource(Footprint2BlockEntity animatable) {
      return new Identifier("smfs", "textures/block/footprint2.png");
   }

   public Identifier getAnimationResource(Footprint2BlockEntity animatable) {
      return null;
   }
}
