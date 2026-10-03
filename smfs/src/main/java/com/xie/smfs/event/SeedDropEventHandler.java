package com.xie.smfs.event;

import com.xie.smfs.registry.ModItems;
import java.util.Random;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.After;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedDropEventHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/SeedDropHandler");
   private static final double DIRTY_SEED_DROP_CHANCE = 0.1;
   private static final double FILTHY_SEED_DROP_CHANCE = 0.02;
   private static final Random RANDOM = new Random();

   public static void register() {
      PlayerBlockBreakEvents.AFTER.register((After)(world, player, pos, state, blockEntity) -> {
         if (!world.isClient()) {
            handleBlockBreak((ServerWorld)world, player, pos, state);
         }
      });
      LOGGER.debug("种子掉落事件处理器已注册");
   }

   private static void handleBlockBreak(ServerWorld world, PlayerEntity player, BlockPos pos, BlockState state) {
      Block block = state.getBlock();
      if (isGrassBlock(block)) {
         ItemStack mainHandStack = player.getMainHandStack();
         if (isGoldenHoe(mainHandStack)) {
            handleSeedDrops(world, player, pos);
         }
      }
   }

   private static boolean isGrassBlock(Block block) {
      return block == Blocks.GRASS_BLOCK || block == Blocks.TALL_GRASS || block == Blocks.GRASS || block == Blocks.FERN || block == Blocks.LARGE_FERN;
   }

   private static boolean isGoldenHoe(ItemStack stack) {
      return stack.getItem() == Items.GOLDEN_HOE;
   }

   private static void handleSeedDrops(ServerWorld world, PlayerEntity player, BlockPos pos) {
      if (RANDOM.nextDouble() < 0.1) {
         dropSeed(world, pos, ModItems.DIRTY_SEED);
         LOGGER.debug("玩家 {} 使用黄金锄头挖掘草方块，掉落肮脏种子", player.getName().getString());
      }

      if (RANDOM.nextDouble() < 0.02) {
         dropSeed(world, pos, ModItems.FILTHY_SEED);
         LOGGER.debug("玩家 {} 使用黄金锄头挖掘草方块，掉落污秽种子", player.getName().getString());
      }
   }

   private static void dropSeed(ServerWorld world, BlockPos pos, Item seedItem) {
      ItemStack seedStack = new ItemStack(seedItem, 1);
      ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, seedStack);
      itemEntity.setVelocity((RANDOM.nextDouble() - 0.5) * 0.2, 0.2, (RANDOM.nextDouble() - 0.5) * 0.2);
      world.spawnEntity(itemEntity);
   }
}
