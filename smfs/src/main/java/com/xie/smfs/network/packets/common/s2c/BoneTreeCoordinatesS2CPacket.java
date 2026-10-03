package com.xie.smfs.network.packets.common.s2c;

import com.mojang.datafixers.util.Pair;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryList.Direct;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoneTreeCoordinatesS2CPacket {
   public static final Identifier ID = new Identifier("smfs", "bone_tree_coordinates");
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/BoneTreeCoordinatesS2CPacket");
   public static final TagKey<Structure> BONE_TREE_STRUCTURE_TAG = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("smfs", "bone_tree"));
   private static final AtomicReference<String> receivedCoordinates = new AtomicReference<>(null);

   public static void registerClient() {
      ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
         String coordinates = buf.readString();
         client.execute(() -> {
            receivedCoordinates.set(coordinates);
            LOGGER.debug("客户端接收到白骨树坐标: {}", coordinates);
         });
      });
   }

   public static String getReceivedCoordinates() {
      return receivedCoordinates.get();
   }

   public static void clearReceivedCoordinates() {
      receivedCoordinates.set(null);
   }

   public static void sendCoordinates(ServerPlayerEntity player, String coordinates) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeString(coordinates);
      ServerPlayNetworking.send(player, ID, buf);
      LOGGER.debug("向玩家 {} 发送白骨树坐标: {}", player.getName().getString(), coordinates);
   }

   public static void sendCoordinates(ServerPlayerEntity player, BlockPos pos) {
      String coordinates = String.format("X: %d, Z: %d", pos.getX(), pos.getZ());
      sendCoordinates(player, coordinates);
   }

   public static void sendCoordinates(ServerPlayerEntity player) {
      BlockPos nearestBoneTree = findNearestBoneTree(player);
      if (nearestBoneTree != null) {
         sendCoordinates(player, nearestBoneTree);
      } else {
         sendCoordinates(player, "未找到白骨树");
         LOGGER.warn("无法为玩家 {} 找到白骨树结构", player.getName().getString());
      }
   }

   public static BlockPos findNearestBoneTree(ServerPlayerEntity player) {
      try {
         BlockPos playerPos = player.getBlockPos();
         ServerWorld serverWorld = player.getServerWorld();
         int searchRadius = 5000;
         LOGGER.debug("开始搜索白骨树结构，玩家位置: {}, {}, {}", playerPos.getX(), playerPos.getY(), playerPos.getZ());
         BlockPos structurePos = serverWorld.locateStructure(BONE_TREE_STRUCTURE_TAG, playerPos, searchRadius, false);
         if (structurePos != null) {
            int centerY = serverWorld.getTopY(Type.WORLD_SURFACE, structurePos.getX(), structurePos.getZ());
            LOGGER.debug("找到白骨树结构，坐标: {}, {}, {}", structurePos.getX(), centerY, structurePos.getZ());
            return new BlockPos(structurePos.getX(), centerY, structurePos.getZ());
         } else {
            LOGGER.debug("TagKey方式未找到白骨树结构，尝试备选方案");
            return findNearestBoneTreeAlternative(player);
         }
      } catch (Exception e) {
         LOGGER.error("搜索白骨树结构时发生错误", e);
         return null;
      }
   }

   public static BlockPos findNearestBoneTreeAlternative(ServerPlayerEntity player) {
      try {
         BlockPos playerPos = player.getBlockPos();
         ServerWorld serverWorld = player.getServerWorld();
         Registry<Structure> structureRegistry = serverWorld.getRegistryManager().get(RegistryKeys.STRUCTURE);
         Identifier boneTreeId = new Identifier("smfs", "bone_tree");
         Structure boneTreeStructure = (Structure)structureRegistry.get(boneTreeId);
         if (boneTreeStructure == null) {
            LOGGER.warn("白骨树结构未注册: {}", boneTreeId);
            return null;
         }

         RegistryEntry<Structure> structureEntry = structureRegistry.getEntry(boneTreeStructure);
         if (structureEntry == null) {
            LOGGER.warn("无法获取白骨树结构的注册表条目");
            return null;
         }

         int searchRadius = 5000;
         Direct<Structure> structureList = RegistryEntryList.of(new RegistryEntry[]{structureEntry});
         ChunkGenerator chunkGenerator = serverWorld.getChunkManager().getChunkGenerator();
         Pair<BlockPos, RegistryEntry<Structure>> structureResult = chunkGenerator.locateStructure(serverWorld, structureList, playerPos, searchRadius, false);
         if (structureResult != null) {
            BlockPos structurePos = (BlockPos)structureResult.getFirst();
            int centerY = serverWorld.getTopY(Type.WORLD_SURFACE, structurePos.getX(), structurePos.getZ());
            LOGGER.debug("找到白骨树结构，坐标: {}, {}, {}", structurePos.getX(), centerY, structurePos.getZ());
            return new BlockPos(structurePos.getX(), centerY, structurePos.getZ());
         }
      } catch (Exception e) {
         LOGGER.error("搜索白骨树结构时发生错误", e);
      }

      return null;
   }

   public static BlockPos findNearestBoneTreeSimple(ServerPlayerEntity player) {
      try {
         BlockPos playerPos = player.getBlockPos();
         ServerWorld serverWorld = player.getServerWorld();
         int searchRadius = 5000;
         BlockPos structurePos = serverWorld.locateStructure(StructureTags.VILLAGE, playerPos, searchRadius, false);
         if (structurePos != null) {
            int centerY = serverWorld.getTopY(Type.WORLD_SURFACE, structurePos.getX(), structurePos.getZ());
            LOGGER.debug("找到测试结构，坐标: {}, {}, {}", structurePos.getX(), centerY, structurePos.getZ());
            return new BlockPos(structurePos.getX(), centerY, structurePos.getZ());
         }

         LOGGER.debug("未找到测试结构，请检查白骨树结构标签配置");
      } catch (Exception e) {
         LOGGER.error("搜索结构时发生错误", e);
      }

      return null;
   }

   public static boolean hasReceivedCoordinates() {
      return receivedCoordinates.get() != null;
   }
}
