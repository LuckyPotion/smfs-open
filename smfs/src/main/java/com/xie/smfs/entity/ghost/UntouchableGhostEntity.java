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
   public boolean method_5643(DamageSource source, float amount) {
      if (source.method_5529() instanceof PlayerEntity player) {
         player.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 2400, 19, false, false, true));
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5916, 200, 254, false, false, true));
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5919, 300, 254, false, false, true));
      }

      return super.method_5643(source, amount);
   }

   @Override
   public boolean isResentmentSystemDisabled() {
      return true;
   }
}
