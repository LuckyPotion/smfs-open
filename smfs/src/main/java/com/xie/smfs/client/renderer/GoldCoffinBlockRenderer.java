package com.xie.smfs.client.renderer;

import com.xie.smfs.block.entity.GoldCoffinBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GoldCoffinBlockRenderer extends GeoBlockRenderer<GoldCoffinBlockEntity> {
   public GoldCoffinBlockRenderer(Context ctx) {
      super(new GeoModel<GoldCoffinBlockEntity>() {
         public Identifier getModelResource(GoldCoffinBlockEntity animatable) {
            return new Identifier("smfs", "geo/gold_coffin.geo.json");
         }

         public Identifier getTextureResource(GoldCoffinBlockEntity animatable) {
            return new Identifier("smfs", "textures/block/gold_coffin.png");
         }

         public Identifier getAnimationResource(GoldCoffinBlockEntity animatable) {
            return new Identifier("smfs", "animations/gold_coffin.animation.json");
         }
      });
   }
}
