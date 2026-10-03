package com.xie.smfs.client.renderer;

import com.xie.smfs.block.entity.RedCoffinBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RedCoffinBlockRenderer extends GeoBlockRenderer<RedCoffinBlockEntity> {
   public RedCoffinBlockRenderer(Context ctx) {
      super(new GeoModel<RedCoffinBlockEntity>() {
         public Identifier getModelResource(RedCoffinBlockEntity animatable) {
            return new Identifier("smfs", "geo/red_coffin.geo.json");
         }

         public Identifier getTextureResource(RedCoffinBlockEntity animatable) {
            return new Identifier("smfs", "textures/block/red_coffin.png");
         }

         public Identifier getAnimationResource(RedCoffinBlockEntity animatable) {
            return new Identifier("smfs", "animations/red_coffin.animation.json");
         }
      });
   }
}
