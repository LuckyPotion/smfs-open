package com.xie.smfs.client.renderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class HumanRenderer extends BipedEntityRenderer<MobEntity, PlayerEntityModel<MobEntity>> {
   public HumanRenderer(Context context) {
      super(context, new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), false), 0.5F);
   }

   public Identifier getTexture(MobEntity entity) {
      String textureName = this.getTextureNameByEntity(entity);
      return new Identifier("smfs", "textures/entity/" + textureName + ".png");
   }

   private String getTextureNameByEntity(MobEntity entity) {
      String className = entity.getClass().getSimpleName();

      return switch (className) {
         case "WangXiaoMingEntity" -> "wang_xiao_ming";
         case "ChenDoctorEntity" -> "chen_doctor";
         case "CaoYanHuaEntity" -> "cao_yan_hua";
         case "LiuXiaoYuEntity" -> "liu_xiao_yu";
         case "YangJianEntity" -> "yang_jian";
         case "LiJunEntity" -> "li_jun";
         case "YeZhenEntity" -> "ye_zhen";
         case "FengQuanEntity" -> "feng_quan";
         case "CaoYangEntity" -> "cao_yang";
         case "FangShiMinEntity" -> "fang_shi_min";
         case "LiLePingEntity" -> "li_le_ping";
         case "YanLiEntity" -> "yan_li";
         case "ZhaoKaiMingEntity" -> "zhao_kai_ming";
         case "YangXiaoEntity" -> "yang_xiao";
         case "NPC1Entity" -> "npc1";
         case "NPC2Entity" -> "npc2";
         case "NPC3Entity" -> "npc3";
         case "NPC4Entity" -> "npc4";
         case "NPC5Entity" -> "npc5";
         case "NPC6Entity" -> "npc6";
         case "YinQiEntity" -> "yin_qi";
         case "XianWangEntity" -> "xian_wang";
         case "GuiNiaoEntity" -> "gui_niao";
         default -> "ghost";
      };
   }

   public void render(MobEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      matrices.push();
      matrices.scale(1.0F, 1.0F, 1.0F);
      Identifier texture = this.getTexture(entity);
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture));
      super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
      matrices.pop();
   }
}
