package com.xie.smfs.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.manager.GhostSpawnManager;
import com.xie.smfs.util.GhostUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GhostTimerCommand {
   public static int execute(CommandContext<ServerCommandSource> context, String dimensionKey) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

      try {
         Map<EntityType<?>, Integer> ghostTimers = ModEvents.getAllGhostSpawnTimers(dimensionKey);
         if (ghostTimers.isEmpty()) {
            player.sendMessage(Text.literal("§c维度 " + dimensionKey + " 当前没有活跃的鬼刷新计时器").formatted(Formatting.RED), false);
            return 1;
         }

         List<Entry<EntityType<?>, Integer>> sortedEntries = new ArrayList<>(ghostTimers.entrySet());
         sortedEntries.sort((a, b) -> {
            int spawnIntervalA = GhostSpawnManager.getSpawnInterval(a.getKey());
            int spawnIntervalB = GhostSpawnManager.getSpawnInterval(b.getKey());
            int remainingTicksA = spawnIntervalA - a.getValue();
            int remainingTicksB = spawnIntervalB - b.getValue();
            return Integer.compare(remainingTicksB, remainingTicksA);
         });
         player.sendMessage(Text.literal("§6=== " + dimensionKey + " 鬼刷新冷却计时器 ===").formatted(Formatting.GOLD), false);

         for (Entry<EntityType<?>, Integer> entry : sortedEntries) {
            EntityType<?> ghostType = entry.getKey();
            int currentTimer = entry.getValue();
            int spawnInterval = GhostSpawnManager.getSpawnInterval(ghostType);
            int remainingTicks = spawnInterval - currentTimer;
            String timeFormat = formatTicksToTime(remainingTicks);
            String ghostName = GhostUtils.getGhostDisplayName(ghostType);
            Formatting color = getColorByRemainingTime(remainingTicks, spawnInterval);
            player.sendMessage(Text.literal(String.format("§f%s: §e%s 剩余", ghostName, timeFormat)).formatted(color), false);
         }

         player.sendMessage(Text.literal("§6========================").formatted(Formatting.GOLD), false);
         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c获取鬼刷新计时器信息失败: " + e.getMessage()).formatted(Formatting.RED), false);
         return 0;
      }
   }

   private static String formatTicksToTime(int ticks) {
      if (ticks <= 0) {
         return "0秒";
      } else {
         int seconds = ticks / 20;
         int minutes = seconds / 60;
         int hours = minutes / 60;
         seconds %= 60;
         minutes %= 60;
         if (hours > 0) {
            return String.format("%d小时%d分%d秒", hours, minutes, seconds);
         } else {
            return minutes > 0 ? String.format("%d分%d秒", minutes, seconds) : String.format("%d秒", seconds);
         }
      }
   }

   private static Formatting getColorByRemainingTime(int remainingTicks, int totalInterval) {
      if (totalInterval <= 0) {
         return Formatting.GREEN;
      } else {
         double ratio = (double)remainingTicks / totalInterval;
         if (ratio <= 0.1) {
            return Formatting.GREEN;
         } else if (ratio <= 0.3) {
            return Formatting.YELLOW;
         } else {
            return ratio <= 0.6 ? Formatting.GOLD : Formatting.RED;
         }
      }
   }
}
