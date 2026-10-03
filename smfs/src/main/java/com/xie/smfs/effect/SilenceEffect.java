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
      super(StatusEffectCategory.field_18272, 3355443);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (!entity.method_6059(ModEffects.PURIFICATION)) {
         if (!entity.method_37908().method_8608() && entity.method_37908() instanceof ServerWorld serverWorld && entity.field_6012 % 5 == 0) {
            Vec3d pos = entity.method_19538();

            for (int i = 0; i < 12; i++) {
               double offsetX = (entity.method_6051().method_43058() - 0.5) * 1.5;
               double offsetY = entity.method_6051().method_43058() * 2.0;
               double offsetZ = (entity.method_6051().method_43058() - 0.5) * 1.5;
               serverWorld.method_14199(
                  ParticleTypes.field_11251, pos.field_1352 + offsetX, pos.field_1351 + offsetY, pos.field_1350 + offsetZ, 2, 0.0, 0.02, 0.0, 0.08
               );
            }
         }

         if (entity instanceof PlayerEntity player) {
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 20, 255, false, false, false));
            player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 20, 0, false, false, false));
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5916, 20, 0, false, false, false));
            NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
            spiritAttributes.method_10549("currentSpirit", 0.0);
            spiritAttributes.method_10549("spiritDamage", 0.0);
            spiritAttributes.method_10549("spiritResistance", 0.0);
            PlayerEvents.setSpiritAttributes(player, spiritAttributes);
            if (GhostDomainManager.isGhostDomainActive(player)) {
               GhostDomainManager.disableGhostDomain(player);
            }
         } else if (entity instanceof GhostEntity ghost) {
            ghost.setSuppressed(true);
            ghost.disableGhostDomain();
            ghost.disableKillingRules();
         } else if (entity instanceof GhostMasterEntity ghostMaster) {
            ghostMaster.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 20, 255, false, false, false));
            ghostMaster.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 20, 0, false, false, false));
            ghostMaster.method_6092(new StatusEffectInstance(StatusEffects.field_5916, 20, 0, false, false, false));
            ghostMaster.setSuppressed(true);
         }
      }
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.method_5562(entity, attributes, amplifier);
      if (entity instanceof PlayerEntity player) {
         NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(player);
         double originalSpiritDamage = this.calculateOriginalSpiritDamage(player);
         double originalSpiritResistance = this.calculateOriginalSpiritResistance(player);
         spiritAttributes.method_10549("spiritDamage", originalSpiritDamage);
         spiritAttributes.method_10549("spiritResistance", originalSpiritResistance);
         double maxSpirit = spiritAttributes.method_10545("maxSpirit") ? spiritAttributes.method_10574("maxSpirit") : 1000.0;
         spiritAttributes.method_10549("currentSpirit", maxSpirit);
         PlayerEvents.setSpiritAttributes(player, spiritAttributes);
      } else if (entity instanceof GhostEntity ghost) {
         ghost.setSuppressed(false);
         ghost.enableGhostDomain();
      } else if (entity instanceof GhostMasterEntity ghostMaster) {
         ghostMaster.setSuppressed(false);
         ghostMaster.method_5977(false);
      }
   }

   private double calculateOriginalSpiritDamage(PlayerEntity player) {
      double damage = 0.0;

      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.isGhostSlotOccupied(player, i)) {
            ItemStack item = PlayerEvents.getGhostSlotItem(player, i);
            if (item.method_7909() instanceof BaseGhostEyeItem ghostEyeItem) {
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
            if (item.method_7909() instanceof BaseGhostEyeItem ghostEyeItem) {
               resistance += ghostEyeItem.getSpiritResistanceBonus();
            }
         }
      }

      return resistance;
   }
}
