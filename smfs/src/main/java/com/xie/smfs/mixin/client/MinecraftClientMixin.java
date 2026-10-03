package com.xie.smfs.mixin.client;

import com.xie.smfs.item.FissuredSpearPurpleItem;
import com.xie.smfs.item.RustyOldBroadswordItem;
import com.xie.smfs.network.packets.common.c2s.SpearRemoteAttackC2SPacket;
import com.xie.smfs.util.TargetingUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
   @Inject(method = "doAttack", at = @At("HEAD"))
   private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
      MinecraftClient client = (MinecraftClient)this;
      if (client.player != null && client.world != null) {
         ItemStack stack = client.player.getMainHandStack();
         if (stack.getItem() instanceof FissuredSpearPurpleItem) {
            String mode = FissuredSpearPurpleItem.getThrowMode(stack);
            if (!mode.equals("wish")) {
               return;
            }

            String preset = FissuredSpearPurpleItem.getWishPreset(stack);
            if (!preset.equals("remote_attack")) {
               return;
            }

            this.performRemoteAttack(client);
         } else if (stack.getItem() instanceof RustyOldBroadswordItem) {
            if (!RustyOldBroadswordItem.isRangedMode(stack)) {
               return;
            }

            this.performRemoteAttack(client);
         }
      }
   }

   private void performRemoteAttack(MinecraftClient client) {
      double reachDistance = 64.0;
      float tickDelta = 1.0F;
      Vec3d eyePos = client.player.getCameraPosVec(tickDelta);
      Vec3d lookVec = client.player.getRotationVec(tickDelta);
      EntityHitResult entityHit = TargetingUtil.projectileRaycast(client.player, eyePos, lookVec, reachDistance, 1.0);
      if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target && target != client.player && target.isAlive()) {
         double normalReach = 4.0;
         double dist = client.player.squaredDistanceTo(target);
         if (dist >= normalReach * normalReach) {
            SpearRemoteAttackC2SPacket.sendToServer(target.getId());
         }
      } else {
         client.player.sendMessage(Text.literal("§c没有目标"), true);
      }
   }
}
