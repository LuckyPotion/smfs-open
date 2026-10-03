package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.registry.ModEffects;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostSuppressionEffect extends StatusEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostSuppressionEffect.class);
   private static final Set<UUID> SAFE_REMOVAL_PLAYERS = new HashSet<>();

   public GhostSuppressionEffect() {
      super(StatusEffectCategory.BENEFICIAL, 9127187);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }

   public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.getWorld().isClient()) {
         this.reduceAllGhostRevivalDegree(player, 600);
         LOGGER.info("玩家 {} 获得鬼中药压制效果，所有鬼复苏程度减少600点", player.getName().getString());
      }

      super.onApplied(entity, attributes, amplifier);
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.getWorld().isClient()) {
         if (SAFE_REMOVAL_PLAYERS.contains(player.getUuid())) {
            SAFE_REMOVAL_PLAYERS.remove(player.getUuid());
            LOGGER.info("玩家 {} 安全移除了鬼中药压制效果，未触发副作用", player.getName().getString());
         } else {
            this.increaseAllGhostRevivalDegree(player, 800);
            LOGGER.info("玩家 {} 鬼中药压制效果结束，所有槽位复苏程度增加800点", player.getName().getString());
         }
      }

      super.onRemoved(entity, attributes, amplifier);
   }

   private void reduceAllGhostRevivalDegree(PlayerEntity player, int amount) {
      for (int i = 0; i < 10; i++) {
         int currentDegree = PlayerEvents.getGhostSlotRevivalDegree(player, i);
         if (currentDegree > 0) {
            int newDegree = Math.max(0, currentDegree - amount);
            PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", newDegree);
         }
      }
   }

   private void increaseAllGhostRevivalDegree(PlayerEntity player, int amount) {
      for (int i = 0; i < 10; i++) {
         if (PlayerEvents.isGhostSlotOccupied(player, i)) {
            int currentDegree = PlayerEvents.getGhostSlotRevivalDegree(player, i);
            int requiredDegree = 1000;
            int newDegree = Math.min(requiredDegree, currentDegree + amount);
            PlayerEvents.updateGhostSlotValue(player, i, "revivalDegree", newDegree);
         }
      }
   }

   public static void safelyRemoveEffect(PlayerEntity player) {
      if (player != null && !player.getWorld().isClient() && player.hasStatusEffect(ModEffects.GHOST_SUPPRESSION)) {
         SAFE_REMOVAL_PLAYERS.add(player.getUuid());
         player.removeStatusEffect(ModEffects.GHOST_SUPPRESSION);
      }
   }
}
