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
         if (client.field_1724 != null) {
            boolean isUsingHumanSkinPaper = isUsingHumanSkinPaper(client.field_1724);
            if (isUsingHumanSkinPaper && !wasUsingHumanSkinPaper) {
               openHumanSkinPaperScreen(client);
            }

            wasUsingHumanSkinPaper = isUsingHumanSkinPaper;
         }
      });
   }

   private static boolean isUsingHumanSkinPaper(PlayerEntity player) {
      ItemStack mainHandStack = player.method_5998(Hand.field_5808);
      ItemStack offHandStack = player.method_5998(Hand.field_5810);
      return (mainHandStack.method_7909() instanceof HumanSkinPaperItem || offHandStack.method_7909() instanceof HumanSkinPaperItem) && player.method_6115();
   }

   private static void openHumanSkinPaperScreen(MinecraftClient client) {
      client.execute(() -> {
         if (client.field_1755 == null) {
            ModConfig config = ModConfig.getInstance();
            if (config.aiHumanSkinPaperEnabled && !config.aiHumanSkinPaperApiKey.isEmpty()) {
               client.method_1507(new AiHumanSkinPaperScreen());
            } else {
               client.method_1507(new TutorialScreen());
            }
         }
      });
   }
}
