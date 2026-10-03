package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.master.LiLePingEntity;
import com.xie.smfs.entity.master.YangJianEntity;
import com.xie.smfs.manager.GhostDreamManager;
import com.xie.smfs.util.InstantKillUtil;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhostDreamEntity extends WolfEntity implements GeoAnimatable {
   private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
   private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
   private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
   private static final RawAnimation LAY_DOWN_ANIM = RawAnimation.begin().thenLoop("down");
   private boolean isLayingDown = false;
   private int layDownTimer = 0;
   private PlayerEntity owner;
   private boolean tamed = false;
   private int tamedLifespanTimer = 0;
   private static final int TAMED_LIFESPAN_TICKS = 200;
   private int noTargetTimer = 0;
   private static final int NO_TARGET_DISAPPEAR_TICKS = 200;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public GhostDreamEntity(EntityType<? extends WolfEntity> entityType, World world) {
      super(entityType, world);
   }

   public static Builder createGhostDreamAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
   }

   protected void initGoals() {
      this.goalSelector.add(1, new MeleeAttackGoal(this, 1.2, false));
      this.goalSelector.add(2, new GhostDreamEntity.LayDownGoal(this));
      this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(5, new LookAroundGoal(this));
      this.targetSelector
         .add(
            1,
            new ActiveTargetGoal(
               this,
               PlayerEntity.class,
               10,
               true,
               false,
               entity -> entity instanceof PlayerEntity player ? !PlayerEvents.hasGhostType(player, "ghost_dream") : true
            )
         );
   }

   public void tick() {
      super.tick();
      if (this.isLayingDown) {
         this.layDownTimer++;
         if (this.layDownTimer >= 120) {
            this.isLayingDown = false;
            this.layDownTimer = 0;
         }
      }

      if (this.tamed && this.owner != null) {
         this.tamedLifespanTimer++;
         double followRange = 24.0;
         double attackRange = 3.0;
         PlayerEntity targetPlayer = null;
         double playerDistance = followRange * followRange;

         for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (player != this.owner && !player.isSpectator() && player.isAlive()) {
               boolean otherHasGhostDream = PlayerEvents.hasGhostType(player, "ghost_dream");
               if (!otherHasGhostDream) {
                  double distance = this.squaredDistanceTo(player);
                  if (distance < playerDistance) {
                     playerDistance = distance;
                     targetPlayer = player;
                  }
               }
            }
         }

         if (targetPlayer != null) {
            double distance = this.squaredDistanceTo(targetPlayer);
            if (distance <= attackRange * attackRange) {
               this.triggerGhostDreamEvent(targetPlayer);
               return;
            }

            this.getNavigation().startMovingTo(targetPlayer, 1.0);
         }

         GhostMasterEntity ghostMasterEntity = null;
         double ghostMasterDistance = followRange * followRange;

         for (GhostMasterEntity entity : this.getWorld()
            .getEntitiesByClass(GhostMasterEntity.class, this.getBoundingBox().expand(followRange), e -> e.isAlive())) {
            if (!(entity instanceof LiLePingEntity)) {
               double distance = this.squaredDistanceTo(entity);
               if (distance < ghostMasterDistance) {
                  ghostMasterDistance = distance;
                  ghostMasterEntity = entity;
               }
            }
         }

         if (ghostMasterEntity != null) {
            double distance = this.squaredDistanceTo(ghostMasterEntity);
            if (distance <= attackRange * attackRange) {
               DamageSource damageSource = this.getDamageSources().mobAttack(this);
               InstantKillUtil.executeMultiLevelInstantKill(ghostMasterEntity, damageSource);
               this.spawnBlackParticles();
               this.discard();
               return;
            }

            this.getNavigation().startMovingTo(ghostMasterEntity, 1.0);
         }

         LivingEntity targetEntity = null;
         double nearestDistance = followRange * followRange;

         for (LivingEntity entity : this.getWorld()
            .getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(followRange), e -> e != this && e != this.owner && e.isAlive())) {
            if (!(entity instanceof PlayerEntity)
               && !(entity instanceof GhostMasterEntity)
               && !(entity instanceof GhostEntity)
               && !(entity instanceof YangJianEntity)) {
               double distance = this.squaredDistanceTo(entity);
               if (distance < nearestDistance) {
                  nearestDistance = distance;
                  targetEntity = entity;
               }
            }
         }

         if (targetEntity != null && ghostMasterEntity == null) {
            double distance = this.squaredDistanceTo(targetEntity);
            if (distance <= attackRange * attackRange) {
               DamageSource damageSource = this.getDamageSources().mobAttack(this);
               InstantKillUtil.executeMultiLevelInstantKill(targetEntity, damageSource);
               this.spawnBlackParticles();
               this.discard();
               return;
            }

            this.getNavigation().startMovingTo(targetEntity, 1.0);
         }

         if (this.tamedLifespanTimer >= 200) {
            this.spawnBlackParticles();
            this.discard();
         }
      } else {
         LivingEntity target = this.getTarget();
         if (target != null && target.isAlive()) {
            this.noTargetTimer = 0;
         } else {
            this.noTargetTimer++;
            if (this.noTargetTimer >= 200) {
               this.spawnBlackParticles();
               this.discard();
            }
         }
      }
   }

   public boolean isLayingDown() {
      return this.isLayingDown;
   }

   public void setLayingDown(boolean layingDown) {
      this.isLayingDown = layingDown;
      this.layDownTimer = 0;
   }

   public boolean damage(DamageSource source, float amount) {
      return this.tamed && this.owner != null ? false : super.damage(source, amount);
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return this.tamed && this.owner != null ? true : super.isInvulnerableTo(damageSource);
   }

   public boolean isTamed() {
      return this.tamed;
   }

   public void setTamed(boolean tamed) {
      this.tamed = tamed;
   }

   public PlayerEntity getOwner() {
      return this.owner;
   }

   public void setOwner(PlayerEntity player) {
      this.owner = player;
   }

   public boolean canBeLeashedBy(PlayerEntity player) {
      return false;
   }

   public UUID getOwnerUuid() {
      return null;
   }

   public EntityView method_48926() {
      return null;
   }

   public void setOwnerUuid(UUID uuid) {
   }

   private void triggerGhostDreamEvent(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         GhostDreamManager.enterGhostDream(serverPlayer);
      }

      this.spawnBlackParticles();
      this.discard();
   }

   private void spawnBlackParticles() {
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      if (this.getWorld().isClient()) {
         World world = this.getWorld();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 4.0;
            double offsetY = (this.random.nextDouble() - 0.5) * 4.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 4.0;
            double velocityX = (this.random.nextDouble() - 0.5) * 0.2;
            double velocityY = (this.random.nextDouble() - 0.5) * 0.2;
            double velocityZ = (this.random.nextDouble() - 0.5) * 0.2;
            world.addParticle(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, velocityX, velocityY, velocityZ);
            world.addParticle(ParticleTypes.LARGE_SMOKE, x + offsetX, y + offsetY, z + offsetZ, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
         }
      } else {
         ServerWorld serverWorld = (ServerWorld)this.getWorld();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 4.0;
            double offsetY = (this.random.nextDouble() - 0.5) * 4.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 4.0;
            double velocityX = (this.random.nextDouble() - 0.5) * 0.2;
            double velocityY = (this.random.nextDouble() - 0.5) * 0.2;
            double velocityZ = (this.random.nextDouble() - 0.5) * 0.2;
            serverWorld.spawnParticles(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.0);
            serverWorld.spawnParticles(
               ParticleTypes.LARGE_SMOKE, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5, 0.0
            );
         }
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "movement", 5, this::handleMovementAnimations));
      controllers.add(new AnimationController<>(this, "attack", 10, this::handleAttackAnimations));
   }

   private PlayState handleMovementAnimations(AnimationState<GhostDreamEntity> state) {
      if (this.isRemoved() || !this.isAlive()) {
         return PlayState.STOP;
      } else if (this.isLayingDown) {
         state.setAnimation(LAY_DOWN_ANIM);
         return PlayState.CONTINUE;
      } else if (this.getVelocity().horizontalLengthSquared() > 0.01) {
         state.setAnimation(WALK_ANIM);
         return PlayState.CONTINUE;
      } else {
         state.setAnimation(IDLE_ANIM);
         return PlayState.CONTINUE;
      }
   }

   private PlayState handleAttackAnimations(AnimationState<GhostDreamEntity> state) {
      if (this.isAttacking()) {
         state.setAnimation(ATTACK_ANIM);
         return PlayState.CONTINUE;
      } else {
         return PlayState.STOP;
      }
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object o) {
      return this.age;
   }

   static class LayDownGoal extends Goal {
      private final GhostDreamEntity ghostDream;
      private int timer;

      public LayDownGoal(GhostDreamEntity ghostDream) {
         this.ghostDream = ghostDream;
      }

      public boolean canStart() {
         return !this.ghostDream.isAttacking()
            && this.ghostDream.getTarget() == null
            && this.ghostDream.random.nextInt(200) == 0
            && !this.ghostDream.isLayingDown();
      }

      public void start() {
         this.ghostDream.setLayingDown(true);
         this.timer = 0;
      }

      public boolean shouldContinue() {
         return this.ghostDream.isLayingDown() && this.timer < 120;
      }

      public void tick() {
         this.timer++;
      }

      public void stop() {
         this.ghostDream.setLayingDown(false);
      }
   }
}
