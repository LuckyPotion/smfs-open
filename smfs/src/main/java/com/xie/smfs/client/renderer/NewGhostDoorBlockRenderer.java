package com.xie.smfs.client.renderer;

import com.xie.smfs.block.entity.NewGhostDoorBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class NewGhostDoorBlockRenderer extends GeoBlockRenderer<NewGhostDoorBlockEntity> {
   public NewGhostDoorBlockRenderer(Context ctx) {
      super(new GeoModel<NewGhostDoorBlockEntity>() {
         public Identifier getModelResource(NewGhostDoorBlockEntity animatable) {
            return new Identifier("smfs", "geo/new_ghost_door.geo.json");
         }

         public Identifier getTextureResource(NewGhostDoorBlockEntity animatable) {
            return new Identifier("smfs", "textures/block/new_ghost_door.png");
         }

         public Identifier getAnimationResource(NewGhostDoorBlockEntity animatable) {
            return new Identifier("smfs", "animations/new_ghost_door.animation.json");
         }
      });
   }
}
