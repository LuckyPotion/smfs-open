package com.xie.smfs.block.entity;

import com.xie.smfs.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Footprint2BlockEntity extends BlockEntity implements GeoBlockEntity {
   private static final String ENTITY_UUID_KEY = "EntityUUID";
   private String entityUUID = "";
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public Footprint2BlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.FOOTPRINT2_BLOCK_ENTITY, pos, state);
   }

   public String getEntityUUID() {
      return this.entityUUID;
   }

   public void setEntityUUID(String entityUUID) {
      this.entityUUID = entityUUID;
      this.method_5431();
   }

   public boolean hasValidEntityInfo() {
      return this.entityUUID != null && !this.entityUUID.isEmpty();
   }

   public LivingEntity getTargetEntity(World world) {
      if (!this.hasValidEntityInfo()) {
         return null;
      }

      try {
         return (LivingEntity)world.method_8390(
               LivingEntity.class,
               new Box(
                  this.field_11867.method_10263() - 1000,
                  this.field_11867.method_10264() - 1000,
                  this.field_11867.method_10260() - 1000,
                  this.field_11867.method_10263() + 1000,
                  this.field_11867.method_10264() + 1000,
                  this.field_11867.method_10260() + 1000
               ),
               entity -> entity.method_5845().equals(this.entityUUID)
            )
            .stream()
            .findFirst()
            .orElse(null);
      } catch (Exception e) {
         return null;
      }
   }

   public void method_11014(NbtCompound nbt) {
      super.method_11014(nbt);
      if (nbt.method_10545("EntityUUID")) {
         this.entityUUID = nbt.method_10558("EntityUUID");
      }
   }

   protected void method_11007(NbtCompound nbt) {
      super.method_11007(nbt);
      nbt.method_10582("EntityUUID", this.entityUUID);
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
