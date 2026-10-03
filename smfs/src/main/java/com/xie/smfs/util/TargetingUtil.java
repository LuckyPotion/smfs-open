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
      Vec3d lookVec = player.method_5828(1.0F);
      List<Entity> entitiesInRange = player.method_37908().method_8335(player, player.method_5829().method_1014(maxDistance));
      LivingEntity bestTarget = null;
      double bestDot = -1.0;

      for (Entity entity : entitiesInRange) {
         if (entity instanceof LivingEntity living && entity != player && (filter == null || filter.test(living))) {
            Vec3d toEntityVec = new Vec3d(
                  entity.method_23317() - player.method_23317(), entity.method_23320() - player.method_23320(), entity.method_23321() - player.method_23321()
               )
               .method_1029();
            double dot = lookVec.method_1026(toEntityVec);
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
      Vec3d lookVec = player.method_5828(1.0F);
      List<Entity> entitiesInRange = player.method_37908().method_8335(player, player.method_5829().method_1014(maxDistance));
      LivingEntity bestTarget = null;
      double bestDot = -1.0;

      for (Entity entity : entitiesInRange) {
         if (entity instanceof LivingEntity living && entity != player && (filter == null || filter.test(living))) {
            Vec3d toEntityVec = new Vec3d(
                  entity.method_23317() - player.method_23317(), entity.method_23320() - player.method_23320(), entity.method_23321() - player.method_23321()
               )
               .method_1029();
            double dot = lookVec.method_1026(toEntityVec);
            if (dot > dotThreshold && dot > bestDot) {
               RaycastContext raycastContext = new RaycastContext(
                  player.method_33571(), entity.method_19538(), ShapeType.field_17558, FluidHandling.field_1348, player
               );
               HitResult hitResult = player.method_37908().method_17742(raycastContext);
               if (hitResult.method_17783() != Type.field_1332) {
                  bestTarget = living;
                  bestDot = dot;
               }
            }
         }
      }

      return bestTarget;
   }

   public static EntityHitResult raycastEntity(ServerPlayerEntity player, double maxDistance) {
      Vec3d startPos = player.method_33571();
      Vec3d lookVec = player.method_5828(1.0F);
      Vec3d endPos = startPos.method_1019(lookVec.method_1021(maxDistance));
      double maxSqDist = maxDistance * maxDistance;
      return ProjectileUtil.method_18075(
         player,
         startPos,
         endPos,
         player.method_5829().method_18804(lookVec.method_1021(maxDistance)).method_1014(1.0),
         e -> !e.method_7325() && e.method_5863() && e instanceof LivingEntity && e != player,
         maxSqDist
      );
   }

   public static EntityHitResult projectileRaycast(Entity entity, Vec3d eyePos, Vec3d lookVec, double maxDistance, double boxExpand) {
      Vec3d maxPos = eyePos.method_1031(lookVec.field_1352 * maxDistance, lookVec.field_1351 * maxDistance, lookVec.field_1350 * maxDistance);
      Box box = entity.method_5829().method_18804(lookVec.method_1021(maxDistance)).method_1014(boxExpand);
      double maxSqDist = maxDistance * maxDistance;
      return ProjectileUtil.method_18075(entity, eyePos, maxPos, box, e -> !e.method_7325() && e.method_5863(), maxSqDist);
   }
}
