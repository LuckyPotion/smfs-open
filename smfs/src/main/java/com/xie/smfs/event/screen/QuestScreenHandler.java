package com.xie.smfs.event.screen;

import com.xie.smfs.Smfs;
import com.xie.smfs.registry.ModScreenHandlers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class QuestScreenHandler extends ScreenHandler {
   public QuestScreenHandler(int syncId, PlayerInventory playerInventory) {
      super(ModScreenHandlers.QUEST_SCREEN_HANDLER, syncId);
   }

   public QuestScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
      this(syncId, playerInventory);
   }

   public boolean method_7597(PlayerEntity player) {
      return true;
   }

   public boolean method_7604(PlayerEntity player, int id) {
      return false;
   }

   public ItemStack method_7601(PlayerEntity player, int slot) {
      return null;
   }

   public static class QuestScreenFactory implements ExtendedScreenHandlerFactory, NamedScreenHandlerFactory {
      public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
      }

      public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            Smfs.LOGGER.info("玩家 {} (UUID: {}) 打开了任务界面", serverPlayer.method_5477().getString(), serverPlayer.method_5667());
         }

         return new QuestScreenHandler(syncId, inv);
      }

      public Text method_5476() {
         return Text.method_43471("screen.smfs.quest");
      }
   }
}
