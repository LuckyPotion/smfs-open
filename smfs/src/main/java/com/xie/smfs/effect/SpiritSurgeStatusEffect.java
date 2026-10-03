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
      super(StatusEffectCategory.field_18271, 10040115);
      this.method_5566(SpiritAttributes.SPIRIT_DAMAGE, "7b9e3a8f-2d4c-4e1a-b6c5-9d8e7f6a5b4c", 6.0, Operation.field_6328);
   }

   public boolean method_5552(int duration, int amplifier) {
      return false;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public void method_5555(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.method_29504()) {
         activePlayers.add(player.method_5667());
      }

      super.method_5555(entity, attributes, amplifier);
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.method_29504()) {
         activePlayers.remove(player.method_5667());
      }

      super.method_5562(entity, attributes, amplifier);
   }

   public static boolean hasSpiritSurgeEffect(PlayerEntity player) {
      return activePlayers.contains(player.method_5667());
   }

   public static void applySpiritSurgeDamage(PlayerEntity attacker, LivingEntity target, float baseDamage) {
      if (hasSpiritSurgeEffect(attacker)) {
         float extraSpiritDamage = 16.0F;
         if (target instanceof PlayerEntity targetPlayer) {
            PlayerEvents.handleSpiritDamage(
               targetPlayer,
               extraSpiritDamage,
               baseDamage,
               ModEvents.processingSpiritDamage.get() ? attacker.method_48923().method_48831() : attacker.method_48923().method_48802(attacker)
            );
         } else if (target instanceof GhostEntity ghost) {
            handleGhostSpiritDamage(attacker, ghost, extraSpiritDamage);
         } else {
            handleDefaultSpiritDamage(attacker, target, extraSpiritDamage, baseDamage);
         }

         if (!target.method_37908().method_8608()) {
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
         ? attacker.method_48923().method_48831()
         : attacker.method_48923().method_48802(attacker);
      if (spiritDamage > 0.0F && !target.method_5679(damageSource)) {
         float currentHealth = target.method_6032();
         float newHealth = Math.max(0.0F, currentHealth - spiritDamage);
         target.method_6033(newHealth);
         if (newHealth <= 0.0F) {
            target.method_6078(damageSource);
         }
      }
   }

   private static void spawnSpiritDamageParticles(LivingEntity target) {
      if (!target.method_37908().method_8608()) {
         ServerWorld serverWorld = (ServerWorld)target.method_37908();
         Vec3d pos = target.method_19538();

         for (int i = 0; i < 15; i++) {
            double offsetX = (target.method_6051().method_43058() - 0.5) * 2.0;
            double offsetY = target.method_6051().method_43058() * 2.0;
            double offsetZ = (target.method_6051().method_43058() - 0.5) * 2.0;
            serverWorld.method_14199(
               ParticleTypes.field_11249, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 1, 0.0, 0.0, 0.0, 0.1
            );
         }
      }
   }
}
