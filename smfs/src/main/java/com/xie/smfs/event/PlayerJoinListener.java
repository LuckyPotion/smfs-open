package com.xie.smfs.event;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.registry.ModItems;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;

public class PlayerJoinListener {
   public static void register() {
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> {
         ServerPlayerEntity player = handler.getPlayer();
         if (!ModConfig.getInstance().unlockSaveLock && GhostDeathHandler.isPlayerPermanentlyLocked(player)) {
            System.out.println("玩家 " + player.getGameProfile().getName() + " 被检测为永久锁定，正在断开连接");
            player.networkHandler.disconnect(Text.translatable("message.smfs.ghost.world_corrupted"));
         } else {
            NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
            if (!playerData.getBoolean("has_received_starter_items")) {
               giveItem(player, new ItemStack(Items.WOODEN_SWORD));
               giveItem(player, new ItemStack(Items.BREAD, 10));
               giveItem(player, new ItemStack(ModItems.HUMAN_SKIN_PAPER));
               giveItem(player, createDefaultBook());
               playerData.putBoolean("has_received_starter_items", true);
               PlayerEvents.setSpiritAttributes(player, playerData);
            }
         }
      });
   }

   private static void giveItem(PlayerEntity player, ItemStack stack) {
      if (!player.getInventory().insertStack(stack)) {
         player.dropItem(stack, false);
      }
   }

   private static ItemStack createDefaultBook() {
      try {
         ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
         NbtCompound tag = book.getOrCreateNbt();
         tag.putString("title", Text.translatable("book.smfs.default_book.title").getString());
         tag.putString("author", Text.translatable("book.smfs.default_book.author").getString());
         NbtList pages = new NbtList();

         for (int i = 0; i < 11; i++) {
            String translationKey = "book.smfs.default_book.page" + (i + 1);
            String pageText = Text.translatable(translationKey).getString();
            String jsonText = Serializer.toJson(Text.literal(pageText));
            pages.add(NbtString.of(jsonText));
         }

         tag.put("pages", pages);
         tag.putBoolean("resolved", true);
         book.setCustomName(Text.translatable("book.smfs.default_book.title"));
         return book;
      } catch (Exception e) {
         return new ItemStack(Items.WRITTEN_BOOK);
      }
   }
}
