package com.xie.smfs.mixin.client;

import com.xie.smfs.client.util.GhostShadowHeadCameraManager;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostShadowHeadNSkillC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
   @Inject(method = "tick", at = @At("HEAD"))
   private void smfs$redirectInputToTarget(CallbackInfo ci) {
      ClientPlayerEntity self = (ClientPlayerEntity)this;
      if (GhostShadowHeadCameraManager.isNControlActive()) {
         Entity boundEntity = GhostShadowHeadCameraManager.getBoundEntity();
         if (boundEntity != null && !boundEntity.method_31481()) {
            float forward = self.field_3913.field_3905;
            float sideways = self.field_3913.field_3907;
            boolean jumping = self.field_3913.field_3904;
            boolean sneaking = self.field_3913.field_3903;
            ClientGhostShadowHeadNSkillC2SPacket.sendToServer(
               boundEntity.method_5628(), forward, sideways, jumping, sneaking, self.method_36454(), self.method_36455()
            );
            self.field_3913.field_3905 = 0.0F;
            self.field_3913.field_3907 = 0.0F;
            self.field_3913.field_3904 = false;
            self.field_3913.field_3903 = false;
            self.method_5728(false);
            self.method_18800(0.0, self.method_18798().field_1351, 0.0);
            self.method_5814(
               GhostShadowHeadCameraManager.getSavedPlayerX(), GhostShadowHeadCameraManager.getSavedPlayerY(), GhostShadowHeadCameraManager.getSavedPlayerZ()
            );
         }
      }
   }
}
