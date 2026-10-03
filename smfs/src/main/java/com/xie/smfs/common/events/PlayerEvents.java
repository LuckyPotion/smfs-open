package com.xie.smfs.common.events;

import com.xie.smfs.api.IPlayerData;
import com.xie.smfs.api.TameableItemAPI;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.effect.GoldenGhostDomainEffect;
import com.xie.smfs.effect.SpiritAttributes;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.item.BaseGhostEyeItem;
import com.xie.smfs.item.BlockGhostItem;
import com.xie.smfs.item.DefiledArmorItem;
import com.xie.smfs.item.FoodGhostItem;
import com.xie.smfs.item.GhostOfficerItem;
import com.xie.smfs.item.GhostShroudArmorItem;
import com.xie.smfs.item.GhostSmokeItem;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.item.QiaomenGhostItem;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.item.ScapegoatGhostItem;
import com.xie.smfs.item.SilentGhostEyeItem;
import com.xie.smfs.item.VillagerGhostItem;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.manager.PassiveSkillManager;
import com.xie.smfs.network.core.SpiritNetworkHandler;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import com.xie.smfs.network.packets.skills.s2c.GiantShadowGhostScaleS2CPacket;
import com.xie.smfs.network.packets.skills.s2c.GiantShadowGhostStopScaleS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.GhostAbilityPopupS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.OpenHumanSkinPaperEndingS2CPacket;
import com.xie.smfs.network.packets.ui.s2c.ScreenEffectS2CPacket;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.InstantKillUtil;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AfterPlayerChange;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.CopyFrom;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.EquipmentChange;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerEvents {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/PlayerEvents");
   public static final int GHOST_SLOT_COUNT = 10;
   public static final String SMFS_DATA_KEY = "smfs_spirit_data";
   private static final String GHOST_SLOTS_KEY = "GhostSlots";
   private static final String BONUS_SUPPRESSION_SLOTS_KEY = "BonusSuppressionSlots";
   public static final Map<UUID, NbtCompound> PLAYER_DATA_CACHE = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> GHOST_SHROUD_COOLDOWN = new ConcurrentHashMap<>();
   private static final long GHOST_SHROUD_COOLDOWN_MS = 5000L;

   public static void register() {
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> {
         ServerPlayerEntity player = handler.getPlayer();
         loadPlayerDataToCache(player);
         initPlayerAttributes(player);
      });
      ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, server) -> {
         ServerPlayerEntity player = handler.getPlayer();
         NbtCompound data = PLAYER_DATA_CACHE.get(player.getUuid());
         if (data != null) {
            saveDataToPlayer(player, data);
            LOGGER.info("玩家 {} 断开连接，已保存灵异数据", player.getName().getString());
         }

         PLAYER_DATA_CACHE.remove(player.getUuid());
      });
      ServerPlayerEvents.COPY_FROM.register((CopyFrom)(oldPlayer, newPlayer, alive) -> {
         copySpiritAttributes(oldPlayer, newPlayer);
         initPlayerAttributes(newPlayer);
      });
      ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((AfterPlayerChange)(player, origin, destination) -> {
         LOGGER.info("玩家 {} 从维度 {} 传送到维度 {}，同步灵异数据", player.getName().getString(), origin.getRegistryKey().getValue(), destination.getRegistryKey().getValue());
         loadPlayerDataToCache(player);
         initPlayerAttributes(player);
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (world.getTime() % 100L == 0L) {
            for (PlayerEntity player : world.getPlayers()) {
               if (GhostDomainManager.hasGhostEntitiesInRange(player, 32)) {
                  GhostDomainManager.sendGhostEyeWarning(player);
               }
            }
         }
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         for (PlayerEntity player : world.getPlayers()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               int mainSlot = MainGhostManager.getMainGhostSlot(serverPlayer);
               String mainGhostType = getGhostTypeInSlot(serverPlayer, mainSlot);
               if (!serverPlayer.isSneaking() || !"giant_shadow_ghost".equals(mainGhostType) && !"complete_shadow_ghost".equals(mainGhostType)) {
                  if (!serverPlayer.isSneaking() && ("giant_shadow_ghost".equals(mainGhostType) || "complete_shadow_ghost".equals(mainGhostType))) {
                     NbtCompound data = getCachedData(serverPlayer);
                     if (data.getBoolean("smfs:giant_shadow_sneaking")) {
                        long sneakStart = data.getLong("smfs:giant_shadow_sneak_start");
                        long sneakDuration = world.getTime() - sneakStart;
                        GhostSkillManager.handleGiantShadowGhostAreaAttack(serverPlayer, 5.0, sneakDuration);
                        data.putBoolean("smfs:giant_shadow_sneaking", false);
                        data.putLong("smfs:giant_shadow_sneak_start", 0L);
                     }
                  }
               } else {
                  NbtCompound data = getCachedData(serverPlayer);
                  if (!data.getBoolean("smfs:giant_shadow_sneaking")) {
                     data.putLong("smfs:giant_shadow_sneak_start", world.getTime());
                     data.putBoolean("smfs:giant_shadow_sneaking", true);
                  }
               }
            }
         }
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         if (world.getTime() % 20L == 0L) {
            for (PlayerEntity player : world.getPlayers()) {
               NbtCompound spiritAttributes = getSpiritAttributes(player);
               float currentSpirit = spiritAttributes.contains("currentSpirit") ? (float)spiritAttributes.getDouble("currentSpirit") : 0.0F;
               float maxSpirit = spiritAttributes.contains("maxSpirit") ? (float)spiritAttributes.getDouble("maxSpirit") : 0.0F;
               float revivalFactor = spiritAttributes.contains("revivalFactor") ? (float)spiritAttributes.getDouble("revivalFactor") : 0.0F;
               if (currentSpirit > maxSpirit) {
                  setCurrentSpirit(player, maxSpirit);
               } else if (currentSpirit < maxSpirit && revivalFactor > 0.0F) {
                  float baseRecovery = 10.0F;
                  float recoveryAmount = baseRecovery + baseRecovery * revivalFactor;
                  float newSpirit = Math.min(currentSpirit + recoveryAmount, maxSpirit);
                  setCurrentSpirit(player, newSpirit);
               }
            }
         }
      });
      ServerTickEvents.END_WORLD_TICK
         .register(
            (EndWorldTick)world -> {
               if (world.getTime() % 20L == 0L) {
                  ModConfig config = ModConfig.getInstance();
                  if (!config.enableSanitySystem) {
                     return;
                  }

                  for (PlayerEntity player : world.getPlayers()) {
                     GameMode gameMode = player.getWorld().isClient() ? null : ((ServerPlayerEntity)player).interactionManager.getGameMode();
                     if (gameMode != GameMode.SPECTATOR && gameMode != GameMode.CREATIVE) {
                        NbtCompound spiritAttributes = getSpiritAttributes(player);
                        float currentSanity = spiritAttributes.contains("sanity") ? (float)spiritAttributes.getDouble("sanity") : 100.0F;
                        float maxSanity = spiritAttributes.contains("maxSanity") ? (float)spiritAttributes.getDouble("maxSanity") : 100.0F;
                        if (currentSanity > maxSanity) {
                           setCurrentSanity(player, maxSanity);
                        } else {
                           if (currentSanity <= 0.0F && player instanceof ServerPlayerEntity serverPlayer) {
                              DamageSource sanityZeroDamage = ModDamageSources.sanityZero(serverPlayer.getWorld());
                              InstantKillUtil.executePlayerSelfKill(serverPlayer, sanityZeroDamage, true, true);
                           }

                           boolean isPlayerProtected = false;
                           if (player instanceof ServerPlayerEntity serverPlayer) {
                              isPlayerProtected = RedGhostCandleItem.isHoldingCandle(serverPlayer)
                                 || CoffinEffectManager.isPlayerInGoldCoffin(serverPlayer)
                                 || GoldBlockProtectionManager.isPlayerInGoldBlockShelter(serverPlayer)
                                 || SpectateModePacket.isInSpectatorMode(serverPlayer.getUuid())
                                 || serverPlayer.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY);
                           }

                           List<LivingEntity> ghostEntities = GhostDomainManager.findGhostEntitiesInRange(player, 16);
                           if (isPlayerProtected && !ghostEntities.isEmpty()) {
                              if (currentSanity < maxSanity) {
                                 float newSanity = Math.min(currentSanity + 1.0F, maxSanity);
                                 setCurrentSanity(player, newSanity);
                              }
                           } else if (ghostEntities.isEmpty()) {
                              if (currentSanity < maxSanity) {
                                 float newSanity = Math.min(currentSanity + 1.0F, maxSanity);
                                 setCurrentSanity(player, newSanity);
                              }
                           } else {
                              boolean hasGhostDream = hasGhostType(player, "ghost_dream");
                              boolean hasGhostShadowHead = hasGhostType(player, "ghost_shadow_head");
                              boolean hasCompleteShadowGhost = hasGhostType(player, "complete_shadow_ghost");
                              boolean isAberration = player instanceof ServerPlayerEntity serverPlayer
                                 && AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration");
                              if (!hasGhostDream && !hasGhostShadowHead && !hasCompleteShadowGhost && !isAberration) {
                                 LivingEntity nearestGhost = null;
                                 double nearestDistance = Double.MAX_VALUE;

                                 for (LivingEntity ghost : ghostEntities) {
                                    boolean isOwnGhost = false;
                                    if (ghost instanceof PlayerGhostEntity playerGhost) {
                                       String ghostPlayerUuid = playerGhost.getPlayerUuid();
                                       if (ghostPlayerUuid != null && ghostPlayerUuid.equals(player.getUuidAsString())) {
                                          isOwnGhost = true;
                                       }
                                    }

                                    if (!isOwnGhost) {
                                       double distance = player.getPos().distanceTo(ghost.getPos());
                                       if (distance < nearestDistance) {
                                          nearestDistance = distance;
                                          nearestGhost = ghost;
                                       }
                                    }
                                 }

                                 if (nearestGhost != null) {
                                    float sanityReduction = 0.0F;
                                    if (nearestDistance <= 3.0) {
                                       sanityReduction = 3.0F;
                                    } else if (nearestDistance <= 5.0) {
                                       sanityReduction = 2.0F;
                                    } else {
                                       sanityReduction = 1.0F;
                                    }

                                    float newSanity = Math.max(currentSanity - sanityReduction, 0.0F);
                                    setCurrentSanity(player, newSanity);
                                    if (newSanity <= 0.0F && player instanceof ServerPlayerEntity serverPlayer) {
                                       DamageSource sanityZeroDamage = ModDamageSources.sanityZero(serverPlayer.getWorld());
                                       InstantKillUtil.executePlayerSelfKill(serverPlayer, sanityZeroDamage, true, true);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         );
      ServerTickEvents.END_WORLD_TICK.register(PassiveSkillManager::triggerAllPlayersPassiveSkills);
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         for (PlayerEntity player : world.getPlayers()) {
            if (!player.isDead()) {
               NbtCompound data = getCachedData(player);
               long survivalTime = data.getLong("survivalTime");
               long totalTime = data.getLong("totalTime");
               data.putLong("survivalTime", survivalTime + 1L);
               data.putLong("totalTime", totalTime + 1L);
            }
         }
      });
      ServerEntityEvents.EQUIPMENT_CHANGE.register((EquipmentChange)(livingEntity, equipmentSlot, previous, next) -> {
         if (livingEntity instanceof PlayerEntity player && !livingEntity.getWorld().isClient()) {
            handleEquipmentChange(player, equipmentSlot, previous, next);
            handleHumanSkinPaperEquip(player, equipmentSlot, next);
         }
      });
      ServerTickEvents.END_WORLD_TICK.register((EndWorldTick)world -> {
         for (PlayerEntity player : world.getPlayers()) {
            if (player.isSneaking()) {
               int mainSlot = MainGhostManager.getMainGhostSlot(player);
               String mainGhostType = getGhostTypeInSlot(player, mainSlot);
               boolean isGiantShadowGhostMain = "giant_shadow_ghost".equals(mainGhostType) || "complete_shadow_ghost".equals(mainGhostType);
               if (isGiantShadowGhostMain) {
                  handleGiantShadowGhostSneakEffect(player);
               }

               formDefenseFormationOnSneak(player);
            } else {
               int mainSlot = MainGhostManager.getMainGhostSlot(player);
               String mainGhostType = getGhostTypeInSlot(player, mainSlot);
               boolean isGiantShadowGhostMain = "giant_shadow_ghost".equals(mainGhostType) || "complete_shadow_ghost".equals(mainGhostType);
               if (player instanceof ServerPlayerEntity serverPlayer && isGiantShadowGhostMain) {
                  GiantShadowGhostStopScaleS2CPacket.sendToClient(serverPlayer);
                  player.removeStatusEffect(StatusEffects.SPEED);
                  player.removeStatusEffect(StatusEffects.INVISIBILITY);
               }
            }
         }
      });
   }

   private static NbtCompound createDefaultSpiritData() {
      NbtCompound data = new NbtCompound();
      NbtCompound ghostSlots = new NbtCompound();

      for (int i = 0; i < 10; i++) {
         NbtCompound slotData = new NbtCompound();
         slotData.putBoolean("occupied", false);
         slotData.put("item", ItemStack.EMPTY.writeNbt(new NbtCompound()));
         slotData.putInt("level", 1);
         slotData.putInt("revivalDegree", 0);
         slotData.putInt("requiredRevivalDegree", 1000);
         slotData.putBoolean("unlocked", i != 0 && i != 3 && i < 6);
         slotData.putBoolean("slotDeadlocked", false);
         ghostSlots.put("Slot" + i, slotData);
      }

      data.put("GhostSlots", ghostSlots);
      data.putDouble("spiritResistance", 0.0);
      data.putDouble("spiritDamage", 0.0);
      data.putDouble("maxSpirit", 0.0);
      data.putDouble("currentSpirit", 0.0);
      data.putDouble("revivalFactor", 0.0);
      data.putDouble("sanity", 100.0);
      data.putDouble("maxSanity", 100.0);
      data.putDouble("maxHealth", 20.0);
      data.putDouble("tempSpiritResistance", 0.0);
      data.putDouble("tempSpiritResistanceMultiplier", 1.0);
      data.putDouble("tempSpiritDamage", 0.0);
      data.putDouble("tempSpiritDamageMultiplier", 1.0);
      data.putDouble("tempMaxSpirit", 0.0);
      data.putDouble("tempSanity", 0.0);
      data.putInt("ghostEyeRecoveryPoints", 0);
      NbtCompound questData = new NbtCompound();
      questData.put("activeQuests", new NbtList());
      questData.put("completedQuests", new NbtList());
      data.put("questData", questData);
      data.putBoolean("humanSkinPaperUnlocked", false);
      data.putLong("survivalTime", 0L);
      data.putLong("totalTime", 0L);
      data.putLong("lastSurvivalTime", 0L);
      initializeServantsData(data);
      data.putInt("BonusSuppressionSlots", 0);
      return data;
   }

   private static void loadPlayerDataToCache(PlayerEntity player) {
      NbtCompound playerNbt = player.writeNbt(new NbtCompound());
      if (playerNbt.contains("smfs_spirit_data")) {
         NbtCompound existingData = playerNbt.getCompound("smfs_spirit_data");
         if (!existingData.contains("GhostSlots")) {
            NbtCompound defaultData = createDefaultSpiritData();

            for (String key : defaultData.getKeys()) {
               if (!existingData.contains(key)) {
                  existingData.put(key, defaultData.get(key).copy());
               }
            }

            saveDataToPlayer(player, existingData);
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), existingData.copy());
         syncAttributesFromNbt(player, existingData);
      } else {
         NbtCompound spiritData = createDefaultSpiritData();
         PLAYER_DATA_CACHE.put(player.getUuid(), spiritData);
         saveDataToPlayer(player, spiritData);
      }
   }

   public static void syncAttributesFromNbt(PlayerEntity player, NbtCompound data) {
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.SPIRIT_RESISTANCE);
      if (instance != null) {
         instance.setBaseValue((float)data.getDouble("spiritResistance"));
      }

      instance = player.getAttributeInstance(SpiritAttributes.SPIRIT_DAMAGE);
      if (instance != null) {
         instance.setBaseValue((float)data.getDouble("spiritDamage"));
      }

      instance = player.getAttributeInstance(SpiritAttributes.CURRENT_SPIRIT);
      if (instance != null) {
         instance.setBaseValue((float)data.getDouble("currentSpirit"));
      }

      instance = player.getAttributeInstance(SpiritAttributes.MAX_SPIRIT);
      if (instance != null) {
         instance.setBaseValue((float)data.getDouble("maxSpirit"));
      }

      instance = player.getAttributeInstance(SpiritAttributes.REVIVAL_FACTOR);
      if (instance != null) {
         instance.setBaseValue((float)data.getDouble("revivalFactor"));
      }

      instance = player.getAttributeInstance(SpiritAttributes.SANITY);
      if (instance != null) {
         double sanityValue = data.contains("sanity") ? data.getDouble("sanity") : 100.0;
         instance.setBaseValue((float)sanityValue);
      }

      instance = player.getAttributeInstance(SpiritAttributes.MAX_SANITY);
      if (instance != null) {
         double maxSanityValue = data.contains("maxSanity") ? data.getDouble("maxSanity") : 100.0;
         instance.setBaseValue((float)maxSanityValue);
      }

      NbtCompound cachedData = getCachedData(player);
      cachedData.putDouble("spiritResistance", data.getDouble("spiritResistance"));
      cachedData.putDouble("spiritDamage", data.getDouble("spiritDamage"));
      cachedData.putDouble("currentSpirit", data.getDouble("currentSpirit"));
      cachedData.putDouble("maxSpirit", data.getDouble("maxSpirit"));
      cachedData.putDouble("revivalFactor", data.getDouble("revivalFactor"));
      double sanityValue = data.contains("sanity") ? data.getDouble("sanity") : 100.0;
      cachedData.putDouble("sanity", sanityValue);
      double maxSanityValue = data.contains("maxSanity") ? data.getDouble("maxSanity") : 100.0;
      cachedData.putDouble("maxSanity", maxSanityValue);
      if (data.contains("GhostSlots")) {
         cachedData.put("GhostSlots", data.getCompound("GhostSlots").copy());
      }

      if (data.contains("wangCurseUnlocked")) {
         cachedData.putBoolean("wangCurseUnlocked", data.getBoolean("wangCurseUnlocked"));
      }

      if (data.contains("servants")) {
         cachedData.put("servants", data.getList("servants", 11).copy());
      }

      PLAYER_DATA_CACHE.put(player.getUuid(), cachedData);
   }

   public static NbtCompound getCachedData(PlayerEntity player) {
      if (!PLAYER_DATA_CACHE.containsKey(player.getUuid())) {
         loadPlayerDataToCache(player);
      }

      return PLAYER_DATA_CACHE.get(player.getUuid());
   }

   public static void saveDataToPlayer(PlayerEntity player, NbtCompound spiritData) {
      PLAYER_DATA_CACHE.put(player.getUuid(), spiritData);
      if (player instanceof IPlayerData) {
         ((IPlayerData)player).setSpiritData(spiritData.copy());
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         sendSpiritDataToClient(serverPlayer);
      }
   }

   private static void copySpiritAttributes(PlayerEntity oldPlayer, PlayerEntity newPlayer) {
      NbtCompound oldData = getCachedData(oldPlayer);
      PLAYER_DATA_CACHE.put(newPlayer.getUuid(), oldData.copy());
      saveDataToPlayer(newPlayer, oldData.copy());
   }

   public static void initPlayerAttributes(PlayerEntity player) {
      setDefaultAttribute(player, SpiritAttributes.CURRENT_SPIRIT, 0.0);
      setDefaultAttribute(player, SpiritAttributes.MAX_SPIRIT, 0.0);
      setDefaultAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, 0.0);
      setDefaultAttribute(player, SpiritAttributes.TEMP_SPIRIT_RESISTANCE, 0.0);
      setDefaultAttribute(player, SpiritAttributes.TEMP_SPIRIT_RESISTANCE_MULTIPLIER, 1.0);
      setDefaultAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, 0.0);
      setDefaultAttribute(player, SpiritAttributes.TEMP_SPIRIT_DAMAGE, 0.0);
      setDefaultAttribute(player, SpiritAttributes.TEMP_SPIRIT_DAMAGE_MULTIPLIER, 1.0);
      setDefaultAttribute(player, SpiritAttributes.TEMP_MAX_SPIRIT, 0.0);
      setDefaultAttribute(player, SpiritAttributes.REVIVAL_FACTOR, 0.0);
      setDefaultAttribute(player, SpiritAttributes.SANITY, 100.0);
      setDefaultAttribute(player, SpiritAttributes.TEMP_SANITY, 0.0);
      setDefaultAttribute(player, SpiritAttributes.MAX_SANITY, 100.0);
      LOGGER.debug("已初始化玩家 {} 的灵异属性", player.getName().getString());
   }

   private static void setDefaultAttribute(PlayerEntity player, EntityAttribute attribute, double defaultValue) {
      EntityAttributeInstance instance = player.getAttributeInstance(attribute);
      if (instance != null) {
         instance.setBaseValue(defaultValue);
      }
   }

   public static float getSpiritAttribute(PlayerEntity player, EntityAttribute attribute) {
      EntityAttributeInstance instance = player.getAttributeInstance(attribute);
      return instance != null ? (float)instance.getBaseValue() : 0.0F;
   }

   public static void setSpiritAttribute(PlayerEntity player, EntityAttribute attribute, float value) {
      EntityAttributeInstance instance = player.getAttributeInstance(attribute);
      if (instance != null) {
         instance.setBaseValue(value);
      }

      NbtCompound data = getCachedData(player);
      if (attribute == SpiritAttributes.SPIRIT_RESISTANCE) {
         double currentValue = data.contains("spiritResistance") ? data.getDouble("spiritResistance") : 0.0;
         data.putDouble("spiritResistance", currentValue + value);
      } else if (attribute == SpiritAttributes.SPIRIT_DAMAGE) {
         double currentValue = data.contains("spiritDamage") ? data.getDouble("spiritDamage") : 0.0;
         data.putDouble("spiritDamage", currentValue + value);
      } else if (attribute == SpiritAttributes.CURRENT_SPIRIT) {
         double currentValue = data.contains("currentSpirit") ? data.getDouble("currentSpirit") : 0.0;
         data.putDouble("currentSpirit", currentValue + value);
      } else if (attribute == SpiritAttributes.MAX_SPIRIT) {
         double currentValue = data.contains("maxSpirit") ? data.getDouble("maxSpirit") : 0.0;
         data.putDouble("maxSpirit", currentValue + value);
      } else if (attribute == SpiritAttributes.REVIVAL_FACTOR) {
         double currentValue = data.contains("revivalFactor") ? data.getDouble("revivalFactor") : 0.0;
         data.putDouble("revivalFactor", currentValue + value);
      } else if (attribute == SpiritAttributes.SANITY) {
         double currentValue = data.contains("sanity") ? data.getDouble("sanity") : 100.0;
         data.putDouble("sanity", currentValue + value);
      } else if (attribute == SpiritAttributes.MAX_SANITY) {
         double currentValue = data.contains("maxSanity") ? data.getDouble("maxSanity") : 100.0;
         data.putDouble("maxSanity", currentValue + value);
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_RESISTANCE) {
         data.putDouble("tempSpiritResistance", value);
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_RESISTANCE_MULTIPLIER) {
         data.putDouble("tempSpiritResistanceMultiplier", value);
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_DAMAGE) {
         data.putDouble("tempSpiritDamage", value);
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_DAMAGE_MULTIPLIER) {
         data.putDouble("tempSpiritDamageMultiplier", value);
      } else if (attribute == SpiritAttributes.TEMP_MAX_SPIRIT) {
         data.putDouble("tempMaxSpirit", value);
      } else if (attribute == SpiritAttributes.TEMP_SANITY) {
         data.putDouble("tempSanity", value);
      }

      PLAYER_DATA_CACHE.put(player.getUuid(), data);
      setSpiritAttributes(player, data);
   }

   public static void subtractSpiritAttributeValue(PlayerEntity player, EntityAttribute attribute, float value) {
      EntityAttributeInstance instance = player.getAttributeInstance(attribute);
      if (instance != null) {
         float currentValue = (float)instance.getBaseValue();
         instance.setBaseValue(Math.max(0.0F, currentValue - value));
      }

      NbtCompound data = getCachedData(player);
      if (attribute == SpiritAttributes.SPIRIT_RESISTANCE) {
         double currentValue = data.contains("spiritResistance") ? data.getDouble("spiritResistance") : 0.0;
         data.putDouble("spiritResistance", Math.max(0.0, currentValue - value));
      } else if (attribute == SpiritAttributes.SPIRIT_DAMAGE) {
         double currentValue = data.contains("spiritDamage") ? data.getDouble("spiritDamage") : 0.0;
         data.putDouble("spiritDamage", Math.max(0.0, currentValue - value));
      } else if (attribute == SpiritAttributes.CURRENT_SPIRIT) {
         double currentValue = data.contains("currentSpirit") ? data.getDouble("currentSpirit") : 0.0;
         data.putDouble("currentSpirit", Math.max(0.0, currentValue - value));
      } else if (attribute == SpiritAttributes.MAX_SPIRIT) {
         double currentValue = data.contains("maxSpirit") ? data.getDouble("maxSpirit") : 0.0;
         data.putDouble("maxSpirit", Math.max(0.0, currentValue - value));
      } else if (attribute == SpiritAttributes.MAX_SANITY) {
         double currentValue = data.contains("maxSanity") ? data.getDouble("maxSanity") : 100.0;
         data.putDouble("maxSanity", Math.max(0.0, currentValue - value));
      } else if (attribute == SpiritAttributes.REVIVAL_FACTOR) {
         double currentValue = data.contains("revivalFactor") ? data.getDouble("revivalFactor") : 0.0;
         data.putDouble("revivalFactor", Math.max(0.0, currentValue - value));
      } else if (attribute == SpiritAttributes.SANITY) {
         double currentValue = data.contains("sanity") ? data.getDouble("sanity") : 100.0;
         data.putDouble("sanity", Math.max(0.0, currentValue - value));
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_RESISTANCE) {
         data.putDouble("tempSpiritResistance", 0.0);
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_RESISTANCE_MULTIPLIER) {
         data.putDouble("tempSpiritResistanceMultiplier", 1.0);
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_DAMAGE) {
         data.putDouble("tempSpiritDamage", 0.0);
      } else if (attribute == SpiritAttributes.TEMP_SPIRIT_DAMAGE_MULTIPLIER) {
         data.putDouble("tempSpiritDamageMultiplier", 1.0);
      } else if (attribute == SpiritAttributes.TEMP_MAX_SPIRIT) {
         data.putDouble("tempMaxSpirit", 0.0);
      } else if (attribute == SpiritAttributes.TEMP_SANITY) {
         data.putDouble("tempSanity", 0.0);
      }

      PLAYER_DATA_CACHE.put(player.getUuid(), data);
      setSpiritAttributes(player, data);
   }

   public static void addSpiritAttribute(PlayerEntity player, EntityAttribute attribute, float amount) {
      float current = getSpiritAttribute(player, attribute);
      setSpiritAttribute(player, attribute, current + amount);
   }

   public static void subtractSpiritAttribute(PlayerEntity player, EntityAttribute attribute, float amount) {
      float current = getSpiritAttribute(player, attribute);
      setSpiritAttribute(player, attribute, Math.max(0.0F, current - amount));
   }

   public static void addSpiritDamage(PlayerEntity player, float amount) {
      addSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, amount);
   }

   public static void subtractSpiritDamage(PlayerEntity player, float amount) {
      subtractSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, amount);
   }

   public static float getCurrentSpirit(PlayerEntity player) {
      return getSpiritAttribute(player, SpiritAttributes.CURRENT_SPIRIT);
   }

   public static float getMaxSpirit(PlayerEntity player) {
      NbtCompound spiritAttributes = getSpiritAttributes(player);
      if (spiritAttributes.contains("maxSpirit")) {
         if (spiritAttributes.contains("maxSpirit", 3)) {
            return spiritAttributes.getInt("maxSpirit");
         } else {
            return spiritAttributes.contains("maxSpirit", 6) ? (float)spiritAttributes.getDouble("maxSpirit") : spiritAttributes.getFloat("maxSpirit");
         }
      } else {
         return 0.0F;
      }
   }

   public static float getMaxSanity(PlayerEntity player) {
      NbtCompound spiritAttributes = getSpiritAttributes(player);
      if (spiritAttributes.contains("maxSanity")) {
         if (spiritAttributes.contains("maxSanity", 3)) {
            return spiritAttributes.getInt("maxSanity");
         } else {
            return spiritAttributes.contains("maxSanity", 6) ? (float)spiritAttributes.getDouble("maxSanity") : spiritAttributes.getFloat("maxSanity");
         }
      } else {
         return 100.0F;
      }
   }

   public static long getSurvivalTime(PlayerEntity player) {
      return getCachedData(player).getLong("survivalTime");
   }

   public static long getTotalTime(PlayerEntity player) {
      return getCachedData(player).getLong("totalTime");
   }

   public static void saveLastSurvivalTime(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      data.putLong("lastSurvivalTime", data.getLong("survivalTime"));
   }

   public static void resetSurvivalTime(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      data.putLong("survivalTime", 0L);
   }

   public static void restoreSurvivalTime(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      long lastSurvivalTime = data.getLong("lastSurvivalTime");
      data.putLong("survivalTime", lastSurvivalTime);
   }

   public static void setCurrentSpirit(PlayerEntity player, float value) {
      float maxSpirit = getMaxSpirit(player);
      float finalValue = Math.min(value, maxSpirit);
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.CURRENT_SPIRIT);
      if (instance != null) {
         instance.setBaseValue(finalValue);
      }

      NbtCompound data = getCachedData(player);
      data.putDouble("currentSpirit", finalValue);
      PLAYER_DATA_CACHE.put(player.getUuid(), data);
      setSpiritAttributes(player, data);
   }

   public static void setCurrentSanity(PlayerEntity player, float value) {
      float maxSanity = getMaxSanity(player);
      float finalValue = Math.min(value, maxSanity);
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.SANITY);
      if (instance != null) {
         instance.setBaseValue(finalValue);
      }

      NbtCompound data = getCachedData(player);
      data.putDouble("sanity", finalValue);
      PLAYER_DATA_CACHE.put(player.getUuid(), data);
      setSpiritAttributes(player, data);
   }

   public static void setMaxSpirit(PlayerEntity player, float value) {
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.MAX_SPIRIT);
      if (instance != null) {
         instance.setBaseValue(value);
      }

      NbtCompound data = getCachedData(player);
      data.putDouble("maxSpirit", value);
      PLAYER_DATA_CACHE.put(player.getUuid(), data);
      float current = getCurrentSpirit(player);
      if (current > value) {
         setCurrentSpirit(player, value);
      }

      setSpiritAttributes(player, data);
   }

   public static NbtCompound getSpiritAttributes(PlayerEntity player) {
      return getCachedData(player).copy();
   }

   public static void setSpiritAttributes(PlayerEntity player, NbtCompound data) {
      PLAYER_DATA_CACHE.put(player.getUuid(), data.copy());
      if (player instanceof ServerPlayerEntity serverPlayer) {
         sendSpiritDataToClient(serverPlayer);
      }

      saveDataToPlayer(player, data);
      LOGGER.debug("玩家 {} 的灵异属性已更新到缓存并同步到客户端", player.getName().getString());
   }

   private static void sendSpiritDataToClient(ServerPlayerEntity player) {
      NbtCompound data = PLAYER_DATA_CACHE.get(player.getUuid());
      if (data != null) {
         SpiritNetworkHandler.syncSpiritData(player);
      }
   }

   public static void initSpiritData(PlayerEntity player) {
      if (!PLAYER_DATA_CACHE.containsKey(player.getUuid())) {
         PLAYER_DATA_CACHE.put(player.getUuid(), createDefaultSpiritData());
      }
   }

   public static int findEquippedGhostEyeSlot(PlayerEntity player) {
      return findEquippedItemSlot(player, SilentGhostEyeItem.class);
   }

   public static int getGhostSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (!slotData.getBoolean("occupied")) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int getUnlockedGhostSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            boolean unlocked = slotData.getBoolean("unlocked");
            boolean occupied = slotData.getBoolean("occupied");
            if (unlocked && !occupied) {
               return i;
            }
         }
      }

      return -1;
   }

   public static void setGhostSlotData(PlayerEntity player, int slotIndex, ItemStack itemStack) {
      if (slotIndex >= 0 && slotIndex < 10) {
         NbtCompound data = getCachedData(player);
         if (!data.contains("GhostSlots")) {
            NbtCompound defaultData = createDefaultSpiritData();

            for (String key : defaultData.getKeys()) {
               if (!data.contains(key)) {
                  data.put(key, defaultData.get(key).copy());
               }
            }

            PLAYER_DATA_CACHE.put(player.getUuid(), data);
         }

         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         String slotKey = "Slot" + slotIndex;
         boolean isUnlocked = true;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound existingSlotData = ghostSlots.getCompound(slotKey);
            isUnlocked = existingSlotData.getBoolean("unlocked");
         } else {
            isUnlocked = slotIndex != 0 && slotIndex != 3 && slotIndex < 6;
         }

         if (isUnlocked) {
            NbtCompound slotData = new NbtCompound();
            slotData.putBoolean("occupied", true);
            slotData.put("item", itemStack.writeNbt(new NbtCompound()));
            int appliedLevel = 1;
            int appliedRevivalDegree = 0;
            NbtCompound itemNbt = itemStack.getNbt();
            if (itemNbt != null) {
               if (itemNbt.contains("StoredLevel")) {
                  appliedLevel = itemNbt.getInt("StoredLevel");
               }

               if (itemNbt.contains("StoredRevivalDegree")) {
                  appliedRevivalDegree = itemNbt.getInt("StoredRevivalDegree");
               }
            }

            if (isGhostChildFused(player)) {
               appliedLevel = Math.max(appliedLevel, 10);
            }

            slotData.putInt("level", appliedLevel);
            slotData.putInt("revivalDegree", appliedRevivalDegree);
            slotData.putInt("requiredRevivalDegree", 1000);
            slotData.putBoolean("unlocked", isUnlocked);
            slotData.putBoolean("slotDeadlocked", false);
            ghostSlots.put(slotKey, slotData);
            data.put("GhostSlots", ghostSlots);
            if (itemStack.getItem() instanceof BaseGhostEyeItem ghostEyeItem) {
               float levelBonusMultiplier;
               if (appliedLevel < 10) {
                  levelBonusMultiplier = 0.5F + (appliedLevel - 1) * 0.05F;
               } else {
                  levelBonusMultiplier = 1.0F;
               }

               addSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, ghostEyeItem.getSpiritResistanceBonus() * levelBonusMultiplier);
               addSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, ghostEyeItem.getSpiritDamageBonus() * levelBonusMultiplier);
               addSpiritAttribute(player, SpiritAttributes.MAX_SPIRIT, ghostEyeItem.getMaxSpiritBonus() * levelBonusMultiplier);
               addSpiritAttribute(player, SpiritAttributes.REVIVAL_FACTOR, (float)(ghostEyeItem.getRevivalFactor() * levelBonusMultiplier));
               addSpiritAttribute(player, SpiritAttributes.MAX_SANITY, ghostEyeItem.getSanityBonus() * levelBonusMultiplier);
            }

            setSpiritAttributes(player, data);
            if (player instanceof ServerPlayerEntity serverPlayer) {
               AdvancementManager.checkAndUnlockTameGhost(serverPlayer);
            }
         }
      }
   }

   public static ItemStack getGhostSlotItem(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      String slotKey = "Slot" + slotIndex;
      if (ghostSlots.contains(slotKey)) {
         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         if (slotData.getBoolean("occupied") && slotData.contains("item")) {
            return ItemStack.fromNbt(slotData.getCompound("item"));
         }
      }

      return ItemStack.EMPTY;
   }

   public static boolean isGhostSlotOccupied(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      String slotKey = "Slot" + slotIndex;
      if (ghostSlots.contains(slotKey)) {
         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         return slotData.getBoolean("occupied");
      } else {
         return false;
      }
   }

   public static String getGhostTypeInSlot(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      String slotKey = "Slot" + slotIndex;
      if (ghostSlots.contains(slotKey)) {
         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         if (slotData.getBoolean("occupied") && slotData.contains("item")) {
            ItemStack itemStack = ItemStack.fromNbt(slotData.getCompound("item"));

            try {
               TameableItemAPI api = TameableItemAPI.getInstance();
               String ghostType = api.getGhostType(itemStack);
               if (ghostType != null && !ghostType.isEmpty()) {
                  return ghostType;
               }
            } catch (Exception e) {
               LOGGER.warn("使用TameableItemAPI获取鬼类型失败，将回退到硬编码检查: {}", e.getMessage());
            }

            if (itemStack.getItem() == ModItems.GHOST_FIRE) {
               return "ghost_fire";
            }

            if (itemStack.getItem() == ModItems.FOG_GHOST) {
               return "fog_ghost";
            }

            if (itemStack.getItem() == ModItems.BLOCK_GHOST) {
               return "block_ghost";
            }

            if (itemStack.getItem() == ModItems.FOOD_GHOST) {
               return "food_ghost";
            }

            if (itemStack.getItem() == ModItems.QIAOMEN_GHOST) {
               return "qiaomen_ghost";
            }

            if (itemStack.getItem() == ModItems.VILLAGER_GHOST) {
               return "villager_ghost";
            }

            if (itemStack.getItem() == ModItems.SILENT_GHOST_EYE) {
               return "silent_ghost_eye";
            }

            if (itemStack.getItem() == ModItems.TAITOU_GHOST) {
               return "taitou_ghost";
            }

            if (itemStack.getItem() == ModItems.DITOU_GHOST) {
               return "ditou_ghost";
            }

            if (itemStack.getItem() == ModItems.BOX_GHOST) {
               return "box_ghost";
            }

            if (itemStack.getItem() == ModItems.JUMP_GHOST) {
               return "jump_ghost";
            }

            if (itemStack.getItem() == ModItems.UNTOUCHABLE_GHOST) {
               return "untouchable_ghost";
            }

            if (itemStack.getItem() == ModItems.DEATH_SIGHT_GHOST) {
               return "death_sight_ghost";
            }

            if (itemStack.getItem() == ModItems.GHOST_MERCHANT) {
               return "ghost_merchant";
            }

            if (itemStack.getItem() == ModItems.LOST_GHOST) {
               return "lost_ghost";
            }

            if (itemStack.getItem() == ModItems.GHOST_FIST) {
               return "ghost_fist";
            }

            if (itemStack.getItem() == ModItems.GHOST_BLOOD) {
               return "ghost_blood";
            }

            if (itemStack.getItem() == ModItems.GIANT_SHADOW_GHOST) {
               return "giant_shadow_ghost";
            }

            if (itemStack.getItem() instanceof BaseGhostEyeItem) {
               return itemStack.getItem().toString().toLowerCase();
            }
         }
      }

      return null;
   }

   public static boolean hasGhostType(PlayerEntity player, String ghostType) {
      if (ghostType != null && !ghostType.isEmpty()) {
         for (int i = 0; i < 10; i++) {
            String currentGhostType = getGhostTypeInSlot(player, i);
            if (ghostType.equals(currentGhostType)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean hasOccupiedGhostSlot(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (isGhostSlotOccupied(player, i)) {
            return true;
         }
      }

      return false;
   }

   public static int countOccupiedGhostSlots(PlayerEntity player) {
      int count = 0;

      for (int i = 0; i < 10; i++) {
         if (isGhostSlotOccupied(player, i)) {
            count++;
         }
      }

      return count;
   }

   public static boolean isGhostChildFused(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         return AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_god");
      } else {
         try {
            NbtCompound data = getCachedData(player);
            if (data != null && data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               if (ghostSlots.contains("Slot6")) {
                  return ghostSlots.getCompound("Slot6").getBoolean("unlocked");
               }
            }
         } catch (Exception var3) {
         }

         return false;
      }
   }

   private static void handleGiantShadowGhostSneakEffect(PlayerEntity player) {
      player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20, 55, false, false));
      player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 20, 0, false, false));
      if (player instanceof ServerPlayerEntity serverPlayer) {
         GiantShadowGhostScaleS2CPacket.sendToClient(serverPlayer);
      }
   }

   private static void formDefenseFormationOnSneak(PlayerEntity player) {
      List<PlayerGhostEntity> nearbyPlayerGhosts = player.getWorld()
         .getEntitiesByClass(
            PlayerGhostEntity.class,
            player.getBoundingBox().expand(20.0),
            ghostx -> ghostx instanceof PlayerGhostEntity
               && ghostx.isAlive()
               && !ghostx.isDeadlocked()
               && ghostx.isServantMode()
               && ghostx.getMasterUuid() != null
               && ghostx.getMasterUuid().equals(player.getUuid())
         );
      List<GhostEntity> playerGhosts = new ArrayList<>();

      for (PlayerGhostEntity ghost : nearbyPlayerGhosts) {
         playerGhosts.add(ghost);
      }

      if (!playerGhosts.isEmpty()) {
         formDefenseFormation(player, playerGhosts);
      }
   }

   private static void formDefenseFormation(PlayerEntity player, List<GhostEntity> ghosts) {
      int ghostCount = ghosts.size();
      double radius = 1.0;
      double playerYaw = player.getYaw() * Math.PI / 180.0;

      for (int i = 0; i < ghostCount; i++) {
         double angle;
         if (ghostCount == 1) {
            angle = playerYaw;
         } else if (ghostCount == 2) {
            angle = playerYaw + Math.PI * i;
         } else if (ghostCount == 3) {
            angle = playerYaw + (Math.PI * 2) * i / 3.0;
         } else if (ghostCount == 4) {
            angle = playerYaw + (Math.PI * 2) * i / 4.0;
         } else {
            angle = playerYaw + (Math.PI * 2) * i / ghostCount;
         }

         double x = player.getX() + radius * Math.cos(angle);
         double z = player.getZ() + radius * Math.sin(angle);
         double y = player.getY();
         GhostEntity ghost = ghosts.get(i);
         ghost.teleport(x, y, z);
      }
   }

   public static int getGhostSlotLevel(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      String slotKey = "Slot" + slotIndex;
      if (ghostSlots.contains(slotKey)) {
         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         return slotData.getInt("level");
      } else {
         return 1;
      }
   }

   public static int getGhostSlotRevivalDegree(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      String slotKey = "Slot" + slotIndex;
      if (ghostSlots.contains(slotKey)) {
         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         return slotData.getInt("revivalDegree");
      } else {
         return 0;
      }
   }

   public static int getGhostSlotRequiredRevivalDegree(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      String slotKey = "Slot" + slotIndex;
      if (ghostSlots.contains(slotKey)) {
         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         return slotData.getInt("requiredRevivalDegree");
      } else {
         return 1000;
      }
   }

   public static boolean isSlotDeadlocked(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         return false;
      } else {
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         String slotKey = "Slot" + slotIndex;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            return slotData.getBoolean("slotDeadlocked");
         } else {
            return false;
         }
      }
   }

   public static void setSlotDeadlocked(PlayerEntity player, int slotIndex, boolean deadlocked) {
      NbtCompound data = getCachedData(player);
      if (data.contains("GhostSlots")) {
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         String slotKey = "Slot" + slotIndex;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            slotData.putBoolean("slotDeadlocked", deadlocked);
            ghostSlots.put(slotKey, slotData);
            data.put("GhostSlots", ghostSlots);
            setSpiritAttributes(player, data);
         }
      }
   }

   public static NbtCompound getGhostSlots(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (!slotData.contains("unlocked")) {
               slotData.putBoolean("unlocked", i != 0 && i != 3 && i < 6);
            }

            if (!slotData.contains("slotDeadlocked")) {
               slotData.putBoolean("slotDeadlocked", false);
            }
         } else {
            NbtCompound slotData = new NbtCompound();
            slotData.putBoolean("occupied", false);
            slotData.put("item", ItemStack.EMPTY.writeNbt(new NbtCompound()));
            slotData.putInt("level", 1);
            slotData.putInt("revivalDegree", 0);
            slotData.putInt("requiredRevivalDegree", 1000);
            slotData.putBoolean("unlocked", i != 0 && i != 3 && i < 6);
            slotData.putBoolean("slotDeadlocked", false);
            ghostSlots.put(slotKey, slotData);
         }
      }

      data.put("GhostSlots", ghostSlots);
      PLAYER_DATA_CACHE.put(player.getUuid(), data);
      return ghostSlots;
   }

   public static int getAvailableTamingSlot(PlayerEntity player) {
      NbtCompound ghostSlots = getGhostSlots(player);

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (!slotData.getBoolean("occupied")) {
               return i;
            }
         }
      }

      return -1;
   }

   public static boolean addTamedGhostToSlot(PlayerEntity player, NbtCompound ghostData, int slotIndex) {
      if (slotIndex >= 0 && slotIndex < 10) {
         NbtCompound data = getCachedData(player);
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         String slotKey = "Slot" + slotIndex;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               return false;
            }
         }

         ItemStack tamedGhostStack = new ItemStack(Items.PAPER);
         NbtCompound itemNbt = new NbtCompound();
         itemNbt.put("TamedGhost", ghostData.copy());
         tamedGhostStack.setNbt(itemNbt);
         if (ghostData.contains("CustomName")) {
            tamedGhostStack.setCustomName(Serializer.fromJson(ghostData.getString("CustomName")));
         } else {
            tamedGhostStack.setCustomName(Text.translatable("item.smfs.tamed_ghost"));
         }

         NbtCompound slotData = new NbtCompound();
         slotData.putBoolean("occupied", true);
         slotData.put("item", tamedGhostStack.writeNbt(new NbtCompound()));
         int initialLevel = isGhostChildFused(player) ? 10 : 1;
         slotData.putInt("level", initialLevel);
         slotData.putInt("revivalDegree", 0);
         slotData.putInt("requiredRevivalDegree", 1000);
         if (ghostData.contains("Type")) {
            slotData.putString("ghostType", ghostData.getString("Type"));
         }

         ghostSlots.put(slotKey, slotData);
         data.put("GhostSlots", ghostSlots);
         setSpiritAttributes(player, data);
         LOGGER.info("玩家 {} 成功驾驭厉鬼到槽位 {}", player.getName().getString(), slotIndex + 1);
         if (countOccupiedGhostSlots(player) == 1) {
            MainGhostManager.setMainGhostSlot(player, slotIndex);
         }

         showGhostAbilityPopup(player, ghostData);
         return true;
      } else {
         return false;
      }
   }

   public static void showGhostAbilityPopup(PlayerEntity player, NbtCompound ghostData) {
      if (!player.getWorld().isClient()) {
         try {
            if (ghostData == null) {
               System.err.println("[SMFS] showGhostAbilityPopup: ghostData is null");
               return;
            }

            if (!ghostData.contains("Type")) {
               System.err.println("[SMFS] showGhostAbilityPopup: ghostData missing 'Type' field");
               return;
            }

            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeNbt(ghostData);
            ServerPlayNetworking.send((ServerPlayerEntity)player, GhostAbilityPopupS2CPacket.ID, buf);
         } catch (Exception e) {
            System.err.println("[SMFS] showGhostAbilityPopup: Error sending packet: " + e.getMessage());
            e.printStackTrace();
         }
      }
   }

   public static boolean canTameGhost(PlayerEntity player, ItemStack containerStack) {
      return !GoldenContainerItem.hasGhost(containerStack) ? false : getAvailableTamingSlot(player) >= 0;
   }

   public static int findEquippedItemSlot(PlayerEntity player, Class<? extends Item> itemClass) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (itemClass.isInstance(item.getItem())) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedGhostFireSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.GHOST_FIRE) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedThickFogSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.FOG_GHOST) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedBlockGhostSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.BLOCK_GHOST) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedFoodGhostSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.FOOD_GHOST) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedQiaomenGhostSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.QIAOMEN_GHOST) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedVillagerGhostSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.VILLAGER_GHOST) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedGhostWindSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.GHOST_WIND) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedGhostOfficerSlot(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               ItemStack item = ItemStack.fromNbt(slotData.getCompound("item"));
               if (item.getItem() == ModItems.GHOST_OFFICER) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findEquippedGhostSmokeSlot(PlayerEntity player) {
      return findEquippedItemSlot(player, GhostSmokeItem.class);
   }

   public static void increaseRevivalDegreeDaily(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (data.contains("GhostSlots")) {
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         boolean anyIncreased = false;

         for (int i = 0; i < 10; i++) {
            String slotKey = "Slot" + i;
            if (ghostSlots.contains(slotKey)) {
               NbtCompound slotData = ghostSlots.getCompound(slotKey);
               if (slotData.getBoolean("occupied")) {
                  int currentDegree = slotData.getInt("revivalDegree");
                  int requiredDegree = slotData.getInt("requiredRevivalDegree");
                  if (requiredDegree != 1000) {
                     updateGhostSlotValue(player, i, "requiredRevivalDegree", 1000);
                     requiredDegree = 1000;
                  }

                  int newDegree = Math.min(currentDegree + 50, requiredDegree);
                  updateGhostSlotValue(player, i, "revivalDegree", newDegree);
                  anyIncreased = true;
                  int var10 = Math.min((int)Math.floor((double)newDegree / requiredDegree * 10.0) + 1, 10);
               }
            }
         }

         if (anyIncreased) {
            player.sendMessage(Text.translatable("message.smfs.revival_increase_daily"), true);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, false, true));
            if (player instanceof ServerPlayerEntity serverPlayer) {
               ScreenEffectS2CPacket.sendTextHallucination(serverPlayer);
            }
         }

         GhostDomainManager.checkRevivalDegree(player);
      }
   }

   public static void updateGhostSlotValue(PlayerEntity player, int slotIndex, String valueKey, int newValue) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");
      String slotKey = "Slot" + slotIndex;
      if (ghostSlots.contains(slotKey)) {
         NbtCompound slotData = ghostSlots.getCompound(slotKey);
         if ("revivalDegree".equals(valueKey)) {
            int currentDegree = slotData.getInt("revivalDegree");
            int currentLevel = slotData.getInt("level");
            slotData.putInt(valueKey, newValue);
            int requiredDegree = slotData.getInt("requiredRevivalDegree");
            int newRevivalLevel = Math.min((int)Math.floor((double)newValue / requiredDegree * 10.0) + 1, 10);
            if (newRevivalLevel > currentLevel) {
               slotData.putInt("level", newRevivalLevel);
               recalculateSpiritAttributesForSlot(player, slotIndex, currentLevel, newRevivalLevel);
            }
         } else {
            slotData.putInt(valueKey, newValue);
         }

         ghostSlots.put(slotKey, slotData);
         data.put("GhostSlots", ghostSlots);
         setSpiritAttributes(player, data);
      }
   }

   public static void updateRevivalDegreeInGhostDomain(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         int targetSlotIndex = -1;
         if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)) {
            targetSlotIndex = findEquippedGhostEyeSlot(player);
            if (targetSlotIndex != -1) {
               int currentDegree = getGhostSlotRevivalDegree(player, targetSlotIndex);
               int requiredDegree = getGhostSlotRequiredRevivalDegree(player, targetSlotIndex);
               int newDegree = Math.min(currentDegree + 2, requiredDegree);
               updateGhostSlotValue(player, targetSlotIndex, "revivalDegree", newDegree);
            }
         }

         if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)) {
            targetSlotIndex = findEquippedGhostFireSlot(player);
            if (targetSlotIndex != -1) {
               int currentDegree = getGhostSlotRevivalDegree(player, targetSlotIndex);
               int requiredDegree = getGhostSlotRequiredRevivalDegree(player, targetSlotIndex);
               int newDegree = Math.min(currentDegree + 2, requiredDegree);
               updateGhostSlotValue(player, targetSlotIndex, "revivalDegree", newDegree);
            }
         }

         if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)) {
            GoldenGhostDomainEffect.updateRevivalDegreeInGhostDomain(player);
            return;
         }

         if (player.hasStatusEffect(ModEffects.THICK_FOG)) {
            targetSlotIndex = findEquippedThickFogSlot(player);
            if (targetSlotIndex != -1) {
               int currentDegree = getGhostSlotRevivalDegree(player, targetSlotIndex);
               int requiredDegree = getGhostSlotRequiredRevivalDegree(player, targetSlotIndex);
               int newDegree = Math.min(currentDegree + 2, requiredDegree);
               updateGhostSlotValue(player, targetSlotIndex, "revivalDegree", newDegree);
            }
         }

         if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)) {
            if (MainGhostManager.isMainGhostType(player, BlockGhostItem.class)) {
               targetSlotIndex = findEquippedBlockGhostSlot(player);
            } else if (MainGhostManager.isMainGhostType(player, FoodGhostItem.class)) {
               targetSlotIndex = findEquippedFoodGhostSlot(player);
            } else if (MainGhostManager.isMainGhostType(player, QiaomenGhostItem.class)) {
               targetSlotIndex = findEquippedQiaomenGhostSlot(player);
            } else if (MainGhostManager.isMainGhostType(player, VillagerGhostItem.class)) {
               targetSlotIndex = findEquippedVillagerGhostSlot(player);
            } else if (MainGhostManager.isMainGhostType(player, GhostOfficerItem.class)) {
               targetSlotIndex = findEquippedGhostOfficerSlot(player);
            } else {
               targetSlotIndex = findEquippedBlockGhostSlot(player);
               if (targetSlotIndex == -1) {
                  targetSlotIndex = findEquippedFoodGhostSlot(player);
               }

               if (targetSlotIndex == -1) {
                  targetSlotIndex = findEquippedQiaomenGhostSlot(player);
               }

               if (targetSlotIndex == -1) {
                  targetSlotIndex = findEquippedVillagerGhostSlot(player);
               }

               if (targetSlotIndex == -1) {
                  targetSlotIndex = findEquippedGhostOfficerSlot(player);
               }
            }

            if (targetSlotIndex != -1) {
               int currentDegree = getGhostSlotRevivalDegree(player, targetSlotIndex);
               int requiredDegree = getGhostSlotRequiredRevivalDegree(player, targetSlotIndex);
               int newDegree = Math.min(currentDegree + 2, requiredDegree);
               updateGhostSlotValue(player, targetSlotIndex, "revivalDegree", newDegree);
            }
         }

         if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)) {
            targetSlotIndex = findEquippedGhostWindSlot(player);
            if (targetSlotIndex != -1) {
               int currentDegree = getGhostSlotRevivalDegree(player, targetSlotIndex);
               int requiredDegree = getGhostSlotRequiredRevivalDegree(player, targetSlotIndex);
               int newDegree = Math.min(currentDegree + 2, requiredDegree);
               updateGhostSlotValue(player, targetSlotIndex, "revivalDegree", newDegree);
            }
         }

         if (player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN)) {
            targetSlotIndex = findEquippedGhostSmokeSlot(player);
            if (targetSlotIndex != -1) {
               int currentDegree = getGhostSlotRevivalDegree(player, targetSlotIndex);
               int requiredDegree = getGhostSlotRequiredRevivalDegree(player, targetSlotIndex);
               int newDegree = Math.min(currentDegree + 2, requiredDegree);
               updateGhostSlotValue(player, targetSlotIndex, "revivalDegree", newDegree);
            }
         }

         if (targetSlotIndex != -1) {
            for (int i = 0; i < 10; i++) {
               if (i != targetSlotIndex) {
                  int otherDegree = getGhostSlotRevivalDegree(player, i);
                  if (otherDegree > 0) {
                     int updatedOtherDegree = Math.max(otherDegree - 1, 0);
                     updateGhostSlotValue(player, i, "revivalDegree", updatedOtherDegree);
                  }
               }
            }
         }
      }

      GhostDomainManager.checkRevivalDegree(player);
   }

   public static void recalculateSpiritAttributesForSlot(PlayerEntity player, int slotIndex, int oldLevel, int newLevel) {
      NbtCompound data = getCachedData(player);
      if (data.contains("GhostSlots")) {
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         String slotKey = "Slot" + slotIndex;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied") && slotData.contains("item")) {
               ItemStack itemStack = ItemStack.fromNbt(slotData.getCompound("item"));
               if (itemStack.getItem() instanceof BaseGhostEyeItem ghostEyeItem) {
                  float oldLevelBonusMultiplier;
                  if (oldLevel < 10) {
                     oldLevelBonusMultiplier = 0.5F + (oldLevel - 1) * 0.05F;
                  } else {
                     oldLevelBonusMultiplier = 1.0F;
                  }

                  float newLevelBonusMultiplier;
                  if (newLevel < 10) {
                     newLevelBonusMultiplier = 0.5F + (newLevel - 1) * 0.05F;
                  } else {
                     newLevelBonusMultiplier = 1.0F;
                  }

                  if (oldLevel != newLevel) {
                     float oldResistance = getSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE);
                     float oldDamage = getSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE);
                     float oldMaxSpirit = getMaxSpirit(player);
                     float oldRevivalFactor = getSpiritAttribute(player, SpiritAttributes.REVIVAL_FACTOR);
                     float oldMaxSanity = getMaxSanity(player);
                     float resistanceDiff = ghostEyeItem.getSpiritResistanceBonus() * (newLevelBonusMultiplier - oldLevelBonusMultiplier);
                     float damageDiff = ghostEyeItem.getSpiritDamageBonus() * (newLevelBonusMultiplier - oldLevelBonusMultiplier);
                     float maxSpiritDiff = ghostEyeItem.getMaxSpiritBonus() * (newLevelBonusMultiplier - oldLevelBonusMultiplier);
                     float revivalFactorDiff = (float)(ghostEyeItem.getRevivalFactor() * (newLevelBonusMultiplier - oldLevelBonusMultiplier));
                     float maxSanityDiff = ghostEyeItem.getSanityBonus() * (newLevelBonusMultiplier - oldLevelBonusMultiplier);
                     addSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, resistanceDiff);
                     addSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, damageDiff);
                     addSpiritAttribute(player, SpiritAttributes.MAX_SPIRIT, maxSpiritDiff);
                     addSpiritAttribute(player, SpiritAttributes.REVIVAL_FACTOR, revivalFactorDiff);
                     addSpiritAttribute(player, SpiritAttributes.MAX_SANITY, maxSanityDiff);
                     float newResistance = getSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE);
                     float newDamage = getSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE);
                     float newMaxSpirit = getMaxSpirit(player);
                     float newRevivalFactor = getSpiritAttribute(player, SpiritAttributes.REVIVAL_FACTOR);
                     float newMaxSanity = getMaxSanity(player);
                     LOGGER.debug(
                        "玩家 {} 的槽位 {} 等级从 {} 提升至 {}，属性加成比例从 {}% 更新为: {}%",
                        player.getName().getString(),
                        slotIndex + 1,
                        oldLevel,
                        newLevel,
                        oldLevelBonusMultiplier * 100.0F,
                        newLevelBonusMultiplier * 100.0F
                     );
                     LOGGER.debug("属性变化 - 旧: 抗性={}, 伤害={}, 最大灵异={}, 复苏因子={}, 最大理智={}", oldResistance, oldDamage, oldMaxSpirit, oldRevivalFactor, oldMaxSanity);
                     LOGGER.debug("属性变化 - 新: 抗性={}, 伤害={}, 最大灵异={}, 复苏因子={}, 最大理智={}", newResistance, newDamage, newMaxSpirit, newRevivalFactor, newMaxSanity);
                  }
               }
            }
         }
      }
   }

   public static void clearGhostSlot(PlayerEntity player, int slotIndex) {
      NbtCompound data = getCachedData(player);
      if (data.contains("GhostSlots")) {
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         String slotKey = "Slot" + slotIndex;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied") && slotData.contains("item")) {
               ItemStack itemStack = ItemStack.fromNbt(slotData.getCompound("item"));
               if (itemStack.getItem() instanceof BaseGhostEyeItem) {
                  BaseGhostEyeItem ghostEyeItem = (BaseGhostEyeItem)itemStack.getItem();
                  int currentLevel = slotData.getInt("level");
                  float levelBonusMultiplier;
                  if (currentLevel < 10) {
                     levelBonusMultiplier = 0.5F + (currentLevel - 1) * 0.05F;
                  } else {
                     levelBonusMultiplier = 1.0F;
                  }

                  subtractSpiritAttributeValue(player, SpiritAttributes.SPIRIT_RESISTANCE, ghostEyeItem.getSpiritResistanceBonus() * levelBonusMultiplier);
                  subtractSpiritAttributeValue(player, SpiritAttributes.SPIRIT_DAMAGE, ghostEyeItem.getSpiritDamageBonus() * levelBonusMultiplier);
                  subtractSpiritAttributeValue(player, SpiritAttributes.MAX_SPIRIT, ghostEyeItem.getMaxSpiritBonus() * levelBonusMultiplier);
                  subtractSpiritAttributeValue(player, SpiritAttributes.REVIVAL_FACTOR, (float)(ghostEyeItem.getRevivalFactor() * levelBonusMultiplier));
                  subtractSpiritAttributeValue(player, SpiritAttributes.MAX_SANITY, ghostEyeItem.getSanityBonus() * levelBonusMultiplier);
                  float currentSpirit = getCurrentSpirit(player);
                  float maxSpirit = getMaxSpirit(player);
                  if (currentSpirit > maxSpirit) {
                     setCurrentSpirit(player, maxSpirit);
                  }

                  float currentSanity = getSpiritAttribute(player, SpiritAttributes.SANITY);
                  float maxSanity = getSpiritAttribute(player, SpiritAttributes.MAX_SANITY);
                  if (currentSanity > maxSanity) {
                     setSpiritAttribute(player, SpiritAttributes.SANITY, maxSanity);
                  }
               }
            }

            NbtCompound emptySlot = new NbtCompound();
            emptySlot.putBoolean("occupied", false);
            emptySlot.put("item", new NbtCompound());
            emptySlot.putInt("level", 0);
            emptySlot.putInt("revivalDegree", 0);
            emptySlot.putInt("requiredRevivalDegree", 1000);
            boolean wasUnlocked = slotData.getBoolean("unlocked");
            emptySlot.putBoolean("unlocked", wasUnlocked);
            ghostSlots.put(slotKey, emptySlot);
            data.put("GhostSlots", ghostSlots);
            setSpiritAttributes(player, data);
         }
      }
   }

   public static void removeGhostByType(PlayerEntity player, String ghostType) {
      if (ghostType != null && !ghostType.isEmpty()) {
         for (int i = 0; i < 10; i++) {
            String currentGhostType = getGhostTypeInSlot(player, i);
            if (ghostType.equals(currentGhostType)) {
               clearGhostSlot(player, i);
               break;
            }
         }
      }
   }

   public static void clearSpiritAttributes(PlayerEntity player) {
      setSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.CURRENT_SPIRIT, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.MAX_SPIRIT, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.REVIVAL_FACTOR, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.SANITY, 100.0F);
      setSpiritAttribute(player, SpiritAttributes.MAX_SANITY, 100.0F);
      setSpiritAttribute(player, SpiritAttributes.TEMP_SPIRIT_RESISTANCE, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.TEMP_SPIRIT_RESISTANCE_MULTIPLIER, 1.0F);
      setSpiritAttribute(player, SpiritAttributes.TEMP_SPIRIT_DAMAGE, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.TEMP_SPIRIT_DAMAGE_MULTIPLIER, 1.0F);
      setSpiritAttribute(player, SpiritAttributes.TEMP_MAX_SPIRIT, 0.0F);
      setSpiritAttribute(player, SpiritAttributes.TEMP_SANITY, 0.0F);
      NbtCompound data = getCachedData(player);
      data.putDouble("spiritResistance", 0.0);
      data.putDouble("spiritDamage", 0.0);
      data.putDouble("currentSpirit", 0.0);
      data.putDouble("maxSpirit", 0.0);
      data.putDouble("revivalFactor", 0.0);
      data.putDouble("sanity", 100.0);
      data.putDouble("maxSanity", 100.0);
      data.putDouble("tempSpiritResistance", 0.0);
      data.putDouble("tempSpiritResistanceMultiplier", 1.0);
      data.putDouble("tempSpiritDamage", 0.0);
      data.putDouble("tempSpiritDamageMultiplier", 1.0);
      data.putDouble("tempMaxSpirit", 0.0);
      data.putDouble("tempSanity", 0.0);
      setSpiritAttributes(player, data);
      LOGGER.info("玩家 {} 的所有灵异属性已清空", player.getName().getString());
   }

   public static void recalculateAllSpiritAttributesFromSlots(PlayerEntity player) {
      NbtCompound cachedData = getCachedData(player);
      double cachedMaxSpirit = cachedData.contains("maxSpirit") ? cachedData.getDouble("maxSpirit") : 0.0;
      double cachedCurrentSpirit = cachedData.contains("currentSpirit") ? cachedData.getDouble("currentSpirit") : 0.0;
      if (cachedMaxSpirit > 0.0 && cachedCurrentSpirit == 0.0) {
         clearSpiritAttributes(player);
         NbtCompound data = getCachedData(player);
         if (data.contains("GhostSlots")) {
            NbtCompound ghostSlots = data.getCompound("GhostSlots");

            for (int i = 0; i < 10; i++) {
               String slotKey = "Slot" + i;
               if (ghostSlots.contains(slotKey)) {
                  NbtCompound slotData = ghostSlots.getCompound(slotKey);
                  if (slotData.getBoolean("occupied") && slotData.contains("item")) {
                     ItemStack itemStack = ItemStack.fromNbt(slotData.getCompound("item"));
                     if (itemStack.getItem() instanceof BaseGhostEyeItem ghostEyeItem) {
                        int level = slotData.getInt("level");
                        float levelBonusMultiplier;
                        if (level < 10) {
                           levelBonusMultiplier = 0.5F + (level - 1) * 0.05F;
                        } else {
                           levelBonusMultiplier = 1.0F;
                        }

                        addSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, ghostEyeItem.getSpiritResistanceBonus() * levelBonusMultiplier);
                        addSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE, ghostEyeItem.getSpiritDamageBonus() * levelBonusMultiplier);
                        addSpiritAttribute(player, SpiritAttributes.MAX_SPIRIT, ghostEyeItem.getMaxSpiritBonus() * levelBonusMultiplier);
                        addSpiritAttribute(player, SpiritAttributes.REVIVAL_FACTOR, (float)(ghostEyeItem.getRevivalFactor() * levelBonusMultiplier));
                        addSpiritAttribute(player, SpiritAttributes.MAX_SANITY, ghostEyeItem.getSanityBonus() * levelBonusMultiplier);
                     }
                  }
               }
            }

            float maxSpirit = getMaxSpirit(player);
            setCurrentSpirit(player, maxSpirit);
         }
      }
   }

   public static float handleSpiritDamage(PlayerEntity player, float spiritDamageAmount, float baseAmount, DamageSource source) {
      if (handleGhostShroudEffect(player)) {
         return 0.0F;
      }

      NbtCompound spiritAttributes = getSpiritAttributes(player);
      float spiritResistance = spiritAttributes.contains("spiritResistance") ? (float)spiritAttributes.getDouble("spiritResistance") : 0.0F;
      float tempSpiritResistance = spiritAttributes.contains("tempSpiritResistance") ? (float)spiritAttributes.getDouble("tempSpiritResistance") : 0.0F;
      float tempSpiritResistanceMultiplier = spiritAttributes.contains("tempSpiritResistanceMultiplier")
         ? (float)spiritAttributes.getDouble("tempSpiritResistanceMultiplier")
         : 1.0F;
      float totalResistance = spiritResistance * tempSpiritResistanceMultiplier + tempSpiritResistance;
      float actualSpiritDamage = calculateSpiritDamage(spiritDamageAmount, totalResistance);
      boolean isPossessingGhost = hasOccupiedGhostSlot(player);
      if (hasGhostType(player, "giant_shadow_ghost") || hasGhostType(player, "complete_shadow_ghost")) {
         float maxSpirit = getMaxSpirit(player);
         if (maxSpirit > 0.0F) {
            float damageThreshold = maxSpirit * 0.1F;
            if (actualSpiritDamage > damageThreshold) {
               actualSpiritDamage = damageThreshold;
               if (!player.getWorld().isClient()) {
                  LOGGER.debug("玩家 {} 触发高大鬼影被动技能：免疫溢出伤害，最大伤害限制为 {}", player.getName().getString(), damageThreshold);
               }
            }
         }
      }

      if (actualSpiritDamage > 0.0F && !player.getWorld().isClient()) {
         spawnSpiritDamageParticles(player);
      }

      if (player.isSneaking()) {
         float remainingDamage = actualSpiritDamage;
         List<PlayerGhostEntity> nearbyPlayerGhosts = player.getWorld()
            .getEntitiesByClass(
               PlayerGhostEntity.class,
               player.getBoundingBox().expand(20.0),
               ghost -> ghost instanceof PlayerGhostEntity
                  && ghost.isAlive()
                  && !ghost.isDeadlocked()
                  && ghost.isServantMode()
                  && ghost.getMasterUuid() != null
                  && ghost.getMasterUuid().equals(player.getUuid())
            );
         nearbyPlayerGhosts.sort((g1, g2) -> Integer.compare(g1.getSpiritualStrength(), g2.getSpiritualStrength()));

         for (PlayerGhostEntity ghost : nearbyPlayerGhosts) {
            if (remainingDamage <= 0.0F) {
               break;
            }

            int ghostSpirit = ghost.getSpiritualStrength();
            if (ghostSpirit > 0) {
               int damageToGhost = (int)Math.min(remainingDamage, ghostSpirit);
               ghost.setSpiritualStrength(ghostSpirit - damageToGhost);
               remainingDamage -= damageToGhost;
               if (damageToGhost > 0 && !player.getWorld().isClient()) {
                  spawnSpiritDamageParticles(ghost);
               }
            }
         }

         if (remainingDamage > 0.0F) {
            float currentSpirit = spiritAttributes.contains("currentSpirit") ? (float)spiritAttributes.getDouble("currentSpirit") : 0.0F;
            if (currentSpirit > 0.0F) {
               float finalRemainingDamage = Math.max(0.0F, remainingDamage - currentSpirit);
               setCurrentSpirit(player, Math.max(0.0F, currentSpirit - remainingDamage));
               if (finalRemainingDamage > 0.0F) {
                  ModEvents.processingSpiritDamage.set(true);

                  try {
                     applySpiritDamageWithPenetration(player, source, finalRemainingDamage);
                  } finally {
                     ModEvents.processingSpiritDamage.set(false);
                  }
               }
            } else {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  applySpiritDamageWithPenetration(player, source, remainingDamage);
               } finally {
                  ModEvents.processingSpiritDamage.set(false);
               }
            }
         }
      } else {
         float currentSpirit = spiritAttributes.contains("currentSpirit") ? (float)spiritAttributes.getDouble("currentSpirit") : 0.0F;
         if (currentSpirit > 0.0F) {
            float remainingDamage = Math.max(0.0F, actualSpiritDamage - currentSpirit);
            setCurrentSpirit(player, Math.max(0.0F, currentSpirit - actualSpiritDamage));
            if (remainingDamage > 0.0F) {
               ModEvents.processingSpiritDamage.set(true);

               try {
                  applySpiritDamageWithPenetration(player, source, remainingDamage);
               } finally {
                  ModEvents.processingSpiritDamage.set(false);
               }
            }
         } else {
            ModEvents.processingSpiritDamage.set(true);

            try {
               applySpiritDamageWithPenetration(player, source, baseAmount);
            } finally {
               ModEvents.processingSpiritDamage.set(false);
            }
         }
      }

      if (actualSpiritDamage > 0.0F && !player.getWorld().isClient()) {
         sendSpiritDamagePacketWithDetails(player, baseAmount, actualSpiritDamage);
      }

      return actualSpiritDamage;
   }

   private static void applySpiritDamageWithPenetration(PlayerEntity player, DamageSource source, float damageAmount) {
      if (!(player.getHealth() - damageAmount <= 0.0F) || !ScapegoatGhostItem.handleScapegoatPassiveSkill(player, source, damageAmount)) {
         float healthBefore = player.getHealth();
         player.damage(source, damageAmount);
         float actualDamage = healthBefore - player.getHealth();
         if ((!player.isCreative() || ModConfig.getInstance().penetrateCreativeMode)
            && (!player.isSpectator() || ModConfig.getInstance().penetrateSpectatorMode)) {
            float penetrationThreshold = (float)ModConfig.getInstance().spiritDamagePenetrationThreshold;
            float threshold = damageAmount * penetrationThreshold;
            if (actualDamage < threshold && actualDamage >= 0.0F && player.isAlive()) {
               float missingDamage = damageAmount - actualDamage;
               if (player.getHealth() - missingDamage <= 0.0F && ScapegoatGhostItem.handleScapegoatPassiveSkill(player, source, missingDamage)) {
                  return;
               }

               player.setHealth(Math.max(0.0F, player.getHealth() - missingDamage));
            }
         }
      }
   }

   private static boolean handleGhostShroudEffect(PlayerEntity player) {
      boolean wearingGhostShroud = false;

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         ItemStack stack = player.getEquippedStack(slot);
         if (stack.getItem() instanceof GhostShroudArmorItem) {
            wearingGhostShroud = true;
            break;
         }
      }

      if (!wearingGhostShroud) {
         return false;
      }

      UUID playerUuid = player.getUuid();
      long currentTime = System.currentTimeMillis();
      Long lastUseTime = GHOST_SHROUD_COOLDOWN.get(playerUuid);
      if (lastUseTime != null && currentTime < lastUseTime) {
         return false;
      }

      boolean isAberration = false;
      if (player instanceof ServerPlayerEntity serverPlayer) {
         isAberration = AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration");
      }

      if (isAberration) {
         GHOST_SHROUD_COOLDOWN.put(playerUuid, currentTime + 5000L);
         player.sendMessage(Text.literal("§7鬼寿衣抵御了一次灵异袭击"), true);
         LOGGER.debug("玩家 {}（异类）触发鬼寿衣效果：直接免疫灵异伤害", player.getName().getString());
         return true;
      }

      if (!player.getWorld().isClient()) {
         ModEvents.processingSpiritDamage.set(true);

         try {
            player.damage(ModDamageSources.ghostShroud(player.getWorld()), 4.0F);
         } finally {
            ModEvents.processingSpiritDamage.set(false);
         }
      }

      player.sendMessage(Text.literal("§7鬼寿衣抵御了一次灵异袭击"), true);
      LOGGER.debug("玩家 {}（非异类）触发鬼寿衣效果：免疫灵异伤害，扣除4点生命", player.getName().getString());
      return true;
   }

   public static void spawnSpiritDamageParticles(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)player.getWorld();
         Vec3d pos = player.getPos();
         serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1.0F, 1.0F);

         for (int i = 0; i < 15; i++) {
            double offsetX = (player.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = player.getRandom().nextDouble() * 2.0;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2.0;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.1);
         }
      }
   }

   public static void spawnSpiritDamageParticles(GhostEntity ghost) {
      if (!ghost.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)ghost.getWorld();
         Vec3d pos = ghost.getPos();
         serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ZOMBIE_HURT, SoundCategory.HOSTILE, 1.0F, 1.0F);

         for (int i = 0; i < 15; i++) {
            double offsetX = (ghost.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = ghost.getRandom().nextDouble() * 2.0;
            double offsetZ = (ghost.getRandom().nextDouble() - 0.5) * 2.0;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.1);
         }
      }
   }

   public static float calculateSpiritDamage(float spiritDamage, float spiritResistance) {
      return spiritDamage <= 0.0F ? 0.0F : spiritDamage * spiritDamage / (spiritDamage + Math.max(0.0F, spiritResistance));
   }

   public static float calculateSpiritSurgeDamage(
      float spiritDamage, float extraDamage, float spiritResistance, float tempSpiritResistance, float tempSpiritResistanceMultiplier
   ) {
      if (spiritDamage <= 0.0F && extraDamage <= 0.0F) {
         return 0.0F;
      }

      float totalDamage = spiritDamage + extraDamage;
      float totalResistance = spiritResistance * tempSpiritResistanceMultiplier + tempSpiritResistance;
      float resistanceReduction = totalResistance / 10.0F;
      return Math.max(0.0F, totalDamage - resistanceReduction);
   }

   public static float calculateSpiritSurgeDamage(float spiritDamage, float extraDamage, float spiritResistance) {
      return calculateSpiritSurgeDamage(spiritDamage, extraDamage, spiritResistance, 0.0F, 1.0F);
   }

   public static void validateGhostSlots(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         NbtCompound defaultData = createDefaultSpiritData();

         for (String key : defaultData.getKeys()) {
            if (!data.contains(key)) {
               data.put(key, defaultData.get(key).copy());
            }
         }

         PLAYER_DATA_CACHE.put(player.getUuid(), data);
      }

      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (!ghostSlots.contains(slotKey)) {
            NbtCompound emptySlot = new NbtCompound();
            emptySlot.putBoolean("occupied", false);
            emptySlot.put("item", new NbtCompound());
            emptySlot.putInt("level", 0);
            emptySlot.putInt("revivalDegree", 0);
            emptySlot.putInt("requiredRevivalDegree", 1000);
            ghostSlots.put(slotKey, emptySlot);
         } else {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (!slotData.contains("occupied")) {
               slotData.putBoolean("occupied", false);
            }

            if (!slotData.contains("item")) {
               slotData.put("item", new NbtCompound());
            }

            if (!slotData.contains("level")) {
               slotData.putInt("level", 0);
            }

            if (!slotData.contains("revivalDegree")) {
               slotData.putInt("revivalDegree", 0);
            }

            if (!slotData.contains("requiredRevivalDegree")) {
               slotData.putInt("requiredRevivalDegree", 1000);
            }

            ghostSlots.put(slotKey, slotData);
         }
      }

      data.put("GhostSlots", ghostSlots);
      PLAYER_DATA_CACHE.put(player.getUuid(), data);
   }

   public static void decreaseAllSlotRevivalDegree(PlayerEntity player, int amount) {
      if (!player.getWorld().isClient()) {
         NbtCompound data = getCachedData(player);
         if (data.contains("GhostSlots")) {
            NbtCompound ghostSlots = data.getCompound("GhostSlots");
            boolean anyDecreased = false;

            for (int i = 0; i < 10; i++) {
               String slotKey = "Slot" + i;
               if (ghostSlots.contains(slotKey)) {
                  NbtCompound slotData = ghostSlots.getCompound(slotKey);
                  if (slotData.getBoolean("occupied")) {
                     int currentDegree = slotData.getInt("revivalDegree");
                     int newDegree = Math.max(0, currentDegree - amount);
                     if (newDegree != currentDegree) {
                        slotData.putInt("revivalDegree", newDegree);
                        ghostSlots.put(slotKey, slotData);
                        anyDecreased = true;
                     }
                  }
               }
            }

            if (anyDecreased) {
               data.put("GhostSlots", ghostSlots);
               setSpiritAttributes(player, data);
            }
         }
      }
   }

   public static void balanceRevivalDegree(PlayerEntity player) {
      balanceRevivalDegree(player, 20);
   }

   public static void balanceRevivalDegree(PlayerEntity player, int increaseAmount) {
      int defaultDecreaseAmount = (int)Math.round(increaseAmount * 0.4);
      balanceRevivalDegree(player, increaseAmount, defaultDecreaseAmount);
   }

   public static void balanceRevivalDegree(PlayerEntity player, int increaseAmount, int decreaseAmount) {
      if (!player.getWorld().isClient()) {
         if (player instanceof ServerPlayerEntity serverPlayer && AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration")) {
            increaseAmount = Math.max(1, (int)Math.floor(increaseAmount * 0.5));
         }

         if (!MainGhostManager.hasMainGhost(player)) {
            int firstOccupiedSlot = -1;

            for (int i = 0; i < 10; i++) {
               if (isGhostSlotOccupied(player, i)) {
                  firstOccupiedSlot = i;
                  break;
               }
            }

            if (firstOccupiedSlot < 0) {
               LOGGER.debug("玩家 {} 尝试使用平衡复苏机制但没有任何鬼魂", player.getName().getString());
               return;
            }

            MainGhostManager.setMainGhostSlot(player, firstOccupiedSlot);
            LOGGER.debug("玩家 {} 没有主鬼，自动设置槽位 {} 为主鬼", player.getName().getString(), firstOccupiedSlot);
         }

         int mainSlot = MainGhostManager.getMainGhostSlot(player);
         NbtCompound data = getCachedData(player);
         if (data.contains("GhostSlots")) {
            NbtCompound ghostSlots = data.getCompound("GhostSlots");
            boolean anyChanged = false;

            for (int i = 0; i < 10; i++) {
               String slotKey = "Slot" + i;
               if (ghostSlots.contains(slotKey)) {
                  NbtCompound slotData = ghostSlots.getCompound(slotKey);
                  if (slotData.getBoolean("occupied")) {
                     int currentDegree = slotData.getInt("revivalDegree");
                     int requiredDegree = slotData.getInt("requiredRevivalDegree");
                     if (i == mainSlot) {
                        int newDegree = Math.min(currentDegree + increaseAmount, requiredDegree);
                        if (newDegree != currentDegree) {
                           slotData.putInt("revivalDegree", newDegree);
                           int currentLevel = slotData.getInt("level");
                           int newRevivalLevel = (int)Math.floor((double)newDegree / requiredDegree * 10.0) + 1;
                           if (newRevivalLevel > 10) {
                              newRevivalLevel = 10;
                           }

                           if (newRevivalLevel > currentLevel) {
                              slotData.putInt("level", newRevivalLevel);
                              recalculateSpiritAttributesForSlot(player, i, currentLevel, newRevivalLevel);
                           }

                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     } else {
                        int newDegree = Math.max(0, currentDegree - decreaseAmount);
                        if (newDegree != currentDegree) {
                           slotData.putInt("revivalDegree", newDegree);
                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     }
                  }
               }
            }

            if (anyChanged) {
               data.put("GhostSlots", ghostSlots);
               setSpiritAttributes(player, data);
               GhostDomainManager.checkRevivalDegree(player);
            }
         }
      }
   }

   public static boolean isDeadlockedByTaitouAndDitouGhosts(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         return false;
      }

      int taitouCount = 0;
      int ditouCount = 0;
      boolean hasNonDeadlockGhost = false;
      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               String ghostType = getGhostTypeInSlot(player, i);
               if (ghostType == null) {
                  hasNonDeadlockGhost = true;
               } else {
                  boolean isDeadlockGhost = "taitou_ghost".equals(ghostType)
                     || "ditou_ghost".equals(ghostType)
                     || "burn_ghost".equals(ghostType)
                     || "ghost_fire".equals(ghostType)
                     || "water_ghost".equals(ghostType)
                     || "wish_ghost".equals(ghostType)
                     || "silent_ghost_eye".equals(ghostType)
                     || "sneak_ghost".equals(ghostType)
                     || "jump_ghost".equals(ghostType);
                  if ("taitou_ghost".equals(ghostType)) {
                     taitouCount++;
                  } else if ("ditou_ghost".equals(ghostType)) {
                     ditouCount++;
                  } else if (!isDeadlockGhost) {
                     hasNonDeadlockGhost = true;
                  }
               }
            }
         }
      }

      return taitouCount > 0 && ditouCount > 0 && taitouCount == ditouCount && !hasNonDeadlockGhost;
   }

   public static void enforceDeadlockRevivalState(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         boolean isDeadlocked = isDeadlockedByTaitouAndDitouGhosts(player);
         if (isDeadlocked) {
            NbtCompound data = getCachedData(player);
            if (data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               boolean anyChanged = false;
               boolean hardcoreMode = ModConfig.getInstance().hardcoreDeadlockMode;

               for (int i = 0; i < 10; i++) {
                  String slotKey = "Slot" + i;
                  if (ghostSlots.contains(slotKey)) {
                     NbtCompound slotData = ghostSlots.getCompound(slotKey);
                     if (slotData.getBoolean("occupied")) {
                        int currentDegree = slotData.getInt("revivalDegree");
                        if (hardcoreMode) {
                           if (currentDegree != 0) {
                              slotData.putInt("revivalDegree", 0);
                              ghostSlots.put(slotKey, slotData);
                              anyChanged = true;
                           }
                        } else if (currentDegree > 900) {
                           slotData.putInt("revivalDegree", 900);
                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     }
                  }
               }

               if (anyChanged) {
                  data.put("GhostSlots", ghostSlots);
                  setSpiritAttributes(player, data);
               }
            }
         }
      }
   }

   public static boolean isDeadlockedByBurnAndWaterGhosts(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         return false;
      }

      int burnCount = 0;
      int waterCount = 0;
      boolean hasNonDeadlockGhost = false;
      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               String ghostType = getGhostTypeInSlot(player, i);
               if (ghostType == null) {
                  hasNonDeadlockGhost = true;
               } else {
                  boolean isDeadlockGhost = "taitou_ghost".equals(ghostType)
                     || "ditou_ghost".equals(ghostType)
                     || "burn_ghost".equals(ghostType)
                     || "ghost_fire".equals(ghostType)
                     || "water_ghost".equals(ghostType)
                     || "wish_ghost".equals(ghostType)
                     || "silent_ghost_eye".equals(ghostType)
                     || "sneak_ghost".equals(ghostType)
                     || "jump_ghost".equals(ghostType);
                  if ("burn_ghost".equals(ghostType) || "ghost_fire".equals(ghostType)) {
                     burnCount++;
                  } else if ("water_ghost".equals(ghostType)) {
                     waterCount++;
                  } else if (!isDeadlockGhost) {
                     hasNonDeadlockGhost = true;
                  }
               }
            }
         }
      }

      return burnCount > 0 && waterCount > 0 && burnCount == waterCount && !hasNonDeadlockGhost;
   }

   public static void enforceBurnWaterDeadlockRevivalState(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         boolean isDeadlocked = isDeadlockedByBurnAndWaterGhosts(player);
         if (isDeadlocked) {
            NbtCompound data = getCachedData(player);
            if (data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               boolean anyChanged = false;
               boolean hardcoreMode = ModConfig.getInstance().hardcoreDeadlockMode;

               for (int i = 0; i < 10; i++) {
                  String slotKey = "Slot" + i;
                  if (ghostSlots.contains(slotKey)) {
                     NbtCompound slotData = ghostSlots.getCompound(slotKey);
                     if (slotData.getBoolean("occupied")) {
                        int currentDegree = slotData.getInt("revivalDegree");
                        if (hardcoreMode) {
                           if (currentDegree != 0) {
                              slotData.putInt("revivalDegree", 0);
                              ghostSlots.put(slotKey, slotData);
                              anyChanged = true;
                           }
                        } else if (currentDegree > 900) {
                           slotData.putInt("revivalDegree", 900);
                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     }
                  }
               }

               if (anyChanged) {
                  data.put("GhostSlots", ghostSlots);
                  setSpiritAttributes(player, data);
               }
            }
         }
      }
   }

   public static boolean hasGhostBlood(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = getGhostSlotItem(player, i);
         if (!ghostItem.isEmpty() && ghostItem.getItem() == ModItems.GHOST_BLOOD) {
            return true;
         }
      }

      return false;
   }

   public static void enforceGhostBloodDeadlockRevivalState(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         boolean hasGhostBlood = hasGhostBlood(player);
         if (hasGhostBlood) {
            NbtCompound data = getCachedData(player);
            if (data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               boolean anyChanged = false;
               boolean hardcoreMode = ModConfig.getInstance().hardcoreDeadlockMode;

               for (int i = 0; i < 10; i++) {
                  String ghostType = getGhostTypeInSlot(player, i);
                  if (!"ghost_blood".equals(ghostType)) {
                     String slotKey = "Slot" + i;
                     if (ghostSlots.contains(slotKey)) {
                        NbtCompound slotData = ghostSlots.getCompound(slotKey);
                        if (slotData.getBoolean("occupied")) {
                           int currentDegree = slotData.getInt("revivalDegree");
                           if (hardcoreMode) {
                              if (currentDegree != 0) {
                                 slotData.putInt("revivalDegree", 0);
                                 ghostSlots.put(slotKey, slotData);
                                 anyChanged = true;
                              }
                           } else if (currentDegree > 900) {
                              slotData.putInt("revivalDegree", 900);
                              ghostSlots.put(slotKey, slotData);
                              anyChanged = true;
                           }
                        }
                     }
                  }
               }

               if (anyChanged) {
                  data.put("GhostSlots", ghostSlots);
                  setSpiritAttributes(player, data);
               }
            }
         }
      }
   }

   public static boolean isDeadlockedByWishGhostAndSilentGhostEye(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         return false;
      }

      int wishGhostCount = 0;
      int silentGhostEyeCount = 0;
      boolean hasNonDeadlockGhost = false;
      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               String ghostType = getGhostTypeInSlot(player, i);
               if (ghostType == null) {
                  hasNonDeadlockGhost = true;
               } else {
                  boolean isDeadlockGhost = "taitou_ghost".equals(ghostType)
                     || "ditou_ghost".equals(ghostType)
                     || "burn_ghost".equals(ghostType)
                     || "ghost_fire".equals(ghostType)
                     || "water_ghost".equals(ghostType)
                     || "wish_ghost".equals(ghostType)
                     || "silent_ghost_eye".equals(ghostType)
                     || "sneak_ghost".equals(ghostType)
                     || "jump_ghost".equals(ghostType);
                  if ("wish_ghost".equals(ghostType)) {
                     wishGhostCount++;
                  } else if ("silent_ghost_eye".equals(ghostType)) {
                     silentGhostEyeCount++;
                  } else if (!isDeadlockGhost) {
                     hasNonDeadlockGhost = true;
                  }
               }
            }
         }
      }

      return wishGhostCount > 0 && silentGhostEyeCount > 0 && wishGhostCount == silentGhostEyeCount && !hasNonDeadlockGhost;
   }

   public static void enforceWishGhostSilentGhostEyeDeadlockRevivalState(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         boolean isDeadlocked = isDeadlockedByWishGhostAndSilentGhostEye(player);
         if (isDeadlocked) {
            NbtCompound data = getCachedData(player);
            if (data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               boolean anyChanged = false;
               boolean hardcoreMode = ModConfig.getInstance().hardcoreDeadlockMode;

               for (int i = 0; i < 10; i++) {
                  String slotKey = "Slot" + i;
                  if (ghostSlots.contains(slotKey)) {
                     NbtCompound slotData = ghostSlots.getCompound(slotKey);
                     if (slotData.getBoolean("occupied")) {
                        int currentDegree = slotData.getInt("revivalDegree");
                        if (hardcoreMode) {
                           if (currentDegree != 0) {
                              slotData.putInt("revivalDegree", 0);
                              ghostSlots.put(slotKey, slotData);
                              anyChanged = true;
                           }
                        } else if (currentDegree > 900) {
                           slotData.putInt("revivalDegree", 900);
                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     }
                  }
               }

               if (anyChanged) {
                  data.put("GhostSlots", ghostSlots);
                  setSpiritAttributes(player, data);
               }
            }
         }
      }
   }

   public static boolean isDeadlockedBySneakAndJumpGhosts(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      if (!data.contains("GhostSlots")) {
         return false;
      }

      int sneakGhostCount = 0;
      int jumpGhostCount = 0;
      boolean hasNonDeadlockGhost = false;
      NbtCompound ghostSlots = data.getCompound("GhostSlots");

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (ghostSlots.contains(slotKey)) {
            NbtCompound slotData = ghostSlots.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               String ghostType = getGhostTypeInSlot(player, i);
               if (ghostType == null) {
                  hasNonDeadlockGhost = true;
               } else {
                  boolean isDeadlockGhost = "taitou_ghost".equals(ghostType)
                     || "ditou_ghost".equals(ghostType)
                     || "burn_ghost".equals(ghostType)
                     || "ghost_fire".equals(ghostType)
                     || "water_ghost".equals(ghostType)
                     || "wish_ghost".equals(ghostType)
                     || "silent_ghost_eye".equals(ghostType)
                     || "sneak_ghost".equals(ghostType)
                     || "jump_ghost".equals(ghostType);
                  if ("sneak_ghost".equals(ghostType)) {
                     sneakGhostCount++;
                  } else if ("jump_ghost".equals(ghostType)) {
                     jumpGhostCount++;
                  } else if (!isDeadlockGhost) {
                     hasNonDeadlockGhost = true;
                  }
               }
            }
         }
      }

      return sneakGhostCount > 0 && jumpGhostCount > 0 && sneakGhostCount == jumpGhostCount && !hasNonDeadlockGhost;
   }

   public static void enforceSneakJumpDeadlockRevivalState(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         boolean isDeadlocked = isDeadlockedBySneakAndJumpGhosts(player);
         if (isDeadlocked) {
            NbtCompound data = getCachedData(player);
            if (data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               boolean anyChanged = false;
               boolean hardcoreMode = ModConfig.getInstance().hardcoreDeadlockMode;

               for (int i = 0; i < 10; i++) {
                  String slotKey = "Slot" + i;
                  if (ghostSlots.contains(slotKey)) {
                     NbtCompound slotData = ghostSlots.getCompound(slotKey);
                     if (slotData.getBoolean("occupied")) {
                        int currentDegree = slotData.getInt("revivalDegree");
                        if (hardcoreMode) {
                           if (currentDegree != 0) {
                              slotData.putInt("revivalDegree", 0);
                              ghostSlots.put(slotKey, slotData);
                              anyChanged = true;
                           }
                        } else if (currentDegree > 900) {
                           slotData.putInt("revivalDegree", 900);
                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     }
                  }
               }

               if (anyChanged) {
                  data.put("GhostSlots", ghostSlots);
                  setSpiritAttributes(player, data);
               }
            }
         }
      }
   }

   public static void enforceSlotDeadlockRevivalState(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         NbtCompound data = getCachedData(player);
         if (data.contains("GhostSlots")) {
            NbtCompound ghostSlots = data.getCompound("GhostSlots");
            boolean anyChanged = false;
            boolean hardcoreMode = ModConfig.getInstance().hardcoreDeadlockMode;

            for (int i = 0; i < 10; i++) {
               String slotKey = "Slot" + i;
               if (ghostSlots.contains(slotKey)) {
                  NbtCompound slotData = ghostSlots.getCompound(slotKey);
                  if (slotData.getBoolean("occupied") && slotData.getBoolean("slotDeadlocked")) {
                     int currentDegree = slotData.getInt("revivalDegree");
                     if (hardcoreMode) {
                        if (currentDegree != 0) {
                           slotData.putInt("revivalDegree", 0);
                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     } else if (currentDegree > 900) {
                        slotData.putInt("revivalDegree", 900);
                        ghostSlots.put(slotKey, slotData);
                        anyChanged = true;
                     }
                  }
               }
            }

            if (anyChanged) {
               data.put("GhostSlots", ghostSlots);
               setSpiritAttributes(player, data);
            }
         }
      }
   }

   public static void enforceGhostChildFusionDeadlockRevivalState(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         if (isGhostChildFused(player)) {
            NbtCompound data = getCachedData(player);
            if (data.contains("GhostSlots")) {
               NbtCompound ghostSlots = data.getCompound("GhostSlots");
               boolean anyChanged = false;
               boolean hardcoreMode = ModConfig.getInstance().hardcoreDeadlockMode;

               for (int i = 0; i < 10; i++) {
                  String slotKey = "Slot" + i;
                  if (ghostSlots.contains(slotKey)) {
                     NbtCompound slotData = ghostSlots.getCompound(slotKey);
                     if (slotData.getBoolean("occupied")) {
                        int currentDegree = slotData.getInt("revivalDegree");
                        if (hardcoreMode) {
                           if (currentDegree != 0) {
                              slotData.putInt("revivalDegree", 0);
                              ghostSlots.put(slotKey, slotData);
                              anyChanged = true;
                           }
                        } else if (currentDegree > 900) {
                           slotData.putInt("revivalDegree", 900);
                           ghostSlots.put(slotKey, slotData);
                           anyChanged = true;
                        }
                     }
                  }
               }

               if (anyChanged) {
                  data.put("GhostSlots", ghostSlots);
                  setSpiritAttributes(player, data);
               }
            }
         }
      }
   }

   public static void enforceAllDeadlockRevivalStates(PlayerEntity player) {
      enforceGhostChildFusionDeadlockRevivalState(player);
      enforceSlotDeadlockRevivalState(player);
      enforceDeadlockRevivalState(player);
      enforceBurnWaterDeadlockRevivalState(player);
      enforceGhostBloodDeadlockRevivalState(player);
      enforceWishGhostSilentGhostEyeDeadlockRevivalState(player);
      enforceSneakJumpDeadlockRevivalState(player);
   }

   public static float getSpiritAttribute(ServerPlayerEntity player, String attributeName, float defaultValue) {
      try {
         NbtCompound spiritAttributes = getSpiritAttributes(player);
         if (spiritAttributes.contains(attributeName)) {
            if (spiritAttributes.contains(attributeName, 3)) {
               return spiritAttributes.getInt(attributeName);
            }

            if (spiritAttributes.contains(attributeName, 6)) {
               return (float)spiritAttributes.getDouble(attributeName);
            }

            return spiritAttributes.getFloat(attributeName);
         }

         LOGGER.warn("属性 {} 未在NBT中找到，使用默认值: {}", attributeName, defaultValue);
      } catch (Exception e) {
         LOGGER.error("获取灵异属性 {} 时出错: {}", attributeName, e.getMessage());
         LOGGER.warn("由于错误，属性 {} 使用默认值: {}", attributeName, defaultValue);
      }

      return defaultValue;
   }

   public static void sendSpiritDamagePacketWithDetails(PlayerEntity player, float baseAmount, float actualSpiritDamage) {
      if (player != null && !(actualSpiritDamage <= 0.0F) && !player.getWorld().isClient()) {
         if (player instanceof ServerPlayerEntity serverPlayer) {
            try {
               PacketByteBuf buf = PacketByteBufs.create();
               buf.writeFloat(baseAmount);
               buf.writeFloat(actualSpiritDamage);
               Identifier packetId = new Identifier("smfs", "spirit_damage");
               ServerPlayNetworking.send(serverPlayer, packetId, buf);
            } catch (Exception e) {
               LOGGER.error("发送灵异伤害网络包时出错: {}", e.getMessage());
            }
         }
      }
   }

   public static int calculateBodyEnhancementResistance(int ghostCount) {
      if (ghostCount < 2) {
         return -1;
      } else if (ghostCount >= 6) {
         return 4;
      } else if (ghostCount >= 5) {
         return 3;
      } else if (ghostCount >= 4) {
         return 2;
      } else {
         return ghostCount >= 3 ? 1 : 0;
      }
   }

   public static void applyBodyEnhancement(PlayerEntity player, int resistanceLevel) {
      if (player != null && !player.getWorld().isClient()) {
         if (resistanceLevel >= 0) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30, resistanceLevel, false, false, true));
         }
      }
   }

   public static void updatePlayerSpiritAttributes(PlayerEntity player) {
      if (player != null && !player.getWorld().isClient()) {
         int mode = ModConfig.getInstance().bodyEnhancementMode;
         switch (mode) {
            case 0:
            default:
               break;
            case 1:
               int ghostCount = countOccupiedGhostSlots(player);
               int resistanceLevel = calculateBodyEnhancementResistance(ghostCount);
               applyBodyEnhancement(player, resistanceLevel);
               break;
            case 2:
               NbtCompound spiritAttributes = getSpiritAttributes(player);
               int spiritualStrength = (int)spiritAttributes.getDouble("currentSpirit");
               if (spiritualStrength > 1000) {
                  player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30, 4, false, false, true));
                  player.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 255, false, false, true));
               }
         }
      }
   }

   private static void handleHumanSkinPaperEquip(PlayerEntity player, EquipmentSlot equipmentSlot, ItemStack next) {
      if (equipmentSlot == EquipmentSlot.HEAD) {
         if (next.isOf(ModItems.HUMAN_SKIN_PAPER)) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               ServerPlayNetworking.send(serverPlayer, OpenHumanSkinPaperEndingS2CPacket.ID, OpenHumanSkinPaperEndingS2CPacket.create());
            }
         }
      }
   }

   private static void handleEquipmentChange(PlayerEntity player, EquipmentSlot equipmentSlot, ItemStack previous, ItemStack next) {
      if (!player.getWorld().isClient()) {
         double resistanceChange = calculateEquipmentResistanceChange(previous, next, equipmentSlot);
         double maxSanityChange = calculateEquipmentMaxSanityChange(previous, next, equipmentSlot);
         double maxSpiritChange = calculateEquipmentMaxSpiritChange(previous, next, equipmentSlot);
         setSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE, (float)resistanceChange);
         setSpiritAttribute(player, SpiritAttributes.MAX_SANITY, (float)maxSanityChange);
         setSpiritAttribute(player, SpiritAttributes.MAX_SPIRIT, (float)maxSpiritChange);
         EntityAttributeInstance sanityInstance = player.getAttributeInstance(SpiritAttributes.SANITY);
         EntityAttributeInstance maxSanityInstance = player.getAttributeInstance(SpiritAttributes.MAX_SANITY);
         EntityAttributeInstance maxSpiritInstance = player.getAttributeInstance(SpiritAttributes.MAX_SPIRIT);
         EntityAttributeInstance currentSpiritInstance = player.getAttributeInstance(SpiritAttributes.CURRENT_SPIRIT);
         if (sanityInstance != null && maxSanityInstance != null) {
            float currentSanity = (float)sanityInstance.getBaseValue();
            float newMaxSanityValue = (float)maxSanityInstance.getBaseValue();
            if (currentSanity > newMaxSanityValue) {
               sanityInstance.setBaseValue(newMaxSanityValue);
               NbtCompound data = getCachedData(player);
               data.putDouble("sanity", newMaxSanityValue);
               PLAYER_DATA_CACHE.put(player.getUuid(), data);
            }
         }

         if (maxSpiritInstance != null && currentSpiritInstance != null) {
            float currentSpirit = (float)currentSpiritInstance.getBaseValue();
            float newMaxSpiritValue = (float)maxSpiritInstance.getBaseValue();
            if (currentSpirit > newMaxSpiritValue) {
               currentSpiritInstance.setBaseValue(newMaxSpiritValue);
               NbtCompound data = getCachedData(player);
               data.putDouble("currentSpirit", newMaxSpiritValue);
               PLAYER_DATA_CACHE.put(player.getUuid(), data);
            }
         }

         if (player instanceof ServerPlayerEntity serverPlayer) {
            AdvancementManager.checkAndUnlockDualGoldenGuns(serverPlayer);
         }
      }
   }

   private static double calculateEquipmentResistanceChange(ItemStack previous, ItemStack next, EquipmentSlot equipmentSlot) {
      double change = 0.0;
      if (!previous.isEmpty() && previous.getItem() instanceof DefiledArmorItem) {
         change -= getDefiledArmorResistance(previous, equipmentSlot);
      }

      if (!next.isEmpty() && next.getItem() instanceof DefiledArmorItem) {
         change += getDefiledArmorResistance(next, equipmentSlot);
      }

      return change;
   }

   private static double calculateEquipmentMaxSanityChange(ItemStack previous, ItemStack next, EquipmentSlot equipmentSlot) {
      double change = 0.0;
      if (!previous.isEmpty() && previous.getItem() instanceof DefiledArmorItem) {
         change -= getDefiledArmorMaxSanity(previous, equipmentSlot);
      }

      if (!next.isEmpty() && next.getItem() instanceof DefiledArmorItem) {
         change += getDefiledArmorMaxSanity(next, equipmentSlot);
      }

      return change;
   }

   private static double calculateEquipmentMaxSpiritChange(ItemStack previous, ItemStack next, EquipmentSlot equipmentSlot) {
      double change = 0.0;
      if (!previous.isEmpty() && previous.getItem() instanceof DefiledArmorItem) {
         change -= getDefiledArmorMaxSpirit(previous, equipmentSlot);
      }

      if (!next.isEmpty() && next.getItem() instanceof DefiledArmorItem) {
         change += getDefiledArmorMaxSpirit(next, equipmentSlot);
      }

      return change;
   }

   private static double getDefiledArmorResistance(ItemStack armorStack, EquipmentSlot equipmentSlot) {
      switch (equipmentSlot) {
         case HEAD:
            return 10.0;
         case CHEST:
            return 20.0;
         case LEGS:
            return 15.0;
         case FEET:
            return 10.0;
         default:
            return 0.0;
      }
   }

   private static double getDefiledArmorMaxSanity(ItemStack armorStack, EquipmentSlot equipmentSlot) {
      switch (equipmentSlot) {
         case HEAD:
            return 5.0;
         case CHEST:
            return 10.0;
         case LEGS:
            return 7.5;
         case FEET:
            return 5.0;
         default:
            return 0.0;
      }
   }

   private static double getDefiledArmorMaxSpirit(ItemStack armorStack, EquipmentSlot equipmentSlot) {
      switch (equipmentSlot) {
         case HEAD:
            return 100.0;
         case CHEST:
            return 200.0;
         case LEGS:
            return 150.0;
         case FEET:
            return 50.0;
         default:
            return 0.0;
      }
   }

   public static boolean hasWangCurseUnlocked(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      return data.getBoolean("wangCurseUnlocked");
   }

   public static boolean unlockWangCurse(PlayerEntity player) {
      if (hasWangCurseUnlocked(player)) {
         return false;
      }

      NbtCompound data = getCachedData(player);
      data.putBoolean("wangCurseUnlocked", true);
      saveDataToPlayer(player, data);
      LOGGER.info("玩家 {} 解锁了王家诅咒", player.getName().getString());
      return true;
   }

   public static List<UUID> getServants(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      NbtList servantsList = data.getList("servants", 11);
      List<UUID> servants = new ArrayList<>();

      for (NbtElement element : servantsList) {
         if (element instanceof NbtIntArray uuidArray) {
            int[] uuidInts = uuidArray.getIntArray();
            if (uuidInts.length == 4) {
               UUID uuid = new UUID((long)uuidInts[0] << 32 | uuidInts[1] & 4294967295L, (long)uuidInts[2] << 32 | uuidInts[3] & 4294967295L);
               servants.add(uuid);
            }
         }
      }

      return servants;
   }

   public static boolean addServant(PlayerEntity player, UUID servantUuid) {
      if (hasServant(player, servantUuid)) {
         return false;
      }

      NbtCompound data = getCachedData(player);
      NbtList servantsList = data.getList("servants", 11);
      int maxServants = getMaxServants(player);
      if (servantsList.size() >= maxServants) {
         if (!player.getWorld().isClient()) {
            player.sendMessage(Text.literal("§c你的厉鬼数量已达到上限！"));
         }

         return false;
      } else {
         long mostSigBits = servantUuid.getMostSignificantBits();
         long leastSigBits = servantUuid.getLeastSignificantBits();
         int[] uuidInts = new int[]{(int)(mostSigBits >> 32), (int)mostSigBits, (int)(leastSigBits >> 32), (int)leastSigBits};
         servantsList.add(new NbtIntArray(uuidInts));
         data.put("servants", servantsList);
         saveDataToPlayer(player, data);
         return true;
      }
   }

   public static boolean removeServant(PlayerEntity player, UUID servantUuid) {
      NbtCompound data = getCachedData(player);
      NbtList servantsList = data.getList("servants", 11);

      for (int i = 0; i < servantsList.size(); i++) {
         if (servantsList.get(i) instanceof NbtIntArray uuidArray) {
            int[] uuidInts = uuidArray.getIntArray();
            if (uuidInts.length == 4) {
               UUID uuid = new UUID((long)uuidInts[0] << 32 | uuidInts[1] & 4294967295L, (long)uuidInts[2] << 32 | uuidInts[3] & 4294967295L);
               if (uuid.equals(servantUuid)) {
                  servantsList.remove(i);
                  data.put("servants", servantsList);
                  NbtCompound servantsData = data.getCompound("servantsData");
                  if (servantsData.contains(servantUuid.toString())) {
                     servantsData.remove(servantUuid.toString());
                     data.put("servantsData", servantsData);
                  }

                  saveDataToPlayer(player, data);
                  return true;
               }
            }
         }
      }

      return false;
   }

   public static boolean hasServant(PlayerEntity player, UUID servantUuid) {
      List<UUID> servants = getServants(player);
      return servants.contains(servantUuid);
   }

   public static int getMaxServants(PlayerEntity player) {
      return 3;
   }

   public static int getCurrentServantsCount(PlayerEntity player) {
      return getServants(player).size();
   }

   public static boolean isPlayerServant(PlayerEntity player, Entity entity) {
      return hasServant(player, entity.getUuid());
   }

   public static Entity getEntityByUuid(PlayerEntity player, UUID uuid) {
      if (!player.getWorld().isClient()) {
         return player.getWorld() instanceof ServerWorld serverWorld ? serverWorld.getEntity(uuid) : null;
      }

      if (player.getWorld() instanceof ClientWorld clientWorld) {
         for (Entity entity : clientWorld.getEntities()) {
            if (entity.getUuid().equals(uuid)) {
               return entity;
            }
         }
      }

      return null;
   }

   public static boolean isPlayerMasterOfServant(PlayerEntity player, PlayerGhostEntity playerGhost) {
      if (!hasWangCurseUnlocked(player)) {
         return false;
      } else {
         return playerGhost.isServantMode() && playerGhost.getMasterUuid() != null
            ? player.getUuid().equals(playerGhost.getMasterUuid())
            : hasServant(player, playerGhost.getUuid());
      }
   }

   public static boolean restoreServantFromStoredData(PlayerEntity master, UUID servantUuid, PlayerGhostEntity playerGhost) {
      if (!hasServant(master, servantUuid)) {
         LOGGER.warn("玩家 {} 尝试恢复不存在的奴仆 {}", master.getName().getString(), servantUuid);
         return false;
      }

      NbtCompound data = getCachedData(master);
      NbtCompound servantsData = data.getCompound("servantsData");
      if (!servantsData.contains(servantUuid.toString())) {
         LOGGER.warn("玩家 {} 的奴仆 {} 没有存储数据，使用默认设置", master.getName().getString(), servantUuid);
         playerGhost.setCustomName(Text.literal("王家奴仆"));
         playerGhost.setCustomNameVisible(true);
         playerGhost.setGhostDomainColor(String.valueOf(8388736));
         playerGhost.setGhostDomainLevel(1);
         playerGhost.setGhostDomainRadius(8.0F);
         playerGhost.setSpiritualStrength(100);
         playerGhost.setMaxSpiritualStrength(100);
         playerGhost.setSpiritualResistance(50);
         playerGhost.setSpiritualDamage(50);
         playerGhost.setRecoveryFactor(0.2F);
         return true;
      }

      NbtCompound servantData = servantsData.getCompound(servantUuid.toString());
      LOGGER.debug("找到玩家 {} 的奴仆 {} 的存储数据: {}", master.getName().getString(), servantUuid, servantData.toString());
      if (servantData.contains("playerName")) {
         String playerName = servantData.getString("playerName");
         playerGhost.setCustomName(Text.literal(playerName));
         playerGhost.setCustomNameVisible(true);
         playerGhost.setPlayerName(playerName);
      }

      if (servantData.contains("playerUuid")) {
         String uuidStr = servantData.getString("playerUuid");

         try {
            UUID originalPlayerUuid = UUID.fromString(uuidStr);
            playerGhost.setPlayerUuid(originalPlayerUuid.toString());
         } catch (IllegalArgumentException e) {
            LOGGER.warn("无效的玩家UUID格式: {}", uuidStr);
         }
      }

      if (servantData.contains("ghostDomainColor")) {
         String ghostDomainColor = servantData.getString("ghostDomainColor");
         playerGhost.setGhostDomainColor(ghostDomainColor);
      }

      if (servantData.contains("ghostDomainLevel")) {
         int ghostDomainLevel = servantData.getInt("ghostDomainLevel");
         playerGhost.setGhostDomainLevel(ghostDomainLevel);
      }

      if (servantData.contains("ghostDomainRadius")) {
         float ghostDomainRadius = servantData.getFloat("ghostDomainRadius");
         playerGhost.setGhostDomainRadius(ghostDomainRadius);
      }

      if (servantData.contains("SpiritualStrength")) {
         int spiritualStrength = servantData.getInt("SpiritualStrength");
         playerGhost.setSpiritualStrength(spiritualStrength);
      }

      if (servantData.contains("MaxSpiritualStrength")) {
         int maxSpiritualStrength = servantData.getInt("MaxSpiritualStrength");
         playerGhost.setMaxSpiritualStrength(maxSpiritualStrength);
      }

      if (servantData.contains("SpiritualResistance")) {
         int spiritualResistance = servantData.getInt("SpiritualResistance");
         playerGhost.setSpiritualResistance(spiritualResistance);
      }

      if (servantData.contains("SpiritualDamage")) {
         int spiritualDamage = servantData.getInt("SpiritualDamage");
         playerGhost.setSpiritualDamage(spiritualDamage);
      }

      if (servantData.contains("RecoveryFactor")) {
         float recoveryFactor = servantData.getFloat("RecoveryFactor");
         playerGhost.setRecoveryFactor(recoveryFactor);
      }

      if (servantData.contains("tamedGhosts")) {
         NbtList tamedGhostsList = servantData.getList("tamedGhosts", 10);
         new ArrayList();

         for (NbtElement element : tamedGhostsList) {
            if (element instanceof NbtCompound var10) {
               ;
            }
         }
      }

      return true;
   }

   public static boolean recallServant(PlayerEntity master, PlayerGhostEntity servant) {
      if (!hasWangCurseUnlocked(master)) {
         LOGGER.warn("玩家 {} 未解锁王家诅咒，无法收回奴仆", master.getName().getString());
         return false;
      }

      if (!servant.isServantMode()) {
         LOGGER.warn("玩家 {} 尝试收回非奴仆模式的玩家鬼 {}", master.getName().getString(), servant.getPlayerName());
         return false;
      }

      if (servant.getMasterUuid() == null) {
         LOGGER.warn("玩家 {} 尝试收回没有主人的奴仆 {}", master.getName().getString(), servant.getPlayerName());
         return false;
      }

      if (!isPlayerMasterOfServant(master, servant)) {
         LOGGER.warn("玩家 {} 尝试收回不属于自己的奴仆 {}", master.getName().getString(), servant.getPlayerName());
         return false;
      }

      try {
         UUID servantUuid = servant.getOriginalServantUuid() != null ? servant.getOriginalServantUuid() : servant.getUuid();
         NbtCompound data = getCachedData(master);
         NbtCompound servantsData = data.getCompound("servantsData");
         LOGGER.debug("玩家 {} 尝试收回奴仆 {}，servantsData中是否包含该UUID: {}", master.getName().getString(), servantUuid, servantsData.contains(servantUuid.toString()));
         String key = servantUuid.toString();
         if (servantsData.contains(key)) {
            NbtCompound servantData = servantsData.getCompound(key);
            LOGGER.debug("奴仆 {} 的数据: {}, 是否包含released标记: {}", servantUuid, servantData, servantData.contains("released"));
            servantData.putBoolean("released", false);
            servantsData.put(key, servantData);
            data.put("servantsData", servantsData);
            saveDataToPlayer(master, data);
            LOGGER.debug("已清除奴仆 {} 的已释放标记，更新后的数据: {}", servantUuid, servantData);
         } else {
            LOGGER.debug("servantsData中没有奴仆 {} 的数据，创建新数据", servantUuid);
            NbtCompound servantData = new NbtCompound();
            servantData.putBoolean("released", false);
            servantsData.put(key, servantData);
            data.put("servantsData", servantsData);
            saveDataToPlayer(master, data);
         }

         if (!master.getWorld().isClient()) {
            master.sendMessage(Text.literal("成功收回奴仆 " + servant.getPlayerName()), true);
         }

         return true;
      } catch (Exception e) {
         LOGGER.error("玩家 {} 收回奴仆 {} 时发生错误", master.getName().getString(), servant.getPlayerName(), e);
         return false;
      }
   }

   public static List<UUID> getReleasedServants(PlayerEntity player) {
      List<UUID> releasedServants = new ArrayList<>();
      NbtCompound data = getCachedData(player);
      NbtCompound servantsData = data.getCompound("servantsData");

      for (String key : servantsData.getKeys()) {
         try {
            UUID servantUuid = UUID.fromString(key);
            NbtCompound servantData = servantsData.getCompound(key);
            if (servantData.contains("released", 1) && servantData.getBoolean("released")) {
               releasedServants.add(servantUuid);
            }
         } catch (IllegalArgumentException e) {
            LOGGER.warn("无效的奴仆UUID格式: {}", key);
         }
      }

      return releasedServants;
   }

   public static boolean isServantReleased(PlayerEntity player, UUID servantUuid) {
      NbtCompound data = getCachedData(player);
      NbtCompound servantsData = data.getCompound("servantsData");
      String key = servantUuid.toString();
      if (servantsData.contains(key)) {
         NbtCompound servantData = servantsData.getCompound(key);
         return servantData.getBoolean("released");
      } else {
         return false;
      }
   }

   public static void markServantAsReleased(PlayerEntity player, UUID servantUuid) {
      try {
         NbtCompound data = getCachedData(player);
         NbtCompound servantsData = data.getCompound("servantsData");
         String key = servantUuid.toString();
         NbtCompound servantData;
         if (servantsData.contains(key)) {
            servantData = servantsData.getCompound(key);
         } else {
            servantData = new NbtCompound();
         }

         servantData.putBoolean("released", true);
         servantsData.put(key, servantData);
         data.put("servantsData", servantsData);
         saveDataToPlayer(player, data);
      } catch (Exception e) {
         LOGGER.error("玩家 {} 标记奴仆 {} 为已释放时发生错误", player.getName().getString(), servantUuid, e);
      }
   }

   private static void initializeServantsData(NbtCompound data) {
      if (!data.contains("servants")) {
         data.put("servants", new NbtList());
      }

      if (!data.contains("wangCurseUnlocked")) {
         data.putBoolean("wangCurseUnlocked", false);
      }

      if (!data.contains("servantsData", 10)) {
         data.put("servantsData", new NbtCompound());
      }
   }

   public static int getBonusSuppressionSlots(PlayerEntity player) {
      NbtCompound data = getCachedData(player);
      return data.contains("BonusSuppressionSlots") ? data.getInt("BonusSuppressionSlots") : 0;
   }

   public static void setBonusSuppressionSlots(PlayerEntity player, int slots) {
      NbtCompound data = getCachedData(player);
      data.putInt("BonusSuppressionSlots", slots);
      saveDataToPlayer(player, data);
   }

   public static void addBonusSuppressionSlots(PlayerEntity player, int amount) {
      int current = getBonusSuppressionSlots(player);
      setBonusSuppressionSlots(player, current + amount);
   }
}
