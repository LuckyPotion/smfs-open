package com.xie.smfs.entity.ghost;

import com.xie.smfs.effect.GhostPressureEffect;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.item.WhiteGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostPressureEntity extends GhostEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger("GhostPressureEntity");
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 36.0;
   private static final char TERROR_LEVEL = 'C';
   private int pressureTicks = 0;
   private LivingEntity currentTarget = null;
   private boolean hasStartedChase = false;
   private int invisibleTicks = 0;

   public GhostPressureEntity(EntityType<GhostPressureEntity> entityType, World world) {
      super(entityType, world, true, 0, 36.0, 'C', 1500, 150, 80, 0.25F);
      this.ghostLevel = 1;
      this.attackCooldown = 0;
      this.initPressureAttributes();
   }

   private void initPressureAttributes() {
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(25.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.45);
      }
   }

   @Override
   protected void initGoals() {
      super.initGoals();
      this.goalSelector.add(1, new MeleeAttackGoal(this, 1.5, true));
      this.targetSelector
         .add(
            0,
            new ActiveTargetGoal(
               this,
               PlayerEntity.class,
               32,
               true,
               false,
               entity -> {
                  if (entity instanceof PlayerEntity player) {
                     if (!RedGhostCandleItem.isHoldingCandle(player)
                        && !CoffinEffectManager.isPlayerInGoldCoffin(player)
                        && !GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)
                        && !player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)) {
                        double distanceSq = this.squaredDistanceTo(player);
                        double trackRange = 32.0;
                        return distanceSq <= trackRange * trackRange;
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               }
            )
         );
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient() && this.isAlive()) {
         LivingEntity target = this.getTarget();
         if (target != null && target.isAlive() && !this.hasStartedChase) {
            this.hasStartedChase = true;
            this.getNavigation().setSpeed(1.5);
         }

         if (this.hasStartedChase && target != null && target.isAlive()) {
            double distance = this.distanceTo(target);
            if (target instanceof PlayerEntity player) {
               boolean var5 = WhiteGhostCandleItem.isHoldingWhiteCandle(player);
            }

            if (distance < 3.0) {
               this.hasStartedChase = false;
               if (target instanceof PlayerEntity player) {
                  player.addStatusEffect(GhostPressureEffect.createEffect(this, 100, 1));
                  player.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 5));
                  player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
                  player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 100, 0));
               }

               this.setVisible(false);
               this.setTarget(null);
               this.invisibleTicks = 100;
               return;
            }

            double distanceSq = this.squaredDistanceTo(target);
            if (distanceSq > 1024.0) {
               this.hasStartedChase = false;
               this.setTarget(null);
            }
         }

         if (this.hasStartedChase && (target == null || !target.isAlive())) {
            this.hasStartedChase = false;
            if (target != null && !target.isAlive()) {
               this.setVisible(true);
            }
         }

         if (!this.isVisible() && target != null && !target.isAlive()) {
            this.setVisible(true);
            this.hasStartedChase = false;
            this.invisibleTicks = 0;
         }

         if (this.invisibleTicks > 0) {
            this.invisibleTicks--;
            if (this.invisibleTicks <= 0) {
               this.setVisible(true);
               this.invisibleTicks = 0;
               this.hasStartedChase = false;
               List<PlayerEntity> nearbyPlayers = this.getWorld()
                  .getEntitiesByClass(
                     PlayerEntity.class,
                     this.getBoundingBox().expand(32.0),
                     playerx -> !RedGhostCandleItem.isHoldingCandle(playerx)
                        && !CoffinEffectManager.isPlayerInGoldCoffin(playerx)
                        && !GoldBlockProtectionManager.isPlayerInGoldBlockShelter(playerx)
                        && !playerx.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)
                  );
               if (!nearbyPlayers.isEmpty()) {
                  PlayerEntity nearestPlayer = nearbyPlayers.get(0);
                  double minDistance = this.distanceTo(nearestPlayer);

                  for (PlayerEntity player : nearbyPlayers) {
                     double distance = this.distanceTo(player);
                     if (distance < minDistance) {
                        minDistance = distance;
                        nearestPlayer = player;
                     }
                  }

                  this.setTarget(nearestPlayer);
                  this.hasStartedChase = true;
                  this.getNavigation().setSpeed(1.5);
               }
            }
         }
      }
   }
}
