package com.xie.smfs.manager;

import com.xie.smfs.Smfs;
import com.xie.smfs.config.GhostRespawnConfig;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostSpawnManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostSpawnManager.class);
   private static final Random RANDOM = new Random();
   private static final Map<EntityType<?>, GhostSpawnManager.GhostSpawnConfig> GHOST_SPAWN_CONFIGS = new HashMap<>();
   private static final Map<EntityType<?>, GhostSpawnManager.GhostSpawnConfig> ADDON_GHOST_SPAWN_CONFIGS = new HashMap<>();
   private static final Map<EntityType<?>, Long> LOCKED_GHOST_TYPES_WITH_TIME = new HashMap<>();
   private static final long LOCK_DURATION_TICK = 72000L;

   private static void reapplyAddonSpawnConfigs() {
      if (!ADDON_GHOST_SPAWN_CONFIGS.isEmpty()) {
         for (Entry<EntityType<?>, GhostSpawnManager.GhostSpawnConfig> entry : ADDON_GHOST_SPAWN_CONFIGS.entrySet()) {
            GHOST_SPAWN_CONFIGS.put(entry.getKey(), entry.getValue());
         }

         LOGGER.info("已恢复 {} 个附属模组鬼魂生成配置", ADDON_GHOST_SPAWN_CONFIGS.size());
      }
   }

   private static void initializeGhostSpawnConfigs() {
      ModConfig config = ModConfig.getInstance();
      if (!config.enableEvilGhosts) {
         GHOST_SPAWN_CONFIGS.clear();
      } else {
         configureGhostSpawn(ModEntities.QIAOMEN_GHOST, "qiaomen_ghost", 5, 60, 24000, true);
         configureGhostSpawn(ModEntities.GHOST_MERCHANT, "ghost_merchant", 6, 60, 24000, false);
         configureGhostSpawn(ModEntities.TAITOU_GHOST, "taitou_ghost", 7, 60, 24000, true);
         configureGhostSpawn(ModEntities.BOX_GHOST, "box_ghost", 8, 60, 30000, true);
         configureGhostSpawn(ModEntities.GHOST_PRESSURE, "ghost_pressure", 9, 70, 31200, true);
         configureGhostSpawn(ModEntities.UNTOUCHABLE_GHOST, "untouchable_ghost", 10, 65, 48000, true);
         configureGhostSpawn(ModEntities.FOG_GHOST, "fog_ghost", 11, 60, 42000, true);
         configureGhostSpawn(ModEntities.DITOU_GHOST, "ditou_ghost", 12, 60, 24000, true);
         configureGhostSpawn(ModEntities.JUMP_GHOST, "jump_ghost", 13, 65, 36000, true);
         configureGhostSpawn(ModEntities.GIANT_SHADOW_GHOST, "giant_shadow_ghost", 14, 70, 21600, true);
         configureGhostSpawn(ModEntities.GANSHI_BRIDE_GHOST, "ganshi_bride_ghost", 15, 65, 38400, true);
         configureGhostSpawn(ModEntities.FOOD_GHOST, "food_ghost", 16, 60, 12000, true);
         configureGhostSpawn(ModEntities.CROP_GHOST, "crop_ghost", 17, 60, 30000, true);
         configureGhostSpawn(ModEntities.STEP_GHOST, "step_ghost", 18, 60, 24000, true);
         configureGhostSpawn(ModEntities.TRASH_GHOST, "trash_ghost", 19, 65, 18000, true);
         configureGhostSpawn(ModEntities.WATER_GHOST, "water_ghost", 20, 65, 24000, true);
         configureGhostSpawn(ModEntities.BLOCK_GHOST, "block_ghost", 21, 60, 14400, true);
         configureGhostSpawn(ModEntities.SUONA_GHOST, "suona_ghost", 22, 65, 33600, true);
         configureGhostSpawn(ModEntities.BURN_GHOST, "burn_ghost", 23, 70, 31200, true);
         configureGhostSpawn(ModEntities.DEATH_SIGHT_GHOST, "death_sight_ghost", 24, 65, 26400, true);
         configureGhostSpawn(ModEntities.CRYING_GHOST, "crying_ghost", 25, 65, 28800, true);
         configureGhostSpawn(ModEntities.GONG_GHOST, "gong_ghost", 26, 60, 24000, true);
         configureGhostSpawn(ModEntities.LOST_GHOST, "lost_ghost", 27, 60, 33600, true);
         configureGhostSpawn(ModEntities.GHOST_WIND, "ghost_wind", 28, 65, 16800, true);
         configureGhostSpawn(ModEntities.VILLAGER_GHOST, "villager_ghost", 29, 60, 24000, true);
         configureGhostSpawn(ModEntities.GOODS_SELLER_GHOST, "goods_seller_ghost", 30, 50, 36000, true);
         configureGhostSpawn(ModEntities.SILENT_GHOST, "silent_ghost", 31, 40, 18000, false);
         configureGhostSpawn(ModEntities.PLAGUE_GHOST, "plague_ghost", 32, 65, 36000, true);
         configureGhostSpawn(ModEntities.MINERAL_GHOST, "mineral_ghost", 33, 60, 30000, true);
         configureGhostSpawn(ModEntities.SHADOW_GHOST, "shadow_ghost", 34, 55, 24000, true);
         configureGhostSpawn(ModEntities.DOOR_GHOST, "door_ghost", 35, 70, 42000, true);
         configureGhostSpawn(ModEntities.GHOST_SMOKE, "ghost_smoke", 36, 50, 18000, true);
         configureGhostSpawn(ModEntities.GHOST_OFFICER, "ghost_officer", 37, 55, 30000, true);
         configureGhostSpawn(ModEntities.SNEAK_GHOST, "sneak_ghost", 38, 60, 24000, true);
         configureGhostSpawn(ModEntities.CLOTHES_GHOST, "clothes_ghost", 39, 60, 24000, true);
         configureGhostSpawn(ModEntities.PUPPET_GHOST, "puppet_ghost", 40, 60, 24000, true);
         configureGhostSpawn(ModEntities.GHOST_SHADOW_HEAD, "ghost_shadow_head", 41, 50, 26400, true);
         configureGhostSpawn(ModEntities.GRAVE_EARTH_GHOST, "grave_earth_ghost", 42, 60, 24000, true);
         configureGhostSpawn(ModEntities.YE_ZHEN, "ye_zhen", 5, 30, 30000, false);
         configureGhostSpawn(ModEntities.FENG_QUAN, "feng_quan", 4, 30, 33600, false);
         configureGhostSpawn(ModEntities.CAO_YANG, "cao_yang", 3, 30, 26400, false);
         configureGhostSpawn(ModEntities.FANG_SHI_MIN, "fang_shi_min", 4, 30, 31200, false);
         configureGhostSpawn(ModEntities.YAN_LI, "yan_li", 10, 30, 24000, false);
         configureGhostSpawn(ModEntities.LI_LE_PING, "li_le_ping", 3, 30, 28800, false);
         configureGhostSpawn(ModEntities.WANG_XIAO_MING, "wang_xiao_ming", 3, 40, 21600, false);
         configureGhostSpawn(ModEntities.CHEN_DOCTOR, "chen_doctor", 1, 40, 19200, false);
         configureGhostSpawn(ModEntities.LI_JUN, "li_jun", 3, 30, 16800, false);
         configureGhostSpawn(ModEntities.ZHAO_KAI_MING, "zhao_kai_ming", 5, 30, 36000, false);
         configureGhostSpawn(ModEntities.NPC1, "npc1", 1, 30, 18000, false);
         configureGhostSpawn(ModEntities.NPC2, "npc2", 2, 30, 18000, false);
         configureGhostSpawn(ModEntities.NPC3, "npc3", 1, 30, 18000, false);
         configureGhostSpawn(ModEntities.NPC4, "npc4", 2, 30, 18000, false);
         configureGhostSpawn(ModEntities.NPC5, "npc5", 1, 30, 18000, false);
         configureGhostSpawn(ModEntities.NPC6, "npc6", 2, 30, 18000, false);
      }
   }

   private static void configureGhostSpawn(
      EntityType<?> entityType, String configKey, int defaultMinDay, int defaultSpawnChance, int defaultSpawnInterval, boolean defaultNightOnly
   ) {
      ModConfig config = ModConfig.getInstance();
      GhostRespawnConfig ghostConfig = config.getGhostRespawnConfig(configKey);
      if (ghostConfig.enabled) {
         int minDay = ghostConfig.respawnDays >= 0 ? ghostConfig.respawnDays : defaultMinDay;
         int spawnChance = (int)(ghostConfig.spawnChance * 100.0);
         if (spawnChance <= 0) {
            spawnChance = defaultSpawnChance;
         }

         int spawnInterval = ghostConfig.spawnCooldown > 0 ? ghostConfig.spawnCooldown : defaultSpawnInterval;
         boolean nightOnly = ghostConfig.nightOnly;
         GHOST_SPAWN_CONFIGS.put(entityType, new GhostSpawnManager.GhostSpawnConfig(minDay, spawnChance, spawnInterval, nightOnly));
      }
   }

   public static void registerGhostSpawnConfig(EntityType<?> entityType, String configKey, int minDay, int spawnChance, int spawnInterval, boolean nightOnly) {
      if (entityType == null) {
         LOGGER.error("尝试注册鬼魂生成配置失败：entityType不能为null");
      } else if (configKey != null && !configKey.trim().isEmpty()) {
         if (minDay < 0) {
            LOGGER.warn("鬼魂生成配置中的minDay为负数，将设置为0：{}", configKey);
            minDay = 0;
         }

         if (spawnChance < 0 || spawnChance > 100) {
            LOGGER.warn("鬼魂生成配置中的spawnChance超出范围(0-100)，将限制在有效范围内：{}", configKey);
            spawnChance = Math.max(0, Math.min(100, spawnChance));
         }

         if (spawnInterval <= 0) {
            LOGGER.warn("鬼魂生成配置中的spawnInterval必须大于0，将设置为默认值24000：{}", configKey);
            spawnInterval = 24000;
         }

         if (GHOST_SPAWN_CONFIGS.containsKey(entityType)) {
            LOGGER.warn("已存在实体类型 {} 的生成配置，将被覆盖", entityType);
         }

         GHOST_SPAWN_CONFIGS.put(entityType, new GhostSpawnManager.GhostSpawnConfig(minDay, spawnChance, spawnInterval, nightOnly));
         ADDON_GHOST_SPAWN_CONFIGS.put(entityType, new GhostSpawnManager.GhostSpawnConfig(minDay, spawnChance, spawnInterval, nightOnly));
         LOGGER.debug("已为附属模组注册鬼魂生成配置：{} ({}), 天数: {}, 几率: {}%, 间隔: {}tick, 夜间生成: {}", configKey, entityType, minDay, spawnChance, spawnInterval, nightOnly);
      } else {
         LOGGER.error("尝试注册鬼魂生成配置失败：configKey不能为空");
      }
   }

   public static void registerGhostSpawnConfig(EntityType<?> entityType, String configKey, int minDay) {
      int spawnChance = 80;
      int spawnInterval = 24000;
      boolean nightOnly = true;
      registerGhostSpawnConfig(entityType, configKey, minDay, spawnChance, spawnInterval, nightOnly);
   }

   public static void unregisterGhostSpawnConfig(EntityType<?> entityType) {
      if (entityType == null) {
         LOGGER.error("尝试移除鬼魂生成配置失败：entityType不能为null");
      } else {
         if (GHOST_SPAWN_CONFIGS.containsKey(entityType)) {
            GHOST_SPAWN_CONFIGS.remove(entityType);
            ADDON_GHOST_SPAWN_CONFIGS.remove(entityType);
            LOGGER.debug("已移除鬼魂生成配置：{}", entityType);
         } else {
            LOGGER.warn("尝试移除不存在的鬼魂生成配置：{}", entityType);
         }
      }
   }

   public static boolean isGhostSpawnConfigRegistered(EntityType<?> entityType) {
      return GHOST_SPAWN_CONFIGS.containsKey(entityType);
   }

   public static int getRegisteredGhostSpawnConfigCount() {
      return GHOST_SPAWN_CONFIGS.size();
   }

   public static boolean canSpawnGhostInOverworld(ServerWorld world, EntityType<?> ghostType) {
      ModConfig config = ModConfig.getInstance();
      LOGGER.debug("主世界：开始检查{}的生成条件，当前世界维度: {}", ghostType, world.method_27983().method_29177());
      LOGGER.debug("主世界：灵异世界维度: {}", Smfs.SPIRIT_REALM_DIMENSION.method_29177());
      LOGGER.debug("主世界：维度比较结果: {}", world.method_27983().equals(Smfs.SPIRIT_REALM_DIMENSION));
      if (!config.enableEvilGhosts) {
         LOGGER.debug("主世界：厉鬼生成失败: 全局开关未启用");
         return false;
      }

      if (ghostType == ModEntities.QIAOMEN_GHOST) {
         LOGGER.debug("主世界：厉鬼生成失败: 敲门鬼由KnockingCurseEffect专门处理生成");
         return false;
      }

      if (isGhostLocked(ghostType) && !world.method_27983().equals(Smfs.SPIRIT_REALM_DIMENSION)) {
         LOGGER.debug("主世界：厉鬼生成失败: 被锁死的鬼类型只能在灵异世界生成");
         return false;
      }

      GhostSpawnManager.GhostSpawnConfig spawnConfig = GHOST_SPAWN_CONFIGS.get(ghostType);
      if (spawnConfig == null) {
         LOGGER.debug("主世界：厉鬼生成失败: 未找到生成配置");
         return false;
      }

      int currentDay = (int)(world.method_8510() / 24000L) + 1;
      LOGGER.debug("主世界：厉鬼生成天数检测：当前第{}天，需要第{}天", currentDay, spawnConfig.minDay);
      if (spawnConfig.minDay > 0 && currentDay < spawnConfig.minDay) {
         LOGGER.debug("主世界：厉鬼生成失败: 天数不足，当前第{}天，需要第{}天", currentDay, spawnConfig.minDay);
         return false;
      }

      if (spawnConfig.isNightOnly && !isNightTime(world)) {
         LOGGER.debug("主世界：厉鬼生成失败: 不是夜间时间");
         return false;
      }

      WorldConfig worldConfig = WorldConfig.getInstance(world);
      if (worldConfig.modDifficulty == 2) {
         LOGGER.debug("主世界：困难模式下厉鬼必定生成（100%概率）");
      } else {
         int randomChance = RANDOM.nextInt(100);
         if (randomChance >= spawnConfig.spawnChance) {
            LOGGER.debug("主世界：厉鬼生成失败: 生成几率不足，随机值{}，需要小于{}", randomChance, spawnConfig.spawnChance);
            return false;
         }
      }

      LOGGER.debug("主世界：条件满足，准备生成{}", ghostType);
      return true;
   }

   public static boolean canSpawnGhostInSpiritRealm(ServerWorld world, EntityType<?> ghostType) {
      ModConfig config = ModConfig.getInstance();
      LOGGER.debug("灵异世界：开始检查{}的生成条件，当前世界维度: {}", ghostType, world.method_27983().method_29177());
      LOGGER.debug("灵异世界：灵异世界维度: {}", Smfs.SPIRIT_REALM_DIMENSION.method_29177());
      LOGGER.debug("灵异世界：维度比较结果: {}", world.method_27983().equals(Smfs.SPIRIT_REALM_DIMENSION));
      if (!config.enableEvilGhosts) {
         LOGGER.debug("灵异世界：厉鬼生成失败: 全局开关未启用");
         return false;
      }

      if (ghostType == ModEntities.QIAOMEN_GHOST) {
         LOGGER.debug("灵异世界：厉鬼生成失败: 敲门鬼由KnockingCurseEffect专门处理生成");
         return false;
      }

      if (isGhostMasterType(ghostType)) {
         LOGGER.debug("灵异世界：厉鬼生成失败: 驭鬼者类型不应在灵异世界生成");
         return false;
      }

      GhostSpawnManager.GhostSpawnConfig spawnConfig = GHOST_SPAWN_CONFIGS.get(ghostType);
      if (spawnConfig == null) {
         LOGGER.debug("灵异世界：厉鬼生成失败: 未找到生成配置");
         return false;
      }

      WorldConfig worldConfig = WorldConfig.getInstance(world);
      if (worldConfig.modDifficulty == 2) {
         LOGGER.debug("灵异世界：困难模式下厉鬼必定生成（100%概率）");
      } else {
         int randomChance = RANDOM.nextInt(100);
         if (randomChance >= spawnConfig.spawnChance) {
            LOGGER.debug("灵异世界：厉鬼生成失败: 生成几率不足，随机值{}，需要小于{}", randomChance, spawnConfig.spawnChance);
            return false;
         }
      }

      LOGGER.debug("灵异世界：条件满足，准备生成{}", ghostType);
      return true;
   }

   public static void markGhostSpawned(ServerWorld world, EntityType<?> ghostType) {
   }

   public static int getSpawnInterval(EntityType<?> ghostType) {
      GhostSpawnManager.GhostSpawnConfig config = GHOST_SPAWN_CONFIGS.get(ghostType);
      return config != null ? config.spawnInterval : 24000;
   }

   public static int getSpawnChance(EntityType<?> ghostType) {
      GhostSpawnManager.GhostSpawnConfig config = GHOST_SPAWN_CONFIGS.get(ghostType);
      return config != null ? config.spawnChance : 1;
   }

   public static int getMinDay(EntityType<?> ghostType) {
      GhostSpawnManager.GhostSpawnConfig config = GHOST_SPAWN_CONFIGS.get(ghostType);
      return config != null ? config.minDay : 0;
   }

   public static Set<EntityType<?>> getSpawnedGhostTypes() {
      return GHOST_SPAWN_CONFIGS.keySet();
   }

   public static boolean updateGhostSpawnConfig(EntityType<?> entityType, int spawnInterval, int spawnChance, int minDay, boolean nightOnly) {
      if (!GHOST_SPAWN_CONFIGS.containsKey(entityType)) {
         LOGGER.warn("尝试更新不存在的鬼魂生成配置：{}", entityType);
         return false;
      }

      if (minDay < 0) {
         LOGGER.warn("鬼魂生成配置中的minDay为负数，将设置为0：{}", entityType);
         minDay = 0;
      }

      if (spawnChance < 0 || spawnChance > 100) {
         LOGGER.warn("鬼魂生成配置中的spawnChance超出范围(0-100)，将限制在有效范围内：{}", entityType);
         spawnChance = Math.max(0, Math.min(100, spawnChance));
      }

      if (spawnInterval <= 0) {
         LOGGER.warn("鬼魂生成配置中的spawnInterval必须大于0，将设置为默认值24000：{}", entityType);
         spawnInterval = 24000;
      }

      GHOST_SPAWN_CONFIGS.put(entityType, new GhostSpawnManager.GhostSpawnConfig(minDay, spawnChance, spawnInterval, nightOnly));
      if (ADDON_GHOST_SPAWN_CONFIGS.containsKey(entityType)) {
         ADDON_GHOST_SPAWN_CONFIGS.put(entityType, new GhostSpawnManager.GhostSpawnConfig(minDay, spawnChance, spawnInterval, nightOnly));
      }

      LOGGER.debug("已更新鬼魂生成配置：{}", entityType);
      return true;
   }

   public static boolean reloadAllGhostSpawnConfigs() {
      try {
         GHOST_SPAWN_CONFIGS.clear();
         initializeGhostSpawnConfigs();
         reapplyAddonSpawnConfigs();
         LOGGER.debug("成功重新加载所有鬼魂生成配置");
         return true;
      } catch (Exception e) {
         LOGGER.error("重新加载鬼魂生成配置失败: {}", e.getMessage());
         return false;
      }
   }

   public static void lockGhostType(ServerWorld world, EntityType<?> ghostType) {
      boolean isGod = AdvancementManager.hasAnyGodAdvancement(world.method_8503());
      WorldConfig worldConfig = WorldConfig.getInstance(world);
      boolean isLinear = "linear".equals(worldConfig.endingMode);
      if (!isGod && !isLinear) {
         LOCKED_GHOST_TYPES_WITH_TIME.put(ghostType, world.method_8510());
      } else {
         LOCKED_GHOST_TYPES_WITH_TIME.put(ghostType, -1L);
      }

      saveLockedGhostTypes(world);
   }

   public static void unlockGhostType(ServerWorld world, EntityType<?> ghostType) {
      LOCKED_GHOST_TYPES_WITH_TIME.remove(ghostType);
      saveLockedGhostTypes(world);
   }

   public static boolean isGhostLocked(EntityType<?> ghostType) {
      return LOCKED_GHOST_TYPES_WITH_TIME.containsKey(ghostType);
   }

   public static Set<EntityType<?>> getLockedGhostTypes() {
      return new HashSet<>(LOCKED_GHOST_TYPES_WITH_TIME.keySet());
   }

   public static void checkAndUnlockExpiredGhostTypes(ServerWorld world) {
      if (!AdvancementManager.hasAnyGodAdvancement(world.method_8503())) {
         WorldConfig worldConfig = WorldConfig.getInstance(world);
         if (!"linear".equals(worldConfig.endingMode)) {
            long currentTime = world.method_8510();
            List<EntityType<?>> expiredTypes = new ArrayList<>();

            for (Entry<EntityType<?>, Long> entry : LOCKED_GHOST_TYPES_WITH_TIME.entrySet()) {
               long lockTime = entry.getValue();
               if (lockTime != -1L && currentTime - lockTime >= 72000L) {
                  expiredTypes.add(entry.getKey());
               }
            }

            for (EntityType<?> ghostType : expiredTypes) {
               LOCKED_GHOST_TYPES_WITH_TIME.remove(ghostType);
               LOGGER.debug("鬼类型 {} 锁死时间已到（3天），已自动解锁", ghostType);
            }

            if (!expiredTypes.isEmpty()) {
               saveLockedGhostTypes(world);
            }
         }
      }
   }

   private static boolean isNightTime(World world) {
      long time = world.method_8532() % 24000L;
      return time >= 13000L && time <= 23000L;
   }

   private static boolean isGhostMasterType(EntityType<?> entityType) {
      return entityType == ModEntities.YE_ZHEN
         || entityType == ModEntities.FENG_QUAN
         || entityType == ModEntities.CAO_YANG
         || entityType == ModEntities.FANG_SHI_MIN
         || entityType == ModEntities.YAN_LI
         || entityType == ModEntities.LI_LE_PING
         || entityType == ModEntities.WANG_XIAO_MING
         || entityType == ModEntities.CHEN_DOCTOR
         || entityType == ModEntities.LI_JUN
         || entityType == ModEntities.ZHAO_KAI_MING
         || entityType == ModEntities.NPC1
         || entityType == ModEntities.NPC2
         || entityType == ModEntities.NPC3
         || entityType == ModEntities.NPC4
         || entityType == ModEntities.NPC5
         || entityType == ModEntities.NPC6;
   }

   public static BlockPos findSuitableSpawnPosition(ServerWorld world, ServerPlayerEntity targetPlayer, int radius) {
      BlockPos playerPos = targetPlayer.method_24515();
      ModConfig config = ModConfig.getInstance();
      int minRange = config.evilGhostMinSpawnRange;
      int maxRange = config.evilGhostMaxSpawnRange;
      int actualRadius = Math.max(radius, minRange);
      actualRadius = Math.min(actualRadius, maxRange);

      for (int attempt = 0; attempt < 10; attempt++) {
         int offsetX = RANDOM.nextInt(actualRadius * 2) - actualRadius;
         int offsetZ = RANDOM.nextInt(actualRadius * 2) - actualRadius;
         BlockPos spawnPos = playerPos.method_10069(offsetX, 0, offsetZ);
         spawnPos = world.method_8598(Type.field_13202, spawnPos);
         if (isValidSpawnPosition(world, spawnPos)) {
            return spawnPos;
         }
      }

      return null;
   }

   private static boolean isValidSpawnPosition(ServerWorld world, BlockPos pos) {
      return !world.method_8320(pos.method_10074()).method_26212(world, pos.method_10074())
         ? false
         : world.method_22347(pos) && world.method_22347(pos.method_10084());
   }

   private static void registerConfigReloadListener() {
      ModConfig config = ModConfig.getInstance();
      config.addConfigReloadListener(modConfig -> {
         GHOST_SPAWN_CONFIGS.clear();
         initializeGhostSpawnConfigs();
         reapplyAddonSpawnConfigs();
      });
   }

   private static void saveLockedGhostTypes(ServerWorld world) {
      MinecraftServer server = world.method_8503();
      PersistentStateManager stateManager = server.method_30002().method_17983();

      try {
         Function<NbtCompound, LockedGhostTypesState> fromNbt = LockedGhostTypesState::fromNbt;
         Supplier<LockedGhostTypesState> supplier = LockedGhostTypesState::new;
         LockedGhostTypesState ghostTypesState = (LockedGhostTypesState)stateManager.method_17924(fromNbt, supplier, "smfs_locked_ghost_types");
         NbtCompound persistentData = new NbtCompound();
         NbtList lockedList = new NbtList();

         for (Entry<EntityType<?>, Long> entry : LOCKED_GHOST_TYPES_WITH_TIME.entrySet()) {
            EntityType<?> ghostType = entry.getKey();
            Long lockTime = entry.getValue();
            NbtCompound ghostEntry = new NbtCompound();
            ghostEntry.method_10582("type", Registries.field_41177.method_10221(ghostType).toString());
            ghostEntry.method_10544("lock_time", lockTime);
            lockedList.add(ghostEntry);
         }

         persistentData.method_10566("locked_ghost_types", lockedList);
         ghostTypesState.setPersistentData(persistentData);
         stateManager.method_125();
         LOGGER.debug("已保存 {} 个锁死鬼类型到持久化数据", LOCKED_GHOST_TYPES_WITH_TIME.size());
      } catch (Exception e) {
         LOGGER.error("保存锁死鬼类型数据失败: {}", e.getMessage());
      }
   }

   public static void loadLockedGhostTypes(ServerWorld world) {
      MinecraftServer server = world.method_8503();
      if (server != null) {
         PersistentStateManager stateManager = server.method_30002().method_17983();
         LOCKED_GHOST_TYPES_WITH_TIME.clear();

         try {
            Function<NbtCompound, LockedGhostTypesState> fromNbt = nbt -> LockedGhostTypesState.fromNbt(nbt);
            Supplier<LockedGhostTypesState> supplier = () -> new LockedGhostTypesState();
            LockedGhostTypesState ghostTypesState = (LockedGhostTypesState)stateManager.method_17924(fromNbt, supplier, "smfs_locked_ghost_types");
            if (ghostTypesState != null && ghostTypesState.getPersistentData() != null) {
               NbtCompound persistentData = ghostTypesState.getPersistentData();
               if (persistentData.method_10545("locked_ghost_types")) {
                  NbtList lockedList = persistentData.method_10554("locked_ghost_types", 10);
                  if (lockedList.isEmpty() && persistentData.method_10554("locked_ghost_types", 8).size() > 0) {
                     lockedList = persistentData.method_10554("locked_ghost_types", 8);

                     for (int i = 0; i < lockedList.size(); i++) {
                        String ghostTypeId = lockedList.method_10608(i);

                        try {
                           EntityType<?> ghostType = (EntityType<?>)Registries.field_41177.method_10223(new Identifier(ghostTypeId));
                           if (ghostType != null) {
                              LOCKED_GHOST_TYPES_WITH_TIME.put(ghostType, -1L);
                           }
                        } catch (Exception e) {
                           LOGGER.warn("无法加载锁死鬼类型 {}: {}", ghostTypeId, e.getMessage());
                        }
                     }
                  } else {
                     for (int i = 0; i < lockedList.size(); i++) {
                        NbtCompound ghostEntry = lockedList.method_10602(i);

                        try {
                           String ghostTypeId = ghostEntry.method_10558("type");
                           long lockTime = ghostEntry.method_10537("lock_time");
                           EntityType<?> ghostType = (EntityType<?>)Registries.field_41177.method_10223(new Identifier(ghostTypeId));
                           if (ghostType != null) {
                              LOCKED_GHOST_TYPES_WITH_TIME.put(ghostType, lockTime);
                           }
                        } catch (Exception e) {
                           LOGGER.warn("无法加载锁死鬼类型: {}", e.getMessage());
                        }
                     }
                  }

                  LOGGER.debug("已从持久化数据加载 {} 个锁死鬼类型", LOCKED_GHOST_TYPES_WITH_TIME.size());
               } else {
                  LOGGER.debug("未找到锁死鬼类型数据，将使用空列表");
               }
            } else {
               LOGGER.debug("未找到锁死鬼类型持久化数据，将使用空列表");
            }
         } catch (Exception e) {
            LOGGER.error("加载锁死鬼类型数据失败: {}", e.getMessage());
         }
      }
   }

   public static Long getLockTime(EntityType<?> ghostType) {
      return LOCKED_GHOST_TYPES_WITH_TIME.get(ghostType);
   }

   static {
      initializeGhostSpawnConfigs();
      reapplyAddonSpawnConfigs();
      registerConfigReloadListener();
   }

   private static class GhostSpawnConfig {
      public final int minDay;
      public final int spawnChance;
      public final int spawnInterval;
      public final boolean isNightOnly;

      public GhostSpawnConfig(int minDay, int spawnChance, int spawnInterval, boolean isNightOnly) {
         this.minDay = minDay;
         this.spawnChance = spawnChance;
         this.spawnInterval = spawnInterval;
         this.isNightOnly = isNightOnly;
      }
   }
}
