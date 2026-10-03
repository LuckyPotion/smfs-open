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
      if (nbt.method_10545("uuid")) {
         this.uuid = UUID.fromString(nbt.method_10558("uuid"));
      }

      this.playerName = nbt.method_10558("playerName");
      this.playerUuid = nbt.method_10558("playerUuid");
      this.released = nbt.method_10577("released");
      this.ghostDomainColor = nbt.method_10558("ghostDomainColor");
      this.ghostDomainLevel = nbt.method_10550("ghostDomainLevel");
      this.ghostDomainRadius = nbt.method_10583("ghostDomainRadius");
      this.spiritualResistance = nbt.method_10550("SpiritualResistance");
      this.spiritualDamage = nbt.method_10550("SpiritualDamage");
      this.spiritualStrength = nbt.method_10550("SpiritualStrength");
      this.maxSpiritualStrength = nbt.method_10550("MaxSpiritualStrength");
      this.recoveryFactor = nbt.method_10583("RecoveryFactor");
      this.playerGhosts = new ArrayList<>();
      if (nbt.method_10545("playerGhosts")) {
         NbtList ghostsList = nbt.method_10554("playerGhosts", 8);

         for (int i = 0; i < ghostsList.size(); i++) {
            this.playerGhosts.add(ghostsList.method_10608(i));
         }
      }
   }

   public NbtCompound toNbt() {
      NbtCompound nbt = new NbtCompound();
      nbt.method_10582("uuid", this.uuid.toString());
      nbt.method_10582("playerName", this.playerName);
      nbt.method_10582("playerUuid", this.playerUuid);
      nbt.method_10556("released", this.released);
      nbt.method_10582("ghostDomainColor", this.ghostDomainColor);
      nbt.method_10569("ghostDomainLevel", this.ghostDomainLevel);
      nbt.method_10548("ghostDomainRadius", this.ghostDomainRadius);
      nbt.method_10569("SpiritualResistance", this.spiritualResistance);
      nbt.method_10569("SpiritualDamage", this.spiritualDamage);
      nbt.method_10569("SpiritualStrength", this.spiritualStrength);
      nbt.method_10569("MaxSpiritualStrength", this.maxSpiritualStrength);
      nbt.method_10548("RecoveryFactor", this.recoveryFactor);
      NbtList ghostsList = new NbtList();

      for (String ghost : this.playerGhosts) {
         ghostsList.add(NbtString.method_23256(ghost));
      }

      nbt.method_10566("playerGhosts", ghostsList);
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
