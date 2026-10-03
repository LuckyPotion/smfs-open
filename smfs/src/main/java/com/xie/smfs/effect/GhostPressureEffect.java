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
      super(StatusEffectCategory.HARMFUL, 3100495);
   }

   public String getTranslationKey() {
      return "effect.smfs.ghost_pressure";
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return duration % 20 == 0 || duration <= 1;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (!entity.getWorld().isClient()) {
         StatusEffectInstance effect = entity.getStatusEffect(this);
         if (effect != null && effect instanceof GhostPressureEffectInstance ghostPressureEffect) {
            UUID sourceUuid = ghostPressureEffect.getSourceUuid();
            if (sourceUuid != null) {
               ServerPlayerEntity sourcePlayer = entity.getServer().getPlayerManager().getPlayer(sourceUuid);
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

                  if (effect.getDuration() > 1) {
                     NbtCompound spiritAttributes = PlayerEvents.getSpiritAttributes(sourcePlayer);
                     float spiritDamage = spiritAttributes.contains("spiritDamage") ? (float)spiritAttributes.getDouble("spiritDamage") : 0.0F;
                     float tempSpiritDamage = spiritAttributes.contains("tempSpiritDamage") ? (float)spiritAttributes.getDouble("tempSpiritDamage") : 0.0F;
                     float tempSpiritDamageMultiplier = spiritAttributes.contains("tempSpiritDamageMultiplier")
                        ? (float)spiritAttributes.getDouble("tempSpiritDamageMultiplier")
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
                        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, slowLevel, false, false, true));
                     }
                  } else {
                     GhostDomainManager.executeSkillSpiritAttack(sourcePlayer, entity);
                  }
               } else if (entity.getWorld() instanceof ServerWorld serverWorld) {
                  Entity sourceEntity = serverWorld.getEntity(sourceUuid);
                  if (sourceEntity instanceof GhostEntity ghostSource) {
                     if (this.tryExecuteSpiritExecution(entity, 0.2F)) {
                        return;
                     }

                     if (effect.getDuration() > 1) {
                        int revivalLevel = Math.max(1, Math.min(10, amplifier + 1));
                        float damageRatio = revivalLevel * 0.1F;
                        float damagePerTick = ghostSource.getSpiritualDamage() * damageRatio;
                        if (damagePerTick > 0.0F) {
                           entity.damage(ghostSource.getDamageSources().mobAttack(ghostSource), damagePerTick);
                        }
                     } else {
                        float endDamage = ghostSource.getSpiritualDamage() * 0.1F;
                        entity.damage(ghostSource.getDamageSources().mobAttack(ghostSource), endDamage);
                     }
                  } else if (sourceEntity instanceof GhostMasterEntity masterSource) {
                     if (this.tryExecuteSpiritExecution(entity, 0.2F)) {
                        return;
                     }

                     if (effect.getDuration() > 1) {
                        int revivalLevel = Math.max(1, Math.min(10, amplifier + 1));
                        float damageRatio = revivalLevel * 0.1F;
                        float damagePerTick = masterSource.getSpiritualDamage() * damageRatio;
                        if (damagePerTick > 0.0F) {
                           entity.damage(masterSource.getDamageSources().mobAttack(masterSource), damagePerTick);
                        }
                     } else {
                        float endDamage = masterSource.getSpiritualDamage() * 0.1F;
                        entity.damage(masterSource.getDamageSources().mobAttack(masterSource), endDamage);
                     }
                  }
               }
            }
         }

         if (effect != null && effect.getDuration() <= 1) {
            entity.removeStatusEffect(this);
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
            float currentHealth = entity.getHealth();
            float maxHealth = entity.getMaxHealth();
            if (maxHealth > 0.0F && currentHealth > 0.0F && currentHealth / maxHealth <= threshold) {
               entity.removeStatusEffect(this);
               GhostPressureDetectionPacket.sendToAllNearby(entity, false);
               entity.kill();
               if (entity.getWorld() instanceof ServerWorld serverWorld) {
                  serverWorld.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.HOSTILE, 1.0F, 1.0F);
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

         if (entity.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.HOSTILE, 1.0F, 1.0F);
         }

         entity.removeStatusEffect(this);
         GhostPressureDetectionPacket.sendToAllNearby(entity, false);
         return true;
      } else {
         return false;
      }
   }

   public static StatusEffectInstance createEffect(ServerPlayerEntity sourcePlayer, int duration, int level) {
      return new GhostPressureEffectInstance(ModEffects.GHOST_PRESSURE, duration, level - 1, false, true, true, sourcePlayer.getUuid());
   }

   public static StatusEffectInstance createEffect(LivingEntity sourceEntity, int duration, int level) {
      return new GhostPressureEffectInstance(ModEffects.GHOST_PRESSURE, duration, level - 1, false, true, true, sourceEntity.getUuid());
   }
}
