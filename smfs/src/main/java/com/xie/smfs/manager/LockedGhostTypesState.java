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
      this.method_80();
   }

   public NbtCompound method_75(NbtCompound nbt) {
      if (this.persistentData != null) {
         nbt.method_10543(this.persistentData);
      }

      return nbt;
   }

   public boolean method_79() {
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
