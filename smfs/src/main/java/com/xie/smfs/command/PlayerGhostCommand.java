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
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("playerghost")
                     .requires(source -> source.method_9259(2)))
                  .then(
                     CommandManager.method_9247("info")
                        .then(CommandManager.method_9244("ghost", EntityArgumentType.method_9309()).executes(PlayerGhostCommand::getGhostInfo))
                  ))
               .then(
                  CommandManager.method_9247("retrieve")
                     .then(CommandManager.method_9244("ghost", EntityArgumentType.method_9309()).executes(PlayerGhostCommand::retrieveCoffinNail))
               ))
            .then(
               CommandManager.method_9247("suppress")
                  .then(CommandManager.method_9244("ghost", EntityArgumentType.method_9309()).executes(PlayerGhostCommand::suppressGhost))
            )
      );
   }

   public static int getGhostInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = (ServerCommandSource)context.getSource();
      PlayerGhostEntity ghost = (PlayerGhostEntity)EntityArgumentType.method_9313(context, "ghost");
      if (ghost != null) {
         source.method_9226(() -> Text.method_43470("=== 玩家鬼魂信息 ===").method_27692(Formatting.field_1065), false);
         source.method_9226(() -> Text.method_43470("玩家: " + ghost.getPlayerName()).method_27692(Formatting.field_1060), false);
         source.method_9226(() -> Text.method_43470("鬼域颜色: " + ghost.getGhostDomainColor()).method_27692(Formatting.field_1060), false);
         List<String> playerGhosts = ghost.getPlayerGhosts();
         if (!playerGhosts.isEmpty()) {
            source.method_9226(() -> Text.method_43470("驾驭的鬼: " + String.join(", ", playerGhosts)).method_27692(Formatting.field_1054), false);
         }

         List<String> killingRules = ghost.getKillingRules();
         if (!killingRules.isEmpty()) {
            source.method_9226(() -> Text.method_43470("杀人规律: " + String.join(", ", killingRules)).method_27692(Formatting.field_1061), false);
         }

         source.method_9226(() -> Text.method_43470("压制状态: " + (ghost.isSuppressed() ? "已压制" : "未压制")).method_27692(Formatting.field_1078), false);
         source.method_9226(() -> Text.method_43470("棺材钉: " + (ghost.hasCoffinNail() ? "已钉入" : "未钉入")).method_27692(Formatting.field_1078), false);
         return 1;
      } else {
         source.method_9213(Text.method_43470("目标不是玩家鬼魂实体"));
         return 0;
      }
   }

   public static int retrieveCoffinNail(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = (ServerCommandSource)context.getSource();
      PlayerGhostEntity ghost = (PlayerGhostEntity)EntityArgumentType.method_9313(context, "ghost");
      if (ghost != null) {
         if (ghost.retrieveCoffinNail()) {
            source.method_9226(() -> Text.method_43470("成功取回棺材钉").method_27692(Formatting.field_1060), false);
            return 1;
         } else {
            source.method_9213(Text.method_43470("无法取回棺材钉（鬼魂未被压制或没有棺材钉）"));
            return 0;
         }
      } else {
         source.method_9213(Text.method_43470("目标不是玩家鬼魂实体"));
         return 0;
      }
   }

   public static int suppressGhost(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerCommandSource source = (ServerCommandSource)context.getSource();
      PlayerGhostEntity ghost = (PlayerGhostEntity)EntityArgumentType.method_9313(context, "ghost");
      if (ghost != null) {
         ghost.setSuppressed(true);
         source.method_9226(() -> Text.method_43470("已压制玩家鬼魂").method_27692(Formatting.field_1060), false);
         return 1;
      } else {
         source.method_9213(Text.method_43470("目标不是玩家鬼魂实体"));
         return 0;
      }
   }
}
