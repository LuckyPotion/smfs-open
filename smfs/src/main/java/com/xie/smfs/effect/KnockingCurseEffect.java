package com.xie.smfs.effect;

import com.xie.smfs.entity.ghost.QiaomenGhostEntity;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModEntities;
import java.util.Random;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class KnockingCurseEffect extends StatusEffect implements ICurseEffect {
   private static final int CURSE_DURATION = 7200;
   private static final int GHOST_KNOCK_DURATION = 36000;
   private static final Random random = new Random();

   public KnockingCurseEffect() {
      super(StatusEffectCategory.HARMFUL, 4860970);
   }

   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return duration % 20 == 0 || duration == 1;
   }

   public void applyUpdateEffect(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.getWorld().isClient) {
         StatusEffectInstance effect = player.getStatusEffect(ModEffects.KNOCKING_CURSE);
         if (effect == null) {
            return;
         }

         int duration = effect.getDuration();
         if (duration == 1) {
            player.removeStatusEffect(ModEffects.KNOCKING_CURSE);
            player.addStatusEffect(new StatusEffectInstance(ModEffects.GHOST_KNOCK, 36000, 0));
            spawnQiaomenGhost(player.getWorld(), player.getBlockPos());
         }
      }
   }

   private static void spawnQiaomenGhost(World world, BlockPos pos) {
      if (!world.isClient) {
         boolean hasNearbyQiaomenGhost = world.getEntitiesByClass(QiaomenGhostEntity.class, new Box(pos).expand(50.0), entity -> true)
            .stream()
            .anyMatch(entity -> entity.getType() == ModEntities.QIAOMEN_GHOST);
         if (!hasNearbyQiaomenGhost) {
            QiaomenGhostEntity ghost = new QiaomenGhostEntity(ModEntities.QIAOMEN_GHOST, world);
            ghost.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            ghost.setPersistent();
            world.spawnEntity(ghost);
         }
      }
   }

   public String getTranslationKey() {
      return "effect.smfs.knocking_curse";
   }
}
