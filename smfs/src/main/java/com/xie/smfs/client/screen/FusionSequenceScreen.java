package com.xie.smfs.client.screen;

import com.xie.smfs.client.preset.BlackScreenOverlay;
import com.xie.smfs.client.util.FusionCameraManager;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.registry.ModSounds;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class FusionSequenceScreen extends Screen {
   private static final float SEQUENCE_DURATION = 12.0F;
   private static final float BLACK_FADE_START = 9.0F;
   private static final float BLACK_FADE_DURATION = 3.0F;
   private long startTime;
   private boolean sequenceComplete = false;
   private float rotationAngle = 0.0F;

   public FusionSequenceScreen() {
      super(Text.literal(""));
   }

   protected void init() {
      this.startTime = System.currentTimeMillis();
      FusionCameraManager.startZoom();
      this.sequenceComplete = false;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.player != null) {
         client.player.playSound(ModSounds.YANG_JIAN_ENTRANCE, 1.0F, 1.0F);
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      long elapsed = System.currentTimeMillis() - this.startTime;
      float elapsedSeconds = (float)elapsed / 1000.0F;
      if (!this.sequenceComplete) {
         if (elapsedSeconds >= 12.0F) {
            this.startBlackScreen();
            this.sequenceComplete = true;
         } else {
            float progress = elapsedSeconds / 12.0F;
            this.renderSequenceBackground(context, progress);
            this.spawnParticles();
            if (elapsedSeconds >= 9.0F) {
               float blackAlpha = MathHelper.clamp((elapsedSeconds - 9.0F) / 3.0F, 0.0F, 1.0F);
               int alpha = (int)(blackAlpha * 255.0F);
               context.fill(0, 0, this.width, this.height, alpha << 24 | 0);
            }
         }
      }

      super.render(context, mouseX, mouseY, delta);
   }

   private void renderSequenceBackground(DrawContext context, float progress) {
      int vignetteAlpha = (int)(progress * 80.0F);
      context.fill(0, 0, this.width, this.height, vignetteAlpha << 24);
   }

   private void spawnParticles() {
   }

   private void startBlackScreen() {
      WorldConfig worldConfig = WorldConfig.loadDefault();
      boolean isLinear = "linear".equals(worldConfig.endingMode);
      if (isLinear) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null) {
            client.execute(() -> {
               FusionCameraManager.stopZoom();
               client.setScreen(new LinearEndingOverlay());
            });
         }
      } else {
         BlackScreenOverlay.showOpaque("六十年以后...", this::onBlackScreenComplete, FusionCameraManager::stopZoom, 2.5F);
      }
   }

   private void onBlackScreenComplete() {
      this.showOpenEnding();
   }

   private void showOpenEnding() {
      new Thread(() -> {
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var2) {
         }

         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null) {
            client.execute(this::showSuccessPopup);
         }
      }).start();
   }

   private void showSuccessPopup() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null) {
         client.execute(() -> {
            List<String> contents = Arrays.asList("", "你为了一个极其特殊的存在", "继承鬼童的全部属性", "可驾驭灵异数量+4", "不再担心厉鬼复苏且驾驭的厉鬼均达到全盛状态", "没有承受极限，使用灵异力量将不再有技能冷却");
            GhostAbilityPopupScreen.showSingleColumnPopup("融合成功", 16766720, 1.2F, contents);
         });
      }
   }

   public boolean shouldPause() {
      return false;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public void close() {
      FusionCameraManager.stopZoom();
      super.close();
   }
}
