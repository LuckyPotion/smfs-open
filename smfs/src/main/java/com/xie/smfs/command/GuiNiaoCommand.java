package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.entity.master.GuiNiaoEntity;
import com.xie.smfs.registry.ModEntities;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class GuiNiaoCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("guiniao").requires(source -> source.hasPermissionLevel(2)))
            .executes(GuiNiaoCommand::execute)
      );
   }

   public static int execute(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
      if (player == null) {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
         return 0;
      } else {
         World world = player.getWorld();
         GuiNiaoEntity guiNiao = new GuiNiaoEntity(ModEntities.GUI_NIAO, world);
         guiNiao.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0F);
         world.spawnEntity(guiNiao);
         ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("§d鬼鸟已召唤！"), true);
         return 1;
      }
   }
}
