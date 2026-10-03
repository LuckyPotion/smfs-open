package com.xie.smfs.network.packets.quests.s2c;

import com.xie.smfs.client.screen.QuestHandledScreen;
import com.xie.smfs.manager.QuestManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestDetectionResultPacket {
   public static final Identifier ID = new Identifier("smfs", "quest_detection_result");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestDetectionResultPacket");
   private final String questId;
   private final String objectiveId;
   private final boolean success;
   private final NbtCompound updatedQuestData;

   public QuestDetectionResultPacket(String questId, String objectiveId, boolean success, NbtCompound updatedQuestData) {
      this.questId = questId;
      this.objectiveId = objectiveId;
      this.success = success;
      this.updatedQuestData = updatedQuestData;
   }

   public QuestDetectionResultPacket(PacketByteBuf buf) {
      this.questId = buf.method_19772();
      this.objectiveId = buf.method_19772();
      this.success = buf.readBoolean();
      this.updatedQuestData = buf.method_10798();
   }

   public void write(PacketByteBuf buf) {
      buf.method_10814(this.questId);
      buf.method_10814(this.objectiveId);
      buf.writeBoolean(this.success);
      buf.method_10794(this.updatedQuestData);
   }

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      String questId = buf.method_19772();
      String objectiveId = buf.method_19772();
      boolean success = buf.readBoolean();
      NbtCompound updatedQuestData = buf.method_10798();
      client.execute(() -> {
         try {
            LOGGER.debug("客户端接收到任务检测结果: questId={}, objectiveId={}, success={}", questId, objectiveId, success);
            if (updatedQuestData != null) {
               QuestManager.updateClientQuestData(updatedQuestData);
               LOGGER.debug("已更新客户端任务数据");
            }

            if (client.field_1755 instanceof QuestHandledScreen questScreen) {
               questScreen.handleDetectionResult(questId, objectiveId, success);
            } else {
               LOGGER.warn("当前未打开任务界面，无法处理检测结果");
            }
         } catch (Exception e) {
            LOGGER.error("客户端处理任务检测结果时发生错误: {}", e.getMessage());
         }
      });
   }
}
