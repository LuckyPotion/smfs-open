package com.xie.smfs.event;

import com.xie.smfs.Smfs;
import com.xie.smfs.block.FootprintBlock;
import com.xie.smfs.block.entity.Footprint2BlockEntity;
import com.xie.smfs.block.entity.FootprintBlockEntity;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.item.FissuredSpearPurpleItem;
import com.xie.smfs.manager.MainGhostManager;
import com.xie.smfs.registry.ModBlocks;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class FootprintGeneratorEventHandler {
   private static final double DETECTION_RANGE = 20.0;
   private static final int GENERATION_INTERVAL = 1;
   private static int tickCounter = 0;
   private static int giantShadowGhostTickCounter = 0;

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         tickCounter++;
         giantShadowGhostTickCounter++;
         if (tickCounter >= 20) {
            tickCounter = 0;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
               handlePlayerHoldingFirewoodKnife(player, player.getWorld());
            }
         }

         if (giantShadowGhostTickCounter >= 1) {
            giantShadowGhostTickCounter = 0;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
               handleGiantShadowGhostSneaking(player, player.getWorld());
            }
         }
      });
   }

   private static void handlePlayerHoldingFirewoodKnife(ServerPlayerEntity player, World world) {
      if (world.getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
         if (isHoldingFirewoodKnife(player)) {
            for (LivingEntity entity : getNearbyLivingEntities(player, world, 20.0)) {
               generateFootprintAtEntityFeet(entity, world);
            }
         }
      }
   }

   private static boolean isHoldingFirewoodKnife(ServerPlayerEntity player) {
      ItemStack mainHandStack = player.getMainHandStack();
      ItemStack offHandStack = player.getOffHandStack();
      if (mainHandStack.getItem() == ModItems.RUSTY_FIREWOOD_KNIFE || offHandStack.getItem() == ModItems.RUSTY_FIREWOOD_KNIFE) {
         return true;
      }

      if (mainHandStack.getItem() == ModItems.FISSURED_SPEAR_PURPLE
         || offHandStack.getItem() == ModItems.FISSURED_SPEAR_PURPLE
         || mainHandStack.getItem() == ModItems.FISSURED_SPEAR_RED
         || offHandStack.getItem() == ModItems.FISSURED_SPEAR_RED) {
         return true;
      }

      if (mainHandStack.getItem() != ModItems.WISH_SPEAR && offHandStack.getItem() != ModItems.WISH_SPEAR) {
         return false;
      }

      String mode = FissuredSpearPurpleItem.getThrowMode(mainHandStack.getItem() == ModItems.WISH_SPEAR ? mainHandStack : offHandStack);
      return mode.equals("medium");
   }

   private static List<LivingEntity> getNearbyLivingEntities(ServerPlayerEntity player, World world, double range) {
      Box detectionBox = new Box(
         player.getX() - range, player.getY() - range, player.getZ() - range, player.getX() + range, player.getY() + range, player.getZ() + range
      );
      return world.getEntitiesByClass(LivingEntity.class, detectionBox, entity -> entity != player && entity.isAlive());
   }

   private static void generateFootprintAtEntityFeet(LivingEntity entity, World world) {
      BlockPos feetPos = new BlockPos((int)Math.floor(entity.getX()), (int)Math.floor(entity.getY()), (int)Math.floor(entity.getZ()));
      if (canPlaceFootprintAt(feetPos, world)) {
         BlockState footprintState = ModBlocks.FOOTPRINT.getDefaultState();
         world.setBlockState(feetPos, footprintState);
         if (world.getBlockEntity(feetPos) instanceof FootprintBlockEntity footprintEntity) {
            footprintEntity.setEntityUUID(entity.getUuidAsString());
         }
      }
   }

   private static boolean canPlaceFootprintAt(BlockPos pos, World world) {
      BlockState currentState = world.getBlockState(pos);
      boolean canReplace = currentState.isAir() || currentState.getBlock() instanceof FootprintBlock || isReplaceableBlock(currentState);
      BlockPos belowPos = pos.down();
      BlockState belowState = world.getBlockState(belowPos);
      boolean hasSupport = !belowState.isAir() && belowState.isSolidBlock(world, belowPos);
      return canReplace && hasSupport;
   }

   private static boolean isReplaceableBlock(BlockState state) {
      String blockName = state.getBlock().getTranslationKey().toLowerCase();
      return blockName.contains("grass")
         || blockName.contains("snow")
         || blockName.contains("flower")
         || blockName.contains("plant")
         || blockName.contains("vine")
         || blockName.contains("fern")
         || blockName.contains("mushroom");
   }

   private static void handleGiantShadowGhostSneaking(ServerPlayerEntity player, World world) {
      if (player.isSneaking() && hasGiantShadowGhost(player)) {
         generateFootprint2AtEntityFeet(player, world);
      }
   }

   private static void generateFootprint2AtEntityFeet(LivingEntity entity, World world) {
      BlockPos[] possiblePositions = new BlockPos[]{
         new BlockPos((int)Math.floor(entity.getX()), (int)Math.floor(entity.getY()), (int)Math.floor(entity.getZ())),
         new BlockPos((int)Math.floor(entity.getX()) + 1, (int)Math.floor(entity.getY()), (int)Math.floor(entity.getZ())),
         new BlockPos((int)Math.floor(entity.getX()), (int)Math.floor(entity.getY()), (int)Math.floor(entity.getZ()) + 1),
         new BlockPos((int)Math.floor(entity.getX()) + 1, (int)Math.floor(entity.getY()), (int)Math.floor(entity.getZ()) + 1)
      };

      for (BlockPos feetPos : possiblePositions) {
         if (canPlaceFootprintAt(feetPos, world)) {
            BlockState footprintState = ModBlocks.FOOTPRINT2.getDefaultState();
            world.setBlockState(feetPos, footprintState);
            if (world.getBlockEntity(feetPos) instanceof Footprint2BlockEntity footprintEntity) {
               footprintEntity.setEntityUUID(entity.getUuidAsString());
            }
            break;
         }
      }
   }

   private static boolean hasGiantShadowGhost(ServerPlayerEntity player) {
      int mainSlot = MainGhostManager.getMainGhostSlot(player);
      String ghostType = PlayerEvents.getGhostTypeInSlot(player, mainSlot);
      return "giant_shadow_ghost".equals(ghostType) || "complete_shadow_ghost".equals(ghostType);
   }
}
