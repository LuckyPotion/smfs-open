package com.xie.smfs.network.packets.quests.c2s;

import com.xie.smfs.manager.DailyQuestManager;
import com.xie.smfs.manager.QuestManager;
import com.xie.smfs.network.ModNetwork;
import com.xie.smfs.network.packets.quests.s2c.RefreshQuestUIPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestAcceptPacket {
   public static final Identifier PACKET_ID = new Identifier("smfs", "quest_accept");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestAcceptPacket");
   private final String questId;
   private final String questType;

   public QuestAcceptPacket(String questId, String questType) {
      this.questId = questId;
      this.questType = questType;
   }

   public String getQuestId() {
      return this.questId;
   }

   public String getQuestType() {
      return this.questType;
   }

   public static void encode(QuestAcceptPacket packet, PacketByteBuf buf) {
      buf.writeString(packet.questId);
      buf.writeString(packet.questType);
   }

   public static QuestAcceptPacket decode(PacketByteBuf buf) {
      String questId = buf.readString();
      String questType = buf.readString();
      return new QuestAcceptPacket(questId, questType);
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      QuestAcceptPacket packet = decode(buf);
      server.execute(() -> {
         try {
            switch (packet.questType) {
               case "daily":
                  handleDailyQuest(player, packet.questId);
                  break;
               case "event":
                  handleEventQuest(player, packet.questId);
                  break;
               default:
                  handleNormalQuest(player, packet.questId);
            }

            player.sendMessage(Text.literal("§a任务接受成功！"), false);
            NbtCompound updatedQuestData = QuestManager.getQuestData(player);
            ModNetwork.sendQuestAcceptResultToClient(packet.questId, true, updatedQuestData, player);
            RefreshQuestUIPacket.sendToClient(player);
         } catch (Exception e) {
            player.sendMessage(Text.literal("§c任务接受失败：" + e.getMessage()), false);
            LOGGER.error("处理任务接受包时出错", e);
         }
      });
   }

   private static void handleDailyQuest(ServerPlayerEntity player, String questId) {
      DailyQuestManager.acceptDailyQuest(player, questId);
   }

   private static void handleEventQuest(ServerPlayerEntity player, String questId) {
      QuestManager.assignQuest(player, questId);
   }

   private static void handleNormalQuest(ServerPlayerEntity player, String questId) {
      QuestManager.assignQuest(player, questId);
   }

   public PacketByteBuf toPacket() {
      PacketByteBuf buf = PacketByteBufs.create();
      encode(this, buf);
      return buf;
   }

   public void write(PacketByteBuf buf) {
      encode(this, buf);
   }
}
