package com.xie.smfs.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface SpiritDamageCallback {
   void onSpiritDamage(PlayerEntity playerEntity, LivingEntity livingEntity, float f, float g, DamageSource damageSource);
}
