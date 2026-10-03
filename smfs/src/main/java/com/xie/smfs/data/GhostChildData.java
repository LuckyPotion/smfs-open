package com.xie.smfs.data;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class GhostChildData {
   private static final int MAX_LEVEL = 30;
   private static final int[] BREAKTHROUGH_LEVELS = new int[]{6, 11, 16, 21, 26, 30};
   private int level = 1;
   private int experience = 0;
   private int spiritPower = 100;
   private int maxSpiritPower = 100;
   private int spiritResistance = 5;
   private int spiritDamage = 5;
   private float revivalFactor = 0.0F;
   private boolean[] breakthroughs = new boolean[BREAKTHROUGH_LEVELS.length];
   private boolean isSummoned = false;
   private List<String> fedGhostTypes = new ArrayList<>();

   public GhostChildData() {
   }

   public GhostChildData(NbtCompound nbt) {
      this.loadFromNbt(nbt);
   }

   public int getLevel() {
      return this.level;
   }

   public void setLevel(int level) {
      this.level = Math.min(level, 30);
   }

   public int getExperience() {
      return this.experience;
   }

   public void addExperience(int amount) {
      this.experience += amount;
      this.checkLevelUp();
   }

   public int getSpiritPower() {
      return this.spiritPower;
   }

   public void setSpiritPower(int spiritPower) {
      this.spiritPower = Math.max(0, Math.min(this.maxSpiritPower, spiritPower));
   }

   public int getMaxSpiritPower() {
      return this.maxSpiritPower;
   }

   public void setMaxSpiritPower(int maxSpiritPower) {
      this.maxSpiritPower = Math.max(10, maxSpiritPower);
   }

   public int getSpiritResistance() {
      return this.spiritResistance;
   }

   public void setSpiritResistance(int spiritResistance) {
      this.spiritResistance = Math.max(0, spiritResistance);
   }

   public int getSpiritDamage() {
      return this.spiritDamage;
   }

   public void setSpiritDamage(int spiritDamage) {
      this.spiritDamage = Math.max(0, spiritDamage);
   }

   public float getRevivalFactor() {
      return this.revivalFactor;
   }

   public void setRevivalFactor(float revivalFactor) {
      this.revivalFactor = Math.max(0.0F, revivalFactor);
   }

   public boolean isSummoned() {
      return this.isSummoned;
   }

   public void setSummoned(boolean summoned) {
      this.isSummoned = summoned;
   }

   public void summon() {
      this.isSummoned = true;
   }

   public void recall() {
      this.isSummoned = false;
   }

   public boolean isBreakthroughRequired(int level) {
      for (int i = 0; i < BREAKTHROUGH_LEVELS.length; i++) {
         if (BREAKTHROUGH_LEVELS[i] == level && !this.breakthroughs[i]) {
            return true;
         }
      }

      return false;
   }

   public boolean canLevelUp() {
      return this.level >= 30 ? false : !this.isBreakthroughRequired(this.level + 1);
   }

   public void performBreakthrough(int levelIndex) {
      if (levelIndex >= 0 && levelIndex < BREAKTHROUGH_LEVELS.length) {
         int breakthroughLevel = BREAKTHROUGH_LEVELS[levelIndex];

         while (this.level < breakthroughLevel && this.experience >= this.getRequiredExperience(this.level)) {
            this.experience = this.experience - this.getRequiredExperience(this.level);
            this.level++;
            this.maxSpiritPower = (int)(this.maxSpiritPower * 1.3);
            this.spiritPower = this.maxSpiritPower;
            this.spiritResistance = (int)(this.spiritResistance * 1.25);
            this.spiritDamage = (int)(this.spiritDamage * 1.25);
            this.revivalFactor *= 1.2F;
         }

         this.breakthroughs[levelIndex] = true;
         this.checkLevelUp();
         this.maxSpiritPower = (int)(this.maxSpiritPower * 1.5);
         this.spiritPower = this.maxSpiritPower;
         this.spiritResistance = (int)(this.spiritResistance * 1.35);
         this.spiritDamage = (int)(this.spiritDamage * 1.35);
         this.revivalFactor = (float)(this.revivalFactor * 1.3);
      }
   }

   private void checkLevelUp() {
      for (int requiredExperience = this.getRequiredExperience(this.level);
         this.experience >= requiredExperience && this.canLevelUp();
         requiredExperience = this.getRequiredExperience(this.level)
      ) {
         this.experience -= requiredExperience;
         this.level++;
         this.maxSpiritPower = (int)(this.maxSpiritPower * 1.3);
         this.spiritPower = this.maxSpiritPower;
         this.spiritResistance = (int)(this.spiritResistance * 1.25);
         this.spiritDamage = (int)(this.spiritDamage * 1.25);
         this.revivalFactor *= 1.2F;
      }
   }

   private int getRequiredExperience(int level) {
      return 100 * level * level;
   }

   public boolean isAtBreakthroughLevel() {
      if (this.level >= 30) {
         return false;
      }

      for (int i = 0; i < BREAKTHROUGH_LEVELS.length; i++) {
         int breakthroughLevel = BREAKTHROUGH_LEVELS[i];
         if (this.level == breakthroughLevel && !this.breakthroughs[i]) {
            return true;
         }
      }

      int nextLevel = this.level + 1;

      for (int i = 0; i < BREAKTHROUGH_LEVELS.length; i++) {
         int breakthroughLevel = BREAKTHROUGH_LEVELS[i];
         if (nextLevel == breakthroughLevel && this.experience >= this.getRequiredExperience(this.level) && !this.breakthroughs[i]) {
            return true;
         }
      }

      return false;
   }

   public int getBreakthroughIndex() {
      if (this.level >= 30) {
         return -1;
      }

      for (int i = 0; i < BREAKTHROUGH_LEVELS.length; i++) {
         if (BREAKTHROUGH_LEVELS[i] == this.level && !this.breakthroughs[i]) {
            return i;
         }
      }

      int nextLevel = this.level + 1;

      for (int i = 0; i < BREAKTHROUGH_LEVELS.length; i++) {
         if (BREAKTHROUGH_LEVELS[i] == nextLevel && this.experience >= this.getRequiredExperience(this.level) && !this.breakthroughs[i]) {
            return i;
         }
      }

      return -1;
   }

   public List<String> getFedGhostTypes() {
      return this.fedGhostTypes;
   }

   public void addFedGhostType(String ghostType) {
      if (ghostType != null && !ghostType.isEmpty() && !this.fedGhostTypes.contains(ghostType)) {
         this.fedGhostTypes.add(ghostType);
      }
   }

   public void clearFedGhostTypes() {
      this.fedGhostTypes.clear();
   }

   public void saveToNbt(NbtCompound nbt) {
      nbt.method_10569("level", this.level);
      nbt.method_10569("experience", this.experience);
      nbt.method_10569("spiritPower", this.spiritPower);
      nbt.method_10569("maxSpiritPower", this.maxSpiritPower);
      nbt.method_10569("spiritResistance", this.spiritResistance);
      nbt.method_10569("spiritDamage", this.spiritDamage);
      nbt.method_10548("revivalFactor", this.revivalFactor);
      nbt.method_10556("isSummoned", this.isSummoned);
      int[] breakthroughsInt = new int[this.breakthroughs.length];

      for (int i = 0; i < this.breakthroughs.length; i++) {
         breakthroughsInt[i] = this.breakthroughs[i] ? 1 : 0;
      }

      nbt.method_10539("breakthroughs", breakthroughsInt);
      NbtList ghostTypesList = new NbtList();

      for (String ghostType : this.fedGhostTypes) {
         ghostTypesList.add(NbtString.method_23256(ghostType));
      }

      nbt.method_10566("fedGhostTypes", ghostTypesList);
   }

   public void loadFromNbt(NbtCompound nbt) {
      this.level = nbt.method_10550("level");
      this.experience = nbt.method_10550("experience");
      this.spiritPower = nbt.method_10550("spiritPower");
      this.maxSpiritPower = nbt.method_10550("maxSpiritPower");
      this.spiritResistance = nbt.method_10550("spiritResistance");
      this.spiritDamage = nbt.method_10550("spiritDamage");
      this.revivalFactor = nbt.method_10583("revivalFactor");
      this.isSummoned = nbt.method_10577("isSummoned");
      if (nbt.method_10545("breakthroughs")) {
         int[] breakthroughsInt = nbt.method_10561("breakthroughs");
         this.breakthroughs = new boolean[Math.min(breakthroughsInt.length, BREAKTHROUGH_LEVELS.length)];

         for (int i = 0; i < this.breakthroughs.length; i++) {
            this.breakthroughs[i] = breakthroughsInt[i] == 1;
         }
      }

      if (nbt.method_10545("fedGhostTypes")) {
         this.fedGhostTypes.clear();
         NbtList ghostTypesList = nbt.method_10554("fedGhostTypes", 8);

         for (int i = 0; i < ghostTypesList.size(); i++) {
            this.fedGhostTypes.add(ghostTypesList.method_10608(i));
         }
      }
   }
}
