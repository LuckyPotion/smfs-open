package com.xie.smfs.block.entity;

import com.xie.smfs.block.FootprintBlock;
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

public class FootprintBlockEntity extends BlockEntity implements GeoBlockEntity {
   private String entityUUID;
   private int decayTime = 240;
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public FootprintBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.FOOTPRINT_BLOCK_ENTITY, pos, state);
   }

   public int getDecayTime() {
      return this.decayTime;
   }

   public void setDecayTime(int decayTime) {
      this.decayTime = FootprintBlock.clampDecayTime(decayTime);
      this.markDirty();
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public String getEntityUUID() {
      return this.entityUUID;
   }

   public void setEntityUUID(String entityUUID) {
      this.entityUUID = entityUUID;
      this.markDirty();
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("entityUUID")) {
         this.entityUUID = nbt.getString("entityUUID");
      }

      if (nbt.contains("decayTime")) {
         this.decayTime = nbt.getInt("decayTime");
      }
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.entityUUID != null) {
         nbt.putString("entityUUID", this.entityUUID);
      }

      nbt.putInt("decayTime", this.decayTime);
   }

   public boolean hasValidEntityInfo() {
      return this.entityUUID != null && !this.entityUUID.isEmpty();
   }

   public LivingEntity getTargetEntity(World world) {
      if (!this.hasValidEntityInfo()) {
         return null;
      }

      try {
         return (LivingEntity)world.getEntitiesByClass(
               LivingEntity.class,
               new Box(
                  this.pos.getX() - 1000,
                  this.pos.getY() - 1000,
                  this.pos.getZ() - 1000,
                  this.pos.getX() + 1000,
                  this.pos.getY() + 1000,
                  this.pos.getZ() + 1000
               ),
               entity -> entity.getUuidAsString().equals(this.entityUUID)
            )
            .stream()
            .findFirst()
            .orElse(null);
      } catch (Exception e) {
         return null;
      }
   }
}
