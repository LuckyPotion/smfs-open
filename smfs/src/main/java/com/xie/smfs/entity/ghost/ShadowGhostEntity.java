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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShadowGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';
   private static final int LIGHT_THRESHOLD = 9;

   public static Builder createLivingAttributes() {
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 75000.0)
         .method_26868(EntityAttributes.field_23719, 0.3)
         .method_26868(EntityAttributes.field_23721, 6.0);
   }

   public ShadowGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1850, 180, 40, 0.12F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(75000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.3);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(6.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.method_6059(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : this.isPlayerInLowLight(player);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.method_5858(player) <= 1024.0;
   }

   private boolean isPlayerInLowLight(PlayerEntity player) {
      BlockPos playerPos = player.method_24515();
      World world = player.method_37908();
      int lightLevel = world.method_22339(playerPos);
      return lightLevel < 9;
   }

   @Override
   public void method_5773() {
      super.method_5773();
      this.updateVisibilityBasedOnLight();
   }

   private void updateVisibilityBasedOnLight() {
      BlockPos pos = this.method_24515();
      World world = this.method_37908();
      int currentLight = world.method_22339(pos);
      boolean shouldBeVisible = currentLight >= 9;
      this.setVisible(shouldBeVisible);
   }

   @Override
   public boolean shouldAppearOnAttack() {
      return false;
   }
}
