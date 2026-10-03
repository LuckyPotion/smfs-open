package com.xie.smfs.network.packets.ui.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostDeadlockSelectC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_deadlock_select");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostDeadlockSelectC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, GhostDeadlockSelectC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      int slotIndex = buf.readInt();
      server.execute(() -> {
         if (slotIndex >= 0 && slotIndex < 10) {
            NbtCompound ghostSlots = PlayerEvents.getGhostSlots(player);
            String slotKey = "Slot" + slotIndex;
            if (ghostSlots.method_10545(slotKey)) {
               NbtCompound slotData = ghostSlots.method_10562(slotKey);
               if (slotData.method_10577("occupied") && !slotData.method_10577("slotDeadlocked")) {
                  PlayerEvents.setSlotDeadlocked(player, slotIndex, true);
                  player.method_7353(Text.method_43469("effect.smfs.ghost_deadlock.success", new Object[]{slotIndex}), true);
                  return;
               }
            }

            player.method_7353(Text.method_43471("effect.smfs.ghost_deadlock.no_ghost"), true);
         } else {
            applyDeadlockRandom(player);
         }
      });
   }

   private static void applyDeadlockRandom(ServerPlayerEntity player) {
      NbtCompound ghostSlots = PlayerEvents.getGhostSlots(player);
      List<Integer> availableSlots = new ArrayList<>();

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.method_10545(slotKey)) {
            NbtCompound slotData = ghostSlots.method_10562(slotKey);
            if (slotData.method_10577("occupied") && !slotData.method_10577("slotDeadlocked")) {
               availableSlots.add(i);
            }
         }
      }

      if (availableSlots.isEmpty()) {
         player.method_7353(Text.method_43471("effect.smfs.ghost_deadlock.no_ghost"), true);
      } else {
         Random random = new Random();
         int targetSlot = availableSlots.get(random.nextInt(availableSlots.size()));
         PlayerEvents.setSlotDeadlocked(player, targetSlot, true);
         player.method_7353(Text.method_43469("effect.smfs.ghost_deadlock.success", new Object[]{targetSlot}), true);
      }
   }
}
