package com.xie.smfs.client.util;

public class ClientGhostUtils {
   private static boolean isGiantShadowGhostScaling = false;
   private static float scale = 1.0F;
   private static float transparency = 1.0F;

   public static void setGiantShadowGhostScaling(boolean scaling) {
      isGiantShadowGhostScaling = scaling;
      if (!scaling) {
         scale = 1.0F;
         transparency = 1.0F;
      }
   }

   public static boolean isGiantShadowGhostScaling() {
      return isGiantShadowGhostScaling;
   }

   public static float getScale() {
      return scale;
   }

   public static void setScale(float scale) {
      ClientGhostUtils.scale = scale;
   }

   public static float getTransparency() {
      return transparency;
   }

   public static void setTransparency(float transparency) {
      ClientGhostUtils.transparency = transparency;
   }
}
