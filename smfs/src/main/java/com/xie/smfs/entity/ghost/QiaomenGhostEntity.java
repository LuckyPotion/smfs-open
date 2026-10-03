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
   private static final TrackedData<Integer> CURRENT_PHASE = DataTracker.registerData(QiaomenGhostEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> PHASE_TIMER = DataTracker.registerData(QiaomenGhostEntity.class, TrackedDataHandlerRegistry.INTEGER);
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
      if (!this.deathListenerRegistered && !this.getWorld().isClient()) {
         ServerLivingEntityEvents.ALLOW_DEATH.register((AllowDeath)(entity, source, damageAmount) -> {
            if (entity != this && !(entity instanceof GhostEntity) && !(entity instanceof GhostSlaveEntity) && this.squaredDistanceTo(entity) <= 4096.0) {
               QiaomenGhostEntity.DeathRecord record = new QiaomenGhostEntity.DeathRecord(entity.getPos(), this.getWorld().getTime(), entity.getType());
               this.deathRecords.add(record);
               LOGGER.debug("敲门鬼检测到精确死亡事件: 实体类型={}, 死亡位置=({}, {}, {})", entity.getType(), entity.getX(), entity.getY(), entity.getZ());
            }

            return true;
         });
         this.deathListenerRegistered = true;
         LOGGER.debug("敲门鬼死亡事件监听器已注册");
      }
   }

   private void cleanupOldDeathRecords() {
      long currentTime = this.getWorld().getTime();
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
               recentDeath.deathPosition.x,
               recentDeath.deathPosition.y,
               recentDeath.deathPosition.z,
               recentDeath.entityType
            );
         }
      }
   }

   private void spawnGhostSlaveAtExactPosition(Vec3d position, EntityType<?> entityType) {
      if (!this.getWorld().isClient) {
         GhostSlaveEntity slave = this.spawnGhostSlave();
         if (slave != null) {
            slave.refreshPositionAndAngles(position.x, position.y, position.z, this.getYaw(), this.getPitch());
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
      this.setNoGravity(false);
      this.attackDamageMultiplier = 1;
      this.failedAttackCycles = 0;
      this.emptyCycles = 0;
      this.lastAttackSuccessful = false;
      this.setSuppressionSlotCost(3);
   }

   @Override
   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(CURRENT_PHASE, QiaomenGhostEntity.Phase.PREPARE_KNOCK.ordinal());
      this.dataTracker.startTracking(PHASE_TIMER, QiaomenGhostEntity.Phase.PREPARE_KNOCK.getDuration());
   }

   public int getCurrentPhaseOrdinal() {
      return (Integer)this.dataTracker.get(CURRENT_PHASE);
   }

   public void setCurrentPhaseOrdinal(int phaseOrdinal) {
      this.dataTracker.set(CURRENT_PHASE, phaseOrdinal);
   }

   public int getPhaseTimerValue() {
      return (Integer)this.dataTracker.get(PHASE_TIMER);
   }

   public void setPhaseTimerValue(int timer) {
      this.dataTracker.set(PHASE_TIMER, timer);
   }

   @Override
   protected void initGoals() {
      super.initGoals();
      LOGGER.debug("敲门鬼AI目标初始化完成");
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.isSuppressed() && !this.isDeadlocked()) {
         if (this.getWorld().isClient) {
            this.handleClientTick();
         } else {
            this.registerDeathListener();
            if (this.age % 100 == 0) {
               this.cleanupOldDeathRecords();
            }

            boolean isMoving = this.isWalking();
            if (!this.getWorld().isClient && !this.isInCriticalActionPhase() && this.age % 60 == 0) {
               PlayerEntity nearest = this.getWorld()
                  .getPlayers()
                  .stream()
                  .filter(p -> !p.isSpectator())
                  .min((a, b) -> Double.compare(this.squaredDistanceTo(a), this.squaredDistanceTo(b)))
                  .orElse(null);
               if (nearest != null) {
                  double dx = nearest.getX() - this.getX();
                  double dz = nearest.getZ() - this.getZ();
                  double dist = Math.sqrt(dx * dx + dz * dz);
                  if (dist > 2.5) {
                     double nx = this.getX() + dx / dist * 2.0;
                     double nz = this.getZ() + dz / dist * 2.0;
                     this.getNavigation().startMovingTo(nx, this.getY(), nz, 1.0);
                     this.getNavigation().setSpeed(1.0);
                  }
               }
            }

            if (isMoving && (this.currentPhase == QiaomenGhostEntity.Phase.KNOCKING || this.currentPhase == QiaomenGhostEntity.Phase.ATTACKING)) {
               this.getNavigation().stop();
               this.setVelocity(0.0, this.getVelocity().y, 0.0);
            }

            if (this.currentPhase == QiaomenGhostEntity.Phase.KNOCKING || this.currentPhase == QiaomenGhostEntity.Phase.ATTACKING) {
               this.getNavigation().stop();
               this.setVelocity(0.0, this.getVelocity().y, 0.0);
            }

            if (!this.getWorld().isClient) {
               this.handSwinging = this.currentPhase == QiaomenGhostEntity.Phase.ATTACKING;
            }

            if (this.knockSoundCounter > 0 && this.knockSoundDelay > 0) {
               this.knockSoundDelay--;
               if (this.knockSoundDelay <= 0) {
                  this.playSound(ModSounds.KNOCKING_SOUND, 0.6F, 0.8F);
                  this.knockSoundCounter = 0;
               }
            }

            if (!this.getWorld().isClient && this.age % 100 == 0) {
               this.replaceBlockUnderFeet();
            }

            if (!this.getWorld().isClient) {
               boolean following = this.getNavigation().isFollowingPath();
               double hs = this.getVelocity().horizontalLengthSquared();
               if ((!following || hs < 2.0E-4) && this.isOnGround()) {
                  if (hs > 2.0E-5) {
                     this.setVelocity(this.getVelocity().multiply(0.3, 1.0, 0.3));
                  } else {
                     this.setVelocity(0.0, this.getVelocity().y, 0.0);
                  }
               }
            }

            if (this.shouldDespawn()) {
               LOGGER.debug("敲门鬼正在被移除，切换到待机状态");
               if (this.walkingStateManager != null) {
                  this.walkingStateManager.reset();
               }

               GhostDeathHandler.markLegitimateRemoval(this);
               this.discard();
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
      QiaomenGhostEntity.Phase clientPhase = QiaomenGhostEntity.Phase.values()[this.dataTracker.get(CURRENT_PHASE)];
      this.handSwinging = clientPhase == QiaomenGhostEntity.Phase.ATTACKING;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return false;
   }

   private void playDelayedKnockSounds(float volume, float pitch, int knockCount) {
      if (!this.getWorld().isClient) {
         this.knockSoundCounter = knockCount;
         this.knockSoundDelay = 1;
      }
   }

   private boolean shouldDespawn() {
      boolean hasPlayers = this.hasPlayersInDomain();
      if (hasPlayers) {
         this.lastPlayerSeenTick = this.age;
         return false;
      }

      if (this.isInCriticalActionPhase()) {
         return false;
      }

      boolean exceedGrace = this.age - this.lastPlayerSeenTick > 200;
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
      return this.getWorld()
         .getPlayers()
         .stream()
         .filter(LivingEntity::isAlive)
         .filter(player -> !player.hasStatusEffect(ModEffects.DEAFNESS))
         .anyMatch(player -> this.squaredDistanceTo(player) <= 4096.0);
   }

   private void replaceBlockUnderFeet() {
      World world = this.getWorld();
      BlockPos centerPos = this.getBlockPos();
      int replacedCount = 0;
      int radius = 1;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos checkPos = centerPos.add(x, y, z);
               BlockState currentState = world.getBlockState(checkPos);
               if (currentState.getBlock() == Blocks.GRASS_BLOCK) {
                  world.setBlockState(checkPos, Blocks.PODZOL.getDefaultState());
                  replacedCount++;
                  world.playSound(null, checkPos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 0.3F, 1.0F);
               } else if (currentState.getBlock() == Blocks.STONE) {
                  world.setBlockState(checkPos, Blocks.MOSSY_COBBLESTONE.getDefaultState());
                  replacedCount++;
                  world.playSound(null, checkPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.3F, 1.0F);
               }
            }
         }
      }
   }

   private void handleGhostSlaveSpawning() {
      if (!this.getWorld().isClient) {
         boolean hasPlayersInDomain = this.getWorld()
            .getPlayers()
            .stream()
            .filter(player -> !player.hasStatusEffect(ModEffects.DEAFNESS))
            .anyMatch(player -> this.squaredDistanceTo(player) <= 4096.0);
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
      if (currentSlaveCount < 30 && this.age % 100 == 0) {
         this.checkForPlayerDeathAndSpawn();
      }
   }

   private void checkForPlayerDeathAndSpawn() {
      this.processDeathRecords();
   }

   private int countGhostSlavesInDomain() {
      return this.getWorld()
         .getEntitiesByClass(GhostSlaveEntity.class, this.getBoundingBox().expand(64.0), slave -> slave.getMaster() == this && slave.isAlive())
         .size();
   }

   private void spawnGhostSlaves(int count) {
      for (int i = 0; i < count; i++) {
         GhostSlaveEntity slave = this.spawnGhostSlave();
         LOGGER.debug("敲门鬼鬼奴已生成！");
      }
   }

   private void spawnGhostSlaveAtCurrentPosition() {
      if (!this.getWorld().isClient) {
         GhostSlaveEntity slave = this.spawnGhostSlave();
         if (slave != null) {
            slave.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            LOGGER.debug("敲门鬼在空循环时于当前位置生成鬼奴");
         }
      }
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("HasInitialSpawnedGhostSlaves", this.hasInitialSpawnedGhostSlaves);
      nbt.putBoolean("hasItem", this.hasItem);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("HasInitialSpawnedGhostSlaves")) {
         this.hasInitialSpawnedGhostSlaves = nbt.getBoolean("HasInitialSpawnedGhostSlaves");
      }

      if (nbt.contains("hasItem")) {
         this.hasItem = nbt.getBoolean("hasItem");
      }
   }

   private void executeKillPhase() {
      LOGGER.debug("敲门鬼开始杀人阶段行为");
      List<PlayerEntity> targets = this.getWorld()
         .getPlayers()
         .stream()
         .filter(player -> this.squaredDistanceTo(player) <= 4096.0)
         .filter(player -> player.hasStatusEffect(ModEffects.GHOST_KNOCK))
         .filter(player -> !this.isPlayerProtected(player))
         .filter(player -> !player.hasStatusEffect(ModEffects.DEAFNESS))
         .collect(Collectors.toList());
      if (!targets.isEmpty()) {
         this.emptyCycles = 0;
         PlayerEntity target = targets.stream().min((p1, p2) -> Double.compare(this.squaredDistanceTo(p1), this.squaredDistanceTo(p2))).orElse(null);
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
      if (this.isAlive()) {
         this.sendDisappearMessageToPlayers();
         GhostDeathHandler.markLegitimateRemoval(this);
         this.discard();
      }
   }

   private void sendDisappearMessageToPlayers() {
      World world = this.getWorld();
      if (!world.isClient) {
         for (PlayerEntity player : world.getPlayers().stream().filter(playerx -> this.squaredDistanceTo(playerx) <= 4096.0).collect(Collectors.toList())) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
               serverPlayer.sendMessage(Text.literal("§a似乎安全了？").formatted(Formatting.GREEN), true);
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
            this.teleport(player.getX(), player.getY(), player.getZ());
            DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
            float baseDamage = this.getSpiritualDamage();
            float actualDamage = baseDamage * this.attackDamageMultiplier;
            float playerHealthBeforeAttack = player.getHealth();
            PlayerEvents.handleSpiritDamage(player, actualDamage, actualDamage, damageSource);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 400, 1));
            this.spawnDoorAt(this.getBlockPos());
            boolean isPlayerDead = !player.isAlive() || player.getHealth() <= 0.0F;
            if (isPlayerDead) {
               this.attackDamageMultiplier = 1;
               this.failedAttackCycles = 0;
               this.lastAttackSuccessful = true;
               LOGGER.debug("敲门鬼成功击杀玩家 {}，伤害倍率已重置为1x", player.getName().getString());
            } else {
               this.attackDamageMultiplier *= 2;
               this.failedAttackCycles++;
               this.lastAttackSuccessful = false;
               LOGGER.debug("敲门鬼攻击玩家 {} 失败，下次攻击伤害倍率提升至 {}x，连续失败次数：{}", player.getName().getString(), this.attackDamageMultiplier, this.failedAttackCycles);
            }

            this.attackCooldown = 100;
         }
      }
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      if (source.getAttacker() instanceof PlayerEntity player) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 9, false, false, true));
      }

      return super.damage(source, amount);
   }

   private void executeKnockPhase() {
      this.searchDoorsAndSpreadBuff();
   }

   private void spawnDoorAt(BlockPos pos) {
      World world = this.getWorld();
      Direction facing = Direction.NORTH;
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
   }

   private void searchDoorsAndSpreadBuff() {
      List<BlockPos> doorPositions = new ArrayList<>();
      BlockPos center = this.getBlockPos();
      int radius = 64;

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos pos = center.add(x, y, z);
               if (this.getWorld().getBlockState(pos).getBlock() instanceof DoorBlock) {
                  doorPositions.add(pos);
               }
            }
         }
      }

      if (doorPositions.isEmpty()) {
         BlockPos selfPos = this.getBlockPos();
         this.spawnDoorAt(selfPos);
         doorPositions.add(selfPos);
      }

      LOGGER.debug("敲门鬼开始随机敲门 ");

      for (BlockPos doorPos : doorPositions) {
         this.teleport(doorPos.getX() + 0.5, doorPos.getY(), doorPos.getZ() + 0.5);
         if (doorPos == doorPositions.get(0)) {
            this.playDelayedKnockSounds(1.5F, 1.0F, 3);
         } else {
            this.playSound(ModSounds.KNOCKING_SOUND, 1.5F, 1.0F);
         }

         for (PlayerEntity player : this.getWorld()
            .getPlayers()
            .stream()
            .filter(playerx -> playerx.squaredDistanceTo(doorPos.getX(), doorPos.getY(), doorPos.getZ()) <= 100.0)
            .filter(playerx -> !playerx.hasStatusEffect(ModEffects.GHOST_KNOCK))
            .filter(playerx -> !playerx.hasStatusEffect(ModEffects.DEAFNESS))
            .collect(Collectors.toList())) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.GHOST_KNOCK, 3600, 0), this);
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
      return this.age;
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "knock", 20, this::handleKnockAnimations));
      super.registerControllers(controllers);
   }

   protected PlayState handleKnockAnimations(AnimationState<QiaomenGhostEntity> state) {
      if (!this.isRemoved() && this.isAlive()) {
         QiaomenGhostEntity.Phase currentPhase;
         if (this.getWorld().isClient) {
            currentPhase = QiaomenGhostEntity.Phase.values()[this.dataTracker.get(CURRENT_PHASE)];
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
      if (!this.getWorld().isClient) {
         PlayerEntity nearestPlayer = this.getWorld().getClosestPlayer(this, 64.0);
         if (nearestPlayer != null) {
            BlockPos playerPos = nearestPlayer.getBlockPos();
            int offsetX = (int)(Math.random() * 16.0 - 8.0);
            int offsetZ = (int)(Math.random() * 16.0 - 8.0);
            BlockPos doorPos = playerPos.add(offsetX, 0, offsetZ);
            doorPos = this.findSuitableDoorPosition(doorPos);
            if (doorPos != null) {
               this.spawnDoorAt(doorPos);
               LOGGER.debug("敲门鬼在玩家{}周围生成一扇门，位置: ({}, {}, {})", nearestPlayer.getName().getString(), doorPos.getX(), doorPos.getY(), doorPos.getZ());
            }
         }
      }
   }

   private BlockPos findSuitableDoorPosition(BlockPos pos) {
      World world = this.getWorld();

      for (int yOffset = -3; yOffset <= 3; yOffset++) {
         BlockPos checkPos = pos.add(0, yOffset, 0);
         BlockPos abovePos = checkPos.up();
         BlockPos belowPos = checkPos.down();
         if (world.getBlockState(belowPos).isSolidBlock(world, belowPos) && world.getBlockState(checkPos).isAir() && world.getBlockState(abovePos).isAir()) {
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
