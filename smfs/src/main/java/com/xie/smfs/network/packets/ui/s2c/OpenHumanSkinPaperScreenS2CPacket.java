package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.screen.TutorialScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenHumanSkinPaperScreenS2CPacket {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/OpenHumanSkinPaperScreenS2CPacket");
   public static final Identifier ID = new Identifier("smfs", "open_human_skin_paper_screen");

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> {
         try {
            LOGGER.info("接收到打开人皮纸界面请求");
            if (client.field_1755 == null) {
               client.method_1507(new TutorialScreen());
               LOGGER.info("成功打开人皮纸界面");
            } else {
               LOGGER.warn("当前已有打开的界面，无法打开人皮纸界面");
            }
         } catch (Exception e) {
            LOGGER.error("打开人皮纸界面时发生错误: {}", e.getMessage());
         }
      });
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, OpenHumanSkinPaperScreenS2CPacket::handle);
   }

   public static PacketByteBuf create() {
      return PacketByteBufs.create();
   }
}
