package com.xie.smfs.manager;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.effect.BlackGhostDomainEffect;
import com.xie.smfs.effect.CyanGhostDomainEffect;
import com.xie.smfs.effect.GoldenGhostDomainEffect;
import com.xie.smfs.effect.GrayGhostDomainEffect;
import com.xie.smfs.effect.GreenGhostDomainEffect;
import com.xie.smfs.effect.LostStatusEffectInstance;
import com.xie.smfs.effect.RedGhostDomainEffect;
import com.xie.smfs.effect.ThickFogEffect;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.entity.master.YangJianEntity;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.event.QuestEventHandler;
import com.xie.smfs.item.BaseGhostEyeItem;
import com.xie.smfs.item.BlockGhostItem;
import com.xie.smfs.item.FogGhostItem;
import com.xie.smfs.item.FoodGhostItem;
import com.xie.smfs.item.GhostFireItem;
import com.xie.smfs.item.GhostOfficerItem;
import com.xie.smfs.item.GhostSmokeItem;
import com.xie.smfs.item.GhostWindItem;
import com.xie.smfs.item.QiaomenGhostItem;
import com.xie.smfs.item.SilentGhostEyeItem;
import com.xie.smfs.item.VillagerGhostItem;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.InstantKillUtil;
import com.xie.smfs.util.TargetingUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World.ExplosionSourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostDomainManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostDomainManager");
   private static final int BASE_DOMAIN_RADIUS = 64;
   private static final int LEVEL_RADIUS_BONUS = 32;
   private static final Map<UUID, Integer> PLAYER_CUSTOM_DOMAIN_LEVEL = new HashMap<>();
   private static final Map<UUID, Integer> PLAYER_DOMAIN_RANGE = new HashMap<>();
   private static final Map<Integer, Double> GOD_CONTROL_ORIGINAL_SPEED = new ConcurrentHashMap<>();

   public static int getDomainRadius(PlayerEntity player, int level) {
      int rawRadius;
      try {
         ModConfig config = ModConfig.getInstance();
         rawRadius = (int)(config.ghostDomainBaseSize + (level - 1) * config.ghostDomainSizePerLevel);
      } catch (Exception e) {
         LOGGER.warn("配置加载失败，使用硬编码默认值计算鬼蜮半径: {}", e.getMessage());
         rawRadius = 64 + (level - 1) * 32;
      }

      int preferredRange = PLAYER_DOMAIN_RANGE.getOrDefault(player.method_5667(), -1);
      return preferredRange <= 0 ? rawRadius : Math.max(5, Math.min(preferredRange, rawRadius));
   }

   public static void setPlayerDomainRange(PlayerEntity player, int range) {
      if (range <= 0) {
         PLAYER_DOMAIN_RANGE.remove(player.method_5667());
      } else {
         PLAYER_DOMAIN_RANGE.put(player.method_5667(), Math.max(5, range));
      }
   }

   public static boolean isHoldingShardItem(PlayerEntity player, String ghostType) {
      for (Hand hand : Hand.values()) {
         ItemStack stack = player.method_5998(hand);
         if (stack.method_7909() instanceof BaseGhostEyeItem item && BaseGhostEyeItem.isShard(stack) && item.getGhostType().equals(ghostType)) {
            return true;
         }
      }

      return false;
   }

   public static GhostDomainManager.SkillCheckResult canUseGhostSkill(PlayerEntity player, String ghostType, Item ghostItem, int requiredLevel) {
      if (isHoldingShardItem(player, ghostType)) {
         return GhostDomainManager.SkillCheckResult.SUCCESS;
      }

      if (!MainGhostManager.hasMainGhost(player)) {
         return GhostDomainManager.SkillCheckResult.NO_GHOST;
      }

      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      ItemStack item = PlayerEvents.getGhostSlotItem(player, mainSlot);
      if (!item.method_7960() && item.method_7909() == ghostItem) {
         if (requiredLevel > 0) {
            int rawLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
            int currentLevel = getEffectiveSkillLevel(player, rawLevel);
            if (currentLevel < requiredLevel) {
               return GhostDomainManager.SkillCheckResult.LEVEL_TOO_LOW;
            }
         }

         return GhostDomainManager.SkillCheckResult.SUCCESS;
      } else {
         return GhostDomainManager.SkillCheckResult.NO_GHOST;
      }
   }

   public static void consumeShardItem(PlayerEntity player, String ghostType) {
      for (Hand hand : Hand.values()) {
         ItemStack stack = player.method_5998(hand);
         if (stack.method_7909() instanceof BaseGhostEyeItem item && BaseGhostEyeItem.isShard(stack) && item.getGhostType().equals(ghostType)) {
            NbtCompound nbt = stack.method_7948();
            int uses = nbt.method_10550("ShardUses");
            if (uses <= 0) {
               uses = 2;
            }

            if (--uses <= 0) {
               player.method_6122(hand, ItemStack.field_8037);
            } else {
               nbt.method_10569("ShardUses", uses);
            }

            player.method_31548().method_5431();
            return;
         }
      }
   }

   public static void toggleGhostDomain(PlayerEntity player) {
      LOGGER.debug("为玩家 {} 切换鬼域状态", player.method_5477().getString());
      if (!player.method_6059(ModEffects.SILENCE) && !player.method_6059(ModEffects.DREAM)) {
         boolean hasGhostEye = hasValidGhostEye(player);
         boolean hasGhostFire = hasValidGhostFire(player);
         boolean hasFogGhost = hasFogGhost(player);
         boolean hasBlockGhost = hasBlockGhost(player);
         boolean hasFoodGhost = hasFoodGhost(player);
         boolean hasQiaomenGhost = hasQiaomenGhost(player);
         boolean hasVillagerGhost = hasVillagerGhost(player);
         boolean hasGhostWind = hasGhostWind(player);
         boolean hasGhostOfficer = hasGhostOfficer(player);
         boolean hasGhostSmoke = hasGhostSmoke(player);
         if (hasGhostEye
            || hasGhostFire
            || hasFogGhost
            || hasBlockGhost
            || hasFoodGhost
            || hasQiaomenGhost
            || hasVillagerGhost
            || hasGhostWind
            || hasGhostOfficer
            || hasGhostSmoke) {
            if (isGhostDomainActive(player)) {
               disableGhostDomain(player);
               PLAYER_CUSTOM_DOMAIN_LEVEL.remove(player.method_5667());
            } else {
               if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.method_5667())) {
                  PLAYER_CUSTOM_DOMAIN_LEVEL.put(player.method_5667(), 1);
               }

               if ((MainGhostManager.isMainGhostType(player, SilentGhostEyeItem.class) || MainGhostManager.isMainGhostType(player, GhostFireItem.class))
                  && hasGhostEye
                  && hasGhostFire) {
                  enableGoldenGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, SilentGhostEyeItem.class)) {
                  enableRedGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, GhostFireItem.class)) {
                  enableGreenGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, FogGhostItem.class)) {
                  enableFogGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, BlockGhostItem.class)) {
                  enableBlockGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, FoodGhostItem.class)) {
                  enableFoodGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, QiaomenGhostItem.class)) {
                  enableQiaomenGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, VillagerGhostItem.class)) {
                  enableVillagerGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, GhostWindItem.class)) {
                  enableCyanGhostDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, GhostOfficerItem.class)) {
                  enableGhostOfficerDomain(player);
               } else if (MainGhostManager.isMainGhostType(player, GhostSmokeItem.class)) {
                  enableGrayGhostDomain(player);
               } else if (hasGhostEye) {
                  enableRedGhostDomain(player);
               } else if (hasGhostFire) {
                  enableGreenGhostDomain(player);
               } else if (hasFogGhost) {
                  enableFogGhostDomain(player);
               } else if (hasBlockGhost) {
                  enableBlockGhostDomain(player);
               } else if (hasFoodGhost) {
                  enableFoodGhostDomain(player);
               } else if (hasQiaomenGhost) {
                  enableQiaomenGhostDomain(player);
               } else if (hasVillagerGhost) {
                  enableVillagerGhostDomain(player);
               } else if (hasGhostWind) {
                  enableCyanGhostDomain(player);
               } else if (hasGhostOfficer) {
                  enableGhostOfficerDomain(player);
               } else if (hasGhostSmoke) {
                  enableGrayGhostDomain(player);
               }
            }
         }
      } else {
         player.method_7353(Text.method_43470("§c体内的鬼陷入沉寂"), true);
      }
   }

   public static int getEffectiveDomainLevel(PlayerEntity player, int ghostLevel) {
      if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.method_5667())) {
         int customLevel = PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.method_5667(), 1);
         if (ghostLevel <= 0) {
            ghostLevel = 1;
         }

         return Math.min(customLevel, ghostLevel);
      } else {
         return ghostLevel;
      }
   }

   public static int getEffectiveSkillLevel(PlayerEntity player, int ghostLevel) {
      return ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.method_5667()) && isGhostDomainActive(player)
         ? PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.method_5667(), 1)
         : ghostLevel;
   }

   public static String getInsufficientLevelMessage(PlayerEntity player) {
      return ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.method_5667()) && isGhostDomainActive(player) ? "§c鬼蜮强度不足" : "§c复苏程度不足";
   }

   public static void increaseGhostDomainLevel(PlayerEntity player) {
      if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.method_5667())) {
         if (isGhostDomainActive(player)) {
            int current = PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.method_5667(), 1);
            int maxLevel = getCurrentGhostMaxLevel(player);
            int newLevel = Math.min(current + 1, maxLevel);
            PLAYER_CUSTOM_DOMAIN_LEVEL.put(player.method_5667(), newLevel);
            refreshGhostDomainLevel(player, newLevel);
            if (player instanceof ServerPlayerEntity) {
               player.method_7353(Text.method_43470("§a鬼域层数: " + newLevel + " / " + maxLevel), true);
            }
         }
      }
   }

   public static void decreaseGhostDomainLevel(PlayerEntity player) {
      if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.method_5667())) {
         if (isGhostDomainActive(player)) {
            int current = PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.method_5667(), 1);
            int newLevel = Math.max(current - 1, 1);
            PLAYER_CUSTOM_DOMAIN_LEVEL.put(player.method_5667(), newLevel);
            refreshGhostDomainLevel(player, newLevel);
            int maxLevel = getCurrentGhostMaxLevel(player);
            if (player instanceof ServerPlayerEntity) {
               player.method_7353(Text.method_43470("§a鬼域层数: " + newLevel + " / " + maxLevel), true);
            }
         }
      }
   }

   private static void refreshGhostDomainLevel(PlayerEntity player, int newLevel) {
      StatusEffectInstance currentEffect = null;
      if (player.method_6059(ModEffects.RED_GHOST_DOMAIN)) {
         currentEffect = player.method_6112(ModEffects.RED_GHOST_DOMAIN);
      } else if (player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)) {
         currentEffect = player.method_6112(ModEffects.GREEN_GHOST_DOMAIN);
      } else if (player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)) {
         currentEffect = player.method_6112(ModEffects.GOLDEN_GHOST_DOMAIN);
      } else if (player.method_6059(ModEffects.THICK_FOG)) {
         currentEffect = player.method_6112(ModEffects.THICK_FOG);
      } else if (player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)) {
         currentEffect = player.method_6112(ModEffects.BLACK_GHOST_DOMAIN);
      } else if (player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)) {
         currentEffect = player.method_6112(ModEffects.CYAN_GHOST_DOMAIN);
      } else if (player.method_6059(ModEffects.GRAY_GHOST_DOMAIN)) {
         currentEffect = player.method_6112(ModEffects.GRAY_GHOST_DOMAIN);
      }

      if (currentEffect != null) {
         StatusEffect effectType = currentEffect.method_5579();
         player.method_6016(effectType);
         player.method_6092(new StatusEffectInstance(effectType, Integer.MAX_VALUE, newLevel - 1, false, false, true));
         applyLevelAbilities(player, newLevel);
      }
   }

   private static int getCurrentGhostMaxLevel(PlayerEntity player) {
      if (isGhostDomainActive(player)) {
         if (player.method_6059(ModEffects.RED_GHOST_DOMAIN)) {
            return getGhostEyeLevel(player);
         }

         if (player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)) {
            return getGhostFireLevel(player);
         }

         if (player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)) {
            return Math.max(getGhostEyeLevel(player), getGhostFireLevel(player));
         }

         if (player.method_6059(ModEffects.THICK_FOG)) {
            return getFogGhostLevel(player);
         }

         if (player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)) {
            return Math.max(
               Math.max(Math.max(getBlockGhostLevel(player), getFoodGhostLevel(player)), getQiaomenGhostLevel(player)),
               Math.max(getVillagerGhostLevel(player), getGhostOfficerLevel(player))
            );
         }

         if (player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)) {
            return getGhostWindLevel(player);
         }

         if (player.method_6059(ModEffects.GRAY_GHOST_DOMAIN)) {
            return getGhostSmokeLevel(player);
         }
      }

      return 1;
   }

   public static void enableRedGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostEyeLevel(player));
      if (level <= 0) {
         LOGGER.warn("玩家 {} 尝试开启红色鬼域但等级无效: {}", player.method_5477().getString(), level);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级红色鬼域", player.method_5477().getString(), level);
         player.method_6092(new StatusEffectInstance(ModEffects.RED_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
         if (player instanceof ServerPlayerEntity) {
            RedGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            applyTargetGhostDomainToOtherPlayers(player, ModEffects.RED_GHOST_DOMAIN_TARGET, level);
         }

         applyLevelAbilities(player, level);
      }
   }

   public static void enableGreenGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostFireLevel(player));
      if (level <= 0) {
         LOGGER.warn("玩家 {} 尝试开启绿色鬼域但等级无效: {}", player.method_5477().getString(), level);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级绿色鬼域", player.method_5477().getString(), level);
         player.method_6092(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
         if (player instanceof ServerPlayerEntity) {
            GreenGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            applyTargetGhostDomainToOtherPlayers(player, ModEffects.GREEN_GHOST_DOMAIN_TARGET, level);
         }

         applyLevelAbilities(player, level);
         LOGGER.debug("玩家 {} 的绿色鬼域已成功开启", player.method_5477().getString());
      }
   }

   public static void enableGoldenGhostDomain(PlayerEntity player) {
      int ghostEyeLevel = getGhostEyeLevel(player);
      int ghostFireLevel = getGhostFireLevel(player);
      int level = getEffectiveDomainLevel(player, Math.max(ghostEyeLevel, ghostFireLevel));
      if (level <= 0) {
         LOGGER.warn("玩家 {} 尝试开启金色鬼域但等级无效: 鬼眼{}级, 鬼火{}级", player.method_5477().getString(), ghostEyeLevel, ghostFireLevel);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级金色鬼域 (鬼眼{}级, 鬼火{}级)", player.method_5477().getString(), level, ghostEyeLevel, ghostFireLevel);
         player.method_6092(new StatusEffectInstance(ModEffects.GOLDEN_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
         if (player instanceof ServerPlayerEntity) {
            LOGGER.debug("在服务端为玩家 {} 更新金色鬼域复苏程度", player.method_5477().getString());
            GoldenGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            applyTargetGhostDomainToOtherPlayers(player, ModEffects.GOLDEN_GHOST_DOMAIN_TARGET, level);
         }

         applyLevelAbilities(player, level);
         LOGGER.debug("玩家 {} 的金色鬼域已成功开启", player.method_5477().getString());
      }
   }

   public static void enableFogGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getFogGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级鬼雾鬼域", player.method_5477().getString(), level);
      player.method_6092(new StatusEffectInstance(ModEffects.THICK_FOG, Integer.MAX_VALUE, 0, false, false, true));
      if (player instanceof ServerPlayerEntity) {
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加鬼雾鬼域效果", player.method_5477().getString());
         int radius = getDomainRadius(player, level);
         ThickFogEffect.updateRevivalDegreeInGhostDomain(player);

         for (PlayerEntity otherPlayer : player.method_37908()
            .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.method_5739(otherPlayer);
            if (distance <= radius) {
               otherPlayer.method_6092(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加鬼雾鬼域效果（TARGET版本）", otherPlayer.method_5477().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的鬼雾鬼域已成功开启", player.method_5477().getString());
   }

   public static void enableBlockGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getBlockGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级方块鬼鬼域", player.method_5477().getString(), level);
      player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的方块鬼鬼域已成功开启", player.method_5477().getString());
   }

   public static void enableFoodGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getFoodGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级食物鬼鬼域", player.method_5477().getString(), level);
      player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加食物鬼鬼域效果", player.method_5477().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.method_37908()
            .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.method_5739(otherPlayer);
            if (distance <= radius) {
               otherPlayer.method_6092(new StatusEffectInstance(StatusEffects.field_5903, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加食物鬼鬼域效果", otherPlayer.method_5477().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的食物鬼鬼域已成功开启", player.method_5477().getString());
   }

   public static void enableQiaomenGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getQiaomenGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级敲门鬼鬼域", player.method_5477().getString(), level);
      player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加敲门鬼鬼域效果", player.method_5477().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.method_37908()
            .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.method_5739(otherPlayer);
            if (distance <= radius) {
               otherPlayer.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加敲门鬼鬼域效果", otherPlayer.method_5477().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的敲门鬼鬼域已成功开启", player.method_5477().getString());
   }

   public static void enableVillagerGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getVillagerGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级村民鬼鬼域", player.method_5477().getString(), level);
      player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加村民鬼鬼域效果", player.method_5477().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.method_37908()
            .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.method_5739(otherPlayer);
            if (distance <= radius) {
               otherPlayer.method_6092(new StatusEffectInstance(StatusEffects.field_5911, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加村民鬼鬼域效果", otherPlayer.method_5477().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的村民鬼鬼域已成功开启", player.method_5477().getString());
   }

   public static void enableGhostOfficerDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostOfficerLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级鬼差鬼域", player.method_5477().getString(), level);
      player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加鬼差鬼域效果", player.method_5477().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.method_37908()
            .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.method_5739(otherPlayer);
            if (distance <= radius) {
               otherPlayer.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 200, level - 1, false, false, false));
               LOGGER.debug("给玩家 {} 施加鬼差鬼域效果（TARGET版本）", otherPlayer.method_5477().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的鬼差鬼域已成功开启", player.method_5477().getString());
   }

   public static void enableCyanGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostWindLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级青色鬼域", player.method_5477().getString(), level);
      player.method_6092(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      CyanGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.CYAN_GHOST_DOMAIN_TARGET, level);
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的青色鬼域已成功开启", player.method_5477().getString());
   }

   public static void disableGhostDomain(PlayerEntity player) {
      LOGGER.debug("为玩家 {} 关闭鬼域", player.method_5477().getString());
      player.method_6016(ModEffects.RED_GHOST_DOMAIN);
      player.method_6016(ModEffects.GREEN_GHOST_DOMAIN);
      player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN);
      player.method_6016(ModEffects.GRAY_GHOST_DOMAIN);
      player.method_6016(ModEffects.THICK_FOG);
      player.method_6016(ModEffects.THICK_FOG_TARGET);
      player.method_6016(ModEffects.BLACK_GHOST_DOMAIN);
      player.method_6016(ModEffects.CYAN_GHOST_DOMAIN);
      player.method_6016(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
      if (player instanceof ServerPlayerEntity) {
         int currentLevel = getCurrentLevel(player);
         int radius = getDomainRadius(player, currentLevel + 1);

         for (PlayerEntity otherPlayer : player.method_37908()
            .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.method_5739(otherPlayer);
            if (distance <= radius) {
               otherPlayer.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
               otherPlayer.method_6016(StatusEffects.field_5903);
               otherPlayer.method_6016(StatusEffects.field_5909);
               otherPlayer.method_6016(StatusEffects.field_5911);
               LOGGER.debug("清除玩家 {} 的方块鬼/食物鬼/敲门鬼/村民鬼鬼域效果", otherPlayer.method_5477().getString());
            }
         }

         removeTargetGhostDomainFromOtherPlayers(player);
      }

      resetLevelAbilities(player);
      LOGGER.debug("玩家 {} 的鬼域已成功关闭", player.method_5477().getString());
   }

   public static void applyTargetGhostDomainToOtherPlayers(PlayerEntity player, StatusEffect targetEffect, int level) {
      if (player instanceof ServerPlayerEntity) {
         ModConfig config = ModConfig.getInstance();
         if (config.enableGhostDomainEffectsOnMobs) {
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("为玩家 {} 给其他玩家和生物施加TARGET后缀鬼域效果: {}, 等级: {}", player.method_5477().getString(), targetEffect, level);
            }

            int radius = getDomainRadius(player, level);

            for (PlayerEntity otherPlayer : player.method_37908()
               .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
               double distance = player.method_5739(otherPlayer);
               if (distance <= radius) {
                  StatusEffectInstance existingEffect = otherPlayer.method_6112(targetEffect);
                  if (existingEffect == null || existingEffect.method_5584() < 100 || existingEffect.method_5578() != level - 1) {
                     otherPlayer.method_6092(new StatusEffectInstance(targetEffect, 200, level - 1, false, false, false));
                  }

                  if (!otherPlayer.method_7325()) {
                     StatusEffectInstance existingLostEffect = otherPlayer.method_6112(ModEffects.LOST);
                     if (existingLostEffect == null || existingLostEffect.method_5584() < 100) {
                        StatusEffectInstance lostEffect = new LostStatusEffectInstance(ModEffects.LOST, 200, 0, false, false, false, player.method_5667());
                        otherPlayer.method_6092(lostEffect);
                     }
                  }
               }
            }

            for (LivingEntity livingEntity : player.method_37908()
               .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entity -> entity != player && !(entity instanceof PlayerEntity))) {
               double distance = player.method_5739(livingEntity);
               if (distance <= radius) {
                  StatusEffectInstance existingDomainEffect = livingEntity.method_6112(targetEffect);
                  if (existingDomainEffect == null || existingDomainEffect.method_5584() < 100) {
                     livingEntity.method_6092(new StatusEffectInstance(targetEffect, 200, level - 1, false, false, false));
                  }

                  StatusEffectInstance existingLostEffect = livingEntity.method_6112(ModEffects.LOST);
                  if (existingLostEffect == null || existingLostEffect.method_5584() < 100) {
                     StatusEffectInstance lostEffect = new LostStatusEffectInstance(ModEffects.LOST, 200, 0, false, false, false, player.method_5667());
                     livingEntity.method_6092(lostEffect);
                  }
               }
            }
         }
      }
   }

   public static void removeTargetGhostDomainFromOtherPlayers(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("为玩家 {} 清除其他玩家的TARGET后缀鬼域效果", player.method_5477().getString());
         }

         int currentLevel = getCurrentLevel(player);
         int radius = getDomainRadius(player, currentLevel + 1);

         for (PlayerEntity otherPlayer : player.method_37908()
            .method_8390(PlayerEntity.class, player.method_5829().method_1014(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.method_5739(otherPlayer);
            if (distance <= radius) {
               otherPlayer.method_6016(ModEffects.RED_GHOST_DOMAIN_TARGET);
               otherPlayer.method_6016(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
               otherPlayer.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
               otherPlayer.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
               otherPlayer.method_6016(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
               otherPlayer.method_6016(ModEffects.THICK_FOG_TARGET);
               otherPlayer.method_6016(ModEffects.LOST);
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("清除玩家 {} 的TARGET后缀鬼域效果和迷失效果", otherPlayer.method_5477().getString());
               }
            }
         }

         for (LivingEntity entity : player.method_37908()
            .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && !(entityx instanceof PlayerEntity))) {
            double distance = player.method_5739(entity);
            if (distance <= radius) {
               entity.method_6016(ModEffects.LOST);
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("清除生物 {} 的迷失效果", entity.method_5477().getString());
               }
            }
         }
      }
   }

   private static void applyLevelAbilities(PlayerEntity player, int level) {
      LOGGER.debug("为玩家 {} 应用 {} 级鬼域能力", player.method_5477().getString(), level);
      if (level >= 2) {
         int radius = getDomainRadius(player, level);
         player.method_37908().method_8335(player, player.method_5829().method_1014(radius)).forEach(entity -> {
            if (entity instanceof LivingEntity) {
               entity.method_5834(true);
            }
         });
      } else {
         player.method_37908().method_8335(player, player.method_5829().method_1014(256.0)).forEach(entity -> {
            if (entity instanceof LivingEntity) {
               entity.method_5834(false);
            }
         });
      }

      if (level >= 3) {
         LOGGER.debug("为玩家 {} 应用3级能力: 飞行", player.method_5477().getString());
         player.method_31549().field_7478 = true;
         player.method_31549().field_7479 = true;
         player.method_7355();
      } else if (player.method_31549().field_7478 && !player.method_7337() && !player.method_7325()) {
         LOGGER.debug("玩家 {} 层数不足3级，禁用飞行", player.method_5477().getString());
         player.method_31549().field_7478 = false;
         player.method_31549().field_7479 = false;
         player.method_7355();
      }

      LOGGER.debug("玩家 {} 的鬼域能力应用完成", player.method_5477().getString());
   }

   public static void sendGhostEyeWarning(PlayerEntity player) {
      if (hasValidGhostEye(player) && player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_7353(Text.method_43470("§c鬼眼正在不安分地转动着..."), true);
      }
   }

   public static void handleGhostDomainTeleport(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      double teleportDistance = 64.0 + (currentLevel - 3) * 32.0;
      Vec3d lookVec = player.method_5828(1.0F);
      Vec3d startPos = player.method_33571();
      Vec3d endPos = startPos.method_1019(lookVec.method_1021(teleportDistance));
      HitResult hitResult = player.method_37908().method_17742(new RaycastContext(startPos, endPos, ShapeType.field_17558, FluidHandling.field_1348, player));
      Vec3d teleportPos;
      if (hitResult.method_17783() == Type.field_1333) {
         teleportPos = endPos;
      } else {
         teleportPos = hitResult.method_17784().method_1020(lookVec.method_1021(0.5));
      }

      player.method_20620(teleportPos.field_1352, teleportPos.field_1351, teleportPos.field_1350);
      LOGGER.debug("玩家 {} 瞬移到位置: {}", player.method_5477().getString(), teleportPos);
   }

   public static void handleGhostDomainTeleportEntity(PlayerEntity player) {
      Vec3d lookVec = player.method_5828(1.0F);
      Vec3d startPos = player.method_33571();
      Entity bestTarget = TargetingUtil.findEntityInLookDirectionWithOcclusion(
         player, 64.0, 0.5, e -> !(e instanceof YangJianEntity) && !(e instanceof LuoQianGhostEntity)
      );
      if (bestTarget != null) {
         if (bestTarget instanceof GhostEntity ghost) {
            if (PlayerEvents.isGhostChildFused(player)) {
               if (player.method_37908() instanceof ServerWorld serverWorld) {
                  GhostSpawnManager.lockGhostType(serverWorld, bestTarget.method_5864());
               }

               GhostDeathHandler.markLegitimateRemoval(ghost);
               ghost.method_31472();
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.method_7353(Text.method_43470("§c已封锁厉鬼" + bestTarget.method_5477().getString()), true);
               }

               LOGGER.debug("玩家 {} 成神后放逐厉鬼 {}，已加入封锁列表", player.method_5477().getString(), bestTarget.method_5477().getString());
            } else if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c需要成神后才能放逐厉鬼"), true);
            }
         } else {
            double newX = bestTarget.method_23317() + (Math.random() * 2000.0 - 1000.0);
            double newY = bestTarget.method_23318() + (Math.random() * 200.0 - 100.0);
            double newZ = bestTarget.method_23321() + (Math.random() * 2000.0 - 1000.0);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已放逐" + bestTarget.method_5477().getString()), true);
            }

            newY = Math.max(0.0, Math.min(newY, player.method_37908().method_31605()));
            bestTarget.method_20620(newX, newY, newZ);
            LOGGER.debug("玩家 {} 传送生物 {} 到位置: ({}, {}, {})", player.method_5477().getString(), bestTarget.method_5477().getString(), newX, newY, newZ);
         }
      } else {
         LOGGER.debug("玩家 {} 视线内没有找到实体，生成激光光束", player.method_5477().getString());
         generateLaserBeam(player, lookVec, startPos);
      }
   }

   public static void handleGhostDomainPauseTime(PlayerEntity player) {
      LOGGER.debug("玩家 {} 暂停时间", player.method_5477().getString());
   }

   public static void handleGhostDomainRemoveBuffs(PlayerEntity player) {
      List<StatusEffect> effectsToRemove = new ArrayList<>();
      player.method_6088()
         .forEach(
            (effect, instance) -> {
               if (!effect.method_5573()
                  && effect != ModEffects.RED_GHOST_DOMAIN
                  && effect != ModEffects.RED_GHOST_DOMAIN_TARGET
                  && effect != ModEffects.GREEN_GHOST_DOMAIN
                  && effect != ModEffects.BLUE_GHOST_DOMAIN
                  && effect != ModEffects.GRAY_GHOST_DOMAIN
                  && effect != ModEffects.GOLDEN_GHOST_DOMAIN
                  && effect != ModEffects.PURPLE_GHOST_DOMAIN
                  && effect != ModEffects.BLACK_GHOST_DOMAIN
                  && effect != ModEffects.CYAN_GHOST_DOMAIN
                  && effect != ModEffects.THICK_FOG) {
                  effectsToRemove.add(effect);
               }
            }
         );
      effectsToRemove.forEach(player::method_6016);
      float currentMaxHealth = player.method_6063();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.method_5996(EntityAttributes.field_23716).method_6192(20.0);
         LOGGER.debug("玩家 {} 鬼域移除负面效果：恢复生命上限到20", player.method_5477().getString());
      } else {
         targetHealth = currentMaxHealth;
      }

      player.method_6033(targetHealth);
      LOGGER.debug("玩家 {} 移除所有负面效果并恢复生命值到{}", player.method_5477().getString(), targetHealth);
   }

   public static void handleLostGhostRestart(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行遗忘鬼重启技能", player.method_5477().getString());
      List<StatusEffect> effectsToRemove = new ArrayList<>();
      player.method_6088()
         .forEach(
            (effect, instance) -> {
               if (!effect.method_5573()
                  && effect != ModEffects.RED_GHOST_DOMAIN
                  && effect != ModEffects.RED_GHOST_DOMAIN_TARGET
                  && effect != ModEffects.GREEN_GHOST_DOMAIN
                  && effect != ModEffects.BLUE_GHOST_DOMAIN
                  && effect != ModEffects.GRAY_GHOST_DOMAIN
                  && effect != ModEffects.GOLDEN_GHOST_DOMAIN
                  && effect != ModEffects.PURPLE_GHOST_DOMAIN
                  && effect != ModEffects.BLACK_GHOST_DOMAIN
                  && effect != ModEffects.CYAN_GHOST_DOMAIN
                  && effect != ModEffects.THICK_FOG) {
                  effectsToRemove.add(effect);
               }
            }
         );
      effectsToRemove.forEach(player::method_6016);
      float currentMaxHealth = player.method_6063();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.method_5996(EntityAttributes.field_23716).method_6192(20.0);
         LOGGER.debug("玩家 {} 遗忘鬼重启技能：恢复生命上限到20", player.method_5477().getString());
      } else {
         targetHealth = currentMaxHealth;
      }

      player.method_6033(targetHealth);
      LOGGER.debug("玩家 {} 遗忘鬼重启技能：移除所有负面效果并恢复生命值到{}", player.method_5477().getString(), targetHealth);
      clearLostGhostRevivalDegree(player);
   }

   private static void clearLostGhostRevivalDegree(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (!ghostItem.method_7960() && ghostItem.method_7909() == ModItems.LOST_GHOST) {
            PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", 0);
            LOGGER.debug("玩家 {} 遗忘鬼槽位 {} 的复苏程度已清空", player.method_5477().getString(), i);
            if (player instanceof ServerPlayerEntity) {
               player.method_7353(Text.method_43470("§a遗忘鬼的复苏程度已被清空"), true);
            }
         }
      }
   }

   public static void handleGhostOfficerRestart(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行鬼差重启技能", player.method_5477().getString());
      List<StatusEffect> effectsToRemove = new ArrayList<>();
      player.method_6088()
         .forEach(
            (effect, instance) -> {
               if (!effect.method_5573()
                  && effect != ModEffects.RED_GHOST_DOMAIN
                  && effect != ModEffects.RED_GHOST_DOMAIN_TARGET
                  && effect != ModEffects.GREEN_GHOST_DOMAIN
                  && effect != ModEffects.BLUE_GHOST_DOMAIN
                  && effect != ModEffects.GRAY_GHOST_DOMAIN
                  && effect != ModEffects.GOLDEN_GHOST_DOMAIN
                  && effect != ModEffects.PURPLE_GHOST_DOMAIN
                  && effect != ModEffects.BLACK_GHOST_DOMAIN
                  && effect != ModEffects.CYAN_GHOST_DOMAIN
                  && effect != ModEffects.THICK_FOG) {
                  effectsToRemove.add(effect);
               }
            }
         );
      effectsToRemove.forEach(player::method_6016);
      float currentMaxHealth = player.method_6063();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.method_5996(EntityAttributes.field_23716).method_6192(20.0);
         LOGGER.debug("玩家 {} 鬼差重启技能：恢复生命上限到20", player.method_5477().getString());
      } else {
         targetHealth = currentMaxHealth;
      }

      player.method_6033(targetHealth);
      LOGGER.debug("玩家 {} 鬼差重启技能：移除所有负面效果并恢复生命值到{}", player.method_5477().getString(), targetHealth);
   }

   private static void clearGhostOfficerRevivalDegree(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (!ghostItem.method_7960() && ghostItem.method_7909() == ModItems.GHOST_OFFICER) {
            PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", 0);
            LOGGER.debug("玩家 {} 鬼差槽位 {} 的复苏程度已清空", player.method_5477().getString(), i);
            if (player instanceof ServerPlayerEntity) {
               player.method_7353(Text.method_43470("§a鬼差的复苏程度已被清空"), true);
            }
         }
      }
   }

   private static void resetLevelAbilities(PlayerEntity player) {
      LOGGER.debug("为玩家 {} 重置鬼域能力", player.method_5477().getString());
      player.method_37908().method_8390(Entity.class, player.method_5829().method_1014(1000.0), entity -> true).forEach(entity -> {
         if (entity instanceof LivingEntity) {
            entity.method_5834(false);
         }
      });
      if (!player.method_7337() && !player.method_7325()) {
         LOGGER.debug("为玩家 {} 禁用飞行能力", player.method_5477().getString());
         player.method_31549().field_7478 = false;
         player.method_31549().field_7479 = false;
         player.method_7355();
      }

      LOGGER.debug("玩家 {} 的鬼域能力重置完成", player.method_5477().getString());
   }

   public static boolean hasValidGhostEye(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "silent_ghost_eye");
   }

   public static boolean hasValidGhostFire(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_fire");
   }

   public static int getGhostEyeLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.SILENT_GHOST_EYE) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getGhostFireLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.GHOST_FIRE) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getFogGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.FOG_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getBlockGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.BLOCK_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getFoodGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.FOOD_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getQiaomenGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.QIAOMEN_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getVillagerGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.VILLAGER_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getGhostWindLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.GHOST_WIND) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getGhostOfficerLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.GHOST_OFFICER) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static boolean isGhostDomainActive(PlayerEntity player) {
      return player.method_6059(ModEffects.RED_GHOST_DOMAIN)
         || player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)
         || player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)
         || player.method_6059(ModEffects.THICK_FOG)
         || player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)
         || player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)
         || player.method_6059(ModEffects.GRAY_GHOST_DOMAIN);
   }

   public static int getCurrentLevel(PlayerEntity player) {
      if (!isGhostDomainActive(player)) {
         return 0;
      }

      int maxLevel = 0;
      if (player.method_6059(ModEffects.RED_GHOST_DOMAIN)) {
         int level = player.method_6112(ModEffects.RED_GHOST_DOMAIN).method_5578() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)) {
         int level = player.method_6112(ModEffects.GREEN_GHOST_DOMAIN).method_5578() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)) {
         int level = player.method_6112(ModEffects.GOLDEN_GHOST_DOMAIN).method_5578() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)) {
         int level = player.method_6112(ModEffects.CYAN_GHOST_DOMAIN).method_5578() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)) {
         int level = player.method_6112(ModEffects.BLACK_GHOST_DOMAIN).method_5578() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.method_6059(ModEffects.GRAY_GHOST_DOMAIN)) {
         int level = player.method_6112(ModEffects.GRAY_GHOST_DOMAIN).method_5578() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.method_6059(ModEffects.THICK_FOG)) {
         StatusEffectInstance effect = player.method_6112(ModEffects.THICK_FOG);
         if (effect != null) {
            maxLevel = Math.max(maxLevel, effect.method_5578() + 1);
         }
      }

      return maxLevel;
   }

   public static boolean isImmuneToGhostDomain(PlayerEntity player, int ghostLevel) {
      return isGhostDomainActive(player) && getCurrentLevel(player) > ghostLevel;
   }

   public static boolean hasGhostEntitiesInRange(PlayerEntity player, int radius) {
      return player.method_37908()
         .method_8335(player, player.method_5829().method_1014(radius))
         .stream()
         .filter(entity -> entity instanceof GhostEntity)
         .anyMatch(ghost -> shouldTriggerGhostEyeWarning(player, (GhostEntity)ghost));
   }

   private static boolean shouldTriggerGhostEyeWarning(PlayerEntity player, GhostEntity ghost) {
      if (ghost instanceof GhostChildEntity) {
         return false;
      } else if (!(ghost instanceof PlayerGhostEntity playerGhost)) {
         return true;
      } else {
         UUID masterUuid = playerGhost.getMasterUuid();
         return masterUuid == null || !masterUuid.equals(player.method_5667());
      }
   }

   public static void checkRevivalDegree(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(player, i);
         if (revivalDegree >= 1000) {
            LOGGER.debug("玩家 {} 槽位 {} 复苏程度达到1000，执行清除", player.method_5477().getString(), i);
            PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", 0);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               InstantKillUtil.executePlayerSelfKill(serverPlayer, null, false, true);
            }
            break;
         }
      }
   }

   public static boolean hasTaitouGhost(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.method_7909() == ModItems.TAITOU_GHOST) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasDitouGhost(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.method_7909() == ModItems.DITOU_GHOST) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasBoxGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "box_ghost");
   }

   private static boolean hasDeathSightGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "death_sight_ghost");
   }

   private static boolean hasGhostMerchant(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_merchant");
   }

   public static boolean hasJumpGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "jump_ghost");
   }

   public static boolean hasUntouchableGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "untouchable_ghost");
   }

   public static boolean hasFogGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "fog_ghost") || PlayerEvents.hasGhostType(player, "fog_ghost");
   }

   public static boolean hasBlockGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "block_ghost") || PlayerEvents.hasGhostType(player, "block_ghost");
   }

   public static boolean hasFoodGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "food_ghost") || PlayerEvents.hasGhostType(player, "food_ghost");
   }

   public static boolean hasCropGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "crop_ghost");
   }

   public static boolean hasStepGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "step_ghost");
   }

   public static boolean hasGraveEarthGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "grave_earth_ghost");
   }

   public static boolean hasTrashGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "trash_ghost");
   }

   public static boolean hasWaterGhost(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "water_ghost");
   }

   public static boolean hasGiantShadowGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "giant_shadow_ghost")
         || PlayerEvents.hasGhostType(player, "giant_shadow_ghost")
         || PlayerEvents.hasGhostType(player, "complete_shadow_ghost");
   }

   public static boolean hasGanshiBrideGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "ganshi_bride_ghost") || PlayerEvents.hasGhostType(player, "ganshi_bride_ghost");
   }

   public static void handleGiantShadowGhostSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int giantShadowGhostLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, giantShadowGhostLevel);
      List<LivingEntity> targetEntities = findBackFacingEntitiesInRange(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有背对你的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个背对自己的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个背对你的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findBackFacingEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> backFacingEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isBackFacingPlayer(player, entity)) {
            backFacingEntities.add(entity);
         }
      }

      return backFacingEntities;
   }

   private static boolean isBackFacingPlayer(PlayerEntity player, LivingEntity entity) {
      Vec3d entityLookVec = entity.method_5828(1.0F).method_1029();
      Vec3d toPlayerVec = new Vec3d(
            player.method_23317() - entity.method_23317(), player.method_23318() - entity.method_23318(), player.method_23321() - entity.method_23321()
         )
         .method_1029();
      double dotProduct = entityLookVec.method_1026(toPlayerVec);
      return dotProduct < 0.0;
   }

   public static void handleTaitouGhostSkill(PlayerEntity player) {
      boolean isShard = isHoldingShardItem(player, "taitou_ghost");
      handleHeadOrientationSkill(player, "抬头", p -> isShard || hasTaitouGhost(p), GhostDomainManager::findHeadUpEntitiesInRange);
   }

   public static void handleDitouGhostSkill(PlayerEntity player) {
      boolean isShard = isHoldingShardItem(player, "ditou_ghost");
      handleHeadOrientationSkill(player, "低头", p -> isShard || hasDitouGhost(p), GhostDomainManager::findHeadDownEntitiesInRange);
   }

   public static void handleBoxGhostSkill(PlayerEntity player) {
      boolean isShard = isHoldingShardItem(player, "box_ghost");
      handleContainerSkill(player, "开箱", p -> isShard || hasBoxGhost(p), GhostDomainManager::findContainerEntitiesInRange);
   }

   public static void handleDeathSightGhostSkill(PlayerEntity player) {
      boolean isShard = isHoldingShardItem(player, "death_sight_ghost");
      handleEyeContactSkill(player, "死亡凝视", p -> isShard || hasDeathSightGhost(p), GhostDomainManager::findEyeContactEntitiesInRange);
   }

   public static void handleGhostMerchantSkill(PlayerEntity player) {
      boolean isShard = isHoldingShardItem(player, "ghost_merchant");
      handleTradeSkill(player, "鬼商人", p -> isShard || hasGhostMerchant(p), GhostDomainManager::findTradeEntitiesInRange);
   }

   public static void handleJumpGhostSkill(PlayerEntity player) {
      boolean isShard = isHoldingShardItem(player, "jump_ghost");
      handleJumpSkill(player, "跳跃", p -> isShard || hasJumpGhost(p), GhostDomainManager::findJumpingEntitiesInRange);
   }

   private static void handleHeadOrientationSkill(
      PlayerEntity player, String orientation, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有" + orientation + "的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个{}的生物", player.method_5477().getString(), targetEntities.size(), orientation);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个" + orientation + "的生物"), true);
            }
         } else {
            boolean bothEquipped = hasTaitouGhost(player) && hasDitouGhost(player);
            attackNearestMarkedEntity(player, markedEntities, bothEquipped ? 2 : 1);
         }
      }
   }

   private static List<LivingEntity> findHeadUpEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> headUpEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         float pitch = entity.method_36455();
         if (pitch < -30.0F) {
            headUpEntities.add(entity);
         }
      }

      return headUpEntities;
   }

   private static List<LivingEntity> findHeadDownEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> headDownEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         float pitch = entity.method_36455();
         if (pitch > 30.0F) {
            headDownEntities.add(entity);
         }
      }

      return headDownEntities;
   }

   private static void handleContainerSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有靠近容器的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个靠近容器的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个靠近容器的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findContainerEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> containerEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isNearContainer(entity)) {
            containerEntities.add(entity);
         }
      }

      return containerEntities;
   }

   private static boolean isNearContainer(LivingEntity entity) {
      BlockPos entityPos = entity.method_24515();
      World world = entity.method_37908();

      for (int x = -3; x <= 3; x++) {
         for (int y = -3; y <= 3; y++) {
            for (int z = -3; z <= 3; z++) {
               BlockPos checkPos = entityPos.method_10069(x, y, z);
               BlockState blockState = world.method_8320(checkPos);
               if (blockState.method_26204() instanceof ChestBlock
                  || blockState.method_26204() instanceof BarrelBlock
                  || blockState.method_26204() instanceof ShulkerBoxBlock
                  || blockState.method_26204() instanceof DispenserBlock
                  || blockState.method_26204() instanceof DropperBlock
                  || blockState.method_26204() instanceof HopperBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void handleEyeContactSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有与你有眼神接触的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个有眼神接触的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个有眼神接触的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findEyeContactEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> eyeContactEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (hasEyeContact(player, entity)) {
            eyeContactEntities.add(entity);
         }
      }

      return eyeContactEntities;
   }

   private static boolean hasEyeContact(PlayerEntity player, LivingEntity entity) {
      Vec3d playerPos = player.method_19538();
      Vec3d entityPos = entity.method_19538();
      Vec3d directionToPlayer = playerPos.method_1020(entityPos).method_1029();
      Vec3d entityLookVec = entity.method_5828(1.0F);
      double dotProduct = entityLookVec.method_1026(directionToPlayer);
      return dotProduct > 0.866;
   }

   private static void handleTradeSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有可交易的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个可交易的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个可交易的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findTradeEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> tradeEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (hasTradeAbility(entity) || hasGhostMoney(player)) {
            tradeEntities.add(entity);
         }
      }

      return tradeEntities;
   }

   private static boolean hasGhostMoney(PlayerEntity player) {
      return player.method_31548().method_43256(itemStack -> itemStack.method_7985() && itemStack.method_7969().method_10545("ghostMoney"));
   }

   private static boolean hasTradeAbility(LivingEntity entity) {
      return entity instanceof VillagerEntity || entity instanceof WanderingTraderEntity || entity instanceof MerchantEntity;
   }

   private static void handleJumpSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有正在跳跃的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个正在跳跃的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个正在跳跃的生物"), true);
            }
         } else {
            boolean bothEquipped = hasJumpGhost(player) && GhostSkillManager.hasSneakGhostEquipped(player);
            attackNearestMarkedEntity(player, markedEntities, bothEquipped ? 2 : 1);
         }
      }
   }

   public static void handleFogGhostSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
      if (markedEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c鬼雾J键：范围内没有被标记的生物"), true);
         }
      } else {
         attackNearestMarkedEntity(player, markedEntities);
      }
   }

   public static void handleBlockGhostSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
      if (markedEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c方块鬼J键：范围内没有被标记的生物"), true);
         }
      } else {
         attackNearestMarkedEntity(player, markedEntities);
      }
   }

   public static void handleFoodGhostSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
      if (markedEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c食物鬼J键：范围内没有被标记的生物"), true);
         }
      } else {
         attackNearestMarkedEntity(player, markedEntities);
      }
   }

   public static void handleFoodGhostVSkill(PlayerEntity player) {
      int level = getFoodGhostLevel(player);
      int radius = getDomainRadius(player, level);
      LivingEntity targetEntity = findTargetEntity(player, radius);
      if (targetEntity == null) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c没有找到目标生物"), true);
         }
      } else {
         handleFamineEffect(player, targetEntity);
      }
   }

   private static LivingEntity findTargetEntity(PlayerEntity player, int radius) {
      return TargetingUtil.findEntityInLookDirectionWithOcclusion(player, radius, 0.866);
   }

   private static void handleFamineEffect(PlayerEntity player, LivingEntity target) {
      if (target instanceof PlayerEntity targetPlayer) {
         consumeTargetHunger(targetPlayer);
         LOGGER.debug("玩家 {} 使用食物鬼V键技能对玩家 {} 施加饥荒效果", player.method_5477().getString(), targetPlayer.method_5477().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§6对玩家 " + targetPlayer.method_5477().getString() + " 施加饥荒效果"), true);
         }
      } else {
         markEntity(player, target);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§6对生物 " + target.method_5477().getString() + " 施加饥荒标记"), true);
         }
      }
   }

   private static void consumeTargetHunger(PlayerEntity targetPlayer) {
      if (targetPlayer instanceof ServerPlayerEntity serverTarget) {
         int currentHunger = serverTarget.method_7344().method_7586();
         int hungerToConsume = Math.max(1, currentHunger / 2);
         int newHunger = Math.max(1, currentHunger - hungerToConsume);
         serverTarget.method_7344().method_7580(newHunger);
         serverTarget.method_7353(Text.method_43470("§c你被施加了饥荒效果，饱食度减少一半"), true);
         LOGGER.debug("玩家 {} 的饱食度从 {} 减少到 {}", targetPlayer.method_5477().getString(), currentHunger, newHunger);
      }
   }

   private static List<LivingEntity> findJumpingEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> jumpingEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isJumping(entity)) {
            jumpingEntities.add(entity);
         }
      }

      return jumpingEntities;
   }

   private static boolean isJumping(LivingEntity entity) {
      return entity.method_18798().field_1351 > 0.1;
   }

   private static void markEntities(PlayerEntity player, List<LivingEntity> entities) {
      for (LivingEntity entity : entities) {
         entity.method_6092(new StatusEffectInstance(ModEffects.MARK_CURSE, 200, 0, false, false, true));
      }
   }

   private static void markEntity(PlayerEntity player, LivingEntity entity) {
      entity.method_6092(new StatusEffectInstance(ModEffects.MARK_CURSE, 200, 0, false, false, true));
   }

   private static List<LivingEntity> findMarkedEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> markedEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (entity.method_6059(ModEffects.MARK_CURSE)) {
            markedEntities.add(entity);
         }
      }

      return markedEntities;
   }

   private static LivingEntity findNearestEntity(PlayerEntity player, List<LivingEntity> entities) {
      if (entities.isEmpty()) {
         return null;
      }

      LivingEntity nearestEntity = null;
      double minDistance = Double.MAX_VALUE;

      for (LivingEntity entity : entities) {
         double distance = player.method_5739(entity);
         if (distance < minDistance) {
            minDistance = distance;
            nearestEntity = entity;
         }
      }

      return nearestEntity;
   }

   public static void attackNearestMarkedEntity(PlayerEntity player, List<LivingEntity> markedEntities) {
      attackNearestMarkedEntity(player, markedEntities, 1);
   }

   public static void attackNearestMarkedEntity(PlayerEntity player, List<LivingEntity> markedEntities, int damageMultiplier) {
      if (!markedEntities.isEmpty()) {
         LivingEntity nearestEntity = findNearestEntity(player, markedEntities);
         if (nearestEntity != null) {
            double minDistance = player.method_5739(nearestEntity);
            LOGGER.debug("玩家 {} 攻击最近被标记的生物 {}，距离: {}", player.method_5477().getString(), nearestEntity.method_5477().getString(), minDistance);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               executeSkillSpiritAttack(serverPlayer, nearestEntity, damageMultiplier);
            }
         }
      }
   }

   public static void executeSkillSpiritAttack(ServerPlayerEntity player, LivingEntity target) {
      NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
      float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 0.0F;
      float tempSpiritDamage = spiritAttributes.method_10545("tempSpiritDamage") ? (float)spiritAttributes.method_10574("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = spiritAttributes.method_10545("tempSpiritDamageMultiplier")
         ? (float)spiritAttributes.method_10574("tempSpiritDamageMultiplier")
         : 1.0F;
      float totalSpiritDamage = spiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
      if (totalSpiritDamage <= 0.0F) {
         totalSpiritDamage = 5.0F;
      }

      executeSkillSpiritAttack(player, target, totalSpiritDamage);
   }

   public static void executeSkillSpiritAttack(ServerPlayerEntity player, LivingEntity target, int damageMultiplier) {
      NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
      float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 0.0F;
      float tempSpiritDamage = spiritAttributes.method_10545("tempSpiritDamage") ? (float)spiritAttributes.method_10574("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = spiritAttributes.method_10545("tempSpiritDamageMultiplier")
         ? (float)spiritAttributes.method_10574("tempSpiritDamageMultiplier")
         : 1.0F;
      float baseSpiritDamage = spiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
      if (baseSpiritDamage <= 0.0F) {
         baseSpiritDamage = 5.0F;
      }

      float totalDamage = baseSpiritDamage * damageMultiplier;
      executeSkillSpiritAttack(player, target, totalDamage);
   }

   public static void executeSkillSpiritAttack(ServerPlayerEntity player, LivingEntity target, float damageAmount) {
      ModEvents.processingSpiritDamage.set(true);

      try {
         DamageSource damageSource = player.method_48923().method_48802(player);
         if (target instanceof ServerPlayerEntity targetPlayer) {
            if (!ModConfig.getInstance().enablePlayerSpiritDamage) {
               return;
            }

            handleSkillDamageToPlayer(player, targetPlayer, damageSource, damageAmount);
         } else if (target instanceof GhostEntity ghost) {
            ghost.handleSkillSpiritDamage(player, damageAmount);
         } else {
            handleSkillDamageToEntity(player, target, damageSource, damageAmount);
         }

         if (!target.method_5805()) {
            QuestEventHandler.handleEntityKill(player, target);
         }
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }

   private static void handleSkillDamageToPlayer(ServerPlayerEntity attacker, ServerPlayerEntity target, DamageSource source, float damageAmount) {
      if (!ClientModConfig.getInstance().isDamageWhitelisted(attacker.method_5667(), target.method_5477().getString())) {
         float actualDamage = PlayerEvents.handleSpiritDamage(target, damageAmount, damageAmount, source);
         if (actualDamage > 0.0F && ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667())) {
            attacker.method_7353(
               Text.method_43470("§a对 §f" + target.method_5477().getString() + "§a 造成 §f" + new DecimalFormat("#.###").format(actualDamage) + "§a 点灵异伤害"), true
            );
         }

         if (damageAmount > 0.0F && !target.method_37908().method_8608()) {
            ServerWorld serverWorld = (ServerWorld)target.method_37908();
            Vec3d pos = target.method_19538();

            for (int i = 0; i < 25; i++) {
               double offsetX = (target.method_6051().method_43058() - 0.5) * 2.0;
               double offsetY = target.method_6051().method_43058() * 1.5 + 0.5;
               double offsetZ = (target.method_6051().method_43058() - 0.5) * 2.0;
               double velocityX = (target.method_6051().method_43058() - 0.5) * 0.2;
               double velocityY = target.method_6051().method_43058() * 0.3 + 0.1;
               double velocityZ = (target.method_6051().method_43058() - 0.5) * 0.2;
               serverWorld.method_14199(
                  ParticleTypes.field_11249,
                  pos.field_1352 + offsetX,
                  pos.field_1351 + offsetY,
                  pos.field_1350 + offsetZ,
                  1,
                  velocityX,
                  velocityY,
                  velocityZ,
                  0.5
               );
            }
         }
      }
   }

   private static void handleSkillDamageToEntity(ServerPlayerEntity attacker, LivingEntity target, DamageSource source, float damageAmount) {
      float healthBeforeDamage = target.method_6032();
      target.method_5643(source, damageAmount);
      if (!target.method_5805() && healthBeforeDamage > 0.0F) {
         QuestEventHandler.handleEntityKill(attacker, target);
      }

      if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.method_5667())) {
         float actualDamage = healthBeforeDamage - target.method_6032();
         attacker.method_7353(
            Text.method_43470("§a对 " + target.method_5477().getString() + " 造成了 " + new DecimalFormat("#.###").format(actualDamage) + " 点灵异伤害"), true
         );
      }

      if (damageAmount > 0.0F && !target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 25; i++) {
            double offsetX = (target.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = target.method_6051().method_43058() * 1.5 + 0.5;
            double offsetZ = (target.method_6051().method_43058() - 0.5) * 2.0;
            double velocityX = (target.method_6051().method_43058() - 0.5) * 0.2;
            double velocityY = target.method_6051().method_43058() * 0.3 + 0.1;
            double velocityZ = (target.method_6051().method_43058() - 0.5) * 0.2;
            serverWorld.method_14199(
               ParticleTypes.field_11249, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5
            );
         }
      }
   }

   public static boolean isSkillOnCooldown(PlayerEntity player, String skillName) {
      if (PlayerEvents.isGhostChildFused(player)) {
         return false;
      }

      NbtCompound data = PlayerEvents.getCachedData(player);
      String cooldownKey = "smfs_skill_cooldown_" + skillName;
      if (data.method_10545(cooldownKey)) {
         long cooldownEndTime = data.method_10537(cooldownKey);
         long currentTime = player.method_37908().method_8510();
         if (currentTime < cooldownEndTime) {
            return true;
         }

         data.method_10551(cooldownKey);
         PlayerEvents.saveDataToPlayer(player, data);
      }

      return false;
   }

   public static long getSkillCooldownRemaining(PlayerEntity player, String skillName) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      String cooldownKey = "smfs_skill_cooldown_" + skillName;
      if (data.method_10545(cooldownKey)) {
         long cooldownEndTime = data.method_10537(cooldownKey);
         long currentTime = player.method_37908().method_8510();
         if (currentTime < cooldownEndTime) {
            return cooldownEndTime - currentTime;
         }

         data.method_10551(cooldownKey);
         PlayerEvents.saveDataToPlayer(player, data);
      }

      return 0L;
   }

   public static void setSkillCooldown(PlayerEntity player, String skillName, int ticks) {
      if (!PlayerEvents.isGhostChildFused(player)) {
         NbtCompound data = PlayerEvents.getCachedData(player);
         String cooldownKey = "smfs_skill_cooldown_" + skillName;
         long currentTime = player.method_37908().method_8510();
         long cooldownEndTime = currentTime + ticks;
         data.method_10544(cooldownKey, cooldownEndTime);
         PlayerEvents.saveDataToPlayer(player, data);
      }
   }

   private static boolean consumeRevivalDegree(PlayerEntity player, int amount) {
      return true;
   }

   public static void markPlayerForGhost(ServerPlayerEntity player, String ghostName, String behavior) {
      List<LivingEntity> entities = new ArrayList<>();
      entities.add(player);
      markEntities(player, entities);
      LOGGER.debug("{}鬼域检测到玩家 {} {}，已自动标记", ghostName, player.method_5477().getString(), behavior);
   }

   public static void markEntityForFogGhost(ServerPlayerEntity player, Entity entity) {
      if (hasFogGhost(player) && entity instanceof LivingEntity) {
         Vec3d velocity = entity.method_18798();
         double speed = Math.sqrt(velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350);
         if (speed > 0.1) {
            List<LivingEntity> entities = new ArrayList<>();
            entities.add((LivingEntity)entity);
            markEntities(player, entities);
            LOGGER.debug("鬼雾鬼域检测到玩家 {} 附近移动实体 {}，速度：{}，已自动标记", player.method_5477().getString(), entity.method_5477().getString(), speed);
         }
      }
   }

   public static boolean hasQiaomenGhost(PlayerEntity player) {
      if (isHoldingShardItem(player, "qiaomen_ghost")) {
         return true;
      }

      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.method_7909() == ModItems.QIAOMEN_GHOST) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasVillagerGhost(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.method_7909() == ModItems.VILLAGER_GHOST) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasGhostWind(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.method_7909() == ModItems.GHOST_WIND) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasGhostOfficer(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_officer");
   }

   public static void handleQiaomenGhostSkill(PlayerEntity player) {
      HitResult hitResult = player.method_5745(20.0, 0.0F, false);
      if (hitResult.method_17783() == Type.field_1333) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c请对准一个门使用敲门鬼J键技能"), true);
         }
      } else {
         BlockPos targetPos;
         if (hitResult.method_17783() == Type.field_1332) {
            targetPos = ((BlockHitResult)hitResult).method_17777();
         } else {
            Entity targetEntity = ((EntityHitResult)hitResult).method_17782();
            targetPos = targetEntity.method_24515();
         }

         World world = player.method_37908();
         BlockState blockState = world.method_8320(targetPos);
         if (!(blockState.method_26204() instanceof DoorBlock)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c请对准一个门使用敲门鬼J键技能"), true);
            }
         } else {
            playKnockingSound(player, 1);
            List<LivingEntity> entitiesInRange = player.method_37908()
               .method_8390(LivingEntity.class, new Box(targetPos).method_1014(3.0), entity -> entity != player);
            int attackCount = 0;
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 0.0F;
            if (spiritDamage <= 0.0F) {
               spiritDamage = 5.0F;
            }

            for (LivingEntity target : entitiesInRange) {
               executeSkillSpiritAttack((ServerPlayerEntity)player, target);
               attackCount++;
            }

            LOGGER.debug("玩家 {} 使用敲门鬼J键技能，敲击了准星位置的门，对 {} 个生物进行了袭击", player.method_5477().getString(), attackCount);
            if (player instanceof ServerPlayerEntity serverPlayer
               && ModConfig.getInstance().showActionBarInfo
               && ClientModConfig.getInstance().showDamageText(player.method_5667())) {
               serverPlayer.method_7353(Text.method_43470("§a对" + attackCount + "个生物造成" + new DecimalFormat("#.###").format(spiritDamage) + "点灵异伤害"), true);
            }
         }
      }
   }

   public static void handleQiaomenGhostNSkill(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<BlockPos> doorPositions = findDoorPositionsInRange(player, radius);
      int doorCount = doorPositions.size();
      if (doorCount == 0) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有门，无法使用鬼敲门技能"), true);
         }
      } else {
         playKnockingSound(player, doorCount);
         List<LivingEntity> entitiesInRange = player.method_37908()
            .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entity -> entity != player);
         int attackedEntities = 0;

         for (LivingEntity target : entitiesInRange) {
            if (hasDoorNearby(target, 3)) {
               executeSkillSpiritAttack((ServerPlayerEntity)player, target, doorCount);
               attackedEntities++;
            }
         }

         LOGGER.debug("玩家 {} 使用敲门鬼N键技能，检测到 {} 扇门，对 {} 个生物进行了袭击（伤害倍率 {}x）", player.method_5477().getString(), doorCount, attackedEntities, doorCount);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§a鬼敲门：共敲响 " + doorCount + " 扇门，对 " + attackedEntities + " 个生物造成 " + doorCount + "倍 灵异叠加"), true);
         }
      }
   }

   public static void handleQiaomenGhostGSkill(PlayerEntity player) {
      HitResult hitResult = player.method_5745(20.0, 0.0F, false);
      if (hitResult.method_17783() != Type.field_1332) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c请对准一个门使用敲门鬼G键技能"), true);
         }
      } else {
         BlockHitResult blockHit = (BlockHitResult)hitResult;
         BlockPos hitPos = blockHit.method_17777();
         World world = player.method_37908();
         BlockState blockState = world.method_8320(hitPos);
         if (!(blockState.method_26204() instanceof DoorBlock)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c请对准一个门使用敲门鬼G键技能"), true);
            }
         } else {
            Direction hitFace = blockHit.method_17780();
            BlockPos teleportPos = hitPos.method_10093(hitFace);
            teleportPlayerToPosition(player, teleportPos);
            LOGGER.debug("玩家 {} 使用敲门鬼G键技能，传送到门位置 {}", player.method_5477().getString(), teleportPos);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已传送到目标门位置"), true);
            }
         }
      }
   }

   private static boolean hasDoorNearby(LivingEntity entity, int radius) {
      BlockPos entityPos = entity.method_24515();
      World world = entity.method_37908();

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = entityPos.method_10069(x, y, z);
               BlockState state = world.method_8320(checkPos);
               if (state.method_26204() instanceof DoorBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static void handleQiaomenGhostVSkill(PlayerEntity player) {
      HitResult hitResult = player.method_5745(20.0, 0.0F, false);
      if (hitResult.method_17783() == Type.field_1333) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c请对准一个位置使用敲门鬼V键技能"), true);
         }
      } else {
         BlockPos targetPos;
         if (hitResult.method_17783() == Type.field_1332) {
            BlockPos hitPos = ((BlockHitResult)hitResult).method_17777();
            BlockPos abovePos = hitPos.method_10084();
            World world = player.method_37908();
            if (world.method_8320(abovePos).method_26215() && world.method_8320(abovePos.method_10084()).method_26215()) {
               targetPos = abovePos;
            } else {
               Direction facing = player.method_5735();
               targetPos = hitPos.method_10093(facing);
               if (!world.method_8320(targetPos).method_26215() || !world.method_8320(targetPos.method_10084()).method_26215()) {
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.method_7353(Text.method_43470("§c目标位置不适合生成门"), true);
                  }

                  return;
               }
            }
         } else {
            targetPos = new BlockPos(
               (int)hitResult.method_17784().field_1352, (int)hitResult.method_17784().field_1351, (int)hitResult.method_17784().field_1350
            );
            World world = player.method_37908();
            if (!world.method_8320(targetPos).method_26215() || !world.method_8320(targetPos.method_10084()).method_26215()) {
               Direction facing = player.method_5735();
               targetPos = player.method_24515().method_10093(facing);
               if (!world.method_8320(targetPos).method_26215() || !world.method_8320(targetPos.method_10084()).method_26215()) {
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.method_7353(Text.method_43470("§c目标位置不适合生成门"), true);
                  }

                  return;
               }
            }
         }

         generateSingleDoor(targetPos, player.method_37908(), player);
         LOGGER.debug("玩家 {} 使用敲门鬼V键技能，在位置 {} 生成了一扇门", player.method_5477().getString(), targetPos);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§a已在目标位置生成一扇门"), true);
         }
      }
   }

   private static void handleDoorKnockingSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<BlockEntity>> doorFinder
   ) {
      if (!hasGhostChecker.test(player)) {
         LOGGER.warn("玩家 {} 尝试使用{}鬼技能但未驾驭{}鬼", player.method_5477().getString(), skillName, skillName);
      } else {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int radius = getDomainRadius(player, level);
         List<BlockEntity> targetDoors = doorFinder.apply(player, radius);
         if (targetDoors.isEmpty()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c范围内没有门"), true);
            }
         } else {
            List<BlockEntity> markedDoors = findMarkedDoorsInRange(player, radius);
            if (markedDoors.isEmpty()) {
               markDoors(player, targetDoors);
               LOGGER.debug("玩家 {} 标记了 {} 扇门", player.method_5477().getString(), targetDoors.size());
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetDoors.size() + " 扇门"), true);
               }
            } else {
               knockNearestMarkedDoor(player, markedDoors);
            }
         }
      }
   }

   private static List<BlockEntity> findDoorEntitiesInRange(PlayerEntity player, int radius) {
      List<BlockEntity> doors = new ArrayList<>();
      BlockPos playerPos = player.method_24515();
      World world = player.method_37908();

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = playerPos.method_10069(x, y, z);
               BlockState blockState = world.method_8320(checkPos);
               if (blockState.method_26204() instanceof DoorBlock) {
                  BlockEntity blockEntity = world.method_8321(checkPos);
                  if (blockEntity != null) {
                     doors.add(blockEntity);
                  }
               }
            }
         }
      }

      return doors;
   }

   private static List<BlockEntity> findDoorsInRange(PlayerEntity player, int radius) {
      return findDoorEntitiesInRange(player, radius);
   }

   private static List<BlockPos> findDoorPositionsInRange(PlayerEntity player, int radius) {
      List<BlockPos> doorPositions = new ArrayList<>();
      BlockPos playerPos = player.method_24515();
      World world = player.method_37908();
      int radiusSquared = radius * radius;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + y * y + z * z <= radiusSquared) {
                  BlockPos checkPos = playerPos.method_10069(x, y, z);
                  BlockState blockState = world.method_8320(checkPos);
                  if (blockState.method_26204() instanceof DoorBlock) {
                     doorPositions.add(checkPos);
                  }
               }
            }
         }
      }

      return doorPositions;
   }

   private static List<BlockEntity> findMarkedDoorsInRange(PlayerEntity player, int radius) {
      return new ArrayList<>();
   }

   private static void markDoors(PlayerEntity player, List<BlockEntity> doors) {
   }

   private static void knockNearestMarkedDoor(PlayerEntity player, List<BlockEntity> markedDoors) {
      if (!markedDoors.isEmpty()) {
         BlockEntity nearestDoor = markedDoors.get(0);
         playKnockingSound(player, 1);
         LOGGER.debug("玩家 {} 敲击了门", player.method_5477().getString());
      }
   }

   private static void playKnockingSound(PlayerEntity player, int knockCount) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         for (int i = 0; i < knockCount; i++) {
            serverPlayer.method_5783(SoundEvents.field_14541, 1.0F, 1.0F);
         }
      }
   }

   private static void generateDoorCircle(LivingEntity target, PlayerEntity player) {
      World world = target.method_37908();
      BlockPos centerPos = target.method_24515();
      int radius = 3;

      for (int i = 0; i < 8; i++) {
         double angle = (Math.PI * 2) * i / 8.0;
         int x = centerPos.method_10263() + (int)(radius * Math.cos(angle));
         int z = centerPos.method_10260() + (int)(radius * Math.sin(angle));
         BlockPos doorPos = new BlockPos(x, centerPos.method_10264(), z);
         if (world.method_8320(doorPos).method_26215() && world.method_8320(doorPos.method_10084()).method_26215()) {
            Direction facing = Direction.method_10150(angle * 180.0 / Math.PI);
            world.method_8501(
               doorPos,
               (BlockState)((BlockState)((BlockState)((BlockState)Blocks.field_10149.method_9564().method_11657(DoorBlock.field_10938, facing))
                        .method_11657(DoorBlock.field_10941, DoorHinge.field_12588))
                     .method_11657(DoorBlock.field_10945, false))
                  .method_11657(DoorBlock.field_10946, DoubleBlockHalf.field_12607)
            );
            world.method_8501(
               doorPos.method_10084(),
               (BlockState)((BlockState)((BlockState)((BlockState)Blocks.field_10149.method_9564().method_11657(DoorBlock.field_10938, facing))
                        .method_11657(DoorBlock.field_10941, DoorHinge.field_12588))
                     .method_11657(DoorBlock.field_10945, false))
                  .method_11657(DoorBlock.field_10946, DoubleBlockHalf.field_12609)
            );
         }
      }
   }

   private static void generateSingleDoor(BlockPos pos, World world, PlayerEntity player) {
      if (world.method_8320(pos).method_26215() && world.method_8320(pos.method_10084()).method_26215()) {
         Direction facing = player.method_5735();
         world.method_8501(
            pos,
            (BlockState)((BlockState)((BlockState)((BlockState)Blocks.field_10149.method_9564().method_11657(DoorBlock.field_10938, facing))
                     .method_11657(DoorBlock.field_10941, DoorHinge.field_12588))
                  .method_11657(DoorBlock.field_10945, false))
               .method_11657(DoorBlock.field_10946, DoubleBlockHalf.field_12607)
         );
         world.method_8501(
            pos.method_10084(),
            (BlockState)((BlockState)((BlockState)((BlockState)Blocks.field_10149.method_9564().method_11657(DoorBlock.field_10938, facing))
                     .method_11657(DoorBlock.field_10941, DoorHinge.field_12588))
                  .method_11657(DoorBlock.field_10945, false))
               .method_11657(DoorBlock.field_10946, DoubleBlockHalf.field_12609)
         );
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_5783(SoundEvents.field_14718, 1.0F, 1.0F);
         }
      } else {
         LOGGER.warn("无法在位置 {} 生成门，位置已被占用", pos);
      }
   }

   private static boolean isSafeTeleportPosition(World world, BlockPos pos) {
      BlockPos feetPos = pos;
      BlockPos headPos = pos.method_10084();
      return world.method_8320(feetPos).method_26215() && world.method_8320(headPos).method_26215();
   }

   private static void teleportPlayerToPosition(PlayerEntity player, BlockPos targetPos) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         double x = targetPos.method_10263() + 0.5;
         double y = targetPos.method_10264();
         double z = targetPos.method_10260() + 0.5;
         serverPlayer.method_20620(x, y, z);
         serverPlayer.method_5783(SoundEvents.field_14879, 1.0F, 1.0F);
      }
   }

   public static void handleVillagerGhostNSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 使用村民鬼N键技能：引爆目标鬼奴", player.method_5477().getString());
      int villagerGhostSlot = PlayerEvents.findEquippedVillagerGhostSlot(player);
      int villagerGhostLevel = PlayerEvents.getGhostSlotLevel(player, villagerGhostSlot);
      int domainRadius = getDomainRadius(player, villagerGhostLevel);
      List<GhostSlaveEntity> ghostSlaves = findGhostSlavesInRange(player, domainRadius);
      if (ghostSlaves.isEmpty()) {
         LOGGER.debug("玩家 {} 鬼域范围内没有鬼奴", player.method_5477().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c鬼域范围内没有鬼奴"), true);
         }
      } else {
         int explodedCount = 0;

         for (GhostSlaveEntity ghostSlave : ghostSlaves) {
            if (explodeGhostSlave(ghostSlave, player)) {
               explodedCount++;
            }
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§a成功引爆 " + explodedCount + " 个鬼奴"), true);
         }

         LOGGER.debug("玩家 {} 引爆了 {} 个鬼奴", player.method_5477().getString(), explodedCount);
      }
   }

   private static List<GhostSlaveEntity> findGhostSlavesInRange(PlayerEntity player, int radius) {
      List<GhostSlaveEntity> ghostSlaves = new ArrayList<>();
      BlockPos playerPos = player.method_24515();
      World world = player.method_37908();

      for (LivingEntity entity : world.method_8390(
         LivingEntity.class,
         new Box(
            playerPos.method_10263() - radius,
            playerPos.method_10264() - radius,
            playerPos.method_10260() - radius,
            playerPos.method_10263() + radius,
            playerPos.method_10264() + radius,
            playerPos.method_10260() + radius
         ),
         entityx -> entityx instanceof GhostSlaveEntity
      )) {
         if (entity instanceof GhostSlaveEntity) {
            ghostSlaves.add((GhostSlaveEntity)entity);
         }
      }

      return ghostSlaves;
   }

   private static boolean explodeGhostSlave(GhostSlaveEntity ghostSlave, PlayerEntity player) {
      try {
         BlockPos ghostSlavePos = ghostSlave.method_24515();
         World world = ghostSlave.method_37908();
         float explosionRadius = 4.0F;
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 0.0F;
         float tempSpiritDamage = spiritAttributes.method_10545("tempSpiritDamage") ? (float)spiritAttributes.method_10574("tempSpiritDamage") : 0.0F;
         float tempSpiritDamageMultiplier = spiritAttributes.method_10545("tempSpiritDamageMultiplier")
            ? (float)spiritAttributes.method_10574("tempSpiritDamageMultiplier")
            : 1.0F;
         float totalSpiritDamage = spiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
         if (totalSpiritDamage <= 0.0F) {
            totalSpiritDamage = 5.0F;
         }

         float explosionDamage = totalSpiritDamage * 1.2F;
         List<LivingEntity> entitiesInRange = world.method_8390(
            LivingEntity.class,
            new Box(
               ghostSlavePos.method_10263() - explosionRadius,
               ghostSlavePos.method_10264() - explosionRadius,
               ghostSlavePos.method_10260() - explosionRadius,
               ghostSlavePos.method_10263() + explosionRadius,
               ghostSlavePos.method_10264() + explosionRadius,
               ghostSlavePos.method_10260() + explosionRadius
            ),
            entity -> entity != player && entity.method_5805() && !(entity instanceof GhostSlaveEntity)
         );
         DamageSource damageSource = ModDamageSources.ghost(world);

         for (LivingEntity target : entitiesInRange) {
            double distance = target.method_5739(ghostSlave);
            if (distance <= explosionRadius) {
               float distanceFactor = (float)Math.max(0.2, 1.0 - distance / explosionRadius * 0.8);
               float actualDamage = explosionDamage * distanceFactor;
               if (target instanceof ServerPlayerEntity targetPlayer) {
                  PlayerEvents.handleSpiritDamage(targetPlayer, actualDamage, actualDamage, damageSource);
               } else if (target instanceof GhostEntity ghost) {
                  ghost.handleSkillSpiritDamage(player instanceof ServerPlayerEntity sp ? sp : null, actualDamage);
               } else {
                  target.method_5643(damageSource, actualDamage);
               }
            }
         }

         if (world instanceof ServerWorld serverWorld) {
            serverWorld.method_14199(
               ParticleTypes.field_11236,
               ghostSlavePos.method_10263() + 0.5,
               ghostSlavePos.method_10264() + 1.0,
               ghostSlavePos.method_10260() + 0.5,
               15,
               0.8,
               0.8,
               0.8,
               0.2
            );
            serverWorld.method_14199(
               ParticleTypes.field_11249,
               ghostSlavePos.method_10263() + 0.5,
               ghostSlavePos.method_10264() + 1.0,
               ghostSlavePos.method_10260() + 0.5,
               20,
               0.5,
               0.5,
               0.5,
               0.1
            );
         }

         world.method_8396(null, ghostSlavePos, SoundEvents.field_15152, SoundCategory.field_15251, 1.5F, 0.8F);
         ghostSlave.method_31472();
         return true;
      } catch (Exception e) {
         LOGGER.error("引爆鬼奴时发生错误", e);
         return false;
      }
   }

   public static void handleVillagerGhostGSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 使用村民鬼G键技能：给鬼域范围内的鬼奴添加力量1buff", player.method_5477().getString());
      int villagerGhostSlot = PlayerEvents.findEquippedVillagerGhostSlot(player);
      int villagerGhostLevel = PlayerEvents.getGhostSlotLevel(player, villagerGhostSlot);
      int domainRadius = getDomainRadius(player, villagerGhostLevel);
      List<GhostSlaveEntity> ghostSlaves = findGhostSlavesInRange(player, domainRadius);
      if (ghostSlaves.isEmpty()) {
         LOGGER.debug("玩家 {} 鬼域范围内没有鬼奴", player.method_5477().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c鬼域范围内没有鬼奴"), true);
         }
      } else {
         int buffedCount = 0;

         for (GhostSlaveEntity ghostSlave : ghostSlaves) {
            if (addStrengthBuffToGhostSlave(ghostSlave, player)) {
               buffedCount++;
            }
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§a成功给 " + buffedCount + " 个鬼奴添加力量1buff"), true);
         }

         LOGGER.debug("玩家 {} 给 {} 个鬼奴添加了力量1buff", player.method_5477().getString(), buffedCount);
      }
   }

   public static void handleVillagerGhostVSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 使用村民鬼V键技能：急速", player.method_5477().getString());
      int villagerGhostSlot = PlayerEvents.findEquippedVillagerGhostSlot(player);
      int villagerGhostLevel = PlayerEvents.getGhostSlotLevel(player, villagerGhostSlot);
      int domainRadius = getDomainRadius(player, villagerGhostLevel);
      List<GhostSlaveEntity> ghostSlaves = findGhostSlavesInRange(player, domainRadius);
      if (ghostSlaves.isEmpty()) {
         LOGGER.debug("玩家 {} 鬼域范围内没有鬼奴", player.method_5477().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c鬼域范围内没有鬼奴"), true);
         }
      } else {
         int buffedCount = 0;

         for (GhostSlaveEntity ghostSlave : ghostSlaves) {
            if (addSpeedBuffToGhostSlave(ghostSlave, player)) {
               buffedCount++;
            }
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§a成功给 " + buffedCount + " 个鬼奴添加速度1buff"), true);
         }

         LOGGER.debug("玩家 {} 给 {} 个鬼奴添加了速度1buff", player.method_5477().getString(), buffedCount);
      }
   }

   private static boolean addStrengthBuffToGhostSlave(GhostSlaveEntity ghostSlave, PlayerEntity player) {
      try {
         StatusEffectInstance strengthEffect = new StatusEffectInstance(StatusEffects.field_5910, 100, 0, true, true);
         ghostSlave.method_6092(strengthEffect);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_5783(SoundEvents.field_14709, 0.5F, 1.0F);
         }

         return true;
      } catch (Exception e) {
         LOGGER.error("给鬼奴添加力量buff时发生错误", e);
         return false;
      }
   }

   private static boolean addSpeedBuffToGhostSlave(GhostSlaveEntity ghostSlave, PlayerEntity player) {
      try {
         StatusEffectInstance speedEffect = new StatusEffectInstance(StatusEffects.field_5904, 100, 3, false, true);
         ghostSlave.method_6092(speedEffect);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_5783(SoundEvents.field_14709, 0.5F, 1.0F);
         }

         return true;
      } catch (Exception e) {
         LOGGER.error("给鬼奴添加速度buff时发生错误", e);
         return false;
      }
   }

   public static int getGhostDomainLevel(PlayerEntity player) {
      return !isGhostDomainActive(player) ? 0 : getCurrentLevel(player);
   }

   public static void handleCropGhostSkill(PlayerEntity player) {
      handleCropSkill(player, "作物", GhostDomainManager::hasCropGhost, GhostDomainManager::findCropEntitiesInRange);
   }

   public static void handleStepGhostSkill(PlayerEntity player) {
      handleStepSkill(player, "踩人", GhostDomainManager::hasStepGhost, GhostDomainManager::findStepEntitiesInRange);
   }

   public static void handleGraveEarthGhostSkill(PlayerEntity player) {
      if (player.method_37908() instanceof ServerWorld serverWorld) {
         BlockPos var6 = null;
         HitResult hitResult = player.method_5745(20.0, 0.0F, false);
         if (hitResult.method_17783() == Type.field_1331) {
            Entity target = ((EntityHitResult)hitResult).method_17782();
            if (target instanceof LivingEntity) {
               var6 = target.method_24515();
            }
         } else if (hitResult.method_17783() == Type.field_1332) {
            BlockHitResult blockHit = (BlockHitResult)hitResult;
            var6 = blockHit.method_17777().method_10093(blockHit.method_17780());
         }

         if (var6 != null) {
            BlockPos groundPos = var6;

            while (groundPos.method_10264() > serverWorld.method_31607() && serverWorld.method_8320(groundPos).method_26215()) {
               groundPos = groundPos.method_10074();
            }

            BlockPos finalPos = groundPos.method_10084();
            if (serverWorld.method_8320(finalPos).method_26215()) {
               serverWorld.method_8501(finalPos, ModBlocks.GRAVE_MOUND.method_9564());
            }
         }
      }
   }

   public static void handleTrashGhostSkill(PlayerEntity player) {
      handleTrashSkill(player, "垃圾", GhostDomainManager::hasTrashGhost, GhostDomainManager::findTrashEntitiesInRange);
   }

   public static void handleWaterGhostSkill(PlayerEntity player) {
      handleWaterSkill(player, "水", GhostDomainManager::hasWaterGhost, GhostDomainManager::findWaterEntitiesInRange);
   }

   private static void handleCropSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int radius = 20;
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static void handleStepSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int radius = 20;
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static void handleTrashSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int radius = 20;
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static void handleWaterSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<LivingEntity>> entityFinder
   ) {
      int radius = 20;
      List<LivingEntity> targetEntities = entityFinder.apply(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
            }
         } else {
            boolean bothEquipped = hasValidGhostFire(player) && hasWaterGhost(player);
            attackNearestMarkedEntity(player, markedEntities, bothEquipped ? 2 : 1);
         }
      }
   }

   private static List<LivingEntity> findCropEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> cropEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isOnCrop(entity)) {
            cropEntities.add(entity);
         }
      }

      return cropEntities;
   }

   private static boolean isOnCrop(LivingEntity entity) {
      BlockPos entityPos = entity.method_24515();
      BlockState blockBelow = entity.method_37908().method_8320(entityPos.method_10074());
      BlockState blockAtFeet = entity.method_37908().method_8320(entityPos);
      return blockBelow.method_26164(BlockTags.field_20341)
         || blockAtFeet.method_26164(BlockTags.field_20341)
         || blockBelow.method_26204() instanceof FarmlandBlock
         || blockAtFeet.method_26204() instanceof FarmlandBlock;
   }

   private static List<LivingEntity> findStepEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> stepEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (entity.method_23318() < player.method_23318()) {
            stepEntities.add(entity);
         }
      }

      return stepEntities;
   }

   private static List<LivingEntity> findTrashEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> trashEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (hasItemsAround(entity, 6)) {
            trashEntities.add(entity);
         }
      }

      return trashEntities;
   }

   private static boolean hasItemsAround(LivingEntity entity, int range) {
      BlockPos entityPos = entity.method_24515();
      World world = entity.method_37908();

      for (int x = -range; x <= range; x++) {
         for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
               BlockPos checkPos = entityPos.method_10069(x, y, z);
               List<ItemEntity> items = world.method_8390(ItemEntity.class, new Box(checkPos), item -> true);
               if (!items.isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static List<LivingEntity> findWaterEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> waterEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isInWater(entity)) {
            waterEntities.add(entity);
         }
      }

      return waterEntities;
   }

   private static boolean isInWater(LivingEntity entity) {
      return entity.method_5799() || entity.method_5869();
   }

   public static void handleGanshiBrideGhostSkill(PlayerEntity player) {
      int radius = 20;
      List<LivingEntity> targetEntities = findGanshiBrideTargetsInRange(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.method_5477().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
            }
         } else {
            depriveGhostFromNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findGanshiBrideTargetsInRange(PlayerEntity player, int radius) {
      List<LivingEntity> targets = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (!(
               entity instanceof PlayerEntity targetPlayer
                  && hasGanshiBrideGhost(targetPlayer)
                  && targetPlayer instanceof ServerPlayerEntity serverTarget
                  && AdvancementManager.hasAdvancement(serverTarget, "smfs:become_aberration")
            )
            && hasAnyGhostItem(entity)) {
            targets.add(entity);
         }
      }

      return targets;
   }

   private static boolean hasAnyGhostItem(LivingEntity entity) {
      return entity instanceof PlayerEntity targetPlayer ? PlayerEvents.hasOccupiedGhostSlot(targetPlayer) : false;
   }

   public static boolean hasWishGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "wish_ghost") || PlayerEvents.hasGhostType(player, "wish_ghost");
   }

   private static void depriveGhostFromNearestMarkedEntity(PlayerEntity player, List<LivingEntity> markedEntities) {
      if (!markedEntities.isEmpty()) {
         LivingEntity nearestEntity = findNearestEntity(player, markedEntities);
         if (nearestEntity != null) {
            if (nearestEntity instanceof PlayerEntity targetPlayer) {
               String deprivedGhostType = depriveRandomGhostFromPlayer(targetPlayer);
               if (deprivedGhostType != null) {
                  giveGhostItemToPlayer(player, deprivedGhostType);
                  LOGGER.debug("玩家 {} 剥夺了玩家 {} 的 {} 鬼，并获得了对应的驾驭物品", player.method_5477().getString(), targetPlayer.method_5477().getString(), deprivedGhostType);
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.method_7353(
                        Text.method_43470("§6成功剥夺了玩家 " + targetPlayer.method_5477().getString() + " 的 " + deprivedGhostType + " 鬼，并获得了对应的驾驭物品"), true
                     );
                  }
               } else {
                  LOGGER.debug("玩家 {} 剥夺了玩家 {} 的一个鬼物品", player.method_5477().getString(), targetPlayer.method_5477().getString());
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.method_7353(Text.method_43470("§6成功剥夺了玩家 " + targetPlayer.method_5477().getString() + " 的一个鬼物品"), true);
                  }
               }

               if (targetPlayer instanceof ServerPlayerEntity targetServerPlayer) {
                  targetServerPlayer.method_7353(Text.method_43470("§c你的一个鬼物品被玩家 " + player.method_5477().getString() + " 剥夺了"), true);
               }
            }
         }
      }
   }

   private static String depriveRandomGhostFromPlayer(PlayerEntity targetPlayer) {
      List<String> ghostTypes = new ArrayList<>();

      for (int i = 0; i < 10; i++) {
         String ghostType = PlayerEvents.getGhostTypeInSlot(targetPlayer, i);
         if (ghostType != null && !ghostType.isEmpty()) {
            ghostTypes.add(ghostType);
         }
      }

      if (ghostTypes.isEmpty()) {
         return null;
      }

      Random random = new Random();
      String selectedGhost = ghostTypes.get(random.nextInt(ghostTypes.size()));
      PlayerEvents.removeGhostByType(targetPlayer, selectedGhost);
      return selectedGhost;
   }

   private static void giveGhostItemToPlayer(PlayerEntity player, String ghostType) {
      if (ghostType != null && !ghostType.isEmpty()) {
         Item itemToGive = null;
         switch (ghostType) {
            case "ghost_fire":
               itemToGive = ModItems.GHOST_FIRE;
               break;
            case "fog_ghost":
               itemToGive = ModItems.FOG_GHOST;
               break;
            case "block_ghost":
               itemToGive = ModItems.BLOCK_GHOST;
               break;
            case "food_ghost":
               itemToGive = ModItems.FOOD_GHOST;
               break;
            case "qiaomen_ghost":
               itemToGive = ModItems.QIAOMEN_GHOST;
               break;
            case "villager_ghost":
               itemToGive = ModItems.VILLAGER_GHOST;
               break;
            case "silent_ghost_eye":
               itemToGive = ModItems.SILENT_GHOST_EYE;
               break;
            case "silent_ghost":
               itemToGive = ModItems.SILENT_GHOST;
               break;
            case "taitou_ghost":
               itemToGive = ModItems.TAITOU_GHOST;
               break;
            case "ditou_ghost":
               itemToGive = ModItems.DITOU_GHOST;
               break;
            case "box_ghost":
               itemToGive = ModItems.BOX_GHOST;
               break;
            case "jump_ghost":
               itemToGive = ModItems.JUMP_GHOST;
               break;
            case "untouchable_ghost":
               itemToGive = ModItems.UNTOUCHABLE_GHOST;
               break;
            case "death_sight_ghost":
               itemToGive = ModItems.DEATH_SIGHT_GHOST;
               break;
            case "ghost_merchant":
               itemToGive = ModItems.GHOST_MERCHANT;
               break;
            case "lost_ghost":
               itemToGive = ModItems.LOST_GHOST;
               break;
            case "giant_shadow_ghost":
               itemToGive = ModItems.GIANT_SHADOW_GHOST;
               break;
            case "ganshi_bride_ghost":
               itemToGive = ModItems.GANSHI_BRIDE_GHOST;
               break;
            case "crop_ghost":
               itemToGive = ModItems.CROP_GHOST;
               break;
            case "step_ghost":
               itemToGive = ModItems.STEP_GHOST;
               break;
            case "trash_ghost":
               itemToGive = ModItems.TRASH_GHOST;
               break;
            case "water_ghost":
               itemToGive = ModItems.WATER_GHOST;
               break;
            default:
               LOGGER.warn("未知的鬼类型: {}", ghostType);
               return;
         }

         if (itemToGive != null) {
            ItemStack itemStack = new ItemStack(itemToGive, 1);
            if (!player.method_31548().method_7394(itemStack)) {
               player.method_37908()
                  .method_8649(new ItemEntity(player.method_37908(), player.method_23317(), player.method_23318(), player.method_23321(), itemStack));
               LOGGER.debug("玩家 {} 背包已满，{} 鬼物品掉落在地面上", player.method_5477().getString(), ghostType);
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.method_7353(Text.method_43470("§e背包已满，" + ghostType + " 鬼物品掉落在地面上"), true);
               }
            } else {
               LOGGER.debug("玩家 {} 成功获得了 {} 鬼物品", player.method_5477().getString(), ghostType);
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.method_7353(Text.method_43470("§a成功获得了 " + ghostType + " 鬼物品"), true);
               }
            }
         }
      }
   }

   public static void handleWishGhostNSkill(PlayerEntity player) {
      handleWishGhostNSkill(player, true);
   }

   public static void handleWishGhostNSkill(PlayerEntity player, boolean showMessage) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         int currentMaxHealth = (int)serverPlayer.method_6063();
         if (currentMaxHealth <= 4) {
            serverPlayer.method_7353(Text.method_43470("§c生命上限不足，无法使用技能"), true);
            return;
         }

         serverPlayer.method_5996(EntityAttributes.field_23716).method_6192(currentMaxHealth - 4);
         float currentHealth = serverPlayer.method_6032();
         serverPlayer.method_5643(serverPlayer.method_48923().method_48830(), 1.0F);
         serverPlayer.method_6033(currentHealth);
         LOGGER.debug("玩家 {} 使用许愿鬼N键技能，扣除4点生命上限，当前生命上限：{}", player.method_5477().getString(), currentMaxHealth - 4);
      }

      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> ghostEntities = findGhostEntitiesInRange(player, radius);
      if (ghostEntities.isEmpty()) {
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c许愿鬼N键：鬼域范围内没有鬼"), true);
         }
      } else {
         for (LivingEntity ghost : ghostEntities) {
            if (ghost instanceof GhostEntity) {
               ghost.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 1200, 255, false, false, true));
            }
         }

         LOGGER.debug("玩家 {} 使用许愿鬼N键技能，给 {} 个鬼添加了1分钟沉寂效果", player.method_5477().getString(), ghostEntities.size());
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§6我说眼前灵异必将退散"), false);
         }
      }
   }

   public static void handleWishGhostGSkill(PlayerEntity player) {
      handleWishGhostGSkill(player, true);
   }

   public static void handleWishGhostGSkill(PlayerEntity player, boolean showMessage) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         int currentMaxHealth = (int)serverPlayer.method_6063();
         if (currentMaxHealth <= 3) {
            serverPlayer.method_7353(Text.method_43470("§c生命上限不足，无法使用技能"), true);
            return;
         }

         serverPlayer.method_5996(EntityAttributes.field_23716).method_6192(currentMaxHealth - 3);
         float currentHealth = serverPlayer.method_6032();
         serverPlayer.method_5643(serverPlayer.method_48923().method_48830(), 1.0F);
         serverPlayer.method_6033(currentHealth);
         LOGGER.debug("玩家 {} 使用许愿鬼G键技能，扣除3点生命上限，当前生命上限：{}", player.method_5477().getString(), currentMaxHealth - 3);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_6092(new StatusEffectInstance(StatusEffects.field_5907, 600, 4, false, false, true));
         serverPlayer.method_6092(new StatusEffectInstance(StatusEffects.field_5910, 600, 254, false, false, true));
      }

      LOGGER.debug("玩家 {} 使用许愿鬼G键技能，获得30秒抗性5和力量255", player.method_5477().getString());
      if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_7353(Text.method_43470("§6我说我行不可摧，志不可改，力可至极限"), false);
      }
   }

   public static void handleWishGhostVSkill(PlayerEntity player) {
      handleWishGhostVSkill(player, true);
   }

   public static void handleWishGhostVSkill(PlayerEntity player, boolean showMessage) {
      boolean hasLostEffect = player.method_6059(ModEffects.LOST);
      boolean hasGhostDomainTargetEffect = player.method_6059(ModEffects.RED_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.GREEN_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.BLUE_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.GRAY_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.PURPLE_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.BLACK_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.CYAN_GHOST_DOMAIN_TARGET)
         || player.method_6059(ModEffects.THICK_FOG_TARGET);
      if (hasLostEffect && hasGhostDomainTargetEffect) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            int currentMaxHealth = (int)serverPlayer.method_6063();
            if (currentMaxHealth <= 2) {
               serverPlayer.method_7353(Text.method_43470("§c生命上限不足，无法使用技能"), true);
               return;
            }

            serverPlayer.method_5996(EntityAttributes.field_23716).method_6192(currentMaxHealth - 2);
            float currentHealth = serverPlayer.method_6032();
            serverPlayer.method_5643(serverPlayer.method_48923().method_48830(), 1.0F);
            serverPlayer.method_6033(currentHealth);
            LOGGER.debug("玩家 {} 使用许愿鬼V键技能，扣除2点生命上限，当前生命上限：{}", player.method_5477().getString(), currentMaxHealth - 2);
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_6012();
            LOGGER.debug("玩家 {} 使用许愿鬼V键技能，清除了所有buff", player.method_5477().getString());
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            BlockPos safePos = findSafePosition(serverPlayer, 100);
            if (safePos == null) {
               if (showMessage) {
                  serverPlayer.method_7353(Text.method_43470("§c许愿鬼V键：找不到安全位置"), true);
               }

               return;
            }

            serverPlayer.method_20620(safePos.method_10263() + 0.5, safePos.method_10264(), safePos.method_10260() + 0.5);
            LOGGER.debug("玩家 {} 使用许愿鬼V键技能，传送到安全位置: {}", player.method_5477().getString(), safePos);
         }

         LOGGER.debug("玩家 {} 使用许愿鬼V键技能，清除所有buff并传送到安全地方", player.method_5477().getString());
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§6我说我必离开这片鬼域"), false);
         }
      } else {
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c许愿鬼：没有被困在鬼域范围内。"), true);
         }
      }
   }

   public static void handleWishGhostJSkill(PlayerEntity player) {
      handleWishGhostJSkill(player, true);
   }

   public static void handleWishGhostJSkill(PlayerEntity player, boolean showMessage) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         int currentMaxHealth = (int)serverPlayer.method_6063();
         if (currentMaxHealth <= 1) {
            serverPlayer.method_7353(Text.method_43470("§c生命上限不足，无法使用技能"), true);
            return;
         }

         serverPlayer.method_5996(EntityAttributes.field_23716).method_6192(currentMaxHealth - 1);
         float currentHealth = serverPlayer.method_6032();
         serverPlayer.method_5643(serverPlayer.method_48923().method_48830(), 1.0F);
         serverPlayer.method_6033(currentHealth);
         LOGGER.debug("玩家 {} 使用许愿鬼J键技能，扣除1点生命上限，当前生命上限：{}", player.method_5477().getString(), currentMaxHealth - 1);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_6033(255.0F);
         LOGGER.debug("玩家 {} 使用许愿鬼J键技能，恢复满生命值（255）", player.method_5477().getString());
      }

      LOGGER.debug("玩家 {} 使用许愿鬼J键技能，扣除1点生命上限并恢复满生命值", player.method_5477().getString());
      if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_7353(Text.method_43470("§6我说我身强体壮，灾病全无"), false);
      }
   }

   private static BlockPos findSafePosition(ServerPlayerEntity player, int radius) {
      World world = player.method_37908();
      BlockPos playerPos = player.method_24515();
      Random random = new Random();

      for (int attempt = 0; attempt < 50; attempt++) {
         int x = playerPos.method_10263() + random.nextInt(radius * 2) - radius;
         int z = playerPos.method_10260() + random.nextInt(radius * 2) - radius;
         BlockPos candidatePos = findSafeYPosition(world, new BlockPos(x, playerPos.method_10264(), z));
         if (candidatePos != null && isPositionSafe(world, candidatePos)) {
            return candidatePos;
         }
      }

      return null;
   }

   private static BlockPos findSafeYPosition(World world, BlockPos pos) {
      for (int y = pos.method_10264(); y < world.method_31600(); y++) {
         BlockPos checkPos = new BlockPos(pos.method_10263(), y, pos.method_10260());
         BlockState blockState = world.method_8320(checkPos);
         BlockState aboveState = world.method_8320(checkPos.method_10084());
         if (blockState.method_26215() && aboveState.method_26215()) {
            return checkPos;
         }
      }

      return null;
   }

   private static boolean isPositionSafe(World world, BlockPos pos) {
      List<Entity> nearbyEntities = world.method_8390(
         Entity.class, new Box(pos).method_1014(5.0), entity -> entity.method_5864().method_5891() == SpawnGroup.field_6302
      );
      return nearbyEntities.isEmpty();
   }

   public static List<LivingEntity> findGhostEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> ghostEntities = new ArrayList<>();

      for (LivingEntity entity : player.method_37908()
         .method_8390(LivingEntity.class, player.method_5829().method_1014(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (entity instanceof GhostEntity) {
            ghostEntities.add(entity);
         }
      }

      return ghostEntities;
   }

   public static void handleScapegoatGhostSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         LivingEntity target = findTargetInSight(player, 10.0);
         if (target == null) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43471("item.smfs.scapegoat_ghost.no_target"), true);
            }
         } else {
            StatusEffectInstance scapegoatMarkEffect = new StatusEffectInstance(ModEffects.SCAPEGOAT_MARK, 600, 0, false, true, true);
            target.method_6092(scapegoatMarkEffect);
            LOGGER.debug("玩家 {} 使用替死鬼J键技能，标记目标 {} 为替死目标", player.method_5477().getString(), target.method_5477().getString());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43471("item.smfs.scapegoat_ghost.target_marked"), false);
            }

            spawnMarkParticles(target);
         }
      }
   }

   private static LivingEntity findTargetInSight(PlayerEntity player, double maxDistance) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         EntityHitResult entityHit = TargetingUtil.raycastEntity(serverPlayer, maxDistance);
         return entityHit != null ? (LivingEntity)entityHit.method_17782() : TargetingUtil.findEntityInLookDirectionWithOcclusion(player, maxDistance, 0.5);
      } else {
         return null;
      }
   }

   private static void spawnMarkParticles(LivingEntity target) {
      if (!target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 20; i++) {
            double offsetX = target.method_6051().method_43058() - 0.5;
            double offsetY = target.method_6051().method_43058() * 2.0 + 1.0;
            double offsetZ = target.method_6051().method_43058() - 0.5;
            serverWorld.method_14199(
               ParticleTypes.field_11240, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, 0.1, 0.1, 0.1, 0.05
            );
         }
      }
   }

   public static boolean hasScapegoatGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "scapegoat_ghost") || PlayerEvents.hasGhostType(player, "scapegoat_ghost");
   }

   public static void handleCandyGhostJSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         float currentHealth = player.method_6032();
         if (currentHealth <= 5.0F) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43471("item.smfs.candy_ghost.insufficient_health"), true);
            }
         } else {
            player.method_5643(player.method_48923().method_48830(), 5.0F);
            ItemStack candyStack = new ItemStack(ModItems.GHOST_CANDY, 1);
            if (!player.method_7270(candyStack)) {
               ItemEntity candyEntity = new ItemEntity(player.method_37908(), player.method_23317(), player.method_23318(), player.method_23321(), candyStack);
               player.method_37908().method_8649(candyEntity);
            }

            LOGGER.debug("玩家 {} 使用糖果鬼J键技能，扣除2点生命值获得鬼糖果", player.method_5477().getString());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43471("item.smfs.candy_ghost.skill_used"), false);
            }
         }
      }
   }

   public static boolean hasCandyGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "candy_ghost") || PlayerEvents.hasGhostType(player, "candy_ghost");
   }

   public static void handleGhostEyeJSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         ItemStack mainHandStack = player.method_6047();
         ItemStack offHandStack = player.method_6079();
         boolean hasDeceptionNecklace = false;
         ItemStack necklaceStack = null;
         if (mainHandStack.method_7909() == ModItems.DECEPTION_GHOST_NECKLACE) {
            hasDeceptionNecklace = true;
            necklaceStack = mainHandStack;
         } else if (offHandStack.method_7909() == ModItems.DECEPTION_GHOST_NECKLACE) {
            hasDeceptionNecklace = true;
            necklaceStack = offHandStack;
         }

         if (!hasDeceptionNecklace) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43471("item.smfs.ghost_eye.no_deception_necklace"), true);
            }
         } else if (necklaceStack.method_7919() == 0) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43471("item.smfs.ghost_eye.necklace_already_full"), true);
            }
         } else {
            necklaceStack.method_7974(0);
            LOGGER.debug("玩家 {} 使用鬼眼J键技能，修复骗人鬼项链耐久度至满", player.method_5477().getString());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43471("item.smfs.ghost_eye.necklace_repaired"), false);
            }
         }
      }
   }

   public static boolean hasGhostEye(PlayerEntity player) {
      return isHoldingShardItem(player, "silent_ghost_eye") || PlayerEvents.hasGhostType(player, "silent_ghost_eye");
   }

   public static boolean hasGhostSmoke(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_smoke");
   }

   public static int getGhostSmokeLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).method_7909() == ModItems.GHOST_SMOKE) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static void enableGrayGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostSmokeLevel(player));
      if (level <= 0) {
         LOGGER.warn("玩家 {} 尝试开启灰色鬼域但等级无效: {}", player.method_5477().getString(), level);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级灰色鬼域", player.method_5477().getString(), level);
         player.method_6092(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
         if (player instanceof ServerPlayerEntity) {
            GrayGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            applyTargetGhostDomainToOtherPlayers(player, ModEffects.GRAY_GHOST_DOMAIN_TARGET, level);
         }

         applyLevelAbilities(player, level);
      }
   }

   private static void generateLaserBeam(PlayerEntity player, Vec3d lookVec, Vec3d startPos) {
      World world = player.method_37908();
      if (!world.method_8608()) {
         ServerWorld serverWorld = (ServerWorld)world;
         double laserLength = 64.0;
         Vec3d endPos = startPos.method_1019(lookVec.method_1021(laserLength));
         generateLaserParticles(serverWorld, startPos, endPos);
         destroyBlocksAlongLaser(serverWorld, startPos, endPos, player);
         LOGGER.debug("玩家 {} 生成激光光束，从 {} 到 {}", player.method_5477().getString(), startPos, endPos);
      }
   }

   private static void generateLaserParticles(ServerWorld world, Vec3d startPos, Vec3d endPos) {
      Vec3d direction = endPos.method_1020(startPos);
      double distance = direction.method_1033();
      direction = direction.method_1029();
      int particlesPerBlock = 5;
      int totalParticles = (int)(distance * particlesPerBlock);
      DustParticleEffect dustEffect = new DustParticleEffect(new Vec3d(1.0, 0.0, 0.0).method_46409(), 1.0F);

      for (int i = 0; i < totalParticles; i++) {
         double progress = (double)i / totalParticles;
         Vec3d particlePos = startPos.method_1019(direction.method_1021(progress * distance));
         world.method_14199(dustEffect, particlePos.field_1352, particlePos.field_1351, particlePos.field_1350, 1, 0.05, 0.05, 0.05, 0.02);
      }
   }

   private static void destroyBlocksAlongLaser(ServerWorld world, Vec3d startPos, Vec3d endPos, PlayerEntity player) {
      Vec3d direction = endPos.method_1020(startPos);
      double distance = direction.method_1033();
      direction = direction.method_1029();
      double destructionRadius = 1.0;

      for (double d = 0.0; d <= distance; d++) {
         Vec3d checkPos = startPos.method_1019(direction.method_1021(d));

         for (double x = -destructionRadius; x <= destructionRadius; x++) {
            for (double y = -destructionRadius; y <= destructionRadius; y++) {
               for (double z = -destructionRadius; z <= destructionRadius; z++) {
                  BlockPos blockPos = new BlockPos(
                     (int)Math.floor(checkPos.field_1352 + x), (int)Math.floor(checkPos.field_1351 + y), (int)Math.floor(checkPos.field_1350 + z)
                  );
                  BlockState blockState = world.method_8320(blockPos);
                  if (!blockState.method_26215() && !(blockState.method_26204().method_36555() < 0.0F)) {
                     world.method_8651(blockPos, false, player);
                  }
               }
            }
         }
      }
   }

   public static void handleBlockGhostVSkill(PlayerEntity player) {
      player.method_5643(player.method_48923().method_48830(), 4.0F);
      ItemStack concreteStack = getRandomConcreteBlock();
      concreteStack.method_7939(16);
      if (!player.method_7270(concreteStack)) {
         ItemEntity concreteEntity = new ItemEntity(player.method_37908(), player.method_23317(), player.method_23318(), player.method_23321(), concreteStack);
         player.method_37908().method_8649(concreteEntity);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.method_7353(Text.method_43470("§6使用灵异力量幻化出了方块 "), true);
      }
   }

   private static ItemStack getRandomConcreteBlock() {
      Item[] concreteBlocks = new Item[]{
         Items.field_8341,
         Items.field_8771,
         Items.field_8508,
         Items.field_8364,
         Items.field_8686,
         Items.field_8839,
         Items.field_8127,
         Items.field_8333,
         Items.field_8735,
         Items.field_8637,
         Items.field_8411,
         Items.field_8737,
         Items.field_8762,
         Items.field_8120,
         Items.field_8197,
         Items.field_8704
      };
      Random random = new Random();
      Item randomConcrete = concreteBlocks[random.nextInt(concreteBlocks.length)];
      return new ItemStack(randomConcrete);
   }

   public static void handlePlagueGhostJSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         LOGGER.debug("玩家 {} 使用瘟鬼J键技能：传染目标生物瘟疫", player.method_5477().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            double maxDistance = 10.0;
            LivingEntity bestTarget = TargetingUtil.findEntityInLookDirection(player, maxDistance, 0.5);
            if (bestTarget != null) {
               bestTarget.method_6092(new StatusEffectInstance(ModEffects.PLAGUE, 600, 0));
            } else {
               serverPlayer.method_7353(Text.method_43470("§c未找到可传染的目标生物"), true);
            }
         }
      }
   }

   public static void handleMineralGhostJSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         World world = player.method_37908();
         BlockPos playerPos = player.method_24515();
         int searchRadius = 24;
         List<BlockPos> mineralBlocks = new ArrayList<>();

         for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
               for (int z = -searchRadius; z <= searchRadius; z++) {
                  BlockPos pos = playerPos.method_10069(x, y, z);
                  BlockState state = world.method_8320(pos);
                  Block block = state.method_26204();
                  if (isMineralBlock(block)) {
                     mineralBlocks.add(pos);
                  }
               }
            }
         }

         if (!mineralBlocks.isEmpty()) {
            RaycastContext raycastContext = new RaycastContext(
               player.method_33571(),
               player.method_33571().method_1019(player.method_5720().method_1021(20.0)),
               ShapeType.field_17559,
               FluidHandling.field_1348,
               player
            );
            BlockHitResult hitResult = world.method_17742(raycastContext);
            Vec3d targetVec = hitResult.method_17784();
            mineralBlocks.sort((pos1, pos2) -> {
               double distance1 = pos1.method_46558().method_1025(targetVec);
               double distance2 = pos2.method_46558().method_1025(targetVec);
               return Double.compare(distance1, distance2);
            });
            new Random();
            int blocksToExplode = Math.min(mineralBlocks.size(), 6);

            for (int i = 0; i < blocksToExplode; i++) {
               BlockPos pos = mineralBlocks.get(i);
               world.method_22352(pos, true);
               float explosionPower = 6.0F;
               world.method_8437(
                  player, pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5, explosionPower, ExplosionSourceType.field_40890
               );
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.method_5783(SoundEvents.field_15152, 1.0F, 1.0F);
               }
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a成功引爆了 " + blocksToExplode + " 个矿物方块"), true);
            }
         } else if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c周围没有发现矿物方块"), true);
         }
      }
   }

   private static boolean isMineralBlock(Block block) {
      Set<Block> mineralBlocks = new HashSet<>(
         Arrays.asList(
            Blocks.field_10418,
            Blocks.field_29219,
            Blocks.field_10212,
            Blocks.field_29027,
            Blocks.field_27120,
            Blocks.field_29221,
            Blocks.field_10571,
            Blocks.field_29026,
            Blocks.field_10080,
            Blocks.field_29030,
            Blocks.field_10090,
            Blocks.field_29028,
            Blocks.field_10442,
            Blocks.field_29029,
            Blocks.field_10013,
            Blocks.field_29220,
            Blocks.field_10213,
            Blocks.field_23077,
            Blocks.field_22109,
            ModBlocks.DEFILED_ORE,
            ModBlocks.DEEP_DEFILED_ORE
         )
      );
      return mineralBlocks.contains(block);
   }

   public static void handleShadowGhostJSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         LOGGER.debug("玩家 {} 使用黑影鬼J键技能：标记处于阴影中的生物并对其发动袭击", player.method_5477().getString());
         int searchRadius = 20;
         LivingEntity bestTarget = TargetingUtil.findEntityInLookDirection(
            player, searchRadius, 0.5, entity -> entity.method_37908().method_22339(entity.method_24515()) <= 4
         );
         if (bestTarget != null) {
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 5.0F;
            executeSkillSpiritAttack((ServerPlayerEntity)player, bestTarget);
            if (player instanceof ServerPlayerEntity serverPlayer && ClientModConfig.getInstance().showDamageText(serverPlayer.method_5667())) {
               serverPlayer.method_7353(Text.method_43470("§a成功袭击了处于阴影中的生物，造成" + new DecimalFormat("#.###").format(spiritDamage) + "点灵异伤害"), true);
            }
         } else if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.method_7353(Text.method_43470("§c周围20格内没有发现处于阴影中的生物"), true);
         }
      }
   }

   public static void handleDoorGhostJSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         HitResult hitResult = player.method_5745(20.0, 0.0F, false);
         if (hitResult.method_17783() == Type.field_1333) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c请对准一个门使用开门鬼技能"), true);
            }
         } else if (hitResult.method_17783() != Type.field_1332) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§c请对准一个门使用开门鬼技能"), true);
            }
         } else {
            BlockPos doorPos = ((BlockHitResult)hitResult).method_17777();
            World world = player.method_37908();
            BlockState doorState = world.method_8320(doorPos);
            if (!(doorState.method_26204() instanceof DoorBlock)) {
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.method_7353(Text.method_43470("§c请对准一个门使用开门鬼技能"), true);
               }
            } else {
               world.method_8501(doorPos, (BlockState)doorState.method_28493(DoorBlock.field_10945));
               playKnockingSound(player, 1);
               List<LivingEntity> entitiesInRange = world.method_8390(LivingEntity.class, new Box(doorPos).method_1014(6.0), entity -> entity != player);
               int attackCount = 0;
               NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
               float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 5.0F;
               float actualDamage = spiritDamage * 2.0F;

               for (LivingEntity target : entitiesInRange) {
                  executeSkillSpiritAttack((ServerPlayerEntity)player, target, actualDamage);
                  attackCount++;
               }

               if (player instanceof ServerPlayerEntity serverPlayer) {
                  if (attackCount > 0 && ClientModConfig.getInstance().showDamageText(serverPlayer.method_5667())) {
                     serverPlayer.method_7353(
                        Text.method_43470("§a成功打开了门并对" + attackCount + "个生物造成了" + new DecimalFormat("#.###").format(actualDamage) + "点灵异伤害"), true
                     );
                  } else {
                     serverPlayer.method_7353(Text.method_43470("§a成功打开了门"), true);
                  }
               }
            }
         }
      }
   }

   public static void handleGhostShadowHeadJSkill(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         LOGGER.debug("玩家 {} 使用鬼影头J键技能：切换摄像机绑定", player.method_5477().getString());
      }
   }

   public static void handleGhostShadowHeadVSkill(PlayerEntity player, int targetId) {
      if (!player.method_37908().method_8608()) {
         LOGGER.debug("玩家 {} 使用鬼影头V键技能：灵异袭击实体ID {}", player.method_5477().getString(), targetId);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            Entity target = player.method_37908().method_8469(targetId);
            if (target != null && target instanceof LivingEntity livingTarget) {
               if (target == player) {
                  serverPlayer.method_7353(Text.method_43470("§c不能袭击自己"), true);
               } else {
                  executeSkillSpiritAttack(serverPlayer, livingTarget);
                  restoreGodControlSpeed(targetId, target);
               }
            } else {
               serverPlayer.method_7353(Text.method_43470("§c目标无效或已消失"), true);
            }
         }
      }
   }

   public static void handleGhostShadowHeadUnbindControl(PlayerEntity player, int targetId) {
      if (!player.method_37908().method_8608()) {
         Entity target = player.method_37908().method_8469(targetId);
         restoreGodControlSpeed(targetId, target);
      }
   }

   private static void restoreGodControlSpeed(int targetId, Entity target) {
      Double originalSpeed = GOD_CONTROL_ORIGINAL_SPEED.remove(targetId);
      if (originalSpeed != null && target instanceof LivingEntity livingTarget) {
         livingTarget.method_5996(EntityAttributes.field_23719).method_6192(originalSpeed);
      }
   }

   public static void handleGhostShadowHeadGSkill(PlayerEntity player, int targetId) {
      if (!player.method_37908().method_8608()) {
         LOGGER.debug("玩家 {} 使用鬼影头G键技能：施加缓慢3和虚弱3，目标实体ID {}", player.method_5477().getString(), targetId);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            Entity target = player.method_37908().method_8469(targetId);
            if (target != null && target instanceof LivingEntity livingTarget) {
               if (target == player) {
                  serverPlayer.method_7353(Text.method_43470("§c不能对自己施加效果"), true);
               } else {
                  livingTarget.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 600, 2));
                  livingTarget.method_6092(new StatusEffectInstance(StatusEffects.field_5911, 600, 2));
                  serverPlayer.method_7353(Text.method_43470("§a影响了 " + livingTarget.method_5477().getString() + "的意识，现在其状态下降了"), true);
               }
            } else {
               serverPlayer.method_7353(Text.method_43470("§c目标无效或已消失"), true);
            }
         }
      }
   }

   public static void handleGhostShadowHeadNSkill(
      PlayerEntity player, int targetId, float forward, float sideways, boolean jumping, boolean sneaking, float playerYaw, float playerPitch
   ) {
      if (!player.method_37908().method_8608()) {
         Entity target = player.method_37908().method_8469(targetId);
         if (target != null && target instanceof LivingEntity livingTarget) {
            if (target != player) {
               double distance = player.method_5739(target);
               if (!(distance > 200.0)) {
                  if (player instanceof ServerPlayerEntity serverPlayer && AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration")) {
                     livingTarget.method_36456(playerYaw);
                     livingTarget.method_5847(playerYaw);
                     livingTarget.method_5636(playerYaw);
                     livingTarget.method_36457(playerPitch);
                  }

                  boolean godControl = false;
                  if (player instanceof ServerPlayerEntity serverPlayer
                     && AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_god")
                     && target instanceof MobEntity mobTarget) {
                     godControl = true;
                     if (!GOD_CONTROL_ORIGINAL_SPEED.containsKey(targetId)) {
                        GOD_CONTROL_ORIGINAL_SPEED.put(targetId, livingTarget.method_26825(EntityAttributes.field_23719));
                     }

                     livingTarget.method_5996(EntityAttributes.field_23719).method_6192(0.0);
                  }

                  float speed = godControl
                     ? GOD_CONTROL_ORIGINAL_SPEED.getOrDefault(targetId, 0.1).floatValue()
                     : (float)livingTarget.method_26825(EntityAttributes.field_23719);
                  if (sneaking) {
                     speed *= 0.3F;
                  }

                  float yaw = livingTarget.method_36454();
                  float yawRad = yaw * (float) Math.PI / 180.0F;
                  double moveX = sideways * Math.cos(yawRad) - forward * Math.sin(yawRad);
                  double moveZ = sideways * Math.sin(yawRad) + forward * Math.cos(yawRad);
                  double len = Math.sqrt(moveX * moveX + moveZ * moveZ);
                  if (len > 0.0) {
                     moveX = moveX / len * speed;
                     moveZ = moveZ / len * speed;
                  }

                  livingTarget.method_18800(moveX, livingTarget.method_18798().field_1351, moveZ);
                  if (jumping && livingTarget.method_24828()) {
                     livingTarget.method_18800(livingTarget.method_18798().field_1352, 0.42, livingTarget.method_18798().field_1350);
                  }
               }
            }
         }
      }
   }

   public static boolean checkAndSetJSkillCooldown(ServerPlayerEntity player, String cooldownKey, int baseCooldownTicks, String skillName) {
      if (PlayerEvents.isGhostChildFused(player)) {
         return true;
      }

      if (isSkillOnCooldown(player, cooldownKey)) {
         long remainingTicks = getSkillCooldownRemaining(player, cooldownKey);
         double remainingSeconds = remainingTicks / 20.0;
         player.method_7353(Text.method_43470("§c" + skillName + "正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
         return false;
      }

      float multiplier = 1.0F;
      if (AdvancementManager.hasAdvancement(player, "smfs:become_aberration")) {
         multiplier = 0.75F;
      }

      int cooldownTicks = Math.round(baseCooldownTicks * multiplier);
      setSkillCooldown(player, cooldownKey, cooldownTicks);
      return true;
   }

   public enum SkillCheckResult {
      SUCCESS,
      NO_GHOST,
      LEVEL_TOO_LOW;
   }
}
