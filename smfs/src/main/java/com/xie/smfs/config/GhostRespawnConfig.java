package com.xie.smfs.config;

public class GhostRespawnConfig {
   public final String ghostType;
   public boolean enabled = true;
   public int respawnDays = 7;
   public int spawnCooldown = 24000;
   public boolean nightOnly = true;
   public double spawnChance = 1.0;

   public GhostRespawnConfig(String ghostType) {
      this.ghostType = ghostType;
   }

   public void validate() {
      this.respawnDays = Math.max(0, Math.min(100, this.respawnDays));
      this.spawnCooldown = Math.max(600, Math.min(72000, this.spawnCooldown));
      this.spawnChance = Math.max(0.0, Math.min(1.0, this.spawnChance));
   }

   public void resetToDefaults() {
      this.enabled = true;
      this.nightOnly = true;
      switch (this.ghostType) {
         case "chen_doctor":
            this.respawnDays = 1;
            this.spawnCooldown = 19200;
            this.spawnChance = 0.4;
            this.nightOnly = false;
            break;
         case "npc1":
            this.respawnDays = 1;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "npc3":
            this.respawnDays = 1;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "npc5":
            this.respawnDays = 1;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "npc2":
            this.respawnDays = 2;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "npc4":
            this.respawnDays = 2;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "npc6":
            this.respawnDays = 2;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "wang_xiao_ming":
            this.respawnDays = 3;
            this.spawnCooldown = 21600;
            this.spawnChance = 0.4;
            this.nightOnly = false;
            break;
         case "cao_yang":
            this.respawnDays = 3;
            this.spawnCooldown = 26400;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "li_le_ping":
            this.respawnDays = 3;
            this.spawnCooldown = 28800;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "li_jun":
            this.respawnDays = 3;
            this.spawnCooldown = 16800;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "feng_quan":
            this.respawnDays = 4;
            this.spawnCooldown = 33600;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "fang_shi_min":
            this.respawnDays = 4;
            this.spawnCooldown = 31200;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "qiaomen_ghost":
            this.respawnDays = 5;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "ye_zhen":
            this.respawnDays = 5;
            this.spawnCooldown = 30000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "zhao_kai_ming":
            this.respawnDays = 5;
            this.spawnCooldown = 36000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "ghost_merchant":
            this.respawnDays = 6;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            this.nightOnly = false;
            break;
         case "taitou_ghost":
            this.respawnDays = 7;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "box_ghost":
            this.respawnDays = 8;
            this.spawnCooldown = 30000;
            this.spawnChance = 0.6;
            break;
         case "ghost_pressure":
            this.respawnDays = 9;
            this.spawnCooldown = 31200;
            this.spawnChance = 0.7;
            break;
         case "untouchable_ghost":
            this.respawnDays = 10;
            this.spawnCooldown = 48000;
            this.spawnChance = 0.65;
            break;
         case "yan_li":
            this.respawnDays = 10;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.3;
            this.nightOnly = false;
            break;
         case "fog_ghost":
            this.respawnDays = 11;
            this.spawnCooldown = 42000;
            this.spawnChance = 0.6;
            break;
         case "ditou_ghost":
            this.respawnDays = 12;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "jump_ghost":
            this.respawnDays = 13;
            this.spawnCooldown = 36000;
            this.spawnChance = 0.65;
            break;
         case "giant_shadow_ghost":
            this.respawnDays = 14;
            this.spawnCooldown = 21600;
            this.spawnChance = 0.7;
            break;
         case "ganshi_bride_ghost":
            this.respawnDays = 15;
            this.spawnCooldown = 38400;
            this.spawnChance = 0.65;
            break;
         case "food_ghost":
            this.respawnDays = 16;
            this.spawnCooldown = 12000;
            this.spawnChance = 0.6;
            break;
         case "crop_ghost":
            this.respawnDays = 17;
            this.spawnCooldown = 30000;
            this.spawnChance = 0.6;
            break;
         case "step_ghost":
            this.respawnDays = 18;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "trash_ghost":
            this.respawnDays = 19;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.65;
            break;
         case "water_ghost":
            this.respawnDays = 20;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.65;
            break;
         case "block_ghost":
            this.respawnDays = 21;
            this.spawnCooldown = 14400;
            this.spawnChance = 0.6;
            break;
         case "suona_ghost":
            this.respawnDays = 22;
            this.spawnCooldown = 33600;
            this.spawnChance = 0.65;
            break;
         case "burn_ghost":
            this.respawnDays = 23;
            this.spawnCooldown = 31200;
            this.spawnChance = 0.7;
            break;
         case "death_sight_ghost":
            this.respawnDays = 24;
            this.spawnCooldown = 26400;
            this.spawnChance = 0.65;
            break;
         case "crying_ghost":
            this.respawnDays = 25;
            this.spawnCooldown = 28800;
            this.spawnChance = 0.65;
            break;
         case "gong_ghost":
            this.respawnDays = 26;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "lost_ghost":
            this.respawnDays = 27;
            this.spawnCooldown = 33600;
            this.spawnChance = 0.6;
            break;
         case "ghost_wind":
            this.respawnDays = 28;
            this.spawnCooldown = 16800;
            this.spawnChance = 0.65;
            break;
         case "villager_ghost":
            this.respawnDays = 29;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "goods_seller_ghost":
            this.respawnDays = 30;
            this.spawnCooldown = 36000;
            this.spawnChance = 0.5;
            break;
         case "silent_ghost":
            this.respawnDays = 31;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.4;
            this.nightOnly = false;
            break;
         case "plague_ghost":
            this.respawnDays = 32;
            this.spawnCooldown = 36000;
            this.spawnChance = 0.65;
            break;
         case "mineral_ghost":
            this.respawnDays = 33;
            this.spawnCooldown = 30000;
            this.spawnChance = 0.6;
            break;
         case "shadow_ghost":
            this.respawnDays = 34;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.55;
            break;
         case "door_ghost":
            this.respawnDays = 35;
            this.spawnCooldown = 42000;
            this.spawnChance = 0.7;
            break;
         case "ghost_smoke":
            this.respawnDays = 36;
            this.spawnCooldown = 18000;
            this.spawnChance = 0.5;
            break;
         case "ghost_officer":
            this.respawnDays = 37;
            this.spawnCooldown = 30000;
            this.spawnChance = 0.55;
            break;
         case "sneak_ghost":
            this.respawnDays = 38;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "clothes_ghost":
            this.respawnDays = 39;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "puppet_ghost":
            this.respawnDays = 40;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         case "ghost_shadow_head":
            this.respawnDays = 41;
            this.spawnCooldown = 26400;
            this.spawnChance = 0.5;
            break;
         case "grave_earth_ghost":
            this.respawnDays = 42;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.6;
            break;
         default:
            this.respawnDays = 7;
            this.spawnCooldown = 24000;
            this.spawnChance = 0.8;
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public int getRespawnDays() {
      return this.respawnDays;
   }

   public void setRespawnDays(int respawnDays) {
      this.respawnDays = respawnDays;
   }

   public int getSpawnCooldown() {
      return this.spawnCooldown;
   }

   public void setSpawnCooldown(int spawnCooldown) {
      this.spawnCooldown = spawnCooldown;
   }

   public boolean isNightOnly() {
      return this.nightOnly;
   }

   public void setNightOnly(boolean nightOnly) {
      this.nightOnly = nightOnly;
   }

   public double getSpawnChance() {
      return this.spawnChance;
   }

   public void setSpawnChance(double spawnChance) {
      this.spawnChance = spawnChance;
   }
}
