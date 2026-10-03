package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JumpGhostEntity extends GhostEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger(JumpGhostEntity.class);
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';
   private int jumpCooldown = 0;
   private int checkInterval = 0;
   private final Map<UUID, Boolean> playerPreviousOnGround = new HashMap<>();

   public static Builder createLivingAttributes() {
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 100000.0)
         .method_26868(EntityAttributes.field_23719, 0.25)
         .method_26868(EntityAttributes.field_23721, 5.0);
   }

   public JumpGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 700, 70, 35, 0.15F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(100000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.25);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(5.0);
      this.attackCooldown = 20;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (this.jumpCooldown > 0) {
         this.jumpCooldown--;
         return false;
      } else {
         UUID playerId = player.method_5667();
         boolean isOnGround = player.method_24828();
         double yVelocity = player.method_18798().field_1351;
         boolean wasOnGround = this.playerPreviousOnGround.getOrDefault(playerId, true);
         boolean justJumped = wasOnGround && !isOnGround && yVelocity > 0.1;
         this.playerPreviousOnGround.put(playerId, isOnGround);
         if (justJumped) {
            this.jumpCooldown = 20;
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (this.field_6012 % 100 == 0) {
         int playerCount = this.method_37908().method_18456().size();
         int inRangeCount = 0;

         for (PlayerEntity player : this.method_37908().method_18456()) {
            if (this.isInDetectionRange(player)) {
               inRangeCount++;
            }
         }
      }

      this.checkInterval++;
      if (this.checkInterval >= 200) {
         int initialSize = this.playerPreviousOnGround.size();
         this.checkInterval = 0;
         this.playerPreviousOnGround.keySet().removeIf(playerId -> {
            PlayerEntity playerx = this.method_37908().method_18470(playerId);
            boolean shouldRemove = playerx == null || !playerx.method_5805() || !this.isInDetectionRange(playerx);
            if (shouldRemove) {
            }

            return shouldRemove;
         });
      }
   }

   private boolean isInDetectionRange(PlayerEntity player) {
      double distanceSquared = this.method_5858(player);
      return distanceSquared <= 1024.0;
   }
}
