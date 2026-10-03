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
   protected void method_5959() {
      super.method_5959();
      if (this.owner != null) {
         this.field_6201
            .method_6277(
               2, new GhostChildEntity.CustomFollowOwnerGoal(this, this.followSpeed, this.followStopDistance, this.followStartDistance, this.allowTeleport)
            );
         this.field_6201.method_6277(1, new MeleeAttackGoal(this, 1.8, false));
         final GhostChildEntity ghostChild = this;
         this.field_6185
            .method_6277(
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
                        } else if (entity.method_6052() == ghostChild) {
                           return false;
                        } else {
                           LivingEntity playerTarget = GhostChildEntity.this.owner.method_6052();
                           if (playerTarget != null && entity == playerTarget) {
                              return true;
                           } else if (entity instanceof GhostChildEntity otherGhostChild
                              && otherGhostChild.getOwner() != null
                              && GhostChildEntity.this.owner != null
                              && otherGhostChild.getOwner().method_5667().equals(GhostChildEntity.this.owner.method_5667())) {
                              return false;
                           } else if (entity instanceof PlayerGhostEntity playerGhost
                              && playerGhost.isServantMode()
                              && playerGhost.getMasterUuid() != null
                              && playerGhost.getMasterUuid().equals(GhostChildEntity.this.owner.method_5667())) {
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
            this.field_6201.method_35113(goal -> true);
            this.field_6185.method_35113(goal -> true);
            this.method_5959();
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public UUID getMasterUuid() {
      return this.owner != null ? this.owner.method_5667() : null;
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
         this.field_6201.method_35113(goal -> true);
         this.field_6185.method_35113(goal -> true);
         this.method_5959();
      }
   }

   public float getFollowStartDistance() {
      return this.followStartDistance;
   }

   public void setFollowStartDistance(float followStartDistance) {
      this.followStartDistance = followStartDistance;
      if (this.owner != null) {
         this.field_6201.method_35113(goal -> true);
         this.field_6185.method_35113(goal -> true);
         this.method_5959();
      }
   }

   public double getFollowSpeed() {
      return this.followSpeed;
   }

   public void setFollowSpeed(double followSpeed) {
      this.followSpeed = followSpeed;
      if (this.owner != null) {
         this.field_6201.method_35113(goal -> true);
         this.field_6185.method_35113(goal -> true);
         this.method_5959();
      }
   }

   public boolean isAllowTeleport() {
      return this.allowTeleport;
   }

   public void setAllowTeleport(boolean allowTeleport) {
      this.allowTeleport = allowTeleport;
      if (this.owner != null) {
         this.field_6201.method_35113(goal -> true);
         this.field_6185.method_35113(goal -> true);
         this.method_5959();
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
      if (this.hasCoffinNail() && !this.method_37908().field_9236) {
         ItemStack coffinNail = this.getCoffinNail();
         if (!coffinNail.method_7960()) {
            ItemEntity itemEntity = new ItemEntity(this.method_37908(), this.method_23317(), this.method_23318(), this.method_23321(), coffinNail);
            this.method_37908().method_8649(itemEntity);
            this.setCoffinNail(ItemStack.field_8037);
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
         && this.getOwner().method_5667().equals(playerGhost.getMasterUuid())) {
         return false;
      } else {
         return ghost instanceof GhostChildEntity ghostChild && ghostChild.getOwner() != null && this.getOwner() != null
            ? !this.getOwner().method_5667().equals(ghostChild.getOwner().method_5667())
            : true;
      }
   }

   @Override
   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      if (this.owner != null) {
         nbt.method_25927("owner", this.owner.method_5667());
      }
   }

   @Override
   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
   }

   @Override
   public void method_5980(@Nullable LivingEntity target) {
      if (!(this.getOwner() != null && target instanceof PlayerEntity player) || !player.method_5667().equals(this.getOwner().method_5667())) {
         super.method_5980(target);
      }
   }

   @Override
   public boolean method_6121(Entity target) {
      return this.getOwner() != null && target instanceof PlayerEntity player && player.method_5667().equals(this.getOwner().method_5667())
         ? false
         : super.method_6121(target);
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if ((this.isDeadlocked() || this.isSuppressed()) && !this.method_37908().field_9236) {
         ServerWorld serverWorld = (ServerWorld)this.method_37908();
         Vec3d pos = this.method_19538();

         for (int i = 0; i < 5; i++) {
            double offsetX = this.method_6051().method_43058() - 0.5;
            double offsetY = (this.method_6051().method_43058() - 0.5) * 2.0 + 1.0;
            double offsetZ = this.method_6051().method_43058() - 0.5;
            serverWorld.method_14199(
               ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, 0.0, 0.1, 0.0, 0.0
            );
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
         this.method_6265(EnumSet.of(Control.field_18405, Control.field_18406));
      }

      public boolean method_6264() {
         PlayerEntity owner = this.ghostChild.getOwner();
         if (owner == null) {
            return false;
         }

         if (!this.ghostChild.isSummoned()) {
            return false;
         }

         if (owner.method_7325()) {
            return false;
         }

         if (this.ghostChild.method_5858(owner) < this.stopDistance) {
            return false;
         }

         this.owner = owner;
         return true;
      }

      public boolean method_6266() {
         return this.owner.method_5805() && this.ghostChild.isSummoned() ? !(this.ghostChild.method_5858(this.owner) > this.startDistance) : false;
      }

      public void method_6269() {
         this.timeToRecalcPath = 0;
      }

      public void method_6270() {
         this.owner = null;
         this.ghostChild.method_5942().method_6340();
      }

      public void method_6268() {
         this.ghostChild.method_5988().method_6226(this.owner, 10.0F, this.ghostChild.method_5978());
         if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.ghostChild.method_5934() && !this.ghostChild.method_5782()) {
               double distance = this.ghostChild.method_5858(this.owner);
               if (distance >= 144.0) {
                  if (this.canTeleportToOwner) {
                     this.teleportToOwner();
                  } else {
                     this.ghostChild.method_5942().method_6335(this.owner, this.speed);
                  }
               } else {
                  this.ghostChild.method_5942().method_6335(this.owner, this.speed);
               }
            }
         }
      }

      private void teleportToOwner() {
         if (this.canTeleportToOwner) {
            Vec3d vec3d = this.owner.method_19538();

            for (int i = 0; i < 10; i++) {
               double d = this.ghostChild.method_6051().method_43058() * 2.0 - 1.0;
               double e = this.ghostChild.method_6051().method_43058() * 2.0 - 1.0;
               double f = this.ghostChild.method_6051().method_43058() * 2.0 - 1.0;
               double g = this.owner.method_17681() + 1.0;
               double h = vec3d.field_1352 + d * g;
               double j = vec3d.field_1351 + e;
               double k = vec3d.field_1350 + f * g;
               this.ghostChild.method_20620(h, j, k);
               if (!this.ghostChild.method_5942().method_6357()) {
                  break;
               }
            }
         }
      }
   }
}
