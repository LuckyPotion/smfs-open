package com.xie.smfs.network.packets.common.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReinvadeRespawnPacket {
   public static final Identifier ID = new Identifier("smfs", "reinvade_respawn");

   public ReinvadeRespawnPacket() {
   }

   public ReinvadeRespawnPacket(PacketByteBuf buf) {
   }

   public void write(PacketByteBuf buf) {
   }

   public static void handle(
      MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender
   ) {
      Logger LOGGER = LoggerFactory.getLogger(ReinvadeRespawnPacket.class);
      server.execute(() -> {
         LOGGER.debug("=== 收到玩家 {} 的重新入侵请求 ===", player.method_5477().getString());
         Vec3d deathPos = player.method_19538();
         UUID playerUuid = player.method_5667();
         if (SpectateModePacket.isInSpectatorMode(playerUuid)) {
            LOGGER.warn("玩家 {} 在重新入侵时仍有旁观者标志，清除它", playerUuid);
            SpectateModePacket.removeSpectatorFlag(playerUuid);
         }

         NbtCompound data = PlayerEvents.getCachedData(player);
         data.method_10549("reinvade_x", deathPos.field_1352);
         data.method_10549("reinvade_y", deathPos.field_1351);
         data.method_10549("reinvade_z", deathPos.field_1350);
         data.method_10556("reinvade_active", true);
         PlayerEvents.saveDataToPlayer(player, data);
         server.execute(() -> server.execute(() -> {
            responseSender.sendPacket(ID, PacketByteBufs.empty());
            LOGGER.debug("已发送重新入侵确认包");
         }));
      });
   }
}
