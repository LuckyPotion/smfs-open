package com.xie.smfs.block.entity;

import com.xie.smfs.client.model.block.FootprintBlockModel;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class FootprintBlockEntityRenderer extends GeoBlockRenderer<FootprintBlockEntity> {
   public FootprintBlockEntityRenderer(Context context) {
      super(new FootprintBlockModel());
   }
}
