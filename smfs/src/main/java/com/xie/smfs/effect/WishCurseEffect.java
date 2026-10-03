package com.xie.smfs.effect;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class WishCurseEffect extends StatusEffect implements ICurseEffect {
   public WishCurseEffect() {
      super(StatusEffectCategory.HARMFUL, 9109504);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity.age % 60 == 0) {
         this.spawnLightningAtTarget(entity);
         if (!entity.getWorld().isClient()) {
            ServerWorld serverWorld = (ServerWorld)entity.getWorld();
            serverWorld.playSound(
               null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1.0F, 1.0F
            );
         }

         if (entity instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("effect.smfs.wish_curse.lightning_hit").formatted(Formatting.RED), true);
         }
      }

      if (entity.age % 20 == 0) {
         entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, amplifier));
         entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, amplifier));
         entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 40, amplifier));
      }

      if (!entity.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)entity.getWorld();
         Vec3d pos = entity.getPos();

         for (int i = 0; i < 3; i++) {
            double offsetX = (entity.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = entity.getRandom().nextDouble() * 2.0;
            double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 2.0;
            serverWorld.spawnParticles(ParticleTypes.LAVA, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 2, 0.1, 0.1, 0.1, 0.02);
         }
      }
   }

   private void spawnLightningAtTarget(LivingEntity target) {
      if (!target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld);
         lightning.setPosition(target.getX(), target.getY() + 1.0, target.getZ());
         lightning.setCosmetic(true);
         serverWorld.spawnEntity(lightning);
      }
   }

   public boolean isBeneficial() {
      return false;
   }
}
