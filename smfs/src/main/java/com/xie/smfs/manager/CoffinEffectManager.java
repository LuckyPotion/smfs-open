package com.xie.smfs.manager;

import com.xie.smfs.block.GhostCoffinBlock;
import com.xie.smfs.block.GoldCoffinBlock;
import com.xie.smfs.block.RedCoffinBlock;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.registry.ModEffects;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoffinEffectManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(CoffinEffectManager.class);
   private static final Map<PlayerEntity, BlockPos> PLAYERS_IN_GOLD_COFFIN = new WeakHashMap<>();
   private static final Map<PlayerEntity, BlockPos> PLAYERS_IN_RED_COFFIN = new WeakHashMap<>();
   private static final Map<PlayerEntity, BlockPos> PLAYERS_IN_GHOST_COFFIN = new WeakHashMap<>();

   public static boolean isPlayerInGoldCoffin(PlayerEntity player) {
      return PLAYERS_IN_GOLD_COFFIN.containsKey(player);
   }

   public static boolean isPlayerInRedCoffin(PlayerEntity player) {
      return PLAYERS_IN_RED_COFFIN.containsKey(player);
   }

   public static boolean isPlayerInGhostCoffin(PlayerEntity player) {
      return PLAYERS_IN_GHOST_COFFIN.containsKey(player);
   }

   public static void setPlayerInGoldCoffin(PlayerEntity player, BlockPos coffinPos) {
      PLAYERS_IN_GOLD_COFFIN.put(player, coffinPos);
      PLAYERS_IN_RED_COFFIN.remove(player);
      PLAYERS_IN_GHOST_COFFIN.remove(player);
      player.removeStatusEffect(ModEffects.LOST);
   }

   public static void setPlayerInRedCoffin(PlayerEntity player, BlockPos coffinPos) {
      PLAYERS_IN_RED_COFFIN.put(player, coffinPos);
      PLAYERS_IN_GOLD_COFFIN.remove(player);
      PLAYERS_IN_GHOST_COFFIN.remove(player);
   }

   public static void setPlayerInGhostCoffin(PlayerEntity player, BlockPos coffinPos) {
      PLAYERS_IN_GHOST_COFFIN.put(player, coffinPos);
      PLAYERS_IN_GOLD_COFFIN.remove(player);
      PLAYERS_IN_RED_COFFIN.remove(player);
   }

   public static void removePlayerFromCoffin(PlayerEntity player) {
      PLAYERS_IN_GOLD_COFFIN.remove(player);
      PLAYERS_IN_RED_COFFIN.remove(player);
      PLAYERS_IN_GHOST_COFFIN.remove(player);
   }

   public static void updateRedCoffinRevivalEffect(PlayerEntity player) {
      if (isPlayerInRedCoffin(player)) {
         reduceRevivalDegreeForAllSlots(player);
      }
   }

   private static void reduceRevivalDegreeForAllSlots(PlayerEntity player) {
      LOGGER.debug("开始降低玩家 {} 所有槽位的厉鬼复苏值", player.getName().getString());
      PlayerEvents.decreaseAllSlotRevivalDegree(player, 2);
   }

   public static boolean isPlayerInCoffin(PlayerEntity player) {
      return isPlayerInGoldCoffin(player) || isPlayerInRedCoffin(player) || isPlayerInGhostCoffin(player);
   }

   public static BlockPos getPlayerCoffinPosition(PlayerEntity player) {
      if (isPlayerInGoldCoffin(player)) {
         return PLAYERS_IN_GOLD_COFFIN.get(player);
      } else if (isPlayerInRedCoffin(player)) {
         return PLAYERS_IN_RED_COFFIN.get(player);
      } else {
         return isPlayerInGhostCoffin(player) ? PLAYERS_IN_GHOST_COFFIN.get(player) : null;
      }
   }

   public static void validatePlayerCoffinStatus(PlayerEntity player, World world) {
      BlockPos coffinPos = getPlayerCoffinPosition(player);
      if (coffinPos != null) {
         if (!(world.getBlockState(coffinPos).getBlock() instanceof GoldCoffinBlock)
            && !(world.getBlockState(coffinPos).getBlock() instanceof RedCoffinBlock)
            && !(world.getBlockState(coffinPos).getBlock() instanceof GhostCoffinBlock)) {
            removePlayerFromCoffin(player);
         } else {
            if (!isPlayerInsideCoffin(player, coffinPos)) {
               removePlayerFromCoffin(player);
            }
         }
      }
   }

   private static boolean isPlayerInsideCoffin(PlayerEntity player, BlockPos coffinPos) {
      double distance = player.getPos().distanceTo(coffinPos.toCenterPos());
      return distance <= 2.0;
   }
}
