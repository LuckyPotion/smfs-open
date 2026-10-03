package com.xie.smfs.client.renderer;

import com.xie.smfs.entity.other.GraveWarningEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;

public class GraveWarningRenderer extends EntityRenderer<GraveWarningEntity> {
   public GraveWarningRenderer(Context context) {
      super(context);
      this.field_4673 = 0.0F;
   }

   public Identifier getTexture(GraveWarningEntity entity) {
      return null;
   }
}
