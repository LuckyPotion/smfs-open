package com.xie.smfs.client.network;

import com.xie.smfs.client.screen.CustomDeathScreen;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowCustomDeathScreenPacketHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(ShowCustomDeathScreenPacketHandler.class);

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      Text deathMessage = buf.readText();
      boolean isSilentGhostReinvade = buf.readBoolean();
      client.execute(() -> {
         if (client.currentScreen == null) {
            client.setScreen(new CustomDeathScreen(deathMessage, true, isSilentGhostReinvade));
            LOGGER.info("显示自定义死亡界面，死亡消息: {}, 静悄悄重新入侵: {}", deathMessage.getString(), isSilentGhostReinvade);
         } else {
            LOGGER.warn("当前已有界面显示，无法显示死亡界面");
         }
      });
   }
}
