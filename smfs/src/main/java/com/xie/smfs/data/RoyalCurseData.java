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
      if (nbt.contains("servants")) {
         NbtList servantsList = nbt.getList("servants", 10);

         for (int i = 0; i < servantsList.size(); i++) {
            NbtCompound servantNbt = servantsList.getCompound(i);
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

      nbt.put("servants", servantsList);
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
