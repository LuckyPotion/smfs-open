package com.xie.smfs.command;

import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.item.BaseGhostEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostItemCommand {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostItemCommand");

   public static int executeSet(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, int revivalDegree, int level) {
      if (player == null) {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("缺少玩家"));
         return 0;
      } else {
         ItemStack heldItem = player.getMainHandStack();
         if (heldItem.isEmpty()) {
            ((ServerCommandSource)context.getSource()).sendError(Text.literal("没有手持物品"));
            return 0;
         } else if (!(heldItem.getItem() instanceof BaseGhostEyeItem)) {
            ((ServerCommandSource)context.getSource()).sendError(Text.literal("手持物品不是厉鬼"));
            return 0;
         } else {
            NbtCompound nbt = heldItem.getOrCreateNbt();
            int oldRevivalDegree = nbt.contains("revivalDegree") ? nbt.getInt("revivalDegree") : 0;
            int oldLevel = nbt.contains("level") ? nbt.getInt("level") : 1;
            nbt.putInt("revivalDegree", revivalDegree);
            nbt.putInt("level", level);
            heldItem.setNbt(nbt);
            ((ServerCommandSource)context.getSource())
               .sendFeedback(
                  () -> Text.literal("已修改手持物品 NBT - 复苏程度: " + oldRevivalDegree + " -> " + revivalDegree + ", 等级: " + oldLevel + " -> " + level), true
               );
            LOGGER.info(
               "OP {} 修改了玩家 {} 手持厉鬼眼的复苏程度: {} -> {}, 等级: {} -> {}",
               ((ServerCommandSource)context.getSource()).getName(),
               player.getName().getString(),
               oldRevivalDegree,
               revivalDegree,
               oldLevel,
               level
            );
            return 1;
         }
      }
   }

   public static int executeGet(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
      if (player == null) {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("缺少玩家"));
         return 0;
      } else {
         ItemStack heldItem = player.getMainHandStack();
         if (heldItem.isEmpty()) {
            ((ServerCommandSource)context.getSource()).sendError(Text.literal("没有手持物品"));
            return 0;
         } else if (!(heldItem.getItem() instanceof BaseGhostEyeItem)) {
            ((ServerCommandSource)context.getSource()).sendError(Text.literal("手持物品不是厉鬼"));
            return 0;
         } else {
            NbtCompound nbt = heldItem.getOrCreateNbt();
            int revivalDegree = nbt.contains("revivalDegree") ? nbt.getInt("revivalDegree") : 0;
            int level = nbt.contains("level") ? nbt.getInt("level") : 1;
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("手持厉鬼眼 - 复苏程度: " + revivalDegree + ", 等级: " + level), false);
            return 1;
         }
      }
   }
}
