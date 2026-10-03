package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GhostSpawnManager;
import com.xie.smfs.util.GhostUtils;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LockedGhostTypesCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)CommandManager.method_9247("xie").then(CommandManager.method_9247("locked_ghosts").executes(LockedGhostTypesCommand::execute))
      );
   }

   public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_9207();

      try {
         Set<EntityType<?>> lockedGhostTypes = GhostSpawnManager.getLockedGhostTypes();
         Set<EntityType<?>> allGhostTypes = GhostSpawnManager.getSpawnedGhostTypes();
         Set<EntityType<?>> filteredGhostTypes = new HashSet<>();

         for (EntityType<?> ghostType : allGhostTypes) {
            if (AdvancementManager.isGhostEntityType(ghostType)) {
               filteredGhostTypes.add(ghostType);
            }
         }

         if (lockedGhostTypes.isEmpty()) {
            player.method_7353(Text.method_43470("§a当前没有被锁死的鬼类型").method_27692(Formatting.field_1060), false);
            showAdvancementProgress(player, lockedGhostTypes, filteredGhostTypes);
            return 1;
         }

         player.method_7353(Text.method_43470("§6=== 被锁死的鬼类型列表 ===").method_27692(Formatting.field_1065), false);
         player.method_7353(Text.method_43470("§7（这些鬼类型只能在灵异世界生成）").method_27692(Formatting.field_1080), false);
         int count = 1;

         for (EntityType<?> ghostType : lockedGhostTypes) {
            if (AdvancementManager.isGhostEntityType(ghostType)) {
               String ghostName = GhostUtils.getGhostDisplayName(ghostType);
               player.method_7353(Text.method_43470(String.format("§f%d. §e%s", count, ghostName)).method_27692(Formatting.field_1054), false);
               count++;
            }
         }

         int actualLockedCount = 0;

         for (EntityType<?> ghostType : lockedGhostTypes) {
            if (AdvancementManager.isGhostEntityType(ghostType)) {
               actualLockedCount++;
            }
         }

         player.method_7353(Text.method_43470("§6总计: §e" + actualLockedCount + " §6个被锁死的鬼类型").method_27692(Formatting.field_1065), false);
         Set<EntityType<?>> missingGhostTypes = new HashSet<>(filteredGhostTypes);
         missingGhostTypes.removeAll(lockedGhostTypes);
         if (!missingGhostTypes.isEmpty()) {
            player.method_7353(Text.method_43470("§6=== 缺少的鬼类型列表 ===").method_27692(Formatting.field_1065), false);
            player.method_7353(Text.method_43470("§7（这些鬼类型还需要被锁死）").method_27692(Formatting.field_1080), false);
            int missingCount = 1;

            for (EntityType<?> ghostType : missingGhostTypes) {
               String ghostName = GhostUtils.getGhostDisplayName(ghostType);
               player.method_7353(Text.method_43470(String.format("§f%d. §c%s", missingCount, ghostName)).method_27692(Formatting.field_1061), false);
               missingCount++;
            }

            player.method_7353(Text.method_43470("§6总计: §c" + missingGhostTypes.size() + " §6个需要锁死的鬼类型").method_27692(Formatting.field_1065), false);
         }

         showAdvancementProgress(player, lockedGhostTypes, filteredGhostTypes);
         player.method_7353(Text.method_43470("§6============================").method_27692(Formatting.field_1065), false);
         return 1;
      } catch (Exception e) {
         player.method_7353(Text.method_43470("§c获取被锁死鬼类型信息失败: " + e.getMessage()).method_27692(Formatting.field_1061), false);
         return 0;
      }
   }

   private static void showAdvancementProgress(ServerPlayerEntity player, Set<EntityType<?>> lockedGhostTypes, Set<EntityType<?>> filteredGhostTypes) {
      try {
         boolean achievementUnlocked = AdvancementManager.hasAdvancement(player, "smfs:end_ghost_era");
         Set<EntityType<?>> allGhostTypes = GhostSpawnManager.getSpawnedGhostTypes();
         Set<EntityType<?>> advancementFilteredGhostTypes = new HashSet<>();

         for (EntityType<?> ghostType : allGhostTypes) {
            if (AdvancementManager.isGhostEntityType(ghostType)) {
               advancementFilteredGhostTypes.add(ghostType);
            }
         }

         int totalGhostTypes = advancementFilteredGhostTypes.size();
         int lockedCount = 0;

         for (EntityType<?> ghostType : lockedGhostTypes) {
            if (advancementFilteredGhostTypes.contains(ghostType)) {
               lockedCount++;
            }
         }

         double progressPercentage = totalGhostTypes > 0 ? (double)lockedCount / totalGhostTypes * 100.0 : 0.0;
         player.method_7353(Text.method_43470("§6=== 成就进度信息 ===").method_27692(Formatting.field_1065), false);
         if (achievementUnlocked) {
            player.method_7353(Text.method_43470("§a✓ 成就已解锁: 终结灵异时代").method_27692(Formatting.field_1060), false);
            player.method_7353(Text.method_43470("§7所有厉鬼都已被驱逐，灵异时代终结！").method_27692(Formatting.field_1080), false);
         } else {
            player.method_7353(Text.method_43470("§e成就进度: 终结灵异时代").method_27692(Formatting.field_1054), false);
            player.method_7353(
               Text.method_43470(String.format("§f进度: §e%d§f/§a%d §f(§6%.1f%%§f)", lockedCount, totalGhostTypes, progressPercentage))
                  .method_27692(Formatting.field_1068),
               false
            );
            player.method_7353(Text.method_43470("§7解锁条件: 所有厉鬼都被驱逐").method_27692(Formatting.field_1080), false);
            int remaining = totalGhostTypes - lockedCount;
            if (remaining > 0) {
               player.method_7353(Text.method_43470(String.format("§c还需锁死 §e%d §c个鬼类型", remaining)).method_27692(Formatting.field_1061), false);
            } else {
               player.method_7353(Text.method_43470("§a所有鬼类型已锁死，成就即将解锁！").method_27692(Formatting.field_1060), false);
            }
         }

         player.method_7353(Text.method_43470("§6=====================").method_27692(Formatting.field_1065), false);
      } catch (Exception e) {
         player.method_7353(Text.method_43470("§c获取成就进度信息失败: " + e.getMessage()).method_27692(Formatting.field_1061), false);
      }
   }
}
