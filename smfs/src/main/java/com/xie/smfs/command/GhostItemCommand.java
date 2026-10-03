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
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("缺少玩家"));
         return 0;
      } else {
         ItemStack heldItem = player.method_6047();
         if (heldItem.method_7960()) {
            ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("没有手持物品"));
            return 0;
         } else if (!(heldItem.method_7909() instanceof BaseGhostEyeItem)) {
            ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("手持物品不是厉鬼"));
            return 0;
         } else {
            NbtCompound nbt = heldItem.method_7948();
            int oldRevivalDegree = nbt.method_10545("revivalDegree") ? nbt.method_10550("revivalDegree") : 0;
            int oldLevel = nbt.method_10545("level") ? nbt.method_10550("level") : 1;
            nbt.method_10569("revivalDegree", revivalDegree);
            nbt.method_10569("level", level);
            heldItem.method_7980(nbt);
            ((ServerCommandSource)context.getSource())
               .method_9226(
                  () -> Text.method_43470("已修改手持物品 NBT - 复苏程度: " + oldRevivalDegree + " -> " + revivalDegree + ", 等级: " + oldLevel + " -> " + level), true
               );
            LOGGER.info(
               "OP {} 修改了玩家 {} 手持厉鬼眼的复苏程度: {} -> {}, 等级: {} -> {}",
               ((ServerCommandSource)context.getSource()).method_9214(),
               player.method_5477().getString(),
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
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("缺少玩家"));
         return 0;
      } else {
         ItemStack heldItem = player.method_6047();
         if (heldItem.method_7960()) {
            ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("没有手持物品"));
            return 0;
         } else if (!(heldItem.method_7909() instanceof BaseGhostEyeItem)) {
            ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("手持物品不是厉鬼"));
            return 0;
         } else {
            NbtCompound nbt = heldItem.method_7948();
            int revivalDegree = nbt.method_10545("revivalDegree") ? nbt.method_10550("revivalDegree") : 0;
            int level = nbt.method_10545("level") ? nbt.method_10550("level") : 1;
            ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("手持厉鬼眼 - 复苏程度: " + revivalDegree + ", 等级: " + level), false);
            return 1;
         }
      }
   }
}
