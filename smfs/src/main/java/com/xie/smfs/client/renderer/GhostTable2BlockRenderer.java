package com.xie.smfs.client.renderer;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GhostTable2BlockRenderer extends GeoBlockRenderer<StaticAnimatable> {
   public GhostTable2BlockRenderer(Context ctx) {
      super(new GeoModel<StaticAnimatable>() {
         public Identifier getModelResource(StaticAnimatable animatable) {
            return new Identifier("smfs", "geo/ghost_table2.geo.json");
         }

         public Identifier getTextureResource(StaticAnimatable animatable) {
            return new Identifier("smfs", "textures/block/ghost_table2.png");
         }

         public Identifier getAnimationResource(StaticAnimatable animatable) {
            return null;
         }
      });
   }
}
