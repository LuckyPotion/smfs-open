package com.xie.smfs.client;

import com.xie.smfs.client.util.FusionCameraManager;
import com.xie.smfs.client.util.GhostShadowHeadCameraManager;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.GhostOfficerItem;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.network.packets.skills.s2c.ClientGhostOfficerQuotaHandler;
import com.xie.smfs.registry.ModEffects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public class SpiritHudOverlay implements HudRenderCallback {
   private static final int ICON_SIZE = 9;
   private static final Identifier EMPTY_SPIRIT_ICON = new Identifier("smfs", "textures/gui/player_spirit_empty.png");
   private static final Identifier FULL_SPIRIT_ICON = new Identifier("smfs", "textures/gui/player_spirit_full.png");
   private static List<GhostEntity> nearbyGhosts = new ArrayList<>();
   private static Map<UUID, Float> ghostAnimationValues = new HashMap<>();

   public static void register() {
      SpiritHudOverlay overlay = new SpiritHudOverlay();
      HudRenderCallback.EVENT.register(overlay);
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.field_1687 != null && client.field_1724 != null) {
            updateNearbyGhosts(client.field_1724);
         }
      });
   }

   private static void updateNearbyGhosts(PlayerEntity player) {
      nearbyGhosts.clear();
      Box searchBox = new Box(
         player.method_23317() - 32.0,
         player.method_23318() - 32.0,
         player.method_23321() - 32.0,
         player.method_23317() + 32.0,
         player.method_23318() + 32.0,
         player.method_23321() + 32.0
      );

      for (GhostEntity ghost : player.method_37908().method_8390(GhostEntity.class, searchBox, entity -> entity.method_5858(player) <= 1024.0)) {
         nearbyGhosts.add(ghost);
      }

      nearbyGhosts.sort((g1, g2) -> {
         double distance1 = g1.method_5858(player);
         double distance2 = g2.method_5858(player);
         return Double.compare(distance1, distance2);
      });
   }

   public void onHudRender(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.method_1551();
      if (client.field_1724 != null) {
         if (!FusionCameraManager.isZooming()) {
            if (!GhostShadowHeadCameraManager.isCameraBound()) {
               int screenWidth = context.method_51421();
               int screenHeight = context.method_51443();
               this.renderPlayerSpiritBar(context, client.field_1724);
               this.renderSanityCircle(context, client.field_1724, screenWidth, screenHeight);
               if (GhostDomainManager.isGhostDomainActive(client.field_1724)) {
                  this.renderNearbyGhostsSpirit(context, screenWidth, screenHeight);
               }

               this.renderRevivalProgress(context, client.field_1724, screenWidth, screenHeight);
               this.renderMainGhostName(context, client.field_1724, screenWidth, screenHeight);
               this.renderSpectatorModeHint(context, client.field_1724, screenWidth, screenHeight);
               this.renderGhostWindProgress(context, client.field_1724, screenWidth, screenHeight);
               this.renderGhostOfficerSuppressionQuota(context, client.field_1724, screenWidth, screenHeight);
            }
         }
      }
   }

   private void renderPlayerSpiritBar(DrawContext context, PlayerEntity player) {
      if (!player.method_7337() && !player.method_7325()) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float currentSpirit = spiritAttributes.method_10545("currentSpirit") ? (float)spiritAttributes.method_10574("currentSpirit") : 0.0F;
         float maxSpirit = spiritAttributes.method_10545("maxSpirit") ? (float)spiritAttributes.method_10574("maxSpirit") : 0.0F;
         if (!(maxSpirit <= 0.0F)) {
            int totalHearts = 10;
            int heartWidth = 8;
            int heartHeight = 9;
            int spacing = 0;
            int barWidth = totalHearts * heartWidth + (totalHearts - 1) * spacing;
            int screenWidth = context.method_51421();
            int screenHeight = context.method_51443();
            int x = screenWidth / 2 - barWidth / 2 + 50;
            int y = screenHeight - 50;
            float spiritRatio = currentSpirit / maxSpirit;
            int fullHearts = (int)(spiritRatio * totalHearts);

            for (int i = 0; i < totalHearts; i++) {
               int heartX = x + i * (heartWidth + spacing);
               if (i < fullHearts) {
                  context.method_25290(FULL_SPIRIT_ICON, heartX, y, 0.0F, 0.0F, heartWidth, heartHeight, heartWidth, heartHeight);
               } else {
                  context.method_25290(EMPTY_SPIRIT_ICON, heartX, y, 0.0F, 0.0F, heartWidth, heartHeight, heartWidth, heartHeight);
               }
            }
         }
      }
   }

   private void renderNearbyGhostsSpirit(DrawContext context, int screenWidth, int screenHeight) {
      if (!nearbyGhosts.isEmpty()) {
         int startY = screenHeight / 2 - nearbyGhosts.size() * 18 / 2;
         TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
         int iconSize = 8;
         int barWidth = 80;
         int barHeight = 3;
         int padding = 8;
         Iterator<UUID> iterator = ghostAnimationValues.keySet().iterator();

         while (iterator.hasNext()) {
            UUID id = iterator.next();
            boolean exists = nearbyGhosts.stream().anyMatch(g -> g.method_5667().equals(id));
            if (!exists) {
               iterator.remove();
            }
         }

         for (int i = 0; i < nearbyGhosts.size(); i++) {
            GhostEntity ghost = nearbyGhosts.get(i);
            int y = startY + i * 18;
            int iconX = padding;
            context.method_25294(iconX, y, iconX + iconSize, y + iconSize, -16777216);
            String name = "未知存在";
            int strength = ghost.getSpiritualStrength();
            int maxStrength = ghost.getMaxSpiritualStrength();
            String info = String.format("%s: %d/%d", name, strength, maxStrength);
            int textX = iconX + iconSize + 3;
            context.method_51448().method_22903();
            context.method_51448().method_22905(0.8F, 0.8F, 1.0F);
            context.method_51433(textRenderer, info, (int)(textX / 0.8F), (int)((y + 1) / 0.8F), 16777215, true);
            context.method_51448().method_22909();
            UUID ghostId = ghost.method_5667();
            float targetRatio = Math.min(1.0F, (float)strength / maxStrength);
            float currentRatio = ghostAnimationValues.getOrDefault(ghostId, targetRatio);
            float smoothFactor = 0.1F;
            currentRatio += (targetRatio - currentRatio) * smoothFactor;
            ghostAnimationValues.put(ghostId, currentRatio);
            int fillWidth = (int)(barWidth * currentRatio);
            int barX = textX;
            int barY = y + 8;
            this.drawRoundedRect(context, barX, barY, barX + barWidth, barY + barHeight, 1, -2144522963);
            if (fillWidth > 0) {
               this.drawRoundedRect(context, barX, barY, barX + fillWidth, barY + barHeight, 1, -2141891073);
            }
         }
      }
   }

   private void drawRoundedRect(DrawContext context, int x1, int y1, int x2, int y2, int radius, int color) {
      context.method_25294(x1 + radius, y1, x2 - radius, y2, color);
      context.method_25294(x1, y1 + radius, x2, y2 - radius, color);
      this.drawCircle(context, x1 + radius, y1 + radius, radius, color);
      this.drawCircle(context, x2 - radius, y1 + radius, radius, color);
      this.drawCircle(context, x1 + radius, y2 - radius, radius, color);
      this.drawCircle(context, x2 - radius, y2 - radius, radius, color);
   }

   private void renderRevivalProgress(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      int tamedGhostCount = PlayerEvents.countOccupiedGhostSlots(player);
      if (tamedGhostCount != 0) {
         if (MainGhostManager.hasMainGhost(player)) {
            TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
            int barHeight = 3;
            int spacing = 2;
            int totalHeight = tamedGhostCount * (barHeight + spacing);
            Text displayText = Text.method_43470("当前: ").method_10852(MainGhostManager.getMainGhostName(player));
            int mainGhostTextWidth = textRenderer.method_27525(displayText);
            int mainGhostX = screenWidth - mainGhostTextWidth - 10;
            int barWidth = mainGhostTextWidth;
            int x = mainGhostX;
            int y = screenHeight - 20 - totalHeight - 10 + 10 - 2;
            int currentY = y;

            for (int slotIndex = 0; slotIndex < 10; slotIndex++) {
               if (PlayerEvents.isGhostSlotOccupied(player, slotIndex)) {
                  int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(player, slotIndex);
                  int requiredRevivalDegree = PlayerEvents.getGhostSlotRequiredRevivalDegree(player, slotIndex);
                  float progressRatio = requiredRevivalDegree > 0 ? (float)revivalDegree / requiredRevivalDegree : 0.0F;
                  progressRatio = Math.min(progressRatio, 1.0F);
                  context.method_25294(x, currentY, x + barWidth, currentY + barHeight, -2144522963);
                  int progressWidth = (int)(barWidth * progressRatio);
                  if (progressWidth > 0) {
                     int color;
                     if (progressRatio < 0.3F) {
                        color = -2141847723;
                     } else if (progressRatio < 0.7F) {
                        color = -2130706603;
                     } else {
                        color = -2130750123;
                     }

                     context.method_25294(x, currentY, x + progressWidth, currentY + barHeight, color);
                  }

                  currentY += barHeight + spacing;
               }
            }
         }
      }
   }

   private void renderMainGhostName(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (MainGhostManager.hasMainGhost(player)) {
         Text mainGhostNameText = MainGhostManager.getMainGhostName(player);
         if (mainGhostNameText != null) {
            TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
            Text displayText = Text.method_43470("当前: ").method_10852(mainGhostNameText);
            int textWidth = textRenderer.method_27525(displayText);
            int x = screenWidth - textWidth - 10;
            int y = screenHeight - 20;
            int padding = 2;
            context.method_25294(x - padding, y - padding, x + textWidth + padding, y + 9 + padding, Integer.MIN_VALUE);
            context.method_51439(textRenderer, displayText, x, y, -10496, true);
         }
      }
   }

   private void renderGhostOfficerSuppressionQuota(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (MainGhostManager.hasMainGhost(player)) {
         if (MainGhostManager.isMainGhostType(player, GhostOfficerItem.class)) {
            if (GhostDomainManager.isGhostDomainActive(player)) {
               int remainingQuota = ClientGhostOfficerQuotaHandler.getRemainingQuota();
               int maxQuota = ClientGhostOfficerQuotaHandler.getMaxQuota();
               TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
               Text quotaText = Text.method_43470("压制名额: " + remainingQuota + "/" + maxQuota).method_27692(Formatting.field_1075);
               int textWidth = textRenderer.method_27525(quotaText);
               int x = screenWidth - textWidth - 10;
               int y = screenHeight / 2;
               int padding = 2;
               context.method_25294(x - padding, y - padding, x + textWidth + padding, y + 9 + padding, Integer.MIN_VALUE);
               context.method_51439(textRenderer, quotaText, x, y, 65535, true);
            }
         }
      }
   }

   private void renderSpectatorModeHint(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (player.method_7325()) {
         if (player.method_37908() != null && player.method_37908().method_8401().method_152()) {
            return;
         }

         TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
         Text hintText = Text.method_43470("按B立即转世！").method_27692(Formatting.field_1054);
         int textWidth = textRenderer.method_27525(hintText);
         int x = screenWidth / 2 - textWidth / 2;
         int y = screenHeight / 2 + 40;
         int padding = 4;
         context.method_25294(x - padding, y - padding, x + textWidth + padding, y + 9 + padding, Integer.MIN_VALUE);
         context.method_51439(textRenderer, hintText, x, y, 16777215, true);
      }
   }

   private void renderGhostWindProgress(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (player.method_6059(ModEffects.GHOST_WIND_EFFECT) && player.method_6059(ModEffects.CYAN_GHOST_DOMAIN_TARGET)) {
         TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
         NbtCompound playerData = PlayerEvents.getCachedData(player);
         float progress = 0.0F;
         if (playerData.method_10545("ghost_wind_effect")) {
            NbtCompound ghostWindData = playerData.method_10562("ghost_wind_effect");
            progress = ghostWindData.method_10583("wind_progress");
         }

         int barWidth = 100;
         int barHeight = 3;
         int x = screenWidth / 2 - barWidth / 2;
         int y = screenHeight - 60;
         context.method_25294(x, y, x + barWidth, y + barHeight, -2144522963);
         int progressWidth = (int)(barWidth * progress);
         if (progressWidth > 0) {
            int color;
            if (progress < 0.3F) {
               color = -2141847723;
            } else if (progress < 0.7F) {
               color = -2130706603;
            } else {
               color = -2130750123;
            }

            context.method_25294(x, y, x + progressWidth, y + barHeight, color);
         }

         context.method_25294(x - 1, y - 1, x + barWidth + 1, y, -2130706433);
         context.method_25294(x - 1, y + barHeight, x + barWidth + 1, y + barHeight + 1, -2130706433);
         context.method_25294(x - 1, y, x, y + barHeight, -2130706433);
         context.method_25294(x + barWidth, y, x + barWidth + 1, y + barHeight, -2130706433);
      }
   }

   private void renderSanityCircle(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (ModConfig.getInstance().enableSanitySystem) {
         if (!player.method_7337() && !player.method_7325()) {
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            float currentSanity = spiritAttributes.method_10545("sanity") ? (float)spiritAttributes.method_10574("sanity") : 100.0F;
            float maxSanity = spiritAttributes.method_10545("maxSanity") ? (float)spiritAttributes.method_10574("maxSanity") : 100.0F;
            float sanityRatio = Math.min(currentSanity / maxSanity, 1.0F);
            int centerX = screenWidth / 2 - 102;
            int centerY = screenHeight - 12;
            int radius = 8;
            context.method_51448().method_22903();

            try {
               TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
               String sanityText = String.format("%.0f", currentSanity);
               int textWidth = textRenderer.method_1727(sanityText);
               context.method_51433(textRenderer, sanityText, centerX - textWidth / 2, centerY - 9 / 2, 16777215, true);
            } finally {
               context.method_51448().method_22909();
            }
         }
      }
   }

   private void drawCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
      if (radius > 0) {
         if (radius == 1) {
            context.method_25294(centerX, centerY, centerX + 1, centerY + 1, color);
         } else {
            int x = 0;
            int y = radius;
            int d = 1 - radius;

            while (x <= y) {
               context.method_25294(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
               context.method_25294(centerX - x, centerY + y, centerX - x + 1, centerY + y + 1, color);
               context.method_25294(centerX + x, centerY - y, centerX + x + 1, centerY - y + 1, color);
               context.method_25294(centerX - x, centerY - y, centerX - x + 1, centerY - y + 1, color);
               context.method_25294(centerX + y, centerY + x, centerX + y + 1, centerY + x + 1, color);
               context.method_25294(centerX - y, centerY + x, centerX - y + 1, centerY + x + 1, color);
               context.method_25294(centerX + y, centerY - x, centerX + y + 1, centerY - x + 1, color);
               context.method_25294(centerX - y, centerY - x, centerX - y + 1, centerY - x + 1, color);
               if (d < 0) {
                  d += 2 * x + 3;
               } else {
                  d += 2 * (x - y) + 5;
                  y--;
               }

               x++;
            }
         }
      }
   }
}
