package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PuppetGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';
   private final Map<UUID, PuppetGhostEntity.PlayerPositionInfo> playerPositions = new HashMap<>();
   private static final int STATIONARY_THRESHOLD = 100;

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 80000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
   }

   public PuppetGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1800, 110, 140, 0.12F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(80000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.2);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(4.0);
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
         this.updatePlayerPositions();
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      }

      if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      }

      if (player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      }

      if (!this.isPlayerInRange(player)) {
         return false;
      }

      PuppetGhostEntity.PlayerPositionInfo info = this.playerPositions.get(player.getUuid());
      return info != null && info.isStationary();
   }

   private void updatePlayerPositions() {
      for (PlayerEntity player : this.getWorld().getPlayers()) {
         UUID playerId = player.getUuid();
         Vec3d currentPos = player.getPos();
         PuppetGhostEntity.PlayerPositionInfo info = this.playerPositions.get(playerId);
         if (info == null) {
            info = new PuppetGhostEntity.PlayerPositionInfo(currentPos);
            this.playerPositions.put(playerId, info);
         } else {
            info.update(currentPos);
         }
      }

      this.playerPositions.keySet().removeIf(playerIdx -> this.getWorld().getPlayerByUuid(playerIdx) == null);
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= 1024.0;
   }

   private static class PlayerPositionInfo {
      private Vec3d lastPosition;
      private int stationaryTicks;

      public PlayerPositionInfo(Vec3d position) {
         this.lastPosition = position;
         this.stationaryTicks = 0;
      }

      public void update(Vec3d currentPosition) {
         if (currentPosition.distanceTo(this.lastPosition) > 0.1) {
            this.stationaryTicks = 0;
            this.lastPosition = currentPosition;
         } else {
            this.stationaryTicks++;
         }
      }

      public boolean isStationary() {
         return this.stationaryTicks >= 100;
      }
   }
}
