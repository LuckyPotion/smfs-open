package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.Smfs;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.event.screen.QuestScreenHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OpenQuestScreenCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)CommandManager.method_9247("xie").then(CommandManager.method_9247("questgui").executes(OpenQuestScreenCommand::execute))
      );
   }

   public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();
      ModConfig config = ModConfig.getInstance();
      if (!config.enableQuestSystem) {
         player.method_7353(Text.method_43470("§c任务系统已禁用，无法打开任务界面"), true);
         return 0;
      }

      try {
         player.method_17355(new QuestScreenHandler.QuestScreenFactory());
         return 1;
      } catch (Exception e) {
         Smfs.LOGGER.error("打开任务界面失败", e);
         player.method_7353(Text.method_43470("§c打开界面失败: " + e.getMessage()), true);
         return 0;
      }
   }
}
