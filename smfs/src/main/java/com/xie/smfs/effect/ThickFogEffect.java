package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThickFogEffect extends StatusEffect implements ICurseEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ThickFogEffect");

   public ThickFogEffect() {
      super(StatusEffectCategory.HARMFUL, 11184810);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.getWorld().isClient() && player instanceof ServerPlayerEntity serverPlayer) {
         int fogGhostSlot = PlayerEvents.findEquippedThickFogSlot(player);
         if (fogGhostSlot != -1) {
            int radius = 10;

            for (LivingEntity movingEntity : findMovingEntitiesInRange(player, radius)) {
               GhostDomainManager.markEntityForFogGhost(serverPlayer, movingEntity);
            }
         }
      }
   }

   private static List<LivingEntity> findMovingEntitiesInRange(PlayerEntity player, int radius) {
      List<LivingEntity> entities = new ArrayList<>();
      BlockPos playerPos = player.getBlockPos();
      World world = player.getWorld();

      for (LivingEntity entity : world.getEntitiesByClass(
         LivingEntity.class, new Box(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius)), entityx -> entityx != player
      )) {
         Vec3d velocity = entity.getVelocity();
         double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
         if (speed > 0.1) {
            entities.add(entity);
         }
      }

      return entities;
   }

   public static void updateRevivalDegreeInGhostDomain(PlayerEntity player) {
      if (!player.getWorld().isClient()) {
         PlayerEvents.balanceRevivalDegree(player, 3, 1);
      }
   }
}
