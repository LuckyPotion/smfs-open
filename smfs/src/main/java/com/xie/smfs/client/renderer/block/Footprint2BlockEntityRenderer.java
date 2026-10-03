package com.xie.smfs.client.renderer.block;

import com.xie.smfs.block.entity.Footprint2BlockEntity;
import com.xie.smfs.client.model.block.Footprint2BlockModel;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class Footprint2BlockEntityRenderer extends GeoBlockRenderer<Footprint2BlockEntity> {
   public Footprint2BlockEntityRenderer(Context context) {
      super(new Footprint2BlockModel());
   }
}
