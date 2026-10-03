package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.event.ModEvents;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class SpiritSurgeStatusEffect extends StatusEffect {
   private static final Set<UUID> activePlayers = new HashSet<>();

   public SpiritSurgeStatusEffect() {
      super(StatusEffectCategory.BENEFICIAL, 10040115);
      this.addAttributeModifier(SpiritAttributes.SPIRIT_DAMAGE, "7b9e3a8f-2d4c-4e1a-b6c5-9d8e7f6a5b4c", 6.0, Operation.ADDITION);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return false;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }

   public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.isDead()) {
         activePlayers.add(player.getUuid());
      }

      super.onApplied(entity, attributes, amplifier);
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.isDead()) {
         activePlayers.remove(player.getUuid());
      }

      super.onRemoved(entity, attributes, amplifier);
   }

   public static boolean hasSpiritSurgeEffect(PlayerEntity player) {
      return activePlayers.contains(player.getUuid());
   }

   public static void applySpiritSurgeDamage(PlayerEntity attacker, LivingEntity target, float baseDamage) {
      if (hasSpiritSurgeEffect(attacker)) {
         float extraSpiritDamage = 16.0F;
         if (target instanceof PlayerEntity targetPlayer) {
            PlayerEvents.handleSpiritDamage(
               targetPlayer,
               extraSpiritDamage,
               baseDamage,
               ModEvents.processingSpiritDamage.get() ? attacker.getDamageSources().magic() : attacker.getDamageSources().playerAttack(attacker)
            );
         } else if (target instanceof GhostEntity ghost) {
            handleGhostSpiritDamage(attacker, ghost, extraSpiritDamage);
         } else {
            handleDefaultSpiritDamage(attacker, target, extraSpiritDamage, baseDamage);
         }

         if (!target.getWorld().isClient()) {
            spawnSpiritDamageParticles(target);
         }
      }
   }

   private static void handleGhostSpiritDamage(PlayerEntity attacker, GhostEntity ghost, float spiritDamage) {
      float ghostSpiritResistance = ghost.getSpiritualResistance();
      float playerSpiritDamage = PlayerEvents.getSpiritAttribute(attacker, SpiritAttributes.SPIRIT_DAMAGE);
      float actualSpiritDamage = PlayerEvents.calculateSpiritSurgeDamage(playerSpiritDamage, spiritDamage, ghostSpiritResistance);
      int currentSpirit = ghost.getSpiritualStrength();
      int newSpirit = Math.max(0, currentSpirit - (int)actualSpiritDamage);
      ghost.setSpiritualStrength(newSpirit);
      if (newSpirit == 0 && !ghost.isDeadlocked()) {
         ghost.setDeadlocked(true);
      }
   }

   private static void handleDefaultSpiritDamage(PlayerEntity attacker, LivingEntity target, float spiritDamage, float baseDamage) {
      DamageSource damageSource = ModEvents.processingSpiritDamage.get()
         ? attacker.getDamageSources().magic()
         : attacker.getDamageSources().playerAttack(attacker);
      if (spiritDamage > 0.0F && !target.isInvulnerableTo(damageSource)) {
         float currentHealth = target.getHealth();
         float newHealth = Math.max(0.0F, currentHealth - spiritDamage);
         target.setHealth(newHealth);
         if (newHealth <= 0.0F) {
            target.onDeath(damageSource);
         }
      }
   }

   private static void spawnSpiritDamageParticles(LivingEntity target) {
      if (!target.getWorld().isClient()) {
         ServerWorld serverWorld = (ServerWorld)target.getWorld();
         Vec3d pos = target.getPos();

         for (int i = 0; i < 15; i++) {
            double offsetX = (target.getRandom().nextDouble() - 0.5) * 2.0;
            double offsetY = target.getRandom().nextDouble() * 2.0;
            double offsetZ = (target.getRandom().nextDouble() - 0.5) * 2.0;
            serverWorld.spawnParticles(ParticleTypes.WITCH, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.1);
         }
      }
   }
}
