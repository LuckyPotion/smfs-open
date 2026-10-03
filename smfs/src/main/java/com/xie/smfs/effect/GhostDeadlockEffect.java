package com.xie.smfs.effect;

import com.xie.smfs.network.packets.ui.s2c.OpenGhostDeadlockSelectS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostDeadlockEffect extends StatusEffect {
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostDeadlockEffect.class);

   public GhostDeadlockEffect() {
      super(StatusEffectCategory.BENEFICIAL, 4915330);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
   }

   public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
      if (entity instanceof ServerPlayerEntity player && !entity.getWorld().isClient()) {
         ServerPlayNetworking.send(player, OpenGhostDeadlockSelectS2CPacket.ID, PacketByteBufs.empty());
      }

      super.onRemoved(entity, attributes, amplifier);
   }
}
