package com.xie.smfs.manager;

import com.xie.smfs.common.events.PlayerEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainGhostManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/MainGhostManager");
   private static final String MAIN_GHOST_KEY = "MainGhostSlot";

   public static int getMainGhostSlot(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      return data.contains("MainGhostSlot") ? data.getInt("MainGhostSlot") : 0;
   }

   public static void setMainGhostSlot(PlayerEntity player, int slotIndex) {
      if (slotIndex >= 0 && slotIndex < 10) {
         NbtCompound data = PlayerEvents.getCachedData(player);
         data.putInt("MainGhostSlot", slotIndex);
         PlayerEvents.setSpiritAttributes(player, data);
      }
   }

   public static void switchMainGhostToSlot(PlayerEntity player, int slotIndex) {
      if (slotIndex >= 0 && slotIndex < 10) {
         if (PlayerEvents.isGhostSlotOccupied(player, slotIndex)) {
            setMainGhostSlot(player, slotIndex);
            LOGGER.debug("玩家 {} 切换主鬼到槽位 {}", player.getName().getString(), slotIndex);
         } else {
            player.sendMessage(Text.literal("§c槽位 " + slotIndex + " 没有鬼魂"), true);
            LOGGER.debug("玩家 {} 尝试切换到空槽位: {}", player.getName().getString(), slotIndex);
         }
      } else {
         LOGGER.debug("无效的槽位索引: {}", slotIndex);
      }
   }

   public static void switchToNextMainGhost(PlayerEntity player) {
      int currentSlot = getMainGhostSlot(player);
      int nextSlot = (currentSlot + 1) % 10;

      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.isGhostSlotOccupied(player, nextSlot)) {
            setMainGhostSlot(player, nextSlot);
            return;
         }

         nextSlot = (nextSlot + 1) % 10;
      }

      setMainGhostSlot(player, currentSlot);
   }

   public static Text getMainGhostName(PlayerEntity player) {
      int mainSlot = getMainGhostSlot(player);
      ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, mainSlot);
      return (Text)(!ghostItem.isEmpty() ? ghostItem.getName() : Text.translatable("text.smfs.no_main_ghost"));
   }

   public static boolean hasMainGhost(PlayerEntity player) {
      int mainSlot = getMainGhostSlot(player);
      return PlayerEvents.isGhostSlotOccupied(player, mainSlot);
   }

   public static int getMainGhostLevel(PlayerEntity player) {
      int mainSlot = getMainGhostSlot(player);
      return PlayerEvents.getGhostSlotLevel(player, mainSlot);
   }

   public static boolean isMainGhostType(PlayerEntity player, Class<?> ghostType) {
      int mainSlot = getMainGhostSlot(player);
      ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, mainSlot);
      return !ghostItem.isEmpty() ? ghostType.isInstance(ghostItem.getItem()) : false;
   }
}
