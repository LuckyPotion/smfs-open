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
      this.method_5875(false);
   }

   public static Builder createGhostSlaveAttributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 200.0)
         .method_26868(EntityAttributes.field_23719, 0.23)
         .method_26868(EntityAttributes.field_23721, 6.0)
         .method_26868(EntityAttributes.field_23717, 32.0)
         .method_26868(EntityAttributes.field_23724, 2.0)
         .method_26868(EntityAttributes.field_23722, 0.0)
         .method_26868(EntityAttributes.field_23718, 0.0)
         .method_26868(EntityAttributes.field_23726, 0.0)
         .method_26868(EntityAttributes.field_23727, 0.0);
   }

   public void method_6078(DamageSource damageSource) {
      super.method_6078(damageSource);
      if (!this.method_37908().field_9236) {
         if (this.method_6051().method_43058() < 0.1) {
            ItemStack goldIngotStack = new ItemStack(Items.field_8695, 1);
            this.method_5699(goldIngotStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落金锭", this.method_5667());
         } else {
            LOGGER.debug("鬼奴 {} 死亡，未掉落金锭", this.method_5667());
         }

         if (this.method_6051().method_43058() < 0.1) {
            ItemStack eerieRagStack = new ItemStack(ModItems.EERIE_RAG, 1 + this.method_6051().method_43048(2));
            this.method_5699(eerieRagStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落沾染灵异的破布", this.method_5667());
         }

         if (this.method_6051().method_43058() < 0.002) {
            ItemStack viscousBloodStack = new ItemStack(ModItems.VISCOUS_BLOOD, 1);
            this.method_5699(viscousBloodStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落粘稠的血液", this.method_5667());
         }

         if (this.method_6051().method_43058() < 0.002) {
            ItemStack blackenedToothStack = new ItemStack(ModItems.BLACKENED_TOOTH, 1 + this.method_6051().method_43048(2));
            this.method_5699(blackenedToothStack, 0.5F);
            LOGGER.debug("鬼奴 {} 死亡，成功掉落发黑的牙齿", this.method_5667());
         }
      }
   }

   public boolean method_5972() {
      return false;
   }

   public boolean method_5979(WorldAccess world, SpawnReason spawnReason) {
      return true;
   }

   public boolean method_5957(WorldView world) {
      if (world instanceof ServerWorld serverWorld) {
         int nearbySlaves = serverWorld.method_8390(GhostSlaveEntity.class, this.method_5829().method_1014(32.0), entity -> true).size();
         return nearbySlaves < 10;
      } else {
         return true;
      }
   }

   protected void method_5959() {
      this.field_6185.method_35113(null);
      this.field_6201.method_35113(null);
      this.field_6185.method_6277(1, new ActiveTargetGoal(this, LivingEntity.class, 10, true, false, entity -> {
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
            if (myMaster instanceof PlayerEntity && playerGhost.getMasterUuid().equals(myMaster.method_5667())) {
               return false;
            }
         }

         if (entity.method_6052() == this) {
            return false;
         } else if (this.masterEntity instanceof PlayerEntity playerMaster) {
            LivingEntity playerTarget = playerMaster.method_6052();
            return playerTarget != null && entity == playerTarget ? true : entity instanceof HostileEntity && !(entity instanceof GhostSlaveEntity);
         } else {
            return entity instanceof PlayerEntity;
         }
      }));
      this.field_6201.method_6277(1, new GhostSlaveEntity.FollowOwnerGoal(this, 1.0, 40.0F, 4.0F));
      this.field_6201.method_6277(2, new MeleeAttackGoal(this, 1.0, false));
      this.field_6201.method_6277(3, new WanderAroundFarGoal(this, 1.0));
   }

   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         this.updateMasterTracking();
         if (this.masterEntity == null && this.masterUuid != null) {
            this.restoreMasterEntity();
         }

         this.followMaster();
         this.checkMasterStatus();
         this.detectAndLogAttackBehavior();
      }
   }

   public boolean method_6121(Entity target) {
      boolean attacked = super.method_6121(target);
      if (attacked && target instanceof VillagerEntity) {
         LOGGER.debug("鬼奴 {} 攻击了村民 {}，但不会转换村民为僵尸", this.method_5477().getString(), target.method_5477().getString());
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
         Iterator var1 = this.method_37908()
            .method_8390(
               Entity.class,
               this.method_5829().method_1014(128.0),
               e -> e.method_5667().equals(this.masterUuid) && (e instanceof GhostEntity || e instanceof PlayerEntity)
            )
            .iterator();
         if (var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            this.masterEntity = entity;
         } else {
            if (this.masterEntity == null) {
               var1 = this.method_37908()
                  .method_8390(
                     Entity.class,
                     this.method_5829().method_1014(512.0),
                     e -> e.method_5667().equals(this.masterUuid) && (e instanceof GhostEntity || e instanceof PlayerEntity)
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
      if (this.masterEntity != null && this.masterEntity.method_5805()) {
         double distance = this.method_5858(this.masterEntity);
         if (distance > 2304.0) {
            this.method_5942().method_6335(this.masterEntity, 1.2);
         } else if (distance > 1024.0) {
            this.method_5942().method_6335(this.masterEntity, 1.0);
         }
      }
   }

   private void checkMasterStatus() {
      if (this.masterEntity != null) {
         this.masterNotFoundCount = 0;
         if (this.masterEntity instanceof GhostEntity ghostMaster && (ghostMaster.isDeadlocked() || ghostMaster.isSuppressed())) {
            this.method_18800(0.0, 0.0, 0.0);
            this.method_5942().method_6340();
         }

         if (!this.masterEntity.method_5805() || this.masterEntity.method_31481()) {
            this.method_5768();
         }
      } else if (this.masterUuid != null) {
         this.masterNotFoundCount++;
         LOGGER.warn("鬼奴 {} 第 {} 次找不到主人，主人UUID: {}", this.method_5667(), this.masterNotFoundCount, this.masterUuid);
         if (this.masterNotFoundCount >= 5) {
            LOGGER.error("鬼奴 {} 连续5次找不到主人，自动死亡", this.method_5667());
            this.method_5768();
         }
      } else {
         this.masterNotFoundCount = 0;
      }
   }

   private void detectAndLogAttackBehavior() {
      LivingEntity currentTarget = this.method_5968();
      if (currentTarget != this.lastTarget) {
         if (currentTarget != null) {
            LOGGER.debug("鬼奴攻击目标变化: {} -> {}", this.method_5477().getString(), currentTarget.method_5477().getString());
            if (currentTarget == this) {
               LOGGER.warn("鬼奴攻击异常: {} 正在攻击自己，已自动取消目标", this.method_5477().getString());
               this.method_5980(null);
               return;
            }

            if (currentTarget instanceof GhostSlaveEntity targetSlave
               && this.masterEntity != null
               && targetSlave.getMaster() != null
               && this.masterEntity.equals(targetSlave.getMaster())) {
               LOGGER.warn("鬼奴攻击异常: {} 正在攻击同主人鬼奴 {}，已自动取消目标", this.method_5477().getString(), currentTarget.method_5477().getString());
               this.method_5980(null);
               return;
            }
         } else {
            LOGGER.debug("鬼奴停止攻击: {} 目标: {}", this.method_5477().getString(), this.lastTarget.method_5477().getString());
         }

         this.lastTarget = currentTarget;
      }
   }

   public void setMaster(Entity master) {
      this.masterUuid = master.method_5667();
      this.masterEntity = master;
   }

   @Nullable
   public Entity getMaster() {
      return this.masterEntity;
   }

   public void dieWithMaster() {
      if (this.method_5805()) {
         LOGGER.debug("鬼奴 {} 随主人一起死亡", this.method_5667());
         this.method_5768();
      }
   }

   public boolean isVisible() {
      return this.masterEntity instanceof GhostEntity ghostMaster ? ghostMaster.isVisible() : true;
   }

   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      if (this.masterUuid != null) {
         nbt.method_25927("MasterUuid", this.masterUuid);
      }
   }

   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_25928("MasterUuid")) {
         this.masterUuid = nbt.method_25926("MasterUuid");
         this.restoreMasterEntity();
      }
   }

   public boolean method_5809() {
      return super.method_5809();
   }

   public void method_5639(int seconds) {
      super.method_5639(seconds);
   }

   public boolean method_6086() {
      return false;
   }

   public boolean method_6049(StatusEffectInstance effect) {
      return true;
   }

   public static GhostSlaveEntity createWithMaster(World world, GhostEntity master) {
      GhostSlaveEntity slave = new GhostSlaveEntity(ModEntities.GHOST_SLAVE, world);
      slave.setMaster(master);
      slave.method_5808(
         master.method_23317() + (world.field_9229.method_43058() - 0.5) * 3.0,
         master.method_23318(),
         master.method_23321() + (world.field_9229.method_43058() - 0.5) * 3.0,
         world.field_9229.method_43057() * 360.0F,
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
         this.method_6265(EnumSet.of(Control.field_18405));
      }

      public boolean method_6264() {
         Entity owner = this.ghostSlave.getMaster();
         if (owner != null && owner.method_5805()) {
            if (owner instanceof PlayerEntity player && player.method_7325()) {
               return false;
            } else {
               double distance = this.ghostSlave.method_5858(owner);
               return distance > this.minDistance * this.minDistance;
            }
         } else {
            return false;
         }
      }

      public boolean method_6266() {
         Entity owner = this.ghostSlave.getMaster();
         if (owner != null && owner.method_5805()) {
            if (owner instanceof PlayerEntity player && player.method_7325()) {
               return false;
            } else {
               double distance = this.ghostSlave.method_5858(owner);
               return distance > this.minDistance * this.minDistance && distance < this.maxDistance * this.maxDistance;
            }
         } else {
            return false;
         }
      }

      public void method_6269() {
         this.updateCountdownTicks = 0;
      }

      public void method_6270() {
         this.ghostSlave.method_5942().method_6340();
      }

      public void method_6268() {
         Entity owner = this.ghostSlave.getMaster();
         if (owner != null && owner.method_5805()) {
            this.ghostSlave.method_5988().method_6226(owner, 10.0F, this.ghostSlave.method_5978());
            if (--this.updateCountdownTicks <= 0) {
               this.updateCountdownTicks = 10;
               double squaredDistance = this.ghostSlave.method_5858(owner);
               double minSquaredDistance = this.minDistance * this.minDistance;
               double maxSquaredDistance = this.maxDistance * this.maxDistance;
               if (squaredDistance <= minSquaredDistance) {
                  this.ghostSlave.method_5942().method_6340();
               } else if (squaredDistance <= maxSquaredDistance) {
                  this.ghostSlave.method_5942().method_6335(owner, this.speed);
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
               double x = owner.method_23317() + (this.ghostSlave.method_6051().method_43058() - 0.5) * 10.0;
               double y = owner.method_23318() + this.ghostSlave.method_6051().method_43058() * 3.0;
               double z = owner.method_23321() + (this.ghostSlave.method_6051().method_43058() - 0.5) * 10.0;
               if (this.ghostSlave.method_6082(x, y, z, true)) {
                  return;
               }
            }
         }
      }
   }
}
