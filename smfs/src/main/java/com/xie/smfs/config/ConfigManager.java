package com.xie.smfs.config;

public class ConfigManager {
   private static boolean initialized = false;

   public static void initialize() {
      if (!initialized) {
         initialized = true;
      }
   }
}
