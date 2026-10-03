package com.xie.smfs.api.impl;

import com.xie.smfs.api.GhostRendererAPI;
import com.xie.smfs.client.renderer.HumanRenderer;
import com.xie.smfs.client.renderer.UniversalGhostRenderer;
import com.xie.smfs.entity.GhostEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GhostRendererAPIImpl implements GhostRendererAPI {
   public static GhostRendererAPI INSTANCE = new GhostRendererAPIImpl();

   @Override
   public boolean registerWithRendererType(EntityType<? extends GhostEntity> entityType, GhostRendererAPI.RendererType rendererType) {
      if (entityType != null && rendererType != null) {
         try {
            switch (rendererType) {
               case UNIVERSAL:
                  EntityRendererRegistry.register(entityType, UniversalGhostRenderer::new);
                  break;
               case HUMAN:
                  EntityRendererRegistry.register(entityType, HumanRenderer::new);
                  break;
               case CUSTOM:
                  EntityRendererRegistry.register(entityType, UniversalGhostRenderer::new);
                  break;
               default:
                  return false;
            }

            return true;
         } catch (Exception e) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean registerWithCustomResources(EntityType<? extends GhostEntity> entityType, GhostRendererAPI.ResourceProvider resourceProvider) {
      if (entityType != null && resourceProvider != null) {
         try {
            GeoModel<GhostEntity> customModel = new GeoModel<GhostEntity>() {
               public Identifier getModelResource(GhostEntity animatable) {
                  return resourceProvider.getModelResource(animatable);
               }

               public Identifier getTextureResource(GhostEntity animatable) {
                  return resourceProvider.getTextureResource(animatable);
               }

               public Identifier getAnimationResource(GhostEntity animatable) {
                  return resourceProvider.getAnimationResource(animatable);
               }
            };
            EntityRendererRegistry.register(entityType, ctx -> new GhostRendererAPIImpl.CustomGhostRenderer(ctx, customModel, resourceProvider));
            return true;
         } catch (Exception e) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean registerWithSimpleResources(EntityType<? extends GhostEntity> entityType, String modelPath, String texturePath, String animationPath) {
      if (entityType != null && modelPath != null && texturePath != null) {
         try {
            GhostRendererAPI.ResourceProvider simpleProvider = new GhostRendererAPI.ResourceProvider() {
               @Override
               public Identifier getModelResource(GhostEntity entity) {
                  return new Identifier("smfs", modelPath);
               }

               @Override
               public Identifier getTextureResource(GhostEntity entity) {
                  return new Identifier("smfs", texturePath);
               }

               @Override
               public Identifier getAnimationResource(GhostEntity entity) {
                  return animationPath != null ? new Identifier("smfs", animationPath) : null;
               }
            };
            return this.registerWithCustomResources(entityType, simpleProvider);
         } catch (Exception e) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static class CustomGhostRenderer extends GeoEntityRenderer<GhostEntity> {
      private final GhostRendererAPI.ResourceProvider resourceProvider;

      public CustomGhostRenderer(Context ctx, GeoModel<GhostEntity> model, GhostRendererAPI.ResourceProvider resourceProvider) {
         super(ctx, model);
         this.resourceProvider = resourceProvider;
      }

      public void render(GhostEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
         float scale = this.resourceProvider.getScale(entity);
         if (scale != 1.0F) {
            poseStack.push();
            poseStack.scale(scale, scale, scale);
            super.method_3936(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            poseStack.pop();
         } else {
            super.method_3936(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
         }
      }

      public RenderLayer getRenderType(GhostEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick) {
         return this.resourceProvider.isTranslucent(animatable)
            ? RenderLayer.getEntityTranslucent(texture)
            : super.getRenderType(animatable, texture, bufferSource, partialTick);
      }
   }
}
