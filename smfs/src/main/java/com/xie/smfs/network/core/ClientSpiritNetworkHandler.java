package com.xie.smfs.network.core;

import com.xie.smfs.client.screen.QuestHandledScreen;
import com.xie.smfs.client.util.ClientGhostUtils;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.network.packets.common.s2c.SyncGhostMerchantOffersS2CPacket;
import com.xie.smfs.network.packets.skills.s2c.GiantShadowGhostScaleS2CPacket;
import com.xie.smfs.network.packets.skills.s2c.GiantShadowGhostStopScaleS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSpiritNetworkHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientSpiritNetworkHandler");

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(SpiritNetworkHandler.SYNC_SPIRIT_DATA, ClientSpiritNetworkHandler::handleSyncSpiritData);
      SyncGhostMerchantOffersS2CPacket.register();
      ClientPlayNetworking.registerGlobalReceiver(GiantShadowGhostScaleS2CPacket.ID, (client, handler, buf, responseSender) -> client.execute(() -> {
         if (client.player != null) {
            ClientGhostUtils.setGiantShadowGhostScaling(true);
         }
      }));
      ClientPlayNetworking.registerGlobalReceiver(GiantShadowGhostStopScaleS2CPacket.ID, (client, handler, buf, responseSender) -> client.execute(() -> {
         if (client.player != null) {
            ClientGhostUtils.setGiantShadowGhostScaling(false);
         }
      }));
   }

   private static void handleSyncSpiritData(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
      NbtCompound spiritData = buf.readNbt();
      client.execute(() -> {
         if (client.player != null) {
            NbtCompound oldData = PlayerEvents.getCachedData(client.player);
            NbtCompound oldQuestData = oldData.contains("questData") ? oldData.getCompound("questData") : new NbtCompound();
            PlayerEvents.PLAYER_DATA_CACHE.put(client.player.getUuid(), spiritData.copy());
            PlayerEvents.syncAttributesFromNbt(client.player, spiritData);
            NbtCompound newQuestData = spiritData.contains("questData") ? spiritData.getCompound("questData") : new NbtCompound();
            if (!oldQuestData.equals(newQuestData)) {
               LOGGER.info("检测到任务数据变化，刷新任务界面");
               if (client.currentScreen instanceof QuestHandledScreen questScreen) {
                  questScreen.refreshQuestData();
               }

               client.player.sendMessage(Text.literal("§a任务数据已同步更新").formatted(Formatting.GREEN), true);
            }
         }
      });
   }
}
