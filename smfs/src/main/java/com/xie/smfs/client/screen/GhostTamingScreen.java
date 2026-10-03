package com.xie.smfs.client.screen;

import com.xie.smfs.event.screen.GhostTamingScreenHandler;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.network.GhostTamingProgressPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostTamingScreen extends HandledScreen<GhostTamingScreenHandler> {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostTamingScreen");
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/ghost_taming_background.png");
   private static final int TEXTURE_WIDTH = 176;
   private static final int TEXTURE_HEIGHT = 166;
   private static final int BUTTON_X = 70;
   private static final int BUTTON_Y = 50;
   private static final int BUTTON_WIDTH = 40;
   private static final int BUTTON_HEIGHT = 16;
   private static final int SUCCESS_RATE_TEXT_X = 70;
   private static final int SUCCESS_RATE_TEXT_Y = 70;
   private ButtonWidget tameButton;
   private static final int PROGRESS_BAR_X = 50;
   private static final int PROGRESS_BAR_Y = 45;
   private static final int PROGRESS_BAR_WIDTH = 80;
   private static final int PROGRESS_BAR_HEIGHT = 4;
   private static final int SLIDER_SIZE = 3;
   private static final int PROGRESS_BAR_BACKGROUND_COLOR = -11184811;
   private static final int PROGRESS_BAR_FOREGROUND_COLOR = -16711936;
   private static final int SLIDER_COLOR = -1;
   private float tamingProgress = 0.0F;
   private float totalProgress = 0.0F;
   private float sliderPosition = 0.0F;
   private float targetZoneStart = 0.3F;
   private float targetZoneEnd = 0.7F;
   private int clickCount = 0;
   private long lastClickTime = 0L;
   private int zoneChangeCount = 0;
   private long lastZoneChangeTime = 0L;
   private static final int MAX_ZONE_CHANGES = 3;
   private static final long ZONE_CHANGE_INTERVAL = 8000L;
   private static final float DECAY_RATE = 0.004F;
   private static final float BASE_INCREASE = 0.04F;

   public GhostTamingScreen(GhostTamingScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 176;
      this.backgroundHeight = 166;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
      if (handler instanceof GhostTamingScreenHandler) {
         String ghostType = handler.getGhostType();
         this.setGhostDifficulty(ghostType);
      }
   }

   protected void init() {
      super.init();
      if (this.handler instanceof GhostTamingScreenHandler) {
         String ghostType = ((GhostTamingScreenHandler)this.handler).getGhostType();
         this.setGhostDifficulty(ghostType);
      }

      this.tameButton = ButtonWidget.builder(Text.translatable("screen.smfs.ghost_taming.tame"), button -> this.onTameButtonClicked())
         .dimensions(this.x + 70 + 3, this.y + 50 + 5, 40, 16)
         .build();
      this.addDrawableChild(this.tameButton);
      this.updateButtonState();
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.drawProgressBar(context);
      this.drawSuccessRatedebug(context);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
   }

   protected void handledScreenTick() {
      super.handledScreenTick();
      this.updateButtonState();
      this.updateTamingProgress();
   }

   private void updateButtonState() {
      if (this.tameButton != null && this.handler != null) {
         this.tameButton.active = true;
      }
   }

   private void updateTamingProgress() {
      if (this.tamingProgress > 0.0F) {
         this.tamingProgress -= 0.004F;
         if (this.tamingProgress < 0.0F) {
            this.tamingProgress = 0.0F;
         }
      }

      this.sliderPosition = this.tamingProgress;
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastClickTime > 2000L) {
         this.clickCount = 0;
      }

      if (this.tamingProgress > 0.0F) {
         this.changeTargetZone();
      }

      boolean inTargetZone = this.sliderPosition >= this.targetZoneStart && this.sliderPosition <= this.targetZoneEnd;
      if (inTargetZone && this.tamingProgress > 0.0F) {
         this.totalProgress = this.totalProgress + this.tamingProgress * 0.02F;
         if (this.totalProgress >= 1.0F) {
            this.totalProgress = 1.0F;
            this.executeTaming();
         }
      }

      if (this.totalProgress > 0.0F && this.client != null && this.client.player != null && this.handler != null) {
         PacketByteBuf buf = PacketByteBufs.create();
         buf.writeFloat(this.totalProgress);
         buf.writeInt(((GhostTamingScreenHandler)this.handler).syncId);
         ClientPlayNetworking.send(GhostTamingProgressPacket.ID, buf);
      }
   }

   private boolean validateTamingConditionsClient() {
      if (this.handler != null && this.client != null && this.client.player != null) {
         ItemStack containerStack = ((GhostTamingScreenHandler)this.handler).getSlot(0).getStack();
         if (!containerStack.isEmpty() && containerStack.getItem() instanceof GoldenContainerItem) {
            NbtCompound nbt = containerStack.getOrCreateNbt();
            boolean hasGhost = nbt.getBoolean("HasGhost");
            if (!hasGhost) {
               LOGGER.debug("客户端检查失败：黄金容器中没有鬼");
               return false;
            } else {
               NbtCompound contained = nbt.getCompound("ContainedGhost");
               String ghostType = contained.getString("id");
               if (ghostType.isEmpty()) {
                  LOGGER.debug("客户端检查失败：无法识别鬼的类型");
                  return false;
               } else {
                  return true;
               }
            }
         } else {
            LOGGER.debug("客户端检查失败：没有黄金容器");
            return false;
         }
      } else {
         return false;
      }
   }

   private void onTameButtonClicked() {
      LOGGER.debug("客户端驾驭按钮被点击");
      if (!this.validateTamingConditionsClient()) {
         LOGGER.debug("客户端检查失败，点击无效");
         if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("§c缺少装有厉鬼的黄金容器"), false);
         }
      } else {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastClickTime < 500L) {
            this.clickCount++;
         } else {
            this.clickCount = 1;
         }

         this.lastClickTime = currentTime;
         this.tamingProgress += 0.04F;
         if (this.tamingProgress > 1.0F) {
            this.tamingProgress = 1.0F;
         }
      }
   }

   private void executeTaming() {
      LOGGER.debug("strate成功！总进度已达到100%");
      if (this.client != null && this.handler != null) {
         PacketByteBuf buf = PacketByteBufs.create();
         buf.writeInt(1);
         buf.writeInt(((GhostTamingScreenHandler)this.handler).syncId);
         ClientPlayNetworking.send(GhostTamingScreenHandler.BUTTON_CLICK_PACKET_ID, buf);
         LOGGER.debug("已发送strate成功请求到服务器");
      }
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
   }

   private void drawProgressBar(DrawContext context) {
      int barX = this.x + 50;
      int barY = this.y + 45;
      context.fill(barX, barY, barX + 80, barY + 4, -11184811);
      int totalProgressWidth = (int)(this.totalProgress * 80.0F);
      context.fill(barX, barY, barX + totalProgressWidth, barY + 4, -16711936);
      int targetStartX = barX + (int)(this.targetZoneStart * 80.0F);
      int targetEndX = barX + (int)(this.targetZoneEnd * 80.0F);
      context.fill(targetStartX, barY, targetEndX, barY + 4, -23296);
      int sliderX = barX + (int)(this.sliderPosition * 77.0F);
      int sliderY = barY - 2;
      context.fill(sliderX, sliderY, sliderX + 3, sliderY + 4 + 4, -1);
   }

   public void setGhostDifficulty(String ghostType) {
      float zoneSize = 0.15F;
      this.targetZoneStart = 0.5F - zoneSize / 2.0F;
      this.targetZoneEnd = 0.5F + zoneSize / 2.0F;
      LOGGER.debug("设置鬼类型'{}'的目标区间宽度: {}%, 区间: [{}, {}]", ghostType, zoneSize * 100.0F, this.targetZoneStart, this.targetZoneEnd);
   }

   private void changeTargetZone() {
      if (this.zoneChangeCount < 3) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastZoneChangeTime >= 8000L) {
            float currentZoneSize = this.targetZoneEnd - this.targetZoneStart;
            float newZoneSize = currentZoneSize - 0.02F;
            if (newZoneSize < 0.02F) {
               newZoneSize = 0.02F;
            }

            float zoneCenter = 0.2F + (float)Math.random() * 0.6F;
            this.targetZoneStart = Math.max(0.0F, zoneCenter - newZoneSize / 2.0F);
            this.targetZoneEnd = Math.min(1.0F, zoneCenter + newZoneSize / 2.0F);
            this.zoneChangeCount++;
            this.lastZoneChangeTime = currentTime;
            LOGGER.debug("目标区间变化第{}次: 宽度{}%, 区间: [{}, {}]", this.zoneChangeCount, newZoneSize * 100.0F, this.targetZoneStart, this.targetZoneEnd);
         }
      }
   }

   private void drawSuccessRatedebug(DrawContext context) {
      boolean hasControlSlot = this.checkPlayerHasControlSlot();
      String successRateText;
      int textColor;
      if (hasControlSlot) {
         successRateText = "85%成功率";
         textColor = 65280;
      } else {
         successRateText = "20%成功率";
         textColor = 16753920;
      }

      int textX = this.x + 70 + 6;
      int textY = this.y + 70 + 5;
      context.getMatrices().push();
      context.getMatrices().scale(0.8F, 0.8F, 1.0F);
      context.drawText(this.textRenderer, Text.literal(successRateText), (int)(textX / 0.8F), (int)(textY / 0.8F), textColor, false);
      context.getMatrices().pop();
   }

   private boolean checkPlayerHasControlSlot() {
      if (this.client != null && this.client.player != null) {
         PlayerEntity player = this.client.player;

         for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem().getTranslationKey().contains("control_slot")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
