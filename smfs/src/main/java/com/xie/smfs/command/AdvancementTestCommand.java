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
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         AdvancementManager.checkAndUnlockEndGhostEra(player);
         player.sendMessage(Text.literal("§a已触发成就检查逻辑").formatted(Formatting.GREEN), false);
         player.sendMessage(Text.literal("§7如果满足条件，成就将自动解锁").formatted(Formatting.GRAY), false);
         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c成就测试失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }

   public static int executeForce(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         boolean alreadyUnlocked = AdvancementManager.hasAdvancement(player, "smfs:end_ghost_era");
         if (alreadyUnlocked) {
            player.sendMessage(Text.literal("§c成就已经解锁，无需强制完成").formatted(Formatting.RED), false);
            return 0;
         } else {
            forceUnlockAdvancement(player, "smfs:end_ghost_era");
            player.sendMessage(Text.literal("§a§l强制解锁成功！").formatted(Formatting.GREEN), false);
            player.sendMessage(Text.literal("§7成就弹窗应该已经显示").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("§7使用 /xie advancement check 确认成就状态").formatted(Formatting.GRAY), false);
            return 1;
         }
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c强制解锁失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }

   private static void forceUnlockAdvancement(ServerPlayerEntity player, String advancementId) {
      if (player != null && advancementId != null) {
         try {
            PlayerAdvancementTracker tracker = player.getAdvancementTracker();
            Advancement advancement = player.getServer().getAdvancementLoader().get(new Identifier(advancementId));
            if (advancement != null) {
               AdvancementProgress progress = tracker.getProgress(advancement);
               if (!progress.isDone()) {
                  Iterable<String> unobtainedCriteria = progress.getUnobtainedCriteria();
                  int totalCriteria = 0;
                  int unlockedCount = 0;

                  for (String criterion : unobtainedCriteria) {
                     totalCriteria++;
                  }

                  player.sendMessage(Text.literal("§e开始强制解锁成就...").formatted(Formatting.YELLOW), false);
                  player.sendMessage(Text.literal("§7需要解锁的条件数量: " + totalCriteria).formatted(Formatting.GRAY), false);

                  for (String criterion : progress.getUnobtainedCriteria()) {
                     unlockedCount++;
                     tracker.grantCriterion(advancement, criterion);
                     player.sendMessage(Text.literal("§a✓ 解锁条件 " + unlockedCount + "/" + totalCriteria + ": " + criterion).formatted(Formatting.GREEN), false);
                  }

                  player.sendMessage(Text.literal("§6§l成就解锁完成: 终结灵异时代§r").formatted(Formatting.GOLD), false);
                  player.sendMessage(Text.literal("§7（通过指令强制解锁）§r").formatted(Formatting.GRAY), false);
                  player.sendMessage(Text.literal("§a✓ 总共解锁了 " + unlockedCount + " 个条件§r").formatted(Formatting.GREEN), false);
               } else {
                  player.sendMessage(Text.literal("§c成就已经解锁，无需操作").formatted(Formatting.RED), false);
               }
            } else {
               player.sendMessage(Text.literal("§c找不到指定的成就: " + advancementId).formatted(Formatting.RED), false);
            }
         } catch (Exception e) {
            throw new RuntimeException("强制解锁成就失败: " + e.getMessage(), e);
         }
      }
   }

   public static int executeCheck(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         boolean hasAdvancement = AdvancementManager.hasAdvancement(player, "smfs:end_ghost_era");
         if (hasAdvancement) {
            player.sendMessage(Text.literal("§6§l成就状态: §a已解锁").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("§7成就: 终结灵异时代").formatted(Formatting.GRAY), false);
         } else {
            player.sendMessage(Text.literal("§6§l成就状态: §c未解锁").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("§7成就: 终结灵异时代").formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("§e解锁条件: 驱逐所有厉鬼").formatted(Formatting.YELLOW), false);
         }

         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c成就检查失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }

   public static int executeInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         player.sendMessage(Text.literal("§6=== 成就系统信息 ===").formatted(Formatting.GOLD), false);
         player.sendMessage(Text.literal("§a可用命令:").formatted(Formatting.GREEN), false);
         player.sendMessage(Text.literal("§f/xie advancement test §7- 测试成就解锁逻辑").formatted(Formatting.WHITE), false);
         player.sendMessage(Text.literal("§f/xie advancement check §7- 检查成就状态").formatted(Formatting.WHITE), false);
         player.sendMessage(Text.literal("§f/xie advancement force §7- §l强制完成成就（测试弹窗）§r").formatted(Formatting.WHITE), false);
         player.sendMessage(Text.literal("§f/xie advancement info §7- 显示此帮助信息").formatted(Formatting.WHITE), false);
         player.sendMessage(Text.literal("§6===================").formatted(Formatting.GOLD), false);
         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c显示信息失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }
}
