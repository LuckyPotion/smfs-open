package com.xie.smfs.manager;

import com.xie.smfs.registry.ModEffects;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoldBlockProtectionManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(GoldBlockProtectionManager.class);
   private static final Map<PlayerEntity, Boolean> PLAYERS_IN_GOLD_BLOCK_SHELTER = new WeakHashMap<>();

   public static boolean isPlayerInGoldBlockShelter(PlayerEntity player) {
      return PLAYERS_IN_GOLD_BLOCK_SHELTER.getOrDefault(player, false);
   }

   public static void setPlayerInGoldBlockShelter(PlayerEntity player, boolean inShelter) {
      PLAYERS_IN_GOLD_BLOCK_SHELTER.put(player, inShelter);
      if (inShelter) {
         LOGGER.debug("玩家 {} 被金块包裹，获得鬼魂攻击免疫效果", player.method_5477().getString());
         player.method_6016(ModEffects.LOST);
      } else {
         LOGGER.debug("玩家 {} 离开金块包裹，失去鬼魂攻击免疫效果", player.method_5477().getString());
      }
   }

   public static boolean checkPlayerSurroundedByGoldBlocks(PlayerEntity player) {
      World world = player.method_37908();
      BlockPos playerPos = player.method_24515();

      for (int x = -1; x <= 1; x++) {
         for (int y = -1; y <= 2; y++) {
            for (int z = -1; z <= 1; z++) {
               BlockPos checkPos = playerPos.method_10069(x, y, z);
               Block block = world.method_8320(checkPos).method_26204();
               if (!block.equals(Blocks.field_10205) && (x != 0 || y < 0 || y > 1 || z != 0)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public static void updatePlayerGoldBlockShelterStatus(PlayerEntity player) {
      boolean isSurrounded = checkPlayerSurroundedByGoldBlocks(player);
      boolean currentStatus = isPlayerInGoldBlockShelter(player);
      if (currentStatus != isSurrounded) {
         setPlayerInGoldBlockShelter(player, isSurrounded);
      }
   }

   public static void removePlayerFromGoldBlockShelter(PlayerEntity player) {
      PLAYERS_IN_GOLD_BLOCK_SHELTER.remove(player);
      LOGGER.debug("移除玩家 {} 的金块包裹状态", player.method_5477().getString());
   }

   public static void validatePlayerGoldBlockShelterStatus(PlayerEntity player) {
      if (isPlayerInGoldBlockShelter(player) && !checkPlayerSurroundedByGoldBlocks(player)) {
         removePlayerFromGoldBlockShelter(player);
      }
   }
}
