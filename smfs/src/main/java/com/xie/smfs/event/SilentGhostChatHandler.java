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
         String content = message.method_44862();
         if (content.contains("鬼")) {
            summonSilentGhost(sender);
         }

         return true;
      });
      ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((AllowCommandMessage)(message, sender, params) -> {
         String content = message.method_44862();
         if (content.contains("鬼")) {
            ServerPlayerEntity player = sender.method_44023();
            if (player != null) {
               summonSilentGhost(player);
            }
         }

         return true;
      });
   }

   private static void summonSilentGhost(ServerPlayerEntity player) {
      World world = player.method_37908();
      if (!world.method_8608()) {
         BlockPos playerPos = player.method_24515();
         BlockPos spawnPos = playerPos.method_10086(2);

         try {
            SilentGhostEntity silentGhost = new SilentGhostEntity(ModEntities.SILENT_GHOST, world);
            silentGhost.method_5808(spawnPos.method_10263() + 0.5, spawnPos.method_10264(), spawnPos.method_10260() + 0.5, player.method_36454(), 0.0F);
            world.method_8649(silentGhost);
            player.method_7353(Text.method_43470("§7你好像触发了某种规律..."), false);
         } catch (Exception e) {
            player.method_7353(Text.method_43470("§c召唤静悄悄鬼时出现错误！"), false);
         }
      }
   }
}
