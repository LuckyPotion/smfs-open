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
      this.itemId = buf.method_19772();
      this.count = buf.readInt();
   }

   public void write(PacketByteBuf buf) {
      buf.method_10814(this.itemId);
      buf.writeInt(this.count);
   }

   public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      String itemId = buf.method_19772();
      int count = buf.readInt();
      client.execute(() -> {
         if (client.field_1724 != null) {
            LOGGER.info("客户端收到奖励通知: {} x {}", itemId, count);
         }
      });
   }

   public static void handleServer(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      String itemId = buf.method_19772();
      int count = buf.readInt();
      server.execute(() -> {
         try {
            ItemStack rewardStack = GhostUtils.createItemStack(itemId, count);
            if (rewardStack == null || rewardStack.method_7960()) {
               LOGGER.warn("无法识别的物品ID: {}", itemId);
               return;
            }

            if (!player.method_31548().method_7394(rewardStack)) {
               player.method_7328(rewardStack, false);
            }

            player.method_7353(Text.method_43470("§a获得奖励: " + count + " 个 " + getItemDisplayName(itemId)), false);
         } catch (Exception e) {
            LOGGER.error("服务端发放奖励时发生错误: {}", e.getMessage());
            player.method_7353(Text.method_43470("§c奖励发放失败"), false);
         }
      });
   }

   private static String getItemDisplayName(String itemId) {
      return GhostUtils.getItemDisplayName(itemId);
   }
}
