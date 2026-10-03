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
   private ItemStack coffinNail = ItemStack.field_8037;
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
   private static final TrackedData<Boolean> DEADLOCKED = DataTracker.method_12791(GhostEntity.class, TrackedDataHandlerRegistry.field_13323);
   private static final TrackedData<Float> GHOST_DOMAIN_RADIUS = DataTracker.method_12791(GhostEntity.class, TrackedDataHandlerRegistry.field_13320);
   private static final TrackedData<Integer> SPIRITUAL_STRENGTH = DataTracker.method_12791(GhostEntity.class, TrackedDataHandlerRegistry.field_13327);
   private static final TrackedData<Integer> MAX_SPIRITUAL_STRENGTH = DataTracker.method_12791(GhostEntity.class, TrackedDataHandlerRegistry.field_13327);
   private static final TrackedData<Integer> SPIRITUAL_RESISTANCE = DataTracker.method_12791(GhostEntity.class, TrackedDataHandlerRegistry.field_13327);
   private static final TrackedData<Integer> SPIRITUAL_DAMAGE = DataTracker.method_12791(GhostEntity.class, TrackedDataHandlerRegistry.field_13327);
   private static final TrackedData<Float> RECOVERY_FACTOR = DataTracker.method_12791(GhostEntity.class, TrackedDataHandlerRegistry.field_13320);
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
      return this.field_6201;
   }

   public GoalSelector getTargetSelector() {
      return this.field_6185;
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
      return this.ghostFactionTarget != null && this.ghostFactionTarget.method_5805();
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
      double ghostHeight = this.method_17682();
      double targetHeight = target.method_17682();
      Vec3d ghostHeadPos = this.method_19538().method_1031(0.0, ghostHeight, 0.0);
      Vec3d ghostFeetPos = this.method_19538();
      Vec3d targetHeadPos = target.method_19538().method_1031(0.0, targetHeight, 0.0);
      Vec3d targetFeetPos = target.method_19538();
      this.checkAndBreakPath(ghostHeadPos, targetHeadPos);
      this.checkAndBreakPath(ghostFeetPos, targetFeetPos);
   }

   private void checkAndBreakPath(Vec3d startPos, Vec3d endPos) {
      RaycastContext context = new RaycastContext(startPos, endPos, ShapeType.field_17558, FluidHandling.field_1348, this);
      BlockHitResult hitResult = this.method_37908().method_17742(context);
      if (hitResult != null && hitResult.method_17783() == Type.field_1332) {
         BlockPos hitPos = hitResult.method_17777();
         double distance = this.method_19538().method_1022(new Vec3d(hitPos.method_10263() + 0.5, hitPos.method_10264() + 0.5, hitPos.method_10260() + 0.5));
         if (distance > 3.0) {
            return;
         }

         BlockState hitState = this.method_37908().method_8320(hitPos);
         if (!hitState.method_27852(Blocks.field_10205) && !hitState.method_27852(Blocks.field_9987)) {
            this.method_37908().method_22352(hitPos, true);
            LOGGER.debug("Ghost {} broke block at {} (distance: {}) while chasing target", this.method_5845(), hitPos, distance);
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
      int difficulty = WorldConfig.getInstance(this.method_37908()).modDifficulty;
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
      this.method_5648(!actualVisible);
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
      return (Boolean)this.field_6011.method_12789(DEADLOCKED);
   }

   public void setSuppressed(boolean suppressed) {
      this.isSuppressed = suppressed;
      this.field_6011.method_12778(DEADLOCKED, suppressed);
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
         this.method_18799(Vec3d.field_1353);
      }
   }

   public boolean isMovementDisabled() {
      return this.movementDisabled;
   }

   public void setDeadlocked(boolean deadlocked) {
      this.field_6011.method_12778(DEADLOCKED, deadlocked);
      if (deadlocked && !(this instanceof GhostOfficerEntity)) {
         this.clearGhostDomainEffects();
      }
   }

   public boolean method_30948() {
      return !this.isSuppressed() && !this.isDeadlocked() && !this.movementDisabled ? super.method_30948() : false;
   }

   public boolean method_5810() {
      return this.method_37908().method_8390(BoatEntity.class, this.method_5829().method_1014(0.5), Entity::method_5805).isEmpty()
            && this.method_37908().method_8390(AbstractMinecartEntity.class, this.method_5829().method_1014(0.5), Entity::method_5805).isEmpty()
         ? super.method_5810()
         : false;
   }

   public void method_5697(Entity entity) {
      if (!(entity instanceof BoatEntity) && !(entity instanceof AbstractMinecartEntity)) {
         super.method_5697(entity);
      }
   }

   public boolean hasCoffinNail() {
      return this.coffinNail != null && !this.coffinNail.method_7960();
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
      Vec3d center = this.method_19538();

      for (PlayerEntity player : this.method_37908().method_18456()) {
         if (player.method_5707(center) <= radius * radius) {
            player.method_6016(ModEffects.LOST);
            player.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
            player.method_6016(StatusEffects.field_5909);
            player.method_6016(ModEffects.RED_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.THICK_FOG_TARGET);
            player.method_6016(StatusEffects.field_38092);
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
      for (PlayerEntity player : this.method_37908().method_18456()) {
         if (this.isInGhostDomain(player)) {
            player.method_6016(ModEffects.LOST);
            player.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
            player.method_6016(StatusEffects.field_5909);
            player.method_6016(ModEffects.RED_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.BLUE_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.GRAY_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.PURPLE_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.CYAN_GHOST_DOMAIN_TARGET);
            player.method_6016(ModEffects.THICK_FOG_TARGET);
            player.method_6016(StatusEffects.field_38092);
         }
      }
   }

   protected float getDefaultGhostDomainRadius() {
      return 10.0F + this.ghostLevel * 2.0F;
   }

   protected boolean isInGhostDomain(PlayerEntity player) {
      return this.method_5858(player) <= this.getGhostDomainRadius() * this.getGhostDomainRadius();
   }

   private int getDefaultAttackCooldown() {
      return 20;
   }

   public static Builder createGhostAttributes() {
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 100000.0)
         .method_26868(EntityAttributes.field_23719, 0.25)
         .method_26868(EntityAttributes.field_23721, 5.0)
         .method_26868(EntityAttributes.field_23722, 0.0)
         .method_26868(EntityAttributes.field_23717, 16.0);
   }

   protected boolean isHasGhostDomain() {
      return this.hasGhostDomain;
   }

   protected void applyDefaultEffects(PlayerEntity player) {
      player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 200, this.getGhostDomainActualLevel() - 1, false, false));
   }

   protected boolean isHoldingGhostBuddhaBeads(PlayerEntity player) {
      if (player.method_6047().method_7909() instanceof GhostBuddhaBeadsItem) {
         return true;
      }

      if (player.method_6079().method_7909() instanceof GhostBuddhaBeadsItem) {
         return true;
      }

      for (int i = 0; i < player.method_31548().method_5439(); i++) {
         ItemStack stack = player.method_31548().method_5438(i);
         if (stack.method_7909() instanceof GhostBuddhaBeadsItem) {
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
      this.field_6011.method_12778(GHOST_DOMAIN_RADIUS, (float)ghostDomainRadius);
      char processedTerrorLevel = Character.toUpperCase(terrorLevel);
      if (processedTerrorLevel < 'A' || processedTerrorLevel > 'S' || processedTerrorLevel > 'D' && processedTerrorLevel < 'S') {
         processedTerrorLevel = 'C';
      }

      this.terrorLevel = processedTerrorLevel;
      this.attackCooldown = 0;
      int adjustedMaxSpiritualStrength = DifficultyManager.adjustSpiritualStrength(maxSpiritualStrength, this.method_37908());
      int adjustedSpiritualDamage = DifficultyManager.adjustSpiritualDamage(spiritualDamage, this.method_37908());
      int adjustedSpiritualResistance = DifficultyManager.adjustSpiritualResistance(spiritualResistance, this.method_37908());
      this.maxSpiritualStrength = adjustedMaxSpiritualStrength;
      this.spiritualDamage = adjustedSpiritualDamage;
      this.spiritualResistance = adjustedSpiritualResistance;
      this.recoveryFactor = recoveryFactor;
      this.field_6011.method_12778(SPIRITUAL_STRENGTH, (int)(adjustedMaxSpiritualStrength * 0.8F));
      this.field_6011.method_12778(MAX_SPIRITUAL_STRENGTH, adjustedMaxSpiritualStrength);
      this.field_6011.method_12778(SPIRITUAL_RESISTANCE, adjustedSpiritualResistance);
      this.field_6011.method_12778(SPIRITUAL_DAMAGE, adjustedSpiritualDamage);
      this.field_6011.method_12778(RECOVERY_FACTOR, recoveryFactor);
      this.walkingStateManager = new WalkingStateManager(this);
   }

   protected void method_5959() {
      super.method_5959();
      this.field_6201.method_6277(1, new MeleeAttackGoal(this, 1.8, false));
      this.field_6201.method_6277(2, new GhostEntity.CustomWanderGoal(this, 1.0));
      this.field_6201.method_6277(3, new GhostEntity.CustomLookAtPlayerGoal(this, PlayerEntity.class, 32.0F));
      final GhostEntity ghost = this;
      this.field_6185.method_6277(1, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, new Predicate<LivingEntity>() {
         public boolean test(LivingEntity entity) {
            if (ghost.isSuppressed() || ghost.isDeadlocked()) {
               return false;
            } else {
               return entity instanceof PlayerEntity player ? WhiteGhostCandleItem.isHoldingWhiteCandle(player) : false;
            }
         }
      }));
      this.field_6185
         .method_6277(2, new ActiveTargetGoal(this, GhostEntity.class, 10, true, false, otherGhost -> this.shouldAttackGhost((GhostEntity)otherGhost)));
   }

   public float getRecoveryFactorByTearorLevel(char level) {
      return switch (Character.toUpperCase(level)) {
         case 'A' -> 0.6F;
         case 'B' -> 0.4F;
         case 'S' -> 0.8F;
         default -> 0.2F;
      };
   }

   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(DEADLOCKED, false);
      this.field_6011.method_12784(GHOST_DOMAIN_RADIUS, 0.0F);
      this.field_6011.method_12784(SPIRITUAL_STRENGTH, 800);
      this.field_6011.method_12784(MAX_SPIRITUAL_STRENGTH, 1000);
      this.field_6011.method_12784(SPIRITUAL_RESISTANCE, 50);
      this.field_6011.method_12784(SPIRITUAL_DAMAGE, 100);
      this.field_6011.method_12784(RECOVERY_FACTOR, 0.2F);
   }

   public void method_5674(TrackedData<?> data) {
      super.method_5674(data);
      if (data == DEADLOCKED && this.isDeadlocked() && !(this instanceof GhostOfficerEntity)) {
         this.clearGhostDomainEffects();
      }
   }

   public Arm method_6068() {
      return Arm.field_6183;
   }

   public void handleSkillSpiritDamage(ServerPlayerEntity player, float damageAmount) {
      if (!this.method_37908().method_8608()) {
         float ghostSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage = PlayerEvents.calculateSpiritDamage(damageAmount, ghostSpiritResistance);
         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         LOGGER.debug(
            "玩家 {} 使用技能攻击鬼 {}，技能伤害值: {}，鬼灵异抗性: {}，实际造成灵异伤害: {}",
            player.method_5477().getString(),
            this.method_5477().getString(),
            damageAmount,
            ghostSpiritResistance,
            actualSpiritDamage
         );
         if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(player.method_5667()) && actualSpiritDamage > 0.0F) {
            player.method_7353(
               Text.method_43470("§a对 §f" + this.method_5477().getString() + " §a造成了§f " + new DecimalFormat("#.###").format(actualSpiritDamage) + " §a点灵异伤害"),
               true
            );
         }

         if (actualSpiritDamage > 0.0F && !this.method_37908().method_8608()) {
            ServerWorld serverWorld = (ServerWorld)this.method_37908();
            Vec3d pos = this.method_19538();
            serverWorld.method_43128(null, pos.field_1352, pos.field_1351, pos.field_1350, SoundEvents.field_14858, SoundCategory.field_15251, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.method_6051().method_43058() - 0.5) * 2.0;
               double offsetY = this.method_6051().method_43058() * 1.5 + 0.5;
               double offsetZ = (this.method_6051().method_43058() - 0.5) * 2.0;
               double velocityX = (this.method_6051().method_43058() - 0.5) * 0.2;
               double velocityY = this.method_6051().method_43058() * 0.3 + 0.1;
               double velocityZ = (this.method_6051().method_43058() - 0.5) * 0.2;
               serverWorld.method_14199(
                  ParticleTypes.field_11249,
                  pos.field_1352 + offsetX,
                  pos.field_1351 + offsetY,
                  pos.field_1350 + offsetZ,
                  1,
                  velocityX,
                  velocityY,
                  velocityZ,
                  0.5
               );
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isChasing() && this.attackCooldown <= 0) {
            this.setResentmentValue(100);
            LOGGER.debug("玩家 {} 使用技能攻击鬼，触发受击反击，怨气值直接积满至100", player.method_5477().getString());
         }
      }
   }

   public boolean method_5643(DamageSource source, float amount) {
      if (this.method_37908().method_8608()) {
         return true;
      }

      if (source.method_5529() instanceof GhostEntity attackerGhost) {
         LOGGER.debug("鬼 {} 正在攻击鬼 {}", attackerGhost.method_5477().getString(), this.method_5477().getString());
         float attackerSpiritDamage = attackerGhost.getSpiritualDamage();
         float defenderSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage = PlayerEvents.calculateSpiritDamage(attackerSpiritDamage, defenderSpiritResistance);
         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         LOGGER.debug(
            "鬼 {} 灵异伤害为 {} 鬼 {} 灵异抗性为 {} 攻击造成的实际灵异伤害: {}",
            attackerGhost.method_5477().getString(),
            attackerSpiritDamage,
            this.method_5477().getString(),
            defenderSpiritResistance,
            actualSpiritDamage
         );
         if (actualSpiritDamage > 0.0F && !this.method_37908().method_8608()) {
            ServerWorld serverWorld = (ServerWorld)this.method_37908();
            Vec3d pos = this.method_19538();
            serverWorld.method_43128(null, pos.field_1352, pos.field_1351, pos.field_1350, SoundEvents.field_14858, SoundCategory.field_15251, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.method_6051().method_43058() - 0.5) * 2.0;
               double offsetY = this.method_6051().method_43058() * 1.5 + 0.5;
               double offsetZ = (this.method_6051().method_43058() - 0.5) * 2.0;
               double velocityX = (this.method_6051().method_43058() - 0.5) * 0.2;
               double velocityY = this.method_6051().method_43058() * 0.3 + 0.1;
               double velocityZ = (this.method_6051().method_43058() - 0.5) * 0.2;
               serverWorld.method_14199(
                  ParticleTypes.field_11249,
                  pos.field_1352 + offsetX,
                  pos.field_1351 + offsetY,
                  pos.field_1350 + offsetZ,
                  1,
                  velocityX,
                  velocityY,
                  velocityZ,
                  0.5
               );
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isSuppressed() && this.attackCooldown <= 0) {
            this.method_5980(attackerGhost);
            this.executeAttack(attackerGhost);
            LOGGER.debug("鬼 {} 被鬼 {} 攻击，触发反击", this.method_5477().getString(), attackerGhost.method_5477().getString());
         }

         return false;
      } else if (source.method_5529() instanceof GhostMasterEntity ghostMaster) {
         LOGGER.debug("驭鬼者 {} 正在攻击鬼 {}", ghostMaster.method_5477().getString(), this.method_5477().getString());
         float attackerSpiritDamage = ghostMaster.getSpiritualDamage();
         float defenderSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage = PlayerEvents.calculateSpiritDamage(attackerSpiritDamage, defenderSpiritResistance);
         int currentSpirit = this.getSpiritualStrength();
         int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
         this.setSpiritualStrength(newSpirit);
         LOGGER.debug(
            "驭鬼者 {} 灵异伤害为 {} 鬼 {} 灵异抗性为 {} 攻击造成的实际灵异伤害: {}",
            ghostMaster.method_5477().getString(),
            attackerSpiritDamage,
            this.method_5477().getString(),
            defenderSpiritResistance,
            actualSpiritDamage
         );
         if (actualSpiritDamage > 0.0F && !this.method_37908().method_8608()) {
            ServerWorld serverWorld = (ServerWorld)this.method_37908();
            Vec3d pos = this.method_19538();
            serverWorld.method_43128(null, pos.field_1352, pos.field_1351, pos.field_1350, SoundEvents.field_14858, SoundCategory.field_15251, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.method_6051().method_43058() - 0.5) * 2.0;
               double offsetY = this.method_6051().method_43058() * 1.5 + 0.5;
               double offsetZ = (this.method_6051().method_43058() - 0.5) * 2.0;
               double velocityX = (this.method_6051().method_43058() - 0.5) * 0.2;
               double velocityY = this.method_6051().method_43058() * 0.3 + 0.1;
               double velocityZ = (this.method_6051().method_43058() - 0.5) * 0.2;
               serverWorld.method_14199(
                  ParticleTypes.field_11249,
                  pos.field_1352 + offsetX,
                  pos.field_1351 + offsetY,
                  pos.field_1350 + offsetZ,
                  1,
                  velocityX,
                  velocityY,
                  velocityZ,
                  0.5
               );
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isSuppressed() && this.attackCooldown <= 0) {
            this.method_5980(ghostMaster);
            this.executeAttack(ghostMaster);
            LOGGER.debug("鬼 {} 被驭鬼者 {} 攻击，触发反击", this.method_5477().getString(), ghostMaster.method_5477().getString());
         }

         return false;
      } else if (source.method_5529() instanceof PlayerEntity player) {
         LOGGER.debug("玩家 {} 正在攻击鬼", player.method_5477().getString());
         boolean isSkillDamage = GhostSkillManager.isSkillDamage(source);
         if (isSkillDamage) {
            LOGGER.debug("玩家 {} 使用技能攻击鬼，跳过灵异武器检查", player.method_5477().getString());
         }

         if (!isSkillDamage) {
            boolean hasSpiritWeapon = SpiritWeapon.isSpiritWeapon(player.method_6047());
            boolean hasCorpseOilWeapon = WeaponOilHandler.getCorpseOilLayers(player.method_6047()) > 0;
            if (!hasSpiritWeapon && !hasCorpseOilWeapon) {
               if (!SpiritSurgeStatusEffect.hasSpiritSurgeEffect(player)) {
                  LOGGER.debug("玩家 {} 没有持有灵异武器、抹尸油武器且无灵异奔涌效果，无法对鬼造成伤害", player.method_5477().getString());
                  return true;
               }

               LOGGER.debug("玩家 {} 拥有灵异奔涌效果，允许对鬼造成伤害", player.method_5477().getString());
            }
         }

         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         float playerSpiritDamage = 0.0F;
         float tempSpiritDamage = 0.0F;
         float tempSpiritDamageMultiplier = 1.0F;
         if (spiritAttributes.method_10545("spiritDamage")) {
            if (spiritAttributes.method_10573("spiritDamage", 3)) {
               playerSpiritDamage = spiritAttributes.method_10550("spiritDamage");
            } else if (spiritAttributes.method_10573("spiritDamage", 6)) {
               playerSpiritDamage = (float)spiritAttributes.method_10574("spiritDamage");
            } else {
               playerSpiritDamage = spiritAttributes.method_10583("spiritDamage");
            }
         }

         if (spiritAttributes.method_10545("tempSpiritDamage")) {
            tempSpiritDamage = (float)spiritAttributes.method_10574("tempSpiritDamage");
         }

         if (spiritAttributes.method_10545("tempSpiritDamageMultiplier")) {
            tempSpiritDamageMultiplier = (float)spiritAttributes.method_10574("tempSpiritDamageMultiplier");
         }

         float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
         float weaponBonus = 0.0F;
         boolean hasCorpseOilWeapon = false;
         if (SpiritWeapon.isSpiritWeapon(player.method_6047())) {
            SpiritWeapon weapon = (SpiritWeapon)player.method_6047().method_7909();
            weaponBonus = this.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : weapon.getSpiritDamageBonus();
         } else if (WeaponOilHandler.getCorpseOilLayers(player.method_6047()) > 0) {
            weaponBonus = 0.0F;
            hasCorpseOilWeapon = true;
         }

         float effectiveSpiritDamage = totalSpiritDamage;
         boolean hasSpiritOrCorpseOilWeapon = SpiritWeapon.isSpiritWeapon(player.method_6047())
            || WeaponOilHandler.getCorpseOilLayers(player.method_6047()) > 0;
         if (totalSpiritDamage <= 0.0F && hasSpiritOrCorpseOilWeapon) {
            if (SpiritWeapon.isSpiritWeapon(player.method_6047())) {
               SpiritWeapon weapon = (SpiritWeapon)player.method_6047().method_7909();
               effectiveSpiritDamage = this.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : weapon.getSpiritDamageBonus();
            } else if (WeaponOilHandler.getCorpseOilLayers(player.method_6047()) > 0) {
               effectiveSpiritDamage = this.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : amount * 10.0F;
               if (this.method_37908().method_27983() != Smfs.GHOST_DREAM_DIMENSION) {
                  WeaponOilHandler.consumeCorpseOilLayer(player.method_6047());
               }
            }

            LOGGER.debug("玩家 {} 使用灵异武器或抹尸油武器但无灵异伤害属性，使用武器伤害加成: {}", player.method_5477().getString(), effectiveSpiritDamage);
         } else {
            float damageMultiplier = 0.5F;
            if (SpiritWeapon.isSpiritWeapon(player.method_6047())) {
               SpiritWeapon weapon = (SpiritWeapon)player.method_6047().method_7909();
               damageMultiplier = this.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION ? 0.0F : weapon.getSpiritDamageMultiplier();
            }

            effectiveSpiritDamage = weaponBonus + totalSpiritDamage * damageMultiplier;
            if (WeaponOilHandler.getCorpseOilLayers(player.method_6047()) > 0 && this.method_37908().method_27983() != Smfs.GHOST_DREAM_DIMENSION) {
               WeaponOilHandler.consumeCorpseOilLayer(player.method_6047());
            }
         }

         float ghostSpiritResistance = this.getSpiritualResistance();
         float actualSpiritDamage;
         if (SpiritSurgeStatusEffect.hasSpiritSurgeEffect(player)) {
            float playerBaseSpiritDamage = playerSpiritDamage;
            actualSpiritDamage = PlayerEvents.calculateSpiritSurgeDamage(playerBaseSpiritDamage, 6.0F, ghostSpiritResistance);
            LOGGER.debug("玩家 {} 使用灵异奔涌效果，基础灵异伤害: {}，使用特殊公式计算伤害", player.method_5477().getString(), playerBaseSpiritDamage);
         } else {
            actualSpiritDamage = PlayerEvents.calculateSpiritDamage(effectiveSpiritDamage, ghostSpiritResistance);
         }

         if (hasSpiritOrCorpseOilWeapon || isSkillDamage) {
            long currentTime = System.currentTimeMillis();
            UUID playerUUID = player.method_5667();
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
            "玩家 {} 灵异伤害为 {} 鬼的灵异抗性为 {} 攻击鬼造成的实际灵异伤害: {}", player.method_5477().getString(), effectiveSpiritDamage, ghostSpiritResistance, actualSpiritDamage
         );
         if (ModConfig.getInstance().showActionBarInfo && ClientModConfig.getInstance().showDamageText(player.method_5667()) && actualSpiritDamage > 0.0F) {
            player.method_7353(
               Text.method_43470("§a对 §f" + this.method_5477().getString() + " §a造成了§f " + new DecimalFormat("#.###").format(actualSpiritDamage) + " §a点灵异伤害"),
               true
            );
         }

         if (actualSpiritDamage > 0.0F && !this.method_37908().method_8608()) {
            ServerWorld serverWorld = (ServerWorld)this.method_37908();
            Vec3d pos = this.method_19538();
            serverWorld.method_43128(null, pos.field_1352, pos.field_1351, pos.field_1350, SoundEvents.field_14858, SoundCategory.field_15251, 1.0F, 1.0F);

            for (int i = 0; i < 25; i++) {
               double offsetX = (this.method_6051().method_43058() - 0.5) * 2.0;
               double offsetY = this.method_6051().method_43058() * 1.5 + 0.5;
               double offsetZ = (this.method_6051().method_43058() - 0.5) * 2.0;
               double velocityX = (this.method_6051().method_43058() - 0.5) * 0.2;
               double velocityY = this.method_6051().method_43058() * 0.3 + 0.1;
               double velocityZ = (this.method_6051().method_43058() - 0.5) * 0.2;
               serverWorld.method_14199(
                  ParticleTypes.field_11249,
                  pos.field_1352 + offsetX,
                  pos.field_1351 + offsetY,
                  pos.field_1350 + offsetZ,
                  1,
                  velocityX,
                  velocityY,
                  velocityZ,
                  0.5
               );
            }
         }

         if (newSpirit == 0 && !this.isDeadlocked()) {
            this.setDeadlocked(true);
         }

         if (actualSpiritDamage > 0.0F && !this.isDeadlocked() && !this.isChasing() && this.attackCooldown <= 0) {
            this.setResentmentValue(100);
            LOGGER.debug("玩家 {} 攻击鬼，触发受击反击，怨气值直接积满至100", player.method_5477().getString());
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
            LOGGER.debug("鬼 {} 灵异强度为0，进入死机状态", this.method_5477().getString());
         }

         if (amount > 0.0F && !this.method_37908().method_8608()) {
            ServerWorld serverWorld = (ServerWorld)this.method_37908();
            Vec3d pos = this.method_19538();

            for (int i = 0; i < 15; i++) {
               double offsetX = (this.method_6051().method_43058() - 0.5) * 1.5;
               double offsetY = this.method_6051().method_43058() * 1.0 + 0.5;
               double offsetZ = (this.method_6051().method_43058() - 0.5) * 1.5;
               double velocityX = (this.method_6051().method_43058() - 0.5) * 0.1;
               double velocityY = this.method_6051().method_43058() * 0.2 + 0.05;
               double velocityZ = (this.method_6051().method_43058() - 0.5) * 0.1;
               serverWorld.method_14199(
                  ParticleTypes.field_11249,
                  pos.field_1352 + offsetX,
                  pos.field_1351 + offsetY,
                  pos.field_1350 + offsetZ,
                  1,
                  velocityX,
                  velocityY,
                  velocityZ,
                  0.3
               );
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public void method_5673(EquipmentSlot slot, ItemStack stack) {
   }

   public ItemStack method_6118(EquipmentSlot slot) {
      return ItemStack.field_8037;
   }

   public void method_5773() {
      super.method_5773();
      if (!this.method_31481() && this.method_5805()) {
         if (!this.method_37908().field_9236) {
            if (this.attackCooldown > 0) {
               this.attackCooldown--;
               LOGGER.debug("Ghost {} attack cooldown: {}", this.method_5845(), this.attackCooldown);
            }

            if (this.whiteCandleTargetLocked && this.whiteCandleTargetLockTimer > 0) {
               this.whiteCandleTargetLockTimer--;
               if (this.whiteCandleTargetLockTimer <= 0) {
                  this.whiteCandleTargetLocked = false;
                  LOGGER.debug("Ghost {} white candle target lock expired", this.method_5845());
               }
            }

            this.positionCheckTimer++;
            if (this.positionCheckTimer >= 60) {
               Vec3d currentPosition = this.method_19538();
               if (this.lastPosition != null
                  && currentPosition.method_1022(this.lastPosition) < 0.1
                  && this.isChasing
                  && this.chaseTarget != null
                  && this.chaseTarget.method_5805()) {
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
                  LOGGER.debug("Ghost {} resentment value: {}", this.method_5845(), this.resentmentValue);
               }
            }

            if (this.isChasing && this.chaseTarget != null && this.chaseTarget.method_5805()) {
               this.chaseTimer--;
               if (this.method_5942().method_6357()) {
                  this.method_5942().method_6335(this.chaseTarget, 2.4);
                  LOGGER.debug("Ghost {} started chasing target with speed 2.4", this.method_5845());
               }

               if (this.chaseTarget.method_5805() && !this.isSuppressed() && !this.isDeadlocked()) {
                  this.method_5988().method_6226(this.chaseTarget, 30.0F, 30.0F);
               }

               double distance = this.method_5858(this.chaseTarget);
               if (distance <= 4.0) {
                  if (this.chaseTarget instanceof PlayerEntity player) {
                     this.executeAttack(player);
                  } else if (this.chaseTarget instanceof GhostEntity ghost) {
                     this.executeAttack(ghost);
                  }

                  this.isChasing = false;
                  this.chaseTarget = null;
                  this.chaseTimer = 0;
                  this.method_5942().method_6340();
                  LOGGER.debug("Ghost {} attacked chase target at close range", this.method_5845());
               } else if (this.chaseTimer <= 0) {
                  int difficulty = WorldConfig.getInstance(this.method_37908()).modDifficulty;
                  if (this.chaseTarget instanceof PlayerEntity player) {
                     if (difficulty == 2) {
                        this.executeAttack(player, true);
                        LOGGER.debug("Ghost {} executed timeout teleport attack on player", this.method_5845());
                     } else {
                        player.method_7353(Text.method_43470("§c你侥幸躲过了一次厉鬼的袭击..."), true);
                        LOGGER.debug("Player {} avoided ghost timeout attack", player.method_5845());
                     }
                  } else if (this.chaseTarget instanceof GhostEntity ghost) {
                     this.executeAttack(ghost, true);
                     LOGGER.debug("Ghost {} executed timeout teleport attack on another ghost", this.method_5845());
                  }

                  this.isChasing = false;
                  this.chaseTarget = null;
                  this.chaseTimer = 0;
                  this.method_5942().method_6340();
               }
            } else if (this.isChasing && (this.chaseTarget == null || !this.chaseTarget.method_5805())) {
               this.isChasing = false;
               this.chaseTarget = null;
               this.chaseTimer = 0;
               this.method_5942().method_6340();
               if (this.method_5968() != null && !this.method_5968().method_5805()) {
                  this.method_5980(null);
               }
            }

            if (this.attackCooldown <= 0 && !this.isChasing) {
               if (this.hasGhostFactionTarget()) {
                  if (this.getGhostFactionTarget() instanceof GhostEntity ghost
                     && ghost.method_5805()
                     && this.method_5858(ghost) <= 256.0
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
                           && currentGhost.getOwner().method_5667().equals(playerGhost.getMasterUuid())) {
                           isImmune = true;
                        }
                     }

                     if (ghost instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null) {
                        if (this instanceof PlayerGhostEntity currentGhost) {
                           if (currentGhost.isServantMode()
                              && currentGhost.getMasterUuid() != null
                              && currentGhost.getMasterUuid().equals(ghostChild.getOwner().method_5667())) {
                              isImmune = true;
                           }
                        } else if (this instanceof GhostChildEntity currentGhost
                           && currentGhost.getOwner() != null
                           && currentGhost.getOwner().method_5667().equals(ghostChild.getOwner().method_5667())) {
                           isImmune = true;
                        }
                     }

                     if (!isImmune) {
                        this.executeAttack(ghost);
                     }
                  }
               } else {
                  for (GhostEntity ghost : this.method_37908().method_8390(GhostEntity.class, this.method_5829().method_1014(16.0), ghostx -> ghostx != this)) {
                     if (this.shouldAttackGhost(ghost)) {
                        this.executeAttack(ghost);
                        break;
                     }
                  }
               }
            }

            if (this.isSuppressed() || this.isDeadlocked() || this.movementDisabled) {
               this.method_18799(Vec3d.field_1353);
               if (this.walkingStateManager != null) {
                  this.walkingStateManager.reset();
               }

               if (this.isDeadlocked()) {
                  this.method_5980(null);
               }
            } else if (this.walkingStateManager != null) {
               this.walkingStateManager.update(this.field_6012);
            }

            double ghostDomainRadius = this.getGhostDomainRadius();
            if (ghostDomainRadius <= 0.0) {
               ghostDomainRadius = 48.0;
            }

            double maxDistanceSquared = ghostDomainRadius * ghostDomainRadius;

            for (PlayerEntity player : this.method_37908().method_18456().stream().filter(playerx -> {
               double dx = playerx.method_23317() - this.method_23317();
               double dy = playerx.method_23318() - this.method_23318();
               double dz = playerx.method_23321() - this.method_23321();
               double distanceSq = dx * dx + dy * dy + dz * dz;
               return distanceSq <= maxDistanceSquared;
            }).filter(playerx -> !this.isPlayerProtected(playerx)).collect(Collectors.toList())) {
               double distance = this.method_5858(player);
               ModConfig config = ModConfig.getInstance();
               if (config.enableResentmentSystem && !this.isResentmentSystemDisabled()) {
                  int resentmentGain = this.calculateResentmentGainByDistance(distance);
                  if (resentmentGain > 0 && this.method_37908().method_8510() % 36L == 0L) {
                     this.increaseResentment(resentmentGain);
                  }

                  if (this.isResentmentThresholdReached() && this.attackCooldown <= 0 && !this.isChasing) {
                     if (this.getGhostDomainLevel() < 1 && this.isEnableChaseAfterRule()) {
                        this.isChasing = true;
                        this.chaseTarget = player;
                        this.method_5980(player);
                        this.chaseTimer = 600;
                        this.setResentmentValue(30);
                        break;
                     }

                     this.method_5980(player);
                     this.executeAttack(player);
                     this.setResentmentValue(30);
                     break;
                  }
               }
            }

            if (this.method_5968() instanceof GhostEntity targetGhost && targetGhost.isDeadlocked()) {
               this.method_5980(null);
            }

            if (this.getSpiritualStrength() < this.getMaxSpiritualStrength() && this.method_37908().method_8510() % 20L == 0L) {
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

            if (this.method_37908().method_27983() == World.field_25179) {
               boolean isPlayerGhost = this instanceof PlayerGhostEntity;
               if (GhostSpawnManager.isGhostLocked(this.method_5864()) && !isPlayerGhost) {
                  LOGGER.debug("鬼 {} 在主世界中被检测到锁死状态，自动移除", this.method_5864().toString());
                  GhostDeathHandler.markLegitimateRemoval(this);
                  this.method_31472();
                  return;
               }
            }

            if (!this.isSuppressed() && !this.isDeadlocked() && this.ghostDomainEnabled && this.hasGhostDomain) {
               double radius = this.getGhostDomainRadius();

               for (PlayerEntity player : this.method_37908().method_18456().stream().filter(playerx -> {
                  double dx = playerx.method_23317() - this.method_23317();
                  double dy = playerx.method_23318() - this.method_23318();
                  double dz = playerx.method_23321() - this.method_23321();
                  double distanceSq = dx * dx + dy * dy + dz * dz;
                  return distanceSq <= radius * radius;
               }).collect(Collectors.toList())) {
                  int ghostLevel = this.getGhostDomainLevel();
                  boolean isImmune = GhostDomainManager.isImmuneToGhostDomain(player, ghostLevel);
                  boolean shouldAttack = this.killingRulesEnabled && this.shouldAttackPlayer(player) && !this.isPlayerProtected(player);
                  if (shouldAttack && this.method_5968() != player) {
                     if (this.getGhostDomainLevel() < 1 && this.isEnableChaseAfterRule()) {
                        if (!this.method_37908().method_8608()) {
                           ServerWorld serverWorld = (ServerWorld)this.method_37908();
                           serverWorld.method_8396(
                              null,
                              player.method_24515(),
                              SoundEvents.field_15203,
                              SoundCategory.field_15251,
                              1.0F,
                              0.8F + this.method_6051().method_43057() * 0.4F
                           );
                        }

                        this.isChasing = true;
                        this.chaseTarget = player;
                        this.chaseTimer = 600;
                        this.method_5980(player);
                        LOGGER.debug("Ghost {} started chasing due to killing rules", this.method_5845());
                        break;
                     }

                     this.executeAttack(player);
                     LOGGER.debug("Ghost {} with domain level {} directly attacked player due to killing rules", this.method_5845(), this.getGhostDomainLevel());
                     break;
                  }

                  if (!this.isHoldingGhostBuddhaBeads(player)) {
                     if (GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)) {
                        player.method_6016(ModEffects.LOST);
                     } else {
                        if (GhostDomainManager.isGhostDomainActive(player)) {
                           int playerDomainLevel = GhostDomainManager.getCurrentLevel(player);
                           int ghostActualDomainLevel = this.getGhostDomainActualLevel();
                           if (playerDomainLevel > ghostActualDomainLevel - 1) {
                              player.method_6016(StatusEffects.field_5909);
                              player.method_6016(ModEffects.LOST);
                              player.method_6016(ModEffects.RED_GHOST_DOMAIN_TARGET);
                              player.method_6016(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
                              player.method_6016(ModEffects.THICK_FOG_TARGET);
                              player.method_6016(ModEffects.GOLDEN_GHOST_DOMAIN_TARGET);
                              player.method_6016(ModEffects.GREEN_GHOST_DOMAIN_TARGET);
                              player.method_6016(StatusEffects.field_5899);
                              player.method_6016(ModEffects.LOST);
                              continue;
                           }
                        }

                        if (!isImmune) {
                           if (this.getGhostDomainLevel() >= 1) {
                              this.applyDefaultEffects(player);
                           }

                           if (this.getGhostDomainLevel() >= 2) {
                              player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 200, 1, false, false));
                              ModConfig config = ModConfig.getInstance();
                              if (config.enableGhostDomainEffectsOnMobs) {
                                 for (LivingEntity entity : this.method_37908()
                                    .method_8390(
                                       LivingEntity.class,
                                       this.method_5829().method_1014(this.getGhostDomainRadius()),
                                       entityx -> entityx != this
                                          && !(entityx instanceof PlayerEntity)
                                          && !entityx.method_7325()
                                          && !(entityx instanceof GhostEntity)
                                    )) {
                                    StatusEffectInstance lostEffect = new LostStatusEffectInstance(
                                       ModEffects.LOST, Integer.MAX_VALUE, 0, false, false, false, this.method_5667()
                                    );
                                    entity.method_6092(lostEffect);
                                 }
                              }
                           }

                           if (this.getGhostDomainLevel() >= 3) {
                              if (!player.method_7325()) {
                                 StatusEffectInstance lostEffect = new LostStatusEffectInstance(
                                    ModEffects.LOST, Integer.MAX_VALUE, 0, false, false, false, this.method_5667()
                                 );
                                 player.method_6092(lostEffect);
                              }

                              if (this instanceof PlayerGhostEntity playerGhost) {
                                 String domainColor = playerGhost.getGhostDomainColor();
                                 if ("red".equals(domainColor)) {
                                    player.method_6092(
                                       new StatusEffectInstance(
                                          ModEffects.RED_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("blindness".equals(domainColor)) {
                                    player.method_6092(
                                       new StatusEffectInstance(
                                          ModEffects.BLACK_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("fog".equals(domainColor)) {
                                    player.method_6092(
                                       new StatusEffectInstance(
                                          ModEffects.THICK_FOG_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("golden".equals(domainColor)) {
                                    player.method_6092(
                                       new StatusEffectInstance(
                                          ModEffects.GOLDEN_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 } else if ("green".equals(domainColor)) {
                                    player.method_6092(
                                       new StatusEffectInstance(
                                          ModEffects.GREEN_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false, false
                                       )
                                    );
                                 }
                              }

                              if (this.getGhostDomainLevel() >= 4) {
                                 player.method_6092(new StatusEffectInstance(StatusEffects.field_5899, 200, 0, false, false));
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (ENABLE_INVULNERABLE
               && ENABLE_BLACK_PARTICLES
               && this.method_37908().field_9236
               && this.getVisible()
               && !this.isSuppressed()
               && !this.isDeadlocked()) {
               Vec3d pos = this.method_19538();

               for (int i = 0; i < 5; i++) {
                  double offsetX = this.random.nextGaussian() * 0.5;
                  double offsetY = this.random.nextGaussian() * 0.5;
                  double offsetZ = this.random.nextGaussian() * 0.5;
                  this.method_37908()
                     .method_8406(ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 0.0, 0.0, 0.0);
               }
            }
         }

         if (this.field_6252) {
            this.field_6279++;
            if (this.field_6279 >= 20) {
               this.field_6252 = false;
               this.field_6279 = 0;
            }
         }
      } else {
         if (this.walkingStateManager != null) {
            this.walkingStateManager.reset();
         }
      }
   }

   public void method_5982() {
      if (ModConfig.getInstance().allowEvilGhostNaturalDespawn) {
         super.method_5982();
      }
   }

   public void method_6078(DamageSource damageSource) {
      if (damageSource.method_5529() instanceof PlayerEntity player) {
         player.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
      }

      this.method_6033(this.method_6063());
   }

   public boolean method_29504() {
      return false;
   }

   public boolean method_5805() {
      return true;
   }

   public void method_6033(float health) {
      if (health <= 0.0F) {
         float maxHealth = this.method_6063();
         super.method_6033(maxHealth);
         PlayerEntity nearestPlayer = this.findNearestPlayer();
         if (nearestPlayer != null) {
            GhostDeathHandler.handleIllegalKillPunishment(nearestPlayer, this, ModDamageSources.ghost(this.method_37908()));
         }

         if (nearestPlayer != null) {
            nearestPlayer.method_7353(Text.method_43471("entity.smfs.ghost.immortal_warning"), true);
         }
      } else {
         super.method_6033(health);
      }
   }

   private PlayerEntity findNearestPlayer() {
      if (this.method_37908() instanceof ServerWorld serverWorld) {
         List<ServerPlayerEntity> nearbyPlayers = serverWorld.method_18766(player -> player.method_5858(this) <= 2500.0);
         return nearbyPlayers.isEmpty()
            ? null
            : (PlayerEntity)nearbyPlayers.stream().min((p1, p2) -> Float.compare((float)p1.method_5858(this), (float)p2.method_5858(this))).orElse(null);
      } else {
         return null;
      }
   }

   protected void method_16080(DamageSource source) {
   }

   protected void method_6099(DamageSource source, int lootingMultiplier, boolean allowDrops) {
   }

   public Iterable<ItemStack> method_5661() {
      return Collections.emptyList();
   }

   public ItemStack method_5998(Hand hand) {
      return ItemStack.field_8037;
   }

   public int getSpiritualStrength() {
      return (Integer)this.field_6011.method_12789(SPIRITUAL_STRENGTH);
   }

   public int getMaxSpiritualStrength() {
      return (Integer)this.field_6011.method_12789(MAX_SPIRITUAL_STRENGTH);
   }

   public int getSpiritualResistance() {
      return (Integer)this.field_6011.method_12789(SPIRITUAL_RESISTANCE);
   }

   public int getSpiritualDamage() {
      return (Integer)this.field_6011.method_12789(SPIRITUAL_DAMAGE);
   }

   public float getRecoveryFactor() {
      return (Float)this.field_6011.method_12789(RECOVERY_FACTOR);
   }

   public void setSpiritualStrength(int strength) {
      this.field_6011.method_12778(SPIRITUAL_STRENGTH, Math.max(0, strength));
   }

   public void setMaxSpiritualStrength(int maxStrength) {
      this.field_6011.method_12778(MAX_SPIRITUAL_STRENGTH, Math.max(0, maxStrength));
   }

   public void setSpiritualResistance(int resistance) {
      this.field_6011.method_12778(SPIRITUAL_RESISTANCE, Math.max(0, resistance));
   }

   public void setSpiritualDamage(int damage) {
      this.field_6011.method_12778(SPIRITUAL_DAMAGE, Math.max(0, damage));
   }

   public void setRecoveryFactor(float factor) {
      this.field_6011.method_12778(RECOVERY_FACTOR, Math.max(0.0F, factor));
   }

   public float getGhostDomainRadius() {
      return (Float)this.field_6011.method_12789(GHOST_DOMAIN_RADIUS);
   }

   public void setGhostDomainRadius(float radius) {
      this.field_6011.method_12778(GHOST_DOMAIN_RADIUS, radius);
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
      this.field_6011.method_12778(RECOVERY_FACTOR, this.getRecoveryFactorByTearorLevel(this.terrorLevel));
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
         LOGGER.debug("鬼 {} 的鬼蜮实际等级设置为: {}", this.method_5477().getString(), level);
      }
   }

   protected Random getGhostRandom() {
      return this.random;
   }

   public boolean method_6121(Entity target) {
      if (!this.isDeadlocked() && !this.isSuppressed()) {
         float f = (float)this.method_26825(EntityAttributes.field_23721);
         float g = (float)this.method_26825(EntityAttributes.field_23722);
         if (target instanceof LivingEntity) {
            f += EnchantmentHelper.method_8218(this.method_6047(), ((LivingEntity)target).method_6046());
            g += EnchantmentHelper.method_8205(this);
         }

         int i = EnchantmentHelper.method_8199(this);
         if (i > 0) {
            target.method_5639(i * 4);
         }

         DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
         boolean bl = false;
         if (target instanceof PlayerEntity) {
            PlayerEvents.handleSpiritDamage((PlayerEntity)target, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
            bl = true;
         } else if (target instanceof GhostEntity targetGhost) {
            if (!targetGhost.isDeadlocked() && !targetGhost.isSuppressed() && targetGhost.getSpiritualStrength() > 0) {
               bl = targetGhost.method_5643(this.method_48923().method_48812(this), this.getSpiritualDamage());
            }
         } else if (target instanceof GhostMasterEntity ghostMaster) {
            if (!ghostMaster.isSuppressed() && !ghostMaster.isDeadlocked()) {
               bl = ghostMaster.method_5643(this.method_48923().method_48812(this), this.getSpiritualDamage());
            }
         } else if (target instanceof LivingEntity) {
            bl = target.method_5643(damageSource, this.getSpiritualDamage());
         } else {
            bl = target.method_5643(this.method_48923().method_48812(this), f);
         }

         if (bl) {
            if (g > 0.0F && target instanceof LivingEntity) {
               ((LivingEntity)target)
                  .method_6005(
                     g * 0.5F,
                     MathHelper.method_15374(this.method_36454() * (float) (Math.PI / 180.0)),
                     -MathHelper.method_15362(this.method_36454() * (float) (Math.PI / 180.0))
                  );
               this.method_18799(this.method_18798().method_18805(0.6, 1.0, 0.6));
            }

            this.method_5723(this, target);
            this.method_6114(target);
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
         || SpectateModePacket.isInSpectatorMode(player.method_5667())
         || player.method_6059(ModEffects.SPIRIT_IMMUNITY);
   }

   public boolean isTargetValid(LivingEntity target) {
      if (target != null && target.method_5805()) {
         if (target instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
            if (this instanceof PlayerGhostEntity currentGhost) {
               if (currentGhost.isServantMode() && currentGhost.getMasterUuid() != null && currentGhost.getMasterUuid().equals(playerGhost.getMasterUuid())) {
                  return false;
               }
            } else if (this instanceof GhostChildEntity currentGhost
               && currentGhost.getOwner() != null
               && currentGhost.getOwner().method_5667().equals(playerGhost.getMasterUuid())) {
               return false;
            }
         }

         if (target instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null) {
            if (this instanceof PlayerGhostEntity currentGhost) {
               if (currentGhost.isServantMode()
                  && currentGhost.getMasterUuid() != null
                  && currentGhost.getMasterUuid().equals(ghostChild.getOwner().method_5667())) {
                  return false;
               }
            } else if (this instanceof GhostChildEntity currentGhost
               && currentGhost.getOwner() != null
               && currentGhost.getOwner().method_5667().equals(ghostChild.getOwner().method_5667())) {
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

   public void method_5980(@Nullable LivingEntity target) {
      if (target != null && !this.isTargetValid(target)) {
         target = null;
      }

      super.method_5980(target);
   }

   protected boolean shouldAttackGhost(GhostEntity ghost) {
      if (this.isDeadlocked()) {
         return false;
      }

      if (ghost.isDeadlocked()) {
         this.method_5980(null);
         return false;
      }

      if (ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
         if (this instanceof PlayerGhostEntity currentGhost) {
            if (currentGhost.isServantMode() && currentGhost.getMasterUuid() != null && currentGhost.getMasterUuid().equals(playerGhost.getMasterUuid())) {
               return false;
            }
         } else if (this instanceof GhostChildEntity currentGhost
            && currentGhost.getOwner() != null
            && currentGhost.getOwner().method_5667().equals(playerGhost.getMasterUuid())) {
            return false;
         }
      }

      if (ghost instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null) {
         if (this instanceof PlayerGhostEntity currentGhost) {
            if (currentGhost.isServantMode()
               && currentGhost.getMasterUuid() != null
               && currentGhost.getMasterUuid().equals(ghostChild.getOwner().method_5667())) {
               return false;
            }
         } else if (this instanceof GhostChildEntity currentGhost
            && currentGhost.getOwner() != null
            && currentGhost.getOwner().method_5667().equals(ghostChild.getOwner().method_5667())) {
            return false;
         }
      }

      return ghost != this
         && this.method_5858(ghost) <= 256.0
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
         double distance = this.method_5858(player);
         this.field_6252 = true;
         this.field_6279 = 0;
         boolean attacked = this.method_6121(player);
         if (attacked) {
            if (distance > 25.0 || isTimeoutAttack) {
               this.method_20620(player.method_23317(), player.method_23318(), player.method_23321());
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
            double distance = this.method_5858(ghost);
            this.field_6252 = true;
            this.field_6279 = 0;
            boolean attacked = this.method_6121(ghost);
            if (attacked) {
               if (distance > 25.0 || isTimeoutAttack) {
                  this.method_20620(ghost.method_23317(), ghost.method_23318(), ghost.method_23321());
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
         double distance = this.method_5858(entity);
         this.field_6252 = true;
         this.field_6279 = 0;
         boolean attacked = this.method_6121(entity);
         if (attacked) {
            if (distance > 25.0 || isTimeoutAttack) {
               this.method_20620(entity.method_23317(), entity.method_23318(), entity.method_23321());
            }

            this.attackCooldown = 100;
         }
      }
   }

   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      nbt.method_10569("SpiritualStrength", this.getSpiritualStrength());
      nbt.method_10569("MaxSpiritualStrength", this.getMaxSpiritualStrength());
      nbt.method_10569("SpiritualResistance", this.getSpiritualResistance());
      nbt.method_10569("SpiritualDamage", this.getSpiritualDamage());
      nbt.method_10548("RecoveryFactor", this.getRecoveryFactor());
      nbt.method_10556("IsSuppressed", this.isSuppressed);
      nbt.method_10556("IsDeadlocked", this.isDeadlocked());
      nbt.method_10556("MovementDisabled", this.movementDisabled);
      nbt.method_10556("GhostDomainEnabled", this.ghostDomainEnabled);
      nbt.method_10556("HasGhostDomain", this.hasGhostDomain);
      nbt.method_10569("GhostDomainLevel", this.ghostDomainLevel);
      nbt.method_10569("GhostDomainActualLevel", this.ghostDomainActualLevel);
      nbt.method_10582("TerrorLevel", String.valueOf(this.terrorLevel));
      nbt.method_10548("GhostDomainRadius", this.getGhostDomainRadius());
      nbt.method_10556("EnableChaseAfterRule", this.enableChaseAfterRule);
      if (this.walkingStateManager != null) {
         nbt.method_10556("IsWalking", this.walkingStateManager.isWalking());
      } else {
         nbt.method_10556("IsWalking", false);
      }

      if (this.coffinNail != null && !this.coffinNail.method_7960()) {
         NbtCompound nailNbt = new NbtCompound();
         this.coffinNail.method_7953(nailNbt);
         nbt.method_10566("CoffinNail", nailNbt);
      }

      if (this.ghostFactionTarget != null && this.ghostFactionTarget.method_5805()) {
         nbt.method_25927("GhostFactionTarget", this.ghostFactionTarget.method_5667());
      }
   }

   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("SpiritualStrength")) {
         this.setSpiritualStrength(nbt.method_10550("SpiritualStrength"));
      }

      if (nbt.method_10545("MaxSpiritualStrength")) {
         this.setMaxSpiritualStrength(nbt.method_10550("MaxSpiritualStrength"));
      }

      if (nbt.method_10545("SpiritualResistance")) {
         this.field_6011.method_12778(SPIRITUAL_RESISTANCE, nbt.method_10550("SpiritualResistance"));
      }

      if (nbt.method_10545("SpiritualDamage")) {
         this.field_6011.method_12778(SPIRITUAL_DAMAGE, nbt.method_10550("SpiritualDamage"));
      }

      if (nbt.method_10545("RecoveryFactor")) {
         this.field_6011.method_12778(RECOVERY_FACTOR, nbt.method_10583("RecoveryFactor"));
      }

      if (nbt.method_10545("IsSuppressed")) {
         this.isSuppressed = nbt.method_10577("IsSuppressed");
      }

      if (nbt.method_10545("IsDeadlocked")) {
         this.setDeadlocked(nbt.method_10577("IsDeadlocked"));
      }

      if (nbt.method_10545("MovementDisabled")) {
         this.movementDisabled = nbt.method_10577("MovementDisabled");
      }

      if (nbt.method_10545("GhostDomainEnabled")) {
         this.ghostDomainEnabled = nbt.method_10577("GhostDomainEnabled");
      }

      if (nbt.method_10545("HasGhostDomain")) {
         this.hasGhostDomain = nbt.method_10577("HasGhostDomain");
      }

      if (nbt.method_10545("GhostDomainLevel")) {
         this.ghostDomainLevel = nbt.method_10550("GhostDomainLevel");
      }

      if (nbt.method_10545("GhostDomainActualLevel")) {
         this.ghostDomainActualLevel = nbt.method_10550("GhostDomainActualLevel");
      }

      if (nbt.method_10545("IsWalking")) {
         boolean savedWalkingState = nbt.method_10577("IsWalking");
         if (this.walkingStateManager != null && savedWalkingState) {
            this.walkingStateManager.setForcedWalking(true);
         }
      }

      if (nbt.method_10545("TerrorLevel")) {
         this.terrorLevel = nbt.method_10558("TerrorLevel").charAt(0);
      }

      if (nbt.method_10545("GhostDomainRadius")) {
         this.setGhostDomainRadius(nbt.method_10583("GhostDomainRadius"));
      }

      if (nbt.method_10545("EnableChaseAfterRule")) {
         this.enableChaseAfterRule = nbt.method_10577("EnableChaseAfterRule");
      } else {
         this.enableChaseAfterRule = true;
      }

      if (nbt.method_10545("CoffinNail")) {
         NbtCompound nailNbt = nbt.method_10562("CoffinNail");
         this.coffinNail = ItemStack.method_7915(nailNbt);
      }

      if (nbt.method_10545("GhostFactionTarget")) {
      }
   }

   @Nullable
   public GhostSlaveEntity spawnGhostSlave() {
      if (this.method_37908().field_9236) {
         return null;
      }

      try {
         GhostSlaveEntity slave = GhostSlaveEntity.createWithMaster(this.method_37908(), this);
         if (this.method_37908().method_8649(slave)) {
            LOGGER.debug("鬼 {} 成功生成鬼奴", this.method_5477().getString());
            return slave;
         } else {
            LOGGER.warn("鬼 {} 生成鬼奴失败", this.method_5477().getString());
            return null;
         }
      } catch (Exception e) {
         LOGGER.error("鬼 {} 生成鬼奴时发生错误", this.method_5477().getString(), e);
         return null;
      }
   }

   public int getGhostSlaveCount() {
      if (this.method_37908().field_9236) {
         return 0;
      }

      double radius = this.getGhostDomainRadius() > 0.0F ? this.getGhostDomainRadius() : 48.0;
      List<GhostSlaveEntity> ghostSlaves = this.method_37908().method_8390(GhostSlaveEntity.class, this.method_5829().method_1014(radius), slave -> {
         Entity master = slave.getMaster();
         return master != null && master.method_5667().equals(this.method_5667());
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
      return this.field_6012;
   }

   protected PlayState handleMovementAnimations(AnimationState<GhostEntity> state) {
      if (this.method_31481() || !this.method_5805()) {
         return PlayState.STOP;
      }

      if (this.isInCustomAnimation()) {
         return PlayState.STOP;
      }

      boolean canMove = !this.isSuppressed() && !this.isDeadlocked() && !this.movementDisabled;
      if (canMove && this.walkingStateManager != null && this.walkingStateManager.isForcedWalking()) {
         return state.setAndContinue(WALK_ANIM);
      }

      boolean grounded = this.method_24828();
      double speedSq = this.method_18798().method_37268();
      if (this.field_6252) {
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
      if (this.method_31481() || !this.method_5805()) {
         return PlayState.STOP;
      } else {
         return this.field_6252 ? state.setAndContinue(ATTACK_ANIM) : PlayState.STOP;
      }
   }

   static class CustomLookAtPlayerGoal extends LookAtEntityGoal {
      private final GhostEntity ghost;

      public CustomLookAtPlayerGoal(GhostEntity ghost, Class<? extends LivingEntity> targetClass, float maxDistance) {
         super(ghost, targetClass, maxDistance);
         this.ghost = ghost;
      }

      public boolean method_6264() {
         return !this.ghost.isSuppressed() && !this.ghost.isDeadlocked() ? super.method_6264() : false;
      }

      public boolean method_6266() {
         return !this.ghost.isSuppressed() && !this.ghost.isDeadlocked() ? super.method_6266() : false;
      }
   }

   static class CustomWanderGoal extends WanderAroundFarGoal {
      private final GhostEntity ghost;

      public CustomWanderGoal(GhostEntity ghost, double speed) {
         super(ghost, speed);
         this.ghost = ghost;
      }

      public boolean method_6264() {
         if (this.ghost.isSuppressed() || this.ghost.isDeadlocked()) {
            return false;
         }

         if (!this.ghost.isChasing && this.ghost.method_5968() == null) {
            if (this.ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode()) {
               return false;
            } else {
               if (this.ghost.field_6012 % (20 + this.ghost.method_6051().method_43048(40)) != 0) {
                  return false;
               }

               if (this.ghost.method_5782()) {
                  return false;
               }

               Vec3d wanderTarget = this.method_6302();
               if (wanderTarget == null) {
                  return false;
               }

               this.field_6563 = wanderTarget.field_1352;
               this.field_6562 = wanderTarget.field_1351;
               this.field_6561 = wanderTarget.field_1350;
               this.field_6565 = false;
               return true;
            }
         } else {
            return false;
         }
      }

      public boolean method_6266() {
         if (!this.ghost.isChasing && this.ghost.method_5968() == null) {
            return this.ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode()
               ? false
               : !this.ghost.isSuppressed() && !this.ghost.isDeadlocked() && super.method_6266();
         } else {
            return false;
         }
      }

      public void method_6269() {
         super.method_6269();
         if (this.ghost.walkingStateManager != null) {
            this.ghost.walkingStateManager.setForcedWalking(true);
         }
      }

      public void method_6270() {
         super.method_6270();
         if (this.ghost.walkingStateManager != null) {
            this.ghost.walkingStateManager.setForcedWalking(false);
         }
      }
   }
}
