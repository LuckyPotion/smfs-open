package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.Smfs;
import com.xie.smfs.event.screen.GhostControlScreenHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OpenGhostScreenCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register((LiteralArgumentBuilder)CommandManager.method_9247("open_ghost_screen").executes(OpenGhostScreenCommand::execute));
   }

   public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();

      try {
         player.method_17355(new GhostControlScreenHandler.GhostControlFactory());
         player.method_7353(Text.method_43471("command.smfs.open_ghost_screen.success"), true);
         return 1;
      } catch (Exception e) {
         Smfs.LOGGER.error("打开厉鬼控制界面失败", e);
         player.method_7353(Text.method_43470("§c打开界面失败: " + e.getMessage()), true);
         return 0;
      }
   }
}
