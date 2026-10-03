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
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 50.0)
         .method_26868(EntityAttributes.field_23719, 0.3)
         .method_26868(EntityAttributes.field_23721, 5.0)
         .method_26868(EntityAttributes.field_23717, 24.0);
   }

   protected void method_5959() {
      this.field_6201.method_6277(1, new MeleeAttackGoal(this, 1.2, false));
      this.field_6201.method_6277(2, new GhostDreamEntity.LayDownGoal(this));
      this.field_6201.method_6277(3, new WanderAroundFarGoal(this, 1.0));
      this.field_6201.method_6277(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.field_6201.method_6277(5, new LookAroundGoal(this));
      this.field_6185
         .method_6277(
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

   public void method_5773() {
      super.method_5773();
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

         for (PlayerEntity player : this.method_37908().method_18456()) {
            if (player != this.owner && !player.method_7325() && player.method_5805()) {
               boolean otherHasGhostDream = PlayerEvents.hasGhostType(player, "ghost_dream");
               if (!otherHasGhostDream) {
                  double distance = this.method_5858(player);
                  if (distance < playerDistance) {
                     playerDistance = distance;
                     targetPlayer = player;
                  }
               }
            }
         }

         if (targetPlayer != null) {
            double distance = this.method_5858(targetPlayer);
            if (distance <= attackRange * attackRange) {
               this.triggerGhostDreamEvent(targetPlayer);
               return;
            }

            this.method_5942().method_6335(targetPlayer, 1.0);
         }

         GhostMasterEntity ghostMasterEntity = null;
         double ghostMasterDistance = followRange * followRange;

         for (GhostMasterEntity entity : this.method_37908()
            .method_8390(GhostMasterEntity.class, this.method_5829().method_1014(followRange), e -> e.method_5805())) {
            if (!(entity instanceof LiLePingEntity)) {
               double distance = this.method_5858(entity);
               if (distance < ghostMasterDistance) {
                  ghostMasterDistance = distance;
                  ghostMasterEntity = entity;
               }
            }
         }

         if (ghostMasterEntity != null) {
            double distance = this.method_5858(ghostMasterEntity);
            if (distance <= attackRange * attackRange) {
               DamageSource damageSource = this.method_48923().method_48812(this);
               InstantKillUtil.executeMultiLevelInstantKill(ghostMasterEntity, damageSource);
               this.spawnBlackParticles();
               this.method_31472();
               return;
            }

            this.method_5942().method_6335(ghostMasterEntity, 1.0);
         }

         LivingEntity targetEntity = null;
         double nearestDistance = followRange * followRange;

         for (LivingEntity entity : this.method_37908()
            .method_8390(LivingEntity.class, this.method_5829().method_1014(followRange), e -> e != this && e != this.owner && e.method_5805())) {
            if (!(entity instanceof PlayerEntity)
               && !(entity instanceof GhostMasterEntity)
               && !(entity instanceof GhostEntity)
               && !(entity instanceof YangJianEntity)) {
               double distance = this.method_5858(entity);
               if (distance < nearestDistance) {
                  nearestDistance = distance;
                  targetEntity = entity;
               }
            }
         }

         if (targetEntity != null && ghostMasterEntity == null) {
            double distance = this.method_5858(targetEntity);
            if (distance <= attackRange * attackRange) {
               DamageSource damageSource = this.method_48923().method_48812(this);
               InstantKillUtil.executeMultiLevelInstantKill(targetEntity, damageSource);
               this.spawnBlackParticles();
               this.method_31472();
               return;
            }

            this.method_5942().method_6335(targetEntity, 1.0);
         }

         if (this.tamedLifespanTimer >= 200) {
            this.spawnBlackParticles();
            this.method_31472();
         }
      } else {
         LivingEntity target = this.method_5968();
         if (target != null && target.method_5805()) {
            this.noTargetTimer = 0;
         } else {
            this.noTargetTimer++;
            if (this.noTargetTimer >= 200) {
               this.spawnBlackParticles();
               this.method_31472();
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

   public boolean method_5643(DamageSource source, float amount) {
      return this.tamed && this.owner != null ? false : super.method_5643(source, amount);
   }

   public boolean method_5679(DamageSource damageSource) {
      return this.tamed && this.owner != null ? true : super.method_5679(damageSource);
   }

   public boolean method_6181() {
      return this.tamed;
   }

   public void method_6173(boolean tamed) {
      this.tamed = tamed;
   }

   public PlayerEntity getOwner() {
      return this.owner;
   }

   public void method_6170(PlayerEntity owner) {
      this.owner = owner;
   }

   public boolean method_5931(PlayerEntity player) {
      return false;
   }

   public UUID method_6139() {
      return null;
   }

   public EntityView method_48926() {
      return null;
   }

   public void method_6174(UUID uuid) {
   }

   private void triggerGhostDreamEvent(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         GhostDreamManager.enterGhostDream(serverPlayer);
      }

      this.spawnBlackParticles();
      this.method_31472();
   }

   private void spawnBlackParticles() {
      double x = this.method_23317();
      double y = this.method_23318();
      double z = this.method_23321();
      if (this.method_37908().method_8608()) {
         World world = this.method_37908();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.field_5974.method_43058() - 0.5) * 4.0;
            double offsetY = (this.field_5974.method_43058() - 0.5) * 4.0;
            double offsetZ = (this.field_5974.method_43058() - 0.5) * 4.0;
            double velocityX = (this.field_5974.method_43058() - 0.5) * 0.2;
            double velocityY = (this.field_5974.method_43058() - 0.5) * 0.2;
            double velocityZ = (this.field_5974.method_43058() - 0.5) * 0.2;
            world.method_8406(ParticleTypes.field_11251, x + offsetX, y + offsetY, z + offsetZ, velocityX, velocityY, velocityZ);
            world.method_8406(ParticleTypes.field_11237, x + offsetX, y + offsetY, z + offsetZ, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5);
         }
      } else {
         ServerWorld serverWorld = (ServerWorld)this.method_37908();

         for (int i = 0; i < 50; i++) {
            double offsetX = (this.field_5974.method_43058() - 0.5) * 4.0;
            double offsetY = (this.field_5974.method_43058() - 0.5) * 4.0;
            double offsetZ = (this.field_5974.method_43058() - 0.5) * 4.0;
            double velocityX = (this.field_5974.method_43058() - 0.5) * 0.2;
            double velocityY = (this.field_5974.method_43058() - 0.5) * 0.2;
            double velocityZ = (this.field_5974.method_43058() - 0.5) * 0.2;
            serverWorld.method_14199(ParticleTypes.field_11251, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX, velocityY, velocityZ, 0.0);
            serverWorld.method_14199(
               ParticleTypes.field_11237, x + offsetX, y + offsetY, z + offsetZ, 1, velocityX * 0.5, velocityY * 0.5, velocityZ * 0.5, 0.0
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
      if (this.method_31481() || !this.method_5805()) {
         return PlayState.STOP;
      } else if (this.isLayingDown) {
         state.setAnimation(LAY_DOWN_ANIM);
         return PlayState.CONTINUE;
      } else if (this.method_18798().method_37268() > 0.01) {
         state.setAnimation(WALK_ANIM);
         return PlayState.CONTINUE;
      } else {
         state.setAnimation(IDLE_ANIM);
         return PlayState.CONTINUE;
      }
   }

   private PlayState handleAttackAnimations(AnimationState<GhostDreamEntity> state) {
      if (this.method_6510()) {
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
      return this.field_6012;
   }

   static class LayDownGoal extends Goal {
      private final GhostDreamEntity ghostDream;
      private int timer;

      public LayDownGoal(GhostDreamEntity ghostDream) {
         this.ghostDream = ghostDream;
      }

      public boolean method_6264() {
         return !this.ghostDream.method_6510()
            && this.ghostDream.method_5968() == null
            && this.ghostDream.field_5974.method_43048(200) == 0
            && !this.ghostDream.isLayingDown();
      }

      public void method_6269() {
         this.ghostDream.setLayingDown(true);
         this.timer = 0;
      }

      public boolean method_6266() {
         return this.ghostDream.isLayingDown() && this.timer < 120;
      }

      public void method_6268() {
         this.timer++;
      }

      public void method_6270() {
         this.ghostDream.setLayingDown(false);
      }
   }
}
