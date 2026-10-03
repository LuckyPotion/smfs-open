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
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 80000.0)
         .method_26868(EntityAttributes.field_23719, 0.2)
         .method_26868(EntityAttributes.field_23721, 4.0);
   }

   public PuppetGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1800, 110, 140, 0.12F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(80000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.2);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(4.0);
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
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

      if (player.method_6059(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      }

      if (!this.isPlayerInRange(player)) {
         return false;
      }

      PuppetGhostEntity.PlayerPositionInfo info = this.playerPositions.get(player.method_5667());
      return info != null && info.isStationary();
   }

   private void updatePlayerPositions() {
      for (PlayerEntity player : this.method_37908().method_18456()) {
         UUID playerId = player.method_5667();
         Vec3d currentPos = player.method_19538();
         PuppetGhostEntity.PlayerPositionInfo info = this.playerPositions.get(playerId);
         if (info == null) {
            info = new PuppetGhostEntity.PlayerPositionInfo(currentPos);
            this.playerPositions.put(playerId, info);
         } else {
            info.update(currentPos);
         }
      }

      this.playerPositions.keySet().removeIf(playerIdx -> this.method_37908().method_18470(playerIdx) == null);
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.method_5858(player) <= 1024.0;
   }

   private static class PlayerPositionInfo {
      private Vec3d lastPosition;
      private int stationaryTicks;

      public PlayerPositionInfo(Vec3d position) {
         this.lastPosition = position;
         this.stationaryTicks = 0;
      }

      public void update(Vec3d currentPosition) {
         if (currentPosition.method_1022(this.lastPosition) > 0.1) {
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
