package com.xie.smfs.network;

import com.xie.smfs.client.network.ShowCustomDeathScreenPacketHandler;
import com.xie.smfs.network.packets.common.c2s.ReinvadeRespawnPacket;
import com.xie.smfs.network.packets.common.c2s.RequestRespawnPacket;
import com.xie.smfs.network.packets.common.c2s.SpearBindC2SPacket;
import com.xie.smfs.network.packets.common.c2s.SpearModeConfigC2SPacket;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import com.xie.smfs.network.packets.common.c2s.SwitchToSurvivalPacket;
import com.xie.smfs.network.packets.common.s2c.GhostDreamTimeS2CPacket;
import com.xie.smfs.network.packets.common.s2c.StructureCoordinatesS2CPacket;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildRecallPacket;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildSummonPacket;
import com.xie.smfs.network.packets.ghostchild.s2c.GhostChildFeedSuccessPacket;
import com.xie.smfs.network.packets.ghostchild.s2c.GhostChildFusionBeginS2CPacket;
import com.xie.smfs.network.packets.goodseller.s2c.GoodsSellerKillScreenS2CPacket;
import com.xie.smfs.network.packets.quests.c2s.QuestAcceptPacket;
import com.xie.smfs.network.packets.quests.c2s.QuestDetectionPacket;
import com.xie.smfs.network.packets.quests.s2c.QuestAcceptResultPacketClientHandler;
import com.xie.smfs.network.packets.quests.s2c.QuestDetectionResultPacket;
import com.xie.smfs.network.packets.quests.s2c.QuestRewardPacket;
import com.xie.smfs.network.packets.quests.s2c.RefreshQuestUIPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainFireIgniteC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.RequestBoneTreeCoordinatesC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.RequestStructureCoordinatesC2SPacket;
import com.xie.smfs.network.packets.skills.s2c.BoxGhostContainerPositionsS2CPacket;
import com.xie.smfs.network.packets.skills.s2c.MineralGhostMineralPositionsS2CPacket;
import com.xie.smfs.network.packets.skills.s2c.SyncGhostOfficerQuotaS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.OpenCodenameInputS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.OpenGhostDeadlockSelectS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.OpenGhostHunterTalkScreenS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.OpenHumanSkinPaperEndingS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.OpenHumanSkinPaperScreenS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.ResetPlayNoticeS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.ShowCustomDeathScreenPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientModNetwork {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientModNetwork");

   public static void register() {
      LOGGER.info("注册客户端专用网络处理器");
      ClientPlayNetworking.registerGlobalReceiver(QuestRewardPacket.ID, QuestRewardPacket::handle);
      ClientPlayNetworking.registerGlobalReceiver(ShowCustomDeathScreenPacket.ID, ShowCustomDeathScreenPacketHandler::handle);
      ClientPlayNetworking.registerGlobalReceiver(QuestDetectionResultPacket.ID, QuestDetectionResultPacket::handle);
      ClientPlayNetworking.registerGlobalReceiver(RefreshQuestUIPacket.PACKET_ID, RefreshQuestUIPacket::handle);
      ClientPlayNetworking.registerGlobalReceiver(StructureCoordinatesS2CPacket.ID, StructureCoordinatesS2CPacket::handle);
      QuestAcceptResultPacketClientHandler.register();
      OpenHumanSkinPaperScreenS2CPacket.register();
      OpenHumanSkinPaperEndingS2CPacket.register();
      BoxGhostContainerPositionsS2CPacket.register();
      MineralGhostMineralPositionsS2CPacket.register();
      SyncGhostOfficerQuotaS2CPacket.register();
      ClientPlayNetworking.registerGlobalReceiver(GhostChildFeedSuccessPacket.ID, (client, handler, buf, responseSender) -> {
         GhostChildFeedSuccessPacket packet = GhostChildFeedSuccessPacket.read(buf);
         client.execute(() -> {
            if (client.field_1724 != null) {
               client.field_1724.method_5783(SoundEvents.field_14709, 1.0F, 1.0F);
            }
         });
      });
      ResetPlayNoticeS2CPacket.register();
      OpenGhostHunterTalkScreenS2CPacket.register();
      OpenCodenameInputS2CPacket.register();
      OpenGhostDeadlockSelectS2CPacket.register();
      ClientPlayNetworking.registerGlobalReceiver(new Identifier("smfs", "ghost_dream_time"), (client, handler, buf, responseSender) -> {
         GhostDreamTimeS2CPacket packet = new GhostDreamTimeS2CPacket(buf);
         client.execute(() -> packet.apply(handler));
      });
      SpearBindC2SPacket.registerClient();
      SpearModeConfigC2SPacket.registerClient();
      GhostChildFusionBeginS2CPacket.register();
      GoodsSellerKillScreenS2CPacket.register();
   }

   public static void sendToServer(QuestAcceptPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(QuestAcceptPacket.PACKET_ID, buf);
   }

   public static void sendToServer(QuestDetectionPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(QuestDetectionPacket.ID, buf);
   }

   public static void sendToServer(RequestBoneTreeCoordinatesC2SPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(RequestBoneTreeCoordinatesC2SPacket.ID, buf);
   }

   public static void sendToServer(RequestStructureCoordinatesC2SPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(RequestStructureCoordinatesC2SPacket.ID, buf);
   }

   public static void sendToServer(ClientGhostDomainFireIgniteC2SPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(ClientGhostDomainFireIgniteC2SPacket.ID, buf);
   }

   public static void sendToServer(SwitchToSurvivalPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(SwitchToSurvivalPacket.ID, buf);
   }

   public static void sendToServer(RequestRespawnPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(RequestRespawnPacket.ID, buf);
   }

   public static void sendToServer(SpectateModePacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      ClientPlayNetworking.send(SpectateModePacket.ID, buf);
   }

   public static void sendToServer(ReinvadeRespawnPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(ReinvadeRespawnPacket.ID, buf);
   }

   public static void sendToServer(GhostChildSummonPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(GhostChildSummonPacket.ID, buf);
   }

   public static void sendToServer(GhostChildRecallPacket packet) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ClientPlayNetworking.send(GhostChildRecallPacket.ID, buf);
   }
}
