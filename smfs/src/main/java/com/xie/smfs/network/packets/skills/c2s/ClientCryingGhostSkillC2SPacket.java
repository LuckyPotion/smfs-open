package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.manager.GhostDomainManager;
import java.text.DecimalFormat;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCryingGhostSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "crying_ghost_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientCryingGhostSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientCryingGhostSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            try {
               LOGGER.info("玩家 {} 执行哭丧鬼J键技能：哭泣标记", player.method_5477().getString());
               PlayerEvents.balanceRevivalDegree(player);
               handleCryingGhostSkill(player);
            } catch (Exception e) {
               LOGGER.error("处理哭丧鬼J键技能时发生错误", e);
            }
         }
      });
   }

   private static void handleCryingGhostSkill(ServerPlayerEntity player) {
      int radius = 10;
      List<LivingEntity> entitiesInRange = player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity);
      if (entitiesInRange.isEmpty()) {
         player.method_7353(Text.method_43470("§c哭丧鬼J键：范围内没有可攻击的生物"), true);
      } else {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 0.0F;
         if (spiritDamage <= 0.0F) {
            spiritDamage = 5.0F;
         }

         int attackCount = 0;

         for (LivingEntity entity : entitiesInRange) {
            GhostDomainManager.executeSkillSpiritAttack(player, entity);
            attackCount++;
            LOGGER.info("玩家 {} 使用哭丧鬼J键技能攻击生物 {}，距离: {}", player.method_5477().getString(), entity.method_5477().getString(), player.method_5739(entity));
         }

         if (ModConfig.getInstance().showActionBarInfo) {
            player.method_7353(Text.method_43470("§a对" + attackCount + "个生物造成" + new DecimalFormat("#.###").format(spiritDamage) + "点灵异伤害"), true);
         }
      }
   }
}
