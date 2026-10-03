package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.item.RedGhostCandleItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SilentGhostEntity extends GhostEntity {
   private final Map<UUID, Long> playerMoveStartTicks = new HashMap<>();
   private final Map<UUID, Vec3d> playerLastPositions = new HashMap<>();
   private static final double MOVEMENT_THRESHOLD = 0.01;
   private static final int ATTACK_TRIGGER_TICKS = 1;
   private static final int AUTO_DISAPPEAR_TICKS = 6000;
   private static final int ATTACK_COOLDOWN_TICKS = 100;
   private long spawnTime = -1L;
   private long lastAttackTime = -1L;

   public SilentGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 0.0, 'C', 1000, 60, 30, 0.15F);
      this.ghostLevel = 1;
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (this.spawnTime == -1L) {
         this.spawnTime = this.method_37908().method_8510();
      }

      long currentTime = this.method_37908().method_8510();
      if (currentTime - this.spawnTime >= 6000L) {
         GhostDeathHandler.markLegitimateRemoval(this);
         this.method_31472();
      } else {
         if (!this.method_37908().field_9236 && !this.isDeadlocked() && !this.isSuppressed()) {
            this.method_37908()
               .method_18456()
               .stream()
               .filter(this::shouldAttackPlayer)
               .filter(player -> this.method_5858(player) <= 256.0)
               .forEach(player -> {
                  this.executeAttack(player);
                  this.lastAttackTime = this.method_37908().method_8510();
               });
         }
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (!this.isSuppressed() && !this.isDeadlocked() && !RedGhostCandleItem.isHoldingCandle(player)) {
         long currentTick = this.method_37908().method_8510();
         if (this.lastAttackTime != -1L && currentTick - this.lastAttackTime < 100L) {
            return false;
         }

         UUID playerId = player.method_5667();
         Vec3d currentPos = player.method_19538();
         Vec3d lastPos = this.playerLastPositions.getOrDefault(playerId, currentPos);
         double distanceMoved = currentPos.method_1022(lastPos);
         boolean isMoving = distanceMoved > 0.01;
         this.playerLastPositions.put(playerId, currentPos);
         if (isMoving) {
            if (this.playerMoveStartTicks.containsKey(playerId)) {
               long moveDuration = currentTick - this.playerMoveStartTicks.get(playerId);
               return moveDuration >= 1L;
            }

            this.playerMoveStartTicks.put(playerId, currentTick);
         } else if (this.playerMoveStartTicks.containsKey(playerId)) {
            this.resetPlayerTracking(player);
         }

         return false;
      } else {
         this.resetPlayerTracking(player);
         return false;
      }
   }

   private void resetPlayerTracking(PlayerEntity player) {
      UUID playerId = player.method_5667();
      this.playerMoveStartTicks.remove(playerId);
      this.playerLastPositions.remove(playerId);
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
   }
}
