package com.xie.smfs.client.renderer;

import com.xie.smfs.block.entity.GhostCoffinBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CoffinBlockRenderer extends GeoBlockRenderer<GhostCoffinBlockEntity> {
   public CoffinBlockRenderer(Context ctx) {
      super(new GeoModel<GhostCoffinBlockEntity>() {
         public Identifier getModelResource(GhostCoffinBlockEntity animatable) {
            return new Identifier("smfs", "geo/ghost_coffin.geo.json");
         }

         public Identifier getTextureResource(GhostCoffinBlockEntity animatable) {
            return new Identifier("smfs", "textures/block/ghost_coffin.png");
         }

         public Identifier getAnimationResource(GhostCoffinBlockEntity animatable) {
            return new Identifier("smfs", "animations/ghost_coffin.animation.json");
         }
      });
   }
}
