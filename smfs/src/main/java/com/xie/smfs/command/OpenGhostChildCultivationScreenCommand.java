package com.xie.smfs.command;

import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.event.screen.GhostChildCultivationScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenGhostChildCultivationScreenCommand {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/OpenGhostChildCultivationScreenCommand");

   public static int execute(CommandContext<ServerCommandSource> context) {
      ServerCommandSource source = (ServerCommandSource)context.getSource();
      PlayerEntity player = source.getPlayer();
      if (player != null) {
         try {
            PlayerGhostChildManager.initializeGhostChild(player);
            player.openHandledScreen(new GhostChildCultivationScreenHandler.GhostChildCultivationFactory());
            return 1;
         } catch (Exception e) {
            LOGGER.error("Error opening Ghost Child Cultivation Screen for player: {}", player.getName().getString(), e);
            player.sendMessage(Text.literal("§cError opening Ghost Child Cultivation Screen: " + e.getMessage()), false);
            return 0;
         }
      } else {
         LOGGER.warn("Player is null, cannot open Ghost Child Cultivation Screen");
         return 0;
      }
   }
}
