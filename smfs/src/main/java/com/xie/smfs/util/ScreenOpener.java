package com.xie.smfs.util;

import com.xie.smfs.client.screen.GhostControlScreen;
import com.xie.smfs.client.screen.PlayerInfoScreen;
import com.xie.smfs.client.screen.QuestHandledScreen;
import com.xie.smfs.event.screen.GhostControlScreenHandler;
import com.xie.smfs.event.screen.GhostTamingScreenHandler;
import com.xie.smfs.event.screen.QuestScreenHandler;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ScreenOpener {
   public static void openTamingScreen(PlayerEntity player) {
      if (player.getWorld().isClient) {
         ClientPlayNetworking.send(new Identifier("smfs", "open_taming_screen"), PacketByteBufs.empty());
      } else {
         ItemStack containerStack = new ItemStack(ModItems.GOLDEN_CONTAINER);
         player.openHandledScreen(new GhostTamingScreenHandler.GhostTamingFactory(containerStack));
      }
   }

   public static void openQuestScreen(PlayerEntity player) {
      if (player.getWorld().isClient) {
         MinecraftClient client = MinecraftClient.getInstance();
         client.execute(
            () -> client.setScreen(
               new QuestHandledScreen(new QuestScreenHandler(0, player.getInventory()), player.getInventory(), Text.translatable("screen.smfs.quest"))
            )
         );
      } else {
         player.openHandledScreen(new QuestScreenHandler.QuestScreenFactory());
      }
   }

   public static void openGhostControlScreen(PlayerEntity player) {
      if (player.getWorld().isClient) {
         MinecraftClient client = MinecraftClient.getInstance();
         client.execute(
            () -> client.setScreen(
               new GhostControlScreen(
                  new GhostControlScreenHandler(0, player.getInventory(), new SimpleInventory(10)),
                  player.getInventory(),
                  Text.translatable("container.ghost_control")
               )
            )
         );
      } else {
         player.openHandledScreen(new GhostControlScreenHandler.GhostControlFactory());
      }
   }

   public static void openInfoScreen(PlayerEntity player) {
      if (player.getWorld().isClient) {
         MinecraftClient client = MinecraftClient.getInstance();
         client.execute(() -> client.setScreen(new PlayerInfoScreen(player)));
      }
   }
}
