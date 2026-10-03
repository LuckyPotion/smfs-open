package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.network.packets.common.s2c.GhostPressureDetectionPacket;
import com.xie.smfs.registry.ModEffects;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class GhostPressureEffect extends StatusEffect {
   public GhostPressureEffect() {
      super(StatusEffectCategory.field_18272, 3100495);
   }

   public String method_5567() {
      return "effect.smfs.ghost_pressure";
   }

   public boolean method_5552(int duration, int amplifier) {
      return duration % 20 == 0 || duration <= 1;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (!entity.method_37908().method_8608()) {
         StatusEffectInstance effect = entity.method_6112(this);
         if (effect != null && effect instanceof GhostPressureEffectInstance ghostPressureEffect) {
            UUID sourceUuid = ghostPressureEffect.getSourceUuid();
            if (sourceUuid != null) {
               ServerPlayerEntity sourcePlayer = entity.method_5682().method_3760().method_14602(sourceUuid);
               if (sourcePlayer != null) {
                  float threshold = 0.0F;
                  if (AdvancementManager.hasAdvancement(sourcePlayer, "smfs:become_aberration")) {
                     threshold = 0.2F;
                  } else if (AdvancementManager.hasAdvancement(sourcePlayer, "smfs:become_god")) {
                     threshold = 0.5F;
                  }

                  if (threshold > 0.0F && this.tryExecuteSpiritExecution(entity, threshold)) {
                     return;
                  }

                  if (effect.method_5584() > 1) {
                     NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(sourcePlayer);
                     float spiritDamage = spiritAttributes.method_10545("spiritDamage") ? (float)spiritAttributes.method_10574("spiritDamage") : 0.0F;
                     float tempSpiritDamage = spiritAttributes.method_10545("tempSpiritDamage")
                        ? (float)spiritAttributes.method_10574("tempSpiritDamage")
                        : 0.0F;
                     float tempSpiritDamageMultiplier = spiritAttributes.method_10545("tempSpiritDamageMultiplier")
                        ? (float)spiritAttributes.method_10574("tempSpiritDamageMultiplier")
                        : 1.0F;
                     float totalSpiritDamage = spiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
                     int revivalLevel = Math.max(1, Math.min(10, amplifier + 1));
                     float damageRatio = revivalLevel * 0.1F;
                     float damagePerTick = totalSpiritDamage * damageRatio;
                     if (damagePerTick > 0.0F) {
                        GhostDomainManager.executeSkillSpiritAttack(sourcePlayer, entity, damagePerTick);
                     }

                     if (!AdvancementManager.hasAdvancement(sourcePlayer, "smfs:become_aberration")) {
                        int slowLevel = Math.min(revivalLevel / 3, 3);
                        entity.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 40, slowLevel, false, false, true));
                     }
                  } else {
                     GhostDomainManager.executeSkillSpiritAttack(sourcePlayer, entity);
                  }
               } else if (entity.method_37908() instanceof ServerWorld serverWorld) {
                  Entity sourceEntity = serverWorld.method_14190(sourceUuid);
                  if (sourceEntity instanceof GhostEntity ghostSource) {
                     if (this.tryExecuteSpiritExecution(entity, 0.2F)) {
                        return;
                     }

                     if (effect.method_5584() > 1) {
                        int revivalLevel = Math.max(1, Math.min(10, amplifier + 1));
                        float damageRatio = revivalLevel * 0.1F;
                        float damagePerTick = ghostSource.getSpiritualDamage() * damageRatio;
                        if (damagePerTick > 0.0F) {
                           entity.method_5643(ghostSource.method_48923().method_48812(ghostSource), damagePerTick);
                        }
                     } else {
                        float endDamage = ghostSource.getSpiritualDamage() * 0.1F;
                        entity.method_5643(ghostSource.method_48923().method_48812(ghostSource), endDamage);
                     }
                  } else if (sourceEntity instanceof GhostMasterEntity masterSource) {
                     if (this.tryExecuteSpiritExecution(entity, 0.2F)) {
                        return;
                     }

                     if (effect.method_5584() > 1) {
                        int revivalLevel = Math.max(1, Math.min(10, amplifier + 1));
                        float damageRatio = revivalLevel * 0.1F;
                        float damagePerTick = masterSource.getSpiritualDamage() * damageRatio;
                        if (damagePerTick > 0.0F) {
                           entity.method_5643(masterSource.method_48923().method_48812(masterSource), damagePerTick);
                        }
                     } else {
                        float endDamage = masterSource.getSpiritualDamage() * 0.1F;
                        entity.method_5643(masterSource.method_48923().method_48812(masterSource), endDamage);
                     }
                  }
               }
            }
         }

         if (effect != null && effect.method_5584() <= 1) {
            entity.method_6016(this);
            GhostPressureDetectionPacket.sendToAllNearby(entity, false);
         }
      }
   }

   private boolean tryExecuteSpiritExecution(LivingEntity entity, float threshold) {
      int currentStrength = 0;
      int maxStrength = 0;
      if (entity instanceof GhostEntity ghost) {
         currentStrength = ghost.getSpiritualStrength();
         maxStrength = ghost.getMaxSpiritualStrength();
      } else {
         if (!(entity instanceof ServerPlayerEntity player)) {
            float currentHealth = entity.method_6032();
            float maxHealth = entity.method_6063();
            if (maxHealth > 0.0F && currentHealth > 0.0F && currentHealth / maxHealth <= threshold) {
               entity.method_6016(this);
               GhostPressureDetectionPacket.sendToAllNearby(entity, false);
               entity.method_5768();
               if (entity.method_37908() instanceof ServerWorld serverWorld) {
                  serverWorld.method_43128(
                     null, entity.method_23317(), entity.method_23318(), entity.method_23321(), SoundEvents.field_15136, SoundCategory.field_15251, 1.0F, 1.0F
                  );
               }

               return true;
            }

            return false;
         }

         currentStrength = (int)PlayerEvents.getCurrentSpirit(player);
         maxStrength = (int)PlayerEvents.getMaxSpirit(player);
      }

      if (maxStrength > 0 && currentStrength > 0 && (float)currentStrength / maxStrength <= threshold) {
         if (entity instanceof GhostEntity ghost) {
            ghost.setSpiritualStrength(0);
            if (!ghost.isDeadlocked()) {
               ghost.setDeadlocked(true);
            }
         } else if (entity instanceof ServerPlayerEntity player) {
            PlayerEvents.setCurrentSpirit(player, 0.0F);
         }

         if (entity.method_37908() instanceof ServerWorld serverWorld) {
            serverWorld.method_43128(
               null, entity.method_23317(), entity.method_23318(), entity.method_23321(), SoundEvents.field_15136, SoundCategory.field_15251, 1.0F, 1.0F
            );
         }

         entity.method_6016(this);
         GhostPressureDetectionPacket.sendToAllNearby(entity, false);
         return true;
      } else {
         return false;
      }
   }

   public static StatusEffectInstance createEffect(ServerPlayerEntity sourcePlayer, int duration, int level) {
      return new GhostPressureEffectInstance(ModEffects.GHOST_PRESSURE, duration, level - 1, false, true, true, sourcePlayer.method_5667());
   }

   public static StatusEffectInstance createEffect(LivingEntity sourceEntity, int duration, int level) {
      return new GhostPressureEffectInstance(ModEffects.GHOST_PRESSURE, duration, level - 1, false, true, true, sourceEntity.method_5667());
   }
}
