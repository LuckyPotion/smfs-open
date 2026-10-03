package com.xie.smfs.client;

import com.xie.smfs.client.screen.AiHumanSkinPaperScreen;
import com.xie.smfs.client.screen.TutorialScreen;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.item.HumanSkinPaperItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HumanSkinPaperClientHandler {
   private static boolean wasUsingHumanSkinPaper = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.player != null) {
            boolean isUsingHumanSkinPaper = isUsingHumanSkinPaper(client.player);
            if (isUsingHumanSkinPaper && !wasUsingHumanSkinPaper) {
               openHumanSkinPaperScreen(client);
            }

            wasUsingHumanSkinPaper = isUsingHumanSkinPaper;
         }
      });
   }

   private static boolean isUsingHumanSkinPaper(PlayerEntity player) {
      ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
      ItemStack offHandStack = player.getStackInHand(Hand.OFF_HAND);
      return (mainHandStack.getItem() instanceof HumanSkinPaperItem || offHandStack.getItem() instanceof HumanSkinPaperItem) && player.isUsingItem();
   }

   private static void openHumanSkinPaperScreen(MinecraftClient client) {
      client.execute(() -> {
         if (client.currentScreen == null) {
            ModConfig config = ModConfig.getInstance();
            if (config.aiHumanSkinPaperEnabled && !config.aiHumanSkinPaperApiKey.isEmpty()) {
               client.setScreen(new AiHumanSkinPaperScreen());
            } else {
               client.setScreen(new TutorialScreen());
            }
         }
      });
   }
}
