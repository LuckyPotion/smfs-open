package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class GhostShadowHeadEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = false;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 24.0;
   private static final char TERROR_LEVEL = 'C';

   public static Builder createLivingAttributes() {
      return GhostEntity.createGhostAttributes().add(EntityAttributes.GENERIC_FLYING_SPEED, 0.28);
   }

   public GhostShadowHeadEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 24.0, 'C', 2200, 160, 30, 0.1F);
      this.setNoGravity(true);
      this.moveControl = new FlightMoveControl(this, 10, true);
      this.navigation = new BirdNavigation(this, world);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(65000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.28);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(5.5);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else {
         return CoffinEffectManager.isPlayerInGoldCoffin(player) ? false : this.squaredDistanceTo(player) <= 576.0;
      }
   }
}
