package com.xie.smfs.client;

import com.xie.smfs.client.preset.FilterRenderer;
import com.xie.smfs.registry.ModEffects;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

public class MusicBoxCurseRenderer {
   private static boolean isActive = false;
   private static long effectStartTime = 0L;
   private static final long FADE_DURATION = 2000L;

   public static void register() {
      HudRenderCallback.EVENT.register(MusicBoxCurseRenderer::render);
   }

   private static void render(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      PlayerEntity player = client.player;
      if (player == null) {
         isActive = false;
      } else {
         StatusEffectInstance effect = player.getStatusEffect(ModEffects.MUSIC_BOX_CURSE);
         if (effect == null) {
            isActive = false;
         } else {
            if (!isActive) {
               isActive = true;
               effectStartTime = System.currentTimeMillis();
            }

            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();
            float fadeProgress = calculateFadeProgress(effect);
            FilterRenderer.renderEdgeGradient(context, screenWidth, screenHeight, FilterRenderer.FilterPreset.RED_CURSE, fadeProgress);
         }
      }
   }

   private static float calculateFadeProgress(StatusEffectInstance effect) {
      long currentTime = System.currentTimeMillis();
      long elapsedTime = currentTime - effectStartTime;
      if (elapsedTime < 2000L) {
         float progress = (float)elapsedTime / 2000.0F;
         return (float)Math.sin(progress * Math.PI / 2.0);
      } else {
         int remainingDuration = effect.getDuration();
         long remainingMs = remainingDuration * 50L;
         if (remainingMs < 2000L) {
            float progress = (float)remainingMs / 2000.0F;
            return (float)Math.sin(progress * Math.PI / 2.0);
         } else {
            return 1.0F;
         }
      }
   }
}
