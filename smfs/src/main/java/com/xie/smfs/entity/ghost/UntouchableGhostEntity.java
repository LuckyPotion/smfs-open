package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class UntouchableGhostEntity extends GhostEntity {
   public UntouchableGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 32.0, 'B', 1800, 180, 90, 0.2F);
      this.ghostLevel = 2;
   }

   @Override
   public boolean damage(DamageSource source, float amount) {
      if (source.getAttacker() instanceof PlayerEntity player) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 2400, 19, false, false, true));
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 254, false, false, true));
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 300, 254, false, false, true));
      }

      return super.damage(source, amount);
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }
}
