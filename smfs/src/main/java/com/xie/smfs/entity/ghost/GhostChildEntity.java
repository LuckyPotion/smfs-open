package com.xie.smfs.entity.ghost;

import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostChildEntity extends GhostEntity {
   private PlayerEntity owner;
   private GhostChildData data;
   private float followStopDistance = 2.0F;
   private float followStartDistance = 15.0F;
   private double followSpeed = 1.0;
   private boolean allowTeleport = true;

   public GhostChildEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world);
      this.data = new GhostChildData();
   }

   public GhostChildEntity(EntityType<? extends GhostEntity> entityType, World world, PlayerEntity owner) {
      super(entityType, world);
      this.owner = owner;
      if (owner != null) {
         this.data = PlayerGhostChildManager.getGhostChildData(owner);
         this.setSpiritualStrength(this.data.getSpiritPower());
         this.setMaxSpiritualStrength(this.data.getMaxSpiritPower());
         this.setSpiritualResistance(this.data.getSpiritResistance());
         this.setSpiritualDamage(this.data.getSpiritDamage());
      } else {
         this.data = new GhostChildData();
      }
   }

   @Override
   protected void initGoals() {
      super.initGoals();
      if (this.owner != null) {
         this.goalSelector
            .add(2, new GhostChildEntity.CustomFollowOwnerGoal(this, this.followSpeed, this.followStopDistance, this.followStartDistance, this.allowTeleport));
         this.goalSelector.add(1, new MeleeAttackGoal(this, 1.8, false));
         final GhostChildEntity ghostChild = this;
         this.targetSelector
            .add(
               1,
               new ActiveTargetGoal(
                  this,
                  LivingEntity.class,
                  20,
                  true,
                  false,
                  new Predicate<LivingEntity>() {
                     public boolean test(LivingEntity entity) {
                        if (ghostChild.isSuppressed() || ghostChild.isDeadlocked()) {
                           return false;
                        } else if (entity == GhostChildEntity.this.owner) {
                           return false;
                        } else if (entity.getAttacking() == ghostChild) {
                           return false;
                        } else {
                           LivingEntity playerTarget = GhostChildEntity.this.owner.getAttacking();
                           if (playerTarget != null && entity == playerTarget) {
                              return true;
                           } else if (entity instanceof GhostChildEntity otherGhostChild
                              && otherGhostChild.getOwner() != null
                              && GhostChildEntity.this.owner != null
                              && otherGhostChild.getOwner().getUuid().equals(GhostChildEntity.this.owner.getUuid())) {
                              return false;
                           } else if (entity instanceof PlayerGhostEntity playerGhost
                              && playerGhost.isServantMode()
                              && playerGhost.getMasterUuid() != null
                              && playerGhost.getMasterUuid().equals(GhostChildEntity.this.owner.getUuid())) {
                              return false;
                           } else if (entity instanceof HostileEntity) {
                              return true;
                           } else {
                              return entity instanceof GhostEntity ? ghostChild.shouldAttackGhost((GhostEntity)entity) : false;
                           }
                        }
                     }
                  }
               )
            );
      }
   }

   public int getLevel() {
      return this.data.getLevel();
   }

   public void setLevel(int level) {
      this.data.setLevel(level);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
         this.setSpiritualStrength(this.data.getSpiritPower());
         this.setMaxSpiritualStrength(this.data.getMaxSpiritPower());
         this.setSpiritualResistance(this.data.getSpiritResistance());
         this.setSpiritualDamage(this.data.getSpiritDamage());
      }
   }

   public int getExperience() {
      return this.data.getExperience();
   }

   public void addExperience(int amount) {
      this.data.addExperience(amount);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
         this.setSpiritualStrength(this.data.getSpiritPower());
         this.setMaxSpiritualStrength(this.data.getMaxSpiritPower());
         this.setSpiritualResistance(this.data.getSpiritResistance());
         this.setSpiritualDamage(this.data.getSpiritDamage());
      }
   }

   public int getSpiritPower() {
      return this.data.getSpiritPower();
   }

   public void setSpiritPower(int spiritPower) {
      this.data.setSpiritPower(spiritPower);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
         this.setSpiritualStrength(this.data.getSpiritPower());
      }
   }

   public int getMaxSpiritPower() {
      return this.data.getMaxSpiritPower();
   }

   public void setMaxSpiritPower(int maxSpiritPower) {
      this.data.setMaxSpiritPower(maxSpiritPower);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
         this.setMaxSpiritualStrength(this.data.getMaxSpiritPower());
      }
   }

   public int getSpiritResistance() {
      return this.data.getSpiritResistance();
   }

   public void setSpiritResistance(int spiritResistance) {
      this.data.setSpiritResistance(spiritResistance);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
         this.setSpiritualResistance(this.data.getSpiritResistance());
      }
   }

   public int getSpiritDamage() {
      return this.data.getSpiritDamage();
   }

   public void setSpiritDamage(int spiritDamage) {
      this.data.setSpiritDamage(spiritDamage);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
         this.setSpiritualDamage(this.data.getSpiritDamage());
      }
   }

   public float getRevivalFactor() {
      return this.data.getRevivalFactor();
   }

   public void setRevivalFactor(float revivalFactor) {
      this.data.setRevivalFactor(revivalFactor);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
      }
   }

   public PlayerEntity getOwner() {
      return this.owner;
   }

   public void setOwner(PlayerEntity owner) {
      this.owner = owner;
      if (owner != null) {
         try {
            this.data = PlayerGhostChildManager.getGhostChildData(owner);
            this.setSpiritualStrength(this.data.getSpiritPower());
            this.setMaxSpiritualStrength(this.data.getMaxSpiritPower());
            this.setSpiritualResistance(this.data.getSpiritResistance());
            this.setSpiritualDamage(this.data.getSpiritDamage());
            this.goalSelector.clear(goal -> true);
            this.targetSelector.clear(goal -> true);
            this.initGoals();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public UUID getMasterUuid() {
      return this.owner != null ? this.owner.getUuid() : null;
   }

   public boolean isSummoned() {
      return this.data.isSummoned();
   }

   public float getFollowStopDistance() {
      return this.followStopDistance;
   }

   public void setFollowStopDistance(float followStopDistance) {
      this.followStopDistance = followStopDistance;
      if (this.owner != null) {
         this.goalSelector.clear(goal -> true);
         this.targetSelector.clear(goal -> true);
         this.initGoals();
      }
   }

   public float getFollowStartDistance() {
      return this.followStartDistance;
   }

   public void setFollowStartDistance(float followStartDistance) {
      this.followStartDistance = followStartDistance;
      if (this.owner != null) {
         this.goalSelector.clear(goal -> true);
         this.targetSelector.clear(goal -> true);
         this.initGoals();
      }
   }

   public double getFollowSpeed() {
      return this.followSpeed;
   }

   public void setFollowSpeed(double followSpeed) {
      this.followSpeed = followSpeed;
      if (this.owner != null) {
         this.goalSelector.clear(goal -> true);
         this.targetSelector.clear(goal -> true);
         this.initGoals();
      }
   }

   public boolean isAllowTeleport() {
      return this.allowTeleport;
   }

   public void setAllowTeleport(boolean allowTeleport) {
      this.allowTeleport = allowTeleport;
      if (this.owner != null) {
         this.goalSelector.clear(goal -> true);
         this.targetSelector.clear(goal -> true);
         this.initGoals();
      }
   }

   public void setSummoned(boolean summoned) {
      this.data.setSummoned(summoned);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
      }
   }

   public void summon() {
      this.data.summon();
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
      }
   }

   public void recall() {
      if (this.hasCoffinNail() && !this.getWorld().isClient) {
         ItemStack coffinNail = this.getCoffinNail();
         if (!coffinNail.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), coffinNail);
            this.getWorld().spawnEntity(itemEntity);
            this.setCoffinNail(ItemStack.EMPTY);
         }
      }

      this.data.recall();
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
      }
   }

   public boolean isBreakthroughRequired(int level) {
      return this.data.isBreakthroughRequired(level);
   }

   public boolean canLevelUp() {
      return this.data.canLevelUp();
   }

   public void performBreakthrough(int levelIndex) {
      this.data.performBreakthrough(levelIndex);
      if (this.owner != null) {
         PlayerGhostChildManager.saveGhostChildData(this.owner, this.data);
      }
   }

   public boolean isAtBreakthroughLevel() {
      return this.data.isAtBreakthroughLevel();
   }

   public int getBreakthroughIndex() {
      return this.data.getBreakthroughIndex();
   }

   @Override
   protected boolean shouldAttackGhost(GhostEntity ghost) {
      if (!super.shouldAttackGhost(ghost)) {
         return false;
      } else if (ghost instanceof PlayerGhostEntity playerGhost
         && playerGhost.isServantMode()
         && playerGhost.getMasterUuid() != null
         && this.getOwner() != null
         && this.getOwner().getUuid().equals(playerGhost.getMasterUuid())) {
         return false;
      } else {
         return ghost instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null && this.getOwner() != null
            ? !this.getOwner().getUuid().equals(ghostChild.getOwner().getUuid())
            : true;
      }
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.owner != null) {
         nbt.putUuid("owner", this.owner.getUuid());
      }
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
   }

   @Override
   public void setTarget(@Nullable LivingEntity target) {
      if (!(this.getOwner() != null && target instanceof PlayerEntity player) || !player.getUuid().equals(this.getOwner().getUuid())) {
         super.setTarget(target);
      }
   }

   @Override
   public boolean tryAttack(Entity target) {
      return this.getOwner() != null && target instanceof PlayerEntity player && player.getUuid().equals(this.getOwner().getUuid())
         ? false
         : super.tryAttack(target);
   }

   @Override
   public void tick() {
      super.tick();
      if ((this.isDeadlocked() || this.isSuppressed()) && !this.getWorld().isClient) {
         ServerWorld serverWorld = (ServerWorld)this.getWorld();
         Vec3d pos = this.getPos();

         for (int i = 0; i < 5; i++) {
            double offsetX = this.getRandom().nextDouble() - 0.5;
            double offsetY = (this.getRandom().nextDouble() - 0.5) * 2.0 + 1.0;
            double offsetZ = this.getRandom().nextDouble() - 0.5;
            serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.1, 0.0, 0.0);
         }
      }
   }

   static class CustomFollowOwnerGoal extends Goal {
      private final GhostChildEntity ghostChild;
      private PlayerEntity owner;
      private final double speed;
      private final float stopDistance;
      private final float startDistance;
      private final boolean canTeleportToOwner;
      private int timeToRecalcPath;

      public CustomFollowOwnerGoal(GhostChildEntity ghostChild, double speed, float stopDistance, float startDistance, boolean teleportToOwner) {
         this.ghostChild = ghostChild;
         this.owner = ghostChild.getOwner();
         this.speed = speed;
         this.stopDistance = stopDistance * stopDistance;
         this.startDistance = startDistance * startDistance;
         this.canTeleportToOwner = teleportToOwner;
         this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
      }

      public boolean canStart() {
         PlayerEntity owner = this.ghostChild.getOwner();
         if (owner == null) {
            return false;
         }

         if (!this.ghostChild.isSummoned()) {
            return false;
         }

         if (owner.isSpectator()) {
            return false;
         }

         if (this.ghostChild.squaredDistanceTo(owner) < this.stopDistance) {
            return false;
         }

         this.owner = owner;
         return true;
      }

      public boolean shouldContinue() {
         return this.owner.isAlive() && this.ghostChild.isSummoned() ? !(this.ghostChild.squaredDistanceTo(this.owner) > this.startDistance) : false;
      }

      public void start() {
         this.timeToRecalcPath = 0;
      }

      public void stop() {
         this.owner = null;
         this.ghostChild.getNavigation().stop();
      }

      public void tick() {
         this.ghostChild.getLookControl().lookAt(this.owner, 10.0F, this.ghostChild.getMaxLookPitchChange());
         if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.ghostChild.isLeashed() && !this.ghostChild.hasPassengers()) {
               double distance = this.ghostChild.squaredDistanceTo(this.owner);
               if (distance >= 144.0) {
                  if (this.canTeleportToOwner) {
                     this.teleportToOwner();
                  } else {
                     this.ghostChild.getNavigation().startMovingTo(this.owner, this.speed);
                  }
               } else {
                  this.ghostChild.getNavigation().startMovingTo(this.owner, this.speed);
               }
            }
         }
      }

      private void teleportToOwner() {
         if (this.canTeleportToOwner) {
            Vec3d vec3d = this.owner.getPos();

            for (int i = 0; i < 10; i++) {
               double d = this.ghostChild.getRandom().nextDouble() * 2.0 - 1.0;
               double e = this.ghostChild.getRandom().nextDouble() * 2.0 - 1.0;
               double f = this.ghostChild.getRandom().nextDouble() * 2.0 - 1.0;
               double g = this.owner.getWidth() + 1.0;
               double h = vec3d.x + d * g;
               double j = vec3d.y + e;
               double k = vec3d.z + f * g;
               this.ghostChild.teleport(h, j, k);
               if (!this.ghostChild.getNavigation().isIdle()) {
                  break;
               }
            }
         }
      }
   }
}
