package com.xie.smfs.client;

import com.xie.smfs.client.preset.FilterRenderer;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.MainGhostManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class GiantShadowGhostSneakRenderer {
   private static boolean isActive = false;
   private static long effectStartTime = 0L;
   private static final long FADE_DURATION = 500L;

   public static void register() {
      HudRenderCallback.EVENT.register(GiantShadowGhostSneakRenderer::render);
   }

   private static void render(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      PlayerEntity player = client.player;
      if (player == null) {
         isActive = false;
      } else {
         boolean shouldActivate = player.isSneaking() && hasGiantShadowGhost(player);
         if (!shouldActivate) {
            isActive = false;
         } else {
            if (!isActive) {
               isActive = true;
               effectStartTime = System.currentTimeMillis();
            }

            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();
            float fadeProgress = calculateFadeProgress();
            FilterRenderer.renderEdgeGradient(context, screenWidth, screenHeight, FilterRenderer.FilterPreset.BLACK_CURSE, fadeProgress);
         }
      }
   }

   private static float calculateFadeProgress() {
      long currentTime = System.currentTimeMillis();
      long elapsedTime = currentTime - effectStartTime;
      if (elapsedTime < 500L) {
         float progress = (float)elapsedTime / 500.0F;
         return (float)Math.sin(progress * Math.PI / 2.0);
      } else {
         return 1.0F;
      }
   }

   private static boolean hasGiantShadowGhost(PlayerEntity player) {
      String ghostType = PlayerEvents.getGhostTypeInSlot(player, MainGhostManager.getMainGhostSlot(player));
      return "giant_shadow_ghost".equals(ghostType) || "complete_shadow_ghost".equals(ghostType);
   }
}
