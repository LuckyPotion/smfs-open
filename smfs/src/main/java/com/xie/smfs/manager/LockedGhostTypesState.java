package com.xie.smfs.manager;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

public class LockedGhostTypesState extends PersistentState {
   private NbtCompound persistentData;

   public LockedGhostTypesState() {
      this.persistentData = new NbtCompound();
   }

   public LockedGhostTypesState(NbtCompound nbt) {
      this.persistentData = nbt != null ? nbt : new NbtCompound();
   }

   public NbtCompound getPersistentData() {
      return this.persistentData;
   }

   public void setPersistentData(NbtCompound persistentData) {
      this.persistentData = persistentData;
      this.markDirty();
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      if (this.persistentData != null) {
         nbt.copyFrom(this.persistentData);
      }

      return nbt;
   }

   public boolean isDirty() {
      return true;
   }

   public static LockedGhostTypesState fromNbt(NbtCompound nbt) {
      LockedGhostTypesState state = new LockedGhostTypesState();
      if (nbt != null) {
         state.persistentData = nbt;
      }

      return state;
   }
}
