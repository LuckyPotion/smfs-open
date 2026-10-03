package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.manager.GhostDomainManager;
import java.text.DecimalFormat;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientGhostFireJSkillC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "client_ghost_fire_j_skill");

   public ClientGhostFireJSkillC2SPacket() {
   }

   public ClientGhostFireJSkillC2SPacket(PacketByteBuf buf) {
   }

   public void write(PacketByteBuf buf) {
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> {
         if (GhostDomainManager.checkAndSetJSkillCooldown(player, "j_key_skill", 20, "J键技能")) {
            attackBurningEntities(player);
         }
      });
   }

   private static void attackBurningEntities(ServerPlayerEntity player) {
      int ghostFireLevel = GhostDomainManager.getEffectiveSkillLevel(player, GhostDomainManager.getGhostFireLevel(player));
      if (ghostFireLevel < 2) {
         player.sendMessage(Text.literal(GhostDomainManager.getInsufficientLevelMessage(player)), true);
      } else {
         double range = 30.0;
         int attackedCount = 0;
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 0.0F;
         if (spiritDamage <= 0.0F) {
            spiritDamage = 5.0F;
         }

         boolean bothEquipped = GhostDomainManager.hasValidGhostFire(player) && GhostDomainManager.hasWaterGhost(player);
         int multiplier = bothEquipped ? 2 : 1;
         float displayDamage = spiritDamage * multiplier;

         for (Entity entity : player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(range))) {
            if (entity.isAlive() && entity != player && entity instanceof LivingEntity livingEntity && livingEntity.isOnFire()) {
               GhostDomainManager.executeSkillSpiritAttack(player, livingEntity, multiplier);
               livingEntity.setOnFireFor(10);
               attackedCount++;
            }
         }

         if (attackedCount > 0) {
            if (ModConfig.getInstance().showActionBarInfo) {
               player.sendMessage(Text.literal("§a对" + attackedCount + "个生物造成" + new DecimalFormat("#.###").format(displayDamage) + "点灵异伤害"), true);
            }
         } else {
            player.sendMessage(Text.literal("§7周围没有燃烧的生物可以袭击"), true);
         }

         PlayerEvents.balanceRevivalDegree(player);
      }
   }
}
