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
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("easter")
                  .then(((LiteralArgumentBuilder)CommandManager.method_9247("yinqi").requires(source -> source.method_9259(2))).executes(YinQiCommand::execute)))
               .then(
                  ((LiteralArgumentBuilder)CommandManager.method_9247("xianwang").requires(source -> source.method_9259(2))).executes(XianWangCommand::execute)
               ))
            .then(((LiteralArgumentBuilder)CommandManager.method_9247("guiniao").requires(source -> source.method_9259(2))).executes(GuiNiaoCommand::execute))
      );
   }
}
