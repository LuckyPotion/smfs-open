package com.xie.smfs.client;

import com.xie.smfs.registry.ModEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.minecraft.client.sound.SoundInstance;

public class DeafnessClientHandler {
   private static boolean isDeaf = false;

   public static void init() {
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.player != null) {
            isDeaf = client.player.hasStatusEffect(ModEffects.DEAFNESS);
         }
      });
   }

   public static boolean shouldPlaySound(SoundInstance soundInstance) {
      return !isDeaf;
   }

   public static boolean isDeaf() {
      return isDeaf;
   }
}
