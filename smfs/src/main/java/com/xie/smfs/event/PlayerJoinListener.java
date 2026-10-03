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
         ServerPlayerEntity player = handler.method_32311();
         if (!ModConfig.getInstance().unlockSaveLock && GhostDeathHandler.isPlayerPermanentlyLocked(player)) {
            System.out.println("玩家 " + player.method_7334().getName() + " 被检测为永久锁定，正在断开连接");
            player.field_13987.method_14367(Text.method_43471("message.smfs.ghost.world_corrupted"));
         } else {
            NbtCompound playerData = PlayerEvents.getSpiritAttributes(player);
            if (!playerData.method_10577("has_received_starter_items")) {
               giveItem(player, new ItemStack(Items.field_8091));
               giveItem(player, new ItemStack(Items.field_8229, 10));
               giveItem(player, new ItemStack(ModItems.HUMAN_SKIN_PAPER));
               giveItem(player, createDefaultBook());
               playerData.method_10556("has_received_starter_items", true);
               PlayerEvents.setSpiritAttributes(player, playerData);
            }
         }
      });
   }

   private static void giveItem(PlayerEntity player, ItemStack stack) {
      if (!player.method_31548().method_7394(stack)) {
         player.method_7328(stack, false);
      }
   }

   private static ItemStack createDefaultBook() {
      try {
         ItemStack book = new ItemStack(Items.field_8360);
         NbtCompound tag = book.method_7948();
         tag.method_10582("title", Text.method_43471("book.smfs.default_book.title").getString());
         tag.method_10582("author", Text.method_43471("book.smfs.default_book.author").getString());
         NbtList pages = new NbtList();

         for (int i = 0; i < 11; i++) {
            String translationKey = "book.smfs.default_book.page" + (i + 1);
            String pageText = Text.method_43471(translationKey).getString();
            String jsonText = Serializer.method_10867(Text.method_43470(pageText));
            pages.add(NbtString.method_23256(jsonText));
         }

         tag.method_10566("pages", pages);
         tag.method_10556("resolved", true);
         book.method_7977(Text.method_43471("book.smfs.default_book.title"));
         return book;
      } catch (Exception e) {
         return new ItemStack(Items.field_8360);
      }
   }
}
