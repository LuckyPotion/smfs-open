package com.xie.smfs.network.packets.ui.c2s;

import com.xie.smfs.faction.FactionManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SubmitCodenameC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "submit_codename");

   public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
      String codename = buf.method_19772();
      server.execute(() -> {
         if (codename != null && !codename.trim().isEmpty()) {
            if (codename.length() > 16) {
               player.method_7353(Text.method_43470("§c代号长度不能超过16个字符！").method_27692(Formatting.field_1061), false);
            } else {
               FactionManager.setCodename(player, codename.trim());
               player.method_7353(Text.method_43470("§a代号设置成功！你的代号是：" + codename.trim()).method_27692(Formatting.field_1060), false);
            }
         } else {
            player.method_7353(Text.method_43470("§c代号不能为空！").method_27692(Formatting.field_1061), false);
         }
      });
   }

   public static void send(String codename) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.method_10814(codename);
      ClientPlayNetworking.send(ID, buf);
   }
}
