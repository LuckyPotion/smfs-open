package com.xie.smfs.network.packets.quests.c2s;

import com.xie.smfs.api.IPlayerData;
import com.xie.smfs.manager.QuestManager;
import com.xie.smfs.network.ModNetwork;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestDetectionPacket {
   public static final Identifier ID = new Identifier("smfs", "quest_detection");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestDetectionPacket");
   private final String questId;
   private final String objectiveId;
   private final String strategyType;
   private final int amount;
   private final boolean consume;

   public QuestDetectionPacket(String questId, String objectiveId, String strategyType, int amount, boolean consume) {
      this.questId = questId;
      this.objectiveId = objectiveId;
      this.strategyType = strategyType;
      this.amount = amount;
      this.consume = consume;
   }

   public QuestDetectionPacket(PacketByteBuf buf) {
      this.questId = buf.method_19772();
      this.objectiveId = buf.method_19772();
      this.strategyType = buf.method_19772();
      this.amount = buf.readInt();
      this.consume = buf.readBoolean();
   }

   public void write(PacketByteBuf buf) {
      buf.method_10814(this.questId);
      buf.method_10814(this.objectiveId);
      buf.method_10814(this.strategyType);
      buf.writeInt(this.amount);
      buf.writeBoolean(this.consume);
   }

   public static void handleServer(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      String questId = buf.method_19772();
      String objectiveId = buf.method_19772();
      String strategyType = buf.method_19772();
      int amount = buf.readInt();
      boolean consume = buf.readBoolean();
      server.execute(() -> {
         try {
            boolean success = QuestManager.checkItemSubmission(player, questId, objectiveId, amount, consume);
            IPlayerData playerData = (IPlayerData)player;
            NbtCompound updatedQuestData = playerData.getQuestData();
            ModNetwork.sendQuestDetectionResultToClient(questId, objectiveId, success, updatedQuestData, player);
         } catch (Exception e) {
            LOGGER.error("服务端处理任务检测时发生错误: {}", e.getMessage());
            IPlayerData playerData = (IPlayerData)player;
            NbtCompound currentQuestData = playerData.getQuestData();
            ModNetwork.sendQuestDetectionResultToClient(questId, objectiveId, false, currentQuestData, player);
         }
      });
   }
}
