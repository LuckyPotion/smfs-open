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
      MinecraftClient client = MinecraftClient.method_1551();
      if (ClientDataManager.isInGhostDream()) {
         TextRenderer textRenderer = client.field_1772;
         int screenWidth = context.method_51421();
         int screenHeight = context.method_51443();
         String dreamTime = ClientDataManager.getFormattedGhostDreamTime();
         Text timeText = Text.method_43470("当前时间: ")
            .method_27692(Formatting.field_1064)
            .method_10852(Text.method_43470(dreamTime).method_27692(Formatting.field_1061));
         int textWidth = textRenderer.method_27525(timeText);
         int x = screenWidth - textWidth - 10;
         int y = 10;
         context.method_51439(textRenderer, timeText, x, y, 16777215, true);
         long remainingTime = ClientDataManager.getGhostDreamRemainingTime();
         long totalTime = 12000L;
         int progress = (int)(remainingTime * 100L / totalTime);
         int barHeight = 4;
         int barY = y + 9 + 4;
         drawProgressBar(context, x, barY, textWidth, barHeight, progress);
         Text hintText = Text.method_43470("目标：活下去！").method_27692(Formatting.field_1080);
         int hintWidth = textRenderer.method_27525(hintText);
         int hintY = barY + barHeight + 4;
         context.method_51439(textRenderer, hintText, x, hintY, 8947848, true);
      }
   }

   private static void drawProgressBar(DrawContext context, int x, int y, int width, int height, int progress) {
      context.method_25294(x, y, x + width, y + height, 1140850688);
      int progressWidth = (int)(progress / 100.0 * width);
      int color = getProgressColor(progress);
      context.method_25294(x, y, x + progressWidth, y + height, color);
      context.method_25294(x, y, x + width, y + 1, -1996488705);
      context.method_25294(x, y + height - 1, x + width, y + height, -1996488705);
      context.method_25294(x, y, x + 1, y + height, -1996488705);
      context.method_25294(x + width - 1, y, x + width, y + height, -1996488705);
   }

   private static int getProgressColor(int progress) {
      if (progress > 60) {
         return -12255420;
      } else {
         return progress > 30 ? -188 : -48060;
      }
   }
}
