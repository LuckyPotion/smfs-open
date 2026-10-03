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
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 95000.0)
         .method_26868(EntityAttributes.field_23719, 0.22)
         .method_26868(EntityAttributes.field_23721, 12.0);
   }

   public DoorGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 1, 32.0, 'B', 1500, 550, 60, 0.08F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(95000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.22);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(12.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.method_6059(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else if (this.attackCooldown > 0) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : false;
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.method_5858(player) <= 1024.0;
   }

   private boolean hasAlivePlayersNearby() {
      World world = this.method_37908();
      Box searchBox = this.method_5829().method_1014(32.0);
      List<PlayerEntity> nearbyPlayers = world.method_8390(PlayerEntity.class, searchBox, player -> player.method_5805() && !player.method_7325());
      return !nearbyPlayers.isEmpty();
   }

   @Override
   public void method_5773() {
      super.method_5773();
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

      if (!this.method_37908().method_8608()) {
         if (this.isSuppressed() || this.isDeadlocked() || !this.hasAlivePlayersNearby()) {
            this.currentDoorTarget = null;
            this.doorTargetTimer = 0;
         } else if (this.currentDoorTarget != null) {
            this.moveToDoor();
         } else if (this.field_6012 % 20 == 0 && this.doorSearchCooldown <= 0) {
            this.searchAndOpenDoors();
         }
      }
   }

   private void searchAndOpenDoors() {
      if (!this.isSuppressed() && !this.isDeadlocked() && this.hasAlivePlayersNearby()) {
         BlockPos centerPos = this.method_24515();
         World world = this.method_37908();

         for (int x = -32; x <= 32.0; x++) {
            for (int y = -32; y <= 32.0; y++) {
               for (int z = -32; z <= 32.0; z++) {
                  BlockPos checkPos = centerPos.method_10069(x, y, z);
                  BlockState blockState = world.method_8320(checkPos);
                  if (blockState.method_26204() instanceof DoorBlock && !(Boolean)blockState.method_11654(DoorBlock.field_10945)) {
                     double distanceToDoor = centerPos.method_10262(checkPos);
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
         World world = this.method_37908();
         BlockState blockState = world.method_8320(this.currentDoorTarget);
         if (!(blockState.method_26204() instanceof DoorBlock) || (Boolean)blockState.method_11654(DoorBlock.field_10945)) {
            this.currentDoorTarget = null;
            this.doorTargetTimer = 0;
         } else if (this.doorTargetTimer > 600) {
            this.openDoor(world, this.currentDoorTarget, blockState);
            this.damageNearbyEntities(world, this.currentDoorTarget);
            this.currentDoorTarget = null;
            this.doorTargetTimer = 0;
            this.doorSearchCooldown = 40;
         } else {
            Vec3d doorCenter = Vec3d.method_24953(this.currentDoorTarget);
            double distanceToDoor = this.method_5707(doorCenter);
            if (distanceToDoor <= 9.0) {
               this.openDoor(world, this.currentDoorTarget, blockState);
               this.damageNearbyEntities(world, this.currentDoorTarget);
               this.currentDoorTarget = null;
               this.doorTargetTimer = 0;
               this.doorSearchCooldown = 40;
            } else {
               this.method_5942().method_6337(doorCenter.field_1352, doorCenter.field_1351, doorCenter.field_1350, 1.0);
            }
         }
      }
   }

   private void openDoor(World world, BlockPos pos, BlockState state) {
      world.method_8501(pos, (BlockState)state.method_11657(DoorBlock.field_10945, true));
      world.method_8396(null, pos, SoundEvents.field_14664, SoundCategory.field_15245, 1.0F, 1.0F);
      if (state.method_11654(DoorBlock.field_10946) == DoubleBlockHalf.field_12607) {
         BlockPos upperPos = pos.method_10084();
         BlockState upperState = world.method_8320(upperPos);
         if (upperState.method_26204() instanceof DoorBlock) {
            world.method_8501(upperPos, (BlockState)upperState.method_11657(DoorBlock.field_10945, true));
         }
      } else if (state.method_11654(DoorBlock.field_10946) == DoubleBlockHalf.field_12609) {
         BlockPos lowerPos = pos.method_10074();
         BlockState lowerState = world.method_8320(lowerPos);
         if (lowerState.method_26204() instanceof DoorBlock) {
            world.method_8501(lowerPos, (BlockState)lowerState.method_11657(DoorBlock.field_10945, true));
         }
      }
   }

   private void damageNearbyEntities(World world, BlockPos doorPos) {
      Box damageBox = new Box(
         doorPos.method_10263() - 16.0,
         doorPos.method_10264() - 16.0,
         doorPos.method_10260() - 16.0,
         doorPos.method_10263() + 16.0,
         doorPos.method_10264() + 16.0,
         doorPos.method_10260() + 16.0
      );
      List<LivingEntity> nearbyEntities = world.method_8390(LivingEntity.class, damageBox, entityx -> entityx != this && entityx instanceof PlayerEntity);
      DamageSource damageSource = ModDamageSources.ghost(world);

      for (LivingEntity entity : nearbyEntities) {
         if (entity instanceof PlayerEntity) {
            float damage = this.getSpiritualDamage() * 2.0F;
            PlayerEvents.handleSpiritDamage((PlayerEntity)entity, damage, damage, damageSource);
         }
      }
   }
}
