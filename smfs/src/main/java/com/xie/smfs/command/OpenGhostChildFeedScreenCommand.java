package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.event.screen.GhostChildFeedScreenHandler;
import java.util.Objects;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;

public class OpenGhostChildFeedScreenCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)CommandManager.literal("xie")
            .then(CommandManager.literal("gui").then(CommandManager.literal("feed").executes(OpenGhostChildFeedScreenCommand::execute)))
      );
   }

   public static int execute(CommandContext<ServerCommandSource> context) {
      Objects.requireNonNull(((ServerCommandSource)context.getSource()).getPlayer()).openHandledScreen(new GhostChildFeedScreenHandler.GhostChildFeedFactory());
      return 1;
   }
}
