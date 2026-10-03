package com.xie.smfs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;

public class ClientModConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("smfs_personal.json");
   private static ClientModConfig INSTANCE;
   public Map<UUID, ClientModConfig.PlayerSettings> playerSettings = new HashMap<>();

   private ClientModConfig() {
   }

   public static ClientModConfig getInstance() {
      if (INSTANCE == null) {
         INSTANCE = load();
      }

      return INSTANCE;
   }

   public boolean showDamageText(UUID playerUuid) {
      return playerUuid == null ? true : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).showDamageText;
   }

   public void setShowDamageText(UUID playerUuid, boolean value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).showDamageText = value;
      this.save();
   }

   public boolean showDamageTakenText(UUID playerUuid) {
      return playerUuid == null ? true : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).showDamageTakenText;
   }

   public void setShowDamageTakenText(UUID playerUuid, boolean value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).showDamageTakenText = value;
      this.save();
   }

   public float getScreenShakeIntensity(UUID playerUuid) {
      return playerUuid == null ? 1.0F : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).screenShakeIntensity;
   }

   public void setScreenShakeIntensity(UUID playerUuid, float value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).screenShakeIntensity = Math.max(0.0F, Math.min(1.0F, value));
      this.save();
   }

   public String getMusicBoxMode(UUID playerUuid) {
      return playerUuid == null ? "default" : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).musicBoxMode;
   }

   public void setMusicBoxMode(UUID playerUuid, String value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).musicBoxMode = value;
      this.save();
   }

   public boolean isCustomGhostDomainLevelEnabled(UUID playerUuid) {
      return playerUuid == null
         ? false
         : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).customGhostDomainLevelEnabled;
   }

   public void setCustomGhostDomainLevelEnabled(UUID playerUuid, boolean value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).customGhostDomainLevelEnabled = value;
      this.save();
   }

   public int getGhostDomainRange(UUID playerUuid) {
      return playerUuid == null ? -1 : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).ghostDomainRange;
   }

   public void setGhostDomainRange(UUID playerUuid, int value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).ghostDomainRange = Math.max(5, value);
      this.save();
   }

   public int getGhostDomainFogRange(UUID playerUuid) {
      return playerUuid == null ? -1 : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).ghostDomainFogRange;
   }

   public void setGhostDomainFogRange(UUID playerUuid, int value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).ghostDomainFogRange = value;
      this.save();
   }

   public boolean isDamageWhitelistEnabled(UUID playerUuid) {
      return playerUuid == null ? false : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).damageWhitelistEnabled;
   }

   public void setDamageWhitelistEnabled(UUID playerUuid, boolean value) {
      this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).damageWhitelistEnabled = value;
      this.save();
   }

   public boolean isDamageWhitelisted(UUID playerUuid, String targetName) {
      if (playerUuid == null || targetName == null) {
         return false;
      } else {
         return !this.isDamageWhitelistEnabled(playerUuid)
            ? false
            : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).damageWhitelist.contains(targetName);
      }
   }

   public Set<String> getDamageWhitelist(UUID playerUuid) {
      return playerUuid == null ? new HashSet<>() : this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).damageWhitelist;
   }

   public void addDamageWhitelist(UUID playerUuid, String targetName) {
      if (playerUuid != null && targetName != null) {
         this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).damageWhitelist.add(targetName);
         this.save();
      }
   }

   public void removeDamageWhitelist(UUID playerUuid, String targetName) {
      if (playerUuid != null && targetName != null) {
         this.playerSettings.computeIfAbsent(playerUuid, k -> new ClientModConfig.PlayerSettings()).damageWhitelist.remove(targetName);
         this.save();
      }
   }

   private static ClientModConfig load() {
      if (Files.exists(CONFIG_PATH)) {
         try {
            return GSON.fromJson(Files.readString(CONFIG_PATH), ClientModConfig.class);
         } catch (Exception e) {
            System.err.println("[SMFS] 加载个人配置失败: " + e.getMessage());
         }
      }

      return new ClientModConfig();
   }

   public void save() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         Files.writeString(CONFIG_PATH, GSON.toJson(this));
      } catch (IOException e) {
         System.err.println("[SMFS] 保存个人配置失败: " + e.getMessage());
      }
   }

   public static class PlayerSettings {
      public boolean showDamageText = true;
      public boolean showDamageTakenText = true;
      public float screenShakeIntensity = 1.0F;
      public String musicBoxMode = "default";
      public boolean customGhostDomainLevelEnabled = false;
      public int ghostDomainRange = -1;
      public int ghostDomainFogRange = -1;
      public boolean damageWhitelistEnabled = false;
      public Set<String> damageWhitelist = new HashSet<>();
   }
}
