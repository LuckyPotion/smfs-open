package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.screen.GhostAbilityPopupScreen;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AberrationPopupS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "aberration_popup");

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         try {
            client.execute(() -> {
               try {
                  List<String> contents = Arrays.asList("", "你对灵异的理解更深刻了！", "技能冷却缩减25%。", "可驾驭灵异数量+2。", "复苏速度减缓50%。", "周围有厉鬼不再消耗理智。");
                  GhostAbilityPopupScreen.showSingleColumnPopup("异类", 16766720, 1.2F, contents);
               } catch (Exception e) {
                  System.err.println("[SMFS] AberrationPopupS2CPacket: Error processing popup: " + e.getMessage());
                  e.printStackTrace();
               }
            });
         } catch (Exception e) {
            System.err.println("[SMFS] AberrationPopupS2CPacket: Error reading packet data: " + e.getMessage());
            e.printStackTrace();
         }
      });
   }

   public static void send(ServerPlayerEntity player) {
      try {
         ServerPlayNetworking.send(player, ID, PacketByteBufs.empty());
      } catch (Exception e) {
         System.err.println("[SMFS] AberrationPopupS2CPacket: Error sending packet: " + e.getMessage());
         e.printStackTrace();
      }
   }
}
