package com.xie.smfs.mixin.client;

import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {
   @Unique
   private static final Logger LOGGER = LoggerFactory.getLogger(InventoryScreenMixin.class);
   @Unique
   private static final Identifier OPEN_GHOST_SCREEN_PACKET = new Identifier("smfs", "open_ghost_screen");
   @Unique
   private static final Identifier TUTORIAL_BUTTON_TEXTURE = new Identifier("smfs", "textures/gui/tutorial_button.png");

   public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
      super(handler, inventory, title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void addGhostControlButton(CallbackInfo ci) {
      int ghostButtonX = this.x + 140;
      int ghostButtonY = this.y + 60;
      ButtonWidget ghostControlButton = ButtonWidget.builder(Text.translatable("button.smfs.ghost_control"), button -> {
         if (this.client != null && this.client.player != null) {
            try {
               PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
               this.client.player.networkHandler.sendPacket(new CustomPayloadC2SPacket(OPEN_GHOST_SCREEN_PACKET, buf));
            } catch (Exception e) {
               LOGGER.error("发送数据包时发生异常: ", e);
            }
         } else {
            LOGGER.warn("客户端或玩家对象为空，无法发送数据包");
         }
      }).position(ghostButtonX, ghostButtonY).size(25, 18).build();
      this.addDrawableChild(ghostControlButton);
   }
}
