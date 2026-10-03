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
      super(StatusEffectCategory.field_18272, 4860970);
   }

   public boolean method_5552(int duration, int amplifier) {
      return duration % 20 == 0 || duration == 1;
   }

   public void method_5572(LivingEntity entity, int amplifier) {
      if (entity instanceof PlayerEntity player && !player.method_37908().field_9236) {
         StatusEffectInstance effect = player.method_6112(ModEffects.KNOCKING_CURSE);
         if (effect == null) {
            return;
         }

         int duration = effect.method_5584();
         if (duration == 1) {
            player.method_6016(ModEffects.KNOCKING_CURSE);
            player.method_6092(new StatusEffectInstance(ModEffects.GHOST_KNOCK, 36000, 0));
            spawnQiaomenGhost(player.method_37908(), player.method_24515());
         }
      }
   }

   private static void spawnQiaomenGhost(World world, BlockPos pos) {
      if (!world.field_9236) {
         boolean hasNearbyQiaomenGhost = world.method_8390(QiaomenGhostEntity.class, new Box(pos).method_1014(50.0), entity -> true)
            .stream()
            .anyMatch(entity -> entity.method_5864() == ModEntities.QIAOMEN_GHOST);
         if (!hasNearbyQiaomenGhost) {
            QiaomenGhostEntity ghost = new QiaomenGhostEntity(ModEntities.QIAOMEN_GHOST, world);
            ghost.method_5808(pos.method_10263() + 0.5, pos.method_10264(), pos.method_10260() + 0.5, 0.0F, 0.0F);
            ghost.method_5971();
            world.method_8649(ghost);
         }
      }
   }

   public String method_5567() {
      return "effect.smfs.knocking_curse";
   }
}
