package com.xie.smfs.network.packets.ui.s2c;

import com.xie.smfs.client.screen.GhostAbilityPopupScreen;
import com.xie.smfs.util.GhostUtils;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text.Serializer;
import net.minecraft.util.Identifier;

public class GhostAbilityPopupS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "ghost_ability_popup");

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(
         ID,
         (client, handler, buf, responseSender) -> {
            try {
               NbtCompound ghostData = buf.method_10798();
               if (ghostData == null) {
                  System.err.println("[SMFS] GhostAbilityPopupS2CPacket: Received null ghost data");
                  return;
               }

               client.execute(
                  () -> {
                     try {
                        if (!ghostData.method_10545("Type")) {
                           System.err.println("[SMFS] GhostAbilityPopupS2CPacket: Missing 'Type' field in ghost data");
                           return;
                        }

                        String ghostType = ghostData.method_10558("Type");
                        String ghostName = ghostData.method_10545("CustomName")
                           ? Serializer.method_10877(ghostData.method_10558("CustomName")).getString()
                           : GhostUtils.getGhostDisplayName(ghostType);
                        List<String> abilityDescriptions = GhostUtils.getGhostAbilityDescriptions(ghostType);
                        GhostAbilityPopupScreen.show(ghostName, abilityDescriptions);
                     } catch (Exception ex) {
                        ex.printStackTrace();
                     }
                  }
               );
            } catch (Exception e) {
               System.err.println("[SMFS] GhostAbilityPopupS2CPacket: Error reading packet data: " + e.getMessage());
               e.printStackTrace();
            }
         }
      );
   }
}
