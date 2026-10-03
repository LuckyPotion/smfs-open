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

      int preferredRange = PLAYER_DOMAIN_RANGE.getOrDefault(player.getUuid(), -1);
      return preferredRange <= 0 ? rawRadius : Math.max(5, Math.min(preferredRange, rawRadius));
   }

   public static void setPlayerDomainRange(PlayerEntity player, int range) {
      if (range <= 0) {
         PLAYER_DOMAIN_RANGE.remove(player.getUuid());
      } else {
         PLAYER_DOMAIN_RANGE.put(player.getUuid(), Math.max(5, range));
      }
   }

   public static boolean isHoldingShardItem(PlayerEntity player, String ghostType) {
      for (Hand hand : Hand.values()) {
         ItemStack stack = player.getStackInHand(hand);
         if (stack.getItem() instanceof BaseGhostEyeItem item && BaseGhostEyeItem.isShard(stack) && item.getGhostType().equals(ghostType)) {
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
      if (!item.isEmpty() && item.getItem() == ghostItem) {
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
         ItemStack stack = player.getStackInHand(hand);
         if (stack.getItem() instanceof BaseGhostEyeItem item && BaseGhostEyeItem.isShard(stack) && item.getGhostType().equals(ghostType)) {
            NbtCompound nbt = stack.getOrCreateNbt();
            int uses = nbt.getInt("ShardUses");
            if (uses <= 0) {
               uses = 2;
            }

            if (--uses <= 0) {
               player.setStackInHand(hand, ItemStack.EMPTY);
            } else {
               nbt.putInt("ShardUses", uses);
            }

            player.getInventory().markDirty();
            return;
         }
      }
   }

   public static void toggleGhostDomain(PlayerEntity player) {
      LOGGER.debug("为玩家 {} 切换鬼域状态", player.getName().getString());
      if (!player.hasStatusEffect(ModEffects.SILENCE) && !player.hasStatusEffect(ModEffects.DREAM)) {
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
               PLAYER_CUSTOM_DOMAIN_LEVEL.remove(player.getUuid());
            } else {
               if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.getUuid())) {
                  PLAYER_CUSTOM_DOMAIN_LEVEL.put(player.getUuid(), 1);
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
         player.sendMessage(Text.literal("§c体内的鬼陷入沉寂"), true);
      }
   }

   public static int getEffectiveDomainLevel(PlayerEntity player, int ghostLevel) {
      if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.getUuid())) {
         int customLevel = PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.getUuid(), 1);
         if (ghostLevel <= 0) {
            ghostLevel = 1;
         }

         return Math.min(customLevel, ghostLevel);
      } else {
         return ghostLevel;
      }
   }

   public static int getEffectiveSkillLevel(PlayerEntity player, int ghostLevel) {
      return ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.getUuid()) && isGhostDomainActive(player)
         ? PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.getUuid(), 1)
         : ghostLevel;
   }

   public static String getInsufficientLevelMessage(PlayerEntity player) {
      return ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.getUuid()) && isGhostDomainActive(player) ? "§c鬼蜮强度不足" : "§c复苏程度不足";
   }

   public static void increaseGhostDomainLevel(PlayerEntity player) {
      if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.getUuid())) {
         if (isGhostDomainActive(player)) {
            int current = PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.getUuid(), 1);
            int maxLevel = getCurrentGhostMaxLevel(player);
            int newLevel = Math.min(current + 1, maxLevel);
            PLAYER_CUSTOM_DOMAIN_LEVEL.put(player.getUuid(), newLevel);
            refreshGhostDomainLevel(player, newLevel);
            if (player instanceof ServerPlayerEntity) {
               player.sendMessage(Text.literal("§a鬼域层数: " + newLevel + " / " + maxLevel), true);
            }
         }
      }
   }

   public static void decreaseGhostDomainLevel(PlayerEntity player) {
      if (ClientModConfig.getInstance().isCustomGhostDomainLevelEnabled(player.getUuid())) {
         if (isGhostDomainActive(player)) {
            int current = PLAYER_CUSTOM_DOMAIN_LEVEL.getOrDefault(player.getUuid(), 1);
            int newLevel = Math.max(current - 1, 1);
            PLAYER_CUSTOM_DOMAIN_LEVEL.put(player.getUuid(), newLevel);
            refreshGhostDomainLevel(player, newLevel);
            int maxLevel = getCurrentGhostMaxLevel(player);
            if (player instanceof ServerPlayerEntity) {
               player.sendMessage(Text.literal("§a鬼域层数: " + newLevel + " / " + maxLevel), true);
            }
         }
      }
   }

   private static void refreshGhostDomainLevel(PlayerEntity player, int newLevel) {
      StatusEffectInstance currentEffect = null;
      if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)) {
         currentEffect = player.getStatusEffect(ModEffects.RED_GHOST_DOMAIN);
      } else if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)) {
         currentEffect = player.getStatusEffect(ModEffects.GREEN_GHOST_DOMAIN);
      } else if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)) {
         currentEffect = player.getStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN);
      } else if (player.hasStatusEffect(ModEffects.THICK_FOG)) {
         currentEffect = player.getStatusEffect(ModEffects.THICK_FOG);
      } else if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)) {
         currentEffect = player.getStatusEffect(ModEffects.BLACK_GHOST_DOMAIN);
      } else if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)) {
         currentEffect = player.getStatusEffect(ModEffects.CYAN_GHOST_DOMAIN);
      } else if (player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN)) {
         currentEffect = player.getStatusEffect(ModEffects.GRAY_GHOST_DOMAIN);
      }

      if (currentEffect != null) {
         StatusEffect effectType = currentEffect.getEffectType();
         player.removeStatusEffect(effectType);
         player.addStatusEffect(new StatusEffectInstance(effectType, Integer.MAX_VALUE, newLevel - 1, false, false, true));
         applyLevelAbilities(player, newLevel);
      }
   }

   private static int getCurrentGhostMaxLevel(PlayerEntity player) {
      if (isGhostDomainActive(player)) {
         if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)) {
            return getGhostEyeLevel(player);
         }

         if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)) {
            return getGhostFireLevel(player);
         }

         if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)) {
            return Math.max(getGhostEyeLevel(player), getGhostFireLevel(player));
         }

         if (player.hasStatusEffect(ModEffects.THICK_FOG)) {
            return getFogGhostLevel(player);
         }

         if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)) {
            return Math.max(
               Math.max(Math.max(getBlockGhostLevel(player), getFoodGhostLevel(player)), getQiaomenGhostLevel(player)),
               Math.max(getVillagerGhostLevel(player), getGhostOfficerLevel(player))
            );
         }

         if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)) {
            return getGhostWindLevel(player);
         }

         if (player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN)) {
            return getGhostSmokeLevel(player);
         }
      }

      return 1;
   }

   public static void enableRedGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostEyeLevel(player));
      if (level <= 0) {
         LOGGER.warn("玩家 {} 尝试开启红色鬼域但等级无效: {}", player.getName().getString(), level);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级红色鬼域", player.getName().getString(), level);
         player.addStatusEffect(new StatusEffectInstance(ModEffects.RED_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
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
         LOGGER.warn("玩家 {} 尝试开启绿色鬼域但等级无效: {}", player.getName().getString(), level);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级绿色鬼域", player.getName().getString(), level);
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
         if (player instanceof ServerPlayerEntity) {
            GreenGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            applyTargetGhostDomainToOtherPlayers(player, ModEffects.GREEN_GHOST_DOMAIN_TARGET, level);
         }

         applyLevelAbilities(player, level);
         LOGGER.debug("玩家 {} 的绿色鬼域已成功开启", player.getName().getString());
      }
   }

   public static void enableGoldenGhostDomain(PlayerEntity player) {
      int ghostEyeLevel = getGhostEyeLevel(player);
      int ghostFireLevel = getGhostFireLevel(player);
      int level = getEffectiveDomainLevel(player, Math.max(ghostEyeLevel, ghostFireLevel));
      if (level <= 0) {
         LOGGER.warn("玩家 {} 尝试开启金色鬼域但等级无效: 鬼眼{}级, 鬼火{}级", player.getName().getString(), ghostEyeLevel, ghostFireLevel);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级金色鬼域 (鬼眼{}级, 鬼火{}级)", player.getName().getString(), level, ghostEyeLevel, ghostFireLevel);
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GOLDEN_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
         if (player instanceof ServerPlayerEntity) {
            LOGGER.debug("在服务端为玩家 {} 更新金色鬼域复苏程度", player.getName().getString());
            GoldenGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            applyTargetGhostDomainToOtherPlayers(player, ModEffects.GOLDEN_GHOST_DOMAIN_TARGET, level);
         }

         applyLevelAbilities(player, level);
         LOGGER.debug("玩家 {} 的金色鬼域已成功开启", player.getName().getString());
      }
   }

   public static void enableFogGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getFogGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级鬼雾鬼域", player.getName().getString(), level);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.THICK_FOG, Integer.MAX_VALUE, 0, false, false, true));
      if (player instanceof ServerPlayerEntity) {
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加鬼雾鬼域效果", player.getName().getString());
         int radius = getDomainRadius(player, level);
         ThickFogEffect.updateRevivalDegreeInGhostDomain(player);

         for (PlayerEntity otherPlayer : player.getWorld()
            .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.distanceTo(otherPlayer);
            if (distance <= radius) {
               otherPlayer.addStatusEffect(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加鬼雾鬼域效果（TARGET版本）", otherPlayer.getName().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的鬼雾鬼域已成功开启", player.getName().getString());
   }

   public static void enableBlockGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getBlockGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级方块鬼鬼域", player.getName().getString(), level);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的方块鬼鬼域已成功开启", player.getName().getString());
   }

   public static void enableFoodGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getFoodGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级食物鬼鬼域", player.getName().getString(), level);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加食物鬼鬼域效果", player.getName().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.getWorld()
            .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.distanceTo(otherPlayer);
            if (distance <= radius) {
               otherPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加食物鬼鬼域效果", otherPlayer.getName().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的食物鬼鬼域已成功开启", player.getName().getString());
   }

   public static void enableQiaomenGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getQiaomenGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级敲门鬼鬼域", player.getName().getString(), level);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加敲门鬼鬼域效果", player.getName().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.getWorld()
            .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.distanceTo(otherPlayer);
            if (distance <= radius) {
               otherPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加敲门鬼鬼域效果", otherPlayer.getName().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的敲门鬼鬼域已成功开启", player.getName().getString());
   }

   public static void enableVillagerGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getVillagerGhostLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级村民鬼鬼域", player.getName().getString(), level);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加村民鬼鬼域效果", player.getName().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.getWorld()
            .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.distanceTo(otherPlayer);
            if (distance <= radius) {
               otherPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0, false, false, false));
               LOGGER.debug("给玩家 {} 施加村民鬼鬼域效果", otherPlayer.getName().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的村民鬼鬼域已成功开启", player.getName().getString());
   }

   public static void enableGhostOfficerDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostOfficerLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级鬼差鬼域", player.getName().getString(), level);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      BlackGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.BLACK_GHOST_DOMAIN_TARGET, level);
         LOGGER.debug("在服务端为玩家 {} 给其他玩家施加鬼差鬼域效果", player.getName().getString());
         int radius = getDomainRadius(player, level);

         for (PlayerEntity otherPlayer : player.getWorld()
            .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.distanceTo(otherPlayer);
            if (distance <= radius) {
               otherPlayer.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 200, level - 1, false, false, false));
               LOGGER.debug("给玩家 {} 施加鬼差鬼域效果（TARGET版本）", otherPlayer.getName().getString());
            }
         }
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的鬼差鬼域已成功开启", player.getName().getString());
   }

   public static void enableCyanGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostWindLevel(player));
      if (level == 0) {
         level = 1;
      }

      LOGGER.debug("为玩家 {} 开启 {} 级青色鬼域", player.getName().getString(), level);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
      CyanGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
      if (player instanceof ServerPlayerEntity) {
         applyTargetGhostDomainToOtherPlayers(player, ModEffects.CYAN_GHOST_DOMAIN_TARGET, level);
      }

      applyLevelAbilities(player, level);
      LOGGER.debug("玩家 {} 的青色鬼域已成功开启", player.getName().getString());
   }

   public static void disableGhostDomain(PlayerEntity player) {
      LOGGER.debug("为玩家 {} 关闭鬼域", player.getName().getString());
      player.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN);
      player.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN);
      player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN);
      player.removeStatusEffect(ModEffects.GRAY_GHOST_DOMAIN);
      player.removeStatusEffect(ModEffects.THICK_FOG);
      player.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
      player.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN);
      player.removeStatusEffect(ModEffects.CYAN_GHOST_DOMAIN);
      player.removeStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
      if (player instanceof ServerPlayerEntity) {
         int currentLevel = getCurrentLevel(player);
         int radius = getDomainRadius(player, currentLevel + 1);

         for (PlayerEntity otherPlayer : player.getWorld()
            .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.distanceTo(otherPlayer);
            if (distance <= radius) {
               otherPlayer.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
               otherPlayer.removeStatusEffect(StatusEffects.HUNGER);
               otherPlayer.removeStatusEffect(StatusEffects.SLOWNESS);
               otherPlayer.removeStatusEffect(StatusEffects.WEAKNESS);
               LOGGER.debug("清除玩家 {} 的方块鬼/食物鬼/敲门鬼/村民鬼鬼域效果", otherPlayer.getName().getString());
            }
         }

         removeTargetGhostDomainFromOtherPlayers(player);
      }

      resetLevelAbilities(player);
      LOGGER.debug("玩家 {} 的鬼域已成功关闭", player.getName().getString());
   }

   public static void applyTargetGhostDomainToOtherPlayers(PlayerEntity player, StatusEffect targetEffect, int level) {
      if (player instanceof ServerPlayerEntity) {
         ModConfig config = ModConfig.getInstance();
         if (config.enableGhostDomainEffectsOnMobs) {
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("为玩家 {} 给其他玩家和生物施加TARGET后缀鬼域效果: {}, 等级: {}", player.getName().getString(), targetEffect, level);
            }

            int radius = getDomainRadius(player, level);

            for (PlayerEntity otherPlayer : player.getWorld()
               .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
               double distance = player.distanceTo(otherPlayer);
               if (distance <= radius) {
                  StatusEffectInstance existingEffect = otherPlayer.getStatusEffect(targetEffect);
                  if (existingEffect == null || existingEffect.getDuration() < 100 || existingEffect.getAmplifier() != level - 1) {
                     otherPlayer.addStatusEffect(new StatusEffectInstance(targetEffect, 200, level - 1, false, false, false));
                  }

                  if (!otherPlayer.isSpectator()) {
                     StatusEffectInstance existingLostEffect = otherPlayer.getStatusEffect(ModEffects.LOST);
                     if (existingLostEffect == null || existingLostEffect.getDuration() < 100) {
                        StatusEffectInstance lostEffect = new LostStatusEffectInstance(ModEffects.LOST, 200, 0, false, false, false, player.getUuid());
                        otherPlayer.addStatusEffect(lostEffect);
                     }
                  }
               }
            }

            for (LivingEntity livingEntity : player.getWorld()
               .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entity -> entity != player && !(entity instanceof PlayerEntity))) {
               double distance = player.distanceTo(livingEntity);
               if (distance <= radius) {
                  StatusEffectInstance existingDomainEffect = livingEntity.getStatusEffect(targetEffect);
                  if (existingDomainEffect == null || existingDomainEffect.getDuration() < 100) {
                     livingEntity.addStatusEffect(new StatusEffectInstance(targetEffect, 200, level - 1, false, false, false));
                  }

                  StatusEffectInstance existingLostEffect = livingEntity.getStatusEffect(ModEffects.LOST);
                  if (existingLostEffect == null || existingLostEffect.getDuration() < 100) {
                     StatusEffectInstance lostEffect = new LostStatusEffectInstance(ModEffects.LOST, 200, 0, false, false, false, player.getUuid());
                     livingEntity.addStatusEffect(lostEffect);
                  }
               }
            }
         }
      }
   }

   public static void removeTargetGhostDomainFromOtherPlayers(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity) {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("为玩家 {} 清除其他玩家的TARGET后缀鬼域效果", player.getName().getString());
         }

         int currentLevel = getCurrentLevel(player);
         int radius = getDomainRadius(player, currentLevel + 1);

         for (PlayerEntity otherPlayer : player.getWorld()
            .getEntitiesByClass(PlayerEntity.class, player.getBoundingBox().expand(radius), p -> p != player && p instanceof ServerPlayerEntity)) {
            double distance = player.distanceTo(otherPlayer);
            if (distance <= radius) {
               otherPlayer.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
               otherPlayer.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
               otherPlayer.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
               otherPlayer.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
               otherPlayer.removeStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
               otherPlayer.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
               otherPlayer.removeStatusEffect(ModEffects.LOST);
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("清除玩家 {} 的TARGET后缀鬼域效果和迷失效果", otherPlayer.getName().getString());
               }
            }
         }

         for (LivingEntity entity : player.getWorld()
            .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && !(entityx instanceof PlayerEntity))) {
            double distance = player.distanceTo(entity);
            if (distance <= radius) {
               entity.removeStatusEffect(ModEffects.LOST);
               if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("清除生物 {} 的迷失效果", entity.getName().getString());
               }
            }
         }
      }
   }

   private static void applyLevelAbilities(PlayerEntity player, int level) {
      LOGGER.debug("为玩家 {} 应用 {} 级鬼域能力", player.getName().getString(), level);
      if (level >= 2) {
         int radius = getDomainRadius(player, level);
         player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(radius)).forEach(entity -> {
            if (entity instanceof LivingEntity) {
               entity.setGlowing(true);
            }
         });
      } else {
         player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(256.0)).forEach(entity -> {
            if (entity instanceof LivingEntity) {
               entity.setGlowing(false);
            }
         });
      }

      if (level >= 3) {
         LOGGER.debug("为玩家 {} 应用3级能力: 飞行", player.getName().getString());
         player.getAbilities().allowFlying = true;
         player.getAbilities().flying = true;
         player.sendAbilitiesUpdate();
      } else if (player.getAbilities().allowFlying && !player.isCreative() && !player.isSpectator()) {
         LOGGER.debug("玩家 {} 层数不足3级，禁用飞行", player.getName().getString());
         player.getAbilities().allowFlying = false;
         player.getAbilities().flying = false;
         player.sendAbilitiesUpdate();
      }

      LOGGER.debug("玩家 {} 的鬼域能力应用完成", player.getName().getString());
   }

   public static void sendGhostEyeWarning(PlayerEntity player) {
      if (hasValidGhostEye(player) && player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.sendMessage(Text.literal("§c鬼眼正在不安分地转动着..."), true);
      }
   }

   public static void handleGhostDomainTeleport(PlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int currentLevel = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      double teleportDistance = 64.0 + (currentLevel - 3) * 32.0;
      Vec3d lookVec = player.getRotationVec(1.0F);
      Vec3d startPos = player.getEyePos();
      Vec3d endPos = startPos.add(lookVec.multiply(teleportDistance));
      HitResult hitResult = player.getWorld().raycast(new RaycastContext(startPos, endPos, ShapeType.COLLIDER, FluidHandling.NONE, player));
      Vec3d teleportPos;
      if (hitResult.getType() == Type.MISS) {
         teleportPos = endPos;
      } else {
         teleportPos = hitResult.getPos().subtract(lookVec.multiply(0.5));
      }

      player.teleport(teleportPos.x, teleportPos.y, teleportPos.z);
      LOGGER.debug("玩家 {} 瞬移到位置: {}", player.getName().getString(), teleportPos);
   }

   public static void handleGhostDomainTeleportEntity(PlayerEntity player) {
      Vec3d lookVec = player.getRotationVec(1.0F);
      Vec3d startPos = player.getEyePos();
      Entity bestTarget = TargetingUtil.findEntityInLookDirectionWithOcclusion(
         player, 64.0, 0.5, e -> !(e instanceof YangJianEntity) && !(e instanceof LuoQianGhostEntity)
      );
      if (bestTarget != null) {
         if (bestTarget instanceof GhostEntity ghost) {
            if (PlayerEvents.isGhostChildFused(player)) {
               if (player.getWorld() instanceof ServerWorld serverWorld) {
                  GhostSpawnManager.lockGhostType(serverWorld, bestTarget.getType());
               }

               GhostDeathHandler.markLegitimateRemoval(ghost);
               ghost.discard();
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.sendMessage(Text.literal("§c已封锁厉鬼" + bestTarget.getName().getString()), true);
               }

               LOGGER.debug("玩家 {} 成神后放逐厉鬼 {}，已加入封锁列表", player.getName().getString(), bestTarget.getName().getString());
            } else if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§c需要成神后才能放逐厉鬼"), true);
            }
         } else {
            double newX = bestTarget.getX() + (Math.random() * 2000.0 - 1000.0);
            double newY = bestTarget.getY() + (Math.random() * 200.0 - 100.0);
            double newZ = bestTarget.getZ() + (Math.random() * 2000.0 - 1000.0);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已放逐" + bestTarget.getName().getString()), true);
            }

            newY = Math.max(0.0, Math.min(newY, player.getWorld().getHeight()));
            bestTarget.teleport(newX, newY, newZ);
            LOGGER.debug("玩家 {} 传送生物 {} 到位置: ({}, {}, {})", player.getName().getString(), bestTarget.getName().getString(), newX, newY, newZ);
         }
      } else {
         LOGGER.debug("玩家 {} 视线内没有找到实体，生成激光光束", player.getName().getString());
         generateLaserBeam(player, lookVec, startPos);
      }
   }

   public static void handleGhostDomainPauseTime(PlayerEntity player) {
      LOGGER.debug("玩家 {} 暂停时间", player.getName().getString());
   }

   public static void handleGhostDomainRemoveBuffs(PlayerEntity player) {
      List<StatusEffect> effectsToRemove = new ArrayList<>();
      player.getActiveStatusEffects()
         .forEach(
            (effect, instance) -> {
               if (!effect.isBeneficial()
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
      effectsToRemove.forEach(player::removeStatusEffect);
      float currentMaxHealth = player.getMaxHealth();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
         LOGGER.debug("玩家 {} 鬼域移除负面效果：恢复生命上限到20", player.getName().getString());
      } else {
         targetHealth = currentMaxHealth;
      }

      player.setHealth(targetHealth);
      LOGGER.debug("玩家 {} 移除所有负面效果并恢复生命值到{}", player.getName().getString(), targetHealth);
   }

   public static void handleLostGhostRestart(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行遗忘鬼重启技能", player.getName().getString());
      List<StatusEffect> effectsToRemove = new ArrayList<>();
      player.getActiveStatusEffects()
         .forEach(
            (effect, instance) -> {
               if (!effect.isBeneficial()
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
      effectsToRemove.forEach(player::removeStatusEffect);
      float currentMaxHealth = player.getMaxHealth();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
         LOGGER.debug("玩家 {} 遗忘鬼重启技能：恢复生命上限到20", player.getName().getString());
      } else {
         targetHealth = currentMaxHealth;
      }

      player.setHealth(targetHealth);
      LOGGER.debug("玩家 {} 遗忘鬼重启技能：移除所有负面效果并恢复生命值到{}", player.getName().getString(), targetHealth);
      clearLostGhostRevivalDegree(player);
   }

   private static void clearLostGhostRevivalDegree(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (!ghostItem.isEmpty() && ghostItem.getItem() == ModItems.LOST_GHOST) {
            PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", 0);
            LOGGER.debug("玩家 {} 遗忘鬼槽位 {} 的复苏程度已清空", player.getName().getString(), i);
            if (player instanceof ServerPlayerEntity) {
               player.sendMessage(Text.literal("§a遗忘鬼的复苏程度已被清空"), true);
            }
         }
      }
   }

   public static void handleGhostOfficerRestart(PlayerEntity player) {
      LOGGER.debug("玩家 {} 执行鬼差重启技能", player.getName().getString());
      List<StatusEffect> effectsToRemove = new ArrayList<>();
      player.getActiveStatusEffects()
         .forEach(
            (effect, instance) -> {
               if (!effect.isBeneficial()
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
      effectsToRemove.forEach(player::removeStatusEffect);
      float currentMaxHealth = player.getMaxHealth();
      float targetHealth;
      if (currentMaxHealth < 20.0F) {
         targetHealth = 20.0F;
         player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0);
         LOGGER.debug("玩家 {} 鬼差重启技能：恢复生命上限到20", player.getName().getString());
      } else {
         targetHealth = currentMaxHealth;
      }

      player.setHealth(targetHealth);
      LOGGER.debug("玩家 {} 鬼差重启技能：移除所有负面效果并恢复生命值到{}", player.getName().getString(), targetHealth);
   }

   private static void clearGhostOfficerRevivalDegree(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (!ghostItem.isEmpty() && ghostItem.getItem() == ModItems.GHOST_OFFICER) {
            PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", 0);
            LOGGER.debug("玩家 {} 鬼差槽位 {} 的复苏程度已清空", player.getName().getString(), i);
            if (player instanceof ServerPlayerEntity) {
               player.sendMessage(Text.literal("§a鬼差的复苏程度已被清空"), true);
            }
         }
      }
   }

   private static void resetLevelAbilities(PlayerEntity player) {
      LOGGER.debug("为玩家 {} 重置鬼域能力", player.getName().getString());
      player.getWorld().getEntitiesByClass(Entity.class, player.getBoundingBox().expand(1000.0), entity -> true).forEach(entity -> {
         if (entity instanceof LivingEntity) {
            entity.setGlowing(false);
         }
      });
      if (!player.isCreative() && !player.isSpectator()) {
         LOGGER.debug("为玩家 {} 禁用飞行能力", player.getName().getString());
         player.getAbilities().allowFlying = false;
         player.getAbilities().flying = false;
         player.sendAbilitiesUpdate();
      }

      LOGGER.debug("玩家 {} 的鬼域能力重置完成", player.getName().getString());
   }

   public static boolean hasValidGhostEye(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "silent_ghost_eye");
   }

   public static boolean hasValidGhostFire(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_fire");
   }

   public static int getGhostEyeLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.SILENT_GHOST_EYE) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getGhostFireLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.GHOST_FIRE) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getFogGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.FOG_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getBlockGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.BLOCK_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getFoodGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.FOOD_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getQiaomenGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.QIAOMEN_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getVillagerGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.VILLAGER_GHOST) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getGhostWindLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.GHOST_WIND) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static int getGhostOfficerLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.GHOST_OFFICER) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static boolean isGhostDomainActive(PlayerEntity player) {
      return player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)
         || player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)
         || player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)
         || player.hasStatusEffect(ModEffects.THICK_FOG)
         || player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)
         || player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)
         || player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN);
   }

   public static int getCurrentLevel(PlayerEntity player) {
      if (!isGhostDomainActive(player)) {
         return 0;
      }

      int maxLevel = 0;
      if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)) {
         int level = player.getStatusEffect(ModEffects.RED_GHOST_DOMAIN).getAmplifier() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)) {
         int level = player.getStatusEffect(ModEffects.GREEN_GHOST_DOMAIN).getAmplifier() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)) {
         int level = player.getStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN).getAmplifier() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)) {
         int level = player.getStatusEffect(ModEffects.CYAN_GHOST_DOMAIN).getAmplifier() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)) {
         int level = player.getStatusEffect(ModEffects.BLACK_GHOST_DOMAIN).getAmplifier() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN)) {
         int level = player.getStatusEffect(ModEffects.GRAY_GHOST_DOMAIN).getAmplifier() + 1;
         maxLevel = Math.max(maxLevel, level);
      }

      if (player.hasStatusEffect(ModEffects.THICK_FOG)) {
         StatusEffectInstance effect = player.getStatusEffect(ModEffects.THICK_FOG);
         if (effect != null) {
            maxLevel = Math.max(maxLevel, effect.getAmplifier() + 1);
         }
      }

      return maxLevel;
   }

   public static boolean isImmuneToGhostDomain(PlayerEntity player, int ghostLevel) {
      return isGhostDomainActive(player) && getCurrentLevel(player) > ghostLevel;
   }

   public static boolean hasGhostEntitiesInRange(PlayerEntity player, int radius) {
      return player.getWorld()
         .getOtherEntities(player, player.getBoundingBox().expand(radius))
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
         return masterUuid == null || !masterUuid.equals(player.getUuid());
      }
   }

   public static void checkRevivalDegree(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         int revivalDegree = PlayerEvents.getGhostSlotRevivalDegree(player, i);
         if (revivalDegree >= 1000) {
            LOGGER.debug("玩家 {} 槽位 {} 复苏程度达到1000，执行清除", player.getName().getString(), i);
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
         if (ghostItem != null && ghostItem.getItem() == ModItems.TAITOU_GHOST) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasDitouGhost(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.getItem() == ModItems.DITOU_GHOST) {
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有背对你的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个背对自己的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个背对你的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findBackFacingEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> backFacingEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isBackFacingPlayer(player, entity)) {
            backFacingEntities.add(entity);
         }
      }

      return backFacingEntities;
   }

   private static boolean isBackFacingPlayer(PlayerEntity player, LivingEntity entity) {
      Vec3d entityLookVec = entity.getRotationVec(1.0F).normalize();
      Vec3d toPlayerVec = new Vec3d(player.getX() - entity.getX(), player.getY() - entity.getY(), player.getZ() - entity.getZ()).normalize();
      double dotProduct = entityLookVec.dotProduct(toPlayerVec);
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有" + orientation + "的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个{}的生物", player.getName().getString(), targetEntities.size(), orientation);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个" + orientation + "的生物"), true);
            }
         } else {
            boolean bothEquipped = hasTaitouGhost(player) && hasDitouGhost(player);
            attackNearestMarkedEntity(player, markedEntities, bothEquipped ? 2 : 1);
         }
      }
   }

   private static List<LivingEntity> findHeadUpEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> headUpEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         float pitch = entity.getPitch();
         if (pitch < -30.0F) {
            headUpEntities.add(entity);
         }
      }

      return headUpEntities;
   }

   private static List<LivingEntity> findHeadDownEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> headDownEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         float pitch = entity.getPitch();
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有靠近容器的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个靠近容器的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个靠近容器的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findContainerEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> containerEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isNearContainer(entity)) {
            containerEntities.add(entity);
         }
      }

      return containerEntities;
   }

   private static boolean isNearContainer(LivingEntity entity) {
      BlockPos entityPos = entity.getBlockPos();
      World world = entity.getWorld();

      for (int x = -3; x <= 3; x++) {
         for (int y = -3; y <= 3; y++) {
            for (int z = -3; z <= 3; z++) {
               BlockPos checkPos = entityPos.add(x, y, z);
               BlockState blockState = world.getBlockState(checkPos);
               if (blockState.getBlock() instanceof ChestBlock
                  || blockState.getBlock() instanceof BarrelBlock
                  || blockState.getBlock() instanceof ShulkerBoxBlock
                  || blockState.getBlock() instanceof DispenserBlock
                  || blockState.getBlock() instanceof DropperBlock
                  || blockState.getBlock() instanceof HopperBlock) {
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有与你有眼神接触的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个有眼神接触的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个有眼神接触的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findEyeContactEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> eyeContactEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (hasEyeContact(player, entity)) {
            eyeContactEntities.add(entity);
         }
      }

      return eyeContactEntities;
   }

   private static boolean hasEyeContact(PlayerEntity player, LivingEntity entity) {
      Vec3d playerPos = player.getPos();
      Vec3d entityPos = entity.getPos();
      Vec3d directionToPlayer = playerPos.subtract(entityPos).normalize();
      Vec3d entityLookVec = entity.getRotationVec(1.0F);
      double dotProduct = entityLookVec.dotProduct(directionToPlayer);
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有可交易的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个可交易的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个可交易的生物"), true);
            }
         } else {
            attackNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findTradeEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> tradeEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (hasTradeAbility(entity) || hasGhostMoney(player)) {
            tradeEntities.add(entity);
         }
      }

      return tradeEntities;
   }

   private static boolean hasGhostMoney(PlayerEntity player) {
      return player.getInventory().containsAny(itemStack -> itemStack.hasNbt() && itemStack.getNbt().contains("ghostMoney"));
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有正在跳跃的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个正在跳跃的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个正在跳跃的生物"), true);
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
            serverPlayer.sendMessage(Text.literal("§c鬼雾J键：范围内没有被标记的生物"), true);
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
            serverPlayer.sendMessage(Text.literal("§c方块鬼J键：范围内没有被标记的生物"), true);
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
            serverPlayer.sendMessage(Text.literal("§c食物鬼J键：范围内没有被标记的生物"), true);
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
            serverPlayer.sendMessage(Text.literal("§c没有找到目标生物"), true);
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
         LOGGER.debug("玩家 {} 使用食物鬼V键技能对玩家 {} 施加饥荒效果", player.getName().getString(), targetPlayer.getName().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§6对玩家 " + targetPlayer.getName().getString() + " 施加饥荒效果"), true);
         }
      } else {
         markEntity(player, target);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§6对生物 " + target.getName().getString() + " 施加饥荒标记"), true);
         }
      }
   }

   private static void consumeTargetHunger(PlayerEntity targetPlayer) {
      if (targetPlayer instanceof ServerPlayerEntity serverTarget) {
         int currentHunger = serverTarget.getHungerManager().getFoodLevel();
         int hungerToConsume = Math.max(1, currentHunger / 2);
         int newHunger = Math.max(1, currentHunger - hungerToConsume);
         serverTarget.getHungerManager().setFoodLevel(newHunger);
         serverTarget.sendMessage(Text.literal("§c你被施加了饥荒效果，饱食度减少一半"), true);
         LOGGER.debug("玩家 {} 的饱食度从 {} 减少到 {}", targetPlayer.getName().getString(), currentHunger, newHunger);
      }
   }

   private static List<LivingEntity> findJumpingEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> jumpingEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isJumping(entity)) {
            jumpingEntities.add(entity);
         }
      }

      return jumpingEntities;
   }

   private static boolean isJumping(LivingEntity entity) {
      return entity.getVelocity().y > 0.1;
   }

   private static void markEntities(PlayerEntity player, List<LivingEntity> entities) {
      for (LivingEntity entity : entities) {
         entity.addStatusEffect(new StatusEffectInstance(ModEffects.MARK_CURSE, 200, 0, false, false, true));
      }
   }

   private static void markEntity(PlayerEntity player, LivingEntity entity) {
      entity.addStatusEffect(new StatusEffectInstance(ModEffects.MARK_CURSE, 200, 0, false, false, true));
   }

   private static List<LivingEntity> findMarkedEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> markedEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (entity.hasStatusEffect(ModEffects.MARK_CURSE)) {
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
         double distance = player.distanceTo(entity);
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
            double minDistance = player.distanceTo(nearestEntity);
            LOGGER.debug("玩家 {} 攻击最近被标记的生物 {}，距离: {}", player.getName().getString(), nearestEntity.getName().getString(), minDistance);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               executeSkillSpiritAttack(serverPlayer, nearestEntity, damageMultiplier);
            }
         }
      }
   }

   public static void executeSkillSpiritAttack(ServerPlayerEntity player, LivingEntity target) {
      NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
      float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 0.0F;
      float tempSpiritDamage = spiritAttributes.contains("tempSpiritDamage") ? (float)spiritAttributes.getDouble("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = spiritAttributes.contains("tempSpiritDamageMultiplier")
         ? (float)spiritAttributes.getDouble("tempSpiritDamageMultiplier")
         : 1.0F;
      float totalSpiritDamage = spiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
      if (totalSpiritDamage <= 0.0F) {
         totalSpiritDamage = 5.0F;
      }

      executeSkillSpiritAttack(player, target, totalSpiritDamage);
   }

   public static void executeSkillSpiritAttack(ServerPlayerEntity player, LivingEntity target, int damageMultiplier) {
      NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
      float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 0.0F;
      float tempSpiritDamage = spiritAttributes.contains("tempSpiritDamage") ? (float)spiritAttributes.getDouble("tempSpiritDamage") : 0.0F;
      float tempSpiritDamageMultiplier = spiritAttributes.contains("tempSpiritDamageMultiplier")
         ? (float)spiritAttributes.getDouble("tempSpiritDamageMultiplier")
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
         DamageSource damageSource = player.getDamageSources().playerAttack(player);
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

         if (!target.isAlive()) {
            QuestEventHandler.handleEntityKill(player, target);
         }
      } finally {
         ModEvents.processingSpiritDamage.set(false);
      }
   }

   private static void handleSkillDamageToPlayer(ServerPlayerEntity attacker, ServerPlayerEntity target, DamageSource source, float damageAmount) {
      if (!ClientModConfig.getInstance().isDamageWhitelisted(attacker.getUuid(), target.getName().getString())) {
         float actualDamage = PlayerEvents.handleSpiritDamage(target, damageAmount, damageAmount, source);
         if (actualDamage > 0.0F && ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid())) {
            attacker.sendMessage(
               Text.literal("§a对 §f" + target.getName().getString() + "§a 造成 §f" + new DecimalFormat("#.###").format(actualDamage) + "§a 点灵异伤害"), true
            );
         }

         if (damageAmount > 0.0F && !target.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)target.getWorld();
            Vec3d pos = target.getPos();

            for (int i = 0; i < 25; i++) {
               double offsetX = (target.getRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = target.getRandom().nextDouble() * 1.5 + 0.5;
               double offsetZ = (target.getRandom().nextDouble() - 0.5) * 2.0;
               double velocityX = (target.getRandom().nextDouble() - 0.5) * 0.2;
               double velocityY = target.getRandom().nextDouble() * 0.3 + 0.1;
               double velocityZ = (target.getRandom().nextDouble() - 0.5) * 0.2;
               serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
            }
         }
      }
   }

   private static void handleSkillDamageToEntity(ServerPlayerEntity attacker, LivingEntity target, DamageSource source, float damageAmount) {
      float healthBeforeDamage = target.getHealth();
      target.damage(source, damageAmount);
      if (!target.isAlive() && healthBeforeDamage > 0.0F) {
         QuestEventHandler.handleEntityKill(attacker, target);
      }

      if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(attacker.getUuid())) {
         float actualDamage = healthBeforeDamage - target.getHealth();
         attacker.sendMessage(Text.literal("§a对 " + target.getName().getString() + " 造成了 " + new DecimalFormat("#.###").format(actualDamage) + " 点灵异伤害"), true);
      }

      if (damageAmount > 0.0F && !target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 25; i++) {
            double offsetX = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = target.getRandom().nextDouble() * 1.5 + 0.5;
            double offsetZ = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double velocityX = (target.getRandom().nextDouble() - 0.5) * 0.2;
            double velocityY = target.getRandom().nextDouble() * 0.3 + 0.1;
            double velocityZ = (target.getRandom().nextDouble() - 0.5) * 0.2;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
         }
      }
   }

   public static boolean isSkillOnCooldown(PlayerEntity player, String skillName) {
      if (PlayerEvents.isGhostChildFused(player)) {
         return false;
      }

      NbtCompound data = PlayerEvents.getCachedData(player);
      String cooldownKey = "smfs_skill_cooldown_" + skillName;
      if (data.contains(cooldownKey)) {
         long cooldownEndTime = data.getLong(cooldownKey);
         long currentTime = player.getWorld().getTime();
         if (currentTime < cooldownEndTime) {
            return true;
         }

         data.remove(cooldownKey);
         PlayerEvents.saveDataToPlayer(player, data);
      }

      return false;
   }

   public static long getSkillCooldownRemaining(PlayerEntity player, String skillName) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      String cooldownKey = "smfs_skill_cooldown_" + skillName;
      if (data.contains(cooldownKey)) {
         long cooldownEndTime = data.getLong(cooldownKey);
         long currentTime = player.getWorld().getTime();
         if (currentTime < cooldownEndTime) {
            return cooldownEndTime - currentTime;
         }

         data.remove(cooldownKey);
         PlayerEvents.saveDataToPlayer(player, data);
      }

      return 0L;
   }

   public static void setSkillCooldown(PlayerEntity player, String skillName, int ticks) {
      if (!PlayerEvents.isGhostChildFused(player)) {
         NbtCompound data = PlayerEvents.getCachedData(player);
         String cooldownKey = "smfs_skill_cooldown_" + skillName;
         long currentTime = player.getWorld().getTime();
         long cooldownEndTime = currentTime + ticks;
         data.putLong(cooldownKey, cooldownEndTime);
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
      LOGGER.debug("{}鬼域检测到玩家 {} {}，已自动标记", ghostName, player.getName().getString(), behavior);
   }

   public static void markEntityForFogGhost(ServerPlayerEntity player, Entity entity) {
      if (hasFogGhost(player) && entity instanceof LivingEntity) {
         Vec3d velocity = entity.getVelocity();
         double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
         if (speed > 0.1) {
            List<LivingEntity> entities = new ArrayList<>();
            entities.add((LivingEntity)entity);
            markEntities(player, entities);
            LOGGER.debug("鬼雾鬼域检测到玩家 {} 附近移动实体 {}，速度：{}，已自动标记", player.getName().getString(), entity.getName().getString(), speed);
         }
      }
   }

   public static boolean hasQiaomenGhost(PlayerEntity player) {
      if (isHoldingShardItem(player, "qiaomen_ghost")) {
         return true;
      }

      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.getItem() == ModItems.QIAOMEN_GHOST) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasVillagerGhost(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.getItem() == ModItems.VILLAGER_GHOST) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasGhostWind(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem != null && ghostItem.getItem() == ModItems.GHOST_WIND) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasGhostOfficer(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "ghost_officer");
   }

   public static void handleQiaomenGhostSkill(PlayerEntity player) {
      HitResult hitResult = player.raycast(20.0, 0.0F, false);
      if (hitResult.getType() == Type.MISS) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c请对准一个门使用敲门鬼J键技能"), true);
         }
      } else {
         BlockPos targetPos;
         if (hitResult.getType() == Type.BLOCK) {
            targetPos = ((BlockHitResult)hitResult).getBlockPos();
         } else {
            Entity targetEntity = ((EntityHitResult)hitResult).getEntity();
            targetPos = targetEntity.getBlockPos();
         }

         World world = player.getWorld();
         BlockState blockState = world.getBlockState(targetPos);
         if (!(blockState.getBlock() instanceof DoorBlock)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§c请对准一个门使用敲门鬼J键技能"), true);
            }
         } else {
            playKnockingSound(player, 1);
            List<LivingEntity> entitiesInRange = player.getWorld()
               .getEntitiesByClass(LivingEntity.class, new Box(targetPos).expand(3.0), entity -> entity != player);
            int attackCount = 0;
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 0.0F;
            if (spiritDamage <= 0.0F) {
               spiritDamage = 5.0F;
            }

            for (LivingEntity target : entitiesInRange) {
               executeSkillSpiritAttack((ServerPlayerEntity)player, target);
               attackCount++;
            }

            LOGGER.debug("玩家 {} 使用敲门鬼J键技能，敲击了准星位置的门，对 {} 个生物进行了袭击", player.getName().getString(), attackCount);
            if (player instanceof ServerPlayerEntity serverPlayer
               && ModConfig.getInstance().showActionBarInfo
               && ClientModConfig.getInstance().showDamageText(player.getUuid())) {
               serverPlayer.sendMessage(Text.literal("§a对" + attackCount + "个生物造成" + new DecimalFormat("#.###").format(spiritDamage) + "点灵异伤害"), true);
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有门，无法使用鬼敲门技能"), true);
         }
      } else {
         playKnockingSound(player, doorCount);
         List<LivingEntity> entitiesInRange = player.getWorld()
            .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entity -> entity != player);
         int attackedEntities = 0;

         for (LivingEntity target : entitiesInRange) {
            if (hasDoorNearby(target, 3)) {
               executeSkillSpiritAttack((ServerPlayerEntity)player, target, doorCount);
               attackedEntities++;
            }
         }

         LOGGER.debug("玩家 {} 使用敲门鬼N键技能，检测到 {} 扇门，对 {} 个生物进行了袭击（伤害倍率 {}x）", player.getName().getString(), doorCount, attackedEntities, doorCount);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§a鬼敲门：共敲响 " + doorCount + " 扇门，对 " + attackedEntities + " 个生物造成 " + doorCount + "倍 灵异叠加"), true);
         }
      }
   }

   public static void handleQiaomenGhostGSkill(PlayerEntity player) {
      HitResult hitResult = player.raycast(20.0, 0.0F, false);
      if (hitResult.getType() != Type.BLOCK) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c请对准一个门使用敲门鬼G键技能"), true);
         }
      } else {
         BlockHitResult blockHit = (BlockHitResult)hitResult;
         BlockPos hitPos = blockHit.getBlockPos();
         World world = player.getWorld();
         BlockState blockState = world.getBlockState(hitPos);
         if (!(blockState.getBlock() instanceof DoorBlock)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§c请对准一个门使用敲门鬼G键技能"), true);
            }
         } else {
            Direction hitFace = blockHit.getSide();
            BlockPos teleportPos = hitPos.offset(hitFace);
            teleportPlayerToPosition(player, teleportPos);
            LOGGER.debug("玩家 {} 使用敲门鬼G键技能，传送到门位置 {}", player.getName().getString(), teleportPos);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已传送到目标门位置"), true);
            }
         }
      }
   }

   private static boolean hasDoorNearby(LivingEntity entity, int radius) {
      BlockPos entityPos = entity.getBlockPos();
      World world = entity.getWorld();

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = entityPos.add(x, y, z);
               BlockState state = world.getBlockState(checkPos);
               if (state.getBlock() instanceof DoorBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static void handleQiaomenGhostVSkill(PlayerEntity player) {
      HitResult hitResult = player.raycast(20.0, 0.0F, false);
      if (hitResult.getType() == Type.MISS) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c请对准一个位置使用敲门鬼V键技能"), true);
         }
      } else {
         BlockPos targetPos;
         if (hitResult.getType() == Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockPos abovePos = hitPos.up();
            World world = player.getWorld();
            if (world.getBlockState(abovePos).isAir() && world.getBlockState(abovePos.up()).isAir()) {
               targetPos = abovePos;
            } else {
               Direction facing = player.getHorizontalFacing();
               targetPos = hitPos.offset(facing);
               if (!world.getBlockState(targetPos).isAir() || !world.getBlockState(targetPos.up()).isAir()) {
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.sendMessage(Text.literal("§c目标位置不适合生成门"), true);
                  }

                  return;
               }
            }
         } else {
            targetPos = new BlockPos((int)hitResult.getPos().x, (int)hitResult.getPos().y, (int)hitResult.getPos().z);
            World world = player.getWorld();
            if (!world.getBlockState(targetPos).isAir() || !world.getBlockState(targetPos.up()).isAir()) {
               Direction facing = player.getHorizontalFacing();
               targetPos = player.getBlockPos().offset(facing);
               if (!world.getBlockState(targetPos).isAir() || !world.getBlockState(targetPos.up()).isAir()) {
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.sendMessage(Text.literal("§c目标位置不适合生成门"), true);
                  }

                  return;
               }
            }
         }

         generateSingleDoor(targetPos, player.getWorld(), player);
         LOGGER.debug("玩家 {} 使用敲门鬼V键技能，在位置 {} 生成了一扇门", player.getName().getString(), targetPos);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§a已在目标位置生成一扇门"), true);
         }
      }
   }

   private static void handleDoorKnockingSkill(
      PlayerEntity player, String skillName, Predicate<PlayerEntity> hasGhostChecker, BiFunction<PlayerEntity, Integer, List<BlockEntity>> doorFinder
   ) {
      if (!hasGhostChecker.test(player)) {
         LOGGER.warn("玩家 {} 尝试使用{}鬼技能但未驾驭{}鬼", player.getName().getString(), skillName, skillName);
      } else {
         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
         int radius = getDomainRadius(player, level);
         List<BlockEntity> targetDoors = doorFinder.apply(player, radius);
         if (targetDoors.isEmpty()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§c范围内没有门"), true);
            }
         } else {
            List<BlockEntity> markedDoors = findMarkedDoorsInRange(player, radius);
            if (markedDoors.isEmpty()) {
               markDoors(player, targetDoors);
               LOGGER.debug("玩家 {} 标记了 {} 扇门", player.getName().getString(), targetDoors.size());
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.sendMessage(Text.literal("§a已标记 " + targetDoors.size() + " 扇门"), true);
               }
            } else {
               knockNearestMarkedDoor(player, markedDoors);
            }
         }
      }
   }

   private static List<BlockEntity> findDoorEntitiesInRange(PlayerEntity player, int radius) {
      List<BlockEntity> doors = new ArrayList<>();
      BlockPos playerPos = player.getBlockPos();
      World world = player.getWorld();

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = playerPos.add(x, y, z);
               BlockState blockState = world.getBlockState(checkPos);
               if (blockState.getBlock() instanceof DoorBlock) {
                  BlockEntity blockEntity = world.getBlockEntity(checkPos);
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
      BlockPos playerPos = player.getBlockPos();
      World world = player.getWorld();
      int radiusSquared = radius * radius;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + y * y + z * z <= radiusSquared) {
                  BlockPos checkPos = playerPos.add(x, y, z);
                  BlockState blockState = world.getBlockState(checkPos);
                  if (blockState.getBlock() instanceof DoorBlock) {
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
         LOGGER.debug("玩家 {} 敲击了门", player.getName().getString());
      }
   }

   private static void playKnockingSound(PlayerEntity player, int knockCount) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         for (int i = 0; i < knockCount; i++) {
            serverPlayer.playSound(SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, 1.0F, 1.0F);
         }
      }
   }

   private static void generateDoorCircle(LivingEntity target, PlayerEntity player) {
      World world = target.getWorld();
      BlockPos centerPos = target.getBlockPos();
      int radius = 3;

      for (int i = 0; i < 8; i++) {
         double angle = (Math.PI * 2) * i / 8.0;
         int x = centerPos.getX() + (int)(radius * Math.cos(angle));
         int z = centerPos.getZ() + (int)(radius * Math.sin(angle));
         BlockPos doorPos = new BlockPos(x, centerPos.getY(), z);
         if (world.getBlockState(doorPos).isAir() && world.getBlockState(doorPos.up()).isAir()) {
            Direction facing = Direction.fromRotation(angle * 180.0 / Math.PI);
            world.setBlockState(
               doorPos,
               (BlockState)((BlockState)((BlockState)((BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.FACING, facing))
                        .with(DoorBlock.HINGE, DoorHinge.LEFT))
                     .with(DoorBlock.OPEN, false))
                  .with(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            );
            world.setBlockState(
               doorPos.up(),
               (BlockState)((BlockState)((BlockState)((BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.FACING, facing))
                        .with(DoorBlock.HINGE, DoorHinge.LEFT))
                     .with(DoorBlock.OPEN, false))
                  .with(DoorBlock.HALF, DoubleBlockHalf.UPPER)
            );
         }
      }
   }

   private static void generateSingleDoor(BlockPos pos, World world, PlayerEntity player) {
      if (world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir()) {
         Direction facing = player.getHorizontalFacing();
         world.setBlockState(
            pos,
            (BlockState)((BlockState)((BlockState)((BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.FACING, facing))
                     .with(DoorBlock.HINGE, DoorHinge.LEFT))
                  .with(DoorBlock.OPEN, false))
               .with(DoorBlock.HALF, DoubleBlockHalf.LOWER)
         );
         world.setBlockState(
            pos.up(),
            (BlockState)((BlockState)((BlockState)((BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.FACING, facing))
                     .with(DoorBlock.HINGE, DoorHinge.LEFT))
                  .with(DoorBlock.OPEN, false))
               .with(DoorBlock.HALF, DoubleBlockHalf.UPPER)
         );
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSound(SoundEvents.BLOCK_WOOD_PLACE, 1.0F, 1.0F);
         }
      } else {
         LOGGER.warn("无法在位置 {} 生成门，位置已被占用", pos);
      }
   }

   private static boolean isSafeTeleportPosition(World world, BlockPos pos) {
      BlockPos feetPos = pos;
      BlockPos headPos = pos.up();
      return world.getBlockState(feetPos).isAir() && world.getBlockState(headPos).isAir();
   }

   private static void teleportPlayerToPosition(PlayerEntity player, BlockPos targetPos) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         double x = targetPos.getX() + 0.5;
         double y = targetPos.getY();
         double z = targetPos.getZ() + 0.5;
         serverPlayer.teleport(x, y, z);
         serverPlayer.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
      }
   }

   public static void handleVillagerGhostNSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 使用村民鬼N键技能：引爆目标鬼奴", player.getName().getString());
      int villagerGhostSlot = PlayerEvents.findEquippedVillagerGhostSlot(player);
      int villagerGhostLevel = PlayerEvents.getGhostSlotLevel(player, villagerGhostSlot);
      int domainRadius = getDomainRadius(player, villagerGhostLevel);
      List<GhostSlaveEntity> ghostSlaves = findGhostSlavesInRange(player, domainRadius);
      if (ghostSlaves.isEmpty()) {
         LOGGER.debug("玩家 {} 鬼域范围内没有鬼奴", player.getName().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c鬼域范围内没有鬼奴"), true);
         }
      } else {
         int explodedCount = 0;

         for (GhostSlaveEntity ghostSlave : ghostSlaves) {
            if (explodeGhostSlave(ghostSlave, player)) {
               explodedCount++;
            }
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§a成功引爆 " + explodedCount + " 个鬼奴"), true);
         }

         LOGGER.debug("玩家 {} 引爆了 {} 个鬼奴", player.getName().getString(), explodedCount);
      }
   }

   private static List<GhostSlaveEntity> findGhostSlavesInRange(PlayerEntity player, int radius) {
      List<GhostSlaveEntity> ghostSlaves = new ArrayList<>();
      BlockPos playerPos = player.getBlockPos();
      World world = player.getWorld();

      for (LivingEntity entity : world.getEntitiesByClass(
         LivingEntity.class,
         new Box(
            playerPos.getX() - radius,
            playerPos.getY() - radius,
            playerPos.getZ() - radius,
            playerPos.getX() + radius,
            playerPos.getY() + radius,
            playerPos.getZ() + radius
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
         BlockPos ghostSlavePos = ghostSlave.getBlockPos();
         World world = ghostSlave.getWorld();
         float explosionRadius = 4.0F;
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 0.0F;
         float tempSpiritDamage = spiritAttributes.contains("tempSpiritDamage") ? (float)spiritAttributes.getDouble("tempSpiritDamage") : 0.0F;
         float tempSpiritDamageMultiplier = spiritAttributes.contains("tempSpiritDamageMultiplier")
            ? (float)spiritAttributes.getDouble("tempSpiritDamageMultiplier")
            : 1.0F;
         float totalSpiritDamage = spiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
         if (totalSpiritDamage <= 0.0F) {
            totalSpiritDamage = 5.0F;
         }

         float explosionDamage = totalSpiritDamage * 1.2F;
         List<LivingEntity> entitiesInRange = world.getEntitiesByClass(
            LivingEntity.class,
            new Box(
               ghostSlavePos.getX() - explosionRadius,
               ghostSlavePos.getY() - explosionRadius,
               ghostSlavePos.getZ() - explosionRadius,
               ghostSlavePos.getX() + explosionRadius,
               ghostSlavePos.getY() + explosionRadius,
               ghostSlavePos.getZ() + explosionRadius
            ),
            entity -> entity != player && entity.isAlive() && !(entity instanceof GhostSlaveEntity)
         );
         DamageSource damageSource = ModDamageSources.ghost(world);

         for (LivingEntity target : entitiesInRange) {
            double distance = target.distanceTo(ghostSlave);
            if (distance <= explosionRadius) {
               float distanceFactor = (float)Math.max(0.2, 1.0 - distance / explosionRadius * 0.8);
               float actualDamage = explosionDamage * distanceFactor;
               if (target instanceof ServerPlayerEntity targetPlayer) {
                  PlayerEvents.handleSpiritDamage(targetPlayer, actualDamage, actualDamage, damageSource);
               } else if (target instanceof GhostEntity ghost) {
                  ghost.handleSkillSpiritDamage(player instanceof ServerPlayerEntity sp ? sp : null, actualDamage);
               } else {
                  target.damage(damageSource, actualDamage);
               }
            }
         }

         if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
               ParticleTypes.EXPLOSION, ghostSlavePos.getX() + 0.5, ghostSlavePos.getY() + 1.0, ghostSlavePos.getZ() + 0.5, 15, 0.8, 0.8, 0.8, 0.2
            );
            serverWorld.spawnParticles(
               ParticleTypes.WITCH, ghostSlavePos.getX() + 0.5, ghostSlavePos.getY() + 1.0, ghostSlavePos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.1
            );
         }

         world.playSound(null, ghostSlavePos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F);
         ghostSlave.discard();
         return true;
      } catch (Exception e) {
         LOGGER.error("引爆鬼奴时发生错误", e);
         return false;
      }
   }

   public static void handleVillagerGhostGSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 使用村民鬼G键技能：给鬼域范围内的鬼奴添加力量1buff", player.getName().getString());
      int villagerGhostSlot = PlayerEvents.findEquippedVillagerGhostSlot(player);
      int villagerGhostLevel = PlayerEvents.getGhostSlotLevel(player, villagerGhostSlot);
      int domainRadius = getDomainRadius(player, villagerGhostLevel);
      List<GhostSlaveEntity> ghostSlaves = findGhostSlavesInRange(player, domainRadius);
      if (ghostSlaves.isEmpty()) {
         LOGGER.debug("玩家 {} 鬼域范围内没有鬼奴", player.getName().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c鬼域范围内没有鬼奴"), true);
         }
      } else {
         int buffedCount = 0;

         for (GhostSlaveEntity ghostSlave : ghostSlaves) {
            if (addStrengthBuffToGhostSlave(ghostSlave, player)) {
               buffedCount++;
            }
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§a成功给 " + buffedCount + " 个鬼奴添加力量1buff"), true);
         }

         LOGGER.debug("玩家 {} 给 {} 个鬼奴添加了力量1buff", player.getName().getString(), buffedCount);
      }
   }

   public static void handleVillagerGhostVSkill(PlayerEntity player) {
      LOGGER.debug("玩家 {} 使用村民鬼V键技能：急速", player.getName().getString());
      int villagerGhostSlot = PlayerEvents.findEquippedVillagerGhostSlot(player);
      int villagerGhostLevel = PlayerEvents.getGhostSlotLevel(player, villagerGhostSlot);
      int domainRadius = getDomainRadius(player, villagerGhostLevel);
      List<GhostSlaveEntity> ghostSlaves = findGhostSlavesInRange(player, domainRadius);
      if (ghostSlaves.isEmpty()) {
         LOGGER.debug("玩家 {} 鬼域范围内没有鬼奴", player.getName().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c鬼域范围内没有鬼奴"), true);
         }
      } else {
         int buffedCount = 0;

         for (GhostSlaveEntity ghostSlave : ghostSlaves) {
            if (addSpeedBuffToGhostSlave(ghostSlave, player)) {
               buffedCount++;
            }
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§a成功给 " + buffedCount + " 个鬼奴添加速度1buff"), true);
         }

         LOGGER.debug("玩家 {} 给 {} 个鬼奴添加了速度1buff", player.getName().getString(), buffedCount);
      }
   }

   private static boolean addStrengthBuffToGhostSlave(GhostSlaveEntity ghostSlave, PlayerEntity player) {
      try {
         StatusEffectInstance strengthEffect = new StatusEffectInstance(StatusEffects.STRENGTH, 100, 0, true, true);
         ghostSlave.addStatusEffect(strengthEffect);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.5F, 1.0F);
         }

         return true;
      } catch (Exception e) {
         LOGGER.error("给鬼奴添加力量buff时发生错误", e);
         return false;
      }
   }

   private static boolean addSpeedBuffToGhostSlave(GhostSlaveEntity ghostSlave, PlayerEntity player) {
      try {
         StatusEffectInstance speedEffect = new StatusEffectInstance(StatusEffects.SPEED, 100, 3, false, true);
         ghostSlave.addStatusEffect(speedEffect);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.5F, 1.0F);
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
      if (player.getWorld() instanceof ServerWorld serverWorld) {
         BlockPos var6 = null;
         HitResult hitResult = player.raycast(20.0, 0.0F, false);
         if (hitResult.getType() == Type.ENTITY) {
            Entity target = ((EntityHitResult)hitResult).getEntity();
            if (target instanceof LivingEntity) {
               var6 = target.getBlockPos();
            }
         } else if (hitResult.getType() == Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult)hitResult;
            var6 = blockHit.getBlockPos().offset(blockHit.getSide());
         }

         if (var6 != null) {
            BlockPos groundPos = var6;

            while (groundPos.getY() > serverWorld.getBottomY() && serverWorld.getBlockState(groundPos).isAir()) {
               groundPos = groundPos.down();
            }

            BlockPos finalPos = groundPos.up();
            if (serverWorld.getBlockState(finalPos).isAir()) {
               serverWorld.setBlockState(finalPos, ModBlocks.GRAVE_MOUND.getDefaultState());
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
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
            serverPlayer.sendMessage(Text.literal("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
            }
         } else {
            boolean bothEquipped = hasValidGhostFire(player) && hasWaterGhost(player);
            attackNearestMarkedEntity(player, markedEntities, bothEquipped ? 2 : 1);
         }
      }
   }

   private static List<LivingEntity> findCropEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> cropEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isOnCrop(entity)) {
            cropEntities.add(entity);
         }
      }

      return cropEntities;
   }

   private static boolean isOnCrop(LivingEntity entity) {
      BlockPos entityPos = entity.getBlockPos();
      BlockState blockBelow = entity.getWorld().getBlockState(entityPos.down());
      BlockState blockAtFeet = entity.getWorld().getBlockState(entityPos);
      return blockBelow.isIn(BlockTags.CROPS)
         || blockAtFeet.isIn(BlockTags.CROPS)
         || blockBelow.getBlock() instanceof FarmlandBlock
         || blockAtFeet.getBlock() instanceof FarmlandBlock;
   }

   private static List<LivingEntity> findStepEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> stepEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (entity.getY() < player.getY()) {
            stepEntities.add(entity);
         }
      }

      return stepEntities;
   }

   private static List<LivingEntity> findTrashEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> trashEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (hasItemsAround(entity, 6)) {
            trashEntities.add(entity);
         }
      }

      return trashEntities;
   }

   private static boolean hasItemsAround(LivingEntity entity, int range) {
      BlockPos entityPos = entity.getBlockPos();
      World world = entity.getWorld();

      for (int x = -range; x <= range; x++) {
         for (int y = -range; y <= range; y++) {
            for (int z = -range; z <= range; z++) {
               BlockPos checkPos = entityPos.add(x, y, z);
               List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, new Box(checkPos), item -> true);
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

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (isInWater(entity)) {
            waterEntities.add(entity);
         }
      }

      return waterEntities;
   }

   private static boolean isInWater(LivingEntity entity) {
      return entity.isTouchingWater() || entity.isSubmergedInWater();
   }

   public static void handleGanshiBrideGhostSkill(PlayerEntity player) {
      int radius = 20;
      List<LivingEntity> targetEntities = findGanshiBrideTargetsInRange(player, radius);
      if (targetEntities.isEmpty()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c范围内没有符合条件的生物"), true);
         }
      } else {
         List<LivingEntity> markedEntities = findMarkedEntitiesInRange(player, radius);
         if (markedEntities.isEmpty()) {
            markEntities(player, targetEntities);
            LOGGER.debug("玩家 {} 标记了 {} 个符合条件的生物", player.getName().getString(), targetEntities.size());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a已标记 " + targetEntities.size() + " 个符合条件的生物"), true);
            }
         } else {
            depriveGhostFromNearestMarkedEntity(player, markedEntities);
         }
      }
   }

   private static List<LivingEntity> findGanshiBrideTargetsInRange(PlayerEntity player, int radius) {
      List<LivingEntity> targets = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
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
                  LOGGER.debug("玩家 {} 剥夺了玩家 {} 的 {} 鬼，并获得了对应的驾驭物品", player.getName().getString(), targetPlayer.getName().getString(), deprivedGhostType);
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.sendMessage(
                        Text.literal("§6成功剥夺了玩家 " + targetPlayer.getName().getString() + " 的 " + deprivedGhostType + " 鬼，并获得了对应的驾驭物品"), true
                     );
                  }
               } else {
                  LOGGER.debug("玩家 {} 剥夺了玩家 {} 的一个鬼物品", player.getName().getString(), targetPlayer.getName().getString());
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     serverPlayer.sendMessage(Text.literal("§6成功剥夺了玩家 " + targetPlayer.getName().getString() + " 的一个鬼物品"), true);
                  }
               }

               if (targetPlayer instanceof ServerPlayerEntity targetServerPlayer) {
                  targetServerPlayer.sendMessage(Text.literal("§c你的一个鬼物品被玩家 " + player.getName().getString() + " 剥夺了"), true);
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
            if (!player.getInventory().insertStack(itemStack)) {
               player.getWorld().spawnEntity(new ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), itemStack));
               LOGGER.debug("玩家 {} 背包已满，{} 鬼物品掉落在地面上", player.getName().getString(), ghostType);
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.sendMessage(Text.literal("§e背包已满，" + ghostType + " 鬼物品掉落在地面上"), true);
               }
            } else {
               LOGGER.debug("玩家 {} 成功获得了 {} 鬼物品", player.getName().getString(), ghostType);
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.sendMessage(Text.literal("§a成功获得了 " + ghostType + " 鬼物品"), true);
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
         int currentMaxHealth = (int)serverPlayer.getMaxHealth();
         if (currentMaxHealth <= 4) {
            serverPlayer.sendMessage(Text.literal("§c生命上限不足，无法使用技能"), true);
            return;
         }

         serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth - 4);
         float currentHealth = serverPlayer.getHealth();
         serverPlayer.damage(serverPlayer.getDamageSources().generic(), 1.0F);
         serverPlayer.setHealth(currentHealth);
         LOGGER.debug("玩家 {} 使用许愿鬼N键技能，扣除4点生命上限，当前生命上限：{}", player.getName().getString(), currentMaxHealth - 4);
      }

      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      int level = PlayerEvents.getGhostSlotLevel(player, mainSlot);
      int radius = getDomainRadius(player, level);
      List<LivingEntity> ghostEntities = findGhostEntitiesInRange(player, radius);
      if (ghostEntities.isEmpty()) {
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c许愿鬼N键：鬼域范围内没有鬼"), true);
         }
      } else {
         for (LivingEntity ghost : ghostEntities) {
            if (ghost instanceof GhostEntity) {
               ghost.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 1200, 255, false, false, true));
            }
         }

         LOGGER.debug("玩家 {} 使用许愿鬼N键技能，给 {} 个鬼添加了1分钟沉寂效果", player.getName().getString(), ghostEntities.size());
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§6我说眼前灵异必将退散"), false);
         }
      }
   }

   public static void handleWishGhostGSkill(PlayerEntity player) {
      handleWishGhostGSkill(player, true);
   }

   public static void handleWishGhostGSkill(PlayerEntity player, boolean showMessage) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         int currentMaxHealth = (int)serverPlayer.getMaxHealth();
         if (currentMaxHealth <= 3) {
            serverPlayer.sendMessage(Text.literal("§c生命上限不足，无法使用技能"), true);
            return;
         }

         serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth - 3);
         float currentHealth = serverPlayer.getHealth();
         serverPlayer.damage(serverPlayer.getDamageSources().generic(), 1.0F);
         serverPlayer.setHealth(currentHealth);
         LOGGER.debug("玩家 {} 使用许愿鬼G键技能，扣除3点生命上限，当前生命上限：{}", player.getName().getString(), currentMaxHealth - 3);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 4, false, false, true));
         serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 600, 254, false, false, true));
      }

      LOGGER.debug("玩家 {} 使用许愿鬼G键技能，获得30秒抗性5和力量255", player.getName().getString());
      if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.sendMessage(Text.literal("§6我说我行不可摧，志不可改，力可至极限"), false);
      }
   }

   public static void handleWishGhostVSkill(PlayerEntity player) {
      handleWishGhostVSkill(player, true);
   }

   public static void handleWishGhostVSkill(PlayerEntity player, boolean showMessage) {
      boolean hasLostEffect = player.hasStatusEffect(ModEffects.LOST);
      boolean hasGhostDomainTargetEffect = player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.BLUE_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET)
         || player.hasStatusEffect(ModEffects.THICK_FOG_TARGET);
      if (hasLostEffect && hasGhostDomainTargetEffect) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            int currentMaxHealth = (int)serverPlayer.getMaxHealth();
            if (currentMaxHealth <= 2) {
               serverPlayer.sendMessage(Text.literal("§c生命上限不足，无法使用技能"), true);
               return;
            }

            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth - 2);
            float currentHealth = serverPlayer.getHealth();
            serverPlayer.damage(serverPlayer.getDamageSources().generic(), 1.0F);
            serverPlayer.setHealth(currentHealth);
            LOGGER.debug("玩家 {} 使用许愿鬼V键技能，扣除2点生命上限，当前生命上限：{}", player.getName().getString(), currentMaxHealth - 2);
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.clearStatusEffects();
            LOGGER.debug("玩家 {} 使用许愿鬼V键技能，清除了所有buff", player.getName().getString());
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            BlockPos safePos = findSafePosition(serverPlayer, 100);
            if (safePos == null) {
               if (showMessage) {
                  serverPlayer.sendMessage(Text.literal("§c许愿鬼V键：找不到安全位置"), true);
               }

               return;
            }

            serverPlayer.teleport(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
            LOGGER.debug("玩家 {} 使用许愿鬼V键技能，传送到安全位置: {}", player.getName().getString(), safePos);
         }

         LOGGER.debug("玩家 {} 使用许愿鬼V键技能，清除所有buff并传送到安全地方", player.getName().getString());
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§6我说我必离开这片鬼域"), false);
         }
      } else {
         if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c许愿鬼：没有被困在鬼域范围内。"), true);
         }
      }
   }

   public static void handleWishGhostJSkill(PlayerEntity player) {
      handleWishGhostJSkill(player, true);
   }

   public static void handleWishGhostJSkill(PlayerEntity player, boolean showMessage) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         int currentMaxHealth = (int)serverPlayer.getMaxHealth();
         if (currentMaxHealth <= 1) {
            serverPlayer.sendMessage(Text.literal("§c生命上限不足，无法使用技能"), true);
            return;
         }

         serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth - 1);
         float currentHealth = serverPlayer.getHealth();
         serverPlayer.damage(serverPlayer.getDamageSources().generic(), 1.0F);
         serverPlayer.setHealth(currentHealth);
         LOGGER.debug("玩家 {} 使用许愿鬼J键技能，扣除1点生命上限，当前生命上限：{}", player.getName().getString(), currentMaxHealth - 1);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.setHealth(255.0F);
         LOGGER.debug("玩家 {} 使用许愿鬼J键技能，恢复满生命值（255）", player.getName().getString());
      }

      LOGGER.debug("玩家 {} 使用许愿鬼J键技能，扣除1点生命上限并恢复满生命值", player.getName().getString());
      if (showMessage && player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.sendMessage(Text.literal("§6我说我身强体壮，灾病全无"), false);
      }
   }

   private static BlockPos findSafePosition(ServerPlayerEntity player, int radius) {
      World world = player.getWorld();
      BlockPos playerPos = player.getBlockPos();
      Random random = new Random();

      for (int attempt = 0; attempt < 50; attempt++) {
         int x = playerPos.getX() + random.nextInt(radius * 2) - radius;
         int z = playerPos.getZ() + random.nextInt(radius * 2) - radius;
         BlockPos candidatePos = findSafeYPosition(world, new BlockPos(x, playerPos.getY(), z));
         if (candidatePos != null && isPositionSafe(world, candidatePos)) {
            return candidatePos;
         }
      }

      return null;
   }

   private static BlockPos findSafeYPosition(World world, BlockPos pos) {
      for (int y = pos.getY(); y < world.getTopY(); y++) {
         BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
         BlockState blockState = world.getBlockState(checkPos);
         BlockState aboveState = world.getBlockState(checkPos.up());
         if (blockState.isAir() && aboveState.isAir()) {
            return checkPos;
         }
      }

      return null;
   }

   private static boolean isPositionSafe(World world, BlockPos pos) {
      List<Entity> nearbyEntities = world.getEntitiesByClass(
         Entity.class, new Box(pos).expand(5.0), entity -> entity.getType().getSpawnGroup() == SpawnGroup.MONSTER
      );
      return nearbyEntities.isEmpty();
   }

   public static List<LivingEntity> findGhostEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> ghostEntities = new ArrayList<>();

      for (LivingEntity entity : player.getWorld()
         .getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx instanceof LivingEntity)) {
         if (entity instanceof GhostEntity) {
            ghostEntities.add(entity);
         }
      }

      return ghostEntities;
   }

   public static void handleScapegoatGhostSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         LivingEntity target = findTargetInSight(player, 10.0);
         if (target == null) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.translatable("item.smfs.scapegoat_ghost.no_target"), true);
            }
         } else {
            StatusEffectInstance scapegoatMarkEffect = new StatusEffectInstance(ModEffects.SCAPEGOAT_MARK, 600, 0, false, true, true);
            target.addStatusEffect(scapegoatMarkEffect);
            LOGGER.debug("玩家 {} 使用替死鬼J键技能，标记目标 {} 为替死目标", player.getName().getString(), target.getName().getString());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.translatable("item.smfs.scapegoat_ghost.target_marked"), false);
            }

            spawnMarkParticles(target);
         }
      }
   }

   private static LivingEntity findTargetInSight(PlayerEntity player, double maxDistance) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         EntityHitResult entityHit = TargetingUtil.raycastEntity(serverPlayer, maxDistance);
         return entityHit != null ? (LivingEntity)entityHit.getEntity() : TargetingUtil.findEntityInLookDirectionWithOcclusion(player, maxDistance, 0.5);
      } else {
         return null;
      }
   }

   private static void spawnMarkParticles(LivingEntity target) {
      if (!target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 20; i++) {
            double offsetX = target.getRandom().nextDouble() - 0.5;
            double offsetY = target.getRandom().nextDouble() * 2.0 + 1.0;
            double offsetZ = target.getRandom().nextDouble() - 0.5;
            serverWorld.spawnParticles(ParticleTypes.FLAME, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.1, 0.1, 0.1, 0.05);
         }
      }
   }

   public static boolean hasScapegoatGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "scapegoat_ghost") || PlayerEvents.hasGhostType(player, "scapegoat_ghost");
   }

   public static void handleCandyGhostJSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         float currentHealth = player.getHealth();
         if (currentHealth <= 5.0F) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.translatable("item.smfs.candy_ghost.insufficient_health"), true);
            }
         } else {
            player.damage(player.getDamageSources().generic(), 5.0F);
            ItemStack candyStack = new ItemStack(ModItems.GHOST_CANDY, 1);
            if (!player.giveItemStack(candyStack)) {
               ItemEntity candyEntity = new ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), candyStack);
               player.getWorld().spawnEntity(candyEntity);
            }

            LOGGER.debug("玩家 {} 使用糖果鬼J键技能，扣除2点生命值获得鬼糖果", player.getName().getString());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.translatable("item.smfs.candy_ghost.skill_used"), false);
            }
         }
      }
   }

   public static boolean hasCandyGhost(PlayerEntity player) {
      return isHoldingShardItem(player, "candy_ghost") || PlayerEvents.hasGhostType(player, "candy_ghost");
   }

   public static void handleGhostEyeJSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         ItemStack mainHandStack = player.getMainHandStack();
         ItemStack offHandStack = player.getOffHandStack();
         boolean hasDeceptionNecklace = false;
         ItemStack necklaceStack = null;
         if (mainHandStack.getItem() == ModItems.DECEPTION_GHOST_NECKLACE) {
            hasDeceptionNecklace = true;
            necklaceStack = mainHandStack;
         } else if (offHandStack.getItem() == ModItems.DECEPTION_GHOST_NECKLACE) {
            hasDeceptionNecklace = true;
            necklaceStack = offHandStack;
         }

         if (!hasDeceptionNecklace) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.translatable("item.smfs.ghost_eye.no_deception_necklace"), true);
            }
         } else if (necklaceStack.getDamage() == 0) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.translatable("item.smfs.ghost_eye.necklace_already_full"), true);
            }
         } else {
            necklaceStack.setDamage(0);
            LOGGER.debug("玩家 {} 使用鬼眼J键技能，修复骗人鬼项链耐久度至满", player.getName().getString());
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.translatable("item.smfs.ghost_eye.necklace_repaired"), false);
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
         if (PlayerEvents.getGhostSlotItem(player, i).getItem() == ModItems.GHOST_SMOKE) {
            return PlayerEvents.getGhostSlotLevel(player, i);
         }
      }

      return 0;
   }

   public static void enableGrayGhostDomain(PlayerEntity player) {
      int level = getEffectiveDomainLevel(player, getGhostSmokeLevel(player));
      if (level <= 0) {
         LOGGER.warn("玩家 {} 尝试开启灰色鬼域但等级无效: {}", player.getName().getString(), level);
      } else {
         LOGGER.debug("为玩家 {} 开启 {} 级灰色鬼域", player.getName().getString(), level);
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN, Integer.MAX_VALUE, level - 1, false, false, true));
         if (player instanceof ServerPlayerEntity) {
            GrayGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            applyTargetGhostDomainToOtherPlayers(player, ModEffects.GRAY_GHOST_DOMAIN_TARGET, level);
         }

         applyLevelAbilities(player, level);
      }
   }

   private static void generateLaserBeam(PlayerEntity player, Vec3d lookVec, Vec3d startPos) {
      World world = player.getWorld();
      if (!world.isClient()) {
         ServerWorld serverWorld = (ServerWorld)world;
         double laserLength = 64.0;
         Vec3d endPos = startPos.add(lookVec.multiply(laserLength));
         generateLaserParticles(serverWorld, startPos, endPos);
         destroyBlocksAlongLaser(serverWorld, startPos, endPos, player);
         LOGGER.debug("玩家 {} 生成激光光束，从 {} 到 {}", player.getName().getString(), startPos, endPos);
      }
   }

   private static void generateLaserParticles(ServerWorld world, Vec3d startPos, Vec3d endPos) {
      Vec3d direction = endPos.subtract(startPos);
      double distance = direction.length();
      direction = direction.normalize();
      int particlesPerBlock = 5;
      int totalParticles = (int)(distance * particlesPerBlock);
      DustParticleEffect dustEffect = new DustParticleEffect(new Vec3d(1.0, 0.0, 0.0).toVector3f(), 1.0F);

      for (int i = 0; i < totalParticles; i++) {
         double progress = (double)i / totalParticles;
         Vec3d particlePos = startPos.add(direction.multiply(progress * distance));
         world.spawnParticles(dustEffect, particlePos.x, particlePos.y, particlePos.z, 1, 0.05, 0.05, 0.05, 0.02);
      }
   }

   private static void destroyBlocksAlongLaser(ServerWorld world, Vec3d startPos, Vec3d endPos, PlayerEntity player) {
      Vec3d direction = endPos.subtract(startPos);
      double distance = direction.length();
      direction = direction.normalize();
      double destructionRadius = 1.0;

      for (double d = 0.0; d <= distance; d++) {
         Vec3d checkPos = startPos.add(direction.multiply(d));

         for (double x = -destructionRadius; x <= destructionRadius; x++) {
            for (double y = -destructionRadius; y <= destructionRadius; y++) {
               for (double z = -destructionRadius; z <= destructionRadius; z++) {
                  BlockPos blockPos = new BlockPos((int)Math.floor(checkPos.x + x), (int)Math.floor(checkPos.y + y), (int)Math.floor(checkPos.z + z));
                  BlockState blockState = world.getBlockState(blockPos);
                  if (!blockState.isAir() && !(blockState.getBlock().getHardness() < 0.0F)) {
                     world.breakBlock(blockPos, false, player);
                  }
               }
            }
         }
      }
   }

   public static void handleBlockGhostVSkill(PlayerEntity player) {
      player.damage(player.getDamageSources().generic(), 4.0F);
      ItemStack concreteStack = getRandomConcreteBlock();
      concreteStack.setCount(16);
      if (!player.giveItemStack(concreteStack)) {
         ItemEntity concreteEntity = new ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), concreteStack);
         player.getWorld().spawnEntity(concreteEntity);
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.sendMessage(Text.literal("§6使用灵异力量幻化出了方块 "), true);
      }
   }

   private static ItemStack getRandomConcreteBlock() {
      Item[] concreteBlocks = new Item[]{
         Items.WHITE_CONCRETE,
         Items.ORANGE_CONCRETE,
         Items.MAGENTA_CONCRETE,
         Items.LIGHT_BLUE_CONCRETE,
         Items.YELLOW_CONCRETE,
         Items.LIME_CONCRETE,
         Items.PINK_CONCRETE,
         Items.GRAY_CONCRETE,
         Items.LIGHT_GRAY_CONCRETE,
         Items.CYAN_CONCRETE,
         Items.PURPLE_CONCRETE,
         Items.BLUE_CONCRETE,
         Items.BROWN_CONCRETE,
         Items.GREEN_CONCRETE,
         Items.RED_CONCRETE,
         Items.BLACK_CONCRETE
      };
      Random random = new Random();
      Item randomConcrete = concreteBlocks[random.nextInt(concreteBlocks.length)];
      return new ItemStack(randomConcrete);
   }

   public static void handlePlagueGhostJSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         LOGGER.debug("玩家 {} 使用瘟鬼J键技能：传染目标生物瘟疫", player.getName().getString());
         if (player instanceof ServerPlayerEntity serverPlayer) {
            double maxDistance = 10.0;
            LivingEntity bestTarget = TargetingUtil.findEntityInLookDirection(player, maxDistance, 0.5);
            if (bestTarget != null) {
               bestTarget.addStatusEffect(new StatusEffectInstance(ModEffects.PLAGUE, 600, 0));
            } else {
               serverPlayer.sendMessage(Text.literal("§c未找到可传染的目标生物"), true);
            }
         }
      }
   }

   public static void handleMineralGhostJSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         World world = player.getWorld();
         BlockPos playerPos = player.getBlockPos();
         int searchRadius = 24;
         List<BlockPos> mineralBlocks = new ArrayList<>();

         for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
               for (int z = -searchRadius; z <= searchRadius; z++) {
                  BlockPos pos = playerPos.add(x, y, z);
                  BlockState state = world.getBlockState(pos);
                  Block block = state.getBlock();
                  if (isMineralBlock(block)) {
                     mineralBlocks.add(pos);
                  }
               }
            }
         }

         if (!mineralBlocks.isEmpty()) {
            RaycastContext raycastContext = new RaycastContext(
               player.getEyePos(), player.getEyePos().add(player.getRotationVector().multiply(20.0)), ShapeType.OUTLINE, FluidHandling.NONE, player
            );
            BlockHitResult hitResult = world.raycast(raycastContext);
            Vec3d targetVec = hitResult.getPos();
            mineralBlocks.sort((pos1, pos2) -> {
               double distance1 = pos1.toCenterPos().squaredDistanceTo(targetVec);
               double distance2 = pos2.toCenterPos().squaredDistanceTo(targetVec);
               return Double.compare(distance1, distance2);
            });
            new Random();
            int blocksToExplode = Math.min(mineralBlocks.size(), 6);

            for (int i = 0; i < blocksToExplode; i++) {
               BlockPos pos = mineralBlocks.get(i);
               world.breakBlock(pos, true);
               float explosionPower = 6.0F;
               world.createExplosion(player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, explosionPower, ExplosionSourceType.MOB);
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
               }
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a成功引爆了 " + blocksToExplode + " 个矿物方块"), true);
            }
         } else if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c周围没有发现矿物方块"), true);
         }
      }
   }

   private static boolean isMineralBlock(Block block) {
      Set<Block> mineralBlocks = new HashSet<>(
         Arrays.asList(
            Blocks.COAL_ORE,
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COPPER_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.GOLD_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.NETHER_QUARTZ_ORE,
            Blocks.NETHER_GOLD_ORE,
            Blocks.ANCIENT_DEBRIS,
            ModBlocks.DEFILED_ORE,
            ModBlocks.DEEP_DEFILED_ORE
         )
      );
      return mineralBlocks.contains(block);
   }

   public static void handleShadowGhostJSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         LOGGER.debug("玩家 {} 使用黑影鬼J键技能：标记处于阴影中的生物并对其发动袭击", player.getName().getString());
         int searchRadius = 20;
         LivingEntity bestTarget = TargetingUtil.findEntityInLookDirection(
            player, searchRadius, 0.5, entity -> entity.getWorld().getLightLevel(entity.getBlockPos()) <= 4
         );
         if (bestTarget != null) {
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 5.0F;
            executeSkillSpiritAttack((ServerPlayerEntity)player, bestTarget);
            if (player instanceof ServerPlayerEntity serverPlayer && ClientModConfig.getInstance().showDamageText(serverPlayer.getUuid())) {
               serverPlayer.sendMessage(Text.literal("§a成功袭击了处于阴影中的生物，造成" + new DecimalFormat("#.###").format(spiritDamage) + "点灵异伤害"), true);
            }
         } else if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(Text.literal("§c周围20格内没有发现处于阴影中的生物"), true);
         }
      }
   }

   public static void handleDoorGhostJSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         HitResult hitResult = player.raycast(20.0, 0.0F, false);
         if (hitResult.getType() == Type.MISS) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§c请对准一个门使用开门鬼技能"), true);
            }
         } else if (hitResult.getType() != Type.BLOCK) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§c请对准一个门使用开门鬼技能"), true);
            }
         } else {
            BlockPos doorPos = ((BlockHitResult)hitResult).getBlockPos();
            World world = player.getWorld();
            BlockState doorState = world.getBlockState(doorPos);
            if (!(doorState.getBlock() instanceof DoorBlock)) {
               if (player instanceof ServerPlayerEntity serverPlayer) {
                  serverPlayer.sendMessage(Text.literal("§c请对准一个门使用开门鬼技能"), true);
               }
            } else {
               world.setBlockState(doorPos, (BlockState)doorState.cycle(DoorBlock.OPEN));
               playKnockingSound(player, 1);
               List<LivingEntity> entitiesInRange = world.getEntitiesByClass(LivingEntity.class, new Box(doorPos).expand(6.0), entity -> entity != player);
               int attackCount = 0;
               NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
               float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 5.0F;
               float actualDamage = spiritDamage * 2.0F;

               for (LivingEntity target : entitiesInRange) {
                  executeSkillSpiritAttack((ServerPlayerEntity)player, target, actualDamage);
                  attackCount++;
               }

               if (player instanceof ServerPlayerEntity serverPlayer) {
                  if (attackCount > 0 && ClientModConfig.getInstance().showDamageText(serverPlayer.getUuid())) {
                     serverPlayer.sendMessage(
                        Text.literal("§a成功打开了门并对" + attackCount + "个生物造成了" + new DecimalFormat("#.###").format(actualDamage) + "点灵异伤害"), true
                     );
                  } else {
                     serverPlayer.sendMessage(Text.literal("§a成功打开了门"), true);
                  }
               }
            }
         }
      }
   }

   public static void handleGhostShadowHeadJSkill(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         LOGGER.debug("玩家 {} 使用鬼影头J键技能：切换摄像机绑定", player.getName().getString());
      }
   }

   public static void handleGhostShadowHeadVSkill(PlayerEntity player, int targetId) {
      if (!player.getWorld().isClient()) {
         LOGGER.debug("玩家 {} 使用鬼影头V键技能：灵异袭击实体ID {}", player.getName().getString(), targetId);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            Entity target = player.getWorld().getEntityById(targetId);
            if (target != null && target instanceof LivingEntity livingTarget) {
               if (target == player) {
                  serverPlayer.sendMessage(Text.literal("§c不能袭击自己"), true);
               } else {
                  executeSkillSpiritAttack(serverPlayer, livingTarget);
                  restoreGodControlSpeed(targetId, target);
               }
            } else {
               serverPlayer.sendMessage(Text.literal("§c目标无效或已消失"), true);
            }
         }
      }
   }

   public static void handleGhostShadowHeadUnbindControl(PlayerEntity player, int targetId) {
      if (!player.getWorld().isClient()) {
         Entity target = player.getWorld().getEntityById(targetId);
         restoreGodControlSpeed(targetId, target);
      }
   }

   private static void restoreGodControlSpeed(int targetId, Entity target) {
      Double originalSpeed = GOD_CONTROL_ORIGINAL_SPEED.remove(targetId);
      if (originalSpeed != null && target instanceof LivingEntity livingTarget) {
         livingTarget.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(originalSpeed);
      }
   }

   public static void handleGhostShadowHeadGSkill(PlayerEntity player, int targetId) {
      if (!player.getWorld().isClient()) {
         LOGGER.debug("玩家 {} 使用鬼影头G键技能：施加缓慢3和虚弱3，目标实体ID {}", player.getName().getString(), targetId);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            Entity target = player.getWorld().getEntityById(targetId);
            if (target != null && target instanceof LivingEntity livingTarget) {
               if (target == player) {
                  serverPlayer.sendMessage(Text.literal("§c不能对自己施加效果"), true);
               } else {
                  livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 2));
                  livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600, 2));
                  serverPlayer.sendMessage(Text.literal("§a影响了 " + livingTarget.getName().getString() + "的意识，现在其状态下降了"), true);
               }
            } else {
               serverPlayer.sendMessage(Text.literal("§c目标无效或已消失"), true);
            }
         }
      }
   }

   public static void handleGhostShadowHeadNSkill(
      PlayerEntity player, int targetId, float forward, float sideways, boolean jumping, boolean sneaking, float playerYaw, float playerPitch
   ) {
      if (!player.getWorld().isClient()) {
         Entity target = player.getWorld().getEntityById(targetId);
         if (target != null && target instanceof LivingEntity livingTarget) {
            if (target != player) {
               double distance = player.distanceTo(target);
               if (!(distance > 200.0)) {
                  if (player instanceof ServerPlayerEntity serverPlayer && AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration")) {
                     livingTarget.setYaw(playerYaw);
                     livingTarget.setHeadYaw(playerYaw);
                     livingTarget.setBodyYaw(playerYaw);
                     livingTarget.setPitch(playerPitch);
                  }

                  boolean godControl = false;
                  if (player instanceof ServerPlayerEntity serverPlayer
                     && AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_god")
                     && target instanceof MobEntity mobTarget) {
                     godControl = true;
                     if (!GOD_CONTROL_ORIGINAL_SPEED.containsKey(targetId)) {
                        GOD_CONTROL_ORIGINAL_SPEED.put(targetId, livingTarget.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                     }

                     livingTarget.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0);
                  }

                  float speed = godControl
                     ? GOD_CONTROL_ORIGINAL_SPEED.getOrDefault(targetId, 0.1).floatValue()
                     : (float)livingTarget.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                  if (sneaking) {
                     speed *= 0.3F;
                  }

                  float yaw = livingTarget.getYaw();
                  float yawRad = yaw * (float) Math.PI / 180.0F;
                  double moveX = sideways * Math.cos(yawRad) - forward * Math.sin(yawRad);
                  double moveZ = sideways * Math.sin(yawRad) + forward * Math.cos(yawRad);
                  double len = Math.sqrt(moveX * moveX + moveZ * moveZ);
                  if (len > 0.0) {
                     moveX = moveX / len * speed;
                     moveZ = moveZ / len * speed;
                  }

                  livingTarget.setVelocity(moveX, livingTarget.getVelocity().y, moveZ);
                  if (jumping && livingTarget.isOnGround()) {
                     livingTarget.setVelocity(livingTarget.getVelocity().x, 0.42, livingTarget.getVelocity().z);
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
         player.sendMessage(Text.literal("§c" + skillName + "正在冷却中，剩余时间：" + String.format("%.1f", remainingSeconds) + "秒"), true);
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
