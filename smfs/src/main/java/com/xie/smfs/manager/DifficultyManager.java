package com.xie.smfs.manager;

import com.xie.smfs.config.WorldConfig;
import net.minecraft.world.World;

public class DifficultyManager {
   public static float getDifficultyMultiplier(World world) {
      WorldConfig config = WorldConfig.getInstance(world);
      switch (config.modDifficulty) {
         case 0:
            return 0.5F;
         case 2:
            return 1.5F;
         default:
            return 1.0F;
      }
   }

   public static int adjustSpiritualDamage(int baseDamage, World world) {
      float multiplier = getDifficultyMultiplier(world);
      return Math.round(baseDamage * multiplier);
   }

   public static int adjustSpiritualResistance(int baseResistance, World world) {
      float multiplier = getDifficultyMultiplier(world);
      return Math.round(baseResistance * multiplier);
   }

   public static int adjustSpiritualStrength(int baseStrength, World world) {
      float multiplier = getDifficultyMultiplier(world);
      return Math.round(baseStrength * multiplier);
   }

   public static String getDifficultyName(World world) {
      WorldConfig config = WorldConfig.getInstance(world);
      switch (config.modDifficulty) {
         case 0:
            return "低";
         case 2:
            return "高";
         default:
            return "中";
      }
   }
}
