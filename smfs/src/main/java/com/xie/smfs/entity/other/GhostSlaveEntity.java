package com.xie.smfs.entity.other;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModEntities;
import com.xie.smfs.registry.ModItems;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.Goal.Control;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostSlaveEntity extends HuskEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostSlaveEntity.class);
   @Nullable
   private UUID masterUuid;
   @Nullable
   private Entity masterEntity;
   private static final double MAX_FOLLOW_DISTANCE = 48.0;
   private static final double GOLD_INGOT_DROP_CHANCE = 0.1;
   private static final double EERIE_RAG_DROP_CHANCE = 0.1;
   private static final double VISCOUS_BLOOD_DROP_CHANCE = 0.002;
   private static final double BLACKENED_TOOTH_DROP_CHANCE = 0.002;
   private LivingEntity lastTarget = null;
   private boolean isInAttackRange = false;
   private int masterNotFoundCount = 0;

   public GhostSlaveEntity(EntityType<? extends HuskEntity> entityType, World world) {
      super(entityType, world);
      this.setNoGravity(false);
   }

   public static Builder createGhostSlaveAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 200.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
         .add(EntityAttributes.GENERIC_ARMOR, 2.0)
         .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.0)
         .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.0)
         .add(EntityAttributes.GENERIC_LUCK, 0.0)
         .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0);
   }

   public void onDeath(DamageSource damageSource) {
      super.onDeath(damageSource);
      if (!this.getWorld().isClient) {
         if (this.getRandom().nextDouble() < 0.1) {
            ItemStack goldIngotStack = new ItemStack(Items.GOLD_INGOT, 1);
            this.dropStack(goldIngotStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落金锭", this.getUuid());
         } else {
            LOGGER.debug("鬼奴 {} 死亡，未掉落金锭", this.getUuid());
         }

         if (this.getRandom().nextDouble() < 0.1) {
            ItemStack eerieRagStack = new ItemStack(ModItems.EERIE_RAG, 1 + this.getRandom().nextInt(2));
            this.dropStack(eerieRagStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落沾染灵异的破布", this.getUuid());
         }

         if (this.getRandom().nextDouble() < 0.002) {
            ItemStack viscousBloodStack = new ItemStack(ModItems.VISCOUS_BLOOD, 1);
            this.dropStack(viscousBloodStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落粘稠的血液", this.getUuid());
         }

         if (this.getRandom().nextDouble() < 0.002) {
            ItemStack blackenedToothStack = new ItemStack(ModItems.BLACKENED_TOOTH, 1 + this.getRandom().nextInt(2));
            this.dropStack(blackenedToothStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落发黑的牙齿", this.getUuid());
         }
      }
   }

   public boolean isAffectedByDaylight() {
      return false;
   }

   public boolean canSpawn(WorldAccess world, SpawnReason spawnReason) {
      return true;
   }

   public boolean canSpawn(WorldView world) {
      if (world instanceof ServerWorld serverWorld) {
         int nearbySlaves = serverWorld.getEntitiesByClass(GhostSlaveEntity.class, this.getBoundingBox().expand(32.0), entity -> true).size();
         return nearbySlaves < 10;
      } else {
         return true;
      }
   }

   protected void initGoals() {
      this.targetSelector.clear(null);
      this.goalSelector.clear(null);
      this.targetSelector.add(1, new ActiveTargetGoal(this, LivingEntity.class, 10, true, false, entity -> {
         if (entity == this.masterEntity) {
            return false;
         }

         if (entity == this) {
            return false;
         }

         if (entity instanceof GhostSlaveEntity otherSlave) {
            Entity otherMaster = otherSlave.getMaster();
            Entity myMaster = this.getMaster();
            if (otherMaster != null && myMaster != null && otherMaster.equals(myMaster)) {
               return false;
            }
         }

         if (entity instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
            Entity myMaster = this.getMaster();
            if (myMaster instanceof PlayerEntity && playerGhost.getMasterUuid().equals(myMaster.getUuid())) {
               return false;
            }
         }

         if (entity.getAttacking() == this) {
            return false;
         } else if (this.masterEntity instanceof PlayerEntity playerMaster) {
            LivingEntity playerTarget = playerMaster.getAttacking();
            return playerTarget != null && entity == playerTarget ? true : entity instanceof HostileEntity && !(entity instanceof GhostSlaveEntity);
         } else {
            return entity instanceof PlayerEntity;
         }
      }));
      this.goalSelector.add(1, new GhostSlaveEntity.FollowOwnerGoal(this, 1.0, 40.0F, 4.0F));
      this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
   }

   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         this.updateMasterTracking();
         if (this.masterEntity == null && this.masterUuid != null) {
            this.restoreMasterEntity();
         }

         this.followMaster();
         this.checkMasterStatus();
         this.detectAndLogAttackBehavior();
      }
   }

   public boolean tryAttack(Entity target) {
      boolean attacked = super.tryAttack(target);
      if (attacked && target instanceof VillagerEntity) {
         LOGGER.debug("鬼奴 {} 攻击了村民 {}，但不会转换村民为僵尸", this.getName().getString(), target.getName().getString());
      }

      return attacked;
   }

   private void updateMasterTracking() {
      if (this.masterEntity == null && this.masterUuid != null) {
         this.restoreMasterEntity();
      }
   }

   private void restoreMasterEntity() {
      if (this.masterUuid != null) {
         Iterator var1 = this.getWorld()
            .getEntitiesByClass(
               Entity.class,
               this.getBoundingBox().expand(128.0),
               e -> e.getUuid().equals(this.masterUuid) && (e instanceof GhostEntity || e instanceof PlayerEntity)
            )
            .iterator();
         if (var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            this.masterEntity = entity;
         } else {
            if (this.masterEntity == null) {
               var1 = this.getWorld()
                  .getEntitiesByClass(
                     Entity.class,
                     this.getBoundingBox().expand(512.0),
                     e -> e.getUuid().equals(this.masterUuid) && (e instanceof GhostEntity || e instanceof PlayerEntity)
                  )
                  .iterator();
               if (var1.hasNext()) {
                  Entity entity = (Entity)var1.next();
                  this.masterEntity = entity;
                  return;
               }
            }
         }
      }
   }

   private void followMaster() {
      if (this.masterEntity != null && this.masterEntity.isAlive()) {
         double distance = this.squaredDistanceTo(this.masterEntity);
         if (distance > 2304.0) {
            this.getNavigation().startMovingTo(this.masterEntity, 1.2);
         } else if (distance > 1024.0) {
            this.getNavigation().startMovingTo(this.masterEntity, 1.0);
         }
      }
   }

   private void checkMasterStatus() {
      if (this.masterEntity != null) {
         this.masterNotFoundCount = 0;
         if (this.masterEntity instanceof GhostEntity ghostMaster && (ghostMaster.isDeadlocked() || ghostMaster.isSuppressed())) {
            this.setVelocity(0.0, 0.0, 0.0);
            this.getNavigation().stop();
         }

         if (!this.masterEntity.isAlive() || this.masterEntity.isRemoved()) {
            this.kill();
         }
      } else if (this.masterUuid != null) {
         this.masterNotFoundCount++;
         LOGGER.warn("鬼奴 {} 第 {} 次找不到主人，主人UUID: {}", this.getUuid(), this.masterNotFoundCount, this.masterUuid);
         if (this.masterNotFoundCount >= 5) {
            LOGGER.error("鬼奴 {} 连续5次找不到主人，自动死亡", this.getUuid());
            this.kill();
         }
      } else {
         this.masterNotFoundCount = 0;
      }
   }

   private void detectAndLogAttackBehavior() {
      LivingEntity currentTarget = this.getTarget();
      if (currentTarget != this.lastTarget) {
         if (currentTarget != null) {
            LOGGER.debug("鬼奴攻击目标变化: {} -> {}", this.getName().getString(), currentTarget.getName().getString());
            if (currentTarget == this) {
               LOGGER.warn("鬼奴攻击异常: {} 正在攻击自己，已自动取消目标", this.getName().getString());
               this.setTarget(null);
               return;
            }

            if (currentTarget instanceof GhostSlaveEntity targetSlave
               && this.masterEntity != null
               && targetSlave.getMaster() != null
               && this.masterEntity.equals(targetSlave.getMaster())) {
               LOGGER.warn("鬼奴攻击异常: {} 正在攻击同主人鬼奴 {}，已自动取消目标", this.getName().getString(), currentTarget.getName().getString());
               this.setTarget(null);
               return;
            }
         } else {
            LOGGER.debug("鬼奴停止攻击: {} 目标: {}", this.getName().getString(), this.lastTarget.getName().getString());
         }

         this.lastTarget = currentTarget;
      }
   }

   public void setMaster(Entity master) {
      this.masterUuid = master.getUuid();
      this.masterEntity = master;
   }

   @Nullable
   public Entity getMaster() {
      return this.masterEntity;
   }

   public void dieWithMaster() {
      if (this.isAlive()) {
         LOGGER.debug("鬼奴 {} 随主人一起死亡", this.getUuid());
         this.kill();
      }
   }

   public boolean isVisible() {
      return this.masterEntity instanceof GhostEntity ghostMaster ? ghostMaster.isVisible() : true;
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.masterUuid != null) {
         nbt.putUuid("MasterUuid", this.masterUuid);
      }
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.containsUuid("MasterUuid")) {
         this.masterUuid = nbt.getUuid("MasterUuid");
         this.restoreMasterEntity();
      }
   }

   public boolean isOnFire() {
      return super.isOnFire();
   }

   public void setOnFireFor(int seconds) {
      super.setOnFireFor(seconds);
   }

   public boolean isAffectedBySplashPotions() {
      return false;
   }

   public boolean canHaveStatusEffect(StatusEffectInstance effect) {
      return true;
   }

   public static GhostSlaveEntity createWithMaster(World world, GhostEntity master) {
      GhostSlaveEntity slave = new GhostSlaveEntity(ModEntities.GHOST_SLAVE, world);
      slave.setMaster(master);
      slave.refreshPositionAndAngles(
         master.getX() + (world.random.nextDouble() - 0.5) * 3.0,
         master.getY(),
         master.getZ() + (world.random.nextDouble() - 0.5) * 3.0,
         world.random.nextFloat() * 360.0F,
         0.0F
      );
      return slave;
   }

   public boolean isInAttackRange() {
      return this.isInAttackRange;
   }

   private static class FollowOwnerGoal extends Goal {
      private final GhostSlaveEntity ghostSlave;
      private final double speed;
      private final float minDistance;
      private final float maxDistance;
      private int updateCountdownTicks;

      public FollowOwnerGoal(GhostSlaveEntity ghostSlave, double speed, float minDistance, float maxDistance) {
         this.ghostSlave = ghostSlave;
         this.speed = speed;
         this.minDistance = minDistance;
         this.maxDistance = maxDistance;
         this.setControls(EnumSet.of(Control.MOVE));
      }

      public boolean canStart() {
         Entity owner = this.ghostSlave.getMaster();
         if (owner != null && owner.isAlive()) {
            if (owner instanceof PlayerEntity player && player.isSpectator()) {
               return false;
            } else {
               double distance = this.ghostSlave.squaredDistanceTo(owner);
               return distance > this.minDistance * this.minDistance;
            }
         } else {
            return false;
         }
      }

      public boolean shouldContinue() {
         Entity owner = this.ghostSlave.getMaster();
         if (owner != null && owner.isAlive()) {
            if (owner instanceof PlayerEntity player && player.isSpectator()) {
               return false;
            } else {
               double distance = this.ghostSlave.squaredDistanceTo(owner);
               return distance > this.minDistance * this.minDistance && distance < this.maxDistance * this.maxDistance;
            }
         } else {
            return false;
         }
      }

      public void start() {
         this.updateCountdownTicks = 0;
      }

      public void stop() {
         this.ghostSlave.getNavigation().stop();
      }

      public void tick() {
         Entity owner = this.ghostSlave.getMaster();
         if (owner != null && owner.isAlive()) {
            this.ghostSlave.getLookControl().lookAt(owner, 10.0F, this.ghostSlave.getMaxLookPitchChange());
            if (--this.updateCountdownTicks <= 0) {
               this.updateCountdownTicks = 10;
               double squaredDistance = this.ghostSlave.squaredDistanceTo(owner);
               double minSquaredDistance = this.minDistance * this.minDistance;
               double maxSquaredDistance = this.maxDistance * this.maxDistance;
               if (squaredDistance <= minSquaredDistance) {
                  this.ghostSlave.getNavigation().stop();
               } else if (squaredDistance <= maxSquaredDistance) {
                  this.ghostSlave.getNavigation().startMovingTo(owner, this.speed);
               } else {
                  this.tryTeleport();
               }
            }
         }
      }

      private void tryTeleport() {
         Entity owner = this.ghostSlave.getMaster();
         if (owner != null) {
            for (int i = 0; i < 10; i++) {
               double x = owner.getX() + (this.ghostSlave.getRandom().nextDouble() - 0.5) * 10.0;
               double y = owner.getY() + this.ghostSlave.getRandom().nextDouble() * 3.0;
               double z = owner.getZ() + (this.ghostSlave.getRandom().nextDouble() - 0.5) * 10.0;
               if (this.ghostSlave.teleport(x, y, z, true)) {
                  return;
               }
            }
         }
      }
   }
}
