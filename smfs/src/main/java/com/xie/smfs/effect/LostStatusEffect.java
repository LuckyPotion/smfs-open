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
      super(StatusEffectCategory.field_18272, 0);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      StatusEffectInstance effectInstance = entity.method_6112(this);
      if (effectInstance != null && effectInstance.method_5579() == ModEffects.LOST) {
         if (!(effectInstance instanceof LostStatusEffectInstance lostInstance)) {
            return;
         }

         UUID sourceUuid = lostInstance.getSourceUuid();
         if (sourceUuid != null && entity.method_37908() instanceof ServerWorld serverWorld) {
            Entity source = serverWorld.method_14190(sourceUuid);
            if (source == null) {
               entity.method_6016(ModEffects.LOST);
               return;
            }

            double ghostDomainRadius;
            Vec3d center;
            if (source instanceof GhostEntity ghost) {
               if (ghost.isSuppressed()) {
                  entity.method_6016(ModEffects.LOST);
                  return;
               }

               ghostDomainRadius = ghost.getGhostDomainRadius();
               center = ghost.method_19538();
            } else if (source instanceof GhostMasterEntity ghostMaster) {
               ghostDomainRadius = 64.0;
               center = ghostMaster.method_19538();
            } else {
               if (!(source instanceof PlayerEntity player)) {
                  return;
               }

               int domainLevel = 1;
               if (player.method_6059(ModEffects.RED_GHOST_DOMAIN)) {
                  domainLevel = player.method_6112(ModEffects.RED_GHOST_DOMAIN).method_5578() + 1;
               } else if (player.method_6059(ModEffects.GREEN_GHOST_DOMAIN)) {
                  domainLevel = player.method_6112(ModEffects.GREEN_GHOST_DOMAIN).method_5578() + 1;
               } else if (player.method_6059(ModEffects.GOLDEN_GHOST_DOMAIN)) {
                  domainLevel = player.method_6112(ModEffects.GOLDEN_GHOST_DOMAIN).method_5578() + 1;
               } else if (player.method_6059(ModEffects.THICK_FOG)) {
                  domainLevel = player.method_6112(ModEffects.THICK_FOG).method_5578() + 1;
               } else if (player.method_6059(ModEffects.BLACK_GHOST_DOMAIN)) {
                  domainLevel = player.method_6112(ModEffects.BLACK_GHOST_DOMAIN).method_5578() + 1;
               } else if (player.method_6059(ModEffects.CYAN_GHOST_DOMAIN)) {
                  domainLevel = player.method_6112(ModEffects.CYAN_GHOST_DOMAIN).method_5578() + 1;
               } else if (player.method_6059(ModEffects.GRAY_GHOST_DOMAIN)) {
                  domainLevel = player.method_6112(ModEffects.GRAY_GHOST_DOMAIN).method_5578() + 1;
               }

               ghostDomainRadius = GhostDomainManager.getDomainRadius(player, domainLevel) - 2.0;
               center = player.method_19538();
            }

            if (ghostDomainRadius <= 0.0) {
               ghostDomainRadius = 64.0;
            }

            double distance = entity.method_5707(center);
            if (distance > ghostDomainRadius * ghostDomainRadius) {
               Vec3d direction = entity.method_19538().method_1020(center).method_1029();
               Vec3d newPos = center.method_1019(direction.method_1021(ghostDomainRadius - 0.5));
               entity.method_20620(newPos.field_1352, newPos.field_1351, newPos.field_1350);
               if (entity instanceof PlayerEntity player) {
                  Vec3d directionToCenter = center.method_1020(entity.method_19538()).method_1029();
                  float newYaw = (float)(Math.atan2(directionToCenter.field_1350, directionToCenter.field_1352) * (180.0 / Math.PI) - 90.0);
                  player.method_36456(newYaw);
                  player.method_5847(newYaw);
                  if (player instanceof ServerPlayerEntity serverPlayer) {
                     double x = player.method_23317();
                     double y = player.method_23318();
                     double z = player.method_23321();
                     float yaw = player.method_36454();
                     float pitch = player.method_36455();
                     serverPlayer.field_13987.method_14364(new PlayerPositionLookS2CPacket(x, y, z, newYaw, player.method_36455(), Collections.emptySet(), 0));
                  }
               }
            }
         }
      }
   }
}
