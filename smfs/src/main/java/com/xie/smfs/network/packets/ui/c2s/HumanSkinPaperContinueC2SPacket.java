package com.xie.smfs.network.packets.ui.c2s;

import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class HumanSkinPaperContinueC2SPacket {
   public static final Identifier ID = new Identifier("smfs", "human_skin_paper_continue");

   public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
      server.execute(() -> {
         ItemStack helmetStack = player.method_31548().method_7372(3);
         if (helmetStack.method_31574(ModItems.HUMAN_SKIN_PAPER)) {
            player.method_31548().field_7548.set(3, ItemStack.field_8037);
            if (!player.method_31548().method_7394(helmetStack)) {
               player.method_7328(helmetStack, false);
            }
         }
      });
   }

   public static void send() {
      PacketByteBuf buf = PacketByteBufs.create();
      ClientPlayNetworking.send(ID, buf);
   }
}
