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
      super(Text.method_43471(isHardcore ? "deathScreen.title.hardcore" : "deathScreen.title"));
      this.deathMessage = deathMessage;
      this.showDeathMessage = !isHardcore;
      this.isSilentGhostReinvade = isSilentGhostReinvade;
   }

   protected void method_25426() {
      super.method_25426();
      ClientPlayNetworking.registerGlobalReceiver(RESPAWN_CONFIRMATION, this::handleRespawnConfirmation);
      ClientPlayNetworking.registerGlobalReceiver(SPECTATE_CONFIRMATION, this::handleSpectateConfirmation);
      ClientPlayNetworking.registerGlobalReceiver(REINVADE_CONFIRMATION, this::handleReinvadeConfirmation);
      LOGGER.debug("网络包监听器注册完成");
      int buttonWidth = 200;
      int buttonHeight = 20;
      int buttonSpacing = 25;
      int centerX = this.field_22789 / 2 - buttonWidth / 2;
      int startY = this.field_22790 / 2 - 30;
      if (this.isSilentGhostReinvade) {
         this.method_37063(ButtonWidget.method_46430(Text.method_43471("smfs.deathScreen.reinvade"), button -> {
            if (this.field_22787 != null && this.field_22787.field_1724 != null) {
               ClientModNetwork.sendToServer(new ReinvadeRespawnPacket());
               this.field_22787.field_1724.method_7331();
               this.field_22787.method_1507(this);
            }
         }).method_46434(centerX, startY, buttonWidth, buttonHeight).method_46431());
      } else {
         this.method_37063(
            ButtonWidget.method_46430(
                  Text.method_43471("smfs.deathScreen.respawn"),
                  button -> {
                     if (this.field_22787 != null && this.field_22787.field_1724 != null) {
                        if (ModConfig.getInstance().instantReincarnation) {
                           ConfirmScreen confirmScreen = new ConfirmScreen(
                              confirmed -> {
                                 if (confirmed) {
                                    ClientModNetwork.sendToServer(new RequestRespawnPacket());
                                    this.field_22787.field_1724.method_7331();
                                    this.field_22787.method_1507(this);
                                 } else {
                                    this.quitLevel();
                                 }
                              },
                              Text.method_43471("smfs.deathScreen.respawn.warning.title"),
                              Text.method_43471("smfs.deathScreen.respawn.warning.message"),
                              Text.method_43471("smfs.deathScreen.respawn.warning.understand"),
                              Text.method_43471("smfs.deathScreen.respawn.warning.quit")
                           );
                           this.field_22787.method_1507(confirmScreen);
                        } else {
                           ClientModNetwork.sendToServer(new SpectateModePacket());
                           this.field_22787.field_1724.method_7331();
                        }
                     }
                  }
               )
               .method_46434(centerX, startY, buttonWidth, buttonHeight)
               .method_46431()
         );
      }

      this.method_37063(ButtonWidget.method_46430(Text.method_43471("smfs.deathScreen.spectate"), button -> {
         if (this.field_22787 != null && this.field_22787.field_1724 != null) {
            LOGGER.debug("玩家点击成为亡魂");
            ClientModNetwork.sendToServer(new SpectateModePacket());
            this.field_22787.field_1724.method_7331();
            LOGGER.debug("已发送包并触发 requestRespawn()，等待服务端确认包关闭界面");
         }
      }).method_46434(centerX, startY + buttonSpacing, buttonWidth, buttonHeight).method_46431());
      this.method_37063(
         ButtonWidget.method_46430(
               Text.method_43471("smfs.deathScreen.quit"),
               button -> {
                  if (this.field_22787 != null) {
                     ConfirmScreen confirmScreen = new ConfirmScreen(
                        confirmed -> {
                           if (confirmed) {
                              this.quitLevel();
                           } else {
                              this.field_22787.method_1507(this);
                           }
                        },
                        Text.method_43471("smfs.deathScreen.quit.confirm"),
                        Text.method_43471("smfs.deathScreen.quit.confirm.detail"),
                        Text.method_43471("gui.yes"),
                        Text.method_43471("gui.no")
                     );
                     this.field_22787.method_1507(confirmScreen);
                  }
               }
            )
            .method_46434(centerX, startY + buttonSpacing * 2, buttonWidth, buttonHeight)
            .method_46431()
      );
   }

   private void quitLevel() {
      if (this.field_22787.field_1687 != null) {
         this.field_22787.field_1687.method_8525();
      }

      this.field_22787.method_18096(new MessageScreen(Text.method_43471("menu.savingLevel")));
      this.field_22787.method_1507(new TitleScreen());
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.method_25420(context);
      if (this.showDeathMessage) {
         Text displayMessage;
         if (this.isSilentGhostReinvade) {
            displayMessage = Text.method_43471("smfs.deathScreen.silentGhost.message");
         } else if (this.deathMessage != null) {
            displayMessage = this.deathMessage;
         } else {
            displayMessage = null;
         }

         if (displayMessage != null) {
            context.method_27534(this.field_22793, displayMessage, this.field_22789 / 2, this.field_22790 / 2 - 50, 16777215);
         }
      }

      context.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, this.field_22790 / 2 - 70, 16777215);
      super.method_25394(context, mouseX, mouseY, delta);
   }

   public boolean method_25422() {
      return false;
   }

   public boolean method_25421() {
      return false;
   }

   private void handleRespawnConfirmation(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> {
         LOGGER.debug("收到立即复活确认包，关闭界面");
         if (client.field_1755 == this) {
            client.method_1507(null);
         }
      });
   }

   private boolean hasSilentGhostEquipped() {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         for (int i = 0; i < 10; i++) {
            String ghostType = PlayerEvents.getGhostTypeInSlot(this.field_22787.field_1724, i);
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
         if (client.field_1755 instanceof CustomDeathScreen) {
            client.method_1507(null);
            LOGGER.debug("死亡界面已强制关闭");
         } else if (client.field_1755 != null) {
            LOGGER.warn("当前界面不是CustomDeathScreen实例，而是: {}", client.field_1755.getClass().getName());
            client.method_1507(null);
            LOGGER.debug("已强制关闭当前界面");
         } else {
            LOGGER.debug("当前没有显示任何界面");
         }
      });
   }

   private void handleReinvadeConfirmation(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> {
         if (client.field_1755 instanceof CustomDeathScreen) {
            client.method_1507(null);
         }
      });
   }

   public void method_25432() {
      super.method_25432();
      LOGGER.debug("CustomDeathScreen被移除，取消注册网络包监听器");
      ClientPlayNetworking.unregisterGlobalReceiver(RESPAWN_CONFIRMATION);
      ClientPlayNetworking.unregisterGlobalReceiver(SPECTATE_CONFIRMATION);
      ClientPlayNetworking.unregisterGlobalReceiver(REINVADE_CONFIRMATION);
      LOGGER.debug("网络包监听器已取消注册");
   }
}
