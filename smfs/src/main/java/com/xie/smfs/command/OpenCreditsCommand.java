package com.xie.smfs.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.client.screen.CreditsScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenCreditsCommand implements Command<FabricClientCommandSource> {
   public static void register() {
      CommandDispatcher<FabricClientCommandSource> clientDispatcher = ClientCommandManager.getActiveDispatcher();
      clientDispatcher.register(
         (LiteralArgumentBuilder)ClientCommandManager.literal("xie").then(ClientCommandManager.literal("credits").executes(new OpenCreditsCommand()))
      );
      CommandRegistrationCallback.EVENT
         .register(
            (CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> dispatcher.register(
               (LiteralArgumentBuilder)CommandManager.literal("xie").then(CommandManager.literal("credits").executes(context -> {
                  ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                  if (player != null) {
                     openCreditsScreen(player);
                     return 1;
                  } else {
                     return 0;
                  }
               }))
            )
         );
   }

   public int run(CommandContext<FabricClientCommandSource> context) {
      CreditsScreen.show();
      return 1;
   }

   public static void openCreditsScreen(ServerPlayerEntity player) {
      if (player.getWorld().isClient) {
         CreditsScreen.show();
      }
   }

   public static int execute(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
      if (player != null) {
         openCreditsScreen(player);
         return 1;
      } else {
         return 0;
      }
   }
}
