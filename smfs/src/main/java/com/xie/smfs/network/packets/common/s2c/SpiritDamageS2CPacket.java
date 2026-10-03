package com.xie.smfs.network.packets.common.s2c;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xie.smfs.client.EffectRenderHandler;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.config.ModConfig;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpiritDamageS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "spirit_damage");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/SpiritDamageS2CPacket");
   private static final AtomicReference<Float> receivedDamage = new AtomicReference<>(0.0F);
   private static final AtomicReference<Float> receivedBaseDamage = new AtomicReference<>(0.0F);
   private static float screenOverlayIntensity = 0.0F;
   private static int screenOverlayDuration = 0;
   private static float lastDamage = 0.0F;
   private static int consecutiveDamageCount = 0;
   private static long lastDamageTime = 0L;
   private static final long DAMAGE_COUNT_WINDOW_MS = 3000L;

   public static void registerClient() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         float baseDamage = buf.readFloat();
         float actualDamage = buf.readFloat();
         client.execute(() -> {
            receivedBaseDamage.set(baseDamage);
            receivedDamage.set(actualDamage);
            LOGGER.debug("客户端接收到灵异伤害: 原始伤害={}, 最终伤害={}", baseDamage, actualDamage);
            showActionBarMessage(client, baseDamage, actualDamage);
            handleSpiritDamageEffects(client, actualDamage);
         });
      });
      HudRenderCallback.EVENT.register(SpiritDamageS2CPacket::renderSpiritDamageEffects);
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> updateEffects());
   }

   private static void showActionBarMessage(MinecraftClient client, float baseDamage, float actualDamage) {
      if (client.field_1724 != null) {
         if (ModConfig.getInstance().showDamageTakenText) {
            if (client.field_1724 == null || ClientModConfig.getInstance().showDamageTakenText(client.field_1724.method_5667())) {
               long currentTime = System.currentTimeMillis();
               if (Math.abs(actualDamage - lastDamage) < 0.1F && currentTime - lastDamageTime <= 3000L) {
                  consecutiveDamageCount++;
               } else {
                  consecutiveDamageCount = 1;
               }

               lastDamage = actualDamage;
               lastDamageTime = currentTime;
               String damageText = "§c受到灵异伤害: §f" + new DecimalFormat("#.###").format(actualDamage);
               if (consecutiveDamageCount > 1) {
                  damageText = damageText + " §bx " + consecutiveDamageCount;
               }

               Text message = Text.method_43470(damageText);
               client.field_1724.method_7353(message, true);
            }
         }
      }
   }

   private static void handleSpiritDamageEffects(MinecraftClient client, float damage) {
      if (client.field_1724 != null && !(damage <= 0.0F)) {
         playSpiritDamageSounds(damage, client.field_1724);
         setScreenEffects(damage);
         float shakeIntensity = client.field_1724 != null ? ClientModConfig.getInstance().getScreenShakeIntensity(client.field_1724.method_5667()) : 1.0F;
         EffectRenderHandler.setScreenShake(shakeIntensity, 0);
      }
   }

   private static void playSpiritDamageSounds(float damageAmount, PlayerEntity player) {
      player.method_5783(SoundEvents.field_15115, 0.8F, 0.9F);
   }

   private static void setScreenEffects(float damageAmount) {
      screenOverlayIntensity = Math.min(0.4F, damageAmount / 100.0F);
      screenOverlayDuration = 8;
   }

   private static void updateEffects() {
      if (screenOverlayDuration > 0) {
         screenOverlayDuration--;
         if (screenOverlayDuration > 6) {
            float fadeInProgress = 1.0F - (screenOverlayDuration - 6) / 2.0F;
            screenOverlayIntensity = Math.min(0.4F, fadeInProgress * 0.4F);
         } else if (screenOverlayDuration <= 2) {
            float fadeOutProgress = screenOverlayDuration / 2.0F;
            screenOverlayIntensity = fadeOutProgress * 0.4F;
         }

         if (screenOverlayDuration <= 0) {
            screenOverlayIntensity = 0.0F;
         }
      }
   }

   private static void renderSpiritDamageEffects(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.method_1551();
      if (client.field_1724 != null) {
         int width = client.method_22683().method_4486();
         int height = client.method_22683().method_4502();
         if (screenOverlayIntensity > 0.0F) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int color = (int)(screenOverlayIntensity * 255.0F) << 24 | 8388736;
            context.method_25294(0, 0, width, height, color);
            RenderSystem.disableBlend();
         }
      }
   }
}
