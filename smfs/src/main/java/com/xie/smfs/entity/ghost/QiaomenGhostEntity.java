package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDeath;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class QiaomenGhostEntity extends GhostEntity implements GeoAnimatable {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 3;
   private static final double GHOST_DOMAIN_RADIUS = 64.0;
   private static final char TERROR_LEVEL = 'A';
   private static final int KNOCK_DELAY = 10;
   private static final TrackedData<Integer> CURRENT_PHASE = DataTracker.method_12791(QiaomenGhostEntity.class, TrackedDataHandlerRegistry.field_13327);
   private static final TrackedData<Integer> PHASE_TIMER = DataTracker.method_12791(QiaomenGhostEntity.class, TrackedDataHandlerRegistry.field_13327);
   private QiaomenGhostEntity.Phase currentPhase = QiaomenGhostEntity.Phase.PREPARE_KNOCK;
   private int phaseTimer = 0;
   private boolean isInitialPhase = true;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final RawAnimation KNOCK_ANIM = RawAnimation.begin().thenLoop("knock");
   private int knockSoundCounter;
   private int knockSoundDelay;
   private boolean hasInitialSpawnedGhostSlaves = false;
   private static final int GHOST_SLAVE_SPAWN_COUNT = 5;
   private static final int MAX_GHOST_SLAVE_COUNT = 30;
   private int doorSpawnTimer = 0;
   private static final int DOOR_SPAWN_INTERVAL = 100;
   private static final int FAILED_ATTACK_THRESHOLD = 1;
   private final List<QiaomenGhostEntity.DeathRecord> deathRecords = new ArrayList<>();
   private boolean deathListenerRegistered = false;
   private static final int DESPAWN_GRACE_TICKS = 200;
   private int lastPlayerSeenTick = 0;
   private static final Logger LOGGER = LoggerFactory.getLogger(QiaomenGhostEntity.class);
   private boolean hasItem = true;
   private int attackDamageMultiplier = 1;
   private int failedAttackCycles = 0;
   private int emptyCycles = 0;
   private boolean lastAttackSuccessful = false;

   private void registerDeathListener() {
      if (!this.deathListenerRegistered && !this.method_37908().method_8608()) {
         ServerLivingEntityEvents.ALLOW_DEATH
            .register(
               (AllowDeath)(entity, source, damageAmount) -> {
                  if (entity != this && !(entity instanceof GhostEntity) && !(entity instanceof GhostSlaveEntity) && this.method_5858(entity) <= 4096.0) {
                     QiaomenGhostEntity.DeathRecord record = new QiaomenGhostEntity.DeathRecord(
                        entity.method_19538(), this.method_37908().method_8510(), entity.method_5864()
                     );
                     this.deathRecords.add(record);
                     LOGGER.debug(
                        "敲门鬼检测到精确死亡事件: 实体类型={}, 死亡位置=({}, {}, {})", entity.method_5864(), entity.method_23317(), entity.method_23318(), entity.method_23321()
                     );
                  }

                  return true;
               }
            );
         this.deathListenerRegistered = true;
         LOGGER.debug("敲门鬼死亡事件监听器已注册");
      }
   }

   private void cleanupOldDeathRecords() {
      long currentTime = this.method_37908().method_8510();
      this.deathRecords.removeIf(record -> currentTime - record.deathTime > 600L);
   }

   private void processDeathRecords() {
      if (!this.deathRecords.isEmpty()) {
         int currentSlaveCount = this.countGhostSlavesInDomain();
         if (currentSlaveCount >= 30) {
            LOGGER.debug("鬼域内鬼奴数量已达最大值({})，不处理死亡记录", 30);
            this.deathRecords.clear();
         } else {
            QiaomenGhostEntity.DeathRecord recentDeath = this.deathRecords.remove(0);
            this.spawnGhostSlaveAtExactPosition(recentDeath.deathPosition, recentDeath.entityType);
            LOGGER.debug(
               "敲门鬼在精确死亡位置生成鬼奴，位置: ({}, {}, {}), 实体类型: {}",
               recentDeath.deathPosition.field_1352,
               recentDeath.deathPosition.field_1351,
               recentDeath.deathPosition.field_1350,
               recentDeath.entityType
            );
         }
      }
   }

   private void spawnGhostSlaveAtExactPosition(Vec3d position, EntityType<?> entityType) {
      if (!this.method_37908().field_9236) {
         GhostSlaveEntity slave = this.spawnGhostSlave();
         if (slave != null) {
            slave.method_5808(position.field_1352, position.field_1351, position.field_1350, this.method_36454(), this.method_36455());
         }
      }
   }

   public boolean hasItem() {
      return this.hasItem;
   }

   public void setHasItem(boolean hasItem) {
      this.hasItem = hasItem;
   }

   public int getAttackDamageMultiplier() {
      return this.attackDamageMultiplier;
   }

   @Override
   public int getGhostSlaveCount() {
      return this.countGhostSlavesInDomain();
   }

   public int getEmptyCycles() {
      return this.emptyCycles;
   }

   public int getFailedAttackCycles() {
      return this.failedAttackCycles;
   }

   public QiaomenGhostEntity(EntityType<QiaomenGhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 64.0, 'A', 6500, 1000, 150, 0.5F);
      this.ghostLevel = 4;
      this.setGhostDomainActualLevel(4);
      this.isInitialPhase = true;
      this.phaseTimer = QiaomenGhostEntity.Phase.PREPARE_KNOCK.getDuration();
      this.knockSoundCounter = 0;
      this.knockSoundDelay = 0;
      this.method_5875(false);
      this.attackDamageMultiplier = 1;
      this.failedAttackCycles = 0;
      this.emptyCycles = 0;
      this.lastAttackSuccessful = false;
      this.setSuppressionSlotCost(3);
   }

   @Override
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(CURRENT_PHASE, QiaomenGhostEntity.Phase.PREPARE_KNOCK.ordinal());
      this.field_6011.method_12784(PHASE_TIMER, QiaomenGhostEntity.Phase.PREPARE_KNOCK.getDuration());
   }

   public int getCurrentPhaseOrdinal() {
      return (Integer)this.field_6011.method_12789(CURRENT_PHASE);
   }

   public void setCurrentPhaseOrdinal(int phaseOrdinal) {
      this.field_6011.method_12778(CURRENT_PHASE, phaseOrdinal);
   }

   public int getPhaseTimerValue() {
      return (Integer)this.field_6011.method_12789(PHASE_TIMER);
   }

   public void setPhaseTimerValue(int timer) {
      this.field_6011.method_12778(PHASE_TIMER, timer);
   }

   @Override
   protected void method_5959() {
      super.method_5959();
      LOGGER.debug("敲门鬼AI目标初始化完成");
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.isSuppressed() && !this.isDeadlocked()) {
         if (this.method_37908().field_9236) {
            this.handleClientTick();
         } else {
            this.registerDeathListener();
            if (this.field_6012 % 100 == 0) {
               this.cleanupOldDeathRecords();
            }

            boolean isMoving = this.isWalking();
            if (!this.method_37908().field_9236 && !this.isInCriticalActionPhase() && this.field_6012 % 60 == 0) {
               PlayerEntity nearest = this.method_37908()
                  .method_18456()
                  .stream()
                  .filter(p -> !p.method_7325())
                  .min((a, b) -> Double.compare(this.method_5858(a), this.method_5858(b)))
                  .orElse(null);
               if (nearest != null) {
                  double dx = nearest.method_23317() - this.method_23317();
                  double dz = nearest.method_23321() - this.method_23321();
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  if (dist > 2.5) {
                     double nx = this.method_23317() + dx / dist * 2.0;
                     double nz = this.method_23321() + dz / dist * 2.0;
                     this.method_5942().method_6337(nx, this.method_23318(), nz, 1.0);
                     this.method_5942().method_6344(1.0);
                  }
               }
            }

            if (isMoving && (this.currentPhase == QiaomenGhostEntity.Phase.KNOCKING || this.currentPhase == QiaomenGhostEntity.Phase.ATTACKING)) {
               this.method_5942().method_6340();
               this.method_18800(0.0, this.method_18798().field_1351, 0.0);
            }

            if (this.currentPhase == QiaomenGhostEntity.Phase.KNOCKING || this.currentPhase == QiaomenGhostEntity.Phase.ATTACKING) {
               this.method_5942().method_6340();
               this.method_18800(0.0, this.method_18798().field_1351, 0.0);
            }

            if (!this.method_37908().field_9236) {
               this.field_6252 = this.currentPhase == QiaomenGhostEntity.Phase.ATTACKING;
            }

            if (this.knockSoundCounter > 0 && this.knockSoundDelay > 0) {
               this.knockSoundDelay--;
               if (this.knockSoundDelay <= 0) {
                  this.method_5783(ModSounds.KNOCKING_SOUND, 0.6F, 0.8F);
                  this.knockSoundCounter = 0;
               }
            }

            if (!this.method_37908().field_9236 && this.field_6012 % 100 == 0) {
               this.replaceBlockUnderFeet();
            }

            if (!this.method_37908().field_9236) {
               boolean following = this.method_5942().method_23966();
               double hs = this.method_18798().method_37268();
               if ((!following || hs < 2.0E-4) && this.method_24828()) {
                  if (hs > 2.0E-5) {
                     this.method_18799(this.method_18798().method_18805(0.3, 1.0, 0.3));
                  } else {
                     this.method_18800(0.0, this.method_18798().field_1351, 0.0);
                  }
               }
            }

            if (this.shouldDespawn()) {
               LOGGER.debug("敲门鬼正在被移除，切换到待机状态");
               if (this.walkingStateManager != null) {
                  this.walkingStateManager.reset();
               }

               GhostDeathHandler.markLegitimateRemoval(this);
               this.method_31472();
            } else {
               this.handleGhostSlaveSpawning();
               if (this.isInitialPhase) {
                  if (this.phaseTimer > 0) {
                     this.phaseTimer--;
                     if (this.phaseTimer % 100 == 0) {
                        LOGGER.debug("敲门鬼初始准备阶段，剩余时间: " + this.phaseTimer + " ticks");
                     }
                  } else {
                     this.isInitialPhase = false;
                     LOGGER.debug("敲门鬼初始准备阶段结束，开始正常循环阶段");
                     this.phaseTimer = this.currentPhase.getDuration();
                     this.setCurrentPhaseOrdinal(this.currentPhase.ordinal());
                     this.setPhaseTimerValue(this.phaseTimer);
                  }
               } else {
                  this.handlePhaseManagement();
               }
            }
         }
      }
   }

   private void executePhaseAction() {
      switch (this.currentPhase) {
         case PREPARE_KNOCK:
            LOGGER.debug("敲门鬼准备敲门阶段结束");
            this.executeKnockPhase();
            break;
         case KNOCKING:
            LOGGER.debug("敲门鬼开始敲门阶段行为");
            break;
         case PREPARE_ATTACK:
            LOGGER.debug("敲门鬼准备攻击阶段结束");
            break;
         case ATTACKING:
            LOGGER.debug("敲门鬼开始攻击阶段行为");
            this.executeKillPhase();
      }
   }

   public QiaomenGhostEntity.Phase getCurrentPhase() {
      return this.currentPhase;
   }

   public int getPhaseTimer() {
      return this.phaseTimer;
   }

   private void handleClientTick() {
      QiaomenGhostEntity.Phase clientPhase = QiaomenGhostEntity.Phase.values()[this.field_6011.method_12789(CURRENT_PHASE)];
      this.field_6252 = clientPhase == QiaomenGhostEntity.Phase.ATTACKING;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return false;
   }

   private void playDelayedKnockSounds(float volume, float pitch, int knockCount) {
      if (!this.method_37908().field_9236) {
         this.knockSoundCounter = knockCount;
         this.knockSoundDelay = 1;
      }
   }

   private boolean shouldDespawn() {
      boolean hasPlayers = this.hasPlayersInDomain();
      if (hasPlayers) {
         this.lastPlayerSeenTick = this.field_6012;
         return false;
      }

      if (this.isInCriticalActionPhase()) {
         return false;
      }

      boolean exceedGrace = this.field_6012 - this.lastPlayerSeenTick > 200;
      if (exceedGrace) {
         LOGGER.debug("敲门鬼检测到玩家离开且超过宽限，准备移除并切换到待机状态");
         if (this.walkingStateManager != null) {
            this.walkingStateManager.reset();
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isInCriticalActionPhase() {
      return this.currentPhase == QiaomenGhostEntity.Phase.KNOCKING || this.currentPhase == QiaomenGhostEntity.Phase.ATTACKING;
   }

   private boolean hasPlayersInDomain() {
      return this.method_37908()
         .method_18456()
         .stream()
         .filter(LivingEntity::method_5805)
         .filter(player -> !player.method_6059(ModEffects.DEAFNESS))
         .anyMatch(player -> this.method_5858(player) <= 4096.0);
   }

   private void replaceBlockUnderFeet() {
      World world = this.method_37908();
      BlockPos centerPos = this.method_24515();
      int replacedCount = 0;
      int radius = 1;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = centerPos.method_10069(x, y, z);
               BlockState currentState = world.method_8320(checkPos);
               if (currentState.method_26204() == Blocks.field_10219) {
                  world.method_8501(checkPos, Blocks.field_10520.method_9564());
                  replacedCount++;
                  world.method_8396(null, checkPos, SoundEvents.field_14653, SoundCategory.field_15245, 0.3F, 1.0F);
               } else if (currentState.method_26204() == Blocks.field_10340) {
                  world.method_8501(checkPos, Blocks.field_9989.method_9564());
                  replacedCount++;
                  world.method_8396(null, checkPos, SoundEvents.field_14574, SoundCategory.field_15245, 0.3F, 1.0F);
               }
            }
         }
      }
   }

   private void handleGhostSlaveSpawning() {
      if (!this.method_37908().field_9236) {
         boolean hasPlayersInDomain = this.method_37908()
            .method_18456()
            .stream()
            .filter(player -> !player.method_6059(ModEffects.DEAFNESS))
            .anyMatch(player -> this.method_5858(player) <= 4096.0);
         if (hasPlayersInDomain && !this.hasInitialSpawnedGhostSlaves) {
            LOGGER.debug("敲门鬼检测到鬼域内有玩家，开始初始生成鬼奴");
            this.spawnGhostSlaves(5);
            this.hasInitialSpawnedGhostSlaves = true;
         } else if (!this.isInitialPhase) {
            if (hasPlayersInDomain) {
               this.checkPlayerDeathAndSpawnGhostSlaves();
            }
         }
      }
   }

   private void checkPlayerDeathAndSpawnGhostSlaves() {
      int currentSlaveCount = this.countGhostSlavesInDomain();
      if (currentSlaveCount < 30 && this.field_6012 % 100 == 0) {
         this.checkForPlayerDeathAndSpawn();
      }
   }

   private void checkForPlayerDeathAndSpawn() {
      this.processDeathRecords();
   }

   private int countGhostSlavesInDomain() {
      return this.method_37908()
         .method_8390(GhostSlaveEntity.class, this.method_5829().method_1014(64.0), slave -> slave.getMaster() == this && slave.method_5805())
         .size();
   }

   private void spawnGhostSlaves(int count) {
      for (int i = 0; i < count; i++) {
         GhostSlaveEntity slave = this.spawnGhostSlave();
         LOGGER.debug("敲门鬼鬼奴已生成！");
      }
   }

   private void spawnGhostSlaveAtCurrentPosition() {
      if (!this.method_37908().field_9236) {
         GhostSlaveEntity slave = this.spawnGhostSlave();
         if (slave != null) {
            slave.method_5808(this.method_23317(), this.method_23318(), this.method_23321(), this.method_36454(), this.method_36455());
            LOGGER.debug("敲门鬼在空循环时于当前位置生成鬼奴");
         }
      }
   }

   @Override
   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10556("HasInitialSpawnedGhostSlaves", this.hasInitialSpawnedGhostSlaves);
      nbt.method_10556("hasItem", this.hasItem);
   }

   @Override
   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("HasInitialSpawnedGhostSlaves")) {
         this.hasInitialSpawnedGhostSlaves = nbt.method_10577("HasInitialSpawnedGhostSlaves");
      }

      if (nbt.method_10545("hasItem")) {
         this.hasItem = nbt.method_10577("hasItem");
      }
   }

   private void executeKillPhase() {
      LOGGER.debug("敲门鬼开始杀人阶段行为");
      List<PlayerEntity> targets = this.method_37908()
         .method_18456()
         .stream()
         .filter(player -> this.method_5858(player) <= 4096.0)
         .filter(player -> player.method_6059(ModEffects.GHOST_KNOCK))
         .filter(player -> !this.isPlayerProtected(player))
         .filter(player -> !player.method_6059(ModEffects.DEAFNESS))
         .collect(Collectors.toList());
      if (!targets.isEmpty()) {
         this.emptyCycles = 0;
         PlayerEntity target = targets.stream().min((p1, p2) -> Double.compare(this.method_5858(p1), this.method_5858(p2))).orElse(null);
         this.executeAttack(target);
      } else {
         this.emptyCycles++;
         LOGGER.debug("敲门鬼在攻击阶段未找到目标玩家，空循环计数：{}", this.emptyCycles);
         int currentSlaveCount = this.countGhostSlavesInDomain();
         if (currentSlaveCount < 30) {
            this.spawnGhostSlaveAtCurrentPosition();
         } else {
            LOGGER.debug("鬼域内鬼奴数量已达最大值({})，空循环时不生成新鬼奴", 30);
         }

         if (this.emptyCycles >= 2) {
            LOGGER.debug("敲门鬼连续两轮循环未找到目标，判断鬼蜮内无玩家，准备消失");
            this.handleEmptyDomainDisappear();
         }
      }
   }

   private void handleEmptyDomainDisappear() {
      if (this.method_5805()) {
         this.sendDisappearMessageToPlayers();
         GhostDeathHandler.markLegitimateRemoval(this);
         this.method_31472();
      }
   }

   private void sendDisappearMessageToPlayers() {
      World world = this.method_37908();
      if (!world.field_9236) {
         for (PlayerEntity player : world.method_18456().stream().filter(playerx -> this.method_5858(playerx) <= 4096.0).collect(Collectors.toList())) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.method_7353(Text.method_43470("§a似乎安全了？").method_27692(Formatting.field_1060), true);
            }
         }
      }
   }

   private void transitionToNextPhase() {
      QiaomenGhostEntity.Phase nextPhase = this.currentPhase.getNextPhase();
      LOGGER.debug("敲门鬼从阶段 {} 转换到阶段 {}，当前伤害倍率：{}x，空循环计数：{}", this.currentPhase.getName(), nextPhase.getName(), this.attackDamageMultiplier, this.emptyCycles);
      this.currentPhase = nextPhase;
      this.phaseTimer = this.currentPhase.getDuration();
      this.setCurrentPhaseOrdinal(this.currentPhase.ordinal());
      this.setPhaseTimerValue(this.phaseTimer);
   }

   private void handlePhaseManagement() {
      if (this.phaseTimer > 0) {
         this.phaseTimer--;
         if (this.phaseTimer <= 0) {
            this.executePhaseAction();
            this.transitionToNextPhase();
         }

         if (this.currentPhase == QiaomenGhostEntity.Phase.PREPARE_KNOCK && this.failedAttackCycles >= 1) {
            this.doorSpawnTimer++;
            if (this.doorSpawnTimer >= 100) {
               this.doorSpawnTimer = 0;
               this.spawnDoorAroundPlayer();
               LOGGER.debug("敲门鬼攻击失败{}次，在准备敲门阶段生成一扇门", this.failedAttackCycles);
            }
         }
      }
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked() && !this.isSuppressed()) {
         if (this.attackCooldown <= 0) {
            this.method_20620(player.method_23317(), player.method_23318(), player.method_23321());
            DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
            float baseDamage = this.getSpiritualDamage();
            float actualDamage = baseDamage * this.attackDamageMultiplier;
            float playerHealthBeforeAttack = player.method_6032();
            PlayerEvents.handleSpiritDamage(player, actualDamage, actualDamage, damageSource);
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5911, 400, 1));
            this.spawnDoorAt(this.method_24515());
            boolean isPlayerDead = !player.method_5805() || player.method_6032() <= 0.0F;
            if (isPlayerDead) {
               this.attackDamageMultiplier = 1;
               this.failedAttackCycles = 0;
               this.lastAttackSuccessful = true;
               LOGGER.debug("敲门鬼成功击杀玩家 {}，伤害倍率已重置为1x", player.method_5477().getString());
            } else {
               this.attackDamageMultiplier *= 2;
               this.failedAttackCycles++;
               this.lastAttackSuccessful = false;
               LOGGER.debug("敲门鬼攻击玩家 {} 失败，下次攻击伤害倍率提升至 {}x，连续失败次数：{}", player.method_5477().getString(), this.attackDamageMultiplier, this.failedAttackCycles);
            }

            this.attackCooldown = 100;
         }
      }
   }

   @Override
   public boolean method_5643(DamageSource source, float amount) {
      if (source.method_5529() instanceof PlayerEntity player) {
         player.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 9, false, false, true));
      }

      return super.method_5643(source, amount);
   }

   private void executeKnockPhase() {
      this.searchDoorsAndSpreadBuff();
   }

   private void spawnDoorAt(BlockPos pos) {
      World world = this.method_37908();
      Direction facing = Direction.field_11043;
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
   }

   private void searchDoorsAndSpreadBuff() {
      List<BlockPos> doorPositions = new ArrayList<>();
      BlockPos center = this.method_24515();
      int radius = 64;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos pos = center.method_10069(x, y, z);
               if (this.method_37908().method_8320(pos).method_26204() instanceof DoorBlock) {
                  doorPositions.add(pos);
               }
            }
         }
      }

      if (doorPositions.isEmpty()) {
         BlockPos selfPos = this.method_24515();
         this.spawnDoorAt(selfPos);
         doorPositions.add(selfPos);
      }

      LOGGER.debug("敲门鬼开始随机敲门 ");

      for (BlockPos doorPos : doorPositions) {
         this.method_20620(doorPos.method_10263() + 0.5, doorPos.method_10264(), doorPos.method_10260() + 0.5);
         if (doorPos == doorPositions.get(0)) {
            this.playDelayedKnockSounds(1.5F, 1.0F, 3);
         } else {
            this.method_5783(ModSounds.KNOCKING_SOUND, 1.5F, 1.0F);
         }

         for (PlayerEntity player : this.method_37908()
            .method_18456()
            .stream()
            .filter(playerx -> playerx.method_5649(doorPos.method_10263(), doorPos.method_10264(), doorPos.method_10260()) <= 100.0)
            .filter(playerx -> !playerx.method_6059(ModEffects.GHOST_KNOCK))
            .filter(playerx -> !playerx.method_6059(ModEffects.DEAFNESS))
            .collect(Collectors.toList())) {
            player.method_37222(new StatusEffectInstance(ModEffects.GHOST_KNOCK, 3600, 0), this);
            LOGGER.debug("玩家被添加了鬼敲门效果。");
         }
      }
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object object) {
      return this.field_6012;
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "knock", 20, this::handleKnockAnimations));
      super.registerControllers(controllers);
   }

   protected PlayState handleKnockAnimations(AnimationState<QiaomenGhostEntity> state) {
      if (!this.method_31481() && this.method_5805()) {
         QiaomenGhostEntity.Phase currentPhase;
         if (this.method_37908().field_9236) {
            currentPhase = QiaomenGhostEntity.Phase.values()[this.field_6011.method_12789(CURRENT_PHASE)];
         } else {
            currentPhase = this.currentPhase;
         }

         if (currentPhase == QiaomenGhostEntity.Phase.KNOCKING) {
            this.setInCustomAnimation(true);
            return state.setAndContinue(KNOCK_ANIM);
         } else {
            this.setInCustomAnimation(false);
            return PlayState.STOP;
         }
      } else {
         this.setInCustomAnimation(false);
         return PlayState.STOP;
      }
   }

   private void spawnDoorAroundPlayer() {
      if (!this.method_37908().field_9236) {
         PlayerEntity nearestPlayer = this.method_37908().method_18460(this, 64.0);
         if (nearestPlayer != null) {
            BlockPos playerPos = nearestPlayer.method_24515();
            int offsetX = (int)(Math.random() * 16.0 - 8.0);
            int offsetZ = (int)(Math.random() * 16.0 - 8.0);
            BlockPos doorPos = playerPos.method_10069(offsetX, 0, offsetZ);
            doorPos = this.findSuitableDoorPosition(doorPos);
            if (doorPos != null) {
               this.spawnDoorAt(doorPos);
               LOGGER.debug(
                  "敲门鬼在玩家{}周围生成一扇门，位置: ({}, {}, {})",
                  nearestPlayer.method_5477().getString(),
                  doorPos.method_10263(),
                  doorPos.method_10264(),
                  doorPos.method_10260()
               );
            }
         }
      }
   }

   private BlockPos findSuitableDoorPosition(BlockPos pos) {
      World world = this.method_37908();

      for (int yOffset = -3; yOffset <= 3; yOffset++) {
         BlockPos checkPos = pos.method_10069(0, yOffset, 0);
         BlockPos abovePos = checkPos.method_10084();
         BlockPos belowPos = checkPos.method_10074();
         if (world.method_8320(belowPos).method_26212(world, belowPos)
            && world.method_8320(checkPos).method_26215()
            && world.method_8320(abovePos).method_26215()) {
            return checkPos;
         }
      }

      return null;
   }

   private static class DeathRecord {
      public final Vec3d deathPosition;
      public final long deathTime;
      public final EntityType<?> entityType;

      public DeathRecord(Vec3d position, long time, EntityType<?> type) {
         this.deathPosition = position;
         this.deathTime = time;
         this.entityType = type;
      }
   }

   public enum Phase {
      PREPARE_KNOCK("准备敲门", 600),
      KNOCKING("敲门", 100),
      PREPARE_ATTACK("准备攻击", 600),
      ATTACKING("攻击", 20);

      private final String name;
      private final int duration;

      Phase(String name, int duration) {
         this.name = name;
         this.duration = duration;
      }

      public String getName() {
         return this.name;
      }

      public int getDuration() {
         return this.duration;
      }

      public QiaomenGhostEntity.Phase getNextPhase() {
         switch (this) {
            case PREPARE_KNOCK:
               return KNOCKING;
            case KNOCKING:
               return PREPARE_ATTACK;
            case PREPARE_ATTACK:
               return ATTACKING;
            case ATTACKING:
               return PREPARE_KNOCK;
            default:
               return PREPARE_KNOCK;
         }
      }
   }
}
