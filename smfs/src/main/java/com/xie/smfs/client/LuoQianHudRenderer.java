package com.xie.smfs.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public class LuoQianHudRenderer {
   private static boolean isInCombat = false;
   private static int targetSpiritualPower = 0;
   private static int displaySpiritualPower = 0;
   private static int maxSpiritualPower = 0;
   private static boolean isInvincible = false;
   private static float recoveryFactor = 0.0F;
   private static long combatStartTime = 0L;
   private static final long FADE_IN_DURATION = 300L;
   private static long lastUpdateTime = 0L;
   private static boolean isFlashing = false;
   private static long flashStartTime = 0L;
   private static final long FLASH_DURATION = 200L;
   private static long lastFlashTime = 0L;
   private static final long FLASH_COOLDOWN = 1000L;
   private static PlayerEntity trackedPlayer = null;

   public static void register() {
      HudRenderCallback.EVENT.register(LuoQianHudRenderer::render);
      ClientTickEvents.END_CLIENT_TICK.register(LuoQianHudRenderer::onClientTick);
   }

   private static void onClientTick(MinecraftClient client) {
      PlayerEntity player = client.field_1724;
      if (isInCombat && (player == null || player.method_29504() || player != trackedPlayer)) {
         reset();
      }

      trackedPlayer = player;
   }

   public static void reset() {
      isInCombat = false;
      targetSpiritualPower = 0;
      displaySpiritualPower = 0;
      maxSpiritualPower = 0;
      isInvincible = false;
      recoveryFactor = 0.0F;
      combatStartTime = 0L;
      lastUpdateTime = 0L;
      isFlashing = false;
      flashStartTime = 0L;
      lastFlashTime = 0L;
      trackedPlayer = null;
   }

   public static void updateCombatState(boolean inCombat, int currentPower, int maxPower, boolean invincible, float recovery) {
      isInCombat = inCombat;
      isInvincible = invincible;
      recoveryFactor = recovery;
      long now = System.currentTimeMillis();
      if (inCombat && targetSpiritualPower > 0 && maxPower > 0) {
         int changeAmount = Math.abs(currentPower - targetSpiritualPower);
         int actualRecovery = (int)(recoveryFactor * 10.0F);
         int flashThreshold = actualRecovery + 10;
         if (changeAmount > flashThreshold && now - lastFlashTime > 1000L) {
            isFlashing = true;
            flashStartTime = now;
            lastFlashTime = now;
         }
      }

      targetSpiritualPower = currentPower;
      maxSpiritualPower = maxPower;
      lastUpdateTime = now;
      if (inCombat && combatStartTime == 0L) {
         combatStartTime = now;
         displaySpiritualPower = currentPower;
      } else if (!inCombat) {
         combatStartTime = 0L;
         isFlashing = false;
         isInvincible = false;
         recoveryFactor = 0.0F;
      }
   }

   private static void render(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.method_1551();
      PlayerEntity player = client.field_1724;
      if (player != null && isInCombat) {
         int screenWidth = context.method_51421();
         float alpha = calculateFadeInProgress();
         if (!(alpha <= 0.0F)) {
            updateDisplayValue();
            int barWidth = 400;
            int barHeight = 10;
            int barX = (screenWidth - barWidth) / 2;
            int barY = 20;
            context.method_25294(barX, barY, barX + barWidth, barY + barHeight, -1879048192);
            float fillPercent = (float)displaySpiritualPower / maxSpiritualPower;
            int fillWidth = (int)((barWidth - 4) * fillPercent);
            int barColor = getBarColor(fillPercent);
            context.method_25294(barX + 2, barY + 2, barX + 2 + fillWidth, barY + barHeight - 2, barColor);
            int borderColor = isInvincible ? -10496 : (isFlashing ? getFlashColor() : -65536);
            context.method_49601(barX, barY, barWidth, barHeight, borderColor);
            String title = isInvincible ? "§6罗千" : "§c罗千";
            int titleWidth = client.field_1772.method_1727(title);
            context.method_51433(client.field_1772, title, barX + (barWidth - titleWidth) / 2, barY - 10, 16777215, true);
         }
      }
   }

   private static void updateDisplayValue() {
      if (displaySpiritualPower != targetSpiritualPower) {
         int diff = targetSpiritualPower - displaySpiritualPower;
         int step = diff / 5;
         if (Math.abs(diff) < 5) {
            displaySpiritualPower = targetSpiritualPower;
         } else {
            displaySpiritualPower += step;
         }
      }
   }

   private static int getBarColor(float fillPercent) {
      return -7667712;
   }

   private static int getFlashColor() {
      float flashProgress = (float)(System.currentTimeMillis() - flashStartTime) / 200.0F;
      if (flashProgress >= 1.0F) {
         isFlashing = false;
         return -65536;
      } else {
         float flashIntensity = (float)Math.sin(flashProgress * Math.PI * 4.0);
         int whiteMix = (int)(Math.abs(flashIntensity) * 255.0F);
         return 0xFF000000 | 0xFF0000 | 255 - whiteMix << 8 | 255 - whiteMix;
      }
   }

   private static float calculateFadeInProgress() {
      if (combatStartTime == 0L) {
         return 0.0F;
      }

      long currentTime = System.currentTimeMillis();
      long elapsedTime = currentTime - combatStartTime;
      return elapsedTime < 300L ? (float)elapsedTime / 300.0F : 1.0F;
   }
}
