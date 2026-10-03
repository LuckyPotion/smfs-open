package com.xie.smfs.effect;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackGhostDomainEffect extends StatusEffect implements ICurseEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/BlackGhostDomainEffect");

   public BlackGhostDomainEffect() {
      super(StatusEffectCategory.field_18272, 0);
   }

   public boolean method_5552(int duration, int amplifier) {
      return true;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player) {
         boolean isTargetVersion = player.method_6059(ModEffects.BLACK_GHOST_DOMAIN_TARGET);
         if (!isTargetVersion) {
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5925, 5, 0, false, false, false));
         }
      }

      if (entity instanceof PlayerEntity player && !player.method_37908().method_8608() && player instanceof ServerPlayerEntity serverPlayer) {
         int blockGhostSlot = PlayerEvents.findEquippedBlockGhostSlot(player);
         if (blockGhostSlot != -1) {
            GhostDomainManager.markPlayerForGhost(serverPlayer, "方块鬼", "放置方块行为");
         }

         int foodGhostSlot = PlayerEvents.findEquippedFoodGhostSlot(player);
         if (foodGhostSlot != -1) {
            GhostDomainManager.markPlayerForGhost(serverPlayer, "食物鬼", "进食行为");
         }

         int qiaomenGhostSlot = PlayerEvents.findEquippedQiaomenGhostSlot(player);
         if (qiaomenGhostSlot != -1) {
            GhostDomainManager.markPlayerForGhost(serverPlayer, "敲门鬼", "开门行为");
         }

         int ghostOfficerSlot = PlayerEvents.findEquippedGhostOfficerSlot(player);
         if (ghostOfficerSlot != -1) {
            GhostDomainManager.markPlayerForGhost(serverPlayer, "鬼差", "使用技能行为");
         }
      }
   }

   public static void updateRevivalDegreeInGhostDomain(PlayerEntity player) {
      if (!player.method_37908().method_8608()) {
         PlayerEvents.balanceRevivalDegree(player, 3, 1);
      }
   }

   public void method_5562(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      super.method_5562(entity, attributes, amplifier);
   }
}
