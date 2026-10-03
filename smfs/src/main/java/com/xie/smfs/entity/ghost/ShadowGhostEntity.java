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
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 75000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0);
   }

   public ShadowGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1850, 180, 40, 0.12F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(75000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.3);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(6.0);
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
         return !this.isPlayerInRange(player) ? false : this.isPlayerInLowLight(player);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= 1024.0;
   }

   private boolean isPlayerInLowLight(PlayerEntity player) {
      BlockPos playerPos = player.getBlockPos();
      World world = player.getWorld();
      int lightLevel = world.getLightLevel(playerPos);
      return lightLevel < 9;
   }

   @Override
   public void tick() {
      super.tick();
      this.updateVisibilityBasedOnLight();
   }

   private void updateVisibilityBasedOnLight() {
      BlockPos pos = this.getBlockPos();
      World world = this.getWorld();
      int currentLight = world.getLightLevel(pos);
      boolean shouldBeVisible = currentLight >= 9;
      this.setVisible(shouldBeVisible);
   }

   @Override
   public boolean shouldAppearOnAttack() {
      return false;
   }
}
