package com.xie.smfs.network.packets.skills.c2s;

import com.xie.smfs.manager.GhostDomainManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientGhostDomainFireIgniteC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "client_ghost_domain_fire_ignite");

   public ClientGhostDomainFireIgniteC2SPacket() {
   }

   public ClientGhostDomainFireIgniteC2SPacket(PacketByteBuf buf) {
   }

   public void write(PacketByteBuf buf) {
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      server.execute(() -> igniteEntitiesInGreenGhostDomain(player));
   }

   private static void igniteEntitiesInGreenGhostDomain(ServerPlayerEntity player) {
      int ghostFireLevel = GhostDomainManager.getEffectiveSkillLevel(player, GhostDomainManager.getGhostFireLevel(player));
      if (ghostFireLevel < 2) {
         player.sendMessage(Text.literal(GhostDomainManager.getInsufficientLevelMessage(player)), true);
      } else {
         double range = 20.0;
         int[] ignitedCount = new int[]{0};
         player.getWorld()
            .getOtherEntities(player, player.getBoundingBox().expand(range), entity -> entity.isAlive() && entity != player && entity instanceof LivingEntity)
            .forEach(entity -> {
               entity.setOnFireFor(5);
               ignitedCount[0]++;
            });
         if (ignitedCount[0] > 0) {
            player.sendMessage(Text.literal("§6鬼火V技能点燃了 §c" + ignitedCount[0] + " §6个生物"), true);
         } else {
            player.sendMessage(Text.literal("§7鬼火V技能范围内没有可点燃的生物"), true);
         }
      }
   }
}
