package com.xie.smfs.client.render;

import com.xie.smfs.client.data.ClientDataManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GhostDreamHudRenderer {
   public static void register() {
      HudRenderCallback.EVENT.register(GhostDreamHudRenderer::render);
   }

   private static void render(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (ClientDataManager.isInGhostDream()) {
         TextRenderer textRenderer = client.textRenderer;
         int screenWidth = context.getScaledWindowWidth();
         int screenHeight = context.getScaledWindowHeight();
         String dreamTime = ClientDataManager.getFormattedGhostDreamTime();
         Text timeText = Text.literal("当前时间: ").formatted(Formatting.DARK_PURPLE).append(Text.literal(dreamTime).formatted(Formatting.RED));
         int textWidth = textRenderer.getWidth(timeText);
         int x = screenWidth - textWidth - 10;
         int y = 10;
         context.drawText(textRenderer, timeText, x, y, 16777215, true);
         long remainingTime = ClientDataManager.getGhostDreamRemainingTime();
         long totalTime = 12000L;
         int progress = (int)(remainingTime * 100L / totalTime);
         int barHeight = 4;
         int barY = y + 9 + 4;
         drawProgressBar(context, x, barY, textWidth, barHeight, progress);
         Text hintText = Text.literal("目标：活下去！").formatted(Formatting.GRAY);
         int hintWidth = textRenderer.getWidth(hintText);
         int hintY = barY + barHeight + 4;
         context.drawText(textRenderer, hintText, x, hintY, 8947848, true);
      }
   }

   private static void drawProgressBar(DrawContext context, int x, int y, int width, int height, int progress) {
      context.fill(x, y, x + width, y + height, 1140850688);
      int progressWidth = (int)(progress / 100.0 * width);
      int color = getProgressColor(progress);
      context.fill(x, y, x + progressWidth, y + height, color);
      context.fill(x, y, x + width, y + 1, -1996488705);
      context.fill(x, y + height - 1, x + width, y + height, -1996488705);
      context.fill(x, y, x + 1, y + height, -1996488705);
      context.fill(x + width - 1, y, x + width, y + height, -1996488705);
   }

   private static int getProgressColor(int progress) {
      if (progress > 60) {
         return -12255420;
      } else {
         return progress > 30 ? -188 : -48060;
      }
   }
}
