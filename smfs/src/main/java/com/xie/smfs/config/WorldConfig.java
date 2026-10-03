package com.xie.smfs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class WorldConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path DEFAULT_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("smfs_world_default.json");
   public static final int DIFFICULTY_LOW = 0;
   public static final int DIFFICULTY_MEDIUM = 1;
   public static final int DIFFICULTY_HIGH = 2;
   public static final String ENDING_MODE_OPEN = "open";
   public static final String ENDING_MODE_LINEAR = "linear";
   public int modDifficulty = 1;
   public String endingMode = "open";
   public boolean enableCityGeneration = false;
   public String lostCityDefaultProfile = "safe";
   private transient World world;

   private WorldConfig() {
   }

   public static WorldConfig getInstance(World world) {
      return world != null && !world.isClient ? loadConfig(world) : loadDefault();
   }

   private static Path getConfigPath(World world) {
      MinecraftServer server = world.getServer();
      if (server == null) {
         return DEFAULT_CONFIG_PATH;
      }

      Path worldDir = server.getRunDirectory().toPath();
      if (!server.isDedicated()) {
         worldDir = worldDir.resolve("saves");
      }

      worldDir = worldDir.resolve(server.getSaveProperties().getLevelName());
      return worldDir.resolve("smfs_world.json");
   }

   private static WorldConfig loadConfig(World world) {
      Path configPath = getConfigPath(world);
      if (Files.exists(configPath)) {
         try {
            String json = Files.readString(configPath);
            WorldConfig config = GSON.fromJson(json, WorldConfig.class);
            config.world = world;
            return config;
         } catch (IOException e) {
            System.err.println("加载世界配置失败，使用默认配置: " + e.getMessage());
         }
      }

      WorldConfig config = loadDefault();
      config.world = world;
      saveConfig(config);
      return config;
   }

   public static WorldConfig loadDefault() {
      if (Files.exists(DEFAULT_CONFIG_PATH)) {
         try {
            String json = Files.readString(DEFAULT_CONFIG_PATH);
            return GSON.fromJson(json, WorldConfig.class);
         } catch (IOException e) {
            System.err.println("加载默认世界配置失败: " + e.getMessage());
         }
      }

      return new WorldConfig();
   }

   public void saveDefault() {
      try {
         Files.createDirectories(DEFAULT_CONFIG_PATH.getParent());
         Files.writeString(DEFAULT_CONFIG_PATH, GSON.toJson(this));
      } catch (IOException e) {
         System.err.println("保存默认世界配置失败: " + e.getMessage());
      }
   }

   public void save() {
      saveConfig(this);
   }

   private static void saveConfig(WorldConfig config) {
      if (config.world != null) {
         try {
            Path configPath = getConfigPath(config.world);
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(config));
         } catch (IOException e) {
            System.err.println("保存世界配置失败: " + e.getMessage());
         }
      }
   }

   public void validate() {
      this.modDifficulty = Math.max(0, Math.min(2, this.modDifficulty));
      if (!"linear".equals(this.endingMode) && !"open".equals(this.endingMode)) {
         this.endingMode = "open";
      }
   }
}
