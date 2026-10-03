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
      this.setNoGravity(true);
      this.setInvulnerable(true);
      this.setInvisible(false);
      this.setSilent(true);
   }

   protected void initDataTracker() {
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.warningTimer = nbt.getInt("WarningTimer");
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putInt("WarningTimer", this.warningTimer);
   }

   public void tick() {
      super.tick();
      if (this.getWorld().isClient()) {
         double x = this.getX();
         double y = this.getY();
         double z = this.getZ();
         this.getWorld().addParticle(ParticleTypes.SMOKE, x, y + 0.3, z, 0.0, 0.02, 0.0);
      }

      if (!this.getWorld().isClient()) {
         this.warningTimer--;
         if (this.warningTimer <= 0) {
            this.generateGrave();
            this.discard();
         }
      }
   }

   public int getWarningTimer() {
      return this.getWorld().isClient() ? Math.max(0, 40 - this.age) : this.warningTimer;
   }

   private void generateGrave() {
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      int blockX = (int)Math.floor(x);
      int blockY = (int)Math.floor(y);
      int blockZ = (int)Math.floor(z);

      while (blockY > 0 && this.getWorld().getBlockState(new BlockPos(blockX, blockY, blockZ)).isAir()) {
         blockY--;
      }

      BlockPos gravePos = new BlockPos(blockX, blockY + 1, blockZ);
      if (this.getWorld().getBlockState(gravePos).isAir() && this.getWorld().getBlockState(gravePos.down()).isSolidBlock(this.getWorld(), gravePos.down())) {
         this.getWorld().playSound(null, gravePos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.HOSTILE, 1.0F, 0.8F);
         this.getWorld().setBlockState(gravePos, ModBlocks.GRAVE_MOUND.getDefaultState());
      }
   }

   public boolean shouldRender(double distance) {
      return distance < 4096.0;
   }

   public boolean isInvisible() {
      return false;
   }
}
