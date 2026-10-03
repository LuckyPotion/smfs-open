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
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(25.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.45);
      }
   }

   @Override
   protected void method_5959() {
      super.method_5959();
      this.field_6201.method_6277(1, new MeleeAttackGoal(this, 1.5, true));
      this.field_6185
         .method_6277(
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
                        && !player.method_6059(ModEffects.SPIRIT_IMMUNITY)) {
                        double distanceSq = this.method_5858(player);
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
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().method_8608() && this.method_5805()) {
         LivingEntity target = this.method_5968();
         if (target != null && target.method_5805() && !this.hasStartedChase) {
            this.hasStartedChase = true;
            this.method_5942().method_6344(1.5);
         }

         if (this.hasStartedChase && target != null && target.method_5805()) {
            double distance = this.method_5739(target);
            if (target instanceof PlayerEntity player) {
               boolean var5 = WhiteGhostCandleItem.isHoldingWhiteCandle(player);
            }

            if (distance < 3.0) {
               this.hasStartedChase = false;
               if (target instanceof PlayerEntity player) {
                  player.method_6092(GhostPressureEffect.createEffect(this, 100, 1));
                  player.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 100, 5));
                  player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 100, 3));
                  player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, 100, 0));
               }

               this.setVisible(false);
               this.method_5980(null);
               this.invisibleTicks = 100;
               return;
            }

            double distanceSq = this.method_5858(target);
            if (distanceSq > 1024.0) {
               this.hasStartedChase = false;
               this.method_5980(null);
            }
         }

         if (this.hasStartedChase && (target == null || !target.method_5805())) {
            this.hasStartedChase = false;
            if (target != null && !target.method_5805()) {
               this.setVisible(true);
            }
         }

         if (!this.isVisible() && target != null && !target.method_5805()) {
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
               List<PlayerEntity> nearbyPlayers = this.method_37908()
                  .method_8390(
                     PlayerEntity.class,
                     this.method_5829().method_1014(32.0),
                     playerx -> !RedGhostCandleItem.isHoldingCandle(playerx)
                        && !CoffinEffectManager.isPlayerInGoldCoffin(playerx)
                        && !GoldBlockProtectionManager.isPlayerInGoldBlockShelter(playerx)
                        && !playerx.method_6059(ModEffects.SPIRIT_IMMUNITY)
                  );
               if (!nearbyPlayers.isEmpty()) {
                  PlayerEntity nearestPlayer = nearbyPlayers.get(0);
                  double minDistance = this.method_5739(nearestPlayer);

                  for (PlayerEntity player : nearbyPlayers) {
                     double distance = this.method_5739(player);
                     if (distance < minDistance) {
                        minDistance = distance;
                        nearestPlayer = player;
                     }
                  }

                  this.method_5980(nearestPlayer);
                  this.hasStartedChase = true;
                  this.method_5942().method_6344(1.5);
               }
            }
         }
      }
   }
}
