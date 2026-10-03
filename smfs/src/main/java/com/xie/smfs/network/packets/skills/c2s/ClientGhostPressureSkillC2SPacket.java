package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.effect.GhostPressureEffect;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.TargetingUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientGhostPressureSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_pressure_skill");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ClientGhostPressureSkillC2SPacket");

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ID, ClientGhostPressureSkillC2SPacket::receive);
   }

   public static void receive(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         try {
            LOGGER.info("玩家 {} 执行鬼压人J键技能：将背上的鬼扔到目标身上", player.method_5477().getString());
            throwGhostToTarget(player);
            PlayerEvents.balanceRevivalDegree(player);
         } catch (Exception e) {
            LOGGER.error("处理鬼压人J键技能时发生错误", e);
         }
      });
   }

   private static void throwGhostToTarget(ServerPlayerEntity player) {
      GhostDomainManager.SkillCheckResult result = GhostDomainManager.canUseGhostSkill(player, "ghost_pressure", ModItems.GHOST_PRESSURE, -1);
      if (result == GhostDomainManager.SkillCheckResult.NO_GHOST) {
         player.method_7353(Text.method_43470("§c您没有驾驭鬼压人，无法使用此技能"), true);
      } else if (GhostDomainManager.isSkillOnCooldown(player, "ghost_pressure")) {
         long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "ghost_pressure");
         double remainingSeconds = remainingTicks / 20.0;
         player.method_7353(Text.method_43470("§c鬼压人技能正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
      } else {
         EntityHitResult entityHitResult = TargetingUtil.raycastEntity(player, 20.0);
         if (entityHitResult == null) {
            player.method_7353(Text.method_43470("§7请对准一个生物或玩家使用此技能"), true);
         } else if (!(entityHitResult.method_17782() instanceof LivingEntity)) {
            player.method_7353(Text.method_43470("§7目标必须是生物或玩家"), true);
         } else {
            LivingEntity target = (LivingEntity)entityHitResult.method_17782();
            if (target == player) {
               player.method_7353(Text.method_43470("§c不能对自己使用此技能"), true);
            } else if (target.method_6059(ModEffects.GHOST_PRESSURE)) {
               GhostDomainManager.setSkillCooldown(player, "ghost_pressure", 100);
               long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "ghost_pressure");
               double remainingSeconds = remainingTicks / 20.0;
               player.method_7353(Text.method_43470("§c技能正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
            } else {
               int mainSlot = MainGhostManager.getMainGhostSlot(player);
               int revivalLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
               revivalLevel = Math.max(1, Math.min(10, revivalLevel));
               StatusEffectInstance ghostPressureEffect = GhostPressureEffect.createEffect(player, 100, revivalLevel);
               target.method_6092(ghostPressureEffect);
               if (target instanceof PlayerEntity targetPlayer) {
                  targetPlayer.method_7353(Text.method_43470("§c你被鬼压住了！"), true);
               }

               PlayerEvents.balanceRevivalDegree(player);
               GhostDomainManager.setSkillCooldown(player, "ghost_pressure", 100);
               long remainingTicks = GhostDomainManager.getSkillCooldownRemaining(player, "ghost_pressure");
               double remainingSeconds = remainingTicks / 20.0;
               player.method_7353(Text.method_43470("§a成功将鬼压到目标身上，" + String.format("%.1f", remainingSeconds) + "秒后将发动袭击"), true);
            }
         }
      }
   }
}
