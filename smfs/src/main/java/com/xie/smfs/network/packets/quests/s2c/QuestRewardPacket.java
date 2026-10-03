package com.xie.smfs.network.packets.quests.s2c;

import com.xie.smfs.util.GhostUtils;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestRewardPacket {
   public static final Identifier ID = new Identifier("smfs", "quest_reward");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/QuestRewardPacket");
   public final String itemId;
   public final int count;

   public QuestRewardPacket(String itemId, int count) {
      this.itemId = itemId;
      this.count = count;
   }

   public QuestRewardPacket(PacketByteBuf buf) {
      this.itemId = buf.readString();
      this.count = buf.readInt();
   }

   public void write(PacketByteBuf buf) {
      buf.writeString(this.itemId);
      buf.writeInt(this.count);
   }

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      String itemId = buf.readString();
      int count = buf.readInt();
      client.execute(() -> {
         if (client.player != null) {
            LOGGER.info("客户端收到奖励通知: {} x {}", itemId, count);
         }
      });
   }

   public static void handleServer(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      String itemId = buf.readString();
      int count = buf.readInt();
      server.execute(() -> {
         try {
            ItemStack rewardStack = GhostUtils.createItemStack(itemId, count);
            if (rewardStack == null || rewardStack.isEmpty()) {
               LOGGER.warn("无法识别的物品ID: {}", itemId);
               return;
            }

            if (!player.getInventory().insertStack(rewardStack)) {
               player.dropItem(rewardStack, false);
            }

            player.sendMessage(Text.literal("§a获得奖励: " + count + " 个 " + getItemDisplayName(itemId)), false);
         } catch (Exception e) {
            LOGGER.error("服务端发放奖励时发生错误: {}", e.getMessage());
            player.sendMessage(Text.literal("§c奖励发放失败"), false);
         }
      });
   }

   private static String getItemDisplayName(String itemId) {
      return GhostUtils.getItemDisplayName(itemId);
   }
}
