package com.xie.smfs.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xie.smfs.Smfs;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.manager.GhostDreamManager;
import com.xie.smfs.registry.ModEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterSetup;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Start;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectRenderHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/EffectRenderHandler");
   private static float[] originalColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
   private static float originalFogStart = 0.0F;
   private static float originalFogEnd = 1.0F;
   private static float[] originalFogColor = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
   private static boolean isRedEffectActive = false;
   private static boolean isFogEffectActive = false;
   private static boolean isGreenEffectActive = false;
   private static boolean isBlueEffectActive = false;
   private static boolean isGrayEffectActive = false;
   private static boolean isGoldenEffectActive = false;
   private static boolean isPurpleEffectActive = false;
   private static boolean isBlackEffectActive = false;
   private static boolean isCyanEffectActive = false;
   private static boolean isRestoring = false;
   private static float screenShakeIntensity = 0.0F;
   private static int screenShakeDuration = 0;
   private static float currentOffsetProgress = 0.0F;
   private static long lastTickTime = 0L;
   private static final int MAX_EXECUTIONS_PER_TICK = 1;
   private static int currentTickExecutions = 0;

   public static boolean hasAnyGhostDomainEffect(PlayerEntity player) {
      if (player == null) {
         return false;
      }

      StatusEffect[] ghostDomainEffects = new StatusEffect[]{
         ModEffects.RED_GHOST_DOMAIN,
         ModEffects.RED_GHOST_DOMAIN_TARGET,
         ModEffects.GREEN_GHOST_DOMAIN,
         ModEffects.GREEN_GHOST_DOMAIN_TARGET,
         ModEffects.BLUE_GHOST_DOMAIN,
         ModEffects.BLUE_GHOST_DOMAIN_TARGET,
         ModEffects.GRAY_GHOST_DOMAIN,
         ModEffects.GRAY_GHOST_DOMAIN_TARGET,
         ModEffects.GOLDEN_GHOST_DOMAIN,
         ModEffects.GOLDEN_GHOST_DOMAIN_TARGET,
         ModEffects.PURPLE_GHOST_DOMAIN,
         ModEffects.PURPLE_GHOST_DOMAIN_TARGET,
         ModEffects.PURPLE_GHOST_DOMAIN_VISUAL,
         ModEffects.BLACK_GHOST_DOMAIN,
         ModEffects.BLACK_GHOST_DOMAIN_TARGET,
         ModEffects.CYAN_GHOST_DOMAIN,
         ModEffects.CYAN_GHOST_DOMAIN_TARGET,
         ModEffects.THICK_FOG,
         ModEffects.THICK_FOG_TARGET
      };

      for (StatusEffect effect : ghostDomainEffects) {
         if (player.hasStatusEffect(effect)) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasSpecificGhostDomainEffect(PlayerEntity player, StatusEffect... effects) {
      if (player == null) {
         return false;
      }

      for (StatusEffect effect : effects) {
         if (player.hasStatusEffect(effect)) {
            return true;
         }
      }

      return false;
   }

   public static void register() {
      WorldRenderEvents.START
         .register(
            (Start)context -> {
               PlayerEntity player = MinecraftClient.getInstance().player;
               if (player != null) {
                  boolean isInSpiritRealm = context.world() != null && context.world().getRegistryKey() == Smfs.SPIRIT_REALM_DIMENSION;
                  boolean hadRedEffect = isRedEffectActive;
                  boolean hadFogEffect = isFogEffectActive;
                  boolean hadGreenEffect = isGreenEffectActive;
                  boolean hadBlueEffect = isBlueEffectActive;
                  boolean hadGrayEffect = isGrayEffectActive;
                  boolean hadGoldenEffect = isGoldenEffectActive;
                  boolean hadPurpleEffect = isPurpleEffectActive;
                  boolean hadBlackEffect = isBlackEffectActive;
                  boolean hadCyanEffect = isCyanEffectActive;
                  isRedEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.RED_GHOST_DOMAIN, ModEffects.RED_GHOST_DOMAIN_TARGET);
                  isFogEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.THICK_FOG, ModEffects.THICK_FOG_TARGET);
                  isGreenEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.GREEN_GHOST_DOMAIN, ModEffects.GREEN_GHOST_DOMAIN_TARGET);
                  isBlueEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.BLUE_GHOST_DOMAIN, ModEffects.BLUE_GHOST_DOMAIN_TARGET);
                  isGrayEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.GRAY_GHOST_DOMAIN, ModEffects.GRAY_GHOST_DOMAIN_TARGET);
                  isGoldenEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.GOLDEN_GHOST_DOMAIN, ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
                  isPurpleEffectActive = hasSpecificGhostDomainEffect(
                     player, ModEffects.PURPLE_GHOST_DOMAIN, ModEffects.PURPLE_GHOST_DOMAIN_TARGET, ModEffects.PURPLE_GHOST_DOMAIN_VISUAL
                  );
                  isBlackEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.BLACK_GHOST_DOMAIN, ModEffects.BLACK_GHOST_DOMAIN_TARGET);
                  isCyanEffectActive = hasSpecificGhostDomainEffect(player, ModEffects.CYAN_GHOST_DOMAIN, ModEffects.CYAN_GHOST_DOMAIN_TARGET);
                  boolean isInGhostDream = context.world() != null && context.world().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION;
                  if ((
                        hadRedEffect && !isRedEffectActive
                           || hadFogEffect && !isFogEffectActive
                           || hadGreenEffect && !isGreenEffectActive
                           || hadBlueEffect && !isBlueEffectActive
                           || hadGrayEffect && !isGrayEffectActive
                           || hadGoldenEffect && !isGoldenEffectActive
                           || hadPurpleEffect && !isPurpleEffectActive
                           || hadBlackEffect && !isBlackEffectActive
                           || hadCyanEffect && !isCyanEffectActive
                     )
                     && !isInSpiritRealm
                     && !isInGhostDream) {
                     restoreOriginalSettings();
                  }

                  if ((
                        isRedEffectActive
                           || isFogEffectActive
                           || isGreenEffectActive
                           || isBlueEffectActive
                           || isGrayEffectActive
                           || isGoldenEffectActive
                           || isPurpleEffectActive
                           || isBlackEffectActive
                           || isCyanEffectActive
                           || isInSpiritRealm
                           || isInGhostDream
                     )
                     && !isRestoring) {
                     saveOriginalSettings();
                  }
               }
            }
         );
      WorldRenderEvents.AFTER_SETUP
         .register(
            (AfterSetup)context -> {
               if (!isRestoring) {
                  PlayerEntity player = MinecraftClient.getInstance().player;
                  if (player != null) {
                     boolean isInSpiritRealm = context.world() != null && context.world().getRegistryKey() == Smfs.SPIRIT_REALM_DIMENSION;
                     boolean hasAnyGhostDomainEffect = isRedEffectActive
                        || isFogEffectActive
                        || isGreenEffectActive
                        || isBlueEffectActive
                        || isGrayEffectActive
                        || isGoldenEffectActive
                        || isPurpleEffectActive
                        || isBlackEffectActive
                        || isCyanEffectActive;
                     boolean isInGhostDream = context.world() != null && context.world().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION;
                     if (!hasAnyGhostDomainEffect) {
                        if (isInSpiritRealm) {
                           RenderSystem.setShaderFogStart(0.5F);
                           RenderSystem.setShaderFogEnd(16.0F);
                           RenderSystem.setShaderFogColor(0.05F, 0.05F, 0.05F, 1.0F);
                        } else if (isInGhostDream) {
                           float[] fogColor = getGhostDreamFogColor();
                           RenderSystem.setShaderFogStart(0.5F);
                           RenderSystem.setShaderFogEnd(fogColor[3]);
                           RenderSystem.setShaderFogColor(fogColor[0], fogColor[1], fogColor[2], 1.0F);
                        }
                     } else if (isBlackEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.05F, 0.05F, 0.05F, 1.0F);
                     } else if (isRedEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.6F, 0.05F, 0.05F, 1.0F);
                     } else if (isGreenEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.05F, 0.6F, 0.05F, 1.0F);
                     } else if (isBlueEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.05F, 0.05F, 0.6F, 1.0F);
                     } else if (isGrayEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.6F, 0.6F, 0.6F, 1.0F);
                     } else if (isGoldenEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.85F, 0.7F, 0.05F, 1.0F);
                     } else if (isPurpleEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.5F, 0.05F, 0.5F, 1.0F);
                     } else if (isCyanEffectActive) {
                        RenderSystem.setShaderFogStart(0.5F);
                        boolean isTargetVersion = player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
                        RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
                        RenderSystem.setShaderFogColor(0.05F, 0.6F, 0.6F, 1.0F);
                     } else if (isFogEffectActive) {
                        applyFogEffect();
                     }

                     applyCameraShakeEffect(context);
                  }
               }
            }
         );
      WorldRenderEvents.LAST
         .register(
            (Last)context -> {
               PlayerEntity player = MinecraftClient.getInstance().player;
               if (player != null) {
                  boolean isInSpiritRealm = context.world() != null && context.world().getRegistryKey() == Smfs.SPIRIT_REALM_DIMENSION;
                  boolean isInGhostDream = context.world() != null && context.world().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION;
                  boolean currentRedEffect = hasSpecificGhostDomainEffect(player, ModEffects.RED_GHOST_DOMAIN, ModEffects.RED_GHOST_DOMAIN_TARGET);
                  boolean currentFogEffect = player.hasStatusEffect(ModEffects.THICK_FOG);
                  boolean currentGreenEffect = hasSpecificGhostDomainEffect(player, ModEffects.GREEN_GHOST_DOMAIN, ModEffects.GREEN_GHOST_DOMAIN_TARGET);
                  boolean currentBlueEffect = hasSpecificGhostDomainEffect(player, ModEffects.BLUE_GHOST_DOMAIN, ModEffects.BLUE_GHOST_DOMAIN_TARGET);
                  boolean currentGrayEffect = hasSpecificGhostDomainEffect(player, ModEffects.GRAY_GHOST_DOMAIN, ModEffects.GRAY_GHOST_DOMAIN_TARGET);
                  boolean currentGoldenEffect = hasSpecificGhostDomainEffect(player, ModEffects.GOLDEN_GHOST_DOMAIN, ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
                  boolean currentPurpleEffect = hasSpecificGhostDomainEffect(
                     player, ModEffects.PURPLE_GHOST_DOMAIN, ModEffects.PURPLE_GHOST_DOMAIN_TARGET, ModEffects.PURPLE_GHOST_DOMAIN_VISUAL
                  );
                  boolean currentBlackEffect = hasSpecificGhostDomainEffect(player, ModEffects.BLACK_GHOST_DOMAIN, ModEffects.BLACK_GHOST_DOMAIN_TARGET);
                  boolean currentCyanEffect = hasSpecificGhostDomainEffect(player, ModEffects.CYAN_GHOST_DOMAIN, ModEffects.CYAN_GHOST_DOMAIN_TARGET);
                  boolean hasAnyGhostDomainEffect = currentRedEffect
                     || currentFogEffect
                     || currentGreenEffect
                     || currentBlueEffect
                     || currentGrayEffect
                     || currentGoldenEffect
                     || currentPurpleEffect
                     || currentBlackEffect
                     || currentCyanEffect;
                  boolean needsRestore = !currentRedEffect && isRedEffectActive
                     || !currentFogEffect && isFogEffectActive
                     || !currentGreenEffect && isGreenEffectActive
                     || !currentBlueEffect && isBlueEffectActive
                     || !currentGrayEffect && isGrayEffectActive
                     || !currentGoldenEffect && isGoldenEffectActive
                     || !currentPurpleEffect && isPurpleEffectActive
                     || !currentBlackEffect && isBlackEffectActive
                     || !currentCyanEffect && isCyanEffectActive;
                  if (needsRestore) {
                     if (!isInSpiritRealm && !isInGhostDream && !hasAnyGhostDomainEffect) {
                        restoreOriginalSettings();
                     }

                     isRedEffectActive = currentRedEffect;
                     isFogEffectActive = currentFogEffect;
                     isGreenEffectActive = currentGreenEffect;
                     isBlueEffectActive = currentBlueEffect;
                     isGrayEffectActive = currentGrayEffect;
                     isGoldenEffectActive = currentGoldenEffect;
                     isPurpleEffectActive = currentPurpleEffect;
                     isBlackEffectActive = currentBlackEffect;
                     isCyanEffectActive = currentCyanEffect;
                  }
               }
            }
         );
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         updateScreenShake();
         updateCameraShake();
      });
   }

   public static void setScreenShake(float intensity, int duration) {
      screenShakeIntensity = intensity;
      if (duration > 0) {
         screenShakeDuration = duration;
      }

      triggerCameraShake();
   }

   private static void triggerCameraShake() {
      if (screenShakeIntensity > 0.0F) {
         currentOffsetProgress = 0.001F;
      }
   }

   private static void updateScreenShake() {
      if (screenShakeDuration > 0) {
         screenShakeDuration--;
         if (screenShakeDuration <= 0) {
            screenShakeIntensity = 0.0F;
         }
      }
   }

   private static void updateCameraShake() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - lastTickTime < 45L) {
            currentTickExecutions++;
            if (currentTickExecutions > 1) {
               return;
            }
         } else {
            lastTickTime = currentTime;
            currentTickExecutions = 0;
         }

         currentTickExecutions++;
         float shakeIntensity = screenShakeIntensity;
         if (shakeIntensity > 0.0F && currentOffsetProgress > 0.0F && currentOffsetProgress < 1.0F) {
            float deltaTime = 0.05F;
            currentOffsetProgress += deltaTime * 3.33F;
            if (currentOffsetProgress >= 1.0F) {
               currentOffsetProgress = 1.0F;
            }
         } else if (shakeIntensity <= 0.0F) {
            currentOffsetProgress = 0.0F;
         }
      }
   }

   private static void applyCameraShakeEffect(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         float shakeIntensity = screenShakeIntensity;
         if (shakeIntensity > 0.0F && currentOffsetProgress > 0.0F) {
            MatrixStack matrices = context.matrixStack();
            float offsetAmount = MathHelper.sin(currentOffsetProgress * (float) Math.PI);
            float maxRotation = shakeIntensity * 15.0F;
            float rotationAmount = offsetAmount * maxRotation;
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-rotationAmount));
         }
      }
   }

   private static void saveOriginalSettings() {
      originalColor = (float[])RenderSystem.getShaderColor().clone();
      originalFogStart = RenderSystem.getShaderFogStart();
      originalFogEnd = RenderSystem.getShaderFogEnd();
      originalFogColor = (float[])RenderSystem.getShaderFogColor().clone();
   }

   private static void restoreOriginalSettings() {
      isRestoring = true;

      try {
         RenderSystem.setShaderColor(originalColor[0], originalColor[1], originalColor[2], originalColor[3]);
         RenderSystem.setShaderFogStart(originalFogStart);
         RenderSystem.setShaderFogEnd(originalFogEnd);
         RenderSystem.setShaderFogColor(originalFogColor[0], originalFogColor[1], originalFogColor[2], originalFogColor[3]);
         isRedEffectActive = false;
         isFogEffectActive = false;
         isGreenEffectActive = false;
         isBlueEffectActive = false;
         isGrayEffectActive = false;
         isGoldenEffectActive = false;
         isPurpleEffectActive = false;
         isBlackEffectActive = false;
         isCyanEffectActive = false;
      } finally {
         isRestoring = false;
      }
   }

   private static void applyFogEffect() {
      RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 0.7F);
      PlayerEntity player = MinecraftClient.getInstance().player;
      boolean isTargetVersion = player != null && player.hasStatusEffect(ModEffects.THICK_FOG_TARGET);
      RenderSystem.setShaderFogStart(0.5F);
      RenderSystem.setShaderFogEnd(getFogEnd(isTargetVersion));
   }

   private static float getFogEnd(boolean isTarget) {
      PlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         int configRange = ClientModConfig.getInstance().getGhostDomainFogRange(player.getUuid());
         if (configRange > 0) {
            return configRange;
         }
      }

      return isTarget ? 16.0F : 48.0F;
   }

   private static float[] getGhostDreamFogColor() {
      MinecraftClient client = MinecraftClient.getInstance();
      PlayerEntity player = client.player;
      if (player == null) {
         return new float[]{0.3F, 0.3F, 0.3F, 64.0F};
      } else {
         long remainingTime = GhostDreamManager.getRemainingGameTime(player);
         long totalTime = GhostDreamManager.getTotalGameTime();
         long elapsedTime = totalTime - remainingTime;
         long gameTimeOfDay = (20000L + elapsedTime * 2L) % 24000L;
         float grayR = 0.3F;
         float grayG = 0.3F;
         float grayB = 0.3F;
         float grayFogEnd = 64.0F;
         float blackR = 0.05F;
         float blackG = 0.05F;
         float blackB = 0.05F;
         float minFogEnd = 8.0F;
         float midFogEnd = 16.0F;
         float preMidnightFogEnd = 24.0F;
         if (gameTimeOfDay < 20000L && gameTimeOfDay >= 8000L) {
            return new float[]{grayR, grayG, grayB, grayFogEnd};
         } else if (gameTimeOfDay >= 20000L) {
            float progress = (float)(gameTimeOfDay - 20000L) / 4000.0F;
            float fogEnd = grayFogEnd + (preMidnightFogEnd - grayFogEnd) * progress;
            float r = grayR + (blackR - grayR) * progress;
            float g = grayG + (blackG - grayG) * progress;
            float b = grayB + (blackB - grayB) * progress;
            return new float[]{r, g, b, fogEnd};
         } else if (gameTimeOfDay < 1500L) {
            float progress = (float)gameTimeOfDay / 1500.0F;
            float fogEnd = preMidnightFogEnd + ((minFogEnd + midFogEnd) / 2.0F - preMidnightFogEnd) * progress;
            return new float[]{blackR, blackG, blackB, fogEnd};
         } else if (gameTimeOfDay < 3000L) {
            float progress = (float)(gameTimeOfDay - 1500L) / 1500.0F;
            float fogEnd = (minFogEnd + midFogEnd) / 2.0F + (minFogEnd - (minFogEnd + midFogEnd) / 2.0F) * progress;
            return new float[]{blackR, blackG, blackB, fogEnd};
         } else if (gameTimeOfDay < 5000L) {
            float progress = (float)(gameTimeOfDay - 3000L) / 2000.0F;
            float fogEnd = minFogEnd + (midFogEnd * 2.0F - minFogEnd) * progress;
            return new float[]{blackR, blackG, blackB, fogEnd};
         } else {
            float progress = (float)(gameTimeOfDay - 5000L) / 3000.0F;
            return interpolateFogColor(blackR, blackG, blackB, midFogEnd * 2.0F, grayR, grayG, grayB, grayFogEnd, progress);
         }
      }
   }

   private static float[] interpolateFogColor(float r1, float g1, float b1, float fogEnd1, float r2, float g2, float b2, float fogEnd2, float progress) {
      float r = r1 + (r2 - r1) * progress;
      float g = g1 + (g2 - g1) * progress;
      float b = b1 + (b2 - b1) * progress;
      float fogEnd = fogEnd1 + (fogEnd2 - fogEnd1) * progress;
      return new float[]{r, g, b, fogEnd};
   }
}
