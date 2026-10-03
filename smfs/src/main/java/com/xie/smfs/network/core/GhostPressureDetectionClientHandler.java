package com.xie.smfs.network.core;

import com.xie.smfs.client.renderer.GhostPullRenderer;
import com.xie.smfs.network.packets.common.s2c.GhostPressureDetectionPacket;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostPressureDetectionClientHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostPressureDetectionClientHandler.class);

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(GhostPressureDetectionPacket.PACKET_ID, (client, handler, buf, responseSender) -> {
         UUID entityId = buf.method_10790();
         boolean hasGhostPressure = buf.readBoolean();
         client.execute(() -> {
            GhostPullRenderer.updateServerDetectionResult(entityId, hasGhostPressure);
            if (hasGhostPressure) {
               LOGGER.debug("客户端收到服务端检测结果: 实体 {} 有鬼压人buff", entityId);
            } else {
               LOGGER.debug("客户端收到服务端检测结果: 实体 {} 没有鬼压人buff", entityId);
            }
         });
      });
   }
}
