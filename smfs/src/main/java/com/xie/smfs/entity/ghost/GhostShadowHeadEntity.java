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
      return GhostEntity.createGhostAttributes().method_26868(EntityAttributes.field_23720, 0.28);
   }

   public GhostShadowHeadEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 24.0, 'C', 2200, 160, 30, 0.1F);
      this.method_5875(true);
      this.field_6207 = new FlightMoveControl(this, 10, true);
      this.field_6189 = new BirdNavigation(this, world);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(65000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.28);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(5.5);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else {
         return CoffinEffectManager.isPlayerInGoldCoffin(player) ? false : this.method_5858(player) <= 576.0;
      }
   }
}
