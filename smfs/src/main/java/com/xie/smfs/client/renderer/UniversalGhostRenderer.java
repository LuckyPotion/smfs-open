package com.xie.smfs.client.renderer;

import com.xie.smfs.api.CustomGhostModelProvider;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.CryingGhostEntity;
import com.xie.smfs.entity.ghost.DeathSightGhostEntity;
import com.xie.smfs.entity.ghost.GanshiBrideGhostEntity;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.entity.ghost.GhostMerchantEntity;
import com.xie.smfs.entity.ghost.GhostOfficerEntity;
import com.xie.smfs.entity.ghost.GhostPressureEntity;
import com.xie.smfs.entity.ghost.GhostShadowHeadEntity;
import com.xie.smfs.entity.ghost.GiantMaleCorpseGhostEntity;
import com.xie.smfs.entity.ghost.GiantShadowGhostEntity;
import com.xie.smfs.entity.ghost.GongGhostEntity;
import com.xie.smfs.entity.ghost.GoodsSellerGhostEntity;
import com.xie.smfs.entity.ghost.LostGhostEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.entity.ghost.QiaomenGhostEntity;
import com.xie.smfs.entity.ghost.SilentGhostEntity;
import com.xie.smfs.entity.ghost.SuonaGhostEntity;
import com.xie.smfs.entity.ghost.TaitouGhostEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class UniversalGhostRenderer extends GeoEntityRenderer<GhostEntity> {
   public UniversalGhostRenderer(Context ctx) {
      super(
         ctx,
         new GeoModel<GhostEntity>() {
            public Identifier getModelResource(GhostEntity animatable) {
               if (animatable instanceof CustomGhostModelProvider provider) {
                  return provider.getModelPath();
               } else if (animatable instanceof TaitouGhostEntity) {
                  return new Identifier("smfs", "geo/taitou_ghost.geo.json");
               } else if (animatable instanceof GanshiBrideGhostEntity) {
                  return new Identifier("smfs", "geo/ganshibride_ghost.geo.json");
               } else if (animatable instanceof GhostMerchantEntity) {
                  return new Identifier("smfs", "geo/ghost_merchant.geo.json");
               } else if (animatable instanceof GongGhostEntity) {
                  return new Identifier("smfs", "geo/gong_ghost.geo.json");
               } else if (animatable instanceof CryingGhostEntity) {
                  return new Identifier("smfs", "geo/ganshibride_ghost.geo.json");
               } else if (animatable instanceof SuonaGhostEntity) {
                  return new Identifier("smfs", "geo/suona_ghost.geo.json");
               } else if (animatable instanceof GhostPressureEntity) {
                  return new Identifier("smfs", "geo/ghost_pressure.geo.json");
               } else if (animatable instanceof GoodsSellerGhostEntity) {
                  return new Identifier("smfs", "geo/goods_seller_ghost.geo.json");
               } else if (animatable instanceof QiaomenGhostEntity) {
                  return new Identifier("smfs", "geo/qiaomen_ghost_entity.geo.json");
               } else if (animatable instanceof SilentGhostEntity) {
                  return new Identifier("smfs", "geo/silent_ghost.geo.json");
               } else if (animatable instanceof GhostChildEntity) {
                  return new Identifier("smfs", "geo/child_ghost.geo.json");
               } else if (animatable instanceof GiantShadowGhostEntity) {
                  return new Identifier("smfs", "geo/giant_shadow_ghost.geo.json");
               } else if (animatable instanceof GiantMaleCorpseGhostEntity) {
                  return new Identifier("smfs", "geo/giant_male_corpse_ghost.geo.json");
               } else if (animatable instanceof GhostOfficerEntity) {
                  return new Identifier("smfs", "geo/ghost_officer.geo.json");
               } else if (animatable instanceof GhostShadowHeadEntity) {
                  return new Identifier("smfs", "geo/ghost_shadow_head.json");
               } else {
                  return animatable instanceof LuoQianGhostEntity
                     ? new Identifier("smfs", "geo/luo_qian_ghost.geo.json")
                     : new Identifier("smfs", "geo/ghost.geo.json");
               }
            }

            public Identifier getTextureResource(GhostEntity animatable) {
               if (animatable instanceof CustomGhostModelProvider provider) {
                  return provider.getTexturePath();
               } else if (animatable instanceof TaitouGhostEntity) {
                  return new Identifier("smfs", "textures/entity/taitou_ghost.png");
               } else if (animatable instanceof LostGhostEntity) {
                  return new Identifier("smfs", "textures/entity/lost_ghost.png");
               } else if (animatable instanceof DeathSightGhostEntity) {
                  return new Identifier("smfs", "textures/entity/sight_ghost.png");
               } else if (animatable instanceof GanshiBrideGhostEntity) {
                  return new Identifier("smfs", "textures/entity/ganshibride_ghost.png");
               } else if (animatable instanceof GhostMerchantEntity) {
                  return new Identifier("smfs", "textures/entity/ghost_merchant.png");
               } else if (animatable instanceof GongGhostEntity) {
                  return new Identifier("smfs", "textures/entity/gong_ghost.png");
               } else if (animatable instanceof CryingGhostEntity) {
                  return new Identifier("smfs", "textures/entity/crying_ghost.png");
               } else if (animatable instanceof SuonaGhostEntity) {
                  return new Identifier("smfs", "textures/entity/suona_ghost.png");
               } else if (animatable instanceof GhostPressureEntity) {
                  return new Identifier("smfs", "textures/entity/ghost_pull.png");
               } else if (animatable instanceof GoodsSellerGhostEntity) {
                  return new Identifier("smfs", "textures/entity/goods_seller_ghost.png");
               } else if (animatable instanceof QiaomenGhostEntity) {
                  return new Identifier("smfs", "textures/entity/qiaomen_ghost_entity.png");
               } else if (animatable instanceof SilentGhostEntity) {
                  return new Identifier("smfs", "textures/entity/silent_ghost.png");
               } else if (animatable instanceof GhostChildEntity) {
                  return new Identifier("smfs", "textures/entity/child_ghost.png");
               } else if (animatable instanceof GiantShadowGhostEntity) {
                  return new Identifier("smfs", "textures/entity/giant_shadow_ghost.png");
               } else if (animatable instanceof GiantMaleCorpseGhostEntity) {
                  return new Identifier("smfs", "textures/entity/giant_male_corpse_ghost.png");
               } else if (animatable instanceof GhostOfficerEntity) {
                  return new Identifier("smfs", "textures/entity/ghost_officer.png");
               } else if (animatable instanceof GhostShadowHeadEntity) {
                  return new Identifier("smfs", "textures/entity/ghost_shadow_head.png");
               } else {
                  return animatable instanceof LuoQianGhostEntity
                     ? new Identifier("smfs", "textures/entity/luo_qian_ghost.png")
                     : new Identifier("smfs", "textures/entity/ghost.png");
               }
            }

            public Identifier getAnimationResource(GhostEntity animatable) {
               if (animatable instanceof CustomGhostModelProvider provider) {
                  return provider.getAnimationPath();
               } else if (animatable instanceof TaitouGhostEntity) {
                  return new Identifier("smfs", "animations/taitou_ghost.animation.json");
               } else if (animatable instanceof GongGhostEntity) {
                  return new Identifier("smfs", "animations/gong_ghost.animation.json");
               } else if (animatable instanceof CryingGhostEntity) {
                  return new Identifier("smfs", "animations/crying_ghost.animation.json");
               } else if (animatable instanceof GanshiBrideGhostEntity) {
                  return new Identifier("smfs", "animations/ganshibride_ghost.animation.json");
               } else if (animatable instanceof SuonaGhostEntity) {
                  return new Identifier("smfs", "animations/suona_ghost.animation.json");
               } else if (animatable instanceof GhostPressureEntity) {
                  return new Identifier("smfs", "animations/ghost_pressure.animation.json");
               } else if (animatable instanceof GoodsSellerGhostEntity) {
                  return new Identifier("smfs", "animations/goods_seller_ghost.animation.json");
               } else if (animatable instanceof QiaomenGhostEntity) {
                  return new Identifier("smfs", "animations/qiaomen_ghost_entity.animation.json");
               } else if (animatable instanceof GhostChildEntity) {
                  return new Identifier("smfs", "animations/child_ghost.animation.json");
               } else if (animatable instanceof GiantShadowGhostEntity) {
                  return new Identifier("smfs", "animations/giant_shadow_ghost.animation.json");
               } else if (animatable instanceof GiantMaleCorpseGhostEntity) {
                  return new Identifier("smfs", "animations/giant_male_corpse_ghost.animation.json");
               } else if (animatable instanceof GhostOfficerEntity) {
                  return new Identifier("smfs", "animations/ghost_officer.animation.json");
               } else if (animatable instanceof GhostShadowHeadEntity) {
                  return new Identifier("smfs", "animations/ghost_shadow_head.animation.json");
               } else {
                  return animatable instanceof LuoQianGhostEntity
                     ? new Identifier("smfs", "animations/luo_qian_ghost.animation.json")
                     : new Identifier("smfs", "animations/ghost_entity.animation.json");
               }
            }
         }
      );
   }

   public void render(GhostEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
      if (entity instanceof CustomGhostModelProvider provider) {
         float scale = provider.getScale();
         if (scale != 1.0F) {
            poseStack.push();
            poseStack.scale(scale, scale, scale);
         }
      }

      if (entity instanceof GhostPressureEntity) {
         poseStack.push();
         poseStack.scale(0.45F, 0.45F, 0.45F);
         super.method_3936(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
         poseStack.pop();
      } else if (entity instanceof LuoQianGhostEntity) {
         poseStack.push();
         poseStack.scale(1.5F, 1.5F, 1.5F);
         super.method_3936(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
         poseStack.pop();
      } else if (entity instanceof GhostShadowHeadEntity) {
         poseStack.push();
         poseStack.scale(2.0F, 2.0F, 2.0F);
         super.method_3936(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
         poseStack.pop();
      } else {
         super.method_3936(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      }

      if (entity instanceof CustomGhostModelProvider provider) {
         float scale = provider.getScale();
         if (scale != 1.0F) {
            poseStack.pop();
         }
      }
   }
}
