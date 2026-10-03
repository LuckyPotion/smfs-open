package com.xie.smfs.client.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FusionCameraManager {
   private static boolean zooming = false;
   private static long zoomStartTime = 0L;
   private static final float ZOOM_DURATION = 12.0F;
   private static final Vec3d START_OFFSET = new Vec3d(0.0, 2.0, 3.0);
   private static final Vec3d END_OFFSET = new Vec3d(0.0, 14.0, 19.0);
   private static float cameraYaw;
   private static float cameraPitch;

   public static void startZoom() {
      zooming = true;
      zoomStartTime = System.currentTimeMillis();
   }

   public static void stopZoom() {
      zooming = false;
   }

   public static boolean isZooming() {
      return zooming;
   }

   public static float getCameraYaw() {
      return cameraYaw;
   }

   public static float getCameraPitch() {
      return cameraPitch;
   }

   private static float getProgress() {
      if (!zooming) {
         return 0.0F;
      }

      float elapsed = (float)(System.currentTimeMillis() - zoomStartTime) / 1000.0F;
      float progress = MathHelper.clamp(elapsed / 12.0F, 0.0F, 1.0F);
      if (progress < 0.5F) {
         return 2.0F * progress * progress;
      }

      float t = (progress - 0.5F) * 2.0F;
      return 0.5F + (float)Math.sin(t * Math.PI / 2.0) * 0.5F;
   }

   public static Vec3d calculateCameraPos(Entity focusedEntity) {
      if (focusedEntity == null) {
         return null;
      }

      float progress = getProgress();
      Vec3d offset = START_OFFSET.lerp(END_OFFSET, progress);
      Vec3d cameraPos = focusedEntity.getEyePos().add(offset);
      double dx = -offset.x;
      double dy = -offset.y;
      double dz = -offset.z;
      double horizontalDist = Math.sqrt(dx * dx + dz * dz);
      cameraYaw = (float)Math.toDegrees(Math.atan2(dx, dz));
      cameraPitch = (float)Math.toDegrees(Math.atan2(-dy, horizontalDist));
      return cameraPos;
   }
}
