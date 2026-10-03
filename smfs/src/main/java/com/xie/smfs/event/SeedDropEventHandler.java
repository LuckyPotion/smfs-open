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
         if (!world.method_8608()) {
            handleBlockBreak((ServerWorld)world, player, pos, state);
         }
      });
      LOGGER.debug("种子掉落事件处理器已注册");
   }

   private static void handleBlockBreak(ServerWorld world, PlayerEntity player, BlockPos pos, BlockState state) {
      Block block = state.method_26204();
      if (isGrassBlock(block)) {
         ItemStack mainHandStack = player.method_6047();
         if (isGoldenHoe(mainHandStack)) {
            handleSeedDrops(world, player, pos);
         }
      }
   }

   private static boolean isGrassBlock(Block block) {
      return block == Blocks.field_10219
         || block == Blocks.field_10214
         || block == Blocks.field_10479
         || block == Blocks.field_10112
         || block == Blocks.field_10313;
   }

   private static boolean isGoldenHoe(ItemStack stack) {
      return stack.method_7909() == Items.field_8303;
   }

   private static void handleSeedDrops(ServerWorld world, PlayerEntity player, BlockPos pos) {
      if (RANDOM.nextDouble() < 0.1) {
         dropSeed(world, pos, ModItems.DIRTY_SEED);
         LOGGER.debug("玩家 {} 使用黄金锄头挖掘草方块，掉落肮脏种子", player.method_5477().getString());
      }

      if (RANDOM.nextDouble() < 0.02) {
         dropSeed(world, pos, ModItems.FILTHY_SEED);
         LOGGER.debug("玩家 {} 使用黄金锄头挖掘草方块，掉落污秽种子", player.method_5477().getString());
      }
   }

   private static void dropSeed(ServerWorld world, BlockPos pos, Item seedItem) {
      ItemStack seedStack = new ItemStack(seedItem, 1);
      ItemEntity itemEntity = new ItemEntity(world, pos.method_10263() + 0.5, pos.method_10264() + 0.5, pos.method_10260() + 0.5, seedStack);
      itemEntity.method_18800((RANDOM.nextDouble() - 0.5) * 0.2, 0.2, (RANDOM.nextDouble() - 0.5) * 0.2);
      world.method_8649(itemEntity);
   }
}
