package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.LuoQianHudRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class LuoQianCombatStateS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "luo_qian_combat_state");

   public static void registerClient() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         boolean isInCombat = buf.readBoolean();
         int currentPower = buf.readInt();
         int maxPower = buf.readInt();
         boolean isInvincible = buf.readBoolean();
         float recoveryFactor = buf.readFloat();
         client.execute(() -> LuoQianHudRenderer.updateCombatState(isInCombat, currentPower, maxPower, isInvincible, recoveryFactor));
      });
   }

   public static void sendToAllPlayers(ServerWorld world, boolean isInCombat, int currentPower, int maxPower, boolean isInvincible, float recoveryFactor) {
      for (PlayerEntity player : world.method_18456()) {
         sendToPlayer((ServerPlayerEntity)player, isInCombat, currentPower, maxPower, isInvincible, recoveryFactor);
      }
   }

   public static void sendToPlayer(ServerPlayerEntity player, boolean isInCombat, int currentPower, int maxPower, boolean isInvincible, float recoveryFactor) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeBoolean(isInCombat);
      buf.writeInt(currentPower);
      buf.writeInt(maxPower);
      buf.writeBoolean(isInvincible);
      buf.writeFloat(recoveryFactor);
      ServerPlayNetworking.send(player, ID, buf);
   }
}
