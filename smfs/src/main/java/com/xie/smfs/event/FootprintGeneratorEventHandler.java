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

            for (ServerPlayerEntity player : server.method_3760().method_14571()) {
               handlePlayerHoldingFirewoodKnife(player, player.method_37908());
            }
         }

         if (giantShadowGhostTickCounter >= 1) {
            giantShadowGhostTickCounter = 0;

            for (ServerPlayerEntity player : server.method_3760().method_14571()) {
               handleGiantShadowGhostSneaking(player, player.method_37908());
            }
         }
      });
   }

   private static void handlePlayerHoldingFirewoodKnife(ServerPlayerEntity player, World world) {
      if (world.method_27983() != Smfs.GHOST_DREAM_DIMENSION) {
         if (isHoldingFirewoodKnife(player)) {
            for (LivingEntity entity : getNearbyLivingEntities(player, world, 20.0)) {
               generateFootprintAtEntityFeet(entity, world);
            }
         }
      }
   }

   private static boolean isHoldingFirewoodKnife(ServerPlayerEntity player) {
      ItemStack mainHandStack = player.method_6047();
      ItemStack offHandStack = player.method_6079();
      if (mainHandStack.method_7909() == ModItems.RUSTY_FIREWOOD_KNIFE || offHandStack.method_7909() == ModItems.RUSTY_FIREWOOD_KNIFE) {
         return true;
      }

      if (mainHandStack.method_7909() == ModItems.FISSURED_SPEAR_PURPLE
         || offHandStack.method_7909() == ModItems.FISSURED_SPEAR_PURPLE
         || mainHandStack.method_7909() == ModItems.FISSURED_SPEAR_RED
         || offHandStack.method_7909() == ModItems.FISSURED_SPEAR_RED) {
         return true;
      }

      if (mainHandStack.method_7909() != ModItems.WISH_SPEAR && offHandStack.method_7909() != ModItems.WISH_SPEAR) {
         return false;
      }

      String mode = FissuredSpearPurpleItem.getThrowMode(mainHandStack.method_7909() == ModItems.WISH_SPEAR ? mainHandStack : offHandStack);
      return mode.equals("medium");
   }

   private static List<LivingEntity> getNearbyLivingEntities(ServerPlayerEntity player, World world, double range) {
      Box detectionBox = new Box(
         player.method_23317() - range,
         player.method_23318() - range,
         player.method_23321() - range,
         player.method_23317() + range,
         player.method_23318() + range,
         player.method_23321() + range
      );
      return world.method_8390(LivingEntity.class, detectionBox, entity -> entity != player && entity.method_5805());
   }

   private static void generateFootprintAtEntityFeet(LivingEntity entity, World world) {
      BlockPos feetPos = new BlockPos((int)Math.floor(entity.method_23317()), (int)Math.floor(entity.method_23318()), (int)Math.floor(entity.method_23321()));
      if (canPlaceFootprintAt(feetPos, world)) {
         BlockState footprintState = ModBlocks.FOOTPRINT.method_9564();
         world.method_8501(feetPos, footprintState);
         if (world.method_8321(feetPos) instanceof FootprintBlockEntity footprintEntity) {
            footprintEntity.setEntityUUID(entity.method_5845());
         }
      }
   }

   private static boolean canPlaceFootprintAt(BlockPos pos, World world) {
      BlockState currentState = world.method_8320(pos);
      boolean canReplace = currentState.method_26215() || currentState.method_26204() instanceof FootprintBlock || isReplaceableBlock(currentState);
      BlockPos belowPos = pos.method_10074();
      BlockState belowState = world.method_8320(belowPos);
      boolean hasSupport = !belowState.method_26215() && belowState.method_26212(world, belowPos);
      return canReplace && hasSupport;
   }

   private static boolean isReplaceableBlock(BlockState state) {
      String blockName = state.method_26204().method_9539().toLowerCase();
      return blockName.contains("grass")
         || blockName.contains("snow")
         || blockName.contains("flower")
         || blockName.contains("plant")
         || blockName.contains("vine")
         || blockName.contains("fern")
         || blockName.contains("mushroom");
   }

   private static void handleGiantShadowGhostSneaking(ServerPlayerEntity player, World world) {
      if (player.method_5715() && hasGiantShadowGhost(player)) {
         generateFootprint2AtEntityFeet(player, world);
      }
   }

   private static void generateFootprint2AtEntityFeet(LivingEntity entity, World world) {
      BlockPos[] possiblePositions = new BlockPos[]{
         new BlockPos((int)Math.floor(entity.method_23317()), (int)Math.floor(entity.method_23318()), (int)Math.floor(entity.method_23321())),
         new BlockPos((int)Math.floor(entity.method_23317()) + 1, (int)Math.floor(entity.method_23318()), (int)Math.floor(entity.method_23321())),
         new BlockPos((int)Math.floor(entity.method_23317()), (int)Math.floor(entity.method_23318()), (int)Math.floor(entity.method_23321()) + 1),
         new BlockPos((int)Math.floor(entity.method_23317()) + 1, (int)Math.floor(entity.method_23318()), (int)Math.floor(entity.method_23321()) + 1)
      };

      for (BlockPos feetPos : possiblePositions) {
         if (canPlaceFootprintAt(feetPos, world)) {
            BlockState footprintState = ModBlocks.FOOTPRINT2.method_9564();
            world.method_8501(feetPos, footprintState);
            if (world.method_8321(feetPos) instanceof Footprint2BlockEntity footprintEntity) {
               footprintEntity.setEntityUUID(entity.method_5845());
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
