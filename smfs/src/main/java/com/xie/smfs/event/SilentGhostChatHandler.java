package com.xie.smfs.event;

import com.xie.smfs.entity.ghost.SilentGhostEntity;
import com.xie.smfs.registry.ModEntities;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowChatMessage;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.AllowCommandMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SilentGhostChatHandler {
   private static final String TRIGGER_WORD = "鬼";
   private static final int SPAWN_HEIGHT = 2;

   public static void register() {
      ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((AllowChatMessage)(message, sender, params) -> {
         String content = message.getSignedContent();
         if (content.contains("鬼")) {
            summonSilentGhost(sender);
         }

         return true;
      });
      ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((AllowCommandMessage)(message, sender, params) -> {
         String content = message.getSignedContent();
         if (content.contains("鬼")) {
            ServerPlayerEntity player = sender.getPlayer();
            if (player != null) {
               summonSilentGhost(player);
            }
         }

         return true;
      });
   }

   private static void summonSilentGhost(ServerPlayerEntity player) {
      World world = player.getWorld();
      if (!world.isClient()) {
         BlockPos playerPos = player.getBlockPos();
         BlockPos spawnPos = playerPos.up(2);

         try {
            SilentGhostEntity silentGhost = new SilentGhostEntity(ModEntities.SILENT_GHOST, world);
            silentGhost.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYaw(), 0.0F);
            world.spawnEntity(silentGhost);
            player.sendMessage(Text.literal("§7你好像触发了某种规律..."), false);
         } catch (Exception e) {
            player.sendMessage(Text.literal("§c召唤静悄悄鬼时出现错误！"), false);
         }
      }
   }
}
