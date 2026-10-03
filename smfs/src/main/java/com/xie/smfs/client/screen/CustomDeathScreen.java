package com.xie.smfs.client.screen;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.packets.common.c2s.ReinvadeRespawnPacket;
import com.xie.smfs.network.packets.common.c2s.RequestRespawnPacket;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDeathScreen extends Screen {
   private static final Logger LOGGER = LoggerFactory.getLogger(CustomDeathScreen.class);
   private static final Identifier BACKGROUND_TEXTURE = new Identifier("textures/gui/demo_background.png");
   private static final Identifier RESPAWN_CONFIRMATION = new Identifier("smfs", "request_respawn");
   private static final Identifier SPECTATE_CONFIRMATION = new Identifier("smfs", "spectate_mode");
   private static final Identifier REINVADE_CONFIRMATION = new Identifier("smfs", "reinvade_respawn");
   private final boolean showDeathMessage;
   private final Text deathMessage;
   private final boolean isSilentGhostReinvade;

   public CustomDeathScreen(Text deathMessage, boolean isHardcore) {
      this(deathMessage, isHardcore, false);
   }

   public CustomDeathScreen(Text deathMessage, boolean isHardcore, boolean isSilentGhostReinvade) {
      super(Text.translatable(isHardcore ? "deathScreen.title.hardcore" : "deathScreen.title"));
      this.deathMessage = deathMessage;
      this.showDeathMessage = !isHardcore;
      this.isSilentGhostReinvade = isSilentGhostReinvade;
   }

   protected void init() {
      super.init();
      ClientPlayNetworking.registerGlobalReceiver(RESPAWN_CONFIRMATION, this::handleRespawnConfirmation);
      ClientPlayNetworking.registerGlobalReceiver(SPECTATE_CONFIRMATION, this::handleSpectateConfirmation);
      ClientPlayNetworking.registerGlobalReceiver(REINVADE_CONFIRMATION, this::handleReinvadeConfirmation);
      LOGGER.debug("网络包监听器注册完成");
      int buttonWidth = 200;
      int buttonHeight = 20;
      int buttonSpacing = 25;
      int centerX = this.width / 2 - buttonWidth / 2;
      int startY = this.height / 2 - 30;
      if (this.isSilentGhostReinvade) {
         this.addDrawableChild(ButtonWidget.builder(Text.translatable("smfs.deathScreen.reinvade"), button -> {
            if (this.client != null && this.client.player != null) {
               ClientModNetwork.sendToServer(new ReinvadeRespawnPacket());
               this.client.player.requestRespawn();
               this.client.setScreen(this);
            }
         }).dimensions(centerX, startY, buttonWidth, buttonHeight).build());
      } else {
         this.addDrawableChild(
            ButtonWidget.builder(
                  Text.translatable("smfs.deathScreen.respawn"),
                  button -> {
                     if (this.client != null && this.client.player != null) {
                        if (ModConfig.getInstance().instantReincarnation) {
                           ConfirmScreen confirmScreen = new ConfirmScreen(
                              confirmed -> {
                                 if (confirmed) {
                                    ClientModNetwork.sendToServer(new RequestRespawnPacket());
                                    this.client.player.requestRespawn();
                                    this.client.setScreen(this);
                                 } else {
                                    this.quitLevel();
                                 }
                              },
                              Text.translatable("smfs.deathScreen.respawn.warning.title"),
                              Text.translatable("smfs.deathScreen.respawn.warning.message"),
                              Text.translatable("smfs.deathScreen.respawn.warning.understand"),
                              Text.translatable("smfs.deathScreen.respawn.warning.quit")
                           );
                           this.client.setScreen(confirmScreen);
                        } else {
                           ClientModNetwork.sendToServer(new SpectateModePacket());
                           this.client.player.requestRespawn();
                        }
                     }
                  }
               )
               .dimensions(centerX, startY, buttonWidth, buttonHeight)
               .build()
         );
      }

      this.addDrawableChild(ButtonWidget.builder(Text.translatable("smfs.deathScreen.spectate"), button -> {
         if (this.client != null && this.client.player != null) {
            LOGGER.debug("玩家点击成为亡魂");
            ClientModNetwork.sendToServer(new SpectateModePacket());
            this.client.player.requestRespawn();
            LOGGER.debug("已发送包并触发 requestRespawn()，等待服务端确认包关闭界面");
         }
      }).dimensions(centerX, startY + buttonSpacing, buttonWidth, buttonHeight).build());
      this.addDrawableChild(
         ButtonWidget.builder(
               Text.translatable("smfs.deathScreen.quit"),
               button -> {
                  if (this.client != null) {
                     ConfirmScreen confirmScreen = new ConfirmScreen(
                        confirmed -> {
                           if (confirmed) {
                              this.quitLevel();
                           } else {
                              this.client.setScreen(this);
                           }
                        },
                        Text.translatable("smfs.deathScreen.quit.confirm"),
                        Text.translatable("smfs.deathScreen.quit.confirm.detail"),
                        Text.translatable("gui.yes"),
                        Text.translatable("gui.no")
                     );
                     this.client.setScreen(confirmScreen);
                  }
               }
            )
            .dimensions(centerX, startY + buttonSpacing * 2, buttonWidth, buttonHeight)
            .build()
      );
   }

   private void quitLevel() {
      if (this.client.world != null) {
         this.client.world.disconnect();
      }

      this.client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
      this.client.setScreen(new TitleScreen());
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context);
      if (this.showDeathMessage) {
         Text displayMessage;
         if (this.isSilentGhostReinvade) {
            displayMessage = Text.translatable("smfs.deathScreen.silentGhost.message");
         } else if (this.deathMessage != null) {
            displayMessage = this.deathMessage;
         } else {
            displayMessage = null;
         }

         if (displayMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, displayMessage, this.width / 2, this.height / 2 - 50, 16777215);
         }
      }

      context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 70, 16777215);
      super.render(context, mouseX, mouseY, delta);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean shouldPause() {
      return false;
   }

   private void handleRespawnConfirmation(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> {
         LOGGER.debug("收到立即复活确认包，关闭界面");
         if (client.currentScreen == this) {
            client.setScreen(null);
         }
      });
   }

   private boolean hasSilentGhostEquipped() {
      if (this.client != null && this.client.player != null) {
         for (int i = 0; i < 10; i++) {
            String ghostType = PlayerEvents.getGhostTypeInSlot(this.client.player, i);
            if ("silent_ghost".equals(ghostType)) {
               return true;
            }
         }
      }

      return false;
   }

   private void handleSpectateConfirmation(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> {
         LOGGER.debug("收到旁观者模式确认包，关闭界面");
         if (client.currentScreen instanceof CustomDeathScreen) {
            client.setScreen(null);
            LOGGER.debug("死亡界面已强制关闭");
         } else if (client.currentScreen != null) {
            LOGGER.warn("当前界面不是CustomDeathScreen实例，而是: {}", client.currentScreen.getClass().getName());
            client.setScreen(null);
            LOGGER.debug("已强制关闭当前界面");
         } else {
            LOGGER.debug("当前没有显示任何界面");
         }
      });
   }

   private void handleReinvadeConfirmation(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> {
         if (client.currentScreen instanceof CustomDeathScreen) {
            client.setScreen(null);
         }
      });
   }

   public void removed() {
      super.removed();
      LOGGER.debug("CustomDeathScreen被移除，取消注册网络包监听器");
      ClientPlayNetworking.unregisterGlobalReceiver(RESPAWN_CONFIRMATION);
      ClientPlayNetworking.unregisterGlobalReceiver(SPECTATE_CONFIRMATION);
      ClientPlayNetworking.unregisterGlobalReceiver(REINVADE_CONFIRMATION);
      LOGGER.debug("网络包监听器已取消注册");
   }
}
