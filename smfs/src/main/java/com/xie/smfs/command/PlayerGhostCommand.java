package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import java.util.List;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PlayerGhostCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("playerghost")
                     .requires(source -> source.hasPermissionLevel(2)))
                  .then(
                     CommandManager.literal("info")
                        .then(CommandManager.argument("ghost", EntityArgumentType.entity()).executes(PlayerGhostCommand::getGhostInfo))
                  ))
               .then(
                  CommandManager.literal("retrieve")
                     .then(CommandManager.argument("ghost", EntityArgumentType.entity()).executes(PlayerGhostCommand::retrieveCoffinNail))
               ))
            .then(
               CommandManager.literal("suppress")
                  .then(CommandManager.argument("ghost", EntityArgumentType.entity()).executes(PlayerGhostCommand::suppressGhost))
            )
      );
   }

   public static int getGhostInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = (ServerCommandSource)context.getSource();
      PlayerGhostEntity ghost = (PlayerGhostEntity)EntityArgumentType.getEntity(context, "ghost");
      if (ghost != null) {
         source.sendFeedback(() -> Text.literal("=== 玩家鬼魂信息 ===").formatted(Formatting.GOLD), false);
         source.sendFeedback(() -> Text.literal("玩家: " + ghost.getPlayerName()).formatted(Formatting.GREEN), false);
         source.sendFeedback(() -> Text.literal("鬼域颜色: " + ghost.getGhostDomainColor()).formatted(Formatting.GREEN), false);
         List<String> playerGhosts = ghost.getPlayerGhosts();
         if (!playerGhosts.isEmpty()) {
            source.sendFeedback(() -> Text.literal("驾驭的鬼: " + String.join(", ", playerGhosts)).formatted(Formatting.YELLOW), false);
         }

         List<String> killingRules = ghost.getKillingRules();
         if (!killingRules.isEmpty()) {
            source.sendFeedback(() -> Text.literal("杀人规律: " + String.join(", ", killingRules)).formatted(Formatting.RED), false);
         }

         source.sendFeedback(() -> Text.literal("压制状态: " + (ghost.isSuppressed() ? "已压制" : "未压制")).formatted(Formatting.BLUE), false);
         source.sendFeedback(() -> Text.literal("棺材钉: " + (ghost.hasCoffinNail() ? "已钉入" : "未钉入")).formatted(Formatting.BLUE), false);
         return 1;
      } else {
         source.sendError(Text.literal("目标不是玩家鬼魂实体"));
         return 0;
      }
   }

   public static int retrieveCoffinNail(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = (ServerCommandSource)context.getSource();
      PlayerGhostEntity ghost = (PlayerGhostEntity)EntityArgumentType.getEntity(context, "ghost");
      if (ghost != null) {
         if (ghost.retrieveCoffinNail()) {
            source.sendFeedback(() -> Text.literal("成功取回棺材钉").formatted(Formatting.GREEN), false);
            return 1;
         } else {
            source.sendError(Text.literal("无法取回棺材钉（鬼魂未被压制或没有棺材钉）"));
            return 0;
         }
      } else {
         source.sendError(Text.literal("目标不是玩家鬼魂实体"));
         return 0;
      }
   }

   public static int suppressGhost(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = (ServerCommandSource)context.getSource();
      PlayerGhostEntity ghost = (PlayerGhostEntity)EntityArgumentType.getEntity(context, "ghost");
      if (ghost != null) {
         ghost.setSuppressed(true);
         source.sendFeedback(() -> Text.literal("已压制玩家鬼魂").formatted(Formatting.GREEN), false);
         return 1;
      } else {
         source.sendError(Text.literal("目标不是玩家鬼魂实体"));
         return 0;
      }
   }
}
