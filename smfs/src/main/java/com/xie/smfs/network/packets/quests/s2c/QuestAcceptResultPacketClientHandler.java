package com.xie.smfs.network.packets.quests.s2c;

import com.xie.smfs.client.screen.QuestHandledScreen;
import com.xie.smfs.manager.QuestManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestAcceptResultPacketClientHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestAcceptResultPacketClientHandler");

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      String questId = buf.method_19772();
      boolean success = buf.readBoolean();
      NbtCompound updatedQuestData = buf.method_10798();
      client.execute(() -> {
         try {
            LOGGER.debug("客户端接收到任务接受结果: questId={}, success={}", questId, success);
            if (updatedQuestData != null && client.field_1724 != null) {
               QuestManager.updateClientQuestData(client.field_1724, updatedQuestData);
               LOGGER.debug("已更新客户端任务数据");
            }

            if (client.field_1755 instanceof QuestHandledScreen questScreen) {
               questScreen.refreshQuestData();
               LOGGER.debug("已刷新任务界面");
            } else {
               LOGGER.warn("当前未打开任务界面，无法处理接受结果");
            }
         } catch (Exception e) {
            LOGGER.error("客户端处理任务接受结果时发生错误: {}", e.getMessage());
         }
      });
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(QuestAcceptResultPacket.ID, QuestAcceptResultPacketClientHandler::handle);
   }
}
