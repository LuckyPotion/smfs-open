package com.xie.smfs.manager;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class PassiveSkillManager {
   private static boolean isSilencedOrDreaming(PlayerEntity player) {
      return player.hasStatusEffect(ModEffects.SILENCE) || player.hasStatusEffect(ModEffects.DREAM);
   }

   public static void tick1Second(World world, PlayerEntity player) {
      if (world.getTime() % 20L == 0L) {
         GhostSkillManager.handlePuppetGhostResistanceSkill(player);
         GhostSkillManager.handleFuneralMusicGhostPassiveSkill(player);
         GhostSkillManager.handleSneakGhostPassiveSkill(player);
         GhostSkillManager.handleCropGhostPassiveSkill(player);
         PlayerEvents.updatePlayerSpiritAttributes(player);
      }
   }

   public static void tick2Seconds(World world, PlayerEntity player) {
      if (world.getTime() % 40L == 0L) {
         GhostSkillManager.handleFoodGhostPassiveSkill(player);
         GhostSkillManager.handleShadowGhostPassiveSkill(player);
         GhostSkillManager.handleGiantShadowGhostPassiveSkill(player);
      }
   }

   public static void tick3Seconds(World world, PlayerEntity player) {
      if (world.getTime() % 60L == 0L) {
         GhostSkillManager.handleVillagerGhostPassiveSkill(player);
      }
   }

   public static void tick5Seconds(World world, PlayerEntity player) {
      if (world.getTime() % 100L == 0L) {
         GhostSkillManager.handleMineralGhostPassiveSkill(player);
         GhostSkillManager.handleBoxGhostPassiveSkill(player);
         GhostSkillManager.handleGhostOfficerPassiveSkill(player);
         GhostSkillManager.handlePuppetGhostPassiveSkill(player);
      }
   }

   public static void tick10Seconds(World world, PlayerEntity player) {
      if (world.getTime() % 200L == 0L) {
         GhostSkillManager.handleLostGhostPassiveSkill(player);
         GhostSkillManager.handleGhostPressurePassiveSkill(player);
         GhostSkillManager.handleClothesGhostPassiveSkill(player);
      }
   }

   public static void triggerAllPassiveSkills(World world, PlayerEntity player) {
      if (!isSilencedOrDreaming(player)) {
         tick1Second(world, player);
         tick2Seconds(world, player);
         tick3Seconds(world, player);
         tick5Seconds(world, player);
         tick10Seconds(world, player);
      }
   }

   public static void triggerAllPlayersPassiveSkills(World world) {
      for (PlayerEntity player : world.getPlayers()) {
         triggerAllPassiveSkills(world, player);
      }
   }
}
