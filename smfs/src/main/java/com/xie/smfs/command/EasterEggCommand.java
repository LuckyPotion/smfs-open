package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;

public class EasterEggCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("easter")
                  .then(
                     ((LiteralArgumentBuilder)CommandManager.literal("yinqi").requires(source -> source.hasPermissionLevel(2))).executes(YinQiCommand::execute)
                  ))
               .then(
                  ((LiteralArgumentBuilder)CommandManager.literal("xianwang").requires(source -> source.hasPermissionLevel(2)))
                     .executes(XianWangCommand::execute)
               ))
            .then(
               ((LiteralArgumentBuilder)CommandManager.literal("guiniao").requires(source -> source.hasPermissionLevel(2))).executes(GuiNiaoCommand::execute)
            )
      );
   }
}
