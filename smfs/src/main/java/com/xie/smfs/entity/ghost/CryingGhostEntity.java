package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class CryingGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 36.0;
   private static final char TERROR_LEVEL = 'B';
   private int cryingPhaseTicks = 0;
   private static final int INITIAL_ATTACK_COOLDOWN = 1200;
   private static final int ATTACK_COOLDOWN = 1200;
   private static final int CRYING_PHASE_DURATION = 200;
   private static final double ATTACK_DISTANCE = 8.0;
   private static final TrackedData<Boolean> IS_IN_CRYING_PHASE = DataTracker.registerData(CryingGhostEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private int cryingDamageTickCounter = 0;
   private int cryingDamageLevel = 0;

   public CryingGhostEntity(EntityType<CryingGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 36.0, 'B', 2500, 110, 70, 0.4F);
      this.ghostLevel = 5;
      this.attackCooldown = 1200;
      this.setEnableChaseAfterRule(false);
      this.initCryingGhostAttributes();
      if (!world.isClient) {
         this.playSound(ModSounds.CRYING_GHOST_ENTRANCE, 1.0F, 1.0F);
         LOGGER.info("哭坟鬼实体生成，播放出场音效: entity.crying_ghost.entrance, 位置: ({}, {}, {})", this.getX(), this.getY(), this.getZ());
      }
   }

   private void initCryingGhostAttributes() {
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.25);
      }

      EntityAttributeInstance attackDamageAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.setBaseValue(12.0);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(36.0);
      }
   }

   @Override
   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(IS_IN_CRYING_PHASE, false);
   }

   public boolean isInCryingPhase() {
      return (Boolean)this.dataTracker.get(IS_IN_CRYING_PHASE);
   }

   public void setInCryingPhase(boolean inCryingPhase) {
      this.dataTracker.set(IS_IN_CRYING_PHASE, inCryingPhase);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isInCryingPhase()) {
         this.setMovementDisabled(true);
      } else {
         this.setMovementDisabled(false);
      }

      if (this.isInCryingPhase()) {
         this.handSwinging = true;
      } else {
         this.handSwinging = false;
      }

      if (!this.getWorld().isClient) {
         if (this.attackCooldown > 0 && !this.isInCryingPhase()) {
            this.attackCooldown--;
         }

         if (this.isInCryingPhase()) {
            this.cryingPhaseTicks--;
            this.cryingDamageTickCounter++;
            if (this.cryingDamageTickCounter >= 20) {
               this.cryingDamageTickCounter = 0;
               this.cryingDamageLevel++;
               this.applyCryingDamageToAllPlayers();
            }

            if (this.cryingPhaseTicks <= 0) {
               this.executeActualAttack();
            }
         }

         if (this.isSuppressed() || this.isDeadlocked()) {
            if (this.isInCryingPhase()) {
               this.cancelCryingPhase();
            }

            return;
         }

         PlayerEntity targetPlayer = this.findTargetPlayer();
         if (targetPlayer != null) {
            this.handlePlayerInteraction(targetPlayer);
         }
      }
   }

   private PlayerEntity findTargetPlayer() {
      List<PlayerEntity> nearbyPlayers = this.getWorld()
         .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(36.0), player -> player.isAlive() && !player.isSpectator());
      return !nearbyPlayers.isEmpty() ? nearbyPlayers.get(0) : null;
   }

   private void handlePlayerInteraction(PlayerEntity player) {
      if (this.attackCooldown <= 0 && !this.isInCryingPhase()) {
         this.startCryingPhase();
      }
   }

   private void startCryingPhase() {
      this.setInCryingPhase(true);
      this.cryingPhaseTicks = 200;
      this.cryingDamageTickCounter = 0;
      this.cryingDamageLevel = 0;
      this.handSwinging = true;
      LOGGER.info("哭坟鬼开始哭泣阶段，持续时间: {} ticks", 200);
   }

   private void cancelCryingPhase() {
      this.setInCryingPhase(false);
      this.cryingPhaseTicks = 0;
      this.cryingDamageTickCounter = 0;
      this.cryingDamageLevel = 0;
      this.handSwinging = false;
      LOGGER.info("哭坟鬼取消哭泣阶段");
   }

   private void applyCryingDamageToAllPlayers() {
      List<PlayerEntity> nearbyPlayers = this.getWorld()
         .getEntitiesByClass(
            PlayerEntity.class,
            this.getBoundingBox().expand(36.0),
            playerx -> playerx.isAlive() && !playerx.isSpectator() && !playerx.hasStatusEffect(ModEffects.DEAFNESS)
         );
      float damage = 10.0F + this.cryingDamageLevel * 2.0F;
      DamageSource damageSource = ModDamageSources.ghost(this.getWorld());

      for (PlayerEntity player : nearbyPlayers) {
         if (!player.isRemoved()) {
            PlayerEvents.handleSpiritDamage(player, damage, damage, damageSource);
            LOGGER.info("哭坟鬼对玩家 {} 造成 {} 点灵异伤害（伤害等级: {}）", player.getName().getString(), damage, this.cryingDamageLevel);
         }
      }
   }

   private void executeActualAttack() {
      this.executeDirectKill();
      LOGGER.info("哭坟鬼哭泣阶段结束，执行最终攻击");
      this.resetCryingState();
   }

   private void executeDirectKill() {
      List<PlayerEntity> nearbyPlayers = this.getWorld()
         .getEntitiesByClass(
            PlayerEntity.class,
            this.getBoundingBox().expand(36.0),
            playerx -> playerx.isAlive() && !playerx.isSpectator() && !playerx.hasStatusEffect(ModEffects.DEAFNESS)
         );
      DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
      float lethalDamage = this.getSpiritualDamage() * 5.0F;

      for (PlayerEntity player : nearbyPlayers) {
         if (player.isAlive() && !player.isRemoved()) {
            PlayerEvents.handleSpiritDamage(player, lethalDamage, lethalDamage, damageSource);
            LOGGER.info("哭坟鬼直接秒杀玩家: {}，造成 {} 点灵异伤害（基于自身灵异伤害 {} 的5倍）", player.getName().getString(), lethalDamage, this.getSpiritualDamage());
         }
      }
   }

   private void resetCryingState() {
      this.setInCryingPhase(false);
      this.cryingPhaseTicks = 0;
      this.cryingDamageTickCounter = 0;
      this.cryingDamageLevel = 0;
      this.attackCooldown = 1200;
      this.handSwinging = false;
      LOGGER.info("哭坟鬼哭泣阶段结束，进入冷却");
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return false;
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      return false;
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("IsInCryingPhase", this.isInCryingPhase());
      nbt.putInt("CryingPhaseTicks", this.cryingPhaseTicks);
      nbt.putInt("CryingDamageLevel", this.cryingDamageLevel);
      nbt.putInt("CryingDamageTickCounter", this.cryingDamageTickCounter);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("IsInCryingPhase")) {
         this.setInCryingPhase(nbt.getBoolean("IsInCryingPhase"));
      }

      if (nbt.contains("CryingPhaseTicks")) {
         this.cryingPhaseTicks = nbt.getInt("CryingPhaseTicks");
      }

      if (nbt.contains("CryingDamageLevel")) {
         this.cryingDamageLevel = nbt.getInt("CryingDamageLevel");
      }

      if (nbt.contains("CryingDamageTickCounter")) {
         this.cryingDamageTickCounter = nbt.getInt("CryingDamageTickCounter");
      }
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      if (this.isInCryingPhase()) {
         LOGGER.info("哭坟鬼在哭泣阶段免疫伤害，伤害源: {}, 伤害值: {}", source.getName(), amount);
         return false;
      } else {
         return super.damage(source, amount);
      }
   }
}
