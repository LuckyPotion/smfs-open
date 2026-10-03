package com.xie.smfs.entity.other;

import com.xie.smfs.registry.ModBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GraveWarningEntity extends Entity {
   private static final int WARNING_DURATION = 40;
   private int warningTimer = 40;

   public GraveWarningEntity(EntityType<? extends GraveWarningEntity> entityType, World world) {
      super(entityType, world);
      this.method_5875(true);
      this.method_5684(true);
      this.method_5648(false);
      this.method_5803(true);
   }

   protected void method_5693() {
   }

   protected void method_5749(NbtCompound nbt) {
      this.warningTimer = nbt.method_10550("WarningTimer");
   }

   protected void method_5652(NbtCompound nbt) {
      nbt.method_10569("WarningTimer", this.warningTimer);
   }

   public void method_5773() {
      super.method_5773();
      if (this.method_37908().method_8608()) {
         double x = this.method_23317();
         double y = this.method_23318();
         double z = this.method_23321();
         this.method_37908().method_8406(ParticleTypes.field_11251, x, y + 0.3, z, 0.0, 0.02, 0.0);
      }

      if (!this.method_37908().method_8608()) {
         this.warningTimer--;
         if (this.warningTimer <= 0) {
            this.generateGrave();
            this.method_31472();
         }
      }
   }

   public int getWarningTimer() {
      return this.method_37908().method_8608() ? Math.max(0, 40 - this.field_6012) : this.warningTimer;
   }

   private void generateGrave() {
      double x = this.method_23317();
      double y = this.method_23318();
      double z = this.method_23321();
      int blockX = (int)Math.floor(x);
      int blockY = (int)Math.floor(y);
      int blockZ = (int)Math.floor(z);

      while (blockY > 0 && this.method_37908().method_8320(new BlockPos(blockX, blockY, blockZ)).method_26215()) {
         blockY--;
      }

      BlockPos gravePos = new BlockPos(blockX, blockY + 1, blockZ);
      if (this.method_37908().method_8320(gravePos).method_26215()
         && this.method_37908().method_8320(gravePos.method_10074()).method_26212(this.method_37908(), gravePos.method_10074())) {
         this.method_37908().method_8396(null, gravePos, SoundEvents.field_14609, SoundCategory.field_15251, 1.0F, 0.8F);
         this.method_37908().method_8501(gravePos, ModBlocks.GRAVE_MOUND.method_9564());
      }
   }

   public boolean method_5640(double distance) {
      return distance < 4096.0;
   }

   public boolean method_5767() {
      return false;
   }
}
