package com.xie.smfs.entity.other;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;

public class FloatingDirtEntity extends Entity {
   private float phase;
   private float amplitude;
   private float period;
   private float direction;
   private float riseSpeed;
   private boolean reachedTarget;
   private float targetHeight;
   private double initialY;
   private double centerX;
   private double centerZ;
   private float orbitRadius;
   private float orbitSpeed;
   private float orbitPhase;
   private boolean isReturning = false;
   private float returnSpeed = 0.15F;
   private boolean isTracking = false;
   private double targetX;
   private double targetY;
   private double targetZ;
   private float trackingSpeed = 0.4F;
   private static final float EXPLOSION_RADIUS = 3.0F;
   private float explosionDamage = 8.0F;

   public FloatingDirtEntity(EntityType<? extends FloatingDirtEntity> entityType, World world) {
      super(entityType, world);
      this.method_5875(true);
      this.method_5684(true);
      this.method_5803(true);
   }

   public void startReturning() {
      this.isReturning = true;
      this.isTracking = false;
   }

   public void setTrackingTarget(double x, double y, double z) {
      this.targetX = x;
      this.targetY = y;
      this.targetZ = z;
      this.isTracking = true;
      this.isReturning = false;
      this.trackingSpeed = 0.4F;
   }

   public void setExplosionDamage(float damage) {
      this.explosionDamage = damage;
   }

   private void explode() {
      this.method_37908().method_8396(null, this.method_24515(), SoundEvents.field_15152, SoundCategory.field_15251, 1.5F, 0.8F);
      this.createExplosionParticles();
      Box damageBox = new Box(this.method_19538().method_1031(-3.0, -3.0, -3.0), this.method_19538().method_1031(3.0, 3.0, 3.0));

      for (PlayerEntity player : this.method_37908().method_8390(PlayerEntity.class, damageBox, p -> !p.method_7325() && !p.method_7337())) {
         float damage = this.explosionDamage;
         PlayerEvents.handleSpiritDamage(player, damage, damage, ModDamageSources.ghost(this.method_37908()));
         Vec3d knockback = player.method_19538().method_1020(this.method_19538()).method_1029().method_18805(1.5, 1.0, 1.5);
         player.method_5762(knockback.field_1352, knockback.field_1351, knockback.field_1350);
         player.field_6037 = true;
      }

      this.method_31472();
   }

   private void createExplosionParticles() {
      World world = this.method_37908();
      double x = this.method_23317();
      double y = this.method_23318();
      double z = this.method_23321();
      if (world instanceof ServerWorld serverWorld) {
         serverWorld.method_14199(ParticleTypes.field_11236, x, y + 1.0, z, 10, 0.5, 0.5, 0.5, 0.2);
      }
   }

   public void init(double targetX, double targetY, double targetZ, double spawnX, double spawnZ) {
      this.centerX = targetX;
      this.centerZ = targetZ;
      double dx = spawnX - targetX;
      double dz = spawnZ - targetZ;
      this.orbitRadius = (float)Math.sqrt(dx * dx + dz * dz);
      this.orbitSpeed = 0.004F + this.method_37908().field_9229.method_43057() * 0.008F;
      this.orbitPhase = (float)Math.atan2(dz, dx);
      this.method_5808(spawnX, targetY, spawnZ, 0.0F, 0.0F);
      this.initialY = targetY;
      this.amplitude = 0.3F + this.method_37908().field_9229.method_43057() * 0.5F;
      this.period = 12.0F + this.method_37908().field_9229.method_43057() * 15.0F;
      this.direction = this.method_37908().field_9229.method_43057() * (float) Math.PI * 2.0F;
      this.riseSpeed = 0.03F + this.method_37908().field_9229.method_43057() * 0.04F;
      this.targetHeight = 4.0F + this.method_37908().field_9229.method_43057() * 4.0F;
      this.phase = this.method_37908().field_9229.method_43057() * (float) Math.PI * 2.0F;
      this.reachedTarget = false;
   }

   protected void method_5693() {
   }

   protected void method_5749(NbtCompound nbt) {
      this.phase = nbt.method_10583("Phase");
      this.amplitude = nbt.method_10583("Amplitude");
      this.period = nbt.method_10583("Period");
      this.direction = nbt.method_10583("Direction");
      this.riseSpeed = nbt.method_10583("RiseSpeed");
      this.reachedTarget = nbt.method_10577("ReachedTarget");
      this.targetHeight = nbt.method_10583("TargetHeight");
      this.initialY = nbt.method_10574("InitialY");
      this.centerX = nbt.method_10574("CenterX");
      this.centerZ = nbt.method_10574("CenterZ");
      this.orbitRadius = nbt.method_10583("OrbitRadius");
      this.orbitSpeed = nbt.method_10583("OrbitSpeed");
      this.orbitPhase = nbt.method_10583("OrbitPhase");
   }

   protected void method_5652(NbtCompound nbt) {
      nbt.method_10548("Phase", this.phase);
      nbt.method_10548("Amplitude", this.amplitude);
      nbt.method_10548("Period", this.period);
      nbt.method_10548("Direction", this.direction);
      nbt.method_10548("RiseSpeed", this.riseSpeed);
      nbt.method_10556("ReachedTarget", this.reachedTarget);
      nbt.method_10548("TargetHeight", this.targetHeight);
      nbt.method_10549("InitialY", this.initialY);
      nbt.method_10549("CenterX", this.centerX);
      nbt.method_10549("CenterZ", this.centerZ);
      nbt.method_10548("OrbitRadius", this.orbitRadius);
      nbt.method_10548("OrbitSpeed", this.orbitSpeed);
      nbt.method_10548("OrbitPhase", this.orbitPhase);
   }

   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608()) {
         if (this.isReturning) {
            double dx = this.centerX - this.method_23317();
            double dz = this.centerZ - this.method_23321();
            double distance = Math.sqrt(dx * dx + dz * dz);
            this.returnSpeed += 0.008F;
            if (this.returnSpeed > 0.8F) {
               this.returnSpeed = 0.8F;
            }

            double moveX = dx / distance * this.returnSpeed;
            double moveZ = dz / distance * this.returnSpeed;
            double moveY = this.centerX > 0.0 ? -0.05F : 0.05F;
            double newX = this.method_23317() + moveX;
            double newY = this.method_23318() + moveY;
            double newZ = this.method_23321() + moveZ;
            this.method_5808(newX, newY, newZ, 0.0F, 0.0F);
            this.method_36456(this.method_36454() + 5.0F);
            if (distance < 2.0) {
               this.method_31472();
            }
         } else if (this.isTracking) {
            double dx = this.targetX - this.method_23317();
            double dz = this.targetZ - this.method_23321();
            double dy = this.targetY - this.method_23318();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            this.trackingSpeed += 0.02F;
            if (this.trackingSpeed > 1.5F) {
               this.trackingSpeed = 1.5F;
            }

            double moveX = dx / distance * this.trackingSpeed;
            double moveY = dy / distance * this.trackingSpeed;
            double moveZ = dz / distance * this.trackingSpeed;
            double newX = this.method_23317() + moveX;
            double newY = this.method_23318() + moveY;
            double newZ = this.method_23321() + moveZ;
            this.method_5808(newX, newY, newZ, 0.0F, 0.0F);
            this.method_36456(this.method_36454() + 8.0F);
            if (newY <= this.method_37908().method_8624(Type.field_13202, (int)newX, (int)newZ) + 1) {
               this.explode();
            } else if (distance < 2.0) {
               this.explode();
            }
         } else {
            double currentY = this.method_23318();
            double targetY = this.initialY + this.targetHeight;
            if (!this.reachedTarget) {
               double newY = currentY + this.riseSpeed;
               if (newY >= targetY) {
                  newY = targetY;
                  this.reachedTarget = true;
               }

               this.orbitPhase = this.orbitPhase + this.orbitSpeed * 0.5F;
               double orbitAngle = this.orbitPhase;
               double x = this.centerX + Math.cos(orbitAngle) * this.orbitRadius;
               double z = this.centerZ + Math.sin(orbitAngle) * this.orbitRadius;
               this.method_5808(x, newY, z, 0.0F, 0.0F);
            } else {
               this.orbitPhase = this.orbitPhase + this.orbitSpeed;
               double orbitAngle = this.orbitPhase;
               double baseX = this.centerX + Math.cos(orbitAngle) * this.orbitRadius;
               double baseZ = this.centerZ + Math.sin(orbitAngle) * this.orbitRadius;
               this.phase += 0.06F;
               float swingX = (float)Math.sin(this.phase * 2.0F * Math.PI / this.period + this.direction) * this.amplitude;
               float swingZ = (float)Math.cos(this.phase * 2.0F * Math.PI / this.period + this.direction) * this.amplitude * 0.7F;
               float swingY = (float)Math.sin(this.phase * 2.5F * Math.PI / this.period) * this.amplitude * 0.4F;
               double newX = baseX + swingX * 0.03F;
               double newY = targetY + swingY * 0.03F;
               double newZ = baseZ + swingZ * 0.03F;
               this.method_5808(newX, newY, newZ, 0.0F, 0.0F);
            }

            this.method_36456(this.method_36454() + 0.8F);
            if (this.field_6012 > 1000) {
               this.method_31472();
            }
         }
      }
   }

   public boolean method_5640(double distance) {
      return distance < 10000.0;
   }

   public boolean method_5767() {
      return false;
   }

   public boolean method_5655() {
      return true;
   }

   public boolean method_5643(DamageSource source, float amount) {
      return false;
   }

   public boolean method_5679(DamageSource damageSource) {
      return true;
   }

   public void method_5711(byte status) {
      if (status != 35 && status != 36) {
         super.method_5711(status);
      }
   }
}
