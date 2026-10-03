package com.xie.smfs.client.renderer;

import com.xie.smfs.block.entity.GhostFurnaceBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GhostFurnaceBlockRenderer extends GeoBlockRenderer<GhostFurnaceBlockEntity> {
   public GhostFurnaceBlockRenderer(Context ctx) {
      super(new GeoModel<GhostFurnaceBlockEntity>() {
         public Identifier getModelResource(GhostFurnaceBlockEntity animatable) {
            return new Identifier("smfs", "geo/ghost_furnace.geo.json");
         }

         public Identifier getTextureResource(GhostFurnaceBlockEntity animatable) {
            return new Identifier("smfs", "textures/block/ghost_furnace.png");
         }

         public Identifier getAnimationResource(GhostFurnaceBlockEntity animatable) {
            return null;
         }
      });
   }
}
