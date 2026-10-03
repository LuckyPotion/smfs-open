package com.xie.smfs.network.packets.common.s2c;

import com.xie.smfs.entity.ghost.GhostMerchantEntity;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncGhostMerchantOffersS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "sync_ghost_merchant_offers");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/SyncGhostMerchantOffersS2CPacket");

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, SyncGhostMerchantOffersS2CPacket::receive);
   }

   public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      NbtCompound data = buf.readNbt();
      client.execute(() -> {
         if (client.player != null && data != null) {
            LOGGER.info("收到鬼商人交易列表同步数据包，数据: {}", data);
            if (data.contains("Offers")) {
               NbtList offersNbt = data.getList("Offers", 10);
               TradeOfferList offers = new TradeOfferList();
               LOGGER.info("解析到 {} 个交易项目", offersNbt.size());

               for (int i = 0; i < offersNbt.size(); i++) {
                  NbtCompound offerNbt = offersNbt.getCompound(i);
                  TradeOffer offer = new TradeOffer(offerNbt);
                  offers.add(offer);
                  LOGGER.info("解析交易项目 {}: {}", i, offer);
               }

               updateClientMerchantOffers(data.getUuid("MerchantId"), offers);
               LOGGER.info("已同步鬼商人交易列表到客户端，共 {} 个交易", offers.size());
            } else {
               LOGGER.warn("数据包中未找到Offers字段");
            }
         } else {
            LOGGER.warn("客户端玩家为空或数据包数据为空");
         }
      });
   }

   private static void updateClientMerchantOffers(UUID merchantId, TradeOfferList offers) {
      ClientWorld world = MinecraftClient.getInstance().world;
      LOGGER.info("开始更新客户端商人交易列表，商人ID: {}, 交易数量: {}", merchantId, offers.size());
      if (world != null && merchantId != null) {
         LOGGER.info("客户端世界存在，开始查找实体");
         Entity entity = null;
         int entityCount = 0;

         try {
            for (Entity e : world.getEntities()) {
               entityCount++;
               if (merchantId.equals(e.getUuid())) {
                  entity = e;
                  LOGGER.info("找到匹配的实体: {} (类型: {})", e.getUuid(), e.getClass().getSimpleName());
                  break;
               }
            }

            LOGGER.info("遍历了 {} 个实体，找到匹配实体: {}", entityCount, entity != null);
         } catch (Exception e) {
            LOGGER.warn("查找实体时出错: {}", e.getMessage());
         }

         if (entity instanceof GhostMerchantEntity ghostMerchant) {
            LOGGER.info("实体是GhostMerchantEntity类型，开始更新交易列表");
            ghostMerchant.setOffersFromServer(offers);
            LOGGER.info("已更新鬼商人 {} 的交易列表，共 {} 个交易", merchantId, offers.size());
            refreshMerchantScreenIfOpen(ghostMerchant);
         } else {
            LOGGER.warn("未找到对应的鬼商人实体: {} (找到的实体类型: {})", merchantId, entity != null ? entity.getClass().getSimpleName() : "null");
         }
      } else {
         LOGGER.warn("客户端世界为空或商人ID为空: world={}, merchantId={}", world != null, merchantId != null);
      }
   }

   private static void refreshMerchantScreenIfOpen(GhostMerchantEntity merchant) {
      LOGGER.info("跳过客户端强制刷新：交易界面由原生数据包驱动更新。");
   }
}
