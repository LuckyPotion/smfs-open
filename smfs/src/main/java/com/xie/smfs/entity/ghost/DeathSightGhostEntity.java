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
         double distance = this.squaredDistanceTo(player);
         if (distance > 400.0) {
            return false;
         }

         Vec3d playerLookVec = player.getRotationVec(1.0F).normalize();
         Vec3d toGhostVec = new Vec3d(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ()).normalize();
         double dot = playerLookVec.dotProduct(toGhostVec);
         return dot < 0.5
            ? false
            : this.getWorld().raycast(new RaycastContext(player.getEyePos(), this.getEyePos(), ShapeType.COLLIDER, FluidHandling.NONE, this)).getType()
               == Type.MISS;
      } else {
         return false;
      }
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (!this.isDeadlocked()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 20;
            DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
            PlayerEvents.handleSpiritDamage(player, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, false, true));
         }
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient && !this.isDeadlocked() && !this.isSuppressed()) {
         this.getWorld().getPlayers().stream().filter(this::shouldAttackPlayer).forEach(this::executeAttack);
      }
   }
}
