package com.xie.smfs.entity.other;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.data.PlayerRoyalCurseManager;
import com.xie.smfs.data.RoyalCurseData;
import com.xie.smfs.data.RoyalCurseServantData;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.BlockGhostEntity;
import com.xie.smfs.entity.ghost.BoxGhostEntity;
import com.xie.smfs.entity.ghost.BurnGhostEntity;
import com.xie.smfs.entity.ghost.CropGhostEntity;
import com.xie.smfs.entity.ghost.DeathSightGhostEntity;
import com.xie.smfs.entity.ghost.DitouGhostEntity;
import com.xie.smfs.entity.ghost.FogGhostEntity;
import com.xie.smfs.entity.ghost.FoodGhostEntity;
import com.xie.smfs.entity.ghost.GanshiBrideGhostEntity;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.entity.ghost.GhostMerchantEntity;
import com.xie.smfs.entity.ghost.GhostSmokeEntity;
import com.xie.smfs.entity.ghost.GhostWindEntity;
import com.xie.smfs.entity.ghost.GiantShadowGhostEntity;
import com.xie.smfs.entity.ghost.GraveEarthGhostEntity;
import com.xie.smfs.entity.ghost.JumpGhostEntity;
import com.xie.smfs.entity.ghost.LostGhostEntity;
import com.xie.smfs.entity.ghost.StepGhostEntity;
import com.xie.smfs.entity.ghost.TaitouGhostEntity;
import com.xie.smfs.entity.ghost.TrashGhostEntity;
import com.xie.smfs.entity.ghost.VillagerGhostEntity;
import com.xie.smfs.entity.ghost.WaterGhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.item.SilentGhostEyeItem;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModItems;
import com.xie.smfs.util.GhostUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerGhostEntity extends GhostEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger(PlayerGhostEntity.class);
   private static final TrackedData<String> PLAYER_UUID = DataTracker.registerData(PlayerGhostEntity.class, TrackedDataHandlerRegistry.STRING);
   private static final TrackedData<String> PLAYER_NAME = DataTracker.registerData(PlayerGhostEntity.class, TrackedDataHandlerRegistry.STRING);
   private String playerUuid;
   private String playerName;
   private List<String> playerGhosts = new ArrayList<>();
   private String ghostDomainColor = "none";
   private List<String> killingRules = new ArrayList<>();
   private NbtCompound ghostSlotsData = new NbtCompound();
   private Map<UUID, Boolean> playerJumpingState = new HashMap<>();
   private Map<UUID, Long> playerLastAttackTime = new HashMap<>();
   private Map<UUID, Long> playerLastAttackLogTime = new HashMap<>();
   private boolean servantMode = false;
   private UUID masterUuid;
   private UUID originalServantUuid;

   public PlayerGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world);
   }

   @Override
   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(PLAYER_UUID, "");
      this.dataTracker.startTracking(PLAYER_NAME, "");
   }

   public static PlayerGhostEntity createFromPlayer(PlayerEntity player, World world) {
      PlayerGhostEntity ghost = new PlayerGhostEntity(ModEntities.PLAYER_GHOST, world);
      ghost.initFromPlayer(player);
      return ghost;
   }

   public static PlayerGhostEntity createFromStoredData(PlayerEntity master, World world, UUID servantUuid) {
      PlayerGhostEntity ghost = new PlayerGhostEntity(ModEntities.PLAYER_GHOST, world);
      RoyalCurseData royalCurseData = PlayerRoyalCurseManager.getRoyalCurseData(master);
      RoyalCurseServantData servantData = null;

      for (int i = 0; i < royalCurseData.getServantCount(); i++) {
         RoyalCurseServantData data = royalCurseData.getServant(i);
         if (data != null && data.getUuid().equals(servantUuid)) {
            servantData = data;
            break;
         }
      }

      if (servantData != null) {
         ghost.setServantMode(true);
         ghost.setMasterUuid(master.getUuid());
         ghost.setOriginalServantUuid(servantUuid);
         ghost.setPlayerName(servantData.getPlayerName());
         ghost.setPlayerUuid(servantData.getPlayerUuid());
         ghost.setCustomName(Text.literal(servantData.getPlayerName()));
         ghost.setCustomNameVisible(true);
         ghost.setGhostDomainColor(servantData.getGhostDomainColor());
         ghost.setGhostDomainLevel(servantData.getGhostDomainLevel());
         ghost.setGhostDomainRadius(servantData.getGhostDomainRadius());
         ghost.setSpiritualResistance(servantData.getSpiritualResistance());
         ghost.setSpiritualDamage(servantData.getSpiritualDamage());
         ghost.setSpiritualStrength(servantData.getSpiritualStrength());
         ghost.setMaxSpiritualStrength(servantData.getMaxSpiritualStrength());
         ghost.setRecoveryFactor(servantData.getRecoveryFactor());
         ghost.disableGhostDomain();
         return ghost;
      } else {
         LOGGER.warn("从存储数据创建玩家鬼魂失败: 无法恢复奴仆数据 (主人: {}, 奴仆UUID: {})，使用默认设置", master.getName().getString(), servantUuid);
         ghost.setServantMode(true);
         ghost.setMasterUuid(master.getUuid());
         ghost.setCustomName(Text.literal("王家厉鬼"));
         ghost.setCustomNameVisible(true);
         ghost.setPlayerName("王家厉鬼");
         ghost.setGhostDomainColor(String.valueOf(8388736));
         ghost.setGhostDomainLevel(1);
         ghost.setGhostDomainRadius(8.0F);
         ghost.disableGhostDomain();
         ghost.setSpiritualStrength(100);
         ghost.setMaxSpiritualStrength(100);
         ghost.setSpiritualResistance(50);
         ghost.setSpiritualDamage(50);
         ghost.setRecoveryFactor(0.2F);
         LOGGER.info("使用默认设置创建玩家鬼魂奴仆 (主人: {})", master.getName().getString());
         return ghost;
      }
   }

   public static Builder createGhostAttributes() {
      return GhostEntity.createGhostAttributes();
   }

   private void initFromPlayer(PlayerEntity player) {
      this.setPlayerUuid(player.getUuidAsString());
      this.setPlayerName(player.getName().getString());
      this.setCustomName(player.getName());
      this.setCustomNameVisible(true);
      this.inheritPlayerAttributes(player);
      this.attackCooldown = 100;
   }

   private void inheritPlayerAttributes(PlayerEntity player) {
      NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
      if (spiritAttributes.contains("spiritResistance")) {
         float spiritResistance = this.getFloatFromNbt(spiritAttributes, "spiritResistance");
         this.setSpiritualResistance((int)spiritResistance);
      }

      if (spiritAttributes.contains("spiritDamage")) {
         float spiritDamage = this.getFloatFromNbt(spiritAttributes, "spiritDamage");
         this.setSpiritualDamage((int)spiritDamage);
      }

      if (spiritAttributes.contains("maxSpirit")) {
         float maxSpirit = this.getFloatFromNbt(spiritAttributes, "maxSpirit");
         this.setSpiritualStrength((int)maxSpirit);
         this.setMaxSpiritualStrength((int)maxSpirit);
      }

      if (spiritAttributes.contains("revivalFactor")) {
         float revivalFactor = this.getFloatFromNbt(spiritAttributes, "revivalFactor");
         this.setRecoveryFactor(revivalFactor);
      }

      this.inheritPlayerGhosts(player);
   }

   private float getFloatFromNbt(NbtCompound nbt, String key) {
      if (nbt.contains(key, 3)) {
         return nbt.getInt(key);
      } else {
         return nbt.contains(key, 6) ? (float)nbt.getDouble(key) : nbt.getFloat(key);
      }
   }

   public void setAttackCooldown(int cooldown) {
      this.attackCooldown = cooldown;
   }

   public NbtCompound getGhostSlotsData() {
      return this.ghostSlotsData;
   }

   public String getGhostDomainColor() {
      return this.ghostDomainColor;
   }

   public List<String> getPlayerGhosts() {
      return this.playerGhosts;
   }

   public List<String> getKillingRules() {
      return this.killingRules;
   }

   private void inheritPlayerGhosts(PlayerEntity player) {
      NbtCompound spiritData = PlayerEvents.getSpiritAttributes(player);
      if (spiritData.contains("GhostSlots")) {
         NbtCompound ghostSlots = spiritData.getCompound("GhostSlots");
         this.ghostSlotsData = ghostSlots.copy();

         for (int i = 0; i < 10; i++) {
            String slotKey = "Slot" + i;
            if (ghostSlots.contains(slotKey)) {
               NbtCompound slotData = ghostSlots.getCompound(slotKey);
               if (slotData.getBoolean("occupied") && slotData.contains("item")) {
                  ItemStack itemStack = ItemStack.fromNbt(slotData.getCompound("item"));
                  String ghostName = itemStack.getItem().getTranslationKey();
                  if (itemStack.getItem() instanceof SilentGhostEyeItem) {
                     this.playerGhosts.add("silent_ghost_eye");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "red";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭鬼眼，继承红色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭鬼眼，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("fog_ghost") || ghostName.contains("鬼雾")) {
                     this.playerGhosts.add("fog_ghost");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "fog";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭鬼雾，继承浓雾鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭鬼雾，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("ghost_fist")) {
                     this.playerGhosts.add("ghost_fist");
                     this.killingRules.add("attack_nearby_players");
                     LOGGER.info("玩家 {} 生前驾驭鬼拳，继承近战攻击能力", this.playerName);
                  } else if (ghostName.contains("ghost_fire")) {
                     this.playerGhosts.add("ghost_fire");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "green";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭鬼火，继承绿色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭鬼火，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("food_ghost") || ghostName.contains("食物鬼")) {
                     this.playerGhosts.add("food_ghost");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "black";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭食物鬼，继承黑色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭食物鬼，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("qiaomen_ghost") || ghostName.contains("敲门鬼")) {
                     this.playerGhosts.add("qiaomen_ghost");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "black";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭敲门鬼，继承黑色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭敲门鬼，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("block_ghost") || ghostName.contains("方块鬼")) {
                     this.playerGhosts.add("block_ghost");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "black";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭方块鬼，继承黑色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭方块鬼，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("villager_ghost") || ghostName.contains("村民鬼")) {
                     this.playerGhosts.add("villager_ghost");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "black";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭村民鬼，继承黑色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭村民鬼，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("ganshi_bride_ghost") || ghostName.contains("鬼新娘")) {
                     this.playerGhosts.add("ganshi_bride_ghost");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "black";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(2);
                        this.setGhostDomainRadius(32.0F);
                        LOGGER.info("玩家 {} 生前驾驭鬼新娘，继承黑色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭鬼新娘，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("ghost_wind") || ghostName.contains("鬼风")) {
                     this.playerGhosts.add("ghost_wind");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "cyan";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭鬼风，继承青色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭鬼风，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (ghostName.contains("ghost_officer") || ghostName.contains("鬼差")) {
                     this.playerGhosts.add("ghost_officer");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "black";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(64.0F);
                        LOGGER.info("玩家 {} 生前驾驭鬼差，继承黑色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭鬼差，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  } else if (!ghostName.contains("ghost_smoke") && !ghostName.contains("鬼烟")) {
                     String ghostType = GhostUtils.getGhostTypeFromItem(itemStack);
                     if (ghostType != null) {
                        this.playerGhosts.add(ghostType);
                        this.addKillingRuleForGhost(ghostType);
                        LOGGER.info("玩家 {} 生前驾驭 {}，继承相应杀人规律", this.playerName, ghostType);
                     } else {
                        this.playerGhosts.add(ghostName);
                        this.addKillingRuleForGhost(ghostName);
                        LOGGER.warn("无法获取物品 {} 的鬼类型，使用翻译键作为替代", ghostName);
                     }
                  } else {
                     this.playerGhosts.add("ghost_smoke");
                     if (this.ghostDomainColor.equals("none")) {
                        this.ghostDomainColor = "gray";
                        this.hasGhostDomain = true;
                        this.setGhostDomainLevel(3);
                        this.setGhostDomainRadius(48.0F);
                        LOGGER.info("玩家 {} 生前驾驭鬼烟，继承灰色鬼域能力", this.playerName);
                     } else {
                        LOGGER.info("玩家 {} 生前驾驭鬼烟，但已有鬼域效果，不重复设置", this.playerName);
                     }
                  }
               }
            }
         }
      }

      int validSlotCount = 0;

      for (int i = 0; i < 10; i++) {
         String slotKey = "Slot" + i;
         if (this.ghostSlotsData.contains(slotKey)) {
            NbtCompound slotData = this.ghostSlotsData.getCompound(slotKey);
            if (slotData.getBoolean("occupied")) {
               validSlotCount++;
            }
         }
      }

      LOGGER.info(
         "玩家 {} 的鬼魂继承完成，驾驭鬼: {}, 杀人规律: {}, 鬼域效果: {}, 槽位数据: {}/{}",
         this.playerName,
         this.playerGhosts,
         this.killingRules,
         this.ghostDomainColor,
         validSlotCount,
         10
      );
   }

   private void addKillingRuleForGhost(String ghostName) {
      if (ghostName.contains("look_up_ghost") || ghostName.contains("抬头鬼")) {
         this.killingRules.add("kill_looking_up");
      }

      if (ghostName.contains("look_down_ghost") || ghostName.contains("低头鬼")) {
         this.killingRules.add("kill_looking_down");
      }

      if (ghostName.contains("jump_ghost") || ghostName.contains("跳跃鬼")) {
         this.killingRules.add("kill_jumping");
      }

      if (ghostName.contains("block_ghost") || ghostName.contains("方块鬼")) {
         this.killingRules.add("kill_placing_blocks");
      }

      if (ghostName.contains("food_ghost") || ghostName.contains("食物鬼")) {
         this.killingRules.add("kill_eating");
      }

      if (ghostName.contains("fog_ghost") || ghostName.contains("鬼雾")) {
         this.killingRules.add("kill_in_fog");
      }

      if (ghostName.contains("lost_ghost") || ghostName.contains("迷失鬼")) {
         this.killingRules.add("kill_lost");
      }

      if (ghostName.contains("death_sight_ghost") || ghostName.contains("死亡视线鬼")) {
         this.killingRules.add("kill_looking_at");
      }

      if (ghostName.contains("water_ghost") || ghostName.contains("水鬼")) {
         this.killingRules.add("kill_in_water");
      }

      if (ghostName.contains("giant_shadow_ghost") || ghostName.contains("高大鬼影") || ghostName.contains("complete_shadow_ghost") || ghostName.contains("完整鬼影")) {
         this.killingRules.add("immune_normal_attack");
      }

      if (ghostName.contains("ganshi_bride_ghost") || ghostName.contains("鬼新娘")) {
         this.killingRules.add("control_corpses");
      }

      if (ghostName.contains("crop_ghost") || ghostName.contains("作物鬼")) {
         this.killingRules.add("kill_harvesting");
      }

      if (ghostName.contains("step_ghost") || ghostName.contains("踩踏鬼")) {
         this.killingRules.add("kill_stepping");
      }

      if (ghostName.contains("grave_earth_ghost") || ghostName.contains("坟土鬼")) {
         this.killingRules.add("kill_statue");
      }

      if (ghostName.contains("trash_ghost") || ghostName.contains("垃圾鬼")) {
         this.killingRules.add("kill_throwing_items");
      }

      if (ghostName.contains("ghost_wind") || ghostName.contains("鬼风")) {
         this.killingRules.add("kill_in_wind");
      }

      if (ghostName.contains("ghost_officer") || ghostName.contains("鬼差")) {
         this.killingRules.add("attack_nearby_players");
      }

      if (ghostName.contains("ghost_smoke") || ghostName.contains("鬼烟")) {
         this.killingRules.add("kill_in_smoke");
      }

      if (!ghostName.contains("qiaomen_ghost") && !ghostName.contains("敲门鬼")) {
         this.killingRules.add("attack_nearby_players");
      }
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.playerUuid != null) {
         nbt.putString("PlayerUuid", this.playerUuid);
      }

      if (this.playerName != null) {
         nbt.putString("PlayerName", this.playerName);
      }

      if (this.originalServantUuid != null) {
         nbt.putUuid("OriginalServantUuid", this.originalServantUuid);
      }

      if (!this.playerGhosts.isEmpty()) {
         NbtCompound ghostsNbt = new NbtCompound();

         for (int i = 0; i < this.playerGhosts.size(); i++) {
            ghostsNbt.putString("Ghost" + i, this.playerGhosts.get(i));
         }

         nbt.put("PlayerGhosts", ghostsNbt);
      }

      nbt.putString("GhostDomainColor", this.ghostDomainColor);
      if (!this.killingRules.isEmpty()) {
         NbtCompound rulesNbt = new NbtCompound();

         for (int i = 0; i < this.killingRules.size(); i++) {
            rulesNbt.putString("Rule" + i, this.killingRules.get(i));
         }

         nbt.put("KillingRules", rulesNbt);
      }

      if (!this.ghostSlotsData.isEmpty()) {
         nbt.put("GhostSlotsData", this.ghostSlotsData);
      }
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("PlayerUuid")) {
         this.playerUuid = nbt.getString("PlayerUuid");
         if (this.dataTracker != null) {
            this.dataTracker.set(PLAYER_UUID, this.playerUuid != null ? this.playerUuid : "");
         }
      }

      if (nbt.contains("PlayerName")) {
         this.playerName = nbt.getString("PlayerName");
         if (this.dataTracker != null) {
            this.dataTracker.set(PLAYER_NAME, this.playerName != null ? this.playerName : "");
         }
      }

      if (nbt.contains("OriginalServantUuid")) {
         this.originalServantUuid = nbt.getUuid("OriginalServantUuid");
      }

      this.playerGhosts.clear();
      if (nbt.contains("PlayerGhosts")) {
         NbtCompound ghostsNbt = nbt.getCompound("PlayerGhosts");

         for (String key : ghostsNbt.getKeys()) {
            this.playerGhosts.add(ghostsNbt.getString(key));
         }
      }

      if (nbt.contains("GhostDomainColor")) {
         this.ghostDomainColor = nbt.getString("GhostDomainColor");
      }

      this.killingRules.clear();
      if (nbt.contains("KillingRules")) {
         NbtCompound rulesNbt = nbt.getCompound("KillingRules");

         for (String key : rulesNbt.getKeys()) {
            this.killingRules.add(rulesNbt.getString(key));
         }
      }

      if (nbt.contains("GhostSlotsData")) {
         this.ghostSlotsData = nbt.getCompound("GhostSlotsData");
      }
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      switch (this.ghostDomainColor) {
         case "red":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.RED_GHOST_DOMAIN_TARGET, 30, this.getGhostDomainLevel() - 1, false, false, false));
            break;
         case "green":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.GREEN_GHOST_DOMAIN_TARGET, 30, this.getGhostDomainLevel() - 1, false, false, false));
            break;
         case "blue":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.BLUE_GHOST_DOMAIN_TARGET, 30, this.getGhostDomainLevel() - 1, false, false, false));
            break;
         case "gray":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.GRAY_GHOST_DOMAIN_TARGET, 20, this.getGhostDomainLevel() - 1, false, false, false));
            break;
         case "golden":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET, 30, this.getGhostDomainLevel() - 1, false, false, false));
            break;
         case "blindness":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 20, 0, false, false));
            break;
         case "fog":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, 20, 0, false, false));
            break;
         case "purple":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.PURPLE_GHOST_DOMAIN_TARGET, 30, this.getGhostDomainLevel() - 1, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(ModEffects.PURPLE_GHOST_DOMAIN_VISUAL, 30, this.getGhostDomainLevel() - 1, false, false, false));
            break;
         case "black":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 30, 0, false, false));
            break;
         case "cyan":
            player.addStatusEffect(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 30, this.getGhostDomainLevel() - 1, false, false, false));
            break;
         default:
            super.applyDefaultEffects(player);
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (this.isSuppressed()) {
         return false;
      }

      if (this.attackCooldown > 0) {
         return false;
      }

      if (this.getMasterUuid() != null && this.getMasterUuid().equals(player.getUuid())) {
         return false;
      }

      if (player instanceof PlayerEntity) {
         GhostChildData ghostChildData = PlayerGhostChildManager.getGhostChildData(player);
         if (ghostChildData.isSummoned() && this.getMasterUuid() != null && this.getMasterUuid().equals(player.getUuid())) {
            return false;
         }
      }

      UUID playerId = player.getUuid();
      long currentTime = this.getWorld().getTime();
      Long lastAttackLogTime = this.playerLastAttackLogTime.get(playerId);
      boolean inLogCooldown = lastAttackLogTime != null && currentTime - lastAttackLogTime < 20L;
      boolean shouldAttack = false;

      for (String ghostName : this.playerGhosts) {
         GhostEntity ghostProxy = this.createGhostProxy(ghostName);
         if (ghostProxy != null) {
            ghostProxy.setPosition(this.getPos());
            ghostProxy.setGhostDomainRadius(this.getGhostDomainRadius());
            if (ghostProxy.shouldAttackPlayer(player)) {
               shouldAttack = true;
               this.playerLastAttackTime.put(playerId, currentTime);
               if (!inLogCooldown) {
                  if (ghostName.contains("jump_ghost") || ghostName.contains("跳跃鬼")) {
                     LOGGER.info("玩家鬼魂 {} 触发跳跃鬼规律攻击：玩家 {} 在鬼域内跳跃", this.playerName, player.getName().getString());
                  } else if (ghostName.contains("block_ghost") || ghostName.contains("方块鬼")) {
                     LOGGER.info("玩家鬼魂 {} 触发方块鬼规律攻击：玩家 {} 在鬼域内放置/破坏方块", this.playerName, player.getName().getString());
                  } else if (ghostName.contains("taitou_ghost") || ghostName.contains("抬头鬼")) {
                     LOGGER.info("玩家鬼魂 {} 触发抬头鬼规律攻击：玩家 {} 在鬼域内抬头", this.playerName, player.getName().getString());
                  } else if (ghostName.contains("ditou_ghost") || ghostName.contains("低头鬼")) {
                     LOGGER.info("玩家鬼魂 {} 触发低头鬼规律攻击：玩家 {} 在鬼域内低头", this.playerName, player.getName().getString());
                  } else if (ghostName.contains("food_ghost") || ghostName.contains("食物鬼")) {
                     LOGGER.info("玩家鬼魂 {} 触发食物鬼规律攻击：玩家 {} 在鬼域内进食", this.playerName, player.getName().getString());
                  } else if (ghostName.contains("fog_ghost") || ghostName.contains("鬼雾")) {
                     LOGGER.info("玩家鬼魂 {} 触发鬼雾规律攻击：玩家 {} 在鬼域内移动", this.playerName, player.getName().getString());
                  } else if (ghostName.contains("lost_ghost") || ghostName.contains("遗忘鬼")) {
                     LOGGER.info("玩家鬼魂 {} 触发遗忘鬼规律攻击：玩家 {} 在鬼域内迷失自己", this.playerName, player.getName().getString());
                  } else if (!ghostName.contains("death_sight_ghost") && !ghostName.contains("死亡视线鬼")) {
                     LOGGER.info("玩家鬼魂 {} 触发 {} 规律攻击：玩家 {} 符合攻击条件", this.playerName, ghostName, player.getName().getString());
                  } else {
                     LOGGER.info("玩家鬼魂 {} 触发死亡视线鬼规律攻击：玩家 {} 与鬼对视", this.playerName, player.getName().getString());
                  }

                  this.playerLastAttackLogTime.put(playerId, currentTime);
               }
               break;
            }
         }
      }

      if (!shouldAttack && this.getGhostDomainLevel() >= 3) {
         if (this.getMasterUuid() != null && this.getMasterUuid().equals(player.getUuid())) {
            return shouldAttack;
         }

         Long lastAttackTime = this.playerLastAttackTime.get(playerId);
         if (this.squaredDistanceTo(player) <= this.getGhostDomainRadius() * this.getGhostDomainRadius()
            && (lastAttackTime == null || currentTime - lastAttackTime >= 1200L)) {
            shouldAttack = true;
            this.playerLastAttackTime.put(playerId, currentTime);
            if (!inLogCooldown) {
               if (lastAttackTime == null) {
                  LOGGER.info("玩家鬼魂 {} 触发首次60秒无规律攻击：玩家 {} 进入鬼域后60秒未触发任何规律", this.playerName, player.getName().getString());
               } else {
                  long timeSinceLastAttack = currentTime - lastAttackTime;
                  LOGGER.info("玩家鬼魂 {} 触发60秒无规律攻击：玩家 {} 距离上次攻击已过去 {} 秒", this.playerName, player.getName().getString(), timeSinceLastAttack / 20L);
               }

               this.playerLastAttackLogTime.put(playerId, currentTime);
            }
         }
      }

      return shouldAttack;
   }

   private GhostEntity createGhostProxy(String ghostName) {
      try {
         if (ghostName.contains("jump_ghost") || ghostName.contains("跳跃鬼")) {
            return new JumpGhostEntity(ModEntities.JUMP_GHOST, this.getWorld());
         }

         if (ghostName.contains("block_ghost") || ghostName.contains("方块鬼")) {
            return new BlockGhostEntity(ModEntities.BLOCK_GHOST, this.getWorld());
         }

         if (ghostName.contains("taitou_ghost") || ghostName.contains("抬头鬼")) {
            return new TaitouGhostEntity(ModEntities.TAITOU_GHOST, this.getWorld());
         }

         if (ghostName.contains("ditou_ghost") || ghostName.contains("低头鬼")) {
            return new DitouGhostEntity(ModEntities.DITOU_GHOST, this.getWorld());
         }

         if (ghostName.contains("food_ghost") || ghostName.contains("食物鬼")) {
            return new FoodGhostEntity(ModEntities.FOOD_GHOST, this.getWorld());
         }

         if (ghostName.contains("fog_ghost") || ghostName.contains("鬼雾")) {
            return new FogGhostEntity(ModEntities.FOG_GHOST, this.getWorld());
         }

         if (ghostName.contains("lost_ghost") || ghostName.contains("迷失鬼")) {
            return new LostGhostEntity(ModEntities.LOST_GHOST, this.getWorld());
         }

         if (ghostName.contains("death_sight_ghost") || ghostName.contains("死亡视线鬼")) {
            return new DeathSightGhostEntity(ModEntities.DEATH_SIGHT_GHOST, this.getWorld());
         }

         if (ghostName.contains("ghost_merchant") || ghostName.contains("鬼商人")) {
            return new GhostMerchantEntity(ModEntities.GHOST_MERCHANT, this.getWorld());
         }

         if (ghostName.contains("villager_ghost") || ghostName.contains("村民鬼")) {
            return new VillagerGhostEntity(ModEntities.VILLAGER_GHOST, this.getWorld());
         }

         if (ghostName.contains("box_ghost") || ghostName.contains("开箱鬼")) {
            return new BoxGhostEntity(ModEntities.BOX_GHOST, this.getWorld());
         }

         if (ghostName.contains("water_ghost") || ghostName.contains("水鬼")) {
            return new WaterGhostEntity(ModEntities.WATER_GHOST, this.getWorld());
         }

         if (ghostName.contains("giant_shadow_ghost")
            || ghostName.contains("高大鬼影")
            || ghostName.contains("complete_shadow_ghost")
            || ghostName.contains("完整鬼影")) {
            return new GiantShadowGhostEntity(ModEntities.GIANT_SHADOW_GHOST, this.getWorld());
         }

         if (ghostName.contains("ganshi_bride_ghost") || ghostName.contains("鬼新娘")) {
            return new GanshiBrideGhostEntity(ModEntities.GANSHI_BRIDE_GHOST, this.getWorld());
         }

         if (ghostName.contains("crop_ghost") || ghostName.contains("作物鬼")) {
            return new CropGhostEntity(ModEntities.CROP_GHOST, this.getWorld());
         }

         if (ghostName.contains("step_ghost") || ghostName.contains("踩踏鬼")) {
            return new StepGhostEntity(ModEntities.STEP_GHOST, this.getWorld());
         }

         if (ghostName.contains("grave_earth_ghost") || ghostName.contains("坟土鬼")) {
            return new GraveEarthGhostEntity(ModEntities.GRAVE_EARTH_GHOST, this.getWorld());
         }

         if (ghostName.contains("trash_ghost") || ghostName.contains("垃圾鬼")) {
            return new TrashGhostEntity(ModEntities.TRASH_GHOST, this.getWorld());
         }

         if (ghostName.contains("burn_ghost") || ghostName.contains("烧死鬼")) {
            return new BurnGhostEntity(ModEntities.BURN_GHOST, this.getWorld());
         }

         if (ghostName.contains("ghost_wind") || ghostName.contains("鬼风")) {
            return new GhostWindEntity(ModEntities.GHOST_WIND, this.getWorld());
         }

         if (ghostName.contains("ghost_smoke") || ghostName.contains("鬼烟")) {
            return new GhostSmokeEntity(ModEntities.GHOST_SMOKE, this.getWorld());
         }

         if (ghostName.contains("qiaomen_ghost") || ghostName.contains("敲门鬼")) {
            return null;
         }

         if (ghostName.contains("ghost_officer") || ghostName.contains("鬼差")) {
            return null;
         }

         if (ghostName.contains("untouchable_ghost") || ghostName.contains("不可触摸鬼")) {
            return null;
         }
      } catch (Exception e) {
         LOGGER.warn("创建鬼代理实例失败: {}, 错误: {}", ghostName, e.getMessage());
      }

      return null;
   }

   @Override
   public void setSuppressed(boolean suppressed) {
      super.setSuppressed(suppressed);
      if (suppressed) {
         this.setMovementSpeed(0.0F);
         this.setAttackCooldown(0);
      } else {
         this.setMovementSpeed(0.3F);
      }
   }

   public boolean retrieveCoffinNail() {
      if (this.hasCoffinNail()) {
         ItemStack coffinNail = new ItemStack(ModItems.COFFIN_NAIL);
         this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), coffinNail));
         this.setSuppressed(false);
         this.setCoffinNail(null);
         this.enableGhostDomain();
         this.setGhostDomainRadius(48.0F);
         this.setGhostDomainLevel(3);
         LOGGER.info("玩家鬼魂 {} 的棺材钉被取回，鬼域已恢复", this.playerName);
         return true;
      } else {
         return false;
      }
   }

   public String getPlayerName() {
      if (this.getWorld() != null && this.getWorld().isClient) {
         String trackedName = (String)this.dataTracker.get(PLAYER_NAME);
         return trackedName.isEmpty() ? this.playerName : trackedName;
      } else {
         return this.playerName;
      }
   }

   public void setPlayerName(String playerName) {
      this.playerName = playerName;
      if (this.dataTracker != null) {
         this.dataTracker.set(PLAYER_NAME, playerName != null ? playerName : "");
      }
   }

   public String getPlayerUuid() {
      if (this.getWorld() != null && this.getWorld().isClient) {
         String trackedUuid = (String)this.dataTracker.get(PLAYER_UUID);
         return trackedUuid.isEmpty() ? this.playerUuid : trackedUuid;
      } else {
         return this.playerUuid;
      }
   }

   public void setPlayerUuid(String playerUuid) {
      this.playerUuid = playerUuid;
      if (this.dataTracker != null) {
         this.dataTracker.set(PLAYER_UUID, playerUuid != null ? playerUuid : "");
      }
   }

   public void setGhostDomainColor(String ghostDomainColor) {
      this.ghostDomainColor = ghostDomainColor;
   }

   public void setServantMode(boolean servantMode) {
      this.servantMode = servantMode;
      if (servantMode && this.getMasterUuid() != null) {
         this.disableGhostDomain();
      }
   }

   public void setMasterUuid(UUID masterUuid) {
      this.masterUuid = masterUuid;
      if (this.isServantMode() && masterUuid != null) {
         this.disableGhostDomain();
      }
   }

   public boolean isServantMode() {
      return this.servantMode;
   }

   public UUID getMasterUuid() {
      return this.masterUuid;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      if (!super.shouldAttackGhost(ghost)) {
         return false;
      } else if (ghost instanceof GhostChildEntity ghostChild
         && ghostChild.getOwner() != null
         && this.isServantMode()
         && this.getMasterUuid() != null
         && this.getMasterUuid().equals(ghostChild.getOwner().getUuid())) {
         return false;
      } else {
         return ghost instanceof PlayerGhostEntity playerGhost
               && playerGhost.isServantMode()
               && this.isServantMode()
               && this.getMasterUuid() != null
               && playerGhost.getMasterUuid() != null
            ? !this.getMasterUuid().equals(playerGhost.getMasterUuid())
            : true;
      }
   }

   public void setOriginalServantUuid(UUID originalServantUuid) {
      this.originalServantUuid = originalServantUuid;
   }

   public UUID getOriginalServantUuid() {
      return this.originalServantUuid;
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return this.isServantMode() && this.getMasterUuid() != null;
   }

   @Override
   protected boolean isHasGhostDomain() {
      return !this.isServantMode() || this.getMasterUuid() == null;
   }

   @Override
   public boolean isGhostDomainEnabled() {
      return !this.isServantMode() || this.getMasterUuid() == null;
   }

   @Override
   public void setGhostDomainEnabled(boolean enabled) {
      if (!this.isServantMode() || this.getMasterUuid() == null) {
         super.setGhostDomainEnabled(enabled);
      }
   }

   @Override
   public void enableGhostDomain() {
      if (!this.isServantMode() || this.getMasterUuid() == null) {
         super.enableGhostDomain();
      }
   }

   @Override
   public void disableGhostDomain() {
      super.disableGhostDomain();
   }

   public boolean hasCustomName() {
      return this.playerName != null;
   }

   public Text getCustomName() {
      return (Text)(this.playerName != null ? Text.literal(this.playerName) : super.getCustomName());
   }

   public boolean canTarget(LivingEntity target) {
      if (this.isServantMode() && this.getMasterUuid() != null && target instanceof PlayerEntity) {
         PlayerEntity master = this.getWorld().getPlayerByUuid(this.getMasterUuid());
         if (master != null && target.getUuid().equals(master.getUuid())) {
            LOGGER.debug("奴仆 {} 跳过攻击主人 {}", this.playerName, master.getName().getString());
            return false;
         }
      }

      return super.canTarget(target);
   }

   @Override
   public void setTarget(@Nullable LivingEntity target) {
      if (this.isServantMode() && this.getMasterUuid() != null && target instanceof PlayerEntity) {
         PlayerEntity master = this.getWorld().getPlayerByUuid(this.getMasterUuid());
         if (master != null && target.getUuid().equals(master.getUuid())) {
            LOGGER.debug("奴仆 {} 拒绝将主人 {} 设为目标", this.playerName, master.getName().getString());
            return;
         }
      }

      super.setTarget(target);
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient && this.attackCooldown > 0) {
         this.attackCooldown--;
         if (this.attackCooldown <= 0) {
            LOGGER.debug("玩家鬼魂 {} 攻击冷却结束", this.playerName);
         }
      }

      if (!this.getWorld().isClient && this.isServantMode() && this.getMasterUuid() != null && !this.isDeadlocked() && !this.isSuppressed()) {
         PlayerEntity master = this.getWorld().getPlayerByUuid(this.getMasterUuid());
         if (master != null && master.isAlive() && !master.isSpectator()) {
            double distanceToMaster = this.squaredDistanceTo(master);
            double actualDistance = Math.sqrt(distanceToMaster);
            if (!this.isSuppressed() && !this.isDeadlocked()) {
               double searchRadius = 25.0;
               List<LivingEntity> potentialTargets = new ArrayList<>();
               List<HostileEntity> hostileMobs = this.getWorld()
                  .getEntitiesByClass(
                     HostileEntity.class,
                     this.getBoundingBox().expand(searchRadius),
                     mob -> mob.isAlive() && this.squaredDistanceTo(mob) <= searchRadius * searchRadius
                  );
               potentialTargets.addAll(hostileMobs);
               List<GhostEntity> otherGhosts = this.getWorld()
                  .getEntitiesByClass(
                     GhostEntity.class,
                     this.getBoundingBox().expand(searchRadius),
                     ghost -> ghost != this
                        && ghost.isAlive()
                        && this.squaredDistanceTo(ghost) <= searchRadius * searchRadius
                        && (
                           !(ghost instanceof PlayerGhostEntity)
                              || ghost instanceof PlayerGhostEntity && ((PlayerGhostEntity)ghost).getMasterUuid() == null
                              || !((PlayerGhostEntity)ghost).getMasterUuid().equals(master.getUuid())
                        )
                  );
               potentialTargets.addAll(otherGhosts);
               if (!potentialTargets.isEmpty()) {
                  potentialTargets.sort((a, b) -> Double.compare(this.squaredDistanceTo(a), this.squaredDistanceTo(b)));
                  LivingEntity target = potentialTargets.get(0);
                  this.setTarget(target);
                  double distanceToTarget = this.squaredDistanceTo(target);
                  if (distanceToTarget <= 256.0 && this.attackCooldown <= 0) {
                     if (target instanceof PlayerEntity) {
                        this.executeAttack((PlayerEntity)target);
                     } else if (target instanceof GhostEntity) {
                        if (this.shouldAttackGhost((GhostEntity)target)) {
                           this.executeAttack((GhostEntity)target);
                        }
                     } else if (target instanceof LivingEntity) {
                        this.executeAttack(target);
                     } else {
                        this.tryAttack(target);
                        this.attackCooldown = 100;
                     }
                  } else if (distanceToTarget > 25.0) {
                     Vec3d direction = new Vec3d(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ()).normalize();
                     float moveSpeed = 0.3F;
                     this.setVelocity(direction.multiply(moveSpeed));
                     this.setYaw((float)Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F);
                  }
               } else if (distanceToMaster > 1225.0) {
                  this.teleport(
                     master.getX() + (this.getRandom().nextDouble() - 0.5) * 2.0, master.getY(), master.getZ() + (this.getRandom().nextDouble() - 0.5) * 2.0
                  );
               } else if (distanceToMaster > 25.0) {
                  Vec3d direction = new Vec3d(master.getX() - this.getX(), master.getY() - this.getY(), master.getZ() - this.getZ()).normalize();
                  float baseSpeed = 0.1F;
                  float distanceFactor = (float)(actualDistance - 5.0) * 0.05F;
                  float followSpeed = Math.min(baseSpeed + distanceFactor, 0.5F);
                  this.setVelocity(direction.multiply(followSpeed));
                  this.setYaw((float)Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F);
               } else if (distanceToMaster < 9.0) {
                  this.setVelocity(Vec3d.ZERO);
               }
            } else if (distanceToMaster > 900.0) {
               this.teleport(
                  master.getX() + (this.getRandom().nextDouble() - 0.5) * 2.0, master.getY(), master.getZ() + (this.getRandom().nextDouble() - 0.5) * 2.0
               );
               LOGGER.debug("奴仆 {} 距离主人过远，已传送至主人身边", this.playerName);
            } else if (distanceToMaster > 25.0) {
               Vec3d direction = new Vec3d(master.getX() - this.getX(), master.getY() - this.getY(), master.getZ() - this.getZ()).normalize();
               float baseSpeed = 0.1F;
               float distanceFactor = (float)(actualDistance - 5.0) * 0.05F;
               float followSpeed = Math.min(baseSpeed + distanceFactor, 0.5F);
               this.setVelocity(direction.multiply(followSpeed));
               this.setYaw((float)Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F);
            } else if (distanceToMaster < 9.0) {
               this.setVelocity(Vec3d.ZERO);
            }
         } else {
            if (this.age % 100 == 0) {
               LOGGER.info("奴仆 {} 的主人不存在或已死亡，自动消失", this.playerName);
            }

            GhostDeathHandler.markLegitimateRemoval(this);
            this.discard();
         }
      }

      if (!this.getWorld().isClient && this.isServantMode() && this.getMasterUuid() != null && (this.isDeadlocked() || this.isSuppressed())) {
         ServerWorld serverWorld = (ServerWorld)this.getWorld();
         Vec3d pos = this.getPos();

         for (int i = 0; i < 5; i++) {
            double offsetX = this.getRandom().nextDouble() - 0.5;
            double offsetY = (this.getRandom().nextDouble() - 0.5) * 2.0 + 1.0;
            double offsetZ = this.getRandom().nextDouble() - 0.5;
            serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.1, 0.0, 0.0);
         }
      }

      if (this.age % 200 == 0) {
         int initialJumpingSize = this.playerJumpingState.size();
         int initialAttackSize = this.playerLastAttackTime.size();
         this.playerJumpingState.keySet().removeIf(playerId -> {
            PlayerEntity player = this.getWorld().getPlayerByUuid(playerId);
            return player == null || !player.isAlive() || this.squaredDistanceTo(player) > this.getGhostDomainRadius() * this.getGhostDomainRadius() * 4.0F;
         });
         this.playerLastAttackTime.keySet().removeIf(playerId -> {
            PlayerEntity player = this.getWorld().getPlayerByUuid(playerId);
            return player == null || !player.isAlive() || this.squaredDistanceTo(player) > this.getGhostDomainRadius() * this.getGhostDomainRadius() * 4.0F;
         });
         if (initialJumpingSize > this.playerJumpingState.size()) {
            LOGGER.debug("清理了 {} 个玩家跳跃状态数据", initialJumpingSize - this.playerJumpingState.size());
         }

         if (initialAttackSize > this.playerLastAttackTime.size()) {
            LOGGER.debug("清理了 {} 个玩家攻击时间数据", initialAttackSize - this.playerLastAttackTime.size());
         }
      }
   }
}
