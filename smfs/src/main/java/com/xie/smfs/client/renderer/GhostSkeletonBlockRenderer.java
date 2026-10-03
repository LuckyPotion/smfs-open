package com.xie.smfs.client.renderer;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GhostSkeletonBlockRenderer extends GeoBlockRenderer<StaticAnimatable> {
   public GhostSkeletonBlockRenderer(Context ctx) {
      super(new GeoModel<StaticAnimatable>() {
         public Identifier getModelResource(StaticAnimatable animatable) {
            return new Identifier("smfs", "geo/ghost_skeleton.geo.json");
         }

         public Identifier getTextureResource(StaticAnimatable animatable) {
            return new Identifier("smfs", "textures/block/ghost_skeleton.png");
         }

         public Identifier getAnimationResource(StaticAnimatable animatable) {
            return null;
         }
      });
   }
}
