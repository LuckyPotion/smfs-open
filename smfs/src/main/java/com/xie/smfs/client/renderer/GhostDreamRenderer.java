package com.xie.smfs.client.renderer;

import com.xie.smfs.entity.ghost.GhostDreamEntity;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GhostDreamRenderer extends GeoEntityRenderer<GhostDreamEntity> {
   public GhostDreamRenderer(Context ctx) {
      super(ctx, new GeoModel<GhostDreamEntity>() {
         public Identifier getModelResource(GhostDreamEntity animatable) {
            return new Identifier("smfs", "geo/ghost_dream.geo.json");
         }

         public Identifier getTextureResource(GhostDreamEntity animatable) {
            return new Identifier("smfs", "textures/entity/ghost_dream.png");
         }

         public Identifier getAnimationResource(GhostDreamEntity animatable) {
            return new Identifier("smfs", "animations/ghost_dream.animation.json");
         }
      });
   }
}
