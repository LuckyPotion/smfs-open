package com.xie.smfs.effect;

import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PlagueEffect extends StatusEffect {
   public PlagueEffect() {
      super(StatusEffectCategory.field_18272, 4876097);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         if (player.method_6059(ModEffects.SILENCE) || player.method_6059(ModEffects.DREAM)) {
            return;
         }

         if (GhostSkillManager.hasPlagueGhostEquipped(player)) {
            player.method_6016(this);
            return;
         }
      }

      if (entity.field_6012 % 20 == 0) {
         entity.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 40, 1));
      }

      if (!entity.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)entity.method_37908();
         Vec3d pos = entity.method_19538();

         for (int i = 0; i < 5; i++) {
            double offsetX = (entity.method_6051().method_43058() - 0.5) * 1.5;
            double offsetY = entity.method_6051().method_43058() * 2.0;
            double offsetZ = (entity.method_6051().method_43058() - 0.5) * 1.5;
            serverWorld.method_14199(
               ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 3, 0.1, 0.1, 0.1, 0.02
            );
         }
      }

      if (entity.field_6012 % 40 == 0 && !entity.method_37908().method_8608()) {
         World world = entity.method_37908();

         for (LivingEntity nearbyEntity : world.method_8390(LivingEntity.class, entity.method_5829().method_1014(3.0), e -> e != entity && !e.method_6059(this))) {
            int plagueDuration = 600 + amplifier * 5 * 20;
            nearbyEntity.method_6092(new StatusEffectInstance(this, plagueDuration, amplifier));
         }
      }
   }

   public boolean method_5573() {
      return false;
   }
}
