package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import com.xie.smfs.util.GhostUtils;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class GanshiBrideGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 2;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'B';
   private static final String GHOST_DOMAIN_COLOR = "black";
   private int beckoningPhaseTicks = 0;
   private static final int INITIAL_ATTACK_COOLDOWN = 0;
   private static final int ATTACK_COOLDOWN = 1200;
   private static final int BECKONING_PHASE_DURATION = 200;
   private static final double ATTACK_DISTANCE = 12.0;
   private PlayerEntity currentTarget = null;
   private static final TrackedData<Boolean> IS_IN_BECKONING_PHASE = DataTracker.registerData(GanshiBrideGhostEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private int attackingAnimationTime = 0;
   private static final int ATTACK_ANIMATION_DURATION = 20;

   public GanshiBrideGhostEntity(EntityType<GanshiBrideGhostEntity> entityType, World world) {
      super(entityType, world, true, 2, 32.0, 'B', 4200, 320, 60, 0.2F);
      this.ghostLevel = 3;
      this.attackCooldown = 0;
      this.setEnableChaseAfterRule(false);
      this.initGanshiBrideAttributes();
   }

   @Override
   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(IS_IN_BECKONING_PHASE, false);
   }

   public boolean isInBeckoningPhase() {
      return (Boolean)this.dataTracker.get(IS_IN_BECKONING_PHASE);
   }

   public void setInBeckoningPhase(boolean inBeckoningPhase) {
      this.dataTracker.set(IS_IN_BECKONING_PHASE, inBeckoningPhase);
   }

   private void initGanshiBrideAttributes() {
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
         attackDamageAttribute.setBaseValue(15.0);
      }

      EntityAttributeInstance attackKnockbackAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
      if (attackKnockbackAttribute != null) {
         attackKnockbackAttribute.setBaseValue(0.3);
      }

      EntityAttributeInstance followRangeAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
      if (followRangeAttribute != null) {
         followRangeAttribute.setBaseValue(24.0);
      }
   }

   public static Builder createAttributes() {
      return GhostEntity.createGhostAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 100000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0)
         .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.3)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isInBeckoningPhase()) {
         this.setMovementDisabled(true);
      } else {
         this.setMovementDisabled(false);
      }

      if (this.isInBeckoningPhase()) {
         this.handSwinging = true;
      } else {
         this.handSwinging = false;
      }

      if (!this.getWorld().isClient) {
         if (this.age == 1) {
            this.playSound(ModSounds.GHOST_BRIDE_ENTRANCE, 1.0F, 1.0F);
            LOGGER.info("鬼新娘实体生成，播放出场音效: entity.ghost_bride.entrance, 位置: ({}, {}, {})", this.getX(), this.getY(), this.getZ());
         }

         if (this.attackCooldown > 0 && !this.isInBeckoningPhase()) {
            this.attackCooldown--;
         }

         if (this.isInBeckoningPhase()) {
            this.beckoningPhaseTicks--;
            if (this.beckoningPhaseTicks <= 0) {
               this.executeActualAttack();
            }
         }

         if (this.isSuppressed() || this.isDeadlocked()) {
            if (this.isInBeckoningPhase()) {
               this.cancelBeckoningPhase();
            }

            return;
         }

         PlayerEntity targetPlayer = this.findTargetPlayer();
         if (targetPlayer != null) {
            this.handlePlayerInteraction(targetPlayer);
         }
      }
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      int amplifier = Math.max(0, this.getGhostDomainLevel() - 1);
      player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 40, amplifier, false, false, false));
   }

   private PlayerEntity findTargetPlayer() {
      List<PlayerEntity> nearbyPlayers = this.getWorld()
         .getPlayers()
         .stream()
         .filter(player -> {
            double dx = player.getX() - this.getX();
            double dy = player.getY() - this.getY();
            double dz = player.getZ() - this.getZ();
            double distanceSq = dx * dx + dy * dy + dz * dz;
            return distanceSq <= this.getGhostDomainRadius() * this.getGhostDomainRadius();
         })
         .filter(
            player -> !(GhostDomainManager.hasGanshiBrideGhost(player) && player instanceof ServerPlayerEntity serverPlayer)
               || !AdvancementManager.hasAdvancement(serverPlayer, "smfs:become_aberration")
         )
         .collect(Collectors.toList());
      return !nearbyPlayers.isEmpty() ? nearbyPlayers.get(0) : null;
   }

   private boolean isPlayerHasGhost(PlayerEntity player) {
      return PlayerEvents.hasOccupiedGhostSlot(player);
   }

   private void handlePlayerInteraction(PlayerEntity player) {
      if (this.attackCooldown <= 0 && !this.isInBeckoningPhase()) {
         this.startBeckoningPhase(player);
      }

      if (this.isInBeckoningPhase() && this.currentTarget != null) {
         this.faceTargetPlayer();
      }
   }

   private void startBeckoningPhase(PlayerEntity target) {
      this.setInBeckoningPhase(true);
      this.beckoningPhaseTicks = 200;
      this.currentTarget = target;
      this.faceTargetPlayer();
      this.playAmbientSound();
      if (target instanceof ServerPlayerEntity serverPlayer) {
         serverPlayer.sendMessage(Text.literal("§c体内的鬼在悸动"), true);
      }
   }

   public void cancelBeckoningPhase() {
      this.setInBeckoningPhase(false);
      this.beckoningPhaseTicks = 0;
      this.currentTarget = null;
      this.attackCooldown = 200;
      LOGGER.info("干尸新娘招手阶段被取消");
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
         boolean hasGhost = PlayerEvents.hasOccupiedGhostSlot(this.currentTarget);
         if (hasGhost) {
            this.executeGhostStripping();
         } else {
            this.executeDirectKill();
         }

         this.playAmbientSound();
         LOGGER.info("干尸新娘执行攻击，目标玩家: {}", this.currentTarget.getName().getString());
      }

      this.resetAttackState();
   }

   private void executeDirectKill() {
      if (this.currentTarget != null && !this.currentTarget.isRemoved()) {
         DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
         float lethalDamage = this.getSpiritualDamage() * 5.0F;
         PlayerEvents.handleSpiritDamage(this.currentTarget, lethalDamage, lethalDamage, damageSource);
         LOGGER.info("干尸新娘直接秒杀玩家: {}，造成 {} 点灵异伤害（基于自身灵异伤害 {} 的5倍）", this.currentTarget.getName().getString(), lethalDamage, this.getSpiritualDamage());
      }
   }

   private void executeGhostStripping() {
      if (this.currentTarget != null && !this.currentTarget.isRemoved()) {
         int occupiedSlot = this.findOccupiedGhostSlot(this.currentTarget);
         if (occupiedSlot >= 0) {
            String ghostType = this.stripGhostFromSlot(this.currentTarget, occupiedSlot);
            if (ghostType != null) {
               this.spawnCorrespondingGhost(ghostType);
               this.currentTarget.damage(ModDamageSources.ghost(this.getWorld()), 6.0F);
               GhostDomainManager.disableGhostDomain(this.currentTarget);
               LOGGER.info("干尸新娘剥离玩家 {} 的槽位 {} 的鬼: {}，并造成6点伤害，同时清除玩家鬼蜮", this.currentTarget.getName().getString(), occupiedSlot, ghostType);
            }
         } else {
            this.executeDirectKill();
         }
      }
   }

   private int findOccupiedGhostSlot(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.isGhostSlotOccupied(player, i)) {
            return i;
         }
      }

      return -1;
   }

   private String stripGhostFromSlot(PlayerEntity player, int slotIndex) {
      String ghostType = PlayerEvents.getGhostTypeInSlot(player, slotIndex);
      if (ghostType != null) {
         PlayerEvents.clearGhostSlot(player, slotIndex);
         return ghostType;
      } else {
         return null;
      }
   }

   private void spawnCorrespondingGhost(String ghostType) {
      GhostEntity spawnedGhost = GhostUtils.createGhostEntityByType(ghostType, this.getWorld());
      if (spawnedGhost != null) {
         double offsetX = this.getRandom().nextGaussian() * 2.0;
         double offsetZ = this.getRandom().nextGaussian() * 2.0;
         spawnedGhost.refreshPositionAndAngles(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ, this.getRandom().nextFloat() * 360.0F, 0.0F);
         this.getWorld().spawnEntity(spawnedGhost);
         LOGGER.info("干尸新娘生成相应的鬼实体: {}", ghostType);
      } else {
         ItemStack ghostItem = GhostUtils.createTamedItem(ghostType);
         if (ghostItem != null && !ghostItem.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), ghostItem);
            this.getWorld().spawnEntity(itemEntity);
            LOGGER.info("干尸新娘没有对应鬼实体，掉落驾驭物品: {}", ghostType);
         }
      }
   }

   private void resetAttackState() {
      this.setInBeckoningPhase(false);
      this.beckoningPhaseTicks = 0;
      this.attackCooldown = 1200;
      this.currentTarget = null;
      LOGGER.info("干尸新娘招手阶段结束，进入冷却");
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

   public int getBeckoningPhaseTicks() {
      return this.beckoningPhaseTicks;
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

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("IsInBeckoningPhase", this.isInBeckoningPhase());
      nbt.putInt("BeckoningPhaseTicks", this.beckoningPhaseTicks);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("IsInBeckoningPhase")) {
         this.setInBeckoningPhase(nbt.getBoolean("IsInBeckoningPhase"));
      }

      if (nbt.contains("BeckoningPhaseTicks")) {
         this.beckoningPhaseTicks = nbt.getInt("BeckoningPhaseTicks");
      }
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      if (this.isInBeckoningPhase()) {
         LOGGER.info("干尸新娘在招手阶段免疫伤害，伤害源: {}, 伤害值: {}", source.getName(), amount);
         return false;
      } else {
         return super.damage(source, amount);
      }
   }
}
