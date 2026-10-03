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
      if (player.method_37908().field_9236) {
         ClientPlayNetworking.send(new Identifier("smfs", "open_taming_screen"), PacketByteBufs.empty());
      } else {
         ItemStack containerStack = new ItemStack(ModItems.GOLDEN_CONTAINER);
         player.method_17355(new GhostTamingScreenHandler.GhostTamingFactory(containerStack));
      }
   }

   public static void openQuestScreen(PlayerEntity player) {
      if (player.method_37908().field_9236) {
         MinecraftClient client = MinecraftClient.method_1551();
         client.execute(
            () -> client.method_1507(
               new QuestHandledScreen(new QuestScreenHandler(0, player.method_31548()), player.method_31548(), Text.method_43471("screen.smfs.quest"))
            )
         );
      } else {
         player.method_17355(new QuestScreenHandler.QuestScreenFactory());
      }
   }

   public static void openGhostControlScreen(PlayerEntity player) {
      if (player.method_37908().field_9236) {
         MinecraftClient client = MinecraftClient.method_1551();
         client.execute(
            () -> client.method_1507(
               new GhostControlScreen(
                  new GhostControlScreenHandler(0, player.method_31548(), new SimpleInventory(10)),
                  player.method_31548(),
                  Text.method_43471("container.ghost_control")
               )
            )
         );
      } else {
         player.method_17355(new GhostControlScreenHandler.GhostControlFactory());
      }
   }

   public static void openInfoScreen(PlayerEntity player) {
      if (player.method_37908().field_9236) {
         MinecraftClient client = MinecraftClient.method_1551();
         client.execute(() -> client.method_1507(new PlayerInfoScreen(player)));
      }
   }
}
