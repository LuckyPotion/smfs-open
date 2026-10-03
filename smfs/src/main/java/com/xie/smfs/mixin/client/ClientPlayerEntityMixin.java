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
         if (boundEntity != null && !boundEntity.isRemoved()) {
            float forward = self.input.movementForward;
            float sideways = self.input.movementSideways;
            boolean jumping = self.input.jumping;
            boolean sneaking = self.input.sneaking;
            ClientGhostShadowHeadNSkillC2SPacket.sendToServer(boundEntity.getId(), forward, sideways, jumping, sneaking, self.getYaw(), self.getPitch());
            self.input.movementForward = 0.0F;
            self.input.movementSideways = 0.0F;
            self.input.jumping = false;
            self.input.sneaking = false;
            self.setSprinting(false);
            self.setVelocity(0.0, self.getVelocity().y, 0.0);
            self.setPosition(
               GhostShadowHeadCameraManager.getSavedPlayerX(), GhostShadowHeadCameraManager.getSavedPlayerY(), GhostShadowHeadCameraManager.getSavedPlayerZ()
            );
         }
      }
   }
}
