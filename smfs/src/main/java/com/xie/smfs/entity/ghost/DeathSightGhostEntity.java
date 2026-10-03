package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class DeathSightGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = false;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 0.0;
   private static final char TERROR_LEVEL = 'B';

   public DeathSightGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 0.0, 'B', 1500, 100, 60, 0.3F);
      this.ghostLevel = 0;
      this.setEnableChaseAfterRule(false);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (!this.isSuppressed() && !this.isDeadlocked() && !RedGhostCandleItem.isHoldingCandle(player)) {
         double distance = this.method_5858(player);
         if (distance > 400.0) {
            return false;
         }

         Vec3d playerLookVec = player.method_5828(1.0F).method_1029();
         Vec3d toGhostVec = new Vec3d(
               this.method_23317() - player.method_23317(), this.method_23320() - player.method_23320(), this.method_23321() - player.method_23321()
            )
            .method_1029();
         double dot = playerLookVec.method_1026(toGhostVec);
         return dot < 0.5
            ? false
            : this.method_37908()
                  .method_17742(new RaycastContext(player.method_33571(), this.method_33571(), ShapeType.field_17558, FluidHandling.field_1348, this))
                  .method_17783()
               == Type.field_1333;
      } else {
         return false;
      }
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 20;
            DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
            PlayerEvents.handleSpiritDamage(player, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
            player.method_6092(new StatusEffectInstance(StatusEffects.field_5919, 60, 0, false, false, true));
         }
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236 && !this.isDeadlocked() && !this.isSuppressed()) {
         this.method_37908().method_18456().stream().filter(this::shouldAttackPlayer).forEach(this::executeAttack);
      }
   }
}
