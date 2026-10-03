package com.xie.smfs.util;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class TargetingUtil {
   public static LivingEntity findEntityInLookDirection(PlayerEntity player, double maxDistance, double dotThreshold) {
      return findEntityInLookDirection(player, maxDistance, dotThreshold, null);
   }

   public static LivingEntity findEntityInLookDirection(PlayerEntity player, double maxDistance, double dotThreshold, Predicate<LivingEntity> filter) {
      Vec3d lookVec = player.getRotationVec(1.0F);
      List<Entity> entitiesInRange = player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(maxDistance));
      LivingEntity bestTarget = null;
      double bestDot = -1.0;

      for (Entity entity : entitiesInRange) {
         if (entity instanceof LivingEntity living && entity != player && (filter == null || filter.test(living))) {
            Vec3d toEntityVec = new Vec3d(entity.getX() - player.getX(), entity.getEyeY() - player.getEyeY(), entity.getZ() - player.getZ()).normalize();
            double dot = lookVec.dotProduct(toEntityVec);
            if (dot > dotThreshold && dot > bestDot) {
               bestTarget = living;
               bestDot = dot;
            }
         }
      }

      return bestTarget;
   }

   public static LivingEntity findEntityInLookDirectionWithOcclusion(PlayerEntity player, double maxDistance, double dotThreshold) {
      return findEntityInLookDirectionWithOcclusion(player, maxDistance, dotThreshold, null);
   }

   public static LivingEntity findEntityInLookDirectionWithOcclusion(
      PlayerEntity player, double maxDistance, double dotThreshold, Predicate<LivingEntity> filter
   ) {
      Vec3d lookVec = player.getRotationVec(1.0F);
      List<Entity> entitiesInRange = player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(maxDistance));
      LivingEntity bestTarget = null;
      double bestDot = -1.0;

      for (Entity entity : entitiesInRange) {
         if (entity instanceof LivingEntity living && entity != player && (filter == null || filter.test(living))) {
            Vec3d toEntityVec = new Vec3d(entity.getX() - player.getX(), entity.getEyeY() - player.getEyeY(), entity.getZ() - player.getZ()).normalize();
            double dot = lookVec.dotProduct(toEntityVec);
            if (dot > dotThreshold && dot > bestDot) {
               RaycastContext raycastContext = new RaycastContext(player.getEyePos(), entity.getPos(), ShapeType.COLLIDER, FluidHandling.NONE, player);
               HitResult hitResult = player.getWorld().raycast(raycastContext);
               if (hitResult.getType() != Type.BLOCK) {
                  bestTarget = living;
                  bestDot = dot;
               }
            }
         }
      }

      return bestTarget;
   }

   public static EntityHitResult raycastEntity(ServerPlayerEntity player, double maxDistance) {
      Vec3d startPos = player.getEyePos();
      Vec3d lookVec = player.getRotationVec(1.0F);
      Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));
      double maxSqDist = maxDistance * maxDistance;
      return ProjectileUtil.raycast(
         player,
         startPos,
         endPos,
         player.getBoundingBox().stretch(lookVec.multiply(maxDistance)).expand(1.0),
         e -> !e.isSpectator() && e.canHit() && e instanceof LivingEntity && e != player,
         maxSqDist
      );
   }

   public static EntityHitResult projectileRaycast(Entity entity, Vec3d eyePos, Vec3d lookVec, double maxDistance, double boxExpand) {
      Vec3d maxPos = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);
      Box box = entity.getBoundingBox().stretch(lookVec.multiply(maxDistance)).expand(boxExpand);
      double maxSqDist = maxDistance * maxDistance;
      return ProjectileUtil.raycast(entity, eyePos, maxPos, box, e -> !e.isSpectator() && e.canHit(), maxSqDist);
   }
}
