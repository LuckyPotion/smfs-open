package com.xie.smfs.client.data;

import com.xie.smfs.client.screen.GhostControlScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

public class ClientDataManager {
   private static final String DATA_FILE_NAME = "smfs_client_data.nbt";
   private static NbtCompound clientData;
   private static long ghostDreamRemainingTime = 0L;
   private static boolean inGhostDream = false;

   public static void init() {
      clientData = loadClientData();
      if (clientData == null) {
         clientData = new NbtCompound();
         clientData.putInt("tutorial_view_count", 0);
         clientData.putBoolean("first_night_curse_triggered", false);
         clientData.putBoolean("has_received_starter_items", false);
         clientData.putString("shown_events", "[]");
         clientData.putBoolean("has_seen_play_notice", false);
         saveClientData();
      }
   }

   private static NbtCompound loadClientData() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client == null) {
         return null;
      }

      File dataFile = new File(client.runDirectory, "smfs_client_data.nbt");
      if (!dataFile.exists()) {
         return null;
      }

      try (FileInputStream fis = new FileInputStream(dataFile)) {
         return NbtIo.readCompressed(fis);
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static void saveClientData() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && clientData != null) {
         File dataFile = new File(client.runDirectory, "smfs_client_data.nbt");

         try (FileOutputStream fos = new FileOutputStream(dataFile)) {
            NbtIo.writeCompressed(clientData, fos);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   public static int getTutorialViewCount() {
      return clientData == null ? 0 : clientData.getInt("tutorial_view_count");
   }

   public static void incrementTutorialViewCount() {
      if (clientData != null) {
         clientData.putInt("tutorial_view_count", getTutorialViewCount() + 1);
         saveClientData();
      }
   }

   public static String getShownEvents() {
      return clientData == null ? "[]" : clientData.getString("shown_events");
   }

   public static void setShownEvents(String events) {
      if (clientData != null) {
         clientData.putString("shown_events", events);
         saveClientData();
      }
   }

   public static void syncPlayerData(NbtCompound data) {
      if (clientData == null) {
         init();
      }

      for (String key : data.getKeys()) {
         clientData.put(key, data.get(key).copy());
      }

      saveClientData();
      MinecraftClient.getInstance().execute(() -> {
         if (MinecraftClient.getInstance().currentScreen instanceof GhostControlScreen) {
            GhostControlScreen var0 = (GhostControlScreen)MinecraftClient.getInstance().currentScreen;
         }
      });
   }

   public static boolean isFirstNightCurseTriggered() {
      return clientData == null ? false : clientData.getBoolean("first_night_curse_triggered");
   }

   public static void setFirstNightCurseTriggered(boolean triggered) {
      if (clientData != null) {
         clientData.putBoolean("first_night_curse_triggered", triggered);
         saveClientData();
      }
   }

   public static boolean hasReceivedStarterItems() {
      return clientData == null ? false : clientData.getBoolean("has_received_starter_items");
   }

   public static void setHasReceivedStarterItems(boolean received) {
      if (clientData != null) {
         clientData.putBoolean("has_received_starter_items", received);
         saveClientData();
      }
   }

   public static boolean hasSeenPlayNotice() {
      return clientData == null ? false : clientData.getBoolean("has_seen_play_notice");
   }

   public static void setHasSeenPlayNotice(boolean seen) {
      if (clientData != null) {
         clientData.putBoolean("has_seen_play_notice", seen);
         saveClientData();
      }
   }

   public static void updateGhostDreamTime(long remainingTime) {
      ghostDreamRemainingTime = remainingTime;
      inGhostDream = remainingTime > 0L;
   }

   public static long getGhostDreamRemainingTime() {
      return ghostDreamRemainingTime;
   }

   public static boolean isInGhostDream() {
      return inGhostDream;
   }

   public static void resetGhostDream() {
      ghostDreamRemainingTime = 0L;
      inGhostDream = false;
   }

   public static String getFormattedGhostDreamTime() {
      if (!inGhostDream) {
         return "08:00";
      }

      long totalTicks = 12000L;
      long elapsedTicks = totalTicks - ghostDreamRemainingTime;
      long gameTimeOfDay = (20000L + elapsedTicks) % 24000L;
      int hours = (int)(gameTimeOfDay / 1000L);
      int minutes = (int)(gameTimeOfDay % 1000L / 16.67);
      if (hours >= 24) {
         hours -= 24;
      }

      return String.format("%02d:%02d", hours, minutes);
   }
}
