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
         String content = message.method_44862();
         if (content.trim().equalsIgnoreCase("杨戬")) {
            summonYangJian(sender);
         }

         return true;
      });
      ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((AllowCommandMessage)(message, sender, params) -> {
         String content = message.method_44862();
         if (content.trim().equalsIgnoreCase("杨戬")) {
            ServerPlayerEntity player = sender.method_44023();
            if (player != null) {
               summonYangJian(player);
            }
         }

         return true;
      });
   }

   private static void summonYangJian(ServerPlayerEntity player) {
      World world = player.method_37908();
      if (!world.method_8608()) {
         if (!player.method_5687(2)) {
            player.method_7353(Text.method_43470("§c只有房主才能召唤杨戬！"), false);
         } else {
            BlockPos playerPos = player.method_24515();
            BlockPos spawnPos = playerPos.method_10086(3);

            try {
               YangJianEntity yangJian = new YangJianEntity(ModEntities.YANG_JIAN, world);
               yangJian.method_5808(spawnPos.method_10263() + 0.5, spawnPos.method_10264(), spawnPos.method_10260() + 0.5, player.method_36454(), 0.0F);
               world.method_8649(yangJian);
            } catch (Exception e) {
               player.method_7353(Text.method_43470("§c召唤杨戬时出现错误！"), false);
            }
         }
      }
   }
}
