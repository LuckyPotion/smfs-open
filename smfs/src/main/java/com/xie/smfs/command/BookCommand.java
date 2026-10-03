package com.xie.smfs.command;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;

public class BookCommand {
   public static int giveBook(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, String bookId) {
      try {
         ItemStack book = createDefaultBook();
         if (!player.giveItemStack(book)) {
            player.dropItem(book, false);
         }

         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("§a已给玩家 " + player.getName().getString() + " 一本《神秘复苏指南》"), true);
         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("§c给书时发生错误: " + e.getMessage()));
         return 0;
      }
   }

   public static int listAvailableBooks(CommandContext<ServerCommandSource> context) {
      try {
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("§6可用的书:"), false);
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("§e- default: 神秘复苏指南"), false);
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("§e用法: /xie book give <玩家> default"), false);
         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("§c列出书籍时发生错误: " + e.getMessage()));
         return 0;
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

   public static ItemStack createCustomBook(String title, String author, String[] pages) {
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      tag.putString("title", title);
      tag.putString("author", author);
      tag.putBoolean("resolved", true);
      NbtList pageList = new NbtList();

      for (String page : pages) {
         String jsonText = Serializer.toJson(Text.literal(page));
         pageList.add(NbtString.of(jsonText));
      }

      tag.put("pages", pageList);
      book.setCustomName(Text.literal("《" + title + "》"));
      return book;
   }

   public static boolean giveDefaultBookToPlayer(ServerPlayerEntity player) {
      try {
         ItemStack book = createDefaultBook();
         if (!player.giveItemStack(book)) {
            player.dropItem(book, false);
         }

         return true;
      } catch (Exception e) {
         return false;
      }
   }
}
