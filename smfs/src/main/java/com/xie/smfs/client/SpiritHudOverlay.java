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
         if (client.world != null && client.player != null) {
            updateNearbyGhosts(client.player);
         }
      });
   }

   private static void updateNearbyGhosts(PlayerEntity player) {
      nearbyGhosts.clear();
      Box searchBox = new Box(
         player.getX() - 32.0, player.getY() - 32.0, player.getZ() - 32.0, player.getX() + 32.0, player.getY() + 32.0, player.getZ() + 32.0
      );

      for (GhostEntity ghost : player.getWorld().getEntitiesByClass(GhostEntity.class, searchBox, entity -> entity.squaredDistanceTo(player) <= 1024.0)) {
         nearbyGhosts.add(ghost);
      }

      nearbyGhosts.sort((g1, g2) -> {
         double distance1 = g1.squaredDistanceTo(player);
         double distance2 = g2.squaredDistanceTo(player);
         return Double.compare(distance1, distance2);
      });
   }

   public void onHudRender(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         if (!FusionCameraManager.isZooming()) {
            if (!GhostShadowHeadCameraManager.isCameraBound()) {
               int screenWidth = context.getScaledWindowWidth();
               int screenHeight = context.getScaledWindowHeight();
               this.renderPlayerSpiritBar(context, client.player);
               this.renderSanityCircle(context, client.player, screenWidth, screenHeight);
               if (GhostDomainManager.isGhostDomainActive(client.player)) {
                  this.renderNearbyGhostsSpirit(context, screenWidth, screenHeight);
               }

               this.renderRevivalProgress(context, client.player, screenWidth, screenHeight);
               this.renderMainGhostName(context, client.player, screenWidth, screenHeight);
               this.renderSpectatorModeHint(context, client.player, screenWidth, screenHeight);
               this.renderGhostWindProgress(context, client.player, screenWidth, screenHeight);
               this.renderGhostOfficerSuppressionQuota(context, client.player, screenWidth, screenHeight);
            }
         }
      }
   }

   private void renderPlayerSpiritBar(DrawContext context, PlayerEntity player) {
      if (!player.isCreative() && !player.isSpectator()) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float currentSpirit = spiritAttributes.contains("currentSpirit") ? (float)spiritAttributes.getDouble("currentSpirit") : 0.0F;
         float maxSpirit = spiritAttributes.contains("maxSpirit") ? (float)spiritAttributes.getDouble("maxSpirit") : 0.0F;
         if (!(maxSpirit <= 0.0F)) {
            int totalHearts = 10;
            int heartWidth = 8;
            int heartHeight = 9;
            int spacing = 0;
            int barWidth = totalHearts * heartWidth + (totalHearts - 1) * spacing;
            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();
            int x = screenWidth / 2 - barWidth / 2 + 50;
            int y = screenHeight - 50;
            float spiritRatio = currentSpirit / maxSpirit;
            int fullHearts = (int)(spiritRatio * totalHearts);

            for (int i = 0; i < totalHearts; i++) {
               int heartX = x + i * (heartWidth + spacing);
               if (i < fullHearts) {
                  context.drawTexture(FULL_SPIRIT_ICON, heartX, y, 0.0F, 0.0F, heartWidth, heartHeight, heartWidth, heartHeight);
               } else {
                  context.drawTexture(EMPTY_SPIRIT_ICON, heartX, y, 0.0F, 0.0F, heartWidth, heartHeight, heartWidth, heartHeight);
               }
            }
         }
      }
   }

   private void renderNearbyGhostsSpirit(DrawContext context, int screenWidth, int screenHeight) {
      if (!nearbyGhosts.isEmpty()) {
         int startY = screenHeight / 2 - nearbyGhosts.size() * 18 / 2;
         TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
         int iconSize = 8;
         int barWidth = 80;
         int barHeight = 3;
         int padding = 8;
         Iterator<UUID> iterator = ghostAnimationValues.keySet().iterator();

         while (iterator.hasNext()) {
            UUID id = iterator.next();
            boolean exists = nearbyGhosts.stream().anyMatch(g -> g.getUuid().equals(id));
            if (!exists) {
               iterator.remove();
            }
         }

         for (int i = 0; i < nearbyGhosts.size(); i++) {
            GhostEntity ghost = nearbyGhosts.get(i);
            int y = startY + i * 18;
            int iconX = padding;
            context.fill(iconX, y, iconX + iconSize, y + iconSize, -16777216);
            String name = "未知存在";
            int strength = ghost.getSpiritualStrength();
            int maxStrength = ghost.getMaxSpiritualStrength();
            String info = String.format("%s: %d/%d", name, strength, maxStrength);
            int textX = iconX + iconSize + 3;
            context.getMatrices().push();
            context.getMatrices().scale(0.8F, 0.8F, 1.0F);
            context.drawText(textRenderer, info, (int)(textX / 0.8F), (int)((y + 1) / 0.8F), 16777215, true);
            context.getMatrices().pop();
            UUID ghostId = ghost.getUuid();
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
      context.fill(x1 + radius, y1, x2 - radius, y2, color);
      context.fill(x1, y1 + radius, x2, y2 - radius, color);
      this.drawCircle(context, x1 + radius, y1 + radius, radius, color);
      this.drawCircle(context, x2 - radius, y1 + radius, radius, color);
      this.drawCircle(context, x1 + radius, y2 - radius, radius, color);
      this.drawCircle(context, x2 - radius, y2 - radius, radius, color);
   }

   private void renderRevivalProgress(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      int tamedGhostCount = PlayerEvents.countOccupiedGhostSlots(player);
      if (tamedGhostCount != 0) {
         if (MainGhostManager.hasMainGhost(player)) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int barHeight = 3;
            int spacing = 2;
            int totalHeight = tamedGhostCount * (barHeight + spacing);
            Text displayText = Text.literal("当前: ").append(MainGhostManager.getMainGhostName(player));
            int mainGhostTextWidth = textRenderer.getWidth(displayText);
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
                  context.fill(x, currentY, x + barWidth, currentY + barHeight, -2144522963);
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

                     context.fill(x, currentY, x + progressWidth, currentY + barHeight, color);
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
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            Text displayText = Text.literal("当前: ").append(mainGhostNameText);
            int textWidth = textRenderer.getWidth(displayText);
            int x = screenWidth - textWidth - 10;
            int y = screenHeight - 20;
            int padding = 2;
            context.fill(x - padding, y - padding, x + textWidth + padding, y + 9 + padding, Integer.MIN_VALUE);
            context.drawText(textRenderer, displayText, x, y, -10496, true);
         }
      }
   }

   private void renderGhostOfficerSuppressionQuota(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (MainGhostManager.hasMainGhost(player)) {
         if (MainGhostManager.isMainGhostType(player, GhostOfficerItem.class)) {
            if (GhostDomainManager.isGhostDomainActive(player)) {
               int remainingQuota = ClientGhostOfficerQuotaHandler.getRemainingQuota();
               int maxQuota = ClientGhostOfficerQuotaHandler.getMaxQuota();
               TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
               Text quotaText = Text.literal("压制名额: " + remainingQuota + "/" + maxQuota).formatted(Formatting.AQUA);
               int textWidth = textRenderer.getWidth(quotaText);
               int x = screenWidth - textWidth - 10;
               int y = screenHeight / 2;
               int padding = 2;
               context.fill(x - padding, y - padding, x + textWidth + padding, y + 9 + padding, Integer.MIN_VALUE);
               context.drawText(textRenderer, quotaText, x, y, 65535, true);
            }
         }
      }
   }

   private void renderSpectatorModeHint(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (player.isSpectator()) {
         if (player.getWorld() != null && player.getWorld().getLevelProperties().isHardcore()) {
            return;
         }

         TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
         Text hintText = Text.literal("按B立即转世！").formatted(Formatting.YELLOW);
         int textWidth = textRenderer.getWidth(hintText);
         int x = screenWidth / 2 - textWidth / 2;
         int y = screenHeight / 2 + 40;
         int padding = 4;
         context.fill(x - padding, y - padding, x + textWidth + padding, y + 9 + padding, Integer.MIN_VALUE);
         context.drawText(textRenderer, hintText, x, y, 16777215, true);
      }
   }

   private void renderGhostWindProgress(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (player.hasStatusEffect(ModEffects.GHOST_WIND_EFFECT) && player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET)) {
         TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
         NbtCompound playerData = PlayerEvents.getCachedData(player);
         float progress = 0.0F;
         if (playerData.contains("ghost_wind_effect")) {
            NbtCompound ghostWindData = playerData.getCompound("ghost_wind_effect");
            progress = ghostWindData.getFloat("wind_progress");
         }

         int barWidth = 100;
         int barHeight = 3;
         int x = screenWidth / 2 - barWidth / 2;
         int y = screenHeight - 60;
         context.fill(x, y, x + barWidth, y + barHeight, -2144522963);
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

            context.fill(x, y, x + progressWidth, y + barHeight, color);
         }

         context.fill(x - 1, y - 1, x + barWidth + 1, y, -2130706433);
         context.fill(x - 1, y + barHeight, x + barWidth + 1, y + barHeight + 1, -2130706433);
         context.fill(x - 1, y, x, y + barHeight, -2130706433);
         context.fill(x + barWidth, y, x + barWidth + 1, y + barHeight, -2130706433);
      }
   }

   private void renderSanityCircle(DrawContext context, PlayerEntity player, int screenWidth, int screenHeight) {
      if (ModConfig.getInstance().enableSanitySystem) {
         if (!player.isCreative() && !player.isSpectator()) {
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            float currentSanity = spiritAttributes.contains("sanity") ? (float)spiritAttributes.getDouble("sanity") : 100.0F;
            float maxSanity = spiritAttributes.contains("maxSanity") ? (float)spiritAttributes.getDouble("maxSanity") : 100.0F;
            float sanityRatio = Math.min(currentSanity / maxSanity, 1.0F);
            int centerX = screenWidth / 2 - 102;
            int centerY = screenHeight - 12;
            int radius = 8;
            context.getMatrices().push();

            try {
               TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
               String sanityText = String.format("%.0f", currentSanity);
               int textWidth = textRenderer.getWidth(sanityText);
               context.drawText(textRenderer, sanityText, centerX - textWidth / 2, centerY - 9 / 2, 16777215, true);
            } finally {
               context.getMatrices().pop();
            }
         }
      }
   }

   private void drawCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
      if (radius > 0) {
         if (radius == 1) {
            context.fill(centerX, centerY, centerX + 1, centerY + 1, color);
         } else {
            int x = 0;
            int y = radius;
            int d = 1 - radius;

            while (x <= y) {
               context.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
               context.fill(centerX - x, centerY + y, centerX - x + 1, centerY + y + 1, color);
               context.fill(centerX + x, centerY - y, centerX + x + 1, centerY - y + 1, color);
               context.fill(centerX - x, centerY - y, centerX - x + 1, centerY - y + 1, color);
               context.fill(centerX + y, centerY + x, centerX + y + 1, centerY + x + 1, color);
               context.fill(centerX - y, centerY + x, centerX - y + 1, centerY + x + 1, color);
               context.fill(centerX + y, centerY - x, centerX + y + 1, centerY - x + 1, color);
               context.fill(centerX - y, centerY - x, centerX - y + 1, centerY - x + 1, color);
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
