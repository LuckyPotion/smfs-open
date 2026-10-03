package com.xie.smfs.manager;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalkingStateManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(WalkingStateManager.class);
   private static final double WALKING_THRESHOLD = 0.01;
   private static final int STATE_UPDATE_INTERVAL = 5;
   private boolean isWalking = false;
   private boolean isForcedWalking = false;
   private int lastUpdateTick = 0;
   private double lastHorizontalSpeedSquared = 0.0;
   private final MobEntity entity;

   public WalkingStateManager(MobEntity entity) {
      this.entity = entity;
   }

   public void update(int currentTick) {
      if (currentTick - this.lastUpdateTick >= 5) {
         this.lastUpdateTick = currentTick;
         if (this.isForcedWalking) {
            if (!this.isWalking) {
               this.isWalking = true;
               LOGGER.debug("Entity {} 进入强制行走状态", this.entity.getUuidAsString());
            }
         } else {
            Vec3d velocity = this.entity.getVelocity();
            double currentHorizontalSpeedSquared = velocity.horizontalLengthSquared();
            double smoothedSpeed = this.smoothSpeed(currentHorizontalSpeedSquared);
            boolean shouldBeWalking = smoothedSpeed > 0.01;
            if (this.isWalking != shouldBeWalking) {
               this.isWalking = shouldBeWalking;
               LOGGER.debug("Entity {} 行走状态更新为: {} (速度: {})", this.entity.getUuidAsString(), shouldBeWalking, smoothedSpeed);
            }

            this.lastHorizontalSpeedSquared = currentHorizontalSpeedSquared;
         }
      }
   }

   private double smoothSpeed(double currentSpeed) {
      double smoothingFactor = 0.7;
      return smoothingFactor * currentSpeed + (1.0 - smoothingFactor) * this.lastHorizontalSpeedSquared;
   }

   public void setForcedWalking(boolean forced) {
      if (this.isForcedWalking != forced) {
         this.isForcedWalking = forced;
         LOGGER.debug("Entity {} 强制行走状态: {}", this.entity.getUuidAsString(), forced);
         if (forced) {
            this.isWalking = true;
         } else {
            Vec3d velocity = this.entity.getVelocity();
            double speed = velocity.horizontalLengthSquared();
            this.isWalking = speed > 0.01;
         }
      }
   }

   public boolean isWalking() {
      return this.isWalking;
   }

   public boolean isForcedWalking() {
      return this.isForcedWalking;
   }

   public void reset() {
      this.isWalking = false;
      this.isForcedWalking = false;
      this.lastHorizontalSpeedSquared = 0.0;
      LOGGER.debug("Entity {} 行走状态已重置", this.entity.getUuidAsString());
   }

   public double getCurrentHorizontalSpeedSquared() {
      return this.lastHorizontalSpeedSquared;
   }

   public static double getWalkingThreshold() {
      return 0.01;
   }

   public static int getStateUpdateInterval() {
      return 5;
   }
}
