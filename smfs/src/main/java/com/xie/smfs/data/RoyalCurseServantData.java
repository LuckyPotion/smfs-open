package com.xie.smfs.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class RoyalCurseServantData {
   private UUID uuid;
   private String playerName;
   private String playerUuid;
   private boolean released;
   private String ghostDomainColor;
   private int ghostDomainLevel;
   private float ghostDomainRadius;
   private int spiritualResistance;
   private int spiritualDamage;
   private int spiritualStrength;
   private int maxSpiritualStrength;
   private float recoveryFactor;
   private List<String> playerGhosts;

   public RoyalCurseServantData(UUID uuid) {
      this.uuid = uuid;
      this.playerName = "王家奴仆";
      this.playerUuid = "";
      this.released = false;
      this.ghostDomainColor = String.valueOf(8388736);
      this.ghostDomainLevel = 1;
      this.ghostDomainRadius = 8.0F;
      this.spiritualResistance = 50;
      this.spiritualDamage = 50;
      this.spiritualStrength = 100;
      this.maxSpiritualStrength = 100;
      this.recoveryFactor = 0.2F;
      this.playerGhosts = new ArrayList<>();
   }

   public RoyalCurseServantData(NbtCompound nbt) {
      if (nbt.contains("uuid")) {
         this.uuid = UUID.fromString(nbt.getString("uuid"));
      }

      this.playerName = nbt.getString("playerName");
      this.playerUuid = nbt.getString("playerUuid");
      this.released = nbt.getBoolean("released");
      this.ghostDomainColor = nbt.getString("ghostDomainColor");
      this.ghostDomainLevel = nbt.getInt("ghostDomainLevel");
      this.ghostDomainRadius = nbt.getFloat("ghostDomainRadius");
      this.spiritualResistance = nbt.getInt("SpiritualResistance");
      this.spiritualDamage = nbt.getInt("SpiritualDamage");
      this.spiritualStrength = nbt.getInt("SpiritualStrength");
      this.maxSpiritualStrength = nbt.getInt("MaxSpiritualStrength");
      this.recoveryFactor = nbt.getFloat("RecoveryFactor");
      this.playerGhosts = new ArrayList<>();
      if (nbt.contains("playerGhosts")) {
         NbtList ghostsList = nbt.getList("playerGhosts", 8);

         for (int i = 0; i < ghostsList.size(); i++) {
            this.playerGhosts.add(ghostsList.getString(i));
         }
      }
   }

   public NbtCompound toNbt() {
      NbtCompound nbt = new NbtCompound();
      nbt.putString("uuid", this.uuid.toString());
      nbt.putString("playerName", this.playerName);
      nbt.putString("playerUuid", this.playerUuid);
      nbt.putBoolean("released", this.released);
      nbt.putString("ghostDomainColor", this.ghostDomainColor);
      nbt.putInt("ghostDomainLevel", this.ghostDomainLevel);
      nbt.putFloat("ghostDomainRadius", this.ghostDomainRadius);
      nbt.putInt("SpiritualResistance", this.spiritualResistance);
      nbt.putInt("SpiritualDamage", this.spiritualDamage);
      nbt.putInt("SpiritualStrength", this.spiritualStrength);
      nbt.putInt("MaxSpiritualStrength", this.maxSpiritualStrength);
      nbt.putFloat("RecoveryFactor", this.recoveryFactor);
      NbtList ghostsList = new NbtList();

      for (String ghost : this.playerGhosts) {
         ghostsList.add(NbtString.of(ghost));
      }

      nbt.put("playerGhosts", ghostsList);
      return nbt;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getPlayerName() {
      return this.playerName;
   }

   public void setPlayerName(String playerName) {
      this.playerName = playerName;
   }

   public String getPlayerUuid() {
      return this.playerUuid;
   }

   public void setPlayerUuid(String playerUuid) {
      this.playerUuid = playerUuid;
   }

   public boolean isReleased() {
      return this.released;
   }

   public void setReleased(boolean released) {
      this.released = released;
   }

   public void summon() {
      this.released = true;
   }

   public void recall() {
      this.released = false;
   }

   public String getGhostDomainColor() {
      return this.ghostDomainColor;
   }

   public void setGhostDomainColor(String ghostDomainColor) {
      this.ghostDomainColor = ghostDomainColor;
   }

   public int getGhostDomainLevel() {
      return this.ghostDomainLevel;
   }

   public void setGhostDomainLevel(int ghostDomainLevel) {
      this.ghostDomainLevel = ghostDomainLevel;
   }

   public float getGhostDomainRadius() {
      return this.ghostDomainRadius;
   }

   public void setGhostDomainRadius(float ghostDomainRadius) {
      this.ghostDomainRadius = ghostDomainRadius;
   }

   public int getSpiritualResistance() {
      return this.spiritualResistance;
   }

   public void setSpiritualResistance(int spiritualResistance) {
      this.spiritualResistance = spiritualResistance;
   }

   public int getSpiritualDamage() {
      return this.spiritualDamage;
   }

   public void setSpiritualDamage(int spiritualDamage) {
      this.spiritualDamage = spiritualDamage;
   }

   public int getSpiritualStrength() {
      return this.spiritualStrength;
   }

   public void setSpiritualStrength(int spiritualStrength) {
      this.spiritualStrength = spiritualStrength;
   }

   public int getMaxSpiritualStrength() {
      return this.maxSpiritualStrength;
   }

   public void setMaxSpiritualStrength(int maxSpiritualStrength) {
      this.maxSpiritualStrength = maxSpiritualStrength;
   }

   public float getRecoveryFactor() {
      return this.recoveryFactor;
   }

   public void setRecoveryFactor(float recoveryFactor) {
      this.recoveryFactor = recoveryFactor;
   }

   public int getSpiritPower() {
      return this.spiritualStrength;
   }

   public void setSpiritPower(int spiritPower) {
      this.spiritualStrength = spiritPower;
   }

   public List<String> getPlayerGhosts() {
      return this.playerGhosts;
   }

   public void setPlayerGhosts(List<String> playerGhosts) {
      this.playerGhosts = playerGhosts;
   }
}
