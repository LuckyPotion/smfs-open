package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.event.screen.RoyalCurseScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OpenRoyalCurseScreenCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("royalcurse").executes(OpenRoyalCurseScreenCommand::openRoyalCurseScreen));
   }

   private static int openRoyalCurseScreen(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
      if (player != null) {
         player.openHandledScreen(new RoyalCurseScreenHandler.RoyalCurseFactory());
         player.sendMessage(Text.literal("已打开王家诅咒界面"), false);
         return 1;
      } else {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("只有玩家可以执行此命令"));
         return 0;
      }
   }

   public static int execute(CommandContext<ServerCommandSource> context) {
      return openRoyalCurseScreen(context);
   }
}
