package com.xie.smfs.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.network.packets.ghostchild.s2c.GhostChildFusionBeginS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.AberrationPopupS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.ScreenEffectS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class UnlockCommand {
   public static int executeAberration(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         NbtCompound data = PlayerEvents.getCachedData(player);
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         int unlockedCount = 0;
         int[] slotsToUnlock = new int[]{0, 3};

         for (int slotIndex : slotsToUnlock) {
            String slotKey = "Slot" + slotIndex;
            if (ghostSlots.contains(slotKey)) {
               NbtCompound slotData = ghostSlots.getCompound(slotKey);
               if (!slotData.getBoolean("unlocked")) {
                  slotData.putBoolean("unlocked", true);
                  ghostSlots.put(slotKey, slotData);
                  unlockedCount++;
               }
            }
         }

         data.put("GhostSlots", ghostSlots);
         PlayerEvents.setSpiritAttributes(player, data);
         if (!AdvancementManager.hasAdvancement(player, "smfs:become_aberration")) {
            if (AdvancementManager.hasHumanSkinPaper(player)) {
               AdvancementManager.checkHumanSkinPaperEnding(player);
               return 1;
            }

            AdvancementManager.unlockAdvancement(player, "smfs:become_aberration");
            player.sendMessage(Text.literal("§6§l恭喜！你解锁了成就：成为异类§r"), false);
            player.sendMessage(Text.literal("§7你已经成为一种特殊的存在！§r"), false);
            ScreenEffectS2CPacket.sendGlitch(player);
         }

         AberrationPopupS2CPacket.send(player);
         player.sendMessage(Text.literal("§a已一键解锁异类！共解锁了 " + unlockedCount + " 个槽位").formatted(Formatting.GREEN), false);
         player.sendMessage(Text.literal("§7解锁的槽位：0, 3").formatted(Formatting.GRAY), false);
         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c解锁异类失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }

   public static int executeSlots(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         NbtCompound data = PlayerEvents.getCachedData(player);
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         int unlockedCount = 0;

         for (int i = 0; i < 10; i++) {
            String slotKey = "Slot" + i;
            if (ghostSlots.contains(slotKey)) {
               NbtCompound slotData = ghostSlots.getCompound(slotKey);
               if (!slotData.getBoolean("unlocked")) {
                  slotData.putBoolean("unlocked", true);
                  ghostSlots.put(slotKey, slotData);
                  unlockedCount++;
               }
            }
         }

         data.put("GhostSlots", ghostSlots);
         PlayerEvents.setSpiritAttributes(player, data);
         player.sendMessage(Text.literal("§a已一键解锁全部10个槽位！共解锁了 " + unlockedCount + " 个槽位").formatted(Formatting.GREEN), false);
         if (unlockedCount > 0) {
            player.sendMessage(Text.literal("§7所有槽位(0-9)现在已全部解锁").formatted(Formatting.GRAY), false);
         } else {
            player.sendMessage(Text.literal("§7所有槽位已经处于解锁状态").formatted(Formatting.GRAY), false);
         }

         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c解锁槽位失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }

   public static int executeFusion(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         GhostChildData data = PlayerGhostChildManager.getGhostChildData(player);
         if (data == null) {
            data = new GhostChildData();
            data.setLevel(11);
         }

         if (data.getLevel() <= 10) {
            data.setLevel(11);
         }

         PlayerGhostChildManager.saveGhostChildData(player, data);
         AdvancementManager.checkAndUnlockBecomeGod(player);
         GhostChildFusionBeginS2CPacket.send(player);
         player.sendMessage(Text.literal("§a已触发鬼童融合成神！").formatted(Formatting.GREEN), false);
         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c融合失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }
}
