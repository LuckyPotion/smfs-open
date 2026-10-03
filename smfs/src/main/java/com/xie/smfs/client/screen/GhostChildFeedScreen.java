package com.xie.smfs.client.screen;

import com.xie.smfs.event.screen.GhostChildCultivationScreenHandler;
import com.xie.smfs.event.screen.GhostChildFeedScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GhostChildFeedScreen extends HandledScreen<GhostChildFeedScreenHandler> {
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("smfs", "textures/gui/ghost_taming_background.png");
   private static final int TEXTURE_WIDTH = 176;
   private static final int TEXTURE_HEIGHT = 166;
   private static final int FEED_BUTTON_X = 45;
   private static final int CLOSE_BUTTON_X = 95;
   private static final int BUTTON_Y = 50;
   private static final int BUTTON_WIDTH = 40;
   private static final int BUTTON_HEIGHT = 16;
   private ButtonWidget feedButton;
   private ButtonWidget closeButton;

   public GhostChildFeedScreen(GhostChildFeedScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
      this.backgroundWidth = 176;
      this.backgroundHeight = 166;
      this.playerInventoryTitleY = this.backgroundHeight - 94;
   }

   protected void init() {
      super.init();
      this.x = (this.width - this.backgroundWidth) / 2;
      this.y = (this.height - this.backgroundHeight) / 2;
      this.feedButton = ButtonWidget.builder(Text.literal("喂食"), button -> {
         if (this.client != null && this.handler != null && this.client.player != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(((GhostChildFeedScreenHandler)this.handler).syncId);
            ClientPlayNetworking.send(GhostChildFeedScreenHandler.FEED_BUTTON_CLICK_PACKET_ID, buf);
         }
      }).dimensions(this.x + 45 + 3, this.y + 50 + 5, 40, 16).build();
      this.closeButton = ButtonWidget.builder(
            Text.literal("关闭"),
            button -> this.client
               .setScreen(
                  new GhostChildCultivationScreen(
                     new GhostChildCultivationScreenHandler(0, this.client.player.getInventory()),
                     this.client.player.getInventory(),
                     Text.translatable("screen.smfs.ghost_child_cultivation")
                  )
               )
         )
         .dimensions(this.x + 95 + 3, this.y + 50 + 5, 40, 16)
         .build();
      this.addDrawableChild(this.feedButton);
      this.addDrawableChild(this.closeButton);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      super.render(context, mouseX, mouseY, delta);
      this.drawMouseoverTooltip(context, mouseX, mouseY);
   }

   protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
      context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
   }

   protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
      context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752, false);
   }
}
