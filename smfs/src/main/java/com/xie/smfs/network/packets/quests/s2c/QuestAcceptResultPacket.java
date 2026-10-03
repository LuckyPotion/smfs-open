package com.xie.smfs.network.packets.quests.s2c;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestAcceptResultPacket {
   public static final Identifier ID = new Identifier("smfs", "quest_accept_result");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestAcceptResultPacket");
   private final String questId;
   private final boolean success;
   private final NbtCompound updatedQuestData;

   public QuestAcceptResultPacket(String questId, boolean success, NbtCompound updatedQuestData) {
      this.questId = questId;
      this.success = success;
      this.updatedQuestData = updatedQuestData;
   }

   public QuestAcceptResultPacket(PacketByteBuf buf) {
      this.questId = buf.readString();
      this.success = buf.readBoolean();
      this.updatedQuestData = buf.readNbt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.questId);
      buf.writeBoolean(this.success);
      buf.writeNbt(this.updatedQuestData);
   }
}
