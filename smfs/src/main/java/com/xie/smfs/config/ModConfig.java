package com.xie.smfs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.fabricmc.loader.api.FabricLoader;

public class ModConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("smfs.json");
   private static ModConfig INSTANCE;
   public boolean enableEvilGhosts = true;
   public int evilGhostMaxSpawnRange = 64;
   public int evilGhostMinSpawnRange = 32;
   public boolean allowEvilGhostNaturalDespawn = false;
   public Map<String, GhostRespawnConfig> ghostRespawnConfigs = new HashMap<>();
   public double ghostDomainBaseSize = 64.0;
   public double ghostDomainSizePerLevel = 32.0;
   public boolean enableGhostDomainEffectsOnMobs = true;
   public boolean instantReincarnation = true;
   public boolean keepGhostsAfterMirrorResurrection = true;
   public int bodyEnhancementMode = 1;
   public static final int DIFFICULTY_LOW = 0;
   public static final int DIFFICULTY_MEDIUM = 1;
   public static final int DIFFICULTY_HIGH = 2;
   public double ghostDreamChance = 0.05;
   public double spiritDamagePenetrationThreshold = 0.7;
   public boolean penetrateCreativeMode = false;
   public boolean penetrateSpectatorMode = false;
   public boolean lockAfterTaming = false;
   public boolean directTameGhost = true;
   public boolean enableQuestSystem = true;
   public boolean enablePlayerGhostRealSkin = false;
   public int irisCompatibilityMode = 2;
   public boolean enableEvilGhostRevival = true;
   public boolean loseGhostsOnDeath = true;
   public boolean ghostMasterRevival = true;
   public boolean showActionBarInfo = true;
   public boolean showDamageTakenText = true;
   public boolean hardcoreDeadlockMode = false;
   public boolean enableResentmentSystem = true;
   public boolean enableSanitySystem = false;
   public boolean enablePlayerSpiritDamage = true;
   public boolean enableGhostPatrol = false;
   public boolean enableGhostWatchdog = false;
   public boolean unlockSaveLock = false;
   public boolean aiHumanSkinPaperEnabled = false;
   public String aiHumanSkinPaperApiKey = "";
   public Map<String, Double> ghostMasterDropChances = new HashMap<>();
   private transient List<Consumer<ModConfig>> configReloadListeners = new ArrayList<>();

   public static void initialize() {
      INSTANCE = loadConfig();
   }

   public static ModConfig getInstance() {
      if (INSTANCE == null) {
         initialize();
      }

      return INSTANCE;
   }

   private static ModConfig loadConfig() {
      if (Files.exists(CONFIG_PATH)) {
         try {
            String json = Files.readString(CONFIG_PATH);
            return GSON.fromJson(json, ModConfig.class);
         } catch (IOException e) {
            System.err.println("加载神秘复苏配置失败，使用默认配置: " + e.getMessage());
         }
      }

      ModConfig config = new ModConfig();
      saveConfig(config);
      return config;
   }

   public void save() {
      saveConfig(this);
   }

   private static void saveConfig(ModConfig config) {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         Files.writeString(CONFIG_PATH, GSON.toJson(config));
      } catch (IOException e) {
         System.err.println("保存神秘复苏配置失败: " + e.getMessage());
      }
   }

   public static void reload() {
      ModConfig oldInstance = INSTANCE;
      INSTANCE = loadConfig();
      if (oldInstance != null && oldInstance.configReloadListeners != null) {
         INSTANCE.configReloadListeners = new ArrayList<>(oldInstance.configReloadListeners);
      }

      if (oldInstance != null && oldInstance.configReloadListeners != null) {
         for (Consumer<ModConfig> listener : oldInstance.configReloadListeners) {
            try {
               listener.accept(INSTANCE);
            } catch (Exception e) {
               System.err.println("配置重载监听器执行失败: " + e.getMessage());
            }
         }
      }
   }

   public void resetToDefaults() {
      this.enableEvilGhosts = true;
      this.evilGhostMaxSpawnRange = 64;
      this.evilGhostMinSpawnRange = 32;
      this.allowEvilGhostNaturalDespawn = false;
      this.ghostRespawnConfigs.clear();
      this.addDefaultGhostRespawnConfig("fog_ghost");
      this.addDefaultGhostRespawnConfig("block_ghost");
      this.addDefaultGhostRespawnConfig("lost_ghost");
      this.addDefaultGhostRespawnConfig("crying_ghost");
      this.addDefaultGhostRespawnConfig("suona_ghost");
      this.addDefaultGhostRespawnConfig("gong_ghost");
      this.addDefaultGhostRespawnConfig("player_ghost");
      this.addDefaultGhostRespawnConfig("giant_shadow_ghost");
      this.addDefaultGhostRespawnConfig("food_ghost");
      this.addDefaultGhostRespawnConfig("villager_ghost");
      this.addDefaultGhostRespawnConfig("ditou_ghost");
      this.addDefaultGhostRespawnConfig("taitou_ghost");
      this.addDefaultGhostRespawnConfig("water_ghost");
      this.addDefaultGhostRespawnConfig("trash_ghost");
      this.addDefaultGhostRespawnConfig("box_ghost");
      this.addDefaultGhostRespawnConfig("crop_ghost");
      this.addDefaultGhostRespawnConfig("step_ghost");
      this.addDefaultGhostRespawnConfig("jump_ghost");
      this.addDefaultGhostRespawnConfig("death_sight_ghost");
      this.addDefaultGhostRespawnConfig("untouchable_ghost");
      this.addDefaultGhostRespawnConfig("ghost_pressure");
      this.addDefaultGhostRespawnConfig("ghost_wind");
      this.addDefaultGhostRespawnConfig("ghost_blood");
      this.addDefaultGhostRespawnConfig("wish_ghost");
      this.addDefaultGhostRespawnConfig("qiaomen_ghost");
      this.addDefaultGhostRespawnConfig("ganshi_bride_ghost");
      this.addDefaultGhostRespawnConfig("burn_ghost");
      this.addDefaultGhostRespawnConfig("ghost_merchant");
      this.addDefaultGhostRespawnConfig("silent_ghost");
      this.addDefaultGhostRespawnConfig("plague_ghost");
      this.addDefaultGhostRespawnConfig("mineral_ghost");
      this.addDefaultGhostRespawnConfig("shadow_ghost");
      this.addDefaultGhostRespawnConfig("door_ghost");
      this.addDefaultGhostRespawnConfig("ghost_smoke");
      this.addDefaultGhostRespawnConfig("ghost_officer");
      this.addDefaultGhostRespawnConfig("sneak_ghost");
      this.addDefaultGhostRespawnConfig("clothes_ghost");
      this.addDefaultGhostRespawnConfig("puppet_ghost");
      this.addDefaultGhostRespawnConfig("ghost_shadow_head");
      this.addDefaultGhostRespawnConfig("grave_earth_ghost");
      this.ghostMasterDropChances.clear();
      this.addDefaultGhostMasterDropChance("li_jun", 0.3);
      this.addDefaultGhostMasterDropChance("ye_zhen", 0.3);
      this.addDefaultGhostMasterDropChance("feng_quan", 0.3);
      this.addDefaultGhostMasterDropChance("cao_yang", 0.3);
      this.addDefaultGhostMasterDropChance("fang_shi_min", 0.3);
      this.addDefaultGhostMasterDropChance("yan_li", 0.3);
      this.addDefaultGhostMasterDropChance("li_le_ping", 0.3);
      this.addDefaultGhostMasterDropChance("zhao_kai_ming", 0.3);
      this.addDefaultGhostMasterDropChance("npc1", 0.3);
      this.addDefaultGhostMasterDropChance("npc2", 0.3);
      this.addDefaultGhostMasterDropChance("npc3", 0.3);
      this.addDefaultGhostMasterDropChance("npc4", 0.3);
      this.addDefaultGhostMasterDropChance("npc5", 0.3);
      this.addDefaultGhostMasterDropChance("npc6", 0.3);
      this.ghostDomainBaseSize = 64.0;
      this.ghostDomainSizePerLevel = 32.0;
      this.enableGhostDomainEffectsOnMobs = true;
      this.instantReincarnation = true;
      this.keepGhostsAfterMirrorResurrection = true;
      this.bodyEnhancementMode = 1;
      this.ghostDreamChance = 0.05;
      this.spiritDamagePenetrationThreshold = 0.7;
      this.penetrateCreativeMode = false;
      this.penetrateSpectatorMode = false;
      this.lockAfterTaming = false;
      this.directTameGhost = true;
      this.enableQuestSystem = true;
      this.enablePlayerGhostRealSkin = true;
      this.irisCompatibilityMode = 1;
      this.enableEvilGhostRevival = true;
      this.loseGhostsOnDeath = true;
      this.ghostMasterRevival = true;
      this.showActionBarInfo = true;
      this.showDamageTakenText = true;
      this.enableResentmentSystem = true;
      this.enablePlayerSpiritDamage = true;
      this.unlockSaveLock = false;
      this.save();
   }

   private void addDefaultGhostRespawnConfig(String ghostType) {
      GhostRespawnConfig config = new GhostRespawnConfig(ghostType);
      config.resetToDefaults();
      this.ghostRespawnConfigs.put(ghostType, config);
   }

   private void addDefaultGhostMasterDropChance(String ghostMasterType, double dropChance) {
      this.ghostMasterDropChances.put(ghostMasterType, dropChance);
   }

   public double getGhostMasterDropChance(String ghostMasterType) {
      return this.ghostMasterDropChances.getOrDefault(ghostMasterType, 0.3);
   }

   public void setGhostMasterDropChance(String ghostMasterType, double dropChance) {
      this.ghostMasterDropChances.put(ghostMasterType, dropChance);
      this.save();
   }

   public GhostRespawnConfig getGhostRespawnConfig(String ghostType) {
      if (!this.ghostRespawnConfigs.containsKey(ghostType)) {
         this.addDefaultGhostRespawnConfig(ghostType);
      }

      return this.ghostRespawnConfigs.get(ghostType);
   }

   public void validate() {
      this.evilGhostMaxSpawnRange = Math.max(16, Math.min(512, this.evilGhostMaxSpawnRange));
      this.evilGhostMinSpawnRange = Math.max(8, Math.min(256, this.evilGhostMinSpawnRange));
      if (this.evilGhostMinSpawnRange > this.evilGhostMaxSpawnRange) {
         this.evilGhostMinSpawnRange = this.evilGhostMaxSpawnRange;
      }

      for (GhostRespawnConfig config : this.ghostRespawnConfigs.values()) {
         config.validate();
      }

      for (Entry<String, Double> entry : this.ghostMasterDropChances.entrySet()) {
         double chance = entry.getValue();
         if (chance < 0.0) {
            chance = 0.0;
         }

         if (chance > 1.0) {
            chance = 1.0;
         }

         this.ghostMasterDropChances.put(entry.getKey(), chance);
      }

      this.ghostDomainBaseSize = Math.max(1.0, Math.min(100.0, this.ghostDomainBaseSize));
      this.ghostDomainSizePerLevel = Math.max(0.5, Math.min(50.0, this.ghostDomainSizePerLevel));
      this.ghostDreamChance = Math.max(0.0, Math.min(1.0, this.ghostDreamChance));
      this.bodyEnhancementMode = Math.max(0, Math.min(2, this.bodyEnhancementMode));
   }

   public void addConfigReloadListener(Consumer<ModConfig> listener) {
      if (this.configReloadListeners == null) {
         this.configReloadListeners = new ArrayList<>();
      }

      this.configReloadListeners.add(listener);
   }

   public void removeConfigReloadListener(Consumer<ModConfig> listener) {
      if (this.configReloadListeners != null) {
         this.configReloadListeners.remove(listener);
      }
   }

   public void clearConfigReloadListeners() {
      if (this.configReloadListeners != null) {
         this.configReloadListeners.clear();
      }
   }
}
