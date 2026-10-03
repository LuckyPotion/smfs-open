package com.xie.smfs.data;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RoyalCurseData {
   private List<RoyalCurseServantData> servants = new ArrayList<>();

   public RoyalCurseData() {
   }

   public RoyalCurseData(NbtCompound nbt) {
      if (nbt.method_10545("servants")) {
         NbtList servantsList = nbt.method_10554("servants", 10);

         for (int i = 0; i < servantsList.size(); i++) {
            NbtCompound servantNbt = servantsList.method_10602(i);
            RoyalCurseServantData servantData = new RoyalCurseServantData(servantNbt);
            this.servants.add(servantData);
         }
      }
   }

   public NbtCompound toNbt() {
      NbtCompound nbt = new NbtCompound();
      NbtList servantsList = new NbtList();

      for (RoyalCurseServantData servant : this.servants) {
         servantsList.add(servant.toNbt());
      }

      nbt.method_10566("servants", servantsList);
      return nbt;
   }

   public List<RoyalCurseServantData> getServants() {
      return this.servants;
   }

   public void addServant(RoyalCurseServantData servant) {
      this.servants.add(servant);
   }

   public RoyalCurseServantData getServant(int index) {
      return index >= 0 && index < this.servants.size() ? this.servants.get(index) : null;
   }

   public int getServantCount() {
      return this.servants.size();
   }

   public void removeServant(int index) {
      if (index >= 0 && index < this.servants.size()) {
         this.servants.remove(index);
      }
   }
}
