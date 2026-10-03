package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.screen.GhostHunterTalkScreen;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class OpenGhostHunterTalkScreenS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "open_ghost_hunter_talk_screen");
   private final int entityId;
   private final List<String> dialogues;
   private final List<String> storyDialogues;
   private final boolean alreadyTalkedToYangXiao;

   public OpenGhostHunterTalkScreenS2CPacket(int entityId, List<String> dialogues) {
      this(entityId, dialogues, null, false);
   }

   public OpenGhostHunterTalkScreenS2CPacket(int entityId, List<String> dialogues, boolean alreadyTalkedToYangXiao) {
      this(entityId, dialogues, null, alreadyTalkedToYangXiao);
   }

   public OpenGhostHunterTalkScreenS2CPacket(int entityId, List<String> dialogues, List<String> storyDialogues, boolean alreadyTalkedToYangXiao) {
      this.entityId = entityId;
      this.dialogues = dialogues != null ? dialogues : new ArrayList<>();
      this.storyDialogues = storyDialogues != null ? storyDialogues : new ArrayList<>();
      this.alreadyTalkedToYangXiao = alreadyTalkedToYangXiao;
   }

   public OpenGhostHunterTalkScreenS2CPacket(PacketByteBuf buf) {
      this.entityId = buf.readInt();
      int size = buf.readInt();
      this.dialogues = new ArrayList<>();

      for (int i = 0; i < size; i++) {
         this.dialogues.add(buf.readString());
      }

      int storySize = buf.readInt();
      this.storyDialogues = new ArrayList<>();

      for (int i = 0; i < storySize; i++) {
         this.storyDialogues.add(buf.readString());
      }

      this.alreadyTalkedToYangXiao = buf.readBoolean();
   }

   public void write(PacketByteBuf buf) {
      buf.writeInt(this.entityId);
      buf.writeInt(this.dialogues.size());

      for (String dialogue : this.dialogues) {
         buf.writeString(dialogue);
      }

      buf.writeInt(this.storyDialogues.size());

      for (String dialogue : this.storyDialogues) {
         buf.writeString(dialogue);
      }

      buf.writeBoolean(this.alreadyTalkedToYangXiao);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public List<String> getDialogues() {
      return this.dialogues;
   }

   public List<String> getStoryDialogues() {
      return this.storyDialogues;
   }

   public boolean isAlreadyTalkedToYangXiao() {
      return this.alreadyTalkedToYangXiao;
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         OpenGhostHunterTalkScreenS2CPacket packet = new OpenGhostHunterTalkScreenS2CPacket(buf);
         client.execute(() -> {
            if (client.world != null && client.currentScreen == null) {
               Entity entity = client.world.getEntityById(packet.getEntityId());
               if (entity != null) {
                  client.setScreen(new GhostHunterTalkScreen(entity, packet.getDialogues(), packet.getStoryDialogues(), packet.isAlreadyTalkedToYangXiao()));
               }
            }
         });
      });
   }
}
