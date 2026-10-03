package com.xie.smfs.api;

import java.util.List;
import net.minecraft.nbt.NbtCompound;

public interface IPlayerData {
   int getCustomProperty();

   void setCustomProperty(int i);

   float getSpiritResistance();

   void setSpiritResistance(float f);

   float getSpiritDamage();

   void setSpiritDamage(float f);

   float getCurrentSpirit();

   void setCurrentSpirit(float f);

   float getMaxSpirit();

   void setMaxSpirit(float f);

   double getRevivalFactor();

   void setRevivalFactor(double d);

   float getSanity();

   void setSanity(float f);

   NbtCompound getQuestData();

   void setQuestData(NbtCompound nbtCompound);

   List<NbtCompound> getActiveQuests();

   boolean hasCompletedQuest(String string);

   NbtCompound getSpiritData();

   void setSpiritData(NbtCompound nbtCompound);

   void copyFrom(IPlayerData iPlayerData);
}
