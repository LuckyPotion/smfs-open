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
         if (!player.method_7270(book)) {
            player.method_7328(book, false);
         }

         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("§a已给玩家 " + player.method_5477().getString() + " 一本《神秘复苏指南》"), true);
         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("§c给书时发生错误: " + e.getMessage()));
         return 0;
      }
   }

   public static int listAvailableBooks(CommandContext<ServerCommandSource> context) {
      try {
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("§6可用的书:"), false);
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("§e- default: 神秘复苏指南"), false);
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("§e用法: /xie book give <玩家> default"), false);
         return 1;
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("§c列出书籍时发生错误: " + e.getMessage()));
         return 0;
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

   public static ItemStack createCustomBook(String title, String author, String[] pages) {
      ItemStack book = new ItemStack(Items.field_8360);
      NbtCompound tag = book.method_7948();
      tag.method_10582("title", title);
      tag.method_10582("author", author);
      tag.method_10556("resolved", true);
      NbtList pageList = new NbtList();

      for (String page : pages) {
         String jsonText = Serializer.method_10867(Text.method_43470(page));
         pageList.add(NbtString.method_23256(jsonText));
      }

      tag.method_10566("pages", pageList);
      book.method_7977(Text.method_43470("《" + title + "》"));
      return book;
   }

   public static boolean giveDefaultBookToPlayer(ServerPlayerEntity player) {
      try {
         ItemStack book = createDefaultBook();
         if (!player.method_7270(book)) {
            player.method_7328(book, false);
         }

         return true;
      } catch (Exception e) {
         return false;
      }
   }
}
