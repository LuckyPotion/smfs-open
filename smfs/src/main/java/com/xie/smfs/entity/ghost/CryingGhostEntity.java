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
   private static final TrackedData<Boolean> IS_IN_CRYING_PHASE = DataTracker.method_12791(CryingGhostEntity.class, TrackedDataHandlerRegistry.field_13323);
   private int cryingDamageTickCounter = 0;
   private int cryingDamageLevel = 0;

   public CryingGhostEntity(EntityType<CryingGhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 36.0, 'B', 2500, 110, 70, 0.4F);
      this.ghostLevel = 5;
      this.attackCooldown = 1200;
      this.setEnableChaseAfterRule(false);
      this.initCryingGhostAttributes();
      if (!world.field_9236) {
         this.method_5783(ModSounds.CRYING_GHOST_ENTRANCE, 1.0F, 1.0F);
         LOGGER.info("哭坟鬼实体生成，播放出场音效: entity.crying_ghost.entrance, 位置: ({}, {}, {})", this.method_23317(), this.method_23318(), this.method_23321());
      }
   }

   private void initCryingGhostAttributes() {
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
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(IS_IN_CRYING_PHASE, false);
   }

   public boolean isInCryingPhase() {
      return (Boolean)this.field_6011.method_12789(IS_IN_CRYING_PHASE);
   }

   public void setInCryingPhase(boolean inCryingPhase) {
      this.field_6011.method_12778(IS_IN_CRYING_PHASE, inCryingPhase);
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (this.isInCryingPhase()) {
         this.setMovementDisabled(true);
      } else {
         this.setMovementDisabled(false);
      }

      if (this.isInCryingPhase()) {
         this.field_6252 = true;
      } else {
         this.field_6252 = false;
      }

      if (!this.method_37908().field_9236) {
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
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_8390(PlayerEntity.class, this.method_5829().method_1014(36.0), player -> player.method_5805() && !player.method_7325());
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
      this.field_6252 = true;
      LOGGER.info("哭坟鬼开始哭泣阶段，持续时间: {} ticks", 200);
   }

   private void cancelCryingPhase() {
      this.setInCryingPhase(false);
      this.cryingPhaseTicks = 0;
      this.cryingDamageTickCounter = 0;
      this.cryingDamageLevel = 0;
      this.field_6252 = false;
      LOGGER.info("哭坟鬼取消哭泣阶段");
   }

   private void applyCryingDamageToAllPlayers() {
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_8390(
            PlayerEntity.class,
            this.method_5829().method_1014(36.0),
            playerx -> playerx.method_5805() && !playerx.method_7325() && !playerx.method_6059(ModEffects.DEAFNESS)
         );
      float damage = 10.0F + this.cryingDamageLevel * 2.0F;
      DamageSource damageSource = ModDamageSources.ghost(this.method_37908());

      for (PlayerEntity player : nearbyPlayers) {
         if (!player.method_31481()) {
            PlayerEvents.handleSpiritDamage(player, damage, damage, damageSource);
            LOGGER.info("哭坟鬼对玩家 {} 造成 {} 点灵异伤害（伤害等级: {}）", player.method_5477().getString(), damage, this.cryingDamageLevel);
         }
      }
   }

   private void executeActualAttack() {
      this.executeDirectKill();
      LOGGER.info("哭坟鬼哭泣阶段结束，执行最终攻击");
      this.resetCryingState();
   }

   private void executeDirectKill() {
      List<PlayerEntity> nearbyPlayers = this.method_37908()
         .method_8390(
            PlayerEntity.class,
            this.method_5829().method_1014(36.0),
            playerx -> playerx.method_5805() && !playerx.method_7325() && !playerx.method_6059(ModEffects.DEAFNESS)
         );
      DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
      float lethalDamage = this.getSpiritualDamage() * 5.0F;

      for (PlayerEntity player : nearbyPlayers) {
         if (player.method_5805() && !player.method_31481()) {
            PlayerEvents.handleSpiritDamage(player, lethalDamage, lethalDamage, damageSource);
            LOGGER.info("哭坟鬼直接秒杀玩家: {}，造成 {} 点灵异伤害（基于自身灵异伤害 {} 的5倍）", player.method_5477().getString(), lethalDamage, this.getSpiritualDamage());
         }
      }
   }

   private void resetCryingState() {
      this.setInCryingPhase(false);
      this.cryingPhaseTicks = 0;
      this.cryingDamageTickCounter = 0;
      this.cryingDamageLevel = 0;
      this.attackCooldown = 1200;
      this.field_6252 = false;
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
   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10556("IsInCryingPhase", this.isInCryingPhase());
      nbt.method_10569("CryingPhaseTicks", this.cryingPhaseTicks);
      nbt.method_10569("CryingDamageLevel", this.cryingDamageLevel);
      nbt.method_10569("CryingDamageTickCounter", this.cryingDamageTickCounter);
   }

   @Override
   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("IsInCryingPhase")) {
         this.setInCryingPhase(nbt.method_10577("IsInCryingPhase"));
      }

      if (nbt.method_10545("CryingPhaseTicks")) {
         this.cryingPhaseTicks = nbt.method_10550("CryingPhaseTicks");
      }

      if (nbt.method_10545("CryingDamageLevel")) {
         this.cryingDamageLevel = nbt.method_10550("CryingDamageLevel");
      }

      if (nbt.method_10545("CryingDamageTickCounter")) {
         this.cryingDamageTickCounter = nbt.method_10550("CryingDamageTickCounter");
      }
   }

   @Override
   public boolean method_5643(DamageSource source, float amount) {
      if (this.isInCryingPhase()) {
         LOGGER.info("哭坟鬼在哭泣阶段免疫伤害，伤害源: {}, 伤害值: {}", source.method_5525(), amount);
         return false;
      } else {
         return super.method_5643(source, amount);
      }
   }
}
