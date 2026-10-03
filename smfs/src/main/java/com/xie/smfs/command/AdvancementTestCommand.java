package com.xie.smfs.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.manager.AdvancementManager;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class AdvancementTestCommand {
   public static int executeTest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();

      try {
         AdvancementManager.checkAndUnlockEndGhostEra(player);
         player.method_7353(Text.method_43470("§a已触发成就检查逻辑").method_27692(Formatting.field_1060), false);
         player.method_7353(Text.method_43470("§7如果满足条件，成就将自动解锁").method_27692(Formatting.field_1080), false);
         return 1;
      } catch (Exception e) {
         player.method_7353(Text.method_43470("§c成就测试失败: " + e.getMessage()).method_27692(Formatting.field_1061), false);
         return 0;
      }
   }

   public static int executeForce(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();

      try {
         boolean alreadyUnlocked = AdvancementManager.hasAdvancement(player, "smfs:end_ghost_era");
         if (alreadyUnlocked) {
            player.method_7353(Text.method_43470("§c成就已经解锁，无需强制完成").method_27692(Formatting.field_1061), false);
            return 0;
         } else {
            forceUnlockAdvancement(player, "smfs:end_ghost_era");
            player.method_7353(Text.method_43470("§a§l强制解锁成功！").method_27692(Formatting.field_1060), false);
            player.method_7353(Text.method_43470("§7成就弹窗应该已经显示").method_27692(Formatting.field_1080), false);
            player.method_7353(Text.method_43470("§7使用 /xie advancement check 确认成就状态").method_27692(Formatting.field_1080), false);
            return 1;
         }
      } catch (Exception e) {
         player.method_7353(Text.method_43470("§c强制解锁失败: " + e.getMessage()).method_27692(Formatting.field_1061), false);
         return 0;
      }
   }

   private static void forceUnlockAdvancement(ServerPlayerEntity player, String advancementId) {
      if (player != null && advancementId != null) {
         try {
            PlayerAdvancementTracker tracker = player.method_14236();
            Advancement advancement = player.method_5682().method_3851().method_12896(new Identifier(advancementId));
            if (advancement != null) {
               AdvancementProgress progress = tracker.method_12882(advancement);
               if (!progress.method_740()) {
                  Iterable<String> unobtainedCriteria = progress.method_731();
                  int totalCriteria = 0;
                  int unlockedCount = 0;

                  for (String criterion : unobtainedCriteria) {
                     totalCriteria++;
                  }

                  player.method_7353(Text.method_43470("§e开始强制解锁成就...").method_27692(Formatting.field_1054), false);
                  player.method_7353(Text.method_43470("§7需要解锁的条件数量: " + totalCriteria).method_27692(Formatting.field_1080), false);

                  for (String criterion : progress.method_731()) {
                     unlockedCount++;
                     tracker.method_12878(advancement, criterion);
                     player.method_7353(
                        Text.method_43470("§a✓ 解锁条件 " + unlockedCount + "/" + totalCriteria + ": " + criterion).method_27692(Formatting.field_1060), false
                     );
                  }

                  player.method_7353(Text.method_43470("§6§l成就解锁完成: 终结灵异时代§r").method_27692(Formatting.field_1065), false);
                  player.method_7353(Text.method_43470("§7（通过指令强制解锁）§r").method_27692(Formatting.field_1080), false);
                  player.method_7353(Text.method_43470("§a✓ 总共解锁了 " + unlockedCount + " 个条件§r").method_27692(Formatting.field_1060), false);
               } else {
                  player.method_7353(Text.method_43470("§c成就已经解锁，无需操作").method_27692(Formatting.field_1061), false);
               }
            } else {
               player.method_7353(Text.method_43470("§c找不到指定的成就: " + advancementId).method_27692(Formatting.field_1061), false);
            }
         } catch (Exception e) {
            throw new RuntimeException("强制解锁成就失败: " + e.getMessage(), e);
         }
      }
   }

   public static int executeCheck(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();

      try {
         boolean hasAdvancement = AdvancementManager.hasAdvancement(player, "smfs:end_ghost_era");
         if (hasAdvancement) {
            player.method_7353(Text.method_43470("§6§l成就状态: §a已解锁").method_27692(Formatting.field_1065), false);
            player.method_7353(Text.method_43470("§7成就: 终结灵异时代").method_27692(Formatting.field_1080), false);
         } else {
            player.method_7353(Text.method_43470("§6§l成就状态: §c未解锁").method_27692(Formatting.field_1065), false);
            player.method_7353(Text.method_43470("§7成就: 终结灵异时代").method_27692(Formatting.field_1080), false);
            player.method_7353(Text.method_43470("§e解锁条件: 驱逐所有厉鬼").method_27692(Formatting.field_1054), false);
         }

         return 1;
      } catch (Exception e) {
         player.method_7353(Text.method_43470("§c成就检查失败: " + e.getMessage()).method_27692(Formatting.field_1061), false);
         return 0;
      }
   }

   public static int executeInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();

      try {
         player.method_7353(Text.method_43470("§6=== 成就系统信息 ===").method_27692(Formatting.field_1065), false);
         player.method_7353(Text.method_43470("§a可用命令:").method_27692(Formatting.field_1060), false);
         player.method_7353(Text.method_43470("§f/xie advancement test §7- 测试成就解锁逻辑").method_27692(Formatting.field_1068), false);
         player.method_7353(Text.method_43470("§f/xie advancement check §7- 检查成就状态").method_27692(Formatting.field_1068), false);
         player.method_7353(Text.method_43470("§f/xie advancement force §7- §l强制完成成就（测试弹窗）§r").method_27692(Formatting.field_1068), false);
         player.method_7353(Text.method_43470("§f/xie advancement info §7- 显示此帮助信息").method_27692(Formatting.field_1068), false);
         player.method_7353(Text.method_43470("§6===================").method_27692(Formatting.field_1065), false);
         return 1;
      } catch (Exception e) {
         player.method_7353(Text.method_43470("§c显示信息失败: " + e.getMessage()).method_27692(Formatting.field_1061), false);
         return 0;
      }
   }
}
