package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.screen.HumanSkinPaperEndingOverlay;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class OpenHumanSkinPaperEndingS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "open_human_skin_paper_ending");

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> client.setScreen(new HumanSkinPaperEndingOverlay()));
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, OpenHumanSkinPaperEndingS2CPacket::handle);
   }

   public static PacketByteBuf create() {
      return PacketByteBufs.create();
   }
}
