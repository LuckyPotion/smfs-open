package com.xie.smfs.api;

import com.xie.smfs.api.impl.GhostRendererAPIImpl;
import com.xie.smfs.entity.GhostEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public interface GhostRendererAPI {
   boolean registerWithRendererType(EntityType<? extends GhostEntity> entityType, GhostRendererAPI.RendererType rendererType);

   boolean registerWithCustomResources(EntityType<? extends GhostEntity> entityType, GhostRendererAPI.ResourceProvider resourceProvider);

   boolean registerWithSimpleResources(EntityType<? extends GhostEntity> entityType, String string, String string2, String string3);

   static GhostRendererAPI getInstance() {
      return GhostRendererAPIImpl.INSTANCE;
   }

   static boolean isRegistered() {
      return GhostRendererAPIImpl.INSTANCE != null;
   }

   static void register(GhostRendererAPI instance) {
      GhostRendererAPIImpl.INSTANCE = instance;
   }

   enum RendererType {
      UNIVERSAL,
      HUMAN,
      CUSTOM;
   }

   interface ResourceProvider {
      Identifier getModelResource(GhostEntity ghostEntity);

      Identifier getTextureResource(GhostEntity ghostEntity);

      Identifier getAnimationResource(GhostEntity ghostEntity);

      default float getScale(GhostEntity entity) {
         return 1.0F;
      }

      default boolean isTranslucent(GhostEntity entity) {
         return true;
      }
   }
}
