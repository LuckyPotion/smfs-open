package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FogGhostEntity extends GhostEntity {
   private final Map<UUID, Long> playerMoveStartTicks = new HashMap<>();
   private final Map<UUID, Vec3d> playerLastPositions = new HashMap<>();
   private static final double MOVEMENT_THRESHOLD = 0.1;
   private static final int ATTACK_TRIGGER_TICKS = 60;

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      player.method_6092(new StatusEffectInstance(ModEffects.THICK_FOG_TARGET, Integer.MAX_VALUE, this.getGhostDomainActualLevel() - 1, false, false));
   }

   public FogGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 64.0, 'B', 2500, 190, 45, 0.25F);
      this.ghostLevel = 2;
      this.setGhostDomainActualLevel(2);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (!this.isSuppressed() && !this.isDeadlocked() && !RedGhostCandleItem.isHoldingCandle(player)) {
         UUID playerId = player.method_5667();
         Vec3d currentPos = player.method_19538();
         Vec3d lastPos = this.playerLastPositions.getOrDefault(playerId, currentPos);
         long currentTick = this.method_37908().method_8510();
         double distanceMoved = currentPos.method_1022(lastPos);
         boolean isMoving = distanceMoved > 0.1;
         this.playerLastPositions.put(playerId, currentPos);
         if (isMoving) {
            if (this.playerMoveStartTicks.containsKey(playerId)) {
               long moveDuration = currentTick - this.playerMoveStartTicks.get(playerId);
               return moveDuration >= 60L;
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
}
