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
      dispatcher.register((LiteralArgumentBuilder)CommandManager.method_9247("royalcurse").executes(OpenRoyalCurseScreenCommand::openRoyalCurseScreen));
   }

   private static int openRoyalCurseScreen(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
      if (player != null) {
         player.method_17355(new RoyalCurseScreenHandler.RoyalCurseFactory());
         player.method_7353(Text.method_43470("已打开王家诅咒界面"), false);
         return 1;
      } else {
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("只有玩家可以执行此命令"));
         return 0;
      }
   }

   public static int execute(CommandContext<ServerCommandSource> context) {
      return openRoyalCurseScreen(context);
   }
}
