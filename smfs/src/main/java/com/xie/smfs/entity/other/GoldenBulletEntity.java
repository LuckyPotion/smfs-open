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
      this.method_5875(true);
   }

   public GoldenBulletEntity(EntityType<? extends PersistentProjectileEntity> entityType, LivingEntity owner, World world) {
      super(entityType, owner, world);
      this.method_5875(true);
   }

   public GoldenBulletEntity(World world, LivingEntity owner) {
      super(EntityType.field_6122, owner, world);
      this.method_5875(true);
   }

   protected void method_7454(EntityHitResult entityHitResult) {
      super.method_7454(entityHitResult);
      this.hitEntity = true;
      if (!this.method_37908().field_9236 && entityHitResult.method_17782() instanceof LivingEntity target) {
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

         target.method_5643(this.method_48923().method_48803(this, this.method_24921()), damage);
      }

      if (!this.method_31481()) {
         this.method_31472();
      }
   }

   protected void method_7488(HitResult hitResult) {
      super.method_7488(hitResult);
      Type type = hitResult.method_17783();
      if (type == Type.field_1331) {
         this.method_37908().method_32888(GameEvent.field_28162, hitResult.method_17784(), Emitter.method_43286(this, (BlockState)null));
      } else if (type == Type.field_1332) {
         BlockHitResult blockHitResult = (BlockHitResult)hitResult;
         BlockPos blockPos = blockHitResult.method_17777();
         this.method_37908().method_43276(GameEvent.field_28162, blockPos, Emitter.method_43286(this, this.method_37908().method_8320(blockPos)));
      }
   }

   protected void method_24920(BlockHitResult blockHitResult) {
      super.method_24920(blockHitResult);
      if (!this.method_31481()) {
         this.method_31472();
      }
   }

   public ItemStack method_7445() {
      return new ItemStack(Items.field_8397);
   }
}
