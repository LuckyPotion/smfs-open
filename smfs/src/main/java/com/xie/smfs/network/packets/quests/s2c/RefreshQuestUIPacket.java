package com.xie.smfs.network.packets.quests.s2c;

import com.xie.smfs.client.screen.QuestHandledScreen;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshQuestUIPacket {
   public static final Identifier PACKET_ID = new Identifier("smfs", "refresh_quest_ui");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/RefreshQuestUIPacket");

   public static void encode(RefreshQuestUIPacket packet, PacketByteBuf buf) {
   }

   public static RefreshQuestUIPacket decode(PacketByteBuf buf) {
      return new RefreshQuestUIPacket();
   }

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      client.execute(() -> {
         try {
            if (client.field_1755 instanceof QuestHandledScreen screen) {
               screen.refreshUI();
            }

            LOGGER.debug("接收到任务界面刷新通知，已刷新界面");
         } catch (Exception e) {
            LOGGER.error("处理任务界面刷新包时出错", e);
         }
      });
   }

   public PacketByteBuf toPacket() {
      PacketByteBuf buf = PacketByteBufs.create();
      encode(this, buf);
      return buf;
   }

   public void write(PacketByteBuf buf) {
      encode(this, buf);
   }

   public static void sendToClient(ServerPlayerEntity player) {
      RefreshQuestUIPacket packet = new RefreshQuestUIPacket();
      ServerPlayNetworking.send(player, PACKET_ID, packet.toPacket());
      LOGGER.debug("已发送任务界面刷新包到玩家: {}", player.method_5477().getString());
   }
}
