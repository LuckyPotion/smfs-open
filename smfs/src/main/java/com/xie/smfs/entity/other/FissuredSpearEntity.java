package com.xie.smfs.entity.other;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.item.BoundSpearItem;
import com.xie.smfs.item.FissuredSpearPurpleItem;
import com.xie.smfs.item.SpiritWeapon;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FissuredSpearEntity extends PersistentProjectileEntity implements GeoAnimatable {
   private static final int STUCK_RETURN_TICKS = 60;
   private static final double RETURN_PICKUP_DISTANCE_SQUARED = 6.25;
   private static final double RETURN_PICKUP_BOX_EXPAND = 1.25;
   private static final double RETURN_MAX_SPEED = 2.0;
   private static final double RETURN_MIN_SPEED = 0.55;
   private static final double RETURN_SPEED_FACTOR = 0.35;
   private static final double TARGET_STICK_MIN_HEIGHT_RATIO = 0.16;
   private static final double TARGET_STICK_MAX_HEIGHT_RATIO = 0.92;
   private static final double TARGET_STICK_RANDOM_HEIGHT_RATIO = 0.18;
   private static final double TARGET_STICK_LATERAL_RATIO = 0.7;
   private static final double TARGET_STICK_SURFACE_RATIO = 0.7;
   private static final TrackedData<Boolean> STUCK = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> STUCK_ON_BLOCK = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Boolean> RETURNING = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   private static final TrackedData<Float> STUCK_YAW = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Float> STUCK_PITCH = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Integer> STUCK_TARGET_ID = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.INTEGER);
   private static final TrackedData<Float> STUCK_TARGET_OFFSET_X = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Float> STUCK_TARGET_OFFSET_Y = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Float> STUCK_TARGET_OFFSET_Z = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private static final TrackedData<Float> STUCK_TARGET_BASE_YAW = DataTracker.registerData(FissuredSpearEntity.class, TrackedDataHandlerRegistry.FLOAT);
   private ItemStack originalStack = ItemStack.EMPTY;
   private String throwMode = "suppress";
   private String wishPreset = "";
   private int stuckTicks = 0;
   private int returnTimer = 0;
   private LivingEntity stuckTarget;
   private Vec3d lastCollisionStart = Vec3d.ZERO;
   private Vec3d lastCollisionEnd = Vec3d.ZERO;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public FissuredSpearEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
      super(entityType, world);
   }

   public FissuredSpearEntity(EntityType<? extends PersistentProjectileEntity> entityType, LivingEntity owner, World world) {
      super(entityType, owner, world);
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(STUCK, false);
      this.dataTracker.startTracking(STUCK_ON_BLOCK, false);
      this.dataTracker.startTracking(RETURNING, false);
      this.dataTracker.startTracking(STUCK_YAW, 0.0F);
      this.dataTracker.startTracking(STUCK_PITCH, 0.0F);
      this.dataTracker.startTracking(STUCK_TARGET_ID, -1);
      this.dataTracker.startTracking(STUCK_TARGET_OFFSET_X, 0.0F);
      this.dataTracker.startTracking(STUCK_TARGET_OFFSET_Y, 0.0F);
      this.dataTracker.startTracking(STUCK_TARGET_OFFSET_Z, 0.0F);
      this.dataTracker.startTracking(STUCK_TARGET_BASE_YAW, 0.0F);
   }

   public void setOriginalStack(ItemStack stack) {
      this.originalStack = stack.copy();
      this.syncThrowSettingsFromStack();
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      if (!this.getWorld().isClient && !this.isStuck() && !this.isReturning()) {
         if (entityHitResult.getEntity() instanceof LivingEntity target) {
            if (target != this.getOwner()) {
               if (this.isSuppressMode()) {
                  this.stickToTarget(target, this.resolveEntityHitPos(target, entityHitResult.getPos()));
                  this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
               } else {
                  this.attackTarget(target);
                  this.startReturning();
               }
            }
         }
      }
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      if (!this.getWorld().isClient && !this.isReturning()) {
         if (this.isSuppressMode()) {
            this.stickToBlock();
         } else {
            this.startReturning();
         }
      }
   }

   public void tick() {
      if (!this.getWorld().isClient && !this.isStuck() && !this.isReturning()) {
         this.updateWishTrackingVelocity();
      }

      if (!this.isReturning() || this.prepareReturningTick()) {
         if (this.isStuck()) {
            this.prepareStuckTick();
         }

         super.tick();
         if (this.isStuck()) {
            this.finishStuckTick();
         }

         if (!this.getWorld().isClient) {
            if (this.isReturning()) {
               if (this.getOwner() instanceof PlayerEntity player) {
                  this.tryReturnToOwner(player);
               }
            } else {
               if (this.isStuck()) {
                  this.tickStuckState();
               }
            }
         }
      }
   }

   private void stickToBlock() {
      this.storeStuckRotation();
      this.setStuck(true);
      this.setStuckOnBlock(true);
      this.clearStuckTarget();
      this.stuckTicks = 0;
      this.shake = 0;
      this.setNoGravity(true);
      this.setNoClip(false);
      this.setVelocity(Vec3d.ZERO);
   }

   private void stickToTarget(LivingEntity target, Vec3d hitPos) {
      Vec3d stuckPos = this.computeTargetStuckPos(target, hitPos);
      this.storeStuckRotation();
      this.stuckTarget = target;
      this.setTargetOffset(target, stuckPos);
      this.setPosition(stuckPos.x, stuckPos.y, stuckPos.z);
      this.setStuck(true);
      this.setStuckOnBlock(false);
      this.stuckTicks = 0;
      this.inGround = false;
      this.inGroundTime = 0;
      this.shake = 0;
      this.setNoGravity(true);
      this.setNoClip(true);
      this.setVelocity(Vec3d.ZERO);
   }

   private void attackTarget(LivingEntity target) {
      float playerSpiritDamage = 0.0F;
      if (this.getOwner() instanceof PlayerEntity player) {
         NbtCompound attr = PlayerEvents.getSpiritAttributes(player);
         float baseSpirit = attr.contains("spiritDamage") ? (float)attr.getDouble("spiritDamage") : 0.0F;
         float tempSpirit = attr.contains("tempSpiritDamage") ? (float)attr.getDouble("tempSpiritDamage") : 0.0F;
         float tempMultiplier = attr.contains("tempSpiritDamageMultiplier") ? (float)attr.getDouble("tempSpiritDamageMultiplier") : 1.0F;
         playerSpiritDamage = baseSpirit * tempMultiplier + tempSpirit;
      }

      float weaponBonus = 150.0F;
      float multiplier = 1.2F;
      SpiritWeapon spiritWeapon = null;
      if (this.originalStack.getItem() instanceof SpiritWeapon weapon) {
         weaponBonus = weapon.getSpiritDamageBonus();
         multiplier = weapon.getSpiritDamageMultiplier();
         spiritWeapon = weapon;
      }

      float damage = weaponBonus * multiplier + playerSpiritDamage;
      Entity owner = this.getOwner();
      if (owner instanceof LivingEntity livingOwner) {
         livingOwner.onAttacking(target);
      }

      FissuredSpearPurpleItem.spawnSpiritAttackParticles(target);
      this.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 1.0F, 1.0F);
      target.damage(this.getDamageSources().arrow(this, this.getOwner()), damage);
      if (!this.getWorld().isClient && owner instanceof LivingEntity livingOwner) {
         EnchantmentHelper.onUserDamaged(target, livingOwner);
         EnchantmentHelper.onTargetDamaged(livingOwner, target);
         if (spiritWeapon != null) {
            spiritWeapon.onSpiritWeaponAttack(this.originalStack, target, livingOwner);
         }
      }
   }

   private void updateWishTrackingVelocity() {
      if (this.throwMode.equals("wish") && this.wishPreset.equals("tracking") && this.getOwner() != null) {
         double trackingRange = 15.0;
         Box searchBox = new Box(
            this.getX() - trackingRange,
            this.getY() - trackingRange,
            this.getZ() - trackingRange,
            this.getX() + trackingRange,
            this.getY() + trackingRange,
            this.getZ() + trackingRange
         );
         List<LivingEntity> nearbyEntities = this.getWorld()
            .getEntitiesByClass(LivingEntity.class, searchBox, entity -> entity != this.getOwner() && entity.isAlive());
         if (!nearbyEntities.isEmpty()) {
            LivingEntity target = nearbyEntities.get(0);
            Vec3d toTarget = target.getEyePos().subtract(this.getPos());
            if (!(toTarget.lengthSquared() <= 1.0E-4)) {
               double speed = Math.min(1.2, toTarget.length() * 0.08);
               this.setVelocity(toTarget.normalize().multiply(speed));
            }
         }
      }
   }

   private boolean prepareReturningTick() {
      Entity owner = this.getOwner();
      if (owner instanceof PlayerEntity player && !owner.isRemoved() && player.isAlive()) {
         if (!this.getWorld().isClient && this.tryReturnToOwner(player)) {
            return false;
         }

         this.inGround = false;
         this.inGroundTime = 0;
         this.setNoGravity(true);
         this.setNoClip(true);
         this.updateReturnVelocity(player);
         return true;
      } else {
         if (!this.getWorld().isClient) {
            this.dropAndDiscard();
         }

         return false;
      }
   }

   private void updateReturnVelocity(PlayerEntity player) {
      Vec3d returnTarget = player.getEyePos().subtract(0.0, 0.15, 0.0);
      Vec3d toOwner = returnTarget.subtract(this.getPos());
      double distance = toOwner.length();
      if (!(distance <= 1.0E-6)) {
         double speed = Math.min(2.0, Math.max(0.55, distance * 0.35));
         speed = Math.min(speed, distance * 0.85);
         this.setVelocity(toOwner.normalize().multiply(speed));
         if (this.returnTimer == 0) {
            this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
         }

         this.returnTimer++;
      }
   }

   private boolean tryReturnToOwner(PlayerEntity player) {
      if (this.isInReturnPickupRange(player)) {
         this.returnToPlayer(player);
         return true;
      } else {
         return false;
      }
   }

   private void startReturning() {
      if (!this.isReturning()) {
         this.setStuck(false);
         this.setStuckOnBlock(false);
         this.clearStuckTarget();
         this.stuckTicks = 0;
         this.returnTimer = 0;
         this.inGround = false;
         this.inGroundTime = 0;
         this.shake = 0;
         this.setNoGravity(true);
         this.setNoClip(true);
         this.setReturning(true);
         if (this.getOwner() instanceof PlayerEntity player) {
            this.updateReturnVelocity(player);
         }
      }
   }

   private void tickStuckState() {
      this.stuckTicks++;
      LivingEntity target = this.getStuckTarget();
      if (this.isStuckOnBlock() || target != null && !target.isRemoved() && target.isAlive()) {
         if (this.isSuppressMode() && target != null && this.stuckTicks % 20 == 0) {
            target.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 40, 0));
         }

         if (this.stuckTicks >= 60) {
            this.startReturning();
         }
      } else {
         this.startReturning();
      }
   }

   private void prepareStuckTick() {
      if (!this.isStuckOnBlock()) {
         this.updateStuckTargetPosition();
         this.inGround = false;
         this.inGroundTime = 0;
         this.setNoClip(true);
      }

      this.setNoGravity(true);
      this.setVelocity(Vec3d.ZERO);
      this.restoreStuckRotation();
   }

   private void finishStuckTick() {
      if (!this.isStuckOnBlock()) {
         this.updateStuckTargetPosition();
      }

      this.setNoGravity(true);
      this.setVelocity(Vec3d.ZERO);
      this.restoreStuckRotation();
   }

   private void updateStuckTargetPosition() {
      LivingEntity target = this.getStuckTarget();
      if (target != null) {
         Vec3d offset = this.getTargetWorldOffset(target);
         this.setPosition(target.getX() + offset.x, target.getY() + offset.y, target.getZ() + offset.z);
      }
   }

   private LivingEntity getStuckTarget() {
      if (this.stuckTarget != null && !this.stuckTarget.isRemoved()) {
         return this.stuckTarget;
      } else {
         int targetId = (Integer)this.dataTracker.get(STUCK_TARGET_ID);
         if (targetId < 0) {
            return null;
         } else if (this.getWorld().getEntityById(targetId) instanceof LivingEntity living) {
            this.stuckTarget = living;
            return living;
         } else {
            return null;
         }
      }
   }

   private void setTargetOffset(LivingEntity target, Vec3d stuckPos) {
      Vec3d worldOffset = stuckPos.subtract(target.getX(), target.getY(), target.getZ());
      Vec3d localOffset = this.toTargetLocalOffset(target, worldOffset);
      this.dataTracker.set(STUCK_TARGET_ID, target.getId());
      this.dataTracker.set(STUCK_TARGET_OFFSET_X, (float)localOffset.x);
      this.dataTracker.set(STUCK_TARGET_OFFSET_Y, (float)localOffset.y);
      this.dataTracker.set(STUCK_TARGET_OFFSET_Z, (float)localOffset.z);
      this.dataTracker.set(STUCK_TARGET_BASE_YAW, target.getYaw());
   }

   private void clearStuckTarget() {
      this.stuckTarget = null;
      this.dataTracker.set(STUCK_TARGET_ID, -1);
      this.dataTracker.set(STUCK_TARGET_OFFSET_X, 0.0F);
      this.dataTracker.set(STUCK_TARGET_OFFSET_Y, 0.0F);
      this.dataTracker.set(STUCK_TARGET_OFFSET_Z, 0.0F);
      this.dataTracker.set(STUCK_TARGET_BASE_YAW, 0.0F);
   }

   private void storeStuckRotation() {
      this.dataTracker.set(STUCK_YAW, this.getYaw());
      this.dataTracker.set(STUCK_PITCH, this.getPitch());
      this.prevYaw = this.getYaw();
      this.prevPitch = this.getPitch();
   }

   private void restoreStuckRotation() {
      float yaw = this.getStuckYaw();
      float pitch = this.getStuckPitch();
      if (!this.isStuckOnBlock()) {
         LivingEntity target = this.getStuckTarget();
         if (target != null) {
            yaw += MathHelper.wrapDegrees(target.getYaw() - (Float)this.dataTracker.get(STUCK_TARGET_BASE_YAW));
         }
      }

      this.setYaw(yaw);
      this.setPitch(pitch);
      this.prevYaw = yaw;
      this.prevPitch = pitch;
   }

   private Vec3d computeTargetStuckPos(LivingEntity target, Vec3d hitPos) {
      Vec3d travelDir = this.getHorizontalTravelDirection();
      Vec3d sideDir = new Vec3d(-travelDir.z, 0.0, travelDir.x);
      double radius = Math.max(0.15, target.getWidth() * 0.5);
      double height = Math.max(0.5, target.getHeight());
      double hitHeight = hitPos.y - target.getY();
      double randomHeight = (target.getRandom().nextDouble() - 0.5) * height * 0.18;
      double stuckHeight = MathHelper.clamp(hitHeight + randomHeight, height * 0.16, height * 0.92);
      Vec3d hitOffset = hitPos.subtract(target.getX(), target.getY(), target.getZ());
      double hitLateral = hitOffset.dotProduct(sideDir);
      double randomLateral = (target.getRandom().nextDouble() - 0.5) * target.getWidth() * 0.25;
      double maxLateral = radius * 0.7;
      double lateral = MathHelper.clamp(hitLateral + randomLateral, -maxLateral, maxLateral);
      double surfaceDepth = radius * 0.7;
      double x = -travelDir.x * surfaceDepth + sideDir.x * lateral;
      double z = -travelDir.z * surfaceDepth + sideDir.z * lateral;
      return new Vec3d(target.getX() + x, target.getY() + stuckHeight, target.getZ() + z);
   }

   private Vec3d resolveEntityHitPos(LivingEntity target, Vec3d fallbackPos) {
      Box hitBox = target.getBoundingBox().expand(0.25);
      if (this.lastCollisionStart.squaredDistanceTo(this.lastCollisionEnd) > 1.0E-6) {
         Optional<Vec3d> hitPos = hitBox.raycast(this.lastCollisionStart, this.lastCollisionEnd);
         if (hitPos.isPresent()) {
            return hitPos.get();
         }
      }

      Vec3d candidate = fallbackPos;
      if (candidate.y <= target.getY() + 0.05) {
         candidate = this.getPos();
      }

      return new Vec3d(
         MathHelper.clamp(candidate.x, hitBox.minX, hitBox.maxX),
         MathHelper.clamp(candidate.y, hitBox.minY, hitBox.maxY),
         MathHelper.clamp(candidate.z, hitBox.minZ, hitBox.maxZ)
      );
   }

   private Vec3d getHorizontalTravelDirection() {
      Vec3d velocity = this.getVelocity();
      Vec3d horizontal = new Vec3d(velocity.x, 0.0, velocity.z);
      if (horizontal.lengthSquared() > 1.0E-6) {
         return horizontal.normalize();
      }

      double yaw = Math.toRadians(this.getYaw());
      return new Vec3d(-Math.sin(yaw), 0.0, Math.cos(yaw)).normalize();
   }

   private Vec3d toTargetLocalOffset(LivingEntity target, Vec3d worldOffset) {
      double yaw = Math.toRadians(target.getYaw());
      double cos = Math.cos(yaw);
      double sin = Math.sin(yaw);
      return new Vec3d(worldOffset.x * cos + worldOffset.z * sin, worldOffset.y, worldOffset.z * cos - worldOffset.x * sin);
   }

   private Vec3d getTargetWorldOffset(LivingEntity target) {
      double localX = ((Float)this.dataTracker.get(STUCK_TARGET_OFFSET_X)).floatValue();
      double localY = ((Float)this.dataTracker.get(STUCK_TARGET_OFFSET_Y)).floatValue();
      double localZ = ((Float)this.dataTracker.get(STUCK_TARGET_OFFSET_Z)).floatValue();
      double yaw = Math.toRadians(target.getYaw());
      double cos = Math.cos(yaw);
      double sin = Math.sin(yaw);
      return new Vec3d(localX * cos - localZ * sin, localY, localX * sin + localZ * cos);
   }

   private boolean isInReturnPickupRange(PlayerEntity player) {
      if (this.getBoundingBox().intersects(player.getBoundingBox().expand(1.25))) {
         return true;
      }

      Vec3d spearPos = this.getPos();
      return spearPos.squaredDistanceTo(player.getPos()) <= 6.25 || spearPos.squaredDistanceTo(player.getEyePos()) <= 6.25;
   }

   private boolean isSuppressMode() {
      return this.throwMode.equals("suppress") || this.throwMode.equals("medium");
   }

   private void setStuck(boolean stuck) {
      this.dataTracker.set(STUCK, stuck);
   }

   private void setStuckOnBlock(boolean stuckOnBlock) {
      this.dataTracker.set(STUCK_ON_BLOCK, stuckOnBlock);
   }

   private boolean isStuckOnBlock() {
      return (Boolean)this.dataTracker.get(STUCK_ON_BLOCK);
   }

   private void setReturning(boolean returning) {
      this.dataTracker.set(RETURNING, returning);
   }

   private boolean isReturning() {
      return (Boolean)this.dataTracker.get(RETURNING);
   }

   private void syncThrowSettingsFromStack() {
      if (this.originalStack.getItem() instanceof FissuredSpearPurpleItem) {
         this.throwMode = FissuredSpearPurpleItem.getThrowMode(this.originalStack);
         this.wishPreset = FissuredSpearPurpleItem.getWishPreset(this.originalStack);
      }
   }

   private void returnToPlayer(PlayerEntity player) {
      if (this.pickupType == PickupPermission.CREATIVE_ONLY && player.getAbilities().creativeMode) {
         this.discard();
      } else {
         ItemStack stack = this.getItemStackToDrop();
         if (player.getInventory().insertStack(stack)) {
            this.discard();
         } else {
            this.dropAndDiscard();
         }
      }
   }

   private void dropAndDiscard() {
      if (!this.getWorld().isClient) {
         this.dropStack(this.getItemStackToDrop(), 0.1F);
      }

      this.discard();
   }

   private ItemStack getItemStackToDrop() {
      return !this.originalStack.isEmpty() ? this.originalStack.copy() : new ItemStack(ModItems.FISSURED_SPEAR_PURPLE);
   }

   protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
      this.lastCollisionStart = currentPosition;
      this.lastCollisionEnd = nextPosition;
      return !this.isStuck() && !this.isReturning() ? super.getEntityCollision(currentPosition, nextPosition) : null;
   }

   protected SoundEvent getHitSound() {
      return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
   }

   protected float getDragInWater() {
      return 0.99F;
   }

   public boolean shouldRender(double distance) {
      return true;
   }

   protected ItemStack asItemStack() {
      return this.getItemStackToDrop();
   }

   protected boolean tryPickup(PlayerEntity player) {
      if (!this.isStuck()) {
         return false;
      } else {
         ItemStack stack = this.getItemStackToDrop();
         if (stack.getItem() instanceof BoundSpearItem boundSpear && boundSpear.isBound(stack) && !boundSpear.isOwner(stack, player)) {
            return false;
         } else if (this.pickupType == PickupPermission.CREATIVE_ONLY && player.getAbilities().creativeMode) {
            this.discard();
            return true;
         } else if (player.getInventory().insertStack(stack)) {
            this.discard();
            return true;
         } else {
            return false;
         }
      }
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (!this.originalStack.isEmpty()) {
         nbt.put("OriginalStack", this.originalStack.writeNbt(new NbtCompound()));
      }

      nbt.putString("ThrowMode", this.throwMode);
      nbt.putString("WishPreset", this.wishPreset);
      nbt.putBoolean("Stuck", this.isStuck());
      nbt.putBoolean("StuckOnBlock", this.isStuckOnBlock());
      nbt.putBoolean("Returning", this.isReturning());
      nbt.putInt("StuckTicks", this.stuckTicks);
      nbt.putFloat("StuckYaw", this.getStuckYaw());
      nbt.putFloat("StuckPitch", this.getStuckPitch());
      nbt.putFloat("StuckTargetOffsetX", (Float)this.dataTracker.get(STUCK_TARGET_OFFSET_X));
      nbt.putFloat("StuckTargetOffsetY", (Float)this.dataTracker.get(STUCK_TARGET_OFFSET_Y));
      nbt.putFloat("StuckTargetOffsetZ", (Float)this.dataTracker.get(STUCK_TARGET_OFFSET_Z));
      nbt.putFloat("StuckTargetBaseYaw", (Float)this.dataTracker.get(STUCK_TARGET_BASE_YAW));
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("OriginalStack")) {
         this.originalStack = ItemStack.fromNbt(nbt.getCompound("OriginalStack"));
         this.syncThrowSettingsFromStack();
      }

      if (nbt.contains("ThrowMode")) {
         this.throwMode = nbt.getString("ThrowMode");
      }

      if (nbt.contains("WishPreset")) {
         this.wishPreset = nbt.getString("WishPreset");
      }

      this.dataTracker.set(STUCK, nbt.getBoolean("Stuck"));
      this.dataTracker.set(STUCK_ON_BLOCK, nbt.getBoolean("StuckOnBlock"));
      this.dataTracker.set(RETURNING, nbt.getBoolean("Returning"));
      this.stuckTicks = nbt.getInt("StuckTicks");
      this.dataTracker.set(STUCK_YAW, nbt.getFloat("StuckYaw"));
      this.dataTracker.set(STUCK_PITCH, nbt.getFloat("StuckPitch"));
      if (nbt.contains("StuckTargetOffsetX")) {
         this.dataTracker.set(STUCK_TARGET_OFFSET_X, nbt.getFloat("StuckTargetOffsetX"));
         this.dataTracker.set(STUCK_TARGET_OFFSET_Y, nbt.getFloat("StuckTargetOffsetY"));
         this.dataTracker.set(STUCK_TARGET_OFFSET_Z, nbt.getFloat("StuckTargetOffsetZ"));
         this.dataTracker.set(STUCK_TARGET_BASE_YAW, nbt.getFloat("StuckTargetBaseYaw"));
      }

      if (this.isReturning()) {
         this.inGround = false;
         this.inGroundTime = 0;
         this.setNoGravity(true);
         this.setNoClip(true);
      }
   }

   public boolean isStuck() {
      return (Boolean)this.dataTracker.get(STUCK);
   }

   public ItemStack getOriginalStack() {
      return !this.originalStack.isEmpty() ? this.originalStack : new ItemStack(ModItems.FISSURED_SPEAR_PURPLE);
   }

   public float getStuckYaw() {
      return (Float)this.dataTracker.get(STUCK_YAW);
   }

   public float getStuckPitch() {
      return (Float)this.dataTracker.get(STUCK_PITCH);
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object object) {
      return 0.0;
   }
}
