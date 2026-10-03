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
      this.setNoGravity(true);
      this.setInvulnerable(true);
      this.setSilent(true);
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
      this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.5F, 0.8F);
      this.createExplosionParticles();
      Box damageBox = new Box(this.getPos().add(-3.0, -3.0, -3.0), this.getPos().add(3.0, 3.0, 3.0));

      for (PlayerEntity player : this.getWorld().getEntitiesByClass(PlayerEntity.class, damageBox, p -> !p.isSpectator() && !p.isCreative())) {
         float damage = this.explosionDamage;
         PlayerEvents.handleSpiritDamage(player, damage, damage, ModDamageSources.ghost(this.getWorld()));
         Vec3d knockback = player.getPos().subtract(this.getPos()).normalize().multiply(1.5, 1.0, 1.5);
         player.addVelocity(knockback.x, knockback.y, knockback.z);
         player.velocityModified = true;
      }

      this.discard();
   }

   private void createExplosionParticles() {
      World world = this.getWorld();
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      if (world instanceof ServerWorld serverWorld) {
         serverWorld.spawnParticles(ParticleTypes.EXPLOSION, x, y + 1.0, z, 10, 0.5, 0.5, 0.5, 0.2);
      }
   }

   public void init(double targetX, double targetY, double targetZ, double spawnX, double spawnZ) {
      this.centerX = targetX;
      this.centerZ = targetZ;
      double dx = spawnX - targetX;
      double dz = spawnZ - targetZ;
      this.orbitRadius = (float)Math.sqrt(dx * dx + dz * dz);
      this.orbitSpeed = 0.004F + this.getWorld().random.nextFloat() * 0.008F;
      this.orbitPhase = (float)Math.atan2(dz, dx);
      this.refreshPositionAndAngles(spawnX, targetY, spawnZ, 0.0F, 0.0F);
      this.initialY = targetY;
      this.amplitude = 0.3F + this.getWorld().random.nextFloat() * 0.5F;
      this.period = 12.0F + this.getWorld().random.nextFloat() * 15.0F;
      this.direction = this.getWorld().random.nextFloat() * (float) Math.PI * 2.0F;
      this.riseSpeed = 0.03F + this.getWorld().random.nextFloat() * 0.04F;
      this.targetHeight = 4.0F + this.getWorld().random.nextFloat() * 4.0F;
      this.phase = this.getWorld().random.nextFloat() * (float) Math.PI * 2.0F;
      this.reachedTarget = false;
   }

   protected void initDataTracker() {
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.phase = nbt.getFloat("Phase");
      this.amplitude = nbt.getFloat("Amplitude");
      this.period = nbt.getFloat("Period");
      this.direction = nbt.getFloat("Direction");
      this.riseSpeed = nbt.getFloat("RiseSpeed");
      this.reachedTarget = nbt.getBoolean("ReachedTarget");
      this.targetHeight = nbt.getFloat("TargetHeight");
      this.initialY = nbt.getDouble("InitialY");
      this.centerX = nbt.getDouble("CenterX");
      this.centerZ = nbt.getDouble("CenterZ");
      this.orbitRadius = nbt.getFloat("OrbitRadius");
      this.orbitSpeed = nbt.getFloat("OrbitSpeed");
      this.orbitPhase = nbt.getFloat("OrbitPhase");
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putFloat("Phase", this.phase);
      nbt.putFloat("Amplitude", this.amplitude);
      nbt.putFloat("Period", this.period);
      nbt.putFloat("Direction", this.direction);
      nbt.putFloat("RiseSpeed", this.riseSpeed);
      nbt.putBoolean("ReachedTarget", this.reachedTarget);
      nbt.putFloat("TargetHeight", this.targetHeight);
      nbt.putDouble("InitialY", this.initialY);
      nbt.putDouble("CenterX", this.centerX);
      nbt.putDouble("CenterZ", this.centerZ);
      nbt.putFloat("OrbitRadius", this.orbitRadius);
      nbt.putFloat("OrbitSpeed", this.orbitSpeed);
      nbt.putFloat("OrbitPhase", this.orbitPhase);
   }

   public void tick() {
      super.tick();
      if (!this.getWorld().isClient()) {
         if (this.isReturning) {
            double dx = this.centerX - this.getX();
            double dz = this.centerZ - this.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            this.returnSpeed += 0.008F;
            if (this.returnSpeed > 0.8F) {
               this.returnSpeed = 0.8F;
            }

            double moveX = dx / distance * this.returnSpeed;
            double moveZ = dz / distance * this.returnSpeed;
            double moveY = this.centerX > 0.0 ? -0.05F : 0.05F;
            double newX = this.getX() + moveX;
            double newY = this.getY() + moveY;
            double newZ = this.getZ() + moveZ;
            this.refreshPositionAndAngles(newX, newY, newZ, 0.0F, 0.0F);
            this.setYaw(this.getYaw() + 5.0F);
            if (distance < 2.0) {
               this.discard();
            }
         } else if (this.isTracking) {
            double dx = this.targetX - this.getX();
            double dz = this.targetZ - this.getZ();
            double dy = this.targetY - this.getY();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            this.trackingSpeed += 0.02F;
            if (this.trackingSpeed > 1.5F) {
               this.trackingSpeed = 1.5F;
            }

            double moveX = dx / distance * this.trackingSpeed;
            double moveY = dy / distance * this.trackingSpeed;
            double moveZ = dz / distance * this.trackingSpeed;
            double newX = this.getX() + moveX;
            double newY = this.getY() + moveY;
            double newZ = this.getZ() + moveZ;
            this.refreshPositionAndAngles(newX, newY, newZ, 0.0F, 0.0F);
            this.setYaw(this.getYaw() + 8.0F);
            if (newY <= this.getWorld().getTopY(Type.WORLD_SURFACE, (int)newX, (int)newZ) + 1) {
               this.explode();
            } else if (distance < 2.0) {
               this.explode();
            }
         } else {
            double currentY = this.getY();
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
               this.refreshPositionAndAngles(x, newY, z, 0.0F, 0.0F);
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
               this.refreshPositionAndAngles(newX, newY, newZ, 0.0F, 0.0F);
            }

            this.setYaw(this.getYaw() + 0.8F);
            if (this.age > 1000) {
               this.discard();
            }
         }
      }
   }

   public boolean shouldRender(double distance) {
      return distance < 10000.0;
   }

   public boolean isInvisible() {
      return false;
   }

   public boolean isInvulnerable() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      return false;
   }

   public boolean isInvulnerableTo(DamageSource damageSource) {
      return true;
   }

   public void handleStatus(byte status) {
      if (status != 35 && status != 36) {
         super.handleStatus(status);
      }
   }
}
