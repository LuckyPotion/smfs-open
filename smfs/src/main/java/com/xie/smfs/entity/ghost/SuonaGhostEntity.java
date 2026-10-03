package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModEffects;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SuonaGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 36.0;
   private static final char TERROR_LEVEL = 'B';
   private int performancePhaseTicks = 0;
   private static final int INITIAL_ATTACK_COOLDOWN = 1200;
   private static final int ATTACK_COOLDOWN = 1200;
   private static final int PERFORMANCE_PHASE_DURATION = 200;
   private static final double ATTACK_DISTANCE = 8.0;
   private int performanceDamageTickCounter = 0;
   private int performanceDamageLevel = 0;
   private PlayerEntity currentTarget = null;
   private static final TrackedData<Boolean> IS_IN_PERFORMANCE_PHASE = DataTracker.registerData(SuonaGhostEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private int attackingAnimationTime = 0;
   private static final int ATTACK_ANIMATION_DURATION = 20;

   public SuonaGhostEntity(EntityType<SuonaGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 36.0, 'B', 1500, 110, 70, 0.4F);
      this.ghostLevel = 5;
      this.attackCooldown = 1200;
      this.setEnableChaseAfterRule(false);
      this.initSuonaGhostAttributes();
   }

   @Override
   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(IS_IN_PERFORMANCE_PHASE, false);
   }

   public boolean isInPerformancePhase() {
      return (Boolean)this.dataTracker.get(IS_IN_PERFORMANCE_PHASE);
   }

   public void setInPerformancePhase(boolean inPerformancePhase) {
      this.dataTracker.set(IS_IN_PERFORMANCE_PHASE, inPerformancePhase);
   }

   private void initSuonaGhostAttributes() {
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
   public void tick() {
      super.tick();
      if (this.isInPerformancePhase()) {
         this.setMovementDisabled(true);
      } else {
         this.setMovementDisabled(false);
      }

      if (this.isInPerformancePhase()) {
         this.handSwinging = true;
      } else {
         this.handSwinging = false;
      }

      if (!this.getWorld().isClient) {
         if (this.attackCooldown > 0 && !this.isInPerformancePhase()) {
            this.attackCooldown--;
         }

         if (this.isInPerformancePhase()) {
            this.performancePhaseTicks--;
            this.performanceDamageTickCounter++;
            if (this.performanceDamageTickCounter >= 20) {
               this.performanceDamageTickCounter = 0;
               this.performanceDamageLevel++;
               this.applyPerformanceDamageToAllPlayers();
            }

            if (this.performancePhaseTicks <= 0) {
               this.executeActualAttack();
            }
         }

         if (this.isSuppressed() || this.isDeadlocked()) {
            if (this.isInPerformancePhase()) {
               this.cancelPerformancePhase();
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
      if (this.attackCooldown <= 0 && !this.isInPerformancePhase()) {
         this.startPerformancePhase(player);
      }

      if (this.isInPerformancePhase() && this.currentTarget != null) {
         this.faceTargetPlayer();
      }
   }

   private void startPerformancePhase(PlayerEntity target) {
      this.setInPerformancePhase(true);
      this.performancePhaseTicks = 200;
      this.performanceDamageTickCounter = 0;
      this.performanceDamageLevel = 0;
      this.currentTarget = target;
      this.faceTargetPlayer();
      LOGGER.info("唢呐鬼开始演奏阶段，目标玩家: {}", target.getName().getString());
   }

   private void applyPerformanceDamageToAllPlayers() {
      List<PlayerEntity> playersInRange = this.getWorld()
         .getEntitiesByClass(
            PlayerEntity.class,
            this.getBoundingBox().expand(36.0),
            playerx -> playerx.isAlive() && playerx.getHealth() > 0.0F && !playerx.hasStatusEffect(ModEffects.DEAFNESS)
         );
      float damage = 10.0F + 2.0F * this.performanceDamageLevel;
      DamageSource damageSource = ModDamageSources.ghost(this.getWorld());

      for (PlayerEntity player : playersInRange) {
         if (player.isAlive() && player.getHealth() > 0.0F) {
            PlayerEvents.handleSpiritDamage(player, damage, damage, damageSource);
            this.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_HURT, this.getSoundCategory(), 1.0F, 1.0F);
         }
      }
   }

   private void cancelPerformancePhase() {
      this.setInPerformancePhase(false);
      this.performancePhaseTicks = 0;
      this.performanceDamageTickCounter = 0;
      this.performanceDamageLevel = 0;
      this.currentTarget = null;
      LOGGER.info("唢呐鬼取消演奏阶段");
   }

   private void faceTargetPlayer() {
      if (this.currentTarget != null) {
         double dx = this.currentTarget.getX() - this.getX();
         double dz = this.currentTarget.getZ() - this.getZ();
         this.setYaw((float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F);
         this.setBodyYaw(this.getYaw());
         this.setHeadYaw(this.getYaw());
      }
   }

   private void executeActualAttack() {
      if (this.currentTarget != null && !this.currentTarget.isRemoved()) {
         this.executeDirectKill();
         LOGGER.info("唢呐鬼执行攻击，目标玩家: {}", this.currentTarget.getName().getString());
      }

      this.resetAttackState();
   }

   private void executeDirectKill() {
      if (this.currentTarget != null && !this.currentTarget.isRemoved() && !this.currentTarget.hasStatusEffect(ModEffects.DEAFNESS)) {
         DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
         float lethalDamage = this.getSpiritualDamage() * 5.0F;
         PlayerEvents.handleSpiritDamage(this.currentTarget, lethalDamage, lethalDamage, damageSource);
         LOGGER.info("唢呐鬼直接秒杀玩家: {}，造成 {} 点灵异伤害（基于自身灵异伤害 {} 的5倍）", this.currentTarget.getName().getString(), lethalDamage, this.getSpiritualDamage());
      }
   }

   private void resetAttackState() {
      this.setInPerformancePhase(false);
      this.performancePhaseTicks = 0;
      this.performanceDamageTickCounter = 0;
      this.performanceDamageLevel = 0;
      this.attackCooldown = 1200;
      this.currentTarget = null;
      LOGGER.info("唢呐鬼演奏阶段结束，进入冷却");
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("IsInPerformancePhase", this.isInPerformancePhase());
      nbt.putInt("PerformancePhaseTicks", this.performancePhaseTicks);
      nbt.putInt("PerformanceDamageLevel", this.performanceDamageLevel);
      nbt.putInt("PerformanceDamageTickCounter", this.performanceDamageTickCounter);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("IsInPerformancePhase")) {
         this.setInPerformancePhase(nbt.getBoolean("IsInPerformancePhase"));
      }

      if (nbt.contains("PerformancePhaseTicks")) {
         this.performancePhaseTicks = nbt.getInt("PerformancePhaseTicks");
      }

      if (nbt.contains("PerformanceDamageLevel")) {
         this.performanceDamageLevel = nbt.getInt("PerformanceDamageLevel");
      }

      if (nbt.contains("PerformanceDamageTickCounter")) {
         this.performanceDamageTickCounter = nbt.getInt("PerformanceDamageTickCounter");
      }
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

   public int getPerformancePhaseTicks() {
      return this.performancePhaseTicks;
   }

   public PlayerEntity getCurrentTarget() {
      return this.currentTarget;
   }

   public boolean isOnAttackCooldown() {
      return this.attackCooldown > 0;
   }

   public int getAttackCooldownTicks() {
      return this.attackCooldown;
   }
}
