package com.xie.smfs.client.renderer;

import com.xie.smfs.block.entity.SpiritBrewingStandBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SpiritBrewingStandBlockRenderer extends GeoBlockRenderer<SpiritBrewingStandBlockEntity> {
   public SpiritBrewingStandBlockRenderer(Context ctx) {
      super(new GeoModel<SpiritBrewingStandBlockEntity>() {
         public Identifier getModelResource(SpiritBrewingStandBlockEntity animatable) {
            return new Identifier("smfs", "geo/spirit_brewing_stand.geo.json");
         }

         public Identifier getTextureResource(SpiritBrewingStandBlockEntity animatable) {
            return new Identifier("smfs", "textures/block/spirit_brewing_stand.png");
         }

         public Identifier getAnimationResource(SpiritBrewingStandBlockEntity animatable) {
            return new Identifier("smfs", "animations/spirit_brewing_stand.animation.json");
         }
      });
   }
}
