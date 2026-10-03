package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.entity.master.YinQiEntity;
import com.xie.smfs.registry.ModEntities;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class YinQiCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("yinqi").requires(source -> source.hasPermissionLevel(2)))
            .executes(YinQiCommand::execute)
      );
   }

   public static int execute(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
      if (player == null) {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
         return 0;
      } else {
         World world = player.getWorld();
         YinQiEntity yinQi = new YinQiEntity(ModEntities.YIN_QI, world);
         yinQi.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0F);
         world.spawnEntity(yinQi);
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("§d楪祈已召唤！"), true);
         return 1;
      }
   }
}
