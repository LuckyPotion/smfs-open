package com.xie.smfs.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.CloudRenderer;

public class EmptyCloudRenderer implements CloudRenderer {
   public void render(WorldRenderContext context) {
   }
}
