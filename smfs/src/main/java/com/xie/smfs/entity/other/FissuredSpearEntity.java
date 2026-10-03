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
   private static final TrackedData<Boolean> STUCK = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13323);
   private static final TrackedData<Boolean> STUCK_ON_BLOCK = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13323);
   private static final TrackedData<Boolean> RETURNING = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13323);
   private static final TrackedData<Float> STUCK_YAW = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13320);
   private static final TrackedData<Float> STUCK_PITCH = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13320);
   private static final TrackedData<Integer> STUCK_TARGET_ID = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13327);
   private static final TrackedData<Float> STUCK_TARGET_OFFSET_X = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13320);
   private static final TrackedData<Float> STUCK_TARGET_OFFSET_Y = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13320);
   private static final TrackedData<Float> STUCK_TARGET_OFFSET_Z = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13320);
   private static final TrackedData<Float> STUCK_TARGET_BASE_YAW = DataTracker.method_12791(FissuredSpearEntity.class, TrackedDataHandlerRegistry.field_13320);
   private ItemStack originalStack = ItemStack.field_8037;
   private String throwMode = "suppress";
   private String wishPreset = "";
   private int stuckTicks = 0;
   private int returnTimer = 0;
   private LivingEntity stuckTarget;
   private Vec3d lastCollisionStart = Vec3d.field_1353;
   private Vec3d lastCollisionEnd = Vec3d.field_1353;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public FissuredSpearEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
      super(entityType, world);
   }

   public FissuredSpearEntity(EntityType<? extends PersistentProjectileEntity> entityType, LivingEntity owner, World world) {
      super(entityType, owner, world);
   }

   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(STUCK, false);
      this.field_6011.method_12784(STUCK_ON_BLOCK, false);
      this.field_6011.method_12784(RETURNING, false);
      this.field_6011.method_12784(STUCK_YAW, 0.0F);
      this.field_6011.method_12784(STUCK_PITCH, 0.0F);
      this.field_6011.method_12784(STUCK_TARGET_ID, -1);
      this.field_6011.method_12784(STUCK_TARGET_OFFSET_X, 0.0F);
      this.field_6011.method_12784(STUCK_TARGET_OFFSET_Y, 0.0F);
      this.field_6011.method_12784(STUCK_TARGET_OFFSET_Z, 0.0F);
      this.field_6011.method_12784(STUCK_TARGET_BASE_YAW, 0.0F);
   }

   public void setOriginalStack(ItemStack stack) {
      this.originalStack = stack.method_7972();
      this.syncThrowSettingsFromStack();
   }

   protected void method_7454(EntityHitResult entityHitResult) {
      if (!this.method_37908().field_9236 && !this.isStuck() && !this.isReturning()) {
         if (entityHitResult.method_17782() instanceof LivingEntity target) {
            if (target != this.method_24921()) {
               if (this.isSuppressMode()) {
                  this.stickToTarget(target, this.resolveEntityHitPos(target, entityHitResult.method_17784()));
                  this.method_37908()
                     .method_43128(
                        null,
                        target.method_23317(),
                        target.method_23318(),
                        target.method_23321(),
                        SoundEvents.field_14931,
                        SoundCategory.field_15254,
                        1.0F,
                        1.0F
                     );
               } else {
                  this.attackTarget(target);
                  this.startReturning();
               }
            }
         }
      }
   }

   protected void method_24920(BlockHitResult blockHitResult) {
      super.method_24920(blockHitResult);
      if (!this.method_37908().field_9236 && !this.isReturning()) {
         if (this.isSuppressMode()) {
            this.stickToBlock();
         } else {
            this.startReturning();
         }
      }
   }

   public void method_5773() {
      if (!this.method_37908().field_9236 && !this.isStuck() && !this.isReturning()) {
         this.updateWishTrackingVelocity();
      }

      if (!this.isReturning() || this.prepareReturningTick()) {
         if (this.isStuck()) {
            this.prepareStuckTick();
         }

         super.method_5773();
         if (this.isStuck()) {
            this.finishStuckTick();
         }

         if (!this.method_37908().field_9236) {
            if (this.isReturning()) {
               if (this.method_24921() instanceof PlayerEntity player) {
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
      this.field_7574 = 0;
      this.method_5875(true);
      this.method_7433(false);
      this.method_18799(Vec3d.field_1353);
   }

   private void stickToTarget(LivingEntity target, Vec3d hitPos) {
      Vec3d stuckPos = this.computeTargetStuckPos(target, hitPos);
      this.storeStuckRotation();
      this.stuckTarget = target;
      this.setTargetOffset(target, stuckPos);
      this.method_5814(stuckPos.field_1352, stuckPos.field_1351, stuckPos.field_1350);
      this.setStuck(true);
      this.setStuckOnBlock(false);
      this.stuckTicks = 0;
      this.field_7588 = false;
      this.field_7576 = 0;
      this.field_7574 = 0;
      this.method_5875(true);
      this.method_7433(true);
      this.method_18799(Vec3d.field_1353);
   }

   private void attackTarget(LivingEntity target) {
      float playerSpiritDamage = 0.0F;
      if (this.method_24921() instanceof PlayerEntity player) {
         NbtCompound attr = PlayerEvents.getSpiritAttributes(player);
         float baseSpirit = attr.method_10545("spiritDamage") ? (float)attr.method_10574("spiritDamage") : 0.0F;
         float tempSpirit = attr.method_10545("tempSpiritDamage") ? (float)attr.method_10574("tempSpiritDamage") : 0.0F;
         float tempMultiplier = attr.method_10545("tempSpiritDamageMultiplier") ? (float)attr.method_10574("tempSpiritDamageMultiplier") : 1.0F;
         playerSpiritDamage = baseSpirit * tempMultiplier + tempSpirit;
      }

      float weaponBonus = 150.0F;
      float multiplier = 1.2F;
      SpiritWeapon spiritWeapon = null;
      if (this.originalStack.method_7909() instanceof SpiritWeapon weapon) {
         weaponBonus = weapon.getSpiritDamageBonus();
         multiplier = weapon.getSpiritDamageMultiplier();
         spiritWeapon = weapon;
      }

      float damage = weaponBonus * multiplier + playerSpiritDamage;
      Entity owner = this.method_24921();
      if (owner instanceof LivingEntity livingOwner) {
         livingOwner.method_6114(target);
      }

      FissuredSpearPurpleItem.spawnSpiritAttackParticles(target);
      this.method_37908()
         .method_43128(
            null, target.method_23317(), target.method_23318(), target.method_23321(), SoundEvents.field_14858, SoundCategory.field_15251, 1.0F, 1.0F
         );
      target.method_5643(this.method_48923().method_48803(this, this.method_24921()), damage);
      if (!this.method_37908().field_9236 && owner instanceof LivingEntity livingOwner) {
         EnchantmentHelper.method_8210(target, livingOwner);
         EnchantmentHelper.method_8213(livingOwner, target);
         if (spiritWeapon != null) {
            spiritWeapon.onSpiritWeaponAttack(this.originalStack, target, livingOwner);
         }
      }
   }

   private void updateWishTrackingVelocity() {
      if (this.throwMode.equals("wish") && this.wishPreset.equals("tracking") && this.method_24921() != null) {
         double trackingRange = 15.0;
         Box searchBox = new Box(
            this.method_23317() - trackingRange,
            this.method_23318() - trackingRange,
            this.method_23321() - trackingRange,
            this.method_23317() + trackingRange,
            this.method_23318() + trackingRange,
            this.method_23321() + trackingRange
         );
         List<LivingEntity> nearbyEntities = this.method_37908()
            .method_8390(LivingEntity.class, searchBox, entity -> entity != this.method_24921() && entity.method_5805());
         if (!nearbyEntities.isEmpty()) {
            LivingEntity target = nearbyEntities.get(0);
            Vec3d toTarget = target.method_33571().method_1020(this.method_19538());
            if (!(toTarget.method_1027() <= 1.0E-4)) {
               double speed = Math.min(1.2, toTarget.method_1033() * 0.08);
               this.method_18799(toTarget.method_1029().method_1021(speed));
            }
         }
      }
   }

   private boolean prepareReturningTick() {
      Entity owner = this.method_24921();
      if (owner instanceof PlayerEntity player && !owner.method_31481() && player.method_5805()) {
         if (!this.method_37908().field_9236 && this.tryReturnToOwner(player)) {
            return false;
         }

         this.field_7588 = false;
         this.field_7576 = 0;
         this.method_5875(true);
         this.method_7433(true);
         this.updateReturnVelocity(player);
         return true;
      } else {
         if (!this.method_37908().field_9236) {
            this.dropAndDiscard();
         }

         return false;
      }
   }

   private void updateReturnVelocity(PlayerEntity player) {
      Vec3d returnTarget = player.method_33571().method_1023(0.0, 0.15, 0.0);
      Vec3d toOwner = returnTarget.method_1020(this.method_19538());
      double distance = toOwner.method_1033();
      if (!(distance <= 1.0E-6)) {
         double speed = Math.min(2.0, Math.max(0.55, distance * 0.35));
         speed = Math.min(speed, distance * 0.85);
         this.method_18799(toOwner.method_1029().method_1021(speed));
         if (this.returnTimer == 0) {
            this.method_5783(SoundEvents.field_14698, 10.0F, 1.0F);
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
         this.field_7588 = false;
         this.field_7576 = 0;
         this.field_7574 = 0;
         this.method_5875(true);
         this.method_7433(true);
         this.setReturning(true);
         if (this.method_24921() instanceof PlayerEntity player) {
            this.updateReturnVelocity(player);
         }
      }
   }

   private void tickStuckState() {
      this.stuckTicks++;
      LivingEntity target = this.getStuckTarget();
      if (this.isStuckOnBlock() || target != null && !target.method_31481() && target.method_5805()) {
         if (this.isSuppressMode() && target != null && this.stuckTicks % 20 == 0) {
            target.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 40, 0));
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
         this.field_7588 = false;
         this.field_7576 = 0;
         this.method_7433(true);
      }

      this.method_5875(true);
      this.method_18799(Vec3d.field_1353);
      this.restoreStuckRotation();
   }

   private void finishStuckTick() {
      if (!this.isStuckOnBlock()) {
         this.updateStuckTargetPosition();
      }

      this.method_5875(true);
      this.method_18799(Vec3d.field_1353);
      this.restoreStuckRotation();
   }

   private void updateStuckTargetPosition() {
      LivingEntity target = this.getStuckTarget();
      if (target != null) {
         Vec3d offset = this.getTargetWorldOffset(target);
         this.method_5814(target.method_23317() + offset.field_1352, target.method_23318() + offset.field_1351, target.method_23321() + offset.field_1350);
      }
   }

   private LivingEntity getStuckTarget() {
      if (this.stuckTarget != null && !this.stuckTarget.method_31481()) {
         return this.stuckTarget;
      } else {
         int targetId = (Integer)this.field_6011.method_12789(STUCK_TARGET_ID);
         if (targetId < 0) {
            return null;
         } else if (this.method_37908().method_8469(targetId) instanceof LivingEntity living) {
            this.stuckTarget = living;
            return living;
         } else {
            return null;
         }
      }
   }

   private void setTargetOffset(LivingEntity target, Vec3d stuckPos) {
      Vec3d worldOffset = stuckPos.method_1023(target.method_23317(), target.method_23318(), target.method_23321());
      Vec3d localOffset = this.toTargetLocalOffset(target, worldOffset);
      this.field_6011.method_12778(STUCK_TARGET_ID, target.method_5628());
      this.field_6011.method_12778(STUCK_TARGET_OFFSET_X, (float)localOffset.field_1352);
      this.field_6011.method_12778(STUCK_TARGET_OFFSET_Y, (float)localOffset.field_1351);
      this.field_6011.method_12778(STUCK_TARGET_OFFSET_Z, (float)localOffset.field_1350);
      this.field_6011.method_12778(STUCK_TARGET_BASE_YAW, target.method_36454());
   }

   private void clearStuckTarget() {
      this.stuckTarget = null;
      this.field_6011.method_12778(STUCK_TARGET_ID, -1);
      this.field_6011.method_12778(STUCK_TARGET_OFFSET_X, 0.0F);
      this.field_6011.method_12778(STUCK_TARGET_OFFSET_Y, 0.0F);
      this.field_6011.method_12778(STUCK_TARGET_OFFSET_Z, 0.0F);
      this.field_6011.method_12778(STUCK_TARGET_BASE_YAW, 0.0F);
   }

   private void storeStuckRotation() {
      this.field_6011.method_12778(STUCK_YAW, this.method_36454());
      this.field_6011.method_12778(STUCK_PITCH, this.method_36455());
      this.field_5982 = this.method_36454();
      this.field_6004 = this.method_36455();
   }

   private void restoreStuckRotation() {
      float yaw = this.getStuckYaw();
      float pitch = this.getStuckPitch();
      if (!this.isStuckOnBlock()) {
         LivingEntity target = this.getStuckTarget();
         if (target != null) {
            yaw += MathHelper.method_15393(target.method_36454() - (Float)this.field_6011.method_12789(STUCK_TARGET_BASE_YAW));
         }
      }

      this.method_36456(yaw);
      this.method_36457(pitch);
      this.field_5982 = yaw;
      this.field_6004 = pitch;
   }

   private Vec3d computeTargetStuckPos(LivingEntity target, Vec3d hitPos) {
      Vec3d travelDir = this.getHorizontalTravelDirection();
      Vec3d sideDir = new Vec3d(-travelDir.field_1350, 0.0, travelDir.field_1352);
      double radius = Math.max(0.15, target.method_17681() * 0.5);
      double height = Math.max(0.5, target.method_17682());
      double hitHeight = hitPos.field_1351 - target.method_23318();
      double randomHeight = (target.method_6051().method_43058() - 0.5) * height * 0.18;
      double stuckHeight = MathHelper.method_15350(hitHeight + randomHeight, height * 0.16, height * 0.92);
      Vec3d hitOffset = hitPos.method_1023(target.method_23317(), target.method_23318(), target.method_23321());
      double hitLateral = hitOffset.method_1026(sideDir);
      double randomLateral = (target.method_6051().method_43058() - 0.5) * target.method_17681() * 0.25;
      double maxLateral = radius * 0.7;
      double lateral = MathHelper.method_15350(hitLateral + randomLateral, -maxLateral, maxLateral);
      double surfaceDepth = radius * 0.7;
      double x = -travelDir.field_1352 * surfaceDepth + sideDir.field_1352 * lateral;
      double z = -travelDir.field_1350 * surfaceDepth + sideDir.field_1350 * lateral;
      return new Vec3d(target.method_23317() + x, target.method_23318() + stuckHeight, target.method_23321() + z);
   }

   private Vec3d resolveEntityHitPos(LivingEntity target, Vec3d fallbackPos) {
      Box hitBox = target.method_5829().method_1014(0.25);
      if (this.lastCollisionStart.method_1025(this.lastCollisionEnd) > 1.0E-6) {
         Optional<Vec3d> hitPos = hitBox.method_992(this.lastCollisionStart, this.lastCollisionEnd);
         if (hitPos.isPresent()) {
            return hitPos.get();
         }
      }

      Vec3d candidate = fallbackPos;
      if (candidate.field_1351 <= target.method_23318() + 0.05) {
         candidate = this.method_19538();
      }

      return new Vec3d(
         MathHelper.method_15350(candidate.field_1352, hitBox.field_1323, hitBox.field_1320),
         MathHelper.method_15350(candidate.field_1351, hitBox.field_1322, hitBox.field_1325),
         MathHelper.method_15350(candidate.field_1350, hitBox.field_1321, hitBox.field_1324)
      );
   }

   private Vec3d getHorizontalTravelDirection() {
      Vec3d velocity = this.method_18798();
      Vec3d horizontal = new Vec3d(velocity.field_1352, 0.0, velocity.field_1350);
      if (horizontal.method_1027() > 1.0E-6) {
         return horizontal.method_1029();
      }

      double yaw = Math.toRadians(this.method_36454());
      return new Vec3d(-Math.sin(yaw), 0.0, Math.cos(yaw)).method_1029();
   }

   private Vec3d toTargetLocalOffset(LivingEntity target, Vec3d worldOffset) {
      double yaw = Math.toRadians(target.method_36454());
      double cos = Math.cos(yaw);
      double sin = Math.sin(yaw);
      return new Vec3d(
         worldOffset.field_1352 * cos + worldOffset.field_1350 * sin, worldOffset.field_1351, worldOffset.field_1350 * cos - worldOffset.field_1352 * sin
      );
   }

   private Vec3d getTargetWorldOffset(LivingEntity target) {
      double localX = ((Float)this.field_6011.method_12789(STUCK_TARGET_OFFSET_X)).floatValue();
      double localY = ((Float)this.field_6011.method_12789(STUCK_TARGET_OFFSET_Y)).floatValue();
      double localZ = ((Float)this.field_6011.method_12789(STUCK_TARGET_OFFSET_Z)).floatValue();
      double yaw = Math.toRadians(target.method_36454());
      double cos = Math.cos(yaw);
      double sin = Math.sin(yaw);
      return new Vec3d(localX * cos - localZ * sin, localY, localX * sin + localZ * cos);
   }

   private boolean isInReturnPickupRange(PlayerEntity player) {
      if (this.method_5829().method_994(player.method_5829().method_1014(1.25))) {
         return true;
      }

      Vec3d spearPos = this.method_19538();
      return spearPos.method_1025(player.method_19538()) <= 6.25 || spearPos.method_1025(player.method_33571()) <= 6.25;
   }

   private boolean isSuppressMode() {
      return this.throwMode.equals("suppress") || this.throwMode.equals("medium");
   }

   private void setStuck(boolean stuck) {
      this.field_6011.method_12778(STUCK, stuck);
   }

   private void setStuckOnBlock(boolean stuckOnBlock) {
      this.field_6011.method_12778(STUCK_ON_BLOCK, stuckOnBlock);
   }

   private boolean isStuckOnBlock() {
      return (Boolean)this.field_6011.method_12789(STUCK_ON_BLOCK);
   }

   private void setReturning(boolean returning) {
      this.field_6011.method_12778(RETURNING, returning);
   }

   private boolean isReturning() {
      return (Boolean)this.field_6011.method_12789(RETURNING);
   }

   private void syncThrowSettingsFromStack() {
      if (this.originalStack.method_7909() instanceof FissuredSpearPurpleItem) {
         this.throwMode = FissuredSpearPurpleItem.getThrowMode(this.originalStack);
         this.wishPreset = FissuredSpearPurpleItem.getWishPreset(this.originalStack);
      }
   }

   private void returnToPlayer(PlayerEntity player) {
      if (this.field_7572 == PickupPermission.field_7594 && player.method_31549().field_7477) {
         this.method_31472();
      } else {
         ItemStack stack = this.getItemStackToDrop();
         if (player.method_31548().method_7394(stack)) {
            this.method_31472();
         } else {
            this.dropAndDiscard();
         }
      }
   }

   private void dropAndDiscard() {
      if (!this.method_37908().field_9236) {
         this.method_5699(this.getItemStackToDrop(), 0.1F);
      }

      this.method_31472();
   }

   private ItemStack getItemStackToDrop() {
      return !this.originalStack.method_7960() ? this.originalStack.method_7972() : new ItemStack(ModItems.FISSURED_SPEAR_PURPLE);
   }

   protected EntityHitResult method_7434(Vec3d currentPosition, Vec3d nextPosition) {
      this.lastCollisionStart = currentPosition;
      this.lastCollisionEnd = nextPosition;
      return !this.isStuck() && !this.isReturning() ? super.method_7434(currentPosition, nextPosition) : null;
   }

   protected SoundEvent method_7440() {
      return SoundEvents.field_15104;
   }

   protected float method_7436() {
      return 0.99F;
   }

   public boolean method_5640(double distance) {
      return true;
   }

   protected ItemStack method_7445() {
      return this.getItemStackToDrop();
   }

   protected boolean method_34713(PlayerEntity player) {
      if (!this.isStuck()) {
         return false;
      } else {
         ItemStack stack = this.getItemStackToDrop();
         if (stack.method_7909() instanceof BoundSpearItem boundSpear && boundSpear.isBound(stack) && !boundSpear.isOwner(stack, player)) {
            return false;
         } else if (this.field_7572 == PickupPermission.field_7594 && player.method_31549().field_7477) {
            this.method_31472();
            return true;
         } else if (player.method_31548().method_7394(stack)) {
            this.method_31472();
            return true;
         } else {
            return false;
         }
      }
   }

   public void method_5652(NbtCompound nbt) {
      super.method_5652(nbt);
      if (!this.originalStack.method_7960()) {
         nbt.method_10566("OriginalStack", this.originalStack.method_7953(new NbtCompound()));
      }

      nbt.method_10582("ThrowMode", this.throwMode);
      nbt.method_10582("WishPreset", this.wishPreset);
      nbt.method_10556("Stuck", this.isStuck());
      nbt.method_10556("StuckOnBlock", this.isStuckOnBlock());
      nbt.method_10556("Returning", this.isReturning());
      nbt.method_10569("StuckTicks", this.stuckTicks);
      nbt.method_10548("StuckYaw", this.getStuckYaw());
      nbt.method_10548("StuckPitch", this.getStuckPitch());
      nbt.method_10548("StuckTargetOffsetX", (Float)this.field_6011.method_12789(STUCK_TARGET_OFFSET_X));
      nbt.method_10548("StuckTargetOffsetY", (Float)this.field_6011.method_12789(STUCK_TARGET_OFFSET_Y));
      nbt.method_10548("StuckTargetOffsetZ", (Float)this.field_6011.method_12789(STUCK_TARGET_OFFSET_Z));
      nbt.method_10548("StuckTargetBaseYaw", (Float)this.field_6011.method_12789(STUCK_TARGET_BASE_YAW));
   }

   public void method_5749(NbtCompound nbt) {
      super.method_5749(nbt);
      if (nbt.method_10545("OriginalStack")) {
         this.originalStack = ItemStack.method_7915(nbt.method_10562("OriginalStack"));
         this.syncThrowSettingsFromStack();
      }

      if (nbt.method_10545("ThrowMode")) {
         this.throwMode = nbt.method_10558("ThrowMode");
      }

      if (nbt.method_10545("WishPreset")) {
         this.wishPreset = nbt.method_10558("WishPreset");
      }

      this.field_6011.method_12778(STUCK, nbt.method_10577("Stuck"));
      this.field_6011.method_12778(STUCK_ON_BLOCK, nbt.method_10577("StuckOnBlock"));
      this.field_6011.method_12778(RETURNING, nbt.method_10577("Returning"));
      this.stuckTicks = nbt.method_10550("StuckTicks");
      this.field_6011.method_12778(STUCK_YAW, nbt.method_10583("StuckYaw"));
      this.field_6011.method_12778(STUCK_PITCH, nbt.method_10583("StuckPitch"));
      if (nbt.method_10545("StuckTargetOffsetX")) {
         this.field_6011.method_12778(STUCK_TARGET_OFFSET_X, nbt.method_10583("StuckTargetOffsetX"));
         this.field_6011.method_12778(STUCK_TARGET_OFFSET_Y, nbt.method_10583("StuckTargetOffsetY"));
         this.field_6011.method_12778(STUCK_TARGET_OFFSET_Z, nbt.method_10583("StuckTargetOffsetZ"));
         this.field_6011.method_12778(STUCK_TARGET_BASE_YAW, nbt.method_10583("StuckTargetBaseYaw"));
      }

      if (this.isReturning()) {
         this.field_7588 = false;
         this.field_7576 = 0;
         this.method_5875(true);
         this.method_7433(true);
      }
   }

   public boolean isStuck() {
      return (Boolean)this.field_6011.method_12789(STUCK);
   }

   public ItemStack getOriginalStack() {
      return !this.originalStack.method_7960() ? this.originalStack : new ItemStack(ModItems.FISSURED_SPEAR_PURPLE);
   }

   public float getStuckYaw() {
      return (Float)this.field_6011.method_12789(STUCK_YAW);
   }

   public float getStuckPitch() {
      return (Float)this.field_6011.method_12789(STUCK_PITCH);
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
