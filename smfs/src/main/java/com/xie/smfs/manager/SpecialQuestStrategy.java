package com.xie.smfs.manager;

import com.mojang.datafixers.util.Pair;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.registry.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryList.Direct;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;

class SpecialQuestStrategy implements QuestDetectionStrategy {
   @Override
   public boolean checkCondition(PlayerEntity player, String objectiveId, int targetCount) {
      switch (objectiveId) {
         case "tame_ghost":
            return this.checkGhostTaming(player);
         case "craft_gold_container":
            return this.checkGoldContainerCrafting(player, targetCount);
         case "craft_ghost_faction":
            return this.checkGhostFactionCrafting(player, targetCount);
         case "capture_ghost":
            return this.checkGhostCapture(player, targetCount);
         case "enter_rich_mall":
            return this.checkStructureEnter(player, "furen_mall", targetCount);
         case "enter_chinese_medicine_shop":
            return this.checkStructureEnter(player, "chinese_medicine_shop", targetCount);
         case "trade_with_wangxiaoming":
            return this.checkWangXiaoMingTrade(player, targetCount);
         default:
            LOGGER.warn("未知的特殊任务目标: {}", objectiveId);
            return false;
      }
   }

   @Override
   public int getCurrentProgress(PlayerEntity player, String objectiveId) {
      switch (objectiveId) {
         case "tame_ghost":
            return this.getTamedGhostCount(player);
         case "craft_gold_container":
            return this.getGoldContainerCount(player);
         case "craft_ghost_faction":
            return this.getGhostFactionCount(player);
         case "capture_ghost":
            return this.getGhostCaptureCount(player);
         case "enter_rich_mall":
            return 0;
         case "enter_chinese_medicine_shop":
            return 0;
         case "trade_with_wangxiaoming":
            return this.getWangXiaoMingTradeCount(player);
         default:
            return 0;
      }
   }

   @Override
   public void consumeItems(PlayerEntity player, String objectiveId, int amount) {
      LOGGER.info("特殊任务目标 {} 不需要消耗物品", objectiveId);
   }

   private boolean checkGhostTaming(PlayerEntity player) {
      return this.getTamedGhostCount(player) > 0;
   }

   private int getTamedGhostCount(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      if (data.contains("GhostSlots")) {
         NbtCompound ghostSlots = data.getCompound("GhostSlots");
         int count = 0;

         for (int i = 0; i < 10; i++) {
            String slotKey = "Slot" + i;
            if (ghostSlots.contains(slotKey)) {
               NbtCompound slotData = ghostSlots.getCompound(slotKey);
               if (slotData.getBoolean("occupied")) {
                  count++;
               }
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   private boolean checkGoldContainerCrafting(PlayerEntity player, int targetCount) {
      return this.getGoldContainerCount(player) >= targetCount;
   }

   private int getGoldContainerCount(PlayerEntity player) {
      return QuestDetectionHelper.countPlayerItems(player, "golden_container");
   }

   private boolean checkGhostFactionCrafting(PlayerEntity player, int targetCount) {
      return this.getGhostFactionCount(player) >= targetCount;
   }

   private int getGhostFactionCount(PlayerEntity player) {
      return QuestDetectionHelper.countPlayerItems(player, "ghost_faction");
   }

   private boolean checkGhostCapture(PlayerEntity player, int targetCount) {
      return this.getGhostCaptureCount(player) >= targetCount;
   }

   private int getGhostCaptureCount(PlayerEntity player) {
      PlayerInventory inventory = player.getInventory();
      int count = 0;

      for (int i = 0; i < inventory.size(); i++) {
         ItemStack stack = inventory.getStack(i);
         if (!stack.isEmpty() && stack.getItem() == ModItems.GOLDEN_CONTAINER && GoldenContainerItem.hasGhost(stack)) {
            count++;
         }
      }

      return count;
   }

   private boolean checkStructureEnter(PlayerEntity player, String structureId, int targetCount) {
      return this.getStructureEnterCount(player, structureId) >= targetCount;
   }

   private int getStructureEnterCount(PlayerEntity player, String structureId) {
      return player instanceof ServerPlayerEntity serverPlayer && this.isPlayerInStructure(serverPlayer, structureId) ? 1 : 0;
   }

   private boolean isPlayerInStructure(ServerPlayerEntity player, String structureId) {
      try {
         BlockPos playerPos = player.getBlockPos();
         ServerWorld serverWorld = player.getServerWorld();
         TagKey<Structure> structureTag = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", structureId));
         BlockPos structurePos = serverWorld.locateStructure(structureTag, playerPos, 5000, false);
         if (structurePos != null) {
            double distance = playerPos.getSquaredDistance(structurePos);
            int detectionRadius = 200;
            if (distance <= detectionRadius * detectionRadius) {
               LOGGER.info("玩家 {} 进入{}结构，距离: {}", player.getName().getString(), structureId, Math.sqrt(distance));
               return true;
            }

            LOGGER.info("玩家 {} 距离{}结构过远，距离: {} (需要 <= {})", player.getName().getString(), structureId, Math.sqrt(distance), detectionRadius);
         } else {
            LOGGER.info("玩家 {} 附近5000格内未找到{}结构", player.getName().getString(), structureId);
            structurePos = this.findStructureAlternative(player, structureId);
            if (structurePos != null) {
               double distance = playerPos.getSquaredDistance(structurePos);
               int detectionRadius = 200;
               if (distance <= detectionRadius * detectionRadius) {
                  LOGGER.info("玩家 {} 通过备选方案进入{}结构，距离: {}", player.getName().getString(), structureId, Math.sqrt(distance));
                  return true;
               }
            }
         }
      } catch (Exception e) {
         LOGGER.error("检测{}结构时发生错误", structureId, e);
      }

      return false;
   }

   private BlockPos findStructureAlternative(ServerPlayerEntity player, String structureId) {
      try {
         BlockPos playerPos = player.getBlockPos();
         ServerWorld serverWorld = player.getServerWorld();
         Registry<Structure> structureRegistry = serverWorld.getRegistryManager().get(RegistryKeys.STRUCTURE);
         Identifier structureIdentifier = new Identifier("smfs", structureId);
         Structure structure = (Structure)structureRegistry.get(structureIdentifier);
         if (structure == null) {
            LOGGER.warn("{}结构未注册: {}", structureId, structureIdentifier);
            return null;
         }

         RegistryEntry<Structure> structureEntry = structureRegistry.getEntry(structure);
         if (structureEntry == null) {
            LOGGER.warn("无法获取{}结构的注册表条目", structureId);
            return null;
         }

         Direct<Structure> structureList = RegistryEntryList.of(new RegistryEntry[]{structureEntry});
         ChunkGenerator chunkGenerator = serverWorld.getChunkManager().getChunkGenerator();
         Pair<BlockPos, RegistryEntry<Structure>> structureResult = chunkGenerator.locateStructure(serverWorld, structureList, playerPos, 5000, false);
         if (structureResult != null) {
            BlockPos structurePos = (BlockPos)structureResult.getFirst();
            int centerY = serverWorld.getTopY(Type.WORLD_SURFACE, structurePos.getX(), structurePos.getZ());
            LOGGER.debug("通过备选方案找到{}结构，坐标: {}, {}, {}", structureId, structurePos.getX(), centerY, structurePos.getZ());
            return new BlockPos(structurePos.getX(), centerY, structurePos.getZ());
         }
      } catch (Exception e) {
         LOGGER.error("备选方案搜索{}结构时发生错误", structureId, e);
      }

      return null;
   }

   private boolean checkWangXiaoMingTrade(PlayerEntity player, int targetCount) {
      return this.getWangXiaoMingTradeCount(player) >= targetCount;
   }

   private int getWangXiaoMingTradeCount(PlayerEntity player) {
      NbtCompound data = PlayerEvents.getCachedData(player);
      if (data.contains("wangxiaoming_trade_count")) {
         int count = data.getInt("wangxiaoming_trade_count");
         LOGGER.info("玩家 {} 与王小明的交易次数: {}", player.getName().getString(), count);
         return count;
      } else {
         LOGGER.info("玩家 {} 尚未与王小明进行过交易", player.getName().getString());
         return 0;
      }
   }
}
