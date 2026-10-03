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
      this.field_2792 = 176;
      this.field_2779 = 166;
      this.field_25270 = this.field_2779 - 94;
      if (handler instanceof GhostTamingScreenHandler) {
         String ghostType = handler.getGhostType();
         this.setGhostDifficulty(ghostType);
      }
   }

   protected void method_25426() {
      super.method_25426();
      if (this.field_2797 instanceof GhostTamingScreenHandler) {
         String ghostType = ((GhostTamingScreenHandler)this.field_2797).getGhostType();
         this.setGhostDifficulty(ghostType);
      }

      this.tameButton = ButtonWidget.method_46430(Text.method_43471("screen.smfs.ghost_taming.tame"), button -> this.onTameButtonClicked())
         .method_46434(this.field_2776 + 70 + 3, this.field_2800 + 50 + 5, 40, 16)
         .method_46431();
      this.method_37063(this.tameButton);
      this.updateButtonState();
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      super.method_25394(context, mouseX, mouseY, delta);
      this.drawProgressBar(context);
      this.drawSuccessRatedebug(context);
      this.method_2380(context, mouseX, mouseY);
   }

   protected void method_2389(DrawContext context, float delta, int mouseX, int mouseY) {
      context.method_25302(BACKGROUND_TEXTURE, this.field_2776, this.field_2800, 0, 0, this.field_2792, this.field_2779);
   }

   protected void method_37432() {
      super.method_37432();
      this.updateButtonState();
      this.updateTamingProgress();
   }

   private void updateButtonState() {
      if (this.tameButton != null && this.field_2797 != null) {
         this.tameButton.field_22763 = true;
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

      if (this.totalProgress > 0.0F && this.field_22787 != null && this.field_22787.field_1724 != null && this.field_2797 != null) {
         PacketByteBuf buf = PacketByteBufs.create();
         buf.writeFloat(this.totalProgress);
         buf.writeInt(((GhostTamingScreenHandler)this.field_2797).field_7763);
         ClientPlayNetworking.send(GhostTamingProgressPacket.ID, buf);
      }
   }

   private boolean validateTamingConditionsClient() {
      if (this.field_2797 != null && this.field_22787 != null && this.field_22787.field_1724 != null) {
         ItemStack containerStack = ((GhostTamingScreenHandler)this.field_2797).method_7611(0).method_7677();
         if (!containerStack.method_7960() && containerStack.method_7909() instanceof GoldenContainerItem) {
            NbtCompound nbt = containerStack.method_7948();
            boolean hasGhost = nbt.method_10577("HasGhost");
            if (!hasGhost) {
               LOGGER.debug("客户端检查失败：黄金容器中没有鬼");
               return false;
            } else {
               NbtCompound contained = nbt.method_10562("ContainedGhost");
               String ghostType = contained.method_10558("id");
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
         if (this.field_22787 != null && this.field_22787.field_1724 != null) {
            this.field_22787.field_1724.method_7353(Text.method_43470("§c缺少装有厉鬼的黄金容器"), false);
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
      if (this.field_22787 != null && this.field_2797 != null) {
         PacketByteBuf buf = PacketByteBufs.create();
         buf.writeInt(1);
         buf.writeInt(((GhostTamingScreenHandler)this.field_2797).field_7763);
         ClientPlayNetworking.send(GhostTamingScreenHandler.BUTTON_CLICK_PACKET_ID, buf);
         LOGGER.debug("已发送strate成功请求到服务器");
      }
   }

   protected void method_2388(DrawContext context, int mouseX, int mouseY) {
      context.method_51439(this.field_22793, this.field_29347, this.field_25269, this.field_25270, 4210752, false);
   }

   private void drawProgressBar(DrawContext context) {
      int barX = this.field_2776 + 50;
      int barY = this.field_2800 + 45;
      context.method_25294(barX, barY, barX + 80, barY + 4, -11184811);
      int totalProgressWidth = (int)(this.totalProgress * 80.0F);
      context.method_25294(barX, barY, barX + totalProgressWidth, barY + 4, -16711936);
      int targetStartX = barX + (int)(this.targetZoneStart * 80.0F);
      int targetEndX = barX + (int)(this.targetZoneEnd * 80.0F);
      context.method_25294(targetStartX, barY, targetEndX, barY + 4, -23296);
      int sliderX = barX + (int)(this.sliderPosition * 77.0F);
      int sliderY = barY - 2;
      context.method_25294(sliderX, sliderY, sliderX + 3, sliderY + 4 + 4, -1);
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

      int textX = this.field_2776 + 70 + 6;
      int textY = this.field_2800 + 70 + 5;
      context.method_51448().method_22903();
      context.method_51448().method_22905(0.8F, 0.8F, 1.0F);
      context.method_51439(this.field_22793, Text.method_43470(successRateText), (int)(textX / 0.8F), (int)(textY / 0.8F), textColor, false);
      context.method_51448().method_22909();
   }

   private boolean checkPlayerHasControlSlot() {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         PlayerEntity player = this.field_22787.field_1724;

         for (int i = 0; i < player.method_31548().method_5439(); i++) {
            ItemStack stack = player.method_31548().method_5438(i);
            if (!stack.method_7960() && stack.method_7909().method_7876().contains("control_slot")) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
