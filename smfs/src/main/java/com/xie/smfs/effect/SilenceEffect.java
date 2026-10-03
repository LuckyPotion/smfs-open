package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.item.BaseGhostEyeItem;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class SilenceEffect extends StatusEffect {
   public SilenceEffect() {
      super(StatusEffectCategory.HARMFUL, 3355443);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (!entity.hasStatusEffect(ModEffects.PURIFICATION)) {
         if (!entity.getWorld().isClient() && entity.getWorld() instanceof ServerWorld serverWorld && entity.age % 5 == 0) {
            Vec3d pos = entity.getPos();

            for (int i = 0; i < 12; i++) {
               double offsetX = (entity.getRandom().nextDouble() - 0.5) * 1.5;
               double offsetY = entity.getRandom().nextDouble() * 2.0;
               double offsetZ = (entity.getRandom().nextDouble() - 0.5) * 1.5;
               serverWorld.spawnParticles(ParticleTypes.SMOKE, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 2, 0.0, 0.02, 0.0, 0.08);
            }
         }

         if (entity instanceof PlayerEntity player) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 255, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 20, 0, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20, 0, false, false, false));
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            spiritAttributes.putDouble("currentSpirit", 0.0);
            spiritAttributes.putDouble("spiritDamage", 0.0);
            spiritAttributes.putDouble("spiritResistance", 0.0);
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
            if (GhostDomainManager.isGhostDomainActive(player)) {
               GhostDomainManager.disableGhostDomain(player);
            }
         } else if (entity instanceof GhostEntity ghost) {
            ghost.setSuppressed(true);
            ghost.disableGhostDomain();
            ghost.disableKillingRules();
         } else if (entity instanceof GhostMasterEntity ghostMaster) {
            ghostMaster.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 255, false, false, false));
            ghostMaster.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 20, 0, false, false, false));
            ghostMaster.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20, 0, false, false, false));
            ghostMaster.setSuppressed(true);
         }
      }
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.onRemoved(entity, attributes, amplifier);
      if (entity instanceof PlayerEntity player) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         double originalSpiritDamage = this.calculateOriginalSpiritDamage(player);
         double originalSpiritResistance = this.calculateOriginalSpiritResistance(player);
         spiritAttributes.putDouble("spiritDamage", originalSpiritDamage);
         spiritAttributes.putDouble("spiritResistance", originalSpiritResistance);
         double maxSpirit = spiritAttributes.contains("maxSpirit") ? spiritAttributes.getDouble("maxSpirit") : 1000.0;
         spiritAttributes.putDouble("currentSpirit", maxSpirit);
         PlayerEvents.setSpiritAttributes(player, spiritAttributes);
      } else if (entity instanceof GhostEntity ghost) {
         ghost.setSuppressed(false);
         ghost.enableGhostDomain();
      } else if (entity instanceof GhostMasterEntity ghostMaster) {
         ghostMaster.setSuppressed(false);
         ghostMaster.setAiDisabled(false);
      }
   }

   private double calculateOriginalSpiritDamage(PlayerEntity player) {
      double damage = 0.0;

      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.isGhostSlotOccupied(player, i)) {
            ItemStack item = PlayerEvents.getGhostSlotItem(player, i);
            if (item.getItem() instanceof BaseGhostEyeItem ghostEyeItem) {
               damage += ghostEyeItem.getSpiritDamageBonus();
            }
         }
      }

      return damage;
   }

   private double calculateOriginalSpiritResistance(PlayerEntity player) {
      double resistance = 0.0;

      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.isGhostSlotOccupied(player, i)) {
            ItemStack item = PlayerEvents.getGhostSlotItem(player, i);
            if (item.getItem() instanceof BaseGhostEyeItem ghostEyeItem) {
               resistance += ghostEyeItem.getSpiritResistanceBonus();
            }
         }
      }

      return resistance;
   }
}
