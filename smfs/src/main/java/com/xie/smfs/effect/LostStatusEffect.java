package com.xie.smfs.effect;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEffects;
import java.util.Collections;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LostStatusEffect extends StatusEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger(LostStatusEffect.class);

   public LostStatusEffect() {
      super(StatusEffectCategory.HARMFUL, 0);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      StatusEffectInstance effectInstance = entity.getStatusEffect(this);
      if (effectInstance != null && effectInstance.getEffectType() == ModEffects.LOST) {
         if (!(effectInstance instanceof LostStatusEffectInstance lostInstance)) {
            return;
         }

         UUID sourceUuid = lostInstance.getSourceUuid();
         if (sourceUuid != null && entity.getWorld() instanceof ServerWorld serverWorld) {
            Entity source = serverWorld.getEntity(sourceUuid);
            if (source == null) {
               entity.removeStatusEffect(ModEffects.LOST);
               return;
            }

            double ghostDomainRadius;
            Vec3d center;
            if (source instanceof GhostEntity ghost) {
               if (ghost.isSuppressed()) {
                  entity.removeStatusEffect(ModEffects.LOST);
                  return;
               }

               ghostDomainRadius = ghost.getGhostDomainRadius();
               center = ghost.getPos();
            } else if (source instanceof GhostMasterEntity ghostMaster) {
               ghostDomainRadius = 64.0;
               center = ghostMaster.getPos();
            } else {
               if (!(source instanceof PlayerEntity player)) {
                  return;
               }

               int domainLevel = 1;
               if (player.hasStatusEffect(ModEffects.RED_GHOST_DOMAIN)) {
                  domainLevel = player.getStatusEffect(ModEffects.RED_GHOST_DOMAIN).getAmplifier() + 1;
               } else if (player.hasStatusEffect(ModEffects.GREEN_GHOST_DOMAIN)) {
                  domainLevel = player.getStatusEffect(ModEffects.GREEN_GHOST_DOMAIN).getAmplifier() + 1;
               } else if (player.hasStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN)) {
                  domainLevel = player.getStatusEffect(ModEffects.GOLDEN_GHOST_DOMAIN).getAmplifier() + 1;
               } else if (player.hasStatusEffect(ModEffects.THICK_FOG)) {
                  domainLevel = player.getStatusEffect(ModEffects.THICK_FOG).getAmplifier() + 1;
               } else if (player.hasStatusEffect(ModEffects.BLACK_GHOST_DOMAIN)) {
                  domainLevel = player.getStatusEffect(ModEffects.BLACK_GHOST_DOMAIN).getAmplifier() + 1;
               } else if (player.hasStatusEffect(ModEffects.CYAN_GHOST_DOMAIN)) {
                  domainLevel = player.getStatusEffect(ModEffects.CYAN_GHOST_DOMAIN).getAmplifier() + 1;
               } else if (player.hasStatusEffect(ModEffects.GRAY_GHOST_DOMAIN)) {
                  domainLevel = player.getStatusEffect(ModEffects.GRAY_GHOST_DOMAIN).getAmplifier() + 1;
               }

               ghostDomainRadius = GhostDomainManager.getDomainRadius(player, domainLevel) - 2.0;
               center = player.getPos();
            }

            if (ghostDomainRadius <= 0.0) {
               ghostDomainRadius = 64.0;
            }

            double distance = entity.squaredDistanceTo(center);
            if (distance > ghostDomainRadius * ghostDomainRadius) {
               Vec3d direction = entity.getPos().subtract(center).normalize();
               Vec3d newPos = center.add(direction.multiply(ghostDomainRadius - 0.5));
               entity.teleport(newPos.x, newPos.y, newPos.z);
               if (entity instanceof PlayerEntity player) {
                  Vec3d directionToCenter = center.subtract(entity.getPos()).normalize();
                  float newYaw = (float)(Math.atan2(directionToCenter.z, directionToCenter.x) * (180.0 / Math.PI) - 90.0);
                  player.setYaw(newYaw);
                  player.setHeadYaw(newYaw);
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     double x = player.getX();
                     double y = player.getY();
                     double z = player.getZ();
                     float yaw = player.getYaw();
                     float pitch = player.getPitch();
                     serverPlayer.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(x, y, z, newYaw, player.getPitch(), Collections.emptySet(), 0));
                  }
               }
            }
         }
      }
   }
}
