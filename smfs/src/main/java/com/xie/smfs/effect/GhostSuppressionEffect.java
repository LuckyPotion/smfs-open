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
      super(StatusEffectCategory.field_18271, 9127187);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
   }

   public void method_5555(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.method_37908().method_8608()) {
         this.reduceAllGhostRevivalDegree(player, 600);
         LOGGER.info("玩家 {} 获得鬼中药压制效果，所有鬼复苏程度减少600点", player.method_5477().getString());
      }

      super.method_5555(entity, attributes, amplifier);
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof PlayerEntity player && !entity.method_37908().method_8608()) {
         if (SAFE_REMOVAL_PLAYERS.contains(player.method_5667())) {
            SAFE_REMOVAL_PLAYERS.remove(player.method_5667());
            LOGGER.info("玩家 {} 安全移除了鬼中药压制效果，未触发副作用", player.method_5477().getString());
         } else {
            this.increaseAllGhostRevivalDegree(player, 800);
            LOGGER.info("玩家 {} 鬼中药压制效果结束，所有槽位复苏程度增加800点", player.method_5477().getString());
         }
      }

      super.method_5562(entity, attributes, amplifier);
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
      if (player != null && !player.method_37908().method_8608() && player.method_6059(ModEffects.GHOST_SUPPRESSION)) {
         SAFE_REMOVAL_PLAYERS.add(player.method_5667());
         player.method_6016(ModEffects.GHOST_SUPPRESSION);
      }
   }
}
