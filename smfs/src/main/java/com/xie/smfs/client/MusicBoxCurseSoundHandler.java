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
         if (client.field_1724 == null || !client.field_1724.method_5805()) {
            stopAll();
         } else if (forceStopped) {
            if (!client.field_1724.method_6059(ModEffects.MUSIC_BOX_CURSE)) {
               forceStopped = false;
            }
         } else {
            boolean hasCurse = client.field_1724.method_6059(ModEffects.MUSIC_BOX_CURSE);
            boolean isPlaying = defaultSound != null || customPlayer != null;
            if (hasCurse && !isPlaying) {
               client.method_1538().method_4859();
               String mode = ClientModConfig.getInstance().getMusicBoxMode(client.field_1724.method_5667());
               if ("custom".equals(mode)) {
                  customPlayer = new CustomMusicBoxPlayer();
                  if (!customPlayer.start()) {
                     customPlayer = null;
                     defaultSound = new MusicBoxCurseSoundInstance(client.field_1724);
                     client.method_1483().method_4873(defaultSound);
                  }
               } else {
                  defaultSound = new MusicBoxCurseSoundInstance(client.field_1724);
                  client.method_1483().method_4873(defaultSound);
               }
            } else if (!hasCurse && isPlaying) {
               stopAll();
            }
         }
      });
   }

   public static void stopAll() {
      if (defaultSound != null) {
         MinecraftClient.method_1551().method_1483().method_4870(defaultSound);
         defaultSound = null;
      }

      if (customPlayer != null) {
         customPlayer.stop();
         customPlayer = null;
      }
   }

   public static void stopOnDeath() {
      forceStopped = true;
      MinecraftClient.method_1551().method_1483().method_4881();
      defaultSound = null;
      if (customPlayer != null) {
         customPlayer.stop();
         customPlayer = null;
      }
   }
}
