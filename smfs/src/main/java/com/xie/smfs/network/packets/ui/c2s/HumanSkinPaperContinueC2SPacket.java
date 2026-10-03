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
         ItemStack helmetStack = player.getInventory().getArmorStack(3);
         if (helmetStack.isOf(ModItems.HUMAN_SKIN_PAPER)) {
            player.getInventory().armor.set(3, ItemStack.EMPTY);
            if (!player.getInventory().insertStack(helmetStack)) {
               player.dropItem(helmetStack, false);
            }
         }
      });
   }

   public static void send() {
      PacketByteBuf buf = PacketByteBufs.create();
      ClientPlayNetworking.send(ID, buf);
   }
}
