package com.xie.smfs.entity.other;

import com.xie.smfs.common.events.PlayerEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;

public class GoldenBulletEntity extends PersistentProjectileEntity {
   private boolean hitEntity = false;

   public GoldenBulletEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
      super(entityType, world);
      this.setNoGravity(true);
   }

   public GoldenBulletEntity(EntityType<? extends PersistentProjectileEntity> entityType, LivingEntity owner, World world) {
      super(entityType, owner, world);
      this.setNoGravity(true);
   }

   public GoldenBulletEntity(World world, LivingEntity owner) {
      super(EntityType.ARROW, owner, world);
      this.setNoGravity(true);
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      this.hitEntity = true;
      if (!this.getWorld().isClient && entityHitResult.getEntity() instanceof LivingEntity target) {
         float damage = 15.0F;
         if (target instanceof PlayerEntity player) {
            int ghostCount = PlayerEvents.countOccupiedGhostSlots(player);
            float damageMultiplier = 1.0F;
            switch (ghostCount) {
               case 1:
                  damageMultiplier = 0.8F;
                  break;
               case 2:
                  damageMultiplier = 0.7F;
                  break;
               case 3:
                  damageMultiplier = 0.6F;
                  break;
               case 4:
                  damageMultiplier = 0.5F;
                  break;
               case 5:
                  damageMultiplier = 0.3F;
                  break;
               case 6:
                  damageMultiplier = 0.1F;
            }

            damage *= damageMultiplier;
         }

         target.damage(this.getDamageSources().arrow(this, this.getOwner()), damage);
      }

      if (!this.isRemoved()) {
         this.discard();
      }
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      Type type = hitResult.getType();
      if (type == Type.ENTITY) {
         this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, hitResult.getPos(), Emitter.of(this, (BlockState)null));
      } else if (type == Type.BLOCK) {
         BlockHitResult blockHitResult = (BlockHitResult)hitResult;
         BlockPos blockPos = blockHitResult.getBlockPos();
         this.getWorld().emitGameEvent(GameEvent.PROJECTILE_LAND, blockPos, Emitter.of(this, this.getWorld().getBlockState(blockPos)));
      }
   }

   protected void onBlockHit(BlockHitResult blockHitResult) {
      super.onBlockHit(blockHitResult);
      if (!this.isRemoved()) {
         this.discard();
      }
   }

   public ItemStack asItemStack() {
      return new ItemStack(Items.GOLD_NUGGET);
   }
}
