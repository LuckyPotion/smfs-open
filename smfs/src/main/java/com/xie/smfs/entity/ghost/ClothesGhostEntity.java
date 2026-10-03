package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModEffects;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ClothesGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 75000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
   }

   public ClothesGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1750, 165, 38, 0.13F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(75000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.25);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(5.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : !this.hasAnyArmor(player);
      }
   }

   private boolean hasAnyArmor(PlayerEntity player) {
      return !player.getInventory().getArmorStack(0).isEmpty()
         || !player.getInventory().getArmorStack(1).isEmpty()
         || !player.getInventory().getArmorStack(2).isEmpty()
         || !player.getInventory().getArmorStack(3).isEmpty();
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= 1024.0;
   }
}
