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
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("guiniao").requires(source -> source.method_9259(2)))
            .executes(GuiNiaoCommand::execute)
      );
   }

   public static int execute(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
      if (player == null) {
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
         return 0;
      } else {
         World world = player.method_37908();
         GuiNiaoEntity guiNiao = new GuiNiaoEntity(ModEntities.GUI_NIAO, world);
         guiNiao.method_5808(player.method_23317(), player.method_23318(), player.method_23321(), player.method_36454(), 0.0F);
         world.method_8649(guiNiao);
         ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("§d鬼鸟已召唤！"), true);
         return 1;
      }
   }
}
