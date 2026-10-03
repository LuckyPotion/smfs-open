package com.xie.smfs.network;

import com.xie.smfs.config.ModConfig;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.event.screen.GhostChildCultivationScreenHandler;
import com.xie.smfs.event.screen.GhostChildFeedScreenHandler;
import com.xie.smfs.event.screen.GhostControlScreenHandler;
import com.xie.smfs.event.screen.GhostTamingScreenHandler;
import com.xie.smfs.event.screen.QuestScreenHandler;
import com.xie.smfs.event.screen.RoyalCurseScreenHandler;
import com.xie.smfs.network.packets.common.c2s.ReinvadeRespawnPacket;
import com.xie.smfs.network.packets.common.c2s.RequestRespawnPacket;
import com.xie.smfs.network.packets.common.c2s.SpearBindC2SPacket;
import com.xie.smfs.network.packets.common.c2s.SpearModeConfigC2SPacket;
import com.xie.smfs.network.packets.common.c2s.SpearRemoteAttackC2SPacket;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import com.xie.smfs.network.packets.common.c2s.SwitchToSurvivalPacket;
import com.xie.smfs.network.packets.common.s2c.GhostDreamTimeS2CPacket;
import com.xie.smfs.network.packets.common.s2c.GhostPressureDetectionPacket;
import com.xie.smfs.network.packets.config.c2s.ConfigSyncC2SPacket;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildFusionC2SPacket;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildRecallPacket;
import com.xie.smfs.network.packets.ghostchild.c2s.GhostChildSummonPacket;
import com.xie.smfs.network.packets.goodseller.c2s.GoodsSellerKillC2SPacket;
import com.xie.smfs.network.packets.quests.c2s.QuestAcceptPacket;
import com.xie.smfs.network.packets.quests.c2s.QuestDetectionPacket;
import com.xie.smfs.network.packets.quests.s2c.QuestAcceptResultPacket;
import com.xie.smfs.network.packets.quests.s2c.QuestDetectionResultPacket;
import com.xie.smfs.network.packets.quests.s2c.QuestRewardPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientBlockGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientClothesGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientCropGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientCryingGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFogGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFoodGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientFuneralMusicGhostVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostBloodSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainFireIgniteAllC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainFireIgniteBlocksC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainFireIgniteC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDreamSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDreamVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostFireJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostLakeVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostPressureSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostWindSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGraveEarthGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientPuppetGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientSneakGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientStepGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientSuonaGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientTrashGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWaterGhostSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostGSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostJSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostNSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientWishGhostVSkillC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.RequestBoneTreeCoordinatesC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.RequestStructureCoordinatesC2SPacket;
import com.xie.smfs.network.packets.ui.c2s.HumanSkinPaperContinueC2SPacket;
import com.xie.smfs.network.packets.ui.c2s.RequestJoinFactionC2SPacket;
import com.xie.smfs.network.packets.ui.c2s.RequestTradeScreenC2SPacket;
import com.xie.smfs.network.packets.ui.c2s.SubmitCodenameC2SPacket;
import com.xie.smfs.network.packets.ui.s2c.ShowCustomDeathScreenPacket;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModNetwork {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ModNetwork");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(
         RequestRespawnPacket.ID, (server, player, handler, buf, responseSender) -> RequestRespawnPacket.handle(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ReinvadeRespawnPacket.ID, (server, player, handler, buf, responseSender) -> ReinvadeRespawnPacket.handle(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         SpectateModePacket.ID, (server, player, handler, buf, responseSender) -> SpectateModePacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         QuestRewardPacket.ID, (server, player, handler, buf, responseSender) -> QuestRewardPacket.handleServer(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         QuestDetectionPacket.ID,
         (server, player, handler, buf, responseSender) -> QuestDetectionPacket.handleServer(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         QuestAcceptPacket.PACKET_ID, (server, player, handler, buf, responseSender) -> QuestAcceptPacket.handle(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostDomainFireIgniteC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostDomainFireIgniteC2SPacket.handle(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         SwitchToSurvivalPacket.ID,
         (server, player, handler, buf, responseSender) -> SwitchToSurvivalPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientFogGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientFogGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientBlockGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientBlockGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientFoodGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientFoodGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientCropGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientCropGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientStepGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientStepGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGraveEarthGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGraveEarthGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientTrashGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientTrashGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientWaterGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientWaterGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostFireJSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostFireJSkillC2SPacket.handle(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostDomainFireIgniteBlocksC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostDomainFireIgniteBlocksC2SPacket.handle(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostDomainFireIgniteAllC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostDomainFireIgniteAllC2SPacket.handle(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         RequestBoneTreeCoordinatesC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> RequestBoneTreeCoordinatesC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         RequestStructureCoordinatesC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> RequestStructureCoordinatesC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientCryingGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientCryingGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientSuonaGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientSuonaGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostPressureSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostPressureSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostWindSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostWindSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostBloodSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostBloodSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientWishGhostNSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientWishGhostNSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientWishGhostGSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientWishGhostGSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientWishGhostVSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientWishGhostVSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientWishGhostJSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientWishGhostJSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(GhostChildFeedScreenHandler.FEED_BUTTON_CLICK_PACKET_ID, (server, player, handler, buf, responseSender) -> {
         int syncId = buf.readInt();
         LOGGER.debug("接收到鬼童喂食界面按钮点击包 - 玩家: {}, 同步ID: {}", player != null ? player.getName().getString() : "null", syncId);
         server.execute(() -> {
            LOGGER.debug("开始处理鬼童喂食界面按钮点击包 - 玩家: {}, 同步ID: {}", player != null ? player.getName().getString() : "null", syncId);
            ScreenHandler currentHandler = player.currentScreenHandler;
            if (currentHandler != null) {
               LOGGER.debug("玩家当前界面类型: {}", currentHandler.getClass().getName());

               try {
                  if (currentHandler instanceof GhostChildFeedScreenHandler feedHandler) {
                     feedHandler.feedGhostChild();
                     LOGGER.debug("鬼童喂食操作执行完成 - 玩家: {}", player.getName().getString());
                  } else {
                     LOGGER.warn("忽略喂食请求：当前未打开鬼童喂食界面");
                  }
               } catch (Exception e) {
                  LOGGER.error("执行鬼童喂食操作时出错: {}", e.getMessage());
                  e.printStackTrace();
               }
            } else {
               LOGGER.warn("玩家当前没有打开任何界面 - 玩家: {}", player.getName().getString());
            }
         });
      });
      ServerPlayNetworking.registerGlobalReceiver(GhostTamingScreenHandler.BUTTON_CLICK_PACKET_ID, (server, player, handler, buf, responseSender) -> {
         int buttonId = buf.readInt();
         int syncId = buf.readInt();
         LOGGER.debug("接收到驭鬼界面按钮点击包 - 玩家: {}, 按钮ID: {}, 同步ID: {}", player != null ? player.getName().getString() : "null", buttonId, syncId);
         server.execute(() -> {
            LOGGER.debug("开始处理驭鬼界面按钮点击包 - 玩家: {}, 按钮ID: {}, 同步ID: {}", player != null ? player.getName().getString() : "null", buttonId, syncId);
            ScreenHandler currentHandler = player.currentScreenHandler;
            if (currentHandler != null) {
               LOGGER.debug("玩家当前界面类型: {}", currentHandler.getClass().getName());
            }

            if (buttonId == 0 || buttonId == 1) {
               if (player.currentScreenHandler instanceof GhostTamingScreenHandler tamingHandler) {
                  if (buttonId == 1) {
                     tamingHandler.handleTaming(player);
                  } else {
                     tamingHandler.handleTaming(player);
                  }
               } else {
                  LOGGER.warn("忽略驾驭请求：当前未打开驭鬼界面");
               }
            }
         });
      });
      ServerPlayNetworking.registerGlobalReceiver(
         new Identifier("smfs", "open_taming_screen"), (server, player, handler, buf, responseSender) -> server.execute(() -> {
            if (player != null) {
               ItemStack containerStack = new ItemStack(ModItems.GOLDEN_CONTAINER);
               player.openHandledScreen(new GhostTamingScreenHandler.GhostTamingFactory(containerStack));
            }
         })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         new Identifier("smfs", "open_royal_curse_screen"), (server, player, handler, buf, responseSender) -> server.execute(() -> {
            if (player != null) {
               player.openHandledScreen(new RoyalCurseScreenHandler.RoyalCurseFactory());
            }
         })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         new Identifier("smfs", "open_ghost_child_screen"), (server, player, handler, buf, responseSender) -> server.execute(() -> {
            if (player != null) {
               try {
                  PlayerGhostChildManager.initializeGhostChild(player);
                  player.openHandledScreen(new GhostChildCultivationScreenHandler.GhostChildCultivationFactory());
               } catch (Exception e) {
                  LOGGER.error("Error opening Ghost Child Cultivation Screen for player: {}", player.getName().getString(), e);
               }
            }
         })
      );
      GhostPressureDetectionPacket.registerServerHandler();
      ServerPlayNetworking.registerGlobalReceiver(GhostTamingProgressPacket.ID, GhostTamingProgressPacket::handleServer);
      ServerPlayNetworking.registerGlobalReceiver(GhostChildSummonPacket.ID, GhostChildSummonPacket::handleServer);
      ServerPlayNetworking.registerGlobalReceiver(GhostChildRecallPacket.ID, GhostChildRecallPacket::handleServer);
      ServerPlayNetworking.registerGlobalReceiver(
         GhostChildFusionC2SPacket.ID, (server, player, handler, buf, responseSender) -> GhostChildFusionC2SPacket.handleServer(player, buf)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientSneakGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientSneakGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientClothesGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientClothesGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientPuppetGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientPuppetGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientFuneralMusicGhostSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientFuneralMusicGhostSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientFuneralMusicGhostNSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientFuneralMusicGhostNSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientFuneralMusicGhostGSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientFuneralMusicGhostGSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientFuneralMusicGhostVSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientFuneralMusicGhostVSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostLakeSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostLakeSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostDreamSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostDreamSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostLakeNSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostLakeNSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostLakeGSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostLakeGSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostLakeVSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostLakeVSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         ClientGhostDreamVSkillC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> ClientGhostDreamVSkillC2SPacket.receive(server, player, handler, buf, responseSender)
      );
      ServerPlayNetworking.registerGlobalReceiver(
         new Identifier("smfs", "open_ghost_screen"), (server, player, handler, buf, responseSender) -> server.execute(() -> {
            try {
               player.openHandledScreen(new GhostControlScreenHandler.GhostControlFactory());
               LOGGER.debug("玩家 {} 通过按钮打开了厉鬼控制界面", player.getName().getString());
            } catch (Exception e) {
               LOGGER.error("打开厉鬼控制界面失败", e);
               player.sendMessage(Text.literal("§c打开界面失败: " + e.getMessage()), true);
            }
         })
      );
      ServerPlayNetworking.registerGlobalReceiver(
         new Identifier("smfs", "open_quest_screen"), (server, player, handler, buf, responseSender) -> server.execute(() -> {
            try {
               ModConfig config = ModConfig.getInstance();
               if (!config.enableQuestSystem) {
                  player.sendMessage(Text.literal("§c任务系统已禁用，无法打开任务界面"), true);
                  return;
               }

               player.openHandledScreen(new QuestScreenHandler.QuestScreenFactory());
            } catch (Exception e) {
               LOGGER.error("打开任务界面失败", e);
               player.sendMessage(Text.literal("§c打开界面失败: " + e.getMessage()), true);
            }
         })
      );
      ServerPlayNetworking.registerGlobalReceiver(ConfigSyncC2SPacket.ID, ConfigSyncC2SPacket::handle);
      ServerPlayNetworking.registerGlobalReceiver(
         RequestTradeScreenC2SPacket.ID,
         (server, player, handler, buf, responseSender) -> RequestTradeScreenC2SPacket.handle(
            RequestTradeScreenC2SPacket.decode(buf), server, player, handler, responseSender
         )
      );
      ServerPlayNetworking.registerGlobalReceiver(RequestJoinFactionC2SPacket.ID, RequestJoinFactionC2SPacket::handle);
      ServerPlayNetworking.registerGlobalReceiver(SubmitCodenameC2SPacket.ID, SubmitCodenameC2SPacket::handle);
      ServerPlayNetworking.registerGlobalReceiver(HumanSkinPaperContinueC2SPacket.ID, HumanSkinPaperContinueC2SPacket::handle);
      SpearBindC2SPacket.registerServer();
      SpearModeConfigC2SPacket.registerServer();
      SpearRemoteAttackC2SPacket.registerServer();
      GoodsSellerKillC2SPacket.register();
   }

   public static void sendToClient(ShowCustomDeathScreenPacket packet, ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ServerPlayNetworking.send(player, ShowCustomDeathScreenPacket.ID, buf);
   }

   public static void sendQuestAcceptResultToClient(String questId, boolean success, NbtCompound updatedQuestData, ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeString(questId);
      buf.writeBoolean(success);
      buf.writeNbt(updatedQuestData);
      ServerPlayNetworking.send(player, QuestAcceptResultPacket.ID, buf);
   }

   public static void sendQuestDetectionResultToClient(
      String questId, String objectiveId, boolean success, NbtCompound updatedQuestData, ServerPlayerEntity player
   ) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeString(questId);
      buf.writeString(objectiveId);
      buf.writeBoolean(success);
      buf.writeNbt(updatedQuestData);
      ServerPlayNetworking.send(player, QuestDetectionResultPacket.ID, buf);
   }

   public static void sendToClient(QuestRewardPacket packet, ServerPlayerEntity player) {
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ServerPlayNetworking.send(player, QuestRewardPacket.ID, buf);
   }

   public static void sendGhostDreamTimeToClient(long remainingTime, ServerPlayerEntity player) {
      GhostDreamTimeS2CPacket packet = new GhostDreamTimeS2CPacket(remainingTime);
      PacketByteBuf buf = PacketByteBufs.create();
      packet.write(buf);
      ServerPlayNetworking.send(player, new Identifier("smfs", "ghost_dream_time"), buf);
   }
}
