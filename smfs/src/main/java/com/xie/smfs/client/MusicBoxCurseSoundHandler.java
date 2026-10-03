package com.xie.smfs.client;

import com.xie.smfs.client.sound.CustomMusicBoxPlayer;
import com.xie.smfs.client.sound.MusicBoxCurseSoundInstance;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.registry.ModEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.MinecraftClient;

public class MusicBoxCurseSoundHandler {
   private static MusicBoxCurseSoundInstance defaultSound;
   private static CustomMusicBoxPlayer customPlayer;
   private static boolean forceStopped = false;

   public static void init() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.player == null || !client.player.isAlive()) {
            stopAll();
         } else if (forceStopped) {
            if (!client.player.hasStatusEffect(ModEffects.MUSIC_BOX_CURSE)) {
               forceStopped = false;
            }
         } else {
            boolean hasCurse = client.player.hasStatusEffect(ModEffects.MUSIC_BOX_CURSE);
            boolean isPlaying = defaultSound != null || customPlayer != null;
            if (hasCurse && !isPlaying) {
               client.getMusicTracker().stop();
               String mode = ClientModConfig.getInstance().getMusicBoxMode(client.player.getUuid());
               if ("custom".equals(mode)) {
                  customPlayer = new CustomMusicBoxPlayer();
                  if (!customPlayer.start()) {
                     customPlayer = null;
                     defaultSound = new MusicBoxCurseSoundInstance(client.player);
                     client.getSoundManager().play(defaultSound);
                  }
               } else {
                  defaultSound = new MusicBoxCurseSoundInstance(client.player);
                  client.getSoundManager().play(defaultSound);
               }
            } else if (!hasCurse && isPlaying) {
               stopAll();
            }
         }
      });
   }

   public static void stopAll() {
      if (defaultSound != null) {
         MinecraftClient.getInstance().getSoundManager().stop(defaultSound);
         defaultSound = null;
      }

      if (customPlayer != null) {
         customPlayer.stop();
         customPlayer = null;
      }
   }

   public static void stopOnDeath() {
      forceStopped = true;
      MinecraftClient.getInstance().getSoundManager().stopAll();
      defaultSound = null;
      if (customPlayer != null) {
         customPlayer.stop();
         customPlayer = null;
      }
   }
}
