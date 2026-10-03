package com.xie.smfs.client;

import com.xie.smfs.config.ModConfig;
import com.xie.smfs.registry.ModEffects;
import java.lang.reflect.Field;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrisCompatibility {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/IrisCompatibility");
   private static boolean wasIrisEnabledBeforeFog = false;
   private static boolean isIrisDisabledByFog = false;
   private static boolean hasGhostDomainEffectLastTick = false;
   private static boolean continuousDetectionEnabled = false;
   private static int detectionInterval = 1000;
   private static long lastDetectionTime = 0L;

   public static void init() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> updateGhostDomainState());
   }

   private static boolean hasGhostDomainEffect(PlayerEntity player) {
      return player == null
         ? false
         : player.method_6059(ModEffects.RED_GHOST_DOMAIN)
            || player.method_6059(ModEffects.RED_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)
            || player.method_6059(ModEffects.GREEN_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.BLUE_GHOST_DOMAIN)
            || player.method_6059(ModEffects.BLUE_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.GRAY_GHOST_DOMAIN)
            || player.method_6059(ModEffects.GRAY_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)
            || player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.PURPLE_GHOST_DOMAIN)
            || player.method_6059(ModEffects.PURPLE_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)
            || player.method_6059(ModEffects.BLACK_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)
            || player.method_6059(ModEffects.CYAN_GHOST_DOMAIN_TARGET)
            || player.method_6059(ModEffects.THICK_FOG)
            || player.method_6059(ModEffects.THICK_FOG_TARGET);
   }

   private static boolean isIrisEnabled() {
      try {
         Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
         return (Boolean)irisClass.getMethod("isPackInUseQuick").invoke(null);
      } catch (ClassNotFoundException e) {
         return false;
      } catch (NoSuchMethodException e) {
         return false;
      } catch (Exception e) {
         return false;
      }
   }

   private static void disableIrisShaders() {
      try {
         boolean currentIrisState = isIrisEnabled();
         if (!currentIrisState) {
            return;
         }

         wasIrisEnabledBeforeFog = true;
         isIrisDisabledByFog = true;
         Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");

         try {
            irisClass.getMethod("setShadersDisabled").invoke(null);
            return;
         } catch (NoSuchMethodException var6) {
            MinecraftClient client = MinecraftClient.method_1551();
            if (client != null) {
               try {
                  irisClass.getMethod("toggleShaders", MinecraftClient.class, boolean.class).invoke(null, client, false);
                  return;
               } catch (NoSuchMethodException var5) {
               }
            }

            try {
               Field shadersEnabledField = irisClass.getDeclaredField("shadersEnabled");
               shadersEnabledField.setAccessible(true);
               shadersEnabledField.setBoolean(null, false);
               return;
            } catch (Exception var4) {
            }
         }
      } catch (ClassNotFoundException var7) {
      } catch (Exception var8) {
      }
   }

   private static void restoreIrisShaders() {
      try {
         if (!isIrisDisabledByFog) {
            return;
         }

         boolean currentIrisState = isIrisEnabled();
         if (currentIrisState != wasIrisEnabledBeforeFog) {
            if (wasIrisEnabledBeforeFog && !currentIrisState) {
               Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");

               try {
                  irisClass.getMethod("setShadersEnabled").invoke(null);
               } catch (NoSuchMethodException e) {
                  MinecraftClient client = MinecraftClient.method_1551();
                  if (client != null) {
                     try {
                        irisClass.getMethod("toggleShaders", MinecraftClient.class, boolean.class).invoke(null, client, true);
                     } catch (NoSuchMethodException e2) {
                        try {
                           Field shadersEnabledField = irisClass.getDeclaredField("shadersEnabled");
                           shadersEnabledField.setAccessible(true);
                           shadersEnabledField.setBoolean(null, true);
                        } catch (Exception var6) {
                        }
                     }
                  }
               }
            } else if (!wasIrisEnabledBeforeFog && currentIrisState) {
            }
         }

         wasIrisEnabledBeforeFog = false;
         isIrisDisabledByFog = false;
      } catch (ClassNotFoundException var9) {
      } catch (Exception var10) {
      }
   }

   private static void updateGhostDomainState() {
      MinecraftClient client = MinecraftClient.method_1551();
      if (client != null && client.field_1724 != null) {
         PlayerEntity player = client.field_1724;
         boolean hasGhostDomainEffect = hasGhostDomainEffect(player);
         if (hasGhostDomainEffect && !hasGhostDomainEffectLastTick) {
            if (ModConfig.getInstance().irisCompatibilityMode == 0) {
               disableIrisShaders();
            }
         } else if (!hasGhostDomainEffect && hasGhostDomainEffectLastTick && ModConfig.getInstance().irisCompatibilityMode == 0) {
            restoreIrisShaders();
         }

         hasGhostDomainEffectLastTick = hasGhostDomainEffect;
      } else {
         hasGhostDomainEffectLastTick = false;
      }
   }

   public static void forceRestoreIrisShaders() {
      restoreIrisShaders();
   }

   public static boolean isIrisDisabledByGhostDomain() {
      return isIrisDisabledByFog;
   }

   public static boolean hasActiveGhostDomain() {
      return hasGhostDomainEffectLastTick;
   }

   public static void startContinuousDetection() {
      continuousDetectionEnabled = true;
      lastDetectionTime = 0L;
   }

   public static void stopContinuousDetection() {
      continuousDetectionEnabled = false;
   }

   public static void setDetectionInterval(int intervalMs) {
      detectionInterval = intervalMs;
   }

   public static void updateContinuousDetection() {
      if (continuousDetectionEnabled) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - lastDetectionTime >= detectionInterval) {
            boolean irisEnabled = isIrisEnabled();
            MinecraftClient client = MinecraftClient.method_1551();
            if (client != null && client.field_1724 != null) {
               boolean hasGhostEffect = hasGhostDomainEffect(client.field_1724);
               boolean var5 = isIrisDisabledByFog;
            }

            lastDetectionTime = currentTime;
         }
      }
   }

   public static void triggerDetection() {
      boolean irisEnabled = isIrisEnabled();
      MinecraftClient client = MinecraftClient.method_1551();
      if (client != null && client.field_1724 != null) {
         boolean hasGhostEffect = hasGhostDomainEffect(client.field_1724);
         boolean var3 = isIrisDisabledByFog;
      }
   }

   public static void enableContinuousDetection(int intervalMs) {
      setDetectionInterval(intervalMs);
      startContinuousDetection();
   }
}
