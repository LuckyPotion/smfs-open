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
      super(StatusEffectCategory.field_18272, 11184810);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.method_37908().method_8608() && player instanceof ServerPlayerEntity serverPlayer) {
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
      BlockPos playerPos = player.method_24515();
      World world = player.method_37908();

      for (LivingEntity entity : world.method_8390(
         LivingEntity.class,
         new Box(playerPos.method_10069(-radius, -radius, -radius), playerPos.method_10069(radius, radius, radius)),
         entityx -> entityx != player
      )) {
         Vec3d velocity = entity.method_18798();
         double speed = Math.sqrt(velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350);
         if (speed > 0.1) {
            entities.add(entity);
         }
      }

      return entities;
   }

   public static void updateRevivalDegreeInGhostDomain(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         PlayerEvents.balanceRevivalDegree(player, 3, 1);
      }
   }
}
