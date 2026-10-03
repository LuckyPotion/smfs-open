package com.xie.smfs.command;

import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.manager.GhostSpawnManager;
import java.util.Map;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CompleteGhostTimersCommand {
   public static int execute(CommandContext<ServerCommandSource> context) {
      return execute(context, "minecraft:overworld");
   }

   public static int execute(CommandContext<ServerCommandSource> context, String dimensionKey) {
      try {
         Map<EntityType<?>, Integer> ghostTimers = ModEvents.getAllGhostSpawnTimers(dimensionKey);
         if (ghostTimers.isEmpty()) {
            ((ServerCommandSource)context.getSource())
               .method_9226(() -> Text.method_43470("维度 " + dimensionKey + " 当前没有活跃的鬼实体刷新计时器").method_27692(Formatting.field_1054), false);
            return 0;
         }

         int completedCount = 0;

         for (EntityType<?> ghostType : ghostTimers.keySet()) {
            int spawnInterval = GhostSpawnManager.getSpawnInterval(ghostType);
            if (spawnInterval > 0) {
               ModEvents.setGhostSpawnTimer(dimensionKey, ghostType, spawnInterval);
               completedCount++;
            }
         }

         int finalCompletedCount = completedCount;
         if (finalCompletedCount > 0) {
            ((ServerCommandSource)context.getSource())
               .method_9226(
                  () -> Text.method_43470("已立即完成维度 " + dimensionKey + " 的 " + finalCompletedCount + " 个鬼实体的冷却计时器").method_27692(Formatting.field_1060), false
               );
            return 1;
         } else {
            ((ServerCommandSource)context.getSource())
               .method_9226(() -> Text.method_43470("维度 " + dimensionKey + " 没有找到有效的鬼实体生成间隔").method_27692(Formatting.field_1054), false);
            return 0;
         }
      } catch (Exception e) {
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("执行命令时发生错误: " + e.getMessage()).method_27692(Formatting.field_1061));
         return -1;
      }
   }
}
