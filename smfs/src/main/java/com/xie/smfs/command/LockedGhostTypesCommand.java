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
         (LiteralArgumentBuilder)CommandManager.literal("xie").then(CommandManager.literal("locked_ghosts").executes(LockedGhostTypesCommand::execute))
      );
   }

   public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayerOrThrow();

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
            player.sendMessage(Text.literal("§a当前没有被锁死的鬼类型").formatted(Formatting.GREEN), false);
            showAdvancementProgress(player, lockedGhostTypes, filteredGhostTypes);
            return 1;
         }

         player.sendMessage(Text.literal("§6=== 被锁死的鬼类型列表 ===").formatted(Formatting.GOLD), false);
         player.sendMessage(Text.literal("§7（这些鬼类型只能在灵异世界生成）").formatted(Formatting.GRAY), false);
         int count = 1;

         for (EntityType<?> ghostType : lockedGhostTypes) {
            if (AdvancementManager.isGhostEntityType(ghostType)) {
               String ghostName = GhostUtils.getGhostDisplayName(ghostType);
               player.sendMessage(Text.literal(String.format("§f%d. §e%s", count, ghostName)).formatted(Formatting.YELLOW), false);
               count++;
            }
         }

         int actualLockedCount = 0;

         for (EntityType<?> ghostType : lockedGhostTypes) {
            if (AdvancementManager.isGhostEntityType(ghostType)) {
               actualLockedCount++;
            }
         }

         player.sendMessage(Text.literal("§6总计: §e" + actualLockedCount + " §6个被锁死的鬼类型").formatted(Formatting.GOLD), false);
         Set<EntityType<?>> missingGhostTypes = new HashSet<>(filteredGhostTypes);
         missingGhostTypes.removeAll(lockedGhostTypes);
         if (!missingGhostTypes.isEmpty()) {
            player.sendMessage(Text.literal("§6=== 缺少的鬼类型列表 ===").formatted(Formatting.GOLD), false);
            player.sendMessage(Text.literal("§7（这些鬼类型还需要被锁死）").formatted(Formatting.GRAY), false);
            int missingCount = 1;

            for (EntityType<?> ghostType : missingGhostTypes) {
               String ghostName = GhostUtils.getGhostDisplayName(ghostType);
               player.sendMessage(Text.literal(String.format("§f%d. §c%s", missingCount, ghostName)).formatted(Formatting.RED), false);
               missingCount++;
            }

            player.sendMessage(Text.literal("§6总计: §c" + missingGhostTypes.size() + " §6个需要锁死的鬼类型").formatted(Formatting.GOLD), false);
         }

         showAdvancementProgress(player, lockedGhostTypes, filteredGhostTypes);
         player.sendMessage(Text.literal("§6============================").formatted(Formatting.GOLD), false);
         return 1;
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c获取被锁死鬼类型信息失败: " + e.getMessage()).formatted(Formatting.RED), false);
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
         player.sendMessage(Text.literal("§6=== 成就进度信息 ===").formatted(Formatting.GOLD), false);
         if (achievementUnlocked) {
            player.sendMessage(Text.literal("§a✓ 成就已解锁: 终结灵异时代").formatted(Formatting.GREEN), false);
            player.sendMessage(Text.literal("§7所有厉鬼都已被驱逐，灵异时代终结！").formatted(Formatting.GRAY), false);
         } else {
            player.sendMessage(Text.literal("§e成就进度: 终结灵异时代").formatted(Formatting.YELLOW), false);
            player.sendMessage(
               Text.literal(String.format("§f进度: §e%d§f/§a%d §f(§6%.1f%%§f)", lockedCount, totalGhostTypes, progressPercentage)).formatted(Formatting.WHITE),
               false
            );
            player.sendMessage(Text.literal("§7解锁条件: 所有厉鬼都被驱逐").formatted(Formatting.GRAY), false);
            int remaining = totalGhostTypes - lockedCount;
            if (remaining > 0) {
               player.sendMessage(Text.literal(String.format("§c还需锁死 §e%d §c个鬼类型", remaining)).formatted(Formatting.RED), false);
            } else {
               player.sendMessage(Text.literal("§a所有鬼类型已锁死，成就即将解锁！").formatted(Formatting.GREEN), false);
            }
         }

         player.sendMessage(Text.literal("§6=====================").formatted(Formatting.GOLD), false);
      } catch (Exception e) {
         player.sendMessage(Text.literal("§c获取成就进度信息失败: " + e.getMessage()).formatted(Formatting.RED), false);
      }
   }
}
