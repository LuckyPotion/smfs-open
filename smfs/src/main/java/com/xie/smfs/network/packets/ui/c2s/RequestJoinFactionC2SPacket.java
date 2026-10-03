package com.xie.smfs.network.packets.ui.c2s;

import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.network.packets.ui.s2c.OpenCodenameInputS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RequestJoinFactionC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "request_join_faction");
   private final int entityId;

   public RequestJoinFactionC2SPacket(int entityId) {
      this.entityId = entityId;
   }

   public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
      int entityId = buf.readInt();
      server.execute(() -> {
         Entity entity = player.method_37908().method_8469(entityId);
         if (entity != null) {
            PlayerFaction targetFaction = FactionManager.getFactionForEntity(entity);
            if (targetFaction != null) {
               boolean joined = FactionManager.tryJoinFaction(player, targetFaction);
               if (joined && !FactionManager.hasCodename(player)) {
                  PacketByteBuf codenameBuf = PacketByteBufs.create();
                  new OpenCodenameInputS2CPacket(entityId).write(codenameBuf);
                  ServerPlayNetworking.send(player, OpenCodenameInputS2CPacket.ID, codenameBuf);
               }
            }
         }
      });
   }

   public static void send(int entityId) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeInt(entityId);
      ClientPlayNetworking.send(ID, buf);
   }
}
