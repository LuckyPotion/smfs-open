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
   private static final TrackedData<Boolean> IS_IN_PERFORMANCE_PHASE = DataTracker.method_12791(SuonaGhostEntity.class, TrackedDataHandlerRegistry.field_13323);
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
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(IS_IN_PERFORMANCE_PHASE, false);
   }

   public boolean isInPerformancePhase() {
      return (Boolean)this.field_6011.method_12789(IS_IN_PERFORMANCE_PHASE);
   }

   public void setInPerformancePhase(boolean inPerformancePhase) {
      this.field_6011.method_12778(IS_IN_PERFORMANCE_PHASE, inPerformancePhase);
   }

   private void initSuonaGhostAttributes() {
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.25);
      }

      EntityAttributeInstance attackDamageAttribute = this.method_5996(EntityAttributes.field_23721);
      if (attackDamageAttribute != null) {
         attackDamageAttribute.method_6192(12.0);
      }

      EntityAttributeInstance followRangeAttribute = this.method_5996(EntityAttributes.field_23717);
      if (followRangeAttribute != null) {
         followRangeAttribute.method_6192(36.0);
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (this.isInPerformancePhase()) {
         this.setMovementDisabled(true);
      } else {
         this.setMovementDisabled(false);
      }

      if (this.isInPerformancePhase()) {
         this.field_6252 = true;
      } else {
         this.field_6252 = false;
      }

      if (!this.method_37908().field_9236) {
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
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_8390(PlayerEntity.class, this.method_5829().method_1014(36.0), player -> player.method_5805() && !player.method_7325());
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
      LOGGER.info("唢呐鬼开始演奏阶段，目标玩家: {}", target.method_5477().getString());
   }

   private void applyPerformanceDamageToAllPlayers() {
      List<PlayerEntity> playersInRange = this.method_37908()
         .method_8390(
            PlayerEntity.class,
            this.method_5829().method_1014(36.0),
            playerx -> playerx.method_5805() && playerx.method_6032() > 0.0F && !playerx.method_6059(ModEffects.DEAFNESS)
         );
      float damage = 10.0F + 2.0F * this.performanceDamageLevel;
      DamageSource damageSource = ModDamageSources.ghost(this.method_37908());

      for (PlayerEntity player : playersInRange) {
         if (player.method_5805() && player.method_6032() > 0.0F) {
            PlayerEvents.handleSpiritDamage(player, damage, damage, damageSource);
            this.method_37908()
               .method_43128(null, player.method_23317(), player.method_23318(), player.method_23321(), SoundEvents.field_15115, this.method_5634(), 1.0F, 1.0F);
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
         double dx = this.currentTarget.method_23317() - this.method_23317();
         double dz = this.currentTarget.method_23321() - this.method_23321();
         this.method_36456((float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F);
         this.method_5636(this.method_36454());
         this.method_5847(this.method_36454());
      }
   }

   private void executeActualAttack() {
      if (this.currentTarget != null && !this.currentTarget.method_31481()) {
         this.executeDirectKill();
         LOGGER.info("唢呐鬼执行攻击，目标玩家: {}", this.currentTarget.method_5477().getString());
      }

      this.resetAttackState();
   }

   private void executeDirectKill() {
      if (this.currentTarget != null && !this.currentTarget.method_31481() && !this.currentTarget.method_6059(ModEffects.DEAFNESS)) {
         DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
         float lethalDamage = this.getSpiritualDamage() * 5.0F;
         PlayerEvents.handleSpiritDamage(this.currentTarget, lethalDamage, lethalDamage, damageSource);
         LOGGER.info("唢呐鬼直接秒杀玩家: {}，造成 {} 点灵异伤害（基于自身灵异伤害 {} 的5倍）", this.currentTarget.method_5477().getString(), lethalDamage, this.getSpiritualDamage());
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
   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10556("IsInPerformancePhase", this.isInPerformancePhase());
      nbt.method_10569("PerformancePhaseTicks", this.performancePhaseTicks);
      nbt.method_10569("PerformanceDamageLevel", this.performanceDamageLevel);
      nbt.method_10569("PerformanceDamageTickCounter", this.performanceDamageTickCounter);
   }

   @Override
   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("IsInPerformancePhase")) {
         this.setInPerformancePhase(nbt.method_10577("IsInPerformancePhase"));
      }

      if (nbt.method_10545("PerformancePhaseTicks")) {
         this.performancePhaseTicks = nbt.method_10550("PerformancePhaseTicks");
      }

      if (nbt.method_10545("PerformanceDamageLevel")) {
         this.performanceDamageLevel = nbt.method_10550("PerformanceDamageLevel");
      }

      if (nbt.method_10545("PerformanceDamageTickCounter")) {
         this.performanceDamageTickCounter = nbt.method_10550("PerformanceDamageTickCounter");
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
