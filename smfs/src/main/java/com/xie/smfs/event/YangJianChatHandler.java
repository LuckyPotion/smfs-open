package com.xie.smfs.event;

import com.xie.smfs.entity.master.YangJianEntity;
import com.xie.smfs.registry.ModEntities;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowChatMessage;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowCommandMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class YangJianChatHandler {
   private static final String TRIGGER_WORD = "杨戬";
   private static final int SPAWN_HEIGHT = 3;

   public static void register() {
      ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((AllowChatMessage)(message, sender, params) -> {
         String content = message.getSignedContent();
         if (content.trim().equalsIgnoreCase("杨戬")) {
            summonYangJian(sender);
         }

         return true;
      });
      ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((AllowCommandMessage)(message, sender, params) -> {
         String content = message.getSignedContent();
         if (content.trim().equalsIgnoreCase("杨戬")) {
            ServerPlayerEntity player = sender.getPlayer();
            if (player != null) {
               summonYangJian(player);
            }
         }

         return true;
      });
   }

   private static void summonYangJian(ServerPlayerEntity player) {
      World world = player.getWorld();
      if (!world.isClient()) {
         if (!player.hasPermissionLevel(2)) {
            player.sendMessage(Text.literal("§c只有房主才能召唤杨戬！"), false);
         } else {
            BlockPos playerPos = player.getBlockPos();
            BlockPos spawnPos = playerPos.up(3);

            try {
               YangJianEntity yangJian = new YangJianEntity(ModEntities.YANG_JIAN, world);
               yangJian.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0F);
               world.spawnEntity(yangJian);
            } catch (Exception e) {
               player.sendMessage(Text.literal("§c召唤杨戬时出现错误！"), false);
            }
         }
      }
   }
}
