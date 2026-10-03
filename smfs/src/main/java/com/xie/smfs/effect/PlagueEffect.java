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
      super(StatusEffectCategory.HARMFUL, 4876097);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         if (player.hasStatusEffect(ModEffects.SILENCE) || player.hasStatusEffect(ModEffects.DREAM)) {
            return;
         }

         if (GhostSkillManager.hasPlagueGhostEquipped(player)) {
            player.removeStatusEffect(this);
            return;
         }
      }

      if (entity.age % 20 == 0) {
         entity.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 40, 1));
      }

      if (!entity.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)entity.getWorld();
         Vec3d pos = entity.getPos();

         for (int i = 0; i < 5; i++) {
            double offsetX = (entity.getRandom().nextDouble() - 0.5) * 1.5;
            double offsetY = entity.getRandom().nextDouble() * 2.0;
            double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 1.5;
            serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 3, 0.1, 0.1, 0.1, 0.02);
         }
      }

      if (entity.age % 40 == 0 && !entity.getWorld().isClient()) {
         World world = entity.getWorld();

         for (LivingEntity nearbyEntity : world.getEntitiesByClass(
            LivingEntity.class, entity.getBoundingBox().expand(3.0), e -> e != entity && !e.hasStatusEffect(this)
         )) {
            int plagueDuration = 600 + amplifier * 5 * 20;
            nearbyEntity.addStatusEffect(new StatusEffectInstance(this, plagueDuration, amplifier));
         }
      }
   }

   public boolean isBeneficial() {
      return false;
   }
}
