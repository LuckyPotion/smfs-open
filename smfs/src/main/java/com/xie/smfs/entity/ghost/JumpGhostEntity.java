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
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 100000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
   }

   public JumpGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 700, 70, 35, 0.15F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(100000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.25);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(5.0);
      this.attackCooldown = 20;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (this.jumpCooldown > 0) {
         this.jumpCooldown--;
         return false;
      } else {
         UUID playerId = player.getUuid();
         boolean isOnGround = player.isOnGround();
         double yVelocity = player.getVelocity().y;
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
   public void tick() {
      super.tick();
      if (this.age % 100 == 0) {
         int playerCount = this.getWorld().getPlayers().size();
         int inRangeCount = 0;

         for (PlayerEntity player : this.getWorld().getPlayers()) {
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
            PlayerEntity playerx = this.getWorld().getPlayerByUuid(playerId);
            boolean shouldRemove = playerx == null || !playerx.isAlive() || !this.isInDetectionRange(playerx);
            if (shouldRemove) {
            }

            return shouldRemove;
         });
      }
   }

   private boolean isInDetectionRange(PlayerEntity player) {
      double distanceSquared = this.squaredDistanceTo(player);
      return distanceSquared <= 1024.0;
   }
}
