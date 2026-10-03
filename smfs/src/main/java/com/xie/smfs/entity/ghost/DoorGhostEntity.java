package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DoorGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = false;
   private static final int GHOST_DOMAIN_LEVEL = 1;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'B';
   private int attackCooldown = 0;
   private static final double DOOR_SEARCH_RANGE = 32.0;
   private static final double DOOR_DAMAGE_RANGE = 16.0;
   private static final double DOOR_NEARBY_RANGE = 3.0;
   private BlockPos currentDoorTarget = null;
   private int doorSearchCooldown = 0;
   private int doorTargetTimer = 0;
   private static final int DOOR_TARGET_TIMEOUT = 600;

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 95000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.22)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0);
   }

   public DoorGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 1, 32.0, 'B', 1500, 550, 60, 0.08F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(95000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.22);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(12.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else if (this.attackCooldown > 0) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : false;
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= 1024.0;
   }

   private boolean hasAlivePlayersNearby() {
      World world = this.getWorld();
      Box searchBox = this.getBoundingBox().expand(32.0);
      List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, searchBox, player -> player.isAlive() && !player.isSpectator());
      return !nearbyPlayers.isEmpty();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.attackCooldown > 0) {
         this.attackCooldown--;
      }

      if (this.doorSearchCooldown > 0) {
         this.doorSearchCooldown--;
      }

      if (this.currentDoorTarget != null) {
         this.doorTargetTimer++;
      } else {
         this.doorTargetTimer = 0;
      }

      if (!this.getWorld().isClient()) {
         if (this.isSuppressed() || this.isDeadlocked() || !this.hasAlivePlayersNearby()) {
            this.currentDoorTarget = null;
            this.doorTargetTimer = 0;
         } else if (this.currentDoorTarget != null) {
            this.moveToDoor();
         } else if (this.age % 20 == 0 && this.doorSearchCooldown <= 0) {
            this.searchAndOpenDoors();
         }
      }
   }

   private void searchAndOpenDoors() {
      if (!this.isSuppressed() && !this.isDeadlocked() && this.hasAlivePlayersNearby()) {
         BlockPos centerPos = this.getBlockPos();
         World world = this.getWorld();

         for (int x = -32; x <= 32.0; x++) {
            for (int y = -32; y <= 32.0; y++) {
               for (int z = -32; z <= 32.0; z++) {
                  BlockPos checkPos = centerPos.add(x, y, z);
                  BlockState blockState = world.getBlockState(checkPos);
                  if (blockState.getBlock() instanceof DoorBlock && !(Boolean)blockState.get(DoorBlock.OPEN)) {
                     double distanceToDoor = centerPos.getSquaredDistance(checkPos);
                     if (distanceToDoor <= 9.0) {
                        this.openDoor(world, checkPos, blockState);
                        this.damageNearbyEntities(world, checkPos);
                        this.doorSearchCooldown = 40;
                        return;
                     }

                     this.currentDoorTarget = checkPos;
                     this.doorTargetTimer = 0;
                     return;
                  }
               }
            }
         }

         this.doorSearchCooldown = 40;
      } else {
         this.currentDoorTarget = null;
         this.doorTargetTimer = 0;
         this.doorSearchCooldown = 40;
      }
   }

   private void moveToDoor() {
      if (this.isSuppressed() || this.isDeadlocked() || !this.hasAlivePlayersNearby()) {
         this.currentDoorTarget = null;
         this.doorTargetTimer = 0;
         this.doorSearchCooldown = 40;
      } else if (this.currentDoorTarget != null) {
         World world = this.getWorld();
         BlockState blockState = world.getBlockState(this.currentDoorTarget);
         if (!(blockState.getBlock() instanceof DoorBlock) || (Boolean)blockState.get(DoorBlock.OPEN)) {
            this.currentDoorTarget = null;
            this.doorTargetTimer = 0;
         } else if (this.doorTargetTimer > 600) {
            this.openDoor(world, this.currentDoorTarget, blockState);
            this.damageNearbyEntities(world, this.currentDoorTarget);
            this.currentDoorTarget = null;
            this.doorTargetTimer = 0;
            this.doorSearchCooldown = 40;
         } else {
            Vec3d doorCenter = Vec3d.ofCenter(this.currentDoorTarget);
            double distanceToDoor = this.squaredDistanceTo(doorCenter);
            if (distanceToDoor <= 9.0) {
               this.openDoor(world, this.currentDoorTarget, blockState);
               this.damageNearbyEntities(world, this.currentDoorTarget);
               this.currentDoorTarget = null;
               this.doorTargetTimer = 0;
               this.doorSearchCooldown = 40;
            } else {
               this.getNavigation().startMovingTo(doorCenter.x, doorCenter.y, doorCenter.z, 1.0);
            }
         }
      }
   }

   private void openDoor(World world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, (BlockState)state.with(DoorBlock.OPEN, true));
      world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
      if (state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
         BlockPos upperPos = pos.up();
         BlockState upperState = world.getBlockState(upperPos);
         if (upperState.getBlock() instanceof DoorBlock) {
            world.setBlockState(upperPos, (BlockState)upperState.with(DoorBlock.OPEN, true));
         }
      } else if (state.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
         BlockPos lowerPos = pos.down();
         BlockState lowerState = world.getBlockState(lowerPos);
         if (lowerState.getBlock() instanceof DoorBlock) {
            world.setBlockState(lowerPos, (BlockState)lowerState.with(DoorBlock.OPEN, true));
         }
      }
   }

   private void damageNearbyEntities(World world, BlockPos doorPos) {
      Box damageBox = new Box(
         doorPos.getX() - 16.0, doorPos.getY() - 16.0, doorPos.getZ() - 16.0, doorPos.getX() + 16.0, doorPos.getY() + 16.0, doorPos.getZ() + 16.0
      );
      List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, damageBox, entityx -> entityx != this && entityx instanceof PlayerEntity);
      DamageSource damageSource = ModDamageSources.ghost(world);

      for (LivingEntity entity : nearbyEntities) {
         if (entity instanceof PlayerEntity) {
            float damage = this.getSpiritualDamage() * 2.0F;
            PlayerEvents.handleSpiritDamage((PlayerEntity)entity, damage, damage, damageSource);
         }
      }
   }
}
