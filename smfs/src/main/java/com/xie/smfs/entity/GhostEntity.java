package com.xie.smfs.entity;

import com.xie.smfs.Smfs;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ClientModConfig;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.effect.LostStatusEffectInstance;
import com.xie.smfs.effect.SpiritSurgeStatusEffect;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.entity.ghost.GhostOfficerEntity;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.item.GhostBuddhaBeadsItem;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.item.SpiritWeapon;
import com.xie.smfs.item.WhiteGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.manager.DifficultyManager;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.manager.GhostSpawnManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.manager.WalkingStateManager;
import com.xie.smfs.network.packets.common.c2s.SpectateModePacket;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.util.WeaponOilHandler;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.Nullable;
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

public class GhostEntity extends PathAwareEntity implements GeoAnimatable {
   private boolean isSuppressed = false;
   private final boolean isDeadlocked = false;
   boolean movementDisabled = false;
   private ItemStack coffinNail = ItemStack.EMPTY;
   boolean ghostDomainEnabled = true;
   public int attackCooldown;
   public int ghostLevel;
   private LivingEntity attackTarget;
   private boolean killingRulesEnabled = true;
   private boolean isVisible = true;
   private int visibleTicks = 0;
   private boolean inClosedCoffin = false;
   public WalkingStateManager walkingStateManager;
   private LivingEntity ghostFactionTarget = null;
   private int chaseTimer = 0;
   private boolean isChasing = false;
   private LivingEntity chaseTarget = null;
   private boolean enableChaseAfterRule = true;
   private int resentmentValue = 0;
   private int resentmentTimer = 0;
   private static final int RESENTMENT_THRESHOLD = 100;
   private static final int RESENTMENT_UPDATE_INTERVAL = 20;
   private boolean whiteCandleTargetLocked = false;
   private int whiteCandleTargetLockTimer = 0;
   private int suppressionSlotCost = 1;
   private static final Map<UUID, Long> SPIRIT_WEAPON_COOLDOWN = new HashMap<>();
   private boolean isInCustomAnimation = false;
   private Vec3d lastPosition = null;
   private int positionCheckTimer = 0;
   protected Consumer<PlayerEntity> customAttackLogic = null;
   protected Predicate<PlayerEntity> customTargetSelector = null;
   protected List<BiConsumer<GhostEntity, String>> stateListeners = null;
   protected Map<String, Function<GhostEntity, NbtElement>> customNbtSerializers = null;
   protected Map<String, BiConsumer<GhostEntity, NbtElement>> customNbtDeserializers = null;
   protected static final Logger LOGGER = LoggerFactory.getLogger(GhostEntity.class);
   private static final TrackedData<Boolean> DEADLOCKED = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Float> GHOST_DOMAIN_RADIUS = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Integer> SPIRITUAL_STRENGTH = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> MAX_SPIRITUAL_STRENGTH = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> SPIRITUAL_RESISTANCE = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Integer> SPIRITUAL_DAMAGE = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Float> RECOVERY_FACTOR = DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final int MIN_STRENGTH_FOR_DEADLOCK = 400;
   private static final int DEFAULT_MAX_SPIRITUAL_STRENGTH = 1000;
   private int maxSpiritualStrength = 1000;
   private int spiritualDamage = 100;
   private int spiritualResistance = 50;
   private float recoveryFactor = 0.2F;
   public static boolean ENABLE_INVULNERABLE = true;
   public static boolean ENABLE_BLACK_PARTICLES = false;
   private final Random random = new Random();
   public static boolean ENABLE_VOID_VORTEX = true;
   public boolean hasGhostDomain;
   private int ghostDomainLevel;
   private int ghostDomainActualLevel = -1;
   private char terrorLevel;
   private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
   private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
   protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenLoop("attack");
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public int getSuppressionSlotCost() {
      return this.suppressionSlotCost;
   }

   public void setSuppressionSlotCost(int cost) {
      this.suppressionSlotCost = cost;
   }

   public void setInCustomAnimation(boolean inCustomAnimation) {
      this.isInCustomAnimation = inCustomAnimation;
   }

   public boolean isInCustomAnimation() {
      return this.isInCustomAnimation;
   }

   public void setInClosedCoffin(boolean inClosedCoffin) {
      this.inClosedCoffin = inClosedCoffin;
      this.updateActualVisibility();
   }

   public boolean isInClosedCoffin() {
      return this.inClosedCoffin;
   }

   public boolean isGhostDomainEnabled() {
      return this.ghostDomainEnabled;
   }

   public int getAttackCooldown() {
      return this.attackCooldown;
   }

   public int getGhostLevel() {
      return this.ghostLevel;
   }

   public boolean isKillingRulesEnabled() {
      return this.killingRulesEnabled;
   }

   public void setKillingRulesEnabled(boolean enabled) {
      this.killingRulesEnabled = enabled;
   }

   public Consumer<PlayerEntity> getCustomAttackLogic() {
      return this.customAttackLogic;
   }

   public Predicate<PlayerEntity> getCustomTargetSelector() {
      return this.customTargetSelector;
   }

   public List<BiConsumer<GhostEntity, String>> getStateListeners() {
      return this.stateListeners;
   }

   public Map<String, Function<GhostEntity, NbtElement>> getCustomNbtSerializers() {
      return this.customNbtSerializers;
   }

   public Map<String, BiConsumer<GhostEntity, NbtElement>> getCustomNbtDeserializers() {
      return this.customNbtDeserializers;
   }

   public void setVisible(boolean visible) {
      this.isVisible = visible;
      this.updateActualVisibility();
   }

   public boolean isVisible() {
      return this.isVisible && !this.inClosedCoffin;
   }

   public boolean getVisible() {
      return this.isVisible && !this.inClosedCoffin;
   }

   public boolean isWalking() {
      return this.walkingStateManager != null && this.walkingStateManager.isWalking();
   }

   public GoalSelector getGoalSelector() {
      return this.goalSelector;
   }

   public GoalSelector getTargetSelector() {
      return this.targetSelector;
   }

   public void setGhostFactionTarget(LivingEntity target) {
      this.ghostFactionTarget = target;
   }

   public LivingEntity getGhostFactionTarget() {
      return this.ghostFactionTarget;
   }

   public boolean isResentmentSystemDisabled() {
      return false;
   }

   public boolean hasGhostFactionTarget() {
      return this.ghostFactionTarget != null && this.ghostFactionTarget.isAlive();
   }

   public void clearGhostFactionTarget() {
      this.ghostFactionTarget = null;
   }

   public boolean isChasing() {
      return this.isChasing;
   }

   public void setChasing(boolean chasing) {
      this.isChasing = chasing;
   }

   public LivingEntity getChaseTarget() {
      return this.chaseTarget;
   }

   public void setChaseTarget(LivingEntity target) {
      this.chaseTarget = target;
   }

   public int getChaseTimer() {
      return this.chaseTimer;
   }

   public void setChaseTimer(int timer) {
      this.chaseTimer = timer;
   }

   public LivingEntity getAttackTarget() {
      return this.attackTarget;
   }

   public void setAttackTarget(LivingEntity target) {
      this.attackTarget = target;
   }

   public boolean isEnableChaseAfterRule() {
      return this.enableChaseAfterRule;
   }

   public void setEnableChaseAfterRule(boolean enable) {
      this.enableChaseAfterRule = enable;
   }

   private void breakBlocksInPath(LivingEntity target) {
      double ghostHeight = this.getHeight();
      double targetHeight = target.getHeight();
      Vec3d ghostHeadPos = this.getPos().add(0.0, ghostHeight, 0.0);
      Vec3d ghostFeetPos = this.getPos();
      Vec3d targetHeadPos = target.getPos().add(0.0, targetHeight, 0.0);
      Vec3d targetFeetPos = target.getPos();
      this.checkAndBreakPath(ghostHeadPos, targetHeadPos);
      this.checkAndBreakPath(ghostFeetPos, targetFeetPos);
   }

   private void checkAndBreakPath(Vec3d startPos, Vec3d endPos) {
      RaycastContext context = new RaycastContext(startPos, endPos, ShapeType.COLLIDER, FluidHandling.NONE, this);
      BlockHitResult hitResult = this.getWorld().raycast(context);
      if (hitResult != null && hitResult.getType() == Type.BLOCK) {
         BlockPos hitPos = hitResult.getBlockPos();
         double distance = this.getPos().distanceTo(new Vec3d(hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5));
         if (distance > 3.0) {
            return;
         }

         BlockState hitState = this.getWorld().getBlockState(hitPos);
         if (!hitState.isOf(Blocks.GOLD_BLOCK) && !hitState.isOf(Blocks.BEDROCK)) {
            this.getWorld().breakBlock(hitPos, true);
            LOGGER.debug("Ghost {} broke block at {} (distance: {}) while chasing target", this.getUuidAsString(), hitPos, distance);
         }
      }
   }

   public int getResentmentValue() {
      return this.resentmentValue;
   }

   public void setResentmentValue(int value) {
      this.resentmentValue = Math.max(0, Math.min(100, value));
   }

   public void increaseResentment(int amount) {
      this.setResentmentValue(this.resentmentValue + amount);
   }

   public void decreaseResentment(int amount) {
      this.setResentmentValue(this.resentmentValue - amount);
   }

   public boolean isResentmentThresholdReached() {
      return this.resentmentValue >= 100;
   }

   private int calculateResentmentGainByDistance(double squaredDistance) {
      double distance = Math.sqrt(squaredDistance);
      double ghostDomainRadius = this.getGhostDomainRadius();
      int difficulty = WorldConfig.getInstance(this.getWorld()).modDifficulty;
      if (ghostDomainRadius <= 0.0) {
         ghostDomainRadius = 48.0;
      }

      if (distance <= 2.0) {
         if (difficulty == 0) {
            return 3;
         } else {
            return difficulty == 1 ? 4 : 5;
         }
      } else if (distance <= 6.0) {
         if (difficulty == 0) {
            return 2;
         } else {
            return difficulty == 1 ? 2 : 3;
         }
      } else if (distance <= 12.0) {
         if (difficulty == 0) {
            return 1;
         } else {
            return difficulty == 1 ? 1 : 2;
         }
      } else if (distance <= ghostDomainRadius) {
         if (difficulty == 0) {
            return 0;
         } else {
            return difficulty == 1 ? 1 : 1;
         }
      } else {
         return 0;
      }
   }

   private void updateActualVisibility() {
      boolean actualVisible = this.isVisible && !this.inClosedCoffin;
      this.setInvisible(!actualVisible);
   }

   public boolean shouldAppearOnAttack() {
      return true;
   }

   public void setVisibleOnAttack() {
      if (this.shouldAppearOnAttack()) {
         this.isVisible = true;
         this.visibleTicks = 60;
         this.updateActualVisibility();
      }
   }

   public boolean isSuppressed() {
      return this.isSuppressed;
   }

   public boolean isDeadlocked() {
      return (Boolean)this.dataTracker.get(DEADLOCKED);
   }

   public void setSuppressed(boolean suppressed) {
      this.isSuppressed = suppressed;
      this.dataTracker.set(DEADLOCKED, suppressed);
      if (suppressed && !(this instanceof GhostOfficerEntity)) {
         this.clearGhostDomainEffects();
      }
   }

   public int getVisibleTicks() {
      return this.visibleTicks;
   }

   public void setVisibleTicks(int ticks) {
      this.visibleTicks = ticks;
   }

   public void setMovementDisabled(boolean disabled) {
      this.movementDisabled = disabled;
      if (disabled) {
         this.setVelocity(Vec3d.ZERO);
      }
   }

   public boolean isMovementDisabled() {
      return this.movementDisabled;
   }

   public void setDeadlocked(boolean deadlocked) {
      this.dataTracker.set(DEADLOCKED, deadlocked);
      if (deadlocked && !(this instanceof GhostOfficerEntity)) {
         this.clearGhostDomainEffects();
      }
   }

   public boolean isCollidable() {
      return !this.isSuppressed() && !this.isDeadlocked() && !this.movementDisabled ? super.isCollidable() : false;
   }

   public boolean isPushable() {
      return this.getWorld().getEntitiesByClass(BoatEntity.class, this.getBoundingBox().expand(0.5), Entity::isAlive).isEmpty()
            && this.getWorld().getEntitiesByClass(AbstractMinecartEntity.class, this.getBoundingBox().expand(0.5), Entity::isAlive).isEmpty()
         ? super.isPushable()
         : false;
   }

   public void pushAwayFrom(Entity entity) {
      if (!(entity instanceof BoatEntity) && !(entity instanceof AbstractMinecartEntity)) {
         super.pushAwayFrom(entity);
      }
   }

   public boolean hasCoffinNail() {
      return this.coffinNail != null && !this.coffinNail.isEmpty();
   }

   public ItemStack getCoffinNail() {
      return this.coffinNail;
   }

   public void setCoffinNail(ItemStack nail) {
      this.coffinNail = nail;
   }

   public void disableGhostDomain() {
      this.ghostDomainEnabled = false;
      double previousRadius = this.getGhostDomainRadius();
      this.hasGhostDomain = false;
      this.setGhostDomainRadius(0.0F);
      this.clearGhostDomainEffects(previousRadius);
   }

   private void clearGhostDomainEffects(double radius) {
      Vec3d center = this.getPos();

      for (PlayerEntity player : this.getWorld().getPlayers()) {
         if (player.squaredDistanceTo(center) <= radius * radius) {
            player.removeStatusEffect(ModEffects.LOST);
            player.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(StatusEffects.SLOWNESS);
            player.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
            player.removeStatusEffect(StatusEffects.DARKNESS);
         }
      }
   }

   public void enableGhostDomain() {
      this.ghostDomainEnabled = true;
      this.hasGhostDomain = true;
      this.setGhostDomainRadius(this.getDefaultGhostDomainRadius());
   }

   public void disableKillingRules() {
      this.killingRulesEnabled = false;
      this.attackCooldown = 0;
      this.attackTarget = null;
   }

   public void enableKillingRules() {
      this.killingRulesEnabled = true;
      this.attackCooldown = this.getDefaultAttackCooldown();
   }

   private void clearGhostDomainEffects() {
      for (PlayerEntity player : this.getWorld().getPlayers()) {
         if (this.isInGhostDomain(player)) {
            player.removeStatusEffect(ModEffects.LOST);
            player.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(StatusEffects.SLOWNESS);
            player.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
            player.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
            player.removeStatusEffect(StatusEffects.DARKNESS);
         }
      }
   }

   protected float getDefaultGhostDomainRadius() {
      return 10.0F + this.ghostLevel * 2.0F;
   }

   protected boolean isInGhostDomain(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= this.getGhostDomainRadius() * this.getGhostDomainRadius();
   }

   private int getDefaultAttackCooldown() {
      return 20;
   }

   public static Builder createGhostAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 100000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
         .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
   }

   protected boolean isHasGhostDomain() {
      return this.hasGhostDomain;
   }

   protected void applyDefaultEffects(PlayerEntity player) {
      player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 200, this.getGhostDomainActualLevel() - 1, false, false));
   }

   protected boolean isHoldingGhostBuddhaBeads(PlayerEntity player) {
      if (player.getMainHandStack().getItem() instanceof GhostBuddhaBeadsItem) {
         return true;
      }

      if (player.getOffHandStack().getItem() instanceof GhostBuddhaBeadsItem) {
         return true;
      }

      for (int i = 0; i < player.getInventory().size(); i++) {
         ItemStack stack = player.getInventory().getStack(i);
         if (stack.getItem() instanceof GhostBuddhaBeadsItem) {
            return true;
         }
      }

      return false;
   }

   public GhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      this(entityType, world, false, 0, 5.0, 'C');
   }

   public GhostEntity(EntityType<? extends GhostEntity> entityType, World world, boolean hasGhostDomain, int ghostDomainLevel, double ghostDomainRadius) {
      this(entityType, world, hasGhostDomain, ghostDomainLevel, ghostDomainRadius, 'C');
   }

   public GhostEntity(
      EntityType<? extends GhostEntity> entityType, World world, boolean hasGhostDomain, int ghostDomainLevel, double ghostDomainRadius, char terrorLevel
   ) {
      this(entityType, world, hasGhostDomain, ghostDomainLevel, ghostDomainRadius, terrorLevel, 1000, 100, 50, 0.2F);
   }

   public GhostEntity(
      EntityType<? extends GhostEntity> entityType,
      World world,
      boolean hasGhostDomain,
      int ghostDomainLevel,
      double ghostDomainRadius,
      char terrorLevel,
      int maxSpiritualStrength,
      int spiritualDamage,
      int spiritualResistance,
      float recoveryFactor
   ) {
      super(entityType, world);
      this.hasGhostDomain = hasGhostDomain;
      this.ghostDomainLevel = ghostDomainLevel;
      this.dataTracker.set(GHOST_DOMAIN_RADIUS, (float)ghostDomainRadius);
      char processedTerrorLevel = Character.toUpperCase(terrorLevel);
      if (processedTerrorLevel < 'A' || processedTerrorLevel > 'S' || processedTerrorLevel > 'D' && processedTerrorLevel < 'S') {
         processedTerrorLevel = 'C';
      }

      this.terrorLevel = processedTerrorLevel;
      this.attackCooldown = 0;
      int adjustedMaxSpiritualStrength = DifficultyManager.adjustSpiritualStrength(maxSpiritualStrength, this.getWorld());
      int adjustedSpiritualDamage = DifficultyManager.adjustSpiritualDamage(spiritualDamage, this.getWorld());
      int adjustedSpiritualResistance = DifficultyManager.adjustSpiritualResistance(spiritualResistance, this.getWorld());
      this.maxSpiritualStrength = adjustedMaxSpiritualStrength;
      this.spiritualDamage = adjustedSpiritualDamage;
      this.spiritualResistance = adjustedSpiritualResistance;
      this.recoveryFactor = recoveryFactor;
      this.dataTracker.set(SPIRITUAL_STRENGTH, (int)(adjustedMaxSpiritualStrength * 0.8F));
      this.dataTracker.set(MAX_SPIRITUAL_STRENGTH, adjustedMaxSpiritualStrength);
      this.dataTracker.set(SPIRITUAL_RESISTANCE, adjustedSpiritualResistance);
      this.dataTracker.set(SPIRITUAL_DAMAGE, adjustedSpiritualDamage);
      this.dataTracker.set(RECOVERY_FACTOR, recoveryFactor);
      this.walkingStateManager = new WalkingStateManager(this);
   }

   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(1, new MeleeAttackGoal(this, 1.8, false));
      this.goalSelector.add(2, new GhostEntity.CustomWanderGoal(this, 1.0));
      this.goalSelector.add(3, new GhostEntity.CustomLookAtPlayerGoal(this, PlayerEntity.class, 32.0F));
      final GhostEntity ghost = this;
      this.targetSelector.add(1, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, new Predicate<LivingEntity>() {
         public boolean test(LivingEntity entity) {
            if (ghost.isSuppressed() || ghost.isDeadlocked()) {
               return false;
            } else {
               return entity instanceof PlayerEntity player ? WhiteGhostCandleItem.isHoldingWhiteCandle(player) : false;
            }
         }
      }));
      this.targetSelector.add(2, new ActiveTargetGoal(this, GhostEntity.class, 10, true, false, otherGhost -> this.shouldAttackGhost((GhostEntity)otherGhost)));
   }

   public float getRecoveryFactorByTearorLevel(char level) {
      return switch (Character.toUpperCase(level)) {
         case 'A' -> 0.6F;
         case 'B' -> 0.4F;
         case 'S' -> 0.8F;
         default -> 0.2F;
      };
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(DEADLOCKED, false);
      this.dataTracker.startTracking(GHOST_DOMAIN_RADIUS, 0.0F);
      this.dataTracker.startTracking(SPIRITUAL_STRENGTH, 800);
      this.dataTracker.startTracking(MAX_SPIRITUAL_STRENGTH, 1000);
      this.dataTracker.startTracking(SPIRITUAL_RESISTANCE, 50);
      this.dataTracker.startTracking(SPIRITUAL_DAMAGE, 100);
      this.dataTracker.startTracking(RECOVERY_FACTOR, 0.2F);
   }

   public void onTrackedDataSet(TrackedData<?> data) {
      super.onTrackedDataSet(data);
      if (data == DEADLOCKED && this.isDeadlocked() && !(this instanceof GhostOfficerEntity)) {
         this.clearGhostDomainEffects();
      }
   }

   public Arm getMainArm() {
      return Arm.RIGHT;
   }

   public void handleSkillSpiritDamage(ServerPlayerEntity player, float damageAmount) {
      if (!this.getWorld().isClient()) {
         float ghostSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage = PlayerEvents.calculateSpiritDamage(damageAmount, ghostSpiritResistance);
         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         LOGGER.debug(
            "玩家 {} 使用技能攻击鬼 {}，技能伤害值: {}，鬼灵异抗性: {}，实际造成灵异伤害: {}",
            player.getName().getString(),
            this.getName().getString(),
            damageAmount,
            ghostSpiritResistance,
            actualSpiritDamage
         );
         if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(player.getUuid()) && actualSpiritDamage > 0.0F) {
            player.sendMessage(
               Text.literal("§a对 §f" + this.getName().getString() + " §a造成了§f " + new DecimalFormat("#.###").format(actualSpiritDamage) + " §a点灵异伤害"), true
            );
         }

         if (actualSpiritDamage > 0.0F && !this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)this.getWorld();
            Vec3d pos = this.getPos();
            serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getRandom().nextDouble() * 1.5 + 0.5;
               double offsetZ = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double velocityX = (this.getRandom().nextDouble() - 0.5) * 0.2;
               double velocityY = this.getRandom().nextDouble() * 0.3 + 0.1;
               double velocityZ = (this.getRandom().nextDouble() - 0.5) * 0.2;
               serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isChasing() && this.attackCooldown <= 0) {
            this.setResentmentValue(100);
            LOGGER.debug("玩家 {} 使用技能攻击鬼，触发受击反击，怨气值直接积满至100", player.getName().getString());
         }
      }
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.getWorld().isClient()) {
         return true;
      }

      if (source.getAttacker() instanceof GhostEntity attackerGhost) {
         LOGGER.debug("鬼 {} 正在攻击鬼 {}", attackerGhost.getName().getString(), this.getName().getString());
         float attackerSpiritDamage = attackerGhost.getSpiritualDamage();
         float defenderSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage = PlayerEvents.calculateSpiritDamage(attackerSpiritDamage, defenderSpiritResistance);
         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         LOGGER.debug(
            "鬼 {} 灵异伤害为 {} 鬼 {} 灵异抗性为 {} 攻击造成的实际灵异伤害: {}",
            attackerGhost.getName().getString(),
            attackerSpiritDamage,
            this.getName().getString(),
            defenderSpiritResistance,
            actualSpiritDamage
         );
         if (actualSpiritDamage > 0.0F && !this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)this.getWorld();
            Vec3d pos = this.getPos();
            serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getRandom().nextDouble() * 1.5 + 0.5;
               double offsetZ = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double velocityX = (this.getRandom().nextDouble() - 0.5) * 0.2;
               double velocityY = this.getRandom().nextDouble() * 0.3 + 0.1;
               double velocityZ = (this.getRandom().nextDouble() - 0.5) * 0.2;
               serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isSuppressed() && this.attackCooldown <= 0) {
            this.setTarget(attackerGhost);
            this.executeAttack(attackerGhost);
            LOGGER.debug("鬼 {} 被鬼 {} 攻击，触发反击", this.getName().getString(), attackerGhost.getName().getString());
         }

         return false;
      } else if (source.getAttacker() instanceof GhostMasterEntity ghostMaster) {
         LOGGER.debug("驭鬼者 {} 正在攻击鬼 {}", ghostMaster.getName().getString(), this.getName().getString());
         float attackerSpiritDamage = ghostMaster.getSpiritualDamage();
         float defenderSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage = PlayerEvents.calculateSpiritDamage(attackerSpiritDamage, defenderSpiritResistance);
         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         LOGGER.debug(
            "驭鬼者 {} 灵异伤害为 {} 鬼 {} 灵异抗性为 {} 攻击造成的实际灵异伤害: {}",
            ghostMaster.getName().getString(),
            attackerSpiritDamage,
            this.getName().getString(),
            defenderSpiritResistance,
            actualSpiritDamage
         );
         if (actualSpiritDamage > 0.0F && !this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)this.getWorld();
            Vec3d pos = this.getPos();
            serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getRandom().nextDouble() * 1.5 + 0.5;
               double offsetZ = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double velocityX = (this.getRandom().nextDouble() - 0.5) * 0.2;
               double velocityY = this.getRandom().nextDouble() * 0.3 + 0.1;
               double velocityZ = (this.getRandom().nextDouble() - 0.5) * 0.2;
               serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isSuppressed() && this.attackCooldown <= 0) {
            this.setTarget(ghostMaster);
            this.executeAttack(ghostMaster);
            LOGGER.debug("鬼 {} 被驭鬼者 {} 攻击，触发反击", this.getName().getString(), ghostMaster.getName().getString());
         }

         return false;
      } else if (source.getAttacker() instanceof PlayerEntity player) {
         LOGGER.debug("玩家 {} 正在攻击鬼", player.getName().getString());
         boolean isSkillDamage = GhostSkillManager.isSkillDamage(source);
         if (isSkillDamage) {
            LOGGER.debug("玩家 {} 使用技能攻击鬼，跳过灵异武器检查", player.getName().getString());
         }

         if (!isSkillDamage) {
            boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(player.getMainHandStack());
            boolean hasCorpseOilWeapon = WeaponOilHandler.getCorpseOilLayers(player.getMainHandStack()) > 0;
            if (!hasSpiritWeapon && !hasCorpseOilWeapon) {
               if (!SpiritSurgeStatusEffect.hasSpiritSurgeEffect(player)) {
                  LOGGER.debug("玩家 {} 没有持有灵异武器、抹尸油武器且无灵异奔涌效果，无法对鬼造成伤害", player.getName().getString());
                  return true;
               }

               LOGGER.debug("玩家 {} 拥有灵异奔涌效果，允许对鬼造成伤害", player.getName().getString());
            }
         }

         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float playerSpiritDamage = 0.0F;
         float tempSpiritDamage = 0.0F;
         float tempSpiritDamageMultiplier = 1.0F;
         if (spiritAttributes.contains("spiritDamage")) {
            if (spiritAttributes.contains("spiritDamage", 3)) {
               playerSpiritDamage = spiritAttributes.getInt("spiritDamage");
            } else if (spiritAttributes.contains("spiritDamage", 6)) {
               playerSpiritDamage = (float)spiritAttributes.getDouble("spiritDamage");
            } else {
               playerSpiritDamage = spiritAttributes.getFloat("spiritDamage");
            }
         }

         if (spiritAttributes.contains("tempSpiritDamage")) {
            tempSpiritDamage = (float)spiritAttributes.getDouble("tempSpiritDamage");
         }

         if (spiritAttributes.contains("tempSpiritDamageMultiplier")) {
            tempSpiritDamageMultiplier = (float)spiritAttributes.getDouble("tempSpiritDamageMultiplier");
         }

         float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
         float weaponBonus = 0.0F;
         boolean hasCorpseOilWeapon = false;
         if (SpiritWeapon.isSpiritWeapon(player.getMainHandStack())) {
            SpiritWeapon weapon = (SpiritWeapon)player.getMainHandStack().getItem();
            weaponBonus = this.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : weapon.getSpiritDamageBonus();
         } else if (WeaponOilHandler.getCorpseOilLayers(player.getMainHandStack()) > 0) {
            weaponBonus = 0.0F;
            hasCorpseOilWeapon = true;
         }

         float effectiveSpiritDamage = totalSpiritDamage;
         boolean hasSpiritOrCorpseOilWeapon = SpiritWeapon.isSpiritWeapon(player.getMainHandStack())
            || WeaponOilHandler.getCorpseOilLayers(player.getMainHandStack()) > 0;
         if (totalSpiritDamage <= 0.0F && hasSpiritOrCorpseOilWeapon) {
            if (SpiritWeapon.isSpiritWeapon(player.getMainHandStack())) {
               SpiritWeapon weapon = (SpiritWeapon)player.getMainHandStack().getItem();
               effectiveSpiritDamage = this.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : weapon.getSpiritDamageBonus();
            } else if (WeaponOilHandler.getCorpseOilLayers(player.getMainHandStack()) > 0) {
               effectiveSpiritDamage = this.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : amount * 10.0F;
               if (this.getWorld().getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
                  WeaponOilHandler.consumeCorpseOilLayer(player.getMainHandStack());
               }
            }

            LOGGER.debug("玩家 {} 使用灵异武器或抹尸油武器但无灵异伤害属性，使用武器伤害加成: {}", player.getName().getString(), effectiveSpiritDamage);
         } else {
            float damageMultiplier = 0.5F;
            if (SpiritWeapon.isSpiritWeapon(player.getMainHandStack())) {
               SpiritWeapon weapon = (SpiritWeapon)player.getMainHandStack().getItem();
               damageMultiplier = this.getWorld().getRegistryKey() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : weapon.getSpiritDamageMultiplier();
            }

            effectiveSpiritDamage = weaponBonus + totalSpiritDamage * damageMultiplier;
            if (WeaponOilHandler.getCorpseOilLayers(player.getMainHandStack()) > 0 && this.getWorld().getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
               WeaponOilHandler.consumeCorpseOilLayer(player.getMainHandStack());
            }
         }

         float ghostSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage;
         if (SpiritSurgeStatusEffect.hasSpiritSurgeEffect(player)) {
            float playerBaseSpiritDamage = playerSpiritDamage;
            actualSpiritDamage = PlayerEvents.calculateSpiritSurgeDamage(playerBaseSpiritDamage, 6.0F, ghostSpiritResistance);
            LOGGER.debug("玩家 {} 使用灵异奔涌效果，基础灵异伤害: {}，使用特殊公式计算伤害", player.getName().getString(), playerBaseSpiritDamage);
         } else {
            actualSpiritDamage = PlayerEvents.calculateSpiritDamage(effectiveSpiritDamage, ghostSpiritResistance);
         }

         if (hasSpiritOrCorpseOilWeapon || isSkillDamage) {
            long currentTime = System.currentTimeMillis();
            UUID playerUUID = player.getUuid();
            Long lastAttackTime = SPIRIT_WEAPON_COOLDOWN.get(playerUUID);
            if (lastAttackTime != null) {
               long timeDiff = currentTime - lastAttackTime;
               if (timeDiff < 1000L) {
                  float cooldownMultiplier = (float)timeDiff / 1000.0F;
                  actualSpiritDamage *= cooldownMultiplier;
               }
            }

            SPIRIT_WEAPON_COOLDOWN.put(playerUUID, currentTime);
         }

         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         LOGGER.debug(
            "玩家 {} 灵异伤害为 {} 鬼的灵异抗性为 {} 攻击鬼造成的实际灵异伤害: {}", player.getName().getString(), effectiveSpiritDamage, ghostSpiritResistance, actualSpiritDamage
         );
         if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(player.getUuid()) && actualSpiritDamage > 0.0F) {
            player.sendMessage(
               Text.literal("§a对 §f" + this.getName().getString() + " §a造成了§f " + new DecimalFormat("#.###").format(actualSpiritDamage) + " §a点灵异伤害"), true
            );
         }

         if (actualSpiritDamage > 0.0F && !this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)this.getWorld();
            Vec3d pos = this.getPos();
            serverWorld.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double offsetY = this.getRandom().nextDouble() * 1.5 + 0.5;
               double offsetZ = (this.getRandom().nextDouble() - 0.5) * 2.0;
               double velocityX = (this.getRandom().nextDouble() - 0.5) * 0.2;
               double velocityY = this.getRandom().nextDouble() * 0.3 + 0.1;
               double velocityZ = (this.getRandom().nextDouble() - 0.5) * 0.2;
               serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.5);
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isChasing() && this.attackCooldown <= 0) {
            this.setResentmentValue(100);
            LOGGER.debug("玩家 {} 攻击鬼，触发受击反击，怨气值直接积满至100", player.getName().getString());
         }

         if (actualSpiritDamage > 0.0F) {
            RedGhostCandleItem.consumeDurabilityOnSpiritDamage(player, actualSpiritDamage);
         }

         return false;
      } else if (ModEvents.processingSpiritDamage.get()) {
         float ghostSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage = PlayerEvents.calculateSpiritDamage(amount, ghostSpiritResistance);
         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
            LOGGER.debug("鬼 {} 灵异强度为0，进入死机状态", this.getName().getString());
         }

         if (amount > 0.0F && !this.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)this.getWorld();
            Vec3d pos = this.getPos();

            for (int i = 0; i < 15; i++) {
               double offsetX = (this.getRandom().nextDouble() - 0.5) * 1.5;
               double offsetY = this.getRandom().nextDouble() * 1.0 + 0.5;
               double offsetZ = (this.getRandom().nextDouble() - 0.5) * 1.5;
               double velocityX = (this.getRandom().nextDouble() - 0.5) * 0.1;
               double velocityY = this.getRandom().nextDouble() * 0.2 + 0.05;
               double velocityZ = (this.getRandom().nextDouble() - 0.5) * 0.1;
               serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.3);
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public void equipStack(EquipmentSlot slot, ItemStack stack) {
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      return ItemStack.EMPTY;
   }

   public void tick() {
      super.tick();
      if (!this.isRemoved() && this.isAlive()) {
         if (!this.getWorld().isClient) {
            if (this.attackCooldown > 0) {
               this.attackCooldown--;
               LOGGER.debug("Ghost {} attack cooldown: {}", this.getUuidAsString(), this.attackCooldown);
            }

            if (this.whiteCandleTargetLocked && this.whiteCandleTargetLockTimer > 0) {
               this.whiteCandleTargetLockTimer--;
               if (this.whiteCandleTargetLockTimer <= 0) {
                  this.whiteCandleTargetLocked = false;
                  LOGGER.debug("Ghost {} white candle target lock expired", this.getUuidAsString());
               }
            }

            this.positionCheckTimer++;
            if (this.positionCheckTimer >= 60) {
               Vec3d currentPosition = this.getPos();
               if (this.lastPosition != null
                  && currentPosition.distanceTo(this.lastPosition) < 0.1
                  && this.isChasing
                  && this.chaseTarget != null
                  && this.chaseTarget.isAlive()) {
                  this.breakBlocksInPath(this.chaseTarget);
               }

               this.lastPosition = currentPosition;
               this.positionCheckTimer = 0;
            }

            if (this.resentmentTimer > 0) {
               this.resentmentTimer--;
               if (this.resentmentTimer <= 0) {
                  this.decreaseResentment(1);
                  this.resentmentTimer = 20;
                  LOGGER.debug("Ghost {} resentment value: {}", this.getUuidAsString(), this.resentmentValue);
               }
            }

            if (this.isChasing && this.chaseTarget != null && this.chaseTarget.isAlive()) {
               this.chaseTimer--;
               if (this.getNavigation().isIdle()) {
                  this.getNavigation().startMovingTo(this.chaseTarget, 2.4);
                  LOGGER.debug("Ghost {} started chasing target with speed 2.4", this.getUuidAsString());
               }

               if (this.chaseTarget.isAlive() && !this.isSuppressed() && !this.isDeadlocked()) {
                  this.getLookControl().lookAt(this.chaseTarget, 30.0F, 30.0F);
               }

               double distance = this.squaredDistanceTo(this.chaseTarget);
               if (distance <= 4.0) {
                  if (this.chaseTarget instanceof PlayerEntity player) {
                     this.executeAttack(player);
                  } else if (this.chaseTarget instanceof GhostEntity ghost) {
                     this.executeAttack(ghost);
                  }

                  this.isChasing = false;
                  this.chaseTarget = null;
                  this.chaseTimer = 0;
                  this.getNavigation().stop();
                  LOGGER.debug("Ghost {} attacked chase target at close range", this.getUuidAsString());
               } else if (this.chaseTimer <= 0) {
                  int difficulty = WorldConfig.getInstance(this.getWorld()).modDifficulty;
                  if (this.chaseTarget instanceof PlayerEntity player) {
                     if (difficulty == 2) {
                        this.executeAttack(player, true);
                        LOGGER.debug("Ghost {} executed timeout teleport attack on player", this.getUuidAsString());
                     } else {
                        player.sendMessage(Text.literal("§c你侥幸躲过了一次厉鬼的袭击..."), true);
                        LOGGER.debug("Player {} avoided ghost timeout attack", player.getUuidAsString());
                     }
                  } else if (this.chaseTarget instanceof GhostEntity ghost) {
                     this.executeAttack(ghost, true);
                     LOGGER.debug("Ghost {} executed timeout teleport attack on another ghost", this.getUuidAsString());
                  }

                  this.isChasing = false;
                  this.chaseTarget = null;
                  this.chaseTimer = 0;
                  this.getNavigation().stop();
               }
            } else if (this.isChasing && (this.chaseTarget == null || !this.chaseTarget.isAlive())) {
               this.isChasing = false;
               this.chaseTarget = null;
               this.chaseTimer = 0;
               this.getNavigation().stop();
               if (this.getTarget() != null && !this.getTarget().isAlive()) {
                  this.setTarget(null);
               }
            }

            if (this.attackCooldown <= 0 && !this.isChasing) {
               if (this.hasGhostFactionTarget()) {
                  if (this.getGhostFactionTarget() instanceof GhostEntity ghost
                     && ghost.isAlive()
                     && this.squaredDistanceTo(ghost) <= 256.0
                     && !this.isDeadlocked()
                     && !ghost.isDeadlocked()) {
                     boolean isImmune = false;
                     if (ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
                        if (this instanceof PlayerGhostEntity currentGhost) {
                           if (currentGhost.isServantMode()
                              && currentGhost.getMasterUuid() != null
                              && currentGhost.getMasterUuid().equals(playerGhost.getMasterUuid())) {
                              isImmune = true;
                           }
                        } else if (this instanceof GhostChildEntity currentGhost
                           && currentGhost.getOwner() != null
                           && currentGhost.getOwner().getUuid().equals(playerGhost.getMasterUuid())) {
                           isImmune = true;
                        }
                     }

                     if (ghost instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null) {
                        if (this instanceof PlayerGhostEntity currentGhost) {
                           if (currentGhost.isServantMode()
                              && currentGhost.getMasterUuid() != null
                              && currentGhost.getMasterUuid().equals(ghostChild.getOwner().getUuid())) {
                              isImmune = true;
                           }
                        } else if (this instanceof GhostChildEntity currentGhost
                           && currentGhost.getOwner() != null
                           && currentGhost.getOwner().getUuid().equals(ghostChild.getOwner().getUuid())) {
                           isImmune = true;
                        }
                     }

                     if (!isImmune) {
                        this.executeAttack(ghost);
                     }
                  }
               } else {
                  for (GhostEntity ghost : this.getWorld().getEntitiesByClass(GhostEntity.class, this.getBoundingBox().expand(16.0), ghostx -> ghostx != this)) {
                     if (this.shouldAttackGhost(ghost)) {
                        this.executeAttack(ghost);
                        break;
                     }
                  }
               }
            }

            if (this.isSuppressed() || this.isDeadlocked() || this.movementDisabled) {
               this.setVelocity(Vec3d.ZERO);
               if (this.walkingStateManager != null) {
                  this.walkingStateManager.reset();
               }

               if (this.isDeadlocked()) {
                  this.setTarget(null);
               }
            } else if (this.walkingStateManager != null) {
               this.walkingStateManager.update(this.age);
            }

            double ghostDomainRadius = this.getGhostDomainRadius();
            if (ghostDomainRadius <= 0.0) {
               ghostDomainRadius = 48.0;
            }

            double maxDistanceSquared = ghostDomainRadius * ghostDomainRadius;

            for (PlayerEntity player : this.getWorld().getPlayers().stream().filter(playerx -> {
               double dx = playerx.getX() - this.getX();
               double dy = playerx.getY() - this.getY();
               double dz = playerx.getZ() - this.getZ();
               double distanceSq = dx * dx + dy * dy + dz * dz;
               return distanceSq <= maxDistanceSquared;
            }).filter(playerx -> !this.isPlayerProtected(playerx)).collect(Collectors.toList())) {
               double distance = this.squaredDistanceTo(player);
               ModConfig config = ModConfig.getInstance();
               if (config.enableResentmentSystem && !this.isResentmentSystemDisabled()) {
                  int resentmentGain = this.calculateResentmentGainByDistance(distance);
                  if (resentmentGain > 0 && this.getWorld().getTime() % 36L == 0L) {
                     this.increaseResentment(resentmentGain);
                  }

                  if (this.isResentmentThresholdReached() && this.attackCooldown <= 0 && !this.isChasing) {
                     if (this.getGhostDomainLevel() < 1 && this.isEnableChaseAfterRule()) {
                        this.isChasing = true;
                        this.chaseTarget = player;
                        this.setTarget(player);
                        this.chaseTimer = 600;
                        this.setResentmentValue(30);
                        break;
                     }

                     this.setTarget(player);
                     this.executeAttack(player);
                     this.setResentmentValue(30);
                     break;
                  }
               }
            }

            if (this.getTarget() instanceof GhostEntity targetGhost && targetGhost.isDeadlocked()) {
               this.setTarget(null);
            }

            if (this.getSpiritualStrength() < this.getMaxSpiritualStrength() && this.getWorld().getTime() % 20L == 0L) {
               int recovery = (int)(this.getRecoveryFactor() * 10.0F);
               int newStrength = Math.min(this.getSpiritualStrength() + recovery, this.getMaxSpiritualStrength());
               this.setSpiritualStrength(newStrength);
            }

            if (this.visibleTicks > 0) {
               this.visibleTicks--;
               if (this.visibleTicks == 0) {
                  this.isVisible = false;
                  this.updateActualVisibility();
               }
            }

            if (this.getSpiritualStrength() < 400 && !this.isDeadlocked()) {
               this.setDeadlocked(true);
            }

            if (this.isDeadlocked() && this.getSpiritualStrength() > this.getMaxSpiritualStrength() * 0.2F) {
               this.setDeadlocked(false);
            }

            if (this.getWorld().getRegistryKey() == World.OVERWORLD) {
               boolean isPlayerGhost = this instanceof PlayerGhostEntity;
               if (GhostSpawnManager.isGhostLocked(this.getType()) && !isPlayerGhost) {
                  LOGGER.debug("鬼 {} 在主世界中被检测到锁死状态，自动移除", this.getType().toString());
                  GhostDeathHandler.markLegitimateRemoval(this);
                  this.discard();
                  return;
               }
            }

            if (!this.isSuppressed() && !this.isDeadlocked() && this.ghostDomainEnabled && this.hasGhostDomain) {
               double radius = this.getGhostDomainRadius();

               for (PlayerEntity player : this.getWorld().getPlayers().stream().filter(playerx -> {
                  double dx = playerx.getX() - this.getX();
                  double dy = playerx.getY() - this.getY();
                  double dz = playerx.getZ() - this.getZ();
                  double distanceSq = dx * dx + dy * dy + dz * dz;
                  return distanceSq <= radius * radius;
               }).collect(Collectors.toList())) {
                  int ghostLevel = this.getGhostDomainLevel();
                  boolean isImmune = GhostDomainManager.isImmuneToGhostDomain(player, ghostLevel);
                  boolean shouldAttack = this.killingRulesEnabled && this.shouldAttackPlayer(player) && !this.isPlayerProtected(player);
                  if (shouldAttack && this.getTarget() != player) {
                     if (this.getGhostDomainLevel() < 1 && this.isEnableChaseAfterRule()) {
                        if (!this.getWorld().isClient()) {
                           ServerWorld serverWorld = (ServerWorld)this.getWorld();
                           serverWorld.playSound(
                              null,
                              player.getBlockPos(),
                              SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE,
                              SoundCategory.HOSTILE,
                              1.0F,
                              0.8F + this.getRandom().nextFloat() * 0.4F
                           );
                        }

                        this.isChasing = true;
                        this.chaseTarget = player;
                        this.chaseTimer = 600;
                        this.setTarget(player);
                        LOGGER.debug("Ghost {} started chasing due to killing rules", this.getUuidAsString());
                        break;
                     }

                     this.executeAttack(player);
                     LOGGER.debug(
                        "Ghost {} with domain level {} directly attacked player due to killing rules", this.getUuidAsString(), this.getGhostDomainLevel()
                     );
                     break;
                  }

                  if (!this.isHoldingGhostBuddhaBeads(player)) {
                     if (GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)) {
                        player.removeStatusEffect(ModEffects.LOST);
                     } else {
                        if (GhostDomainManager.isGhostDomainActive(player)) {
                           int playerDomainLevel = GhostDomainManager.getCurrentLevel(player);
                           int ghostActualDomainLevel = this.getGhostDomainActualLevel();
                           if (playerDomainLevel > ghostActualDomainLevel - 1) {
                              player.removeStatusEffect(StatusEffects.SLOWNESS);
                              player.removeStatusEffect(ModEffects.LOST);
                              player.removeStatusEffect(ModEffects.RED_GHOST_DOMAIN_TARGET);
                              player.removeStatusEffect(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
                              player.removeStatusEffect(ModEffects.THICK_FOG_TARGET);
                              player.removeStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
                              player.removeStatusEffect(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
                              player.removeStatusEffect(StatusEffects.POISON);
                              player.removeStatusEffect(ModEffects.LOST);
                              continue;
                           }
                        }

                        if (!isImmune) {
                           if (this.getGhostDomainLevel() >= 1) {
                              this.applyDefaultEffects(player);
                           }

                           if (this.getGhostDomainLevel() >= 2) {
                              player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1, false, false));
                              ModConfig config = ModConfig.getInstance();
                              if (config.enableGhostDomainEffectsOnMobs) {
                                 for (LivingEntity entity : this.getWorld()
                                    .getEntitiesByClass(
                                       LivingEntity.class,
                                       this.getBoundingBox().expand(this.getGhostDomainRadius()),
                                       entityx -> entityx != this
                                          && !(entityx instanceof PlayerEntity)
                                          && !entityx.isSpectator()
                                          && !(entityx instanceof GhostEntity)
                                    )) {
                                    StatusEffectInstance lostEffect = new LostStatusEffectInstance(
                                       ModEffects.LOST, Integer.MAX_VALUE, 0, false, false, false, this.getUuid()
                                    );
                                    entity.addStatusEffect(lostEffect);
                                 }
                              }
                           }

                           if (this.getGhostDomainLevel() >= 3) {
                              if (!player.isSpectator()) {
                                 StatusEffectInstance lostEffect = new LostStatusEffectInstance(
                                    ModEffects.LOST, Integer.MAX_VALUE, 0, false, false, false, this.getUuid()
                                 );
                                 player.addStatusEffect(lostEffect);
                              }

                              if (this instanceof PlayerGhostEntity playerGhost) {
                                 String domainColor = playerGhost.getGhostDomainColor();
                                 if ("red".equals(domainColor)) {
                                    player.addStatusEffect(
                                       new StatusEffectInstance(
                                          ModEffects.RED_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("blindness".equals(domainColor)) {
                                    player.addStatusEffect(
                                       new StatusEffectInstance(
                                          ModEffects.BLACK_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("fog".equals(domainColor)) {
                                    player.addStatusEffect(
                                       new StatusEffectInstance(
                                          ModEffects.THICK_FOG_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("golden".equals(domainColor)) {
                                    player.addStatusEffect(
                                       new StatusEffectInstance(
                                          ModEffects.GOLDEN_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("green".equals(domainColor)) {
                                    player.addStatusEffect(
                                       new StatusEffectInstance(
                                          ModEffects.GREEN_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 }
                              }

                              if (this.getGhostDomainLevel() >= 4) {
                                 player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 0, false, false));
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (ENABLE_INVULNERABLE && ENABLE_BLACK_PARTICLES && this.getWorld().isClient && this.getVisible() && !this.isSuppressed() && !this.isDeadlocked()) {
               Vec3d pos = this.getPos();

               for (int i = 0; i < 5; i++) {
                  double offsetX = this.random.nextGaussian() * 0.5;
                  double offsetY = this.random.nextGaussian() * 0.5;
                  double offsetZ = this.random.nextGaussian() * 0.5;
                  this.getWorld().addParticle(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0.0, 0.0, 0.0);
               }
            }
         }

         if (this.handSwinging) {
            this.handSwingTicks++;
            if (this.handSwingTicks >= 20) {
               this.handSwinging = false;
               this.handSwingTicks = 0;
            }
         }
      } else {
         if (this.walkingStateManager != null) {
            this.walkingStateManager.reset();
         }
      }
   }

   public void checkDespawn() {
      if (ModConfig.getInstance().allowEvilGhostNaturalDespawn) {
         super.checkDespawn();
      }
   }

   public void onDeath(DamageSource damageSource) {
      if (damageSource.getAttacker() instanceof PlayerEntity player) {
         player.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
      }

      this.setHealth(this.getMaxHealth());
   }

   public boolean isDead() {
      return false;
   }

   public boolean isAlive() {
      return true;
   }

   public void setHealth(float health) {
      if (health <= 0.0F) {
         float maxHealth = this.getMaxHealth();
         super.setHealth(maxHealth);
         PlayerEntity nearestPlayer = this.findNearestPlayer();
         if (nearestPlayer != null) {
            GhostDeathHandler.handleIllegalKillPunishment(nearestPlayer, this, ModDamageSources.ghost(this.getWorld()));
         }

         if (nearestPlayer != null) {
            nearestPlayer.sendMessage(Text.translatable("entity.smfs.ghost.immortal_warning"), true);
         }
      } else {
         super.setHealth(health);
      }
   }

   private PlayerEntity findNearestPlayer() {
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         List<ServerPlayerEntity> nearbyPlayers = serverWorld.getPlayers(player -> player.squaredDistanceTo(this) <= 2500.0);
         return nearbyPlayers.isEmpty()
            ? null
            : (PlayerEntity)nearbyPlayers.stream()
               .min((p1, p2) -> Float.compare((float)p1.squaredDistanceTo(this), (float)p2.squaredDistanceTo(this)))
               .orElse(null);
      } else {
         return null;
      }
   }

   protected void drop(DamageSource source) {
   }

   protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
   }

   public Iterable<ItemStack> getArmorItems() {
      return Collections.emptyList();
   }

   public ItemStack getStackInHand(Hand hand) {
      return ItemStack.EMPTY;
   }

   public int getSpiritualStrength() {
      return (Integer)this.dataTracker.get(SPIRITUAL_STRENGTH);
   }

   public int getMaxSpiritualStrength() {
      return (Integer)this.dataTracker.get(MAX_SPIRITUAL_STRENGTH);
   }

   public int getSpiritualResistance() {
      return (Integer)this.dataTracker.get(SPIRITUAL_RESISTANCE);
   }

   public int getSpiritualDamage() {
      return (Integer)this.dataTracker.get(SPIRITUAL_DAMAGE);
   }

   public float getRecoveryFactor() {
      return (Float)this.dataTracker.get(RECOVERY_FACTOR);
   }

   public void setSpiritualStrength(int strength) {
      this.dataTracker.set(SPIRITUAL_STRENGTH, Math.max(0, strength));
   }

   public void setMaxSpiritualStrength(int maxStrength) {
      this.dataTracker.set(MAX_SPIRITUAL_STRENGTH, Math.max(0, maxStrength));
   }

   public void setSpiritualResistance(int resistance) {
      this.dataTracker.set(SPIRITUAL_RESISTANCE, Math.max(0, resistance));
   }

   public void setSpiritualDamage(int damage) {
      this.dataTracker.set(SPIRITUAL_DAMAGE, Math.max(0, damage));
   }

   public void setRecoveryFactor(float factor) {
      this.dataTracker.set(RECOVERY_FACTOR, Math.max(0.0F, factor));
   }

   public float getGhostDomainRadius() {
      return (Float)this.dataTracker.get(GHOST_DOMAIN_RADIUS);
   }

   public void setGhostDomainRadius(float radius) {
      this.dataTracker.set(GHOST_DOMAIN_RADIUS, radius);
   }

   public void setGhostDomainEnabled(boolean enabled) {
      this.ghostDomainEnabled = enabled;
   }

   public char getTerrorLevel() {
      return this.terrorLevel;
   }

   public void setTerrorLevel(char level) {
      char processedLevel = Character.toUpperCase(level);
      if (processedLevel < 'A' || processedLevel > 'S' || processedLevel > 'D' && processedLevel < 'S') {
         processedLevel = 'C';
      }

      this.terrorLevel = processedLevel;
      this.dataTracker.set(RECOVERY_FACTOR, this.getRecoveryFactorByTearorLevel(this.terrorLevel));
   }

   public boolean hasGhostDomain() {
      return this.hasGhostDomain;
   }

   public void setHasGhostDomain(boolean hasGhostDomain) {
      this.hasGhostDomain = hasGhostDomain;
   }

   public int getGhostDomainLevel() {
      return this.ghostDomainLevel;
   }

   public void setGhostDomainLevel(int level) {
      this.ghostDomainLevel = level;
   }

   public int getGhostDomainActualLevel() {
      return this.ghostDomainActualLevel == -1 ? 3 : this.ghostDomainActualLevel;
   }

   public void setGhostDomainActualLevel(int level) {
      if (this.ghostDomainActualLevel != level) {
         this.ghostDomainActualLevel = level;
         LOGGER.debug("鬼 {} 的鬼蜮实际等级设置为: {}", this.getName().getString(), level);
      }
   }

   protected Random getGhostRandom() {
      return this.random;
   }

   public boolean tryAttack(Entity target) {
      if (!this.isDeadlocked() && !this.isSuppressed()) {
         float f = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
         float g = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
         if (target instanceof LivingEntity) {
            f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), ((LivingEntity)target).getGroup());
            g += EnchantmentHelper.getKnockback(this);
         }

         int i = EnchantmentHelper.getFireAspect(this);
         if (i > 0) {
            target.setOnFireFor(i * 4);
         }

         DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
         boolean bl = false;
         if (target instanceof PlayerEntity) {
            PlayerEvents.handleSpiritDamage((PlayerEntity)target, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
            bl = true;
         } else if (target instanceof GhostEntity targetGhost) {
            if (!targetGhost.isDeadlocked() && !targetGhost.isSuppressed() && targetGhost.getSpiritualStrength() > 0) {
               bl = targetGhost.damage(this.getDamageSources().mobAttack(this), this.getSpiritualDamage());
            }
         } else if (target instanceof GhostMasterEntity ghostMaster) {
            if (!ghostMaster.isSuppressed() && !ghostMaster.isDeadlocked()) {
               bl = ghostMaster.damage(this.getDamageSources().mobAttack(this), this.getSpiritualDamage());
            }
         } else if (target instanceof LivingEntity) {
            bl = target.damage(damageSource, this.getSpiritualDamage());
         } else {
            bl = target.damage(this.getDamageSources().mobAttack(this), f);
         }

         if (bl) {
            if (g > 0.0F && target instanceof LivingEntity) {
               ((LivingEntity)target)
                  .takeKnockback(
                     g * 0.5F, MathHelper.sin(this.getYaw() * (float) (Math.PI / 180.0)), -MathHelper.cos(this.getYaw() * (float) (Math.PI / 180.0))
                  );
               this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }

            this.applyDamageEffects(this, target);
            this.onAttacking(target);
         }

         return bl;
      } else {
         return false;
      }
   }

   public boolean shouldAttackPlayer(PlayerEntity player) {
      return false;
   }

   public boolean isPlayerProtected(PlayerEntity player) {
      return RedGhostCandleItem.isHoldingCandle(player)
         || CoffinEffectManager.isPlayerInGoldCoffin(player)
         || GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)
         || SpectateModePacket.isInSpectatorMode(player.getUuid())
         || player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY);
   }

   public boolean isTargetValid(LivingEntity target) {
      if (target != null && target.isAlive()) {
         if (target instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
            if (this instanceof PlayerGhostEntity currentGhost) {
               if (currentGhost.isServantMode() && currentGhost.getMasterUuid() != null && currentGhost.getMasterUuid().equals(playerGhost.getMasterUuid())) {
                  return false;
               }
            } else if (this instanceof GhostChildEntity currentGhost
               && currentGhost.getOwner() != null
               && currentGhost.getOwner().getUuid().equals(playerGhost.getMasterUuid())) {
               return false;
            }
         }

         if (target instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null) {
            if (this instanceof PlayerGhostEntity currentGhost) {
               if (currentGhost.isServantMode() && currentGhost.getMasterUuid() != null && currentGhost.getMasterUuid().equals(ghostChild.getOwner().getUuid())
                  )
                {
                  return false;
               }
            } else if (this instanceof GhostChildEntity currentGhost
               && currentGhost.getOwner() != null
               && currentGhost.getOwner().getUuid().equals(ghostChild.getOwner().getUuid())) {
               return false;
            }
         }

         if (target instanceof PlayerEntity && this.isPlayerProtected((PlayerEntity)target)) {
            return false;
         } else {
            return target instanceof GhostEntity ghost ? !ghost.isDeadlocked() : true;
         }
      } else {
         return false;
      }
   }

   public void setTarget(@Nullable LivingEntity target) {
      if (target != null && !this.isTargetValid(target)) {
         target = null;
      }

      super.setTarget(target);
   }

   protected boolean shouldAttackGhost(GhostEntity ghost) {
      if (this.isDeadlocked()) {
         return false;
      }

      if (ghost.isDeadlocked()) {
         this.setTarget(null);
         return false;
      }

      if (ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
         if (this instanceof PlayerGhostEntity currentGhost) {
            if (currentGhost.isServantMode() && currentGhost.getMasterUuid() != null && currentGhost.getMasterUuid().equals(playerGhost.getMasterUuid())) {
               return false;
            }
         } else if (this instanceof GhostChildEntity currentGhost
            && currentGhost.getOwner() != null
            && currentGhost.getOwner().getUuid().equals(playerGhost.getMasterUuid())) {
            return false;
         }
      }

      if (ghost instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null) {
         if (this instanceof PlayerGhostEntity currentGhost) {
            if (currentGhost.isServantMode() && currentGhost.getMasterUuid() != null && currentGhost.getMasterUuid().equals(ghostChild.getOwner().getUuid())) {
               return false;
            }
         } else if (this instanceof GhostChildEntity currentGhost
            && currentGhost.getOwner() != null
            && currentGhost.getOwner().getUuid().equals(ghostChild.getOwner().getUuid())) {
            return false;
         }
      }

      return ghost != this
         && this.squaredDistanceTo(ghost) <= 256.0
         && this.attackCooldown <= 0
         && this.hasGhostFactionTarget()
         && this.getGhostFactionTarget() == ghost;
   }

   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked() && !this.isSuppressed) {
         this.executeAttack(player, false);
      }
   }

   protected void executeAttack(PlayerEntity player, boolean isTimeoutAttack) {
      if (!this.isDeadlocked() && !this.isSuppressed) {
         double distance = this.squaredDistanceTo(player);
         this.handSwinging = true;
         this.handSwingTicks = 0;
         boolean attacked = this.tryAttack(player);
         if (attacked) {
            if (distance > 25.0 || isTimeoutAttack) {
               this.teleport(player.getX(), player.getY(), player.getZ());
            }

            this.attackCooldown = 100;
         }
      }
   }

   protected void executeAttack(GhostEntity ghost) {
      this.executeAttack(ghost, false);
   }

   protected void executeAttack(GhostEntity ghost, boolean isTimeoutAttack) {
      if (!this.isDeadlocked() && !this.isSuppressed) {
         if (this.shouldAttackGhost(ghost)) {
            double distance = this.squaredDistanceTo(ghost);
            this.handSwinging = true;
            this.handSwingTicks = 0;
            boolean attacked = this.tryAttack(ghost);
            if (attacked) {
               if (distance > 25.0 || isTimeoutAttack) {
                  this.teleport(ghost.getX(), ghost.getY(), ghost.getZ());
               }

               this.attackCooldown = 100;
            }
         }
      }
   }

   protected void executeAttack(LivingEntity entity) {
      this.executeAttack(entity, false);
   }

   protected void executeAttack(LivingEntity entity, boolean isTimeoutAttack) {
      if (!this.isDeadlocked() && !this.isSuppressed) {
         double distance = this.squaredDistanceTo(entity);
         this.handSwinging = true;
         this.handSwingTicks = 0;
         boolean attacked = this.tryAttack(entity);
         if (attacked) {
            if (distance > 25.0 || isTimeoutAttack) {
               this.teleport(entity.getX(), entity.getY(), entity.getZ());
            }

            this.attackCooldown = 100;
         }
      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("SpiritualStrength", this.getSpiritualStrength());
      nbt.putInt("MaxSpiritualStrength", this.getMaxSpiritualStrength());
      nbt.putInt("SpiritualResistance", this.getSpiritualResistance());
      nbt.putInt("SpiritualDamage", this.getSpiritualDamage());
      nbt.putFloat("RecoveryFactor", this.getRecoveryFactor());
      nbt.putBoolean("IsSuppressed", this.isSuppressed);
      nbt.putBoolean("IsDeadlocked", this.isDeadlocked());
      nbt.putBoolean("MovementDisabled", this.movementDisabled);
      nbt.putBoolean("GhostDomainEnabled", this.ghostDomainEnabled);
      nbt.putBoolean("HasGhostDomain", this.hasGhostDomain);
      nbt.putInt("GhostDomainLevel", this.ghostDomainLevel);
      nbt.putInt("GhostDomainActualLevel", this.ghostDomainActualLevel);
      nbt.putString("TerrorLevel", String.valueOf(this.terrorLevel));
      nbt.putFloat("GhostDomainRadius", this.getGhostDomainRadius());
      nbt.putBoolean("EnableChaseAfterRule", this.enableChaseAfterRule);
      if (this.walkingStateManager != null) {
         nbt.putBoolean("IsWalking", this.walkingStateManager.isWalking());
      } else {
         nbt.putBoolean("IsWalking", false);
      }

      if (this.coffinNail != null && !this.coffinNail.isEmpty()) {
         NbtCompound nailNbt = new NbtCompound();
         this.coffinNail.writeNbt(nailNbt);
         nbt.put("CoffinNail", nailNbt);
      }

      if (this.ghostFactionTarget != null && this.ghostFactionTarget.isAlive()) {
         nbt.putUuid("GhostFactionTarget", this.ghostFactionTarget.getUuid());
      }
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("SpiritualStrength")) {
         this.setSpiritualStrength(nbt.getInt("SpiritualStrength"));
      }

      if (nbt.contains("MaxSpiritualStrength")) {
         this.setMaxSpiritualStrength(nbt.getInt("MaxSpiritualStrength"));
      }

      if (nbt.contains("SpiritualResistance")) {
         this.dataTracker.set(SPIRITUAL_RESISTANCE, nbt.getInt("SpiritualResistance"));
      }

      if (nbt.contains("SpiritualDamage")) {
         this.dataTracker.set(SPIRITUAL_DAMAGE, nbt.getInt("SpiritualDamage"));
      }

      if (nbt.contains("RecoveryFactor")) {
         this.dataTracker.set(RECOVERY_FACTOR, nbt.getFloat("RecoveryFactor"));
      }

      if (nbt.contains("IsSuppressed")) {
         this.isSuppressed = nbt.getBoolean("IsSuppressed");
      }

      if (nbt.contains("IsDeadlocked")) {
         this.setDeadlocked(nbt.getBoolean("IsDeadlocked"));
      }

      if (nbt.contains("MovementDisabled")) {
         this.movementDisabled = nbt.getBoolean("MovementDisabled");
      }

      if (nbt.contains("GhostDomainEnabled")) {
         this.ghostDomainEnabled = nbt.getBoolean("GhostDomainEnabled");
      }

      if (nbt.contains("HasGhostDomain")) {
         this.hasGhostDomain = nbt.getBoolean("HasGhostDomain");
      }

      if (nbt.contains("GhostDomainLevel")) {
         this.ghostDomainLevel = nbt.getInt("GhostDomainLevel");
      }

      if (nbt.contains("GhostDomainActualLevel")) {
         this.ghostDomainActualLevel = nbt.getInt("GhostDomainActualLevel");
      }

      if (nbt.contains("IsWalking")) {
         boolean savedWalkingState = nbt.getBoolean("IsWalking");
         if (this.walkingStateManager != null && savedWalkingState) {
            this.walkingStateManager.setForcedWalking(true);
         }
      }

      if (nbt.contains("TerrorLevel")) {
         this.terrorLevel = nbt.getString("TerrorLevel").charAt(0);
      }

      if (nbt.contains("GhostDomainRadius")) {
         this.setGhostDomainRadius(nbt.getFloat("GhostDomainRadius"));
      }

      if (nbt.contains("EnableChaseAfterRule")) {
         this.enableChaseAfterRule = nbt.getBoolean("EnableChaseAfterRule");
      } else {
         this.enableChaseAfterRule = true;
      }

      if (nbt.contains("CoffinNail")) {
         NbtCompound nailNbt = nbt.getCompound("CoffinNail");
         this.coffinNail = ItemStack.fromNbt(nailNbt);
      }

      if (nbt.contains("GhostFactionTarget")) {
      }
   }

   @Nullable
   public GhostSlaveEntity spawnGhostSlave() {
      if (this.getWorld().isClient) {
         return null;
      }

      try {
         GhostSlaveEntity slave = GhostSlaveEntity.createWithMaster(this.getWorld(), this);
         if (this.getWorld().spawnEntity(slave)) {
            LOGGER.debug("鬼 {} 成功生成鬼奴", this.getName().getString());
            return slave;
         } else {
            LOGGER.warn("鬼 {} 生成鬼奴失败", this.getName().getString());
            return null;
         }
      } catch (Exception e) {
         LOGGER.error("鬼 {} 生成鬼奴时发生错误", this.getName().getString(), e);
         return null;
      }
   }

   public int getGhostSlaveCount() {
      if (this.getWorld().isClient) {
         return 0;
      }

      double radius = this.getGhostDomainRadius() > 0.0F ? this.getGhostDomainRadius() : 48.0;
      List<GhostSlaveEntity> ghostSlaves = this.getWorld().getEntitiesByClass(GhostSlaveEntity.class, this.getBoundingBox().expand(radius), slave -> {
         Entity master = slave.getMaster();
         return master != null && master.getUuid().equals(this.getUuid());
      });
      return ghostSlaves.size();
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "attack", 10, this::handleAttackAnimations));
      controllers.add(new AnimationController<>(this, "movement", 5, this::handleMovementAnimations));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object object) {
      return this.age;
   }

   protected PlayState handleMovementAnimations(AnimationState<GhostEntity> state) {
      if (this.isRemoved() || !this.isAlive()) {
         return PlayState.STOP;
      }

      if (this.isInCustomAnimation()) {
         return PlayState.STOP;
      }

      boolean canMove = !this.isSuppressed() && !this.isDeadlocked() && !this.movementDisabled;
      if (canMove && this.walkingStateManager != null && this.walkingStateManager.isForcedWalking()) {
         return state.setAndContinue(WALK_ANIM);
      }

      boolean grounded = this.isOnGround();
      double speedSq = this.getVelocity().horizontalLengthSquared();
      if (this.handSwinging) {
         return PlayState.STOP;
      }

      if (canMove && grounded && speedSq > 0.001) {
         return state.setAndContinue(WALK_ANIM);
      }

      if (state.getController().getCurrentAnimation() != null && state.getController().getCurrentAnimation().animation().equals(WALK_ANIM)) {
         state.getController().forceAnimationReset();
      }

      return state.setAndContinue(IDLE_ANIM);
   }

   protected PlayState handleAttackAnimations(AnimationState<GhostEntity> state) {
      if (this.isRemoved() || !this.isAlive()) {
         return PlayState.STOP;
      } else {
         return this.handSwinging ? state.setAndContinue(ATTACK_ANIM) : PlayState.STOP;
      }
   }

   static class CustomLookAtPlayerGoal extends LookAtEntityGoal {
      private final GhostEntity ghost;

      public CustomLookAtPlayerGoal(GhostEntity ghost, Class<? extends LivingEntity> targetClass, float maxDistance) {
         super(ghost, targetClass, maxDistance);
         this.ghost = ghost;
      }

      public boolean canStart() {
         return !this.ghost.isSuppressed() && !this.ghost.isDeadlocked() ? super.canStart() : false;
      }

      public boolean shouldContinue() {
         return !this.ghost.isSuppressed() && !this.ghost.isDeadlocked() ? super.shouldContinue() : false;
      }
   }

   static class CustomWanderGoal extends WanderAroundFarGoal {
      private final GhostEntity ghost;

      public CustomWanderGoal(GhostEntity ghost, double speed) {
         super(ghost, speed);
         this.ghost = ghost;
      }

      public boolean canStart() {
         if (this.ghost.isSuppressed() || this.ghost.isDeadlocked()) {
            return false;
         }

         if (!this.ghost.isChasing && this.ghost.getTarget() == null) {
            if (this.ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode()) {
               return false;
            } else {
               if (this.ghost.age % (20 + this.ghost.getRandom().nextInt(40)) != 0) {
                  return false;
               }

               if (this.ghost.hasPassengers()) {
                  return false;
               }

               Vec3d wanderTarget = this.getWanderTarget();
               if (wanderTarget == null) {
                  return false;
               }

               this.targetX = wanderTarget.x;
               this.targetY = wanderTarget.y;
               this.targetZ = wanderTarget.z;
               this.ignoringChance = false;
               return true;
            }
         } else {
            return false;
         }
      }

      public boolean shouldContinue() {
         if (!this.ghost.isChasing && this.ghost.getTarget() == null) {
            return this.ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode()
               ? false
               : !this.ghost.isSuppressed() && !this.ghost.isDeadlocked() && super.shouldContinue();
         } else {
            return false;
         }
      }

      public void start() {
         super.start();
         if (this.ghost.walkingStateManager != null) {
            this.ghost.walkingStateManager.setForcedWalking(true);
         }
      }

      public void stop() {
         super.stop();
         if (this.ghost.walkingStateManager != null) {
            this.ghost.walkingStateManager.setForcedWalking(false);
         }
      }
   }
}
