package com.xie.smfs.mixin.client;

import com.xie.smfs.client.util.ClientGhostUtils;
import com.xie.smfs.item.GhostShroudArmorItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
   @Unique
   private boolean smfs$ghostShroudLayersHidden;
   @Unique
   private boolean smfs$rightSleeveVisible;
   @Unique
   private boolean smfs$leftSleeveVisible;
   @Unique
   private boolean smfs$jacketVisible;

   @Inject(
      method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD")
   )
   private void smfs$preRender(
      AbstractClientPlayerEntity player, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci
   ) {
      if (ClientGhostUtils.isGiantShadowGhostScaling()) {
         matrixStack.method_22903();
         float scale = ClientGhostUtils.getScale();
         scale *= 0.95F;
         if (scale < 0.01F) {
            scale = 0.01F;
         }

         ClientGhostUtils.setScale(scale);
         matrixStack.method_22905(scale, scale, scale);
         float transparency = ClientGhostUtils.getTransparency();
         transparency *= 0.95F;
         if (transparency < 0.01F) {
            transparency = 0.01F;
         }

         ClientGhostUtils.setTransparency(transparency);
      }

      this.smfs$hideGhostShroudPlayerLayers(player);
   }

   @Inject(
      method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("TAIL")
   )
   private void smfs$postRender(
      AbstractClientPlayerEntity player, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci
   ) {
      this.smfs$restoreGhostShroudPlayerLayers();
      if (ClientGhostUtils.isGiantShadowGhostScaling()) {
         matrixStack.method_22909();
      }
   }

   @Unique
   private void smfs$hideGhostShroudPlayerLayers(AbstractClientPlayerEntity player) {
      if (!(player.method_6118(EquipmentSlot.field_6174).method_7909() instanceof GhostShroudArmorItem)) {
         this.smfs$ghostShroudLayersHidden = false;
      } else {
         PlayerEntityModel<AbstractClientPlayerEntity> model = (PlayerEntityModel<AbstractClientPlayerEntity>)((PlayerEntityRenderer)this).method_4038();
         this.smfs$ghostShroudLayersHidden = true;
         this.smfs$rightSleeveVisible = model.field_3486.field_3665;
         this.smfs$leftSleeveVisible = model.field_3484.field_3665;
         this.smfs$jacketVisible = model.field_3483.field_3665;
         model.field_3486.field_3665 = false;
         model.field_3484.field_3665 = false;
         model.field_3483.field_3665 = false;
      }
   }

   @Unique
   private void smfs$restoreGhostShroudPlayerLayers() {
      if (this.smfs$ghostShroudLayersHidden) {
         PlayerEntityModel<AbstractClientPlayerEntity> model = (PlayerEntityModel<AbstractClientPlayerEntity>)((PlayerEntityRenderer)this).method_4038();
         model.field_3486.field_3665 = this.smfs$rightSleeveVisible;
         model.field_3484.field_3665 = this.smfs$leftSleeveVisible;
         model.field_3483.field_3665 = this.smfs$jacketVisible;
         this.smfs$ghostShroudLayersHidden = false;
      }
   }
}
